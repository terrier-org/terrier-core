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
 * The Original Code is PhraseScoreModifier.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching.dsms;

import gnu.trove.TIntHashSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.ResultSet;
import org.terrier.structures.postings.ProximityIterablePosting;
import org.terrier.querying.parser.Query;
import org.terrier.querying.parser.SingleTermQuery;
import org.terrier.structures.Index;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.PhraseIterablePosting;

/**
 * This is the class performs the re-scoring for a phrase queries.
 * 
 * If the block distance (window size) is greater than 1 then ProximityIterablePosting
 * is used, else PhraseIterablePosting is used.
 * 
 * @author Richard McCreadie
 *
 */
public class PhraseScoreModifier implements DocumentScoreModifier {

	/** the logger for this class */
    protected static final Logger logger = LoggerFactory.getLogger(PhraseScoreModifier.class);	
	
	/**
	 * The maximum distance, in blocks, that is allowed between the phrase
	 * terms. The default value of one corresponds to phrase search, while any
	 * higher value enables proximity search.
	 */
	protected int blockDistance = 1;
	
	/** A list of the strings of the phrase terms. */
	protected List<Query> phraseTerms;

	/**
	 * Indicates whether the phrase should appear in the retrieved documents, or
	 * not. The default value is true.
	 */
	protected boolean required = true;

	
	/**
	 * Constructs a phrase score modifier for a given set of query terms.
	 * 
	 * @param pTerms
	 *            ArrayList the terms that make up the query.
	 */
	public PhraseScoreModifier(List<Query> pTerms) {
		phraseTerms = pTerms;
	}

	/**
	 * Constructs a phrase score modifier for a given set of query terms and the
	 * allowed distance between them.
	 * 
	 * @param pTerms
	 *            ArrayList the terms that make up the query.
	 * @param bDist
	 *            int the allowed distance between phrase terms.
	 */
	public PhraseScoreModifier(List<Query> pTerms, int bDist) {
		phraseTerms = pTerms;
		blockDistance = bDist;
	}

	/**
	 * Constructs a phrase score modifier for a given set of query terms.
	 * 
	 * @param pTerms
	 *            ArrayList the terms that make up the query.
	 * @param r
	 *            boolean indicates whether the phrase is required.
	 */
	public PhraseScoreModifier(List<Query> pTerms, boolean r) {
		this(pTerms);
		required = r;
	}

	/**
	 * Constructs a phrase score modifier for a given set of query terms,
	 * whether they are required to appear in a document, and the allowed
	 * distance between the phrase terms.
	 * 
	 * @param pTerms
	 *            ArrayList the terms that make up the query.
	 * @param r
	 *            boolean indicates whether the phrase is required.
	 * @param bDist
	 *            int the allowed distance between the phrase terms.
	 */
	public PhraseScoreModifier(List<Query> pTerms, boolean r, int bDist) {
		this(pTerms, bDist);
		required = r;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean modifyScores(Index index, MatchingQueryTerms terms,
			ResultSet resultSet) 
	{
		PostingIndex<Pointer> invIndex = (PostingIndex<Pointer>) index.getInvertedIndex();
		int[] docids = resultSet.getDocids();
		int[] docidsAsc = new int[docids.length];
		System.arraycopy(docids, 0, docidsAsc, 0, docids.length);
		Arrays.sort(docidsAsc);
		final int phraseLength = phraseTerms.size();
		Pointer[] ps = new Pointer[phraseLength];
		TIntHashSet matchedPhrase = new TIntHashSet();
		try{
			for (int i = 0; i < phraseLength; i++) {
				String t = ((SingleTermQuery) phraseTerms.get(i)).getTerm();
				if (terms.getStatistics(t) == null)
				{
					LexiconEntry le = index.getLexicon().getLexiconEntry(t);
					if (le == null)
						continue;
					terms.setTermProperty(t, le);
				}
				ps[i] = (Pointer) terms.getStatistics(t);
			}
						
			IterablePosting phrase = null;
			try{
				if (blockDistance > 1)
				{
					phrase = ProximityIterablePosting.createProximityPostingList(ps, invIndex, blockDistance);
					
				} else {
					
					phrase = PhraseIterablePosting.createPhrasePostingList(ps, invIndex, false);
				} 
			} catch (ClassCastException cce) {
				throw new RuntimeException("Index does not have positions enabled - re-index with block.indexing=true", cce);
			}
			
			int foundDocid = phrase.next();
			if (foundDocid != -1)
				for(int targetDocid : docidsAsc)
				{
					if (logger.isDebugEnabled())
						logger.debug("found=" + foundDocid + " target="+ targetDocid);
					
					if(foundDocid < targetDocid)
						foundDocid = phrase.next(targetDocid);
					if (logger.isDebugEnabled())
						logger.debug("found=" + foundDocid + " target="+ targetDocid);
					
					if (foundDocid == targetDocid)
					{
						if (logger.isDebugEnabled())
							logger.debug("match found for " + targetDocid);
						matchedPhrase.add(targetDocid);
					}
					else if (foundDocid == IterablePosting.EOL)
					{
						break;
					}
				}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		boolean alteredResults = false;
		final double[] scores = resultSet.getScores();
		for(int i=0;i<docids.length;i++)
		{
			if (matchedPhrase.contains(docids[i]))
			{
				if (! required)
				{
					scores[i] = Double.NEGATIVE_INFINITY;
					alteredResults = true;
				}
			}
			else if (required) {
				scores[i] = Double.NEGATIVE_INFINITY;
				alteredResults = true;
			}
		}		
		return alteredResults;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	/** Clones this DSM. Note that phraseTerms is shallow copied, because Strings are immutable */
	public Object clone() {
		return (Object)new PhraseScoreModifier(new ArrayList<Query>(phraseTerms), required, blockDistance);
	}

}