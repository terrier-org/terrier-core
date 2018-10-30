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
 * The Original Code is SingleTermOp.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.matching.matchops;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.FieldOnlyIterablePosting;
import org.terrier.structures.postings.IterablePosting;

/** This class implements a single indexing token as a matching op.
 * @since 5.0
 */
public class SingleTermOp extends Operator {

	private static final long serialVersionUID = 1L;
	
	
	protected static final Logger logger = LoggerFactory.getLogger(SingleTermOp.class);
	
	String queryTerm;
	String field = null;
	
	public SingleTermOp(String t)
	{
		queryTerm = t;
	}
	
	public SingleTermOp(String t, String f)
	{
		queryTerm = t;
		this.field = f;
	}
	
	public String getTerm() {
		return queryTerm;
	}
	
	public void setTerm(String newTerm) {
		queryTerm = newTerm;
	}
	public void setField(String newField) {
		field = newField;
	}
	
	@Override
	public String toString() {
		if (field == null)
			return queryTerm;
		return queryTerm + '.'  + field;
	}
	
	public SingleTermOp clone()
	{
		SingleTermOp rtr = (SingleTermOp) super.clone();
		rtr.queryTerm = queryTerm; //strings are immutable
		return rtr;
	}
	
	public Pair<EntryStatistics,IterablePosting> getPostingIterator(Index index) throws IOException
	{
		Lexicon<String> lexicon = index.getLexicon();
		LexiconEntry t = lexicon.getLexiconEntry(queryTerm);
		PostingIndex<?> invertedIndex = index.getInvertedIndex();
		if (t == null) {
			logger.debug("Term Not Found: " + queryTerm);
			//previousTerm = false;	
			return Pair.of((EntryStatistics) null, (IterablePosting) null);
		}
			
		IterablePosting postingList = invertedIndex.getPostings((Pointer) t);
		if (field != null)
		{
			int fieldId = IndexUtil.getFieldId(index, "inverted", field);
			if (fieldId == -1)
				throw new IOException("Unknown field " + field);
			postingList = new FieldOnlyIterablePosting(postingList, fieldId);
			//TODO do we correct field stats
		}
		
		//slight hack: we will adjust the max tf if we have additional knowledge of it.
		if (t.getMaxFrequencyInDocuments() == Integer.MAX_VALUE && index.hasIndexStructure("maxtf"))
		{
			@SuppressWarnings("unchecked")
			List<Integer> maxTFStructure = (List<Integer>) index.getIndexStructure("maxtf");
			if (maxTFStructure != null)
			{
				t.setMaxFrequencyInDocuments(maxTFStructure.get(t.getTermId()));
			}
		}
		return Pair.of((EntryStatistics) t, postingList);
	}
	
	@Override
	public MatchingEntry getMatcher(
			MatchingQueryTerms.QueryTermProperties qtp,
			Index index,
			Lexicon<String> lexicon,
			PostingIndex<Pointer> invertedIndex, 
			CollectionStatistics collectionStatistics) throws IOException {
		
		WeightingModel[] wmodels = qtp.termModels.toArray(new WeightingModel[0]);
		EntryStatistics entryStats = qtp.stats;
		
		Pair<EntryStatistics,IterablePosting> pair = getPostingIterator(index);
		EntryStatistics t = pair.getLeft();
		IterablePosting postingList = pair.getRight();
		
		if (t == null) {
			logger.debug("Term Not Found: " + queryTerm);
			return null;
		} else if (IGNORE_LOW_IDF_TERMS && collectionStatistics.getNumberOfDocuments() < t.getFrequency()) {
			logger.warn("query term " + queryTerm + " has low idf - ignored from scoring.");
			//previousTerm = false;
			return null;
		}
		if (entryStats == null)
			qtp.stats = entryStats = t;
		for (WeightingModel w : wmodels)
		{
			w.setEntryStatistics(entryStats);
			w.setKeyFrequency(qtp.weight);
			w.setCollectionStatistics(collectionStatistics);
			IndexUtil.configure(index, w);
			w.prepare();			
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Term " + queryTerm + " field "+field+" stats " + entryStats.toString() + " weight " + qtp.weight);
		
		MatchingEntry.Requirement required = MatchingEntry.Requirement.UNKNOWN;
		if (qtp.required != null && qtp.required)
			required = MatchingEntry.Requirement.REQUIRED;
		if (qtp.required != null && ! qtp.required)
			required = MatchingEntry.Requirement.NEG_REQUIRED;
		
		return new MatchingEntry(postingList, 
				entryStats, qtp.weight, wmodels, required, qtp.tags);
	}

	public String getField() {
		return this.field;
	}

}
