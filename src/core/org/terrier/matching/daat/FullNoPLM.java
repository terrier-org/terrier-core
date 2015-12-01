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
package org.terrier.matching.daat;

import it.unimi.dsi.fastutil.longs.LongHeapPriorityQueue;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;

import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Queue;

import org.terrier.matching.BaseMatching;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.ResultSet;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.Index;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
/**
 * Performs the matching of documents with a query, in a document-at-a-time (DAAT)
 * manner. In particular, the posting lists for all query terms are processed
 * in parallel (but without threads). In comparision to TAAT matching, this
 * reduces the memory consumption during matching, as documents which will
 * not make the final retrieved set are discarded.
 * After matching, the document score modifiers are applied if necessary.
 * Documents are matched in a document-at-a-time fashion.
 * @author Nicola Tonellotto and Craig Macdonald
 * @see org.terrier.matching.Matching
 * @see org.terrier.matching.taat.Full
 */
public class FullNoPLM extends BaseMatching
{
	/** Create a new Matching instance based on the specified index */
	public FullNoPLM(Index index) 
	{
		super(index);
	}	
	
	/** {@inheritDoc} */
	@Override
	public ResultSet match(String queryNumber, MatchingQueryTerms queryTerms) throws IOException 
	{
		// The first step is to initialise the arrays of scores and document ids.
		initialise(queryTerms);

		
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
		// longs are kept, as these contain both the docid (high byte)
		// and the corresponding query term array index (low byte)
        final LongPriorityQueue postingHeap = new LongHeapPriorityQueue();
		
		final int queryLength = queryTermsToMatchList.size();
		// The posting list iterator array (one per term) and initialization
		IterablePosting postingListArray[] = new IterablePosting[queryLength];
        for (int i = 0; i < queryLength; i++) {
			LexiconEntry 					lexiconEntry = queryTermsToMatchList.get(i).getValue();
			if(logger.isDebugEnabled()) logger.debug((i + 1) + ": " + queryTermStrings[i].trim() + " with " + lexiconEntry.getDocumentFrequency() + " documents (TF is " + lexiconEntry.getFrequency() + ").");
			postingListArray[i] = invertedIndex.getPostings((BitIndexPointer)lexiconEntry);
			postingListArray[i].next();
			long docid = postingListArray[i].getId();
			assert(docid != -1);
			postingHeap.enqueue((docid << 32) + i);
		}
        boolean targetResultSetSizeReached = false;
        final Queue<CandidateResult> candidateResultList = new PriorityQueue<CandidateResult>();
        int currentDocId = selectMinimumDocId(postingHeap);
        IterablePosting currentPosting = null;
        double threshold = 0.0d;
        //int scored = 0;
        
        //while not end of all posting lists
        while (currentDocId != -1)  {
            // We create a new candidate for the doc id considered
            CandidateResult currentCandidate = new CandidateResult(currentDocId);
            
            int currentPostingListIndex = (int) (postingHeap.firstLong() & 0xFFFF), nextDocid;
            //System.err.println("currentDocid="+currentDocId+" currentPostingListIndex="+currentPostingListIndex);
            currentPosting = postingListArray[currentPostingListIndex]; 
            //scored++;
            do {
            	assignScore(currentPostingListIndex, wm[currentPostingListIndex], currentCandidate, currentPosting);
            	long newDocid = postingListArray[currentPostingListIndex].next();
            	postingHeap.dequeueLong();
                if (newDocid != IterablePosting.EOL)
                    postingHeap.enqueue((newDocid << 32) + currentPostingListIndex);
                else if (postingHeap.isEmpty())
                    break;
                long elem = postingHeap.firstLong();
                currentPostingListIndex = (int) (elem & 0xFFFF);
                currentPosting = postingListArray[currentPostingListIndex];
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
        		threshold = candidateResultList.peek().getScore();
        	}
            currentDocId = selectMinimumDocId(postingHeap);
        }
        
       // System.err.println("Scored " + scored + " documents");
               		
        // Fifth, we build the result set
        resultSet = new CandidateResultSet(candidateResultList);
        numberOfRetrievedDocuments = resultSet.getScores().length;
        finalise(queryTerms);
		return resultSet;
	}

	/** assign the score for this posting to this candidate result.
	 * @param i which query term index this represents
	 * @param wModels weighting models for this term
	 * @param cc the candidate result object for this document
	 * @param posting the posting for this query term
	 * @throws IOException
	 */
	private void assignScore(int i, final WeightingModel[] wModels, CandidateResult cc, final Posting posting) throws IOException
	{
		cc.updateScore(scoreIt(wModels, posting));
		cc.updateOccurrence((i < 16) ? (short)(1 << i) : 0);
	}
	
	/** calculate the score for this posting using the specified weighting models
	 * @param wModels weighting models for this term
	 * @param posting the posting for the current term
	 */
	protected double scoreIt(final WeightingModel[] wModels, final Posting posting)
	{
		double score = 0.0;
		for (WeightingModel wmodel: wModels)
			score += wmodel.score(posting);
		return score;
	}
	
	/** returns the docid of the lowest posting */
	protected final int selectMinimumDocId(final LongPriorityQueue postingHeap)
    {
        return (postingHeap.isEmpty()) ? -1 : (int) (postingHeap.firstLong() >>> 32);
    }


	@Override
	public String getInfo() {
		return "daat.FullNoPLM";
	}

}
