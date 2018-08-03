/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is MultiTermOp.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.matching.matchops;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.MatchingQueryTerms.QueryTermProperties;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Lexicon;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;

/** this is an abstract operator, which implements common 
 * functionalities for operators that consist of multiple terms. */
public abstract class MultiTermOp extends Operator {

	private static final long serialVersionUID = 1L;
	protected static final Logger logger = LoggerFactory.getLogger(MultiTermOp.class);
	Operator[] terms;
	public MultiTermOp(String[] ts)
	{
		this(getSingleTerms(ts));	
	}
	
	public MultiTermOp(Operator[] _ts)
	{
		this.terms = _ts;
	}
	
	public Operator[] getConstituents() {
		return terms;
	}
	
	static Operator[] getSingleTerms(String[] ts) {
		 Operator[] rtr = new Operator[ts.length];
		 for(int i=0;i<ts.length;i++)
			 rtr[i] = new SingleTermOp(ts[i]);
		 return rtr;
	}
	
	/** merges several EntryStatistics for a single effective term simply by adding */
	public static EntryStatistics addStatistics(EntryStatistics[] entryStats)
	{
		if (entryStats == null)
			return null;
		EntryStatistics rtr = entryStats[0];
		for(int i=1;i<entryStats.length;i++)
			rtr.add(entryStats[i]);
		return rtr;
	}
	
	protected EntryStatistics mergeStatistics(EntryStatistics[] entryStats, CollectionStatistics collStats)
	{
		return addStatistics(entryStats);
	}
	
	protected abstract IterablePosting createFinalPostingIterator(
			List<IterablePosting> postings,
			List<EntryStatistics> pointers) throws IOException;
	
	@Override
	public Pair<EntryStatistics,IterablePosting> getPostingIterator(Index index) throws IOException
	{
		List<EntryStatistics> _le = new ArrayList<EntryStatistics>(terms.length);
		List<IterablePosting> _joinedPostings = new ArrayList<IterablePosting>(terms.length);
		for(Operator ts : terms) {
			Pair<EntryStatistics,IterablePosting> pair = ts.getPostingIterator(index);
			if (pair == null || pair.getLeft() == null)
			{
				logger.debug("Component term Not Found: " + ts);
			} else if (IGNORE_LOW_IDF_TERMS && index.getCollectionStatistics().getNumberOfDocuments() < pair.getKey().getFrequency()) {
				logger.warn("query term " + ts + " has low idf - ignored from scoring.");
			} else {
				//assert pair.getLeft() != null : "query term " + ts + " has null entrystatistics?";
				_le.add(pair.getLeft());
				_joinedPostings.add(pair.getRight());
			}			
		}
		
		if (_le.size() == 0)
		{
			//TODO consider if we should return an empty posting list iterator instead
			logger.warn("No alternatives matched in " + Arrays.toString(terms));
			return null;
		}
		//TODO: shouldnt collstats be allowed to come from elsewhere?
		EntryStatistics entryStats = mergeStatistics(_le.toArray(new EntryStatistics[_le.size()]), index.getCollectionStatistics());
		
		IterablePosting ip = createFinalPostingIterator(_joinedPostings, _le);
		return Pair.of(entryStats, ip);
	}
	
	@Override
	public MatchingEntry getMatcher(QueryTermProperties qtp, Index index,
			Lexicon<String> lexicon, PostingIndex<Pointer> invertedIndex,
			CollectionStatistics collectionStatistics) throws IOException 
	{
		WeightingModel[] wmodels = qtp.termModels.toArray(new WeightingModel[0]);
		if (wmodels.length == 0) {
			logger.warn("No weighting models for multi-term query group "+toString()+" , skipping scoring");
			return null;
		}
		EntryStatistics entryStats = qtp.stats;
		
		Pair<EntryStatistics,IterablePosting> pair = this.getPostingIterator(index);
		if (pair == null)
			return null;
		
		if (entryStats == null)
			qtp.stats = entryStats = pair.getKey();
		if (logger.isDebugEnabled())
			logger.debug(this.getClass().getSimpleName() + " term " + Arrays.toString(terms) + " stats " + entryStats.toString() + " weight " + qtp.weight);
		for (WeightingModel w : wmodels)
		{
			w.setEntryStatistics(entryStats);
			w.setKeyFrequency(qtp.weight);
			w.setCollectionStatistics(collectionStatistics);
			IndexUtil.configure(index, w);
			w.prepare();			
		}
		MatchingEntry.Requirement required = MatchingEntry.Requirement.UNKNOWN;
		if (qtp.required != null && qtp.required)
			required = MatchingEntry.Requirement.REQUIRED;
		if (qtp.required != null && ! qtp.required)
			required = MatchingEntry.Requirement.NEG_REQUIRED;
		return new MatchingEntry(pair.getRight(), entryStats, qtp.weight, wmodels, required, qtp.tags);
	}
	
	public MultiTermOp clone()
	{
		MultiTermOp rtr = (MultiTermOp) super.clone();
		rtr.terms = new Operator[this.terms.length];
		int i=0;
		for (Operator op : terms)
		{
			rtr.terms[i++] = op.clone();
		}
		return rtr;
	}
	
}
