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
 * The Original Code is TestFatCandidateResultSet.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */

package org.terrier.fat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.terrier.matching.FatResultSet;
import org.terrier.matching.FatUtils;
import org.terrier.matching.daat.CandidateResult;
import org.terrier.matching.daat.FatCandidateResult;
import org.terrier.matching.daat.FatCandidateResultSet;
import org.terrier.structures.BasicLexiconEntry;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.postings.BasicPostingImpl;


public class TestFatCandidateResultSet {

	@Test public void testOneDocumentOnePosting() throws Exception
	{
		final FatCandidateResult result = new FatCandidateResult(20, 1);
		result.updateScore(5.3d);
		result.setPosting(0, new BasicPostingImpl(20, 5));
		final List<CandidateResult> l = new ArrayList<CandidateResult>();
		l.add(result);
		final FatCandidateResultSet input = new FatCandidateResultSet(
				l, 
				new CollectionStatistics(5, 5, 5, 5, new long[0]),
				new String[]{"a"}, 
				new EntryStatistics[]{new BasicLexiconEntry(5, 20, 25)},
				new double[]{10}
				);
		
		FatResultSet output = FatUtils.recreate(input);

		String name = "input";
		for(FatResultSet rs : new FatResultSet[]{input, output})
		{
			System.err.println("Iteration: " + name);
			//meta
			assertEquals(1, rs.getResultSize());
			
			//1 OF: collection statistics
			assertNotNull(rs.getCollectionStatistics());
			assertEquals(5, rs.getCollectionStatistics().getNumberOfDocuments());
			assertEquals(5, rs.getCollectionStatistics().getNumberOfPointers());
			assertEquals(5, rs.getCollectionStatistics().getNumberOfTokens());
			assertEquals(5, rs.getCollectionStatistics().getNumberOfUniqueTerms());
			
			
			//T OF: query terms etc
			assertNotNull(rs.getQueryTerms());
			assertEquals(1, rs.getQueryTerms().length);
			assertEquals("a", rs.getQueryTerms()[0]);
			
			assertNotNull(rs.getKeyFrequencies());
			assertEquals(1, rs.getKeyFrequencies().length);
			assertEquals(10, rs.getKeyFrequencies()[0], 0.0d);
			
			assertNotNull(rs.getEntryStatistics());
			assertEquals(1, rs.getEntryStatistics().length);
			assertNotNull(rs.getEntryStatistics()[0]);
			assertEquals(5, rs.getEntryStatistics()[0].getTermId());
			assertEquals(20, rs.getEntryStatistics()[0].getDocumentFrequency());
			assertEquals(25, rs.getEntryStatistics()[0].getFrequency());		
			
			//D OF: docids
			assertNotNull(rs.getDocids());
			assertEquals(1, rs.getDocids().length);
			assertEquals(20, rs.getDocids()[0]);
	
			//scores
			assertNotNull(rs.getScores());
			assertEquals(1, rs.getScores().length);
			assertEquals(5.3d, rs.getScores()[0], 0.0d);
			
			//occurrences
			assertNotNull(rs.getOccurrences());
			assertEquals(1, rs.getOccurrences().length);
	
			//postings
			assertNotNull(rs.getPostings());
			assertEquals(1, rs.getPostings().length);
			assertEquals(1, rs.getPostings()[0].length);
			assertEquals(20, rs.getPostings()[0][0].getId());
			assertEquals(5, rs.getPostings()[0][0].getFrequency());
			
			
			name = "output";
		}
	}
	
	//TODO: test multiple documents with fields
	//TODO: test single document with fields
	//TODO: test multiple documents without fields
	
}
