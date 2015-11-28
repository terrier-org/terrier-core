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
package org.terrier.matching.daat;

import it.unimi.dsi.fastutil.longs.LongHeapPriorityQueue;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;

import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Queue;

import org.terrier.matching.BaseMatching;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.PostingListManager;
import org.terrier.matching.ResultSet;
import org.terrier.structures.Index;
import org.terrier.structures.postings.IterablePosting;
/**
 * Performs the matching of documents with a query, by first assigning scores to documents for each query term
 * and modifying these scores with the appropriate modifiers.
 * Documents are matched in a document-at-a-time fashion.
 * In particular, the posting lists for all query terms are processed
 * in parallel (but without threads). In comparison to TAAT matching, this
 * reduces the memory consumption during matching, as documents which will
 * not make the final retrieved set are discarded.
 * After matching, the document score modifiers are applied if necessary.
 * This Matching strategy uses the PostingListManager for opening
 * and scoring postings.
 * 
 * @author Nicola Tonellotto and Craig Macdonald
 * @see org.terrier.matching.PostingListManager
 * @since 3.5
 */
public class Full extends BaseMatching
{
	/** Create a new Matching instance based on the specified index */
	public Full(Index index) 
	{
		super(index);
	}
	
	/** posting list manager opens and scores postings */
	PostingListManager plm;
	
	@Override
	protected void initialisePostings(MatchingQueryTerms queryTerms) {
		
	}	
	
	/** {@inheritDoc} */
	@SuppressWarnings("resource") //IterablePosting need not be closed
	@Override
	public ResultSet match(String queryNumber, MatchingQueryTerms queryTerms) throws IOException 
	{
		// The first step is to initialise the arrays of scores and document ids.
		initialise(queryTerms);
		plm = new PostingListManager(index, super.collectionStatistics, queryTerms);
		plm.prepare(true);
		
		// Check whether we need to match an empty query. If so, then return the existing result set.
		String[] queryTermStrings = queryTerms.getTerms();
		if (MATCH_EMPTY_QUERY && queryTermStrings.length == 0) {
			resultSet.setExactResultSize(collectionStatistics.getNumberOfDocuments());
			resultSet.setResultSize(collectionStatistics.getNumberOfDocuments());
			return resultSet;
		}
		
		//the number of documents with non-zero score.
		numberOfRetrievedDocuments = 0;
		
		// The posting list min heap for minimum selection
        LongPriorityQueue postingHeap = new LongHeapPriorityQueue();
		
		// The posting list iterator array (one per term) and initialization
		for (int i = 0; i < plm.size(); i++) {
			long docid = plm.getPosting(i).getId();
			assert(docid != IterablePosting.EOL);
			postingHeap.enqueue((docid << 32) + i);
		}
        boolean targetResultSetSizeReached = false;
        Queue<CandidateResult> candidateResultList = new PriorityQueue<CandidateResult>();
        int currentDocId = selectMinimumDocId(postingHeap);
        IterablePosting currentPosting = null;
        double threshold = 0.0d;
        //int scored = 0;
        
        while (currentDocId != -1)  {
            // We create a new candidate for the doc id considered
            CandidateResult currentCandidate = makeCandidateResult(currentDocId);
            
            int currentPostingListIndex = (int) (postingHeap.firstLong() & 0xFFFF), nextDocid;
            //System.err.println("currentDocid="+currentDocId+" currentPostingListIndex="+currentPostingListIndex);
            currentPosting = plm.getPosting(currentPostingListIndex); 
            //scored++;
            do {
            	assignScore(currentPostingListIndex, currentCandidate);
            	//assignScore(currentPostingListIndex, wm[currentPostingListIndex], currentCandidate, currentPosting);
            	long newDocid = currentPosting.next();
            	postingHeap.dequeueLong();
                if (newDocid != IterablePosting.EOL)
                    postingHeap.enqueue((newDocid << 32) + currentPostingListIndex);
                else if (postingHeap.isEmpty())
                    break;
                long elem = postingHeap.firstLong();
                currentPostingListIndex = (int) (elem & 0xFFFF);
                currentPosting = plm.getPosting(currentPostingListIndex);
                nextDocid = (int) (elem >>> 32);
            } while (nextDocid == currentDocId);
            
            
            
            if ((! targetResultSetSizeReached) || currentCandidate.getScore() > threshold) {
            	//System.err.println("New document " + currentCandidate.getDocId() + " with score " + currentCandidate.getScore() + " passes threshold of " + threshold);
        		candidateResultList.add(currentCandidate);
        		if (RETRIEVED_SET_SIZE != 0 && candidateResultList.size() == RETRIEVED_SET_SIZE + 1)
        		{
        			targetResultSetSizeReached = true;
        			candidateResultList.poll();
        			//System.err.println("Removing document with score " + candidateResultList.poll().getScore());
        		}
        		//System.err.println("Now have " + candidateResultList.size() + " retrieved docs");
        		threshold = candidateResultList.peek().getScore();
        	}
            currentDocId = selectMinimumDocId(postingHeap);
        }
        
        // System.err.println("Scored " + scored + " documents");
        plm.close();
        
        // Fifth, we build the result set
        resultSet = makeResultSet(candidateResultList);
        numberOfRetrievedDocuments = resultSet.getScores().length;
        finalise(queryTerms);
		return resultSet;
	}

	protected CandidateResultSet makeResultSet(
			Queue<CandidateResult> candidateResultList) {
		return new CandidateResultSet(candidateResultList);
	}

	protected CandidateResult makeCandidateResult(int currentDocId) {
		return new CandidateResult(currentDocId);
	}
	
	/** assign the score for this posting to this candidate result.
	 * @param i which query term index this represents
	 * @param cc the candidate result object for this document
	 * @throws IOException
	 */
	protected void assignScore(final int i, CandidateResult cc) throws IOException
    {
        cc.updateScore(plm.score(i));
        cc.updateOccurrence((i < 16) ? (short)(1 << i) : 0);
    }
	
	/** returns the docid of the lowest posting */
	protected final int selectMinimumDocId(final LongPriorityQueue postingHeap)
    {
        return (postingHeap.isEmpty()) ? -1 : (int) (postingHeap.firstLong() >>> 32);
    }

	/** {@inheritDoc} */
	@Override
	public String getInfo() {
		return "daat.Full";
	}

}
