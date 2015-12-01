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
 * The Original Code is FullNoPLM.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Nicola Tonellotto (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   
 */
package org.terrier.matching.taat;

import java.io.IOException;

import org.terrier.matching.AccumulatorResultSet;
import org.terrier.matching.BaseMatching;
import org.terrier.matching.CollectionResultSet;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.ResultSet;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.Index;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.postings.IterablePosting;

/** An exhaustive term-at-a-time approach for matching documents to a query.
 * Posting lists for terms are processed in a sequential manner, i.e. 
 * the postings for a given query terms are scored before scoring commences
 * on the next term in the query. In this class, all postings for all query
 * terms are scored. After matching, the document score modifiers are applied if necessary.
 * <p>
 * The nature of scoring by taat.Full means that it can consume large
 * amounts of memory for scoring large indices. If this is a problem or
 * concern, use daat.Full instead.
 * @author Nicola Tonellotto, Craig Macdonald
 * @since 3.0
 * @see org.terrier.matching.daat.Full
 * @see org.terrier.matching.Matching
 */
public class FullNoPLM extends BaseMatching
{
	/** number of documents to warn about inefficient TAAT */
	static final int WARN_DOCS = 4000000;
	
	/** Create a new Matching instance based on the specified index */
	public FullNoPLM(Index index) 
	{
		super(index);
		if (this.getClass() == FullNoPLM.class) 
		{
			logger.warn(this.getClass().getName() + " is not suitable for indices with large numbers of documents (> "+WARN_DOCS+") "
					+"- consider using org.terrier.matching.daat.FullNoPLM");
		}
		
		resultSet = new AccumulatorResultSet(collectionStatistics.getNumberOfDocuments());		
	}

	/** {@inheritDoc} */
	public String getInfo() 
	{
		return "taat.FullNoPLM";
	}
	
	/** {@inheritDoc} */
	public ResultSet match(String queryNumber, MatchingQueryTerms queryTerms) throws IOException 
	{
		final long starttime = System.currentTimeMillis();
		initialise(queryTerms);
		// Check whether we need to match an empty query. If so, then return the existing result set.
		// String[] queryTermStrings = queryTerms.getTerms();
		if (MATCH_EMPTY_QUERY && queryTermsToMatchList.size() == 0) {
			resultSet = new CollectionResultSet(collectionStatistics.getNumberOfDocuments());
			resultSet.setExactResultSize(collectionStatistics.getNumberOfDocuments());
			resultSet.setResultSize(collectionStatistics.getNumberOfDocuments());
			return resultSet;
		}
						
		int queryLength = queryTermsToMatchList.size();
		// The posting list iterator from the inverted file
		IterablePosting postings;		
		for (int i = 0; i < queryLength; i++) 
		{
			LexiconEntry lexiconEntry = queryTermsToMatchList.get(i).getValue();
			postings = invertedIndex.getPostings(lexiconEntry);
			assignScores(i, wm[i], (AccumulatorResultSet) resultSet, postings);
		}

		resultSet.initialise();
		this.numberOfRetrievedDocuments = resultSet.getExactResultSize();
		finalise(queryTerms);
		if (logger.isDebugEnabled())
			logger.debug("Time to match results: " + (System.currentTimeMillis() - starttime) + "ms");
		return resultSet;
	}
	
	protected void assignScores(int i, final WeightingModel[] wModels, AccumulatorResultSet rs, final IterablePosting postings) throws IOException
	{
		int docid;
		double score;
		
		short mask = 0;
		if (i < 16)
			mask = (short)(1 << i);
		
		while (postings.next() != IterablePosting.EOL)
		{
			score = 0.0; docid = postings.getId();

			for (WeightingModel wmodel: wModels)
				score += wmodel.score(postings);
			//logger.info("Docid=" + docid + " score=" + score);
			if ((!rs.scoresMap.contains(docid)) && (score > 0.0d))
				numberOfRetrievedDocuments++;
			else if ((rs.scoresMap.contains(docid)) && (score < 0.0d))
				numberOfRetrievedDocuments--;

			rs.scoresMap.adjustOrPutValue(docid, score, score);
			rs.occurrencesMap.put(docid, (short)(rs.occurrencesMap.get(docid) | mask));
		}
	}
}
