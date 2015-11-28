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
 * The Original Code is TestFatFullMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 *   Eric Sutherland
 */

package org.terrier.fat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.matching.FatResultSet;
import org.terrier.matching.FatUtils;
import org.terrier.matching.Matching;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.ResultSet;
import org.terrier.matching.TestMatching;
import org.terrier.matching.daat.FatCandidateResultSet;
import org.terrier.matching.models.DLH13;
import org.terrier.structures.Index;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.utility.ApplicationSetup;

public class TestFatFullMatching extends TestMatching {

	@Override
	protected Matching makeMatching(Index i) {
		return new org.terrier.matching.daat.FatFull(i);
	}
	
	ResultSet _testSingleDocumentIndexMatchingFields() throws Exception
	{
		ApplicationSetup.setProperty("FieldTags.process", "TITLE,ELSE");
		Index index = IndexTestUtils.makeIndexFields(
				new String[]{"doc1"}, 
				new String[]{"The <TITLE>quick brown</TITLE> fox jumps over the lazy dog"});
		System.err.println("_testSingleDocumentIndexMatchingFields: " + index.toString());
		assertNotNull(index);
		assertEquals(1, index.getCollectionStatistics().getNumberOfDocuments());
		assertEquals(2, index.getCollectionStatistics().getNumberOfFields());
		assertEquals(2, index.getCollectionStatistics().getFieldTokens()[0]);
		assertEquals(7, index.getCollectionStatistics().getFieldTokens()[1]);
		assertEquals(9, index.getDocumentIndex().getDocumentLength(0));
		Matching matching = makeMatching(index);
		assertNotNull(matching);
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setTermProperty("quick", 1);
		mqt.setDefaultTermWeightingModel(new DLH13());
		ResultSet rs = matching.match("query1", mqt);
		assertNotNull(rs);
		assertEquals(1, rs.getResultSize());
		assertEquals(0, rs.getDocids()[0]);
		assertTrue(rs.getScores()[0] > 0);
		
		//now check FatIndex.
		
		Index fatIndex = FatUtils.makeIndex((FatResultSet)rs);
		assertEquals( index.getCollectionStatistics().getNumberOfDocuments(), fatIndex.getCollectionStatistics().getNumberOfDocuments());
		assertEquals(index.getCollectionStatistics().getNumberOfFields(), fatIndex.getCollectionStatistics().getNumberOfFields());
		assertEquals(index.getCollectionStatistics().getFieldTokens()[0], fatIndex.getCollectionStatistics().getFieldTokens()[0]);
		assertEquals(index.getCollectionStatistics().getFieldTokens()[1], fatIndex.getCollectionStatistics().getFieldTokens()[1]);
		//assertEquals(index.getDocumentIndex().getDocumentLength(0), fatIndex.getDocumentIndex().getDocumentLength(0));
		assertNotNull(fatIndex.getLexicon());
		LexiconEntry le = fatIndex.getLexicon().getLexiconEntry("quick");
		assertNotNull(le);
		assertEquals(1, le.getFrequency());
		PostingIndex<?> inv = fatIndex.getInvertedIndex();
		assertNotNull(inv);
		IterablePosting ip = inv.getPostings(le);
		assertNotNull(ip);
		assertEquals(0,ip.next());
		assertEquals(1,ip.getFrequency());
		assertEquals(-1,ip.next());
				
		return rs;
	}
	
	@Test public void testSingleDocumentIndexMatchingFields() throws Exception
	{
		ResultSet rs = _testSingleDocumentIndexMatchingFields();
		//get postings from ResultSet for first ranked document
		assertTrue(rs instanceof FatCandidateResultSet);
		Posting[] postings = ((FatCandidateResultSet)rs).getPostings()[0];
		assertEquals(1, postings.length);
		assertEquals(0, postings[0].getId());
		assertEquals(1, postings[0].getFrequency());
		assertEquals(9, postings[0].getDocumentLength());
		FieldPosting p = (FieldPosting)postings[0];
		assertEquals(1, p.getFieldFrequencies()[0]);
		assertEquals(0, p.getFieldFrequencies()[1]);
		assertEquals(2, p.getFieldLengths()[0]);
		assertEquals(7, p.getFieldLengths()[1]);
	}
	
	
	@Test public void testSingleDocumentIndexMatching() throws Exception
	{
		ResultSet rs = super._testSingleDocumentIndexMatching();
		//get postings from ResultSet for first ranked document
		assertTrue(rs instanceof FatCandidateResultSet);
		Posting[] postings = ((FatCandidateResultSet)rs).getPostings()[0];
		assertEquals(1, postings.length);
		assertEquals(0, postings[0].getId());
		assertEquals(1, postings[0].getFrequency());
		assertEquals(9, postings[0].getDocumentLength());
	}
	
	@Test public void testTwoDocumentsIndexMatching() throws Exception
	{
		ResultSet rs = super._testTwoDocumentsIndexMatching();
		assertTrue(rs instanceof FatCandidateResultSet);
		Posting[] postings = ((FatCandidateResultSet)rs).getPostings()[0];
		assertEquals(1, postings.length); //how many in Postings here?
	}
	
	@Test public void testThreeDocumentsSynonymIndexMatching() throws Exception
	{
		ResultSet rs = super._testThreeDocumentsSynonymIndexMatching();
		assertTrue(rs instanceof FatCandidateResultSet);
		Posting[] postings = ((FatCandidateResultSet)rs).getPostings()[0];
		assertEquals(1, postings.length); //how many in Postings here?
	}

	@Test public void testMatchingNonStatisticsOverwrite() throws Exception
	{
		ResultSet rs = super._testMatchingNonStatisticsOverwrite();
		assertTrue(rs instanceof FatCandidateResultSet);
		Posting[] postings = ((FatCandidateResultSet)rs).getPostings()[0];
		assertEquals(1, postings.length);
	}
	
	@Test public void testTwoDocumentsTwoTerms() throws Exception
	{
		ResultSet rs = super._testTwoDocumentsTwoTerms();
		assertTrue(rs instanceof FatCandidateResultSet);
		Posting[] postings = ((FatCandidateResultSet)rs).getPostings()[0];
		assertEquals(2, postings.length);
		assertEquals(1, postings[0].getId());
		assertEquals(1, postings[0].getFrequency());
		assertEquals(8, postings[0].getDocumentLength());
		assertEquals(1, postings[1].getId());
		assertEquals(1, postings[1].getFrequency());
		assertEquals(8, postings[1].getDocumentLength());
		postings = ((FatCandidateResultSet)rs).getPostings()[1];
		assertEquals(2, postings.length);
		assertEquals(0, postings[0].getId());
		assertEquals(1, postings[0].getFrequency());
		assertEquals(9, postings[0].getDocumentLength());
		assertNull(postings[1]);
	}
	
}
