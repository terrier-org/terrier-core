/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - Department of Computing Science
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
 * The Original Code is Full.java.
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
import org.terrier.matching.PostingListManager;
import org.terrier.matching.ResultSet;
import org.terrier.structures.Index;
import org.terrier.structures.postings.IterablePosting;

/** An exhaustive TAAT approach for matching documents to a query.
 * This Matching strategy uses the PostingListManager for opening
 * and scoring postings.
 * @author Nicola Tonellotto, Craig Macdonald
 * @since 3.0
 * @see org.terrier.matching.PostingListManager
 */
public class Full extends BaseMatching
{
	/** number of documents to warn about inefficient TAAT */
	static final int WARN_DOCS = 4000000;
	
	/** Create a new Matching instance based on the specified index */
	public Full(Index index) 
	{
		super(index);
		if (this.getClass() == Full.class) 
		{
			logger.warn(this.getClass().getName() + " is not suitable for indices with large numbers of documents (> "+WARN_DOCS+") "
					+"- consider using org.terrier.matching.daat.Full");
		}
		resultSet = new AccumulatorResultSet(collectionStatistics.getNumberOfDocuments());		
	}

	/** {@inheritDoc} */
	@Override
	public String getInfo() 
	{
		return "taat.Full";
	}

	/** posting list manager opens and scores postings */
	PostingListManager plm;
	
	/** {@inheritDoc} */
	public ResultSet match(String queryNumber, MatchingQueryTerms queryTerms) throws IOException 
	{
		final long starttime = System.currentTimeMillis();
		initialise(queryTerms);
		
		plm = new PostingListManager(index, super.collectionStatistics, queryTerms);
		if (MATCH_EMPTY_QUERY && plm.size() == 0)
		{
			// Check whether we need to match an empty query. If so, then return the existing result set.
			resultSet = new CollectionResultSet(collectionStatistics.getNumberOfDocuments());
			resultSet.setExactResultSize(collectionStatistics.getNumberOfDocuments());
			resultSet.setResultSize(collectionStatistics.getNumberOfDocuments());
			return resultSet;
		}
		//DO NOT prepare the posting lists for TAAT retrieval
		plm.prepare(false);
				
		for(int i=0; i< plm.size(); i++)
		{			
			assignScores(i, (AccumulatorResultSet) resultSet, plm.getPosting(i));
		}

		resultSet.initialise();
		plm.close();
		this.numberOfRetrievedDocuments = resultSet.getExactResultSize();
		finalise(queryTerms);
		if (logger.isDebugEnabled())
			logger.debug("Time to match "+numberOfRetrievedDocuments+" results: " + (System.currentTimeMillis() - starttime) + "ms");
		return resultSet;
	}
	
	protected void assignScores(int i, AccumulatorResultSet rs, final IterablePosting postings) throws IOException
	{
		int docid;
		double score;
		
		short mask = 0;
		if (i < 16)
			mask = (short)(1 << i);
		
		while (postings.next() != IterablePosting.EOL)
		{
			score = plm.score(i);
			docid = postings.getId();
			//logger.info("Docid=" + docid + " score=" + score);
			if ((!rs.scoresMap.contains(docid)) && (score != Double.NEGATIVE_INFINITY))
				numberOfRetrievedDocuments++;
			else if ((rs.scoresMap.contains(docid)) && (score == Double.NEGATIVE_INFINITY))
				numberOfRetrievedDocuments--;

			rs.scoresMap.adjustOrPutValue(docid, score, score);
			rs.occurrencesMap.put(docid, (short)(rs.occurrencesMap.get(docid) | mask));
		}
	}

	@Override
	protected void initialisePostings(MatchingQueryTerms queryTerms) {
		
	}
}
