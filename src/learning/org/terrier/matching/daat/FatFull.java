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
 * The Original Code is FatFull.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Eric Sutherland
 */

package org.terrier.matching.daat;

import java.io.IOException;
import java.util.Queue;

import org.terrier.matching.FatResultSet;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.Posting;
import org.terrier.structures.postings.WritablePosting;


/**
 * A subclass of {@link Full} that creates a {@link FatResultSet}. This class 
 * is a key component of the Fat framework,
 * as described by Macdonald et al., TOIS 2013.
 * 
 * @author Eric Sutherland, Craig Macdonald
 * @since 4.0
 * @see "About Learning Models with Multiple Query Dependent Features. Craig Macdonald, Rodrygo L.T. Santos, Iadh Ounis and Ben He. Transactions on Information Systems. 31(3). 2013. <a href="http://www.dcs.gla.ac.uk/~craigm/publications/macdonald13multquerydf.pdf">[PDF]</a>"
 */
public class FatFull extends Full {

	final boolean fields;
	final int fieldCount;
	
	public FatFull(Index index) {
		super(index);
		fields = super.collectionStatistics.getNumberOfFields() > 0;
		fieldCount = super.collectionStatistics.getNumberOfFields(); 
	}

	@Override
	protected CandidateResult makeCandidateResult(int currentDocId) {
		return new FatCandidateResult(currentDocId, plm.getNumTerms());
	}	
	
	@Override
	protected CandidateResultSet makeResultSet(
			Queue<CandidateResult> candidateResultList) 
	{
		int terms = plm.getNumTerms();
		String[] queryTerms = new String[terms];
		EntryStatistics[] entryStats = new EntryStatistics[terms];
		double[] keyFreqs = new double[terms];
		
		for (int i=0; i<terms; i++) {			
			queryTerms[i] = plm.getTerm(i);
			entryStats[i] = plm.getStatistics(i).getWritableEntryStatistics();
			keyFreqs[i] = plm.getKeyFrequency(i);
			logger.info("term " + queryTerms[i] + " ks="+keyFreqs[i] + " es=" + entryStats[i]);
		}
		
		return new FatCandidateResultSet(candidateResultList, super.collectionStatistics, queryTerms, entryStats, keyFreqs);
	}

	@Override
	protected void assignScore(final int i, final CandidateResult cc) throws IOException {
		
		//update the score as normal
		cc.updateScore(plm.score(i));
        cc.updateOccurrence((i < 16) ? (short)(1 << i) : 0);
        
        //get a deep copy of the posting
        final Posting p = plm.getPosting(i);
        
        //writable postings don't copy or retain document length. Make this not so.
        final WritablePosting wp = p.asWritablePosting();
        assert wp.getId() == cc.getDocId() : "Posting does not have same docid as candidate result";
        
        wp.setDocumentLength(p.getDocumentLength());
        if (fields)
        {
        	final int[] fieldLengths =  ((FieldPosting)p).getFieldLengths();
        	final int[] newFieldLengths = new int[fieldCount];
        	System.arraycopy(fieldLengths, 0, newFieldLengths, 0, fieldCount);
        	//System.err.println(fieldLengths);
        	assert fieldLengths.length == super.collectionStatistics.getNumberOfFields() 
        		: " posting "+p +" for docid " + p.getId() + " has wrong number of fields for length";
        	((FieldPosting)wp).setFieldLengths(newFieldLengths);
        }
        //store somewhere
        ((FatCandidateResult)cc).setPosting(i, wp);
	}
}
