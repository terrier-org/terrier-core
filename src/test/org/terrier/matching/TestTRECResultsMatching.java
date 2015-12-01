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
 * The Original Code is TestTRECResultsMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.matching;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.Writer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.terrier.matching.dsms.ResetScores;
import org.terrier.structures.ArrayMetaIndex;
import org.terrier.structures.Index;
import org.terrier.structures.IndexUtil;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

/**
 * @author Craig Macdonald, Rodrygo Santos
 */
public class TestTRECResultsMatching extends ApplicationSetupBasedTest {

	@Rule public TemporaryFolder folder = new TemporaryFolder();
	
	protected Matching getMatching(String[] rows, String[] docnos) throws Exception
	{
		File tmpFile = folder.newFile("tmp.res");
		Writer w = Files.writeFileWriter(tmpFile);
		for(String row : rows)
			w.append(row + "\n");
		w.close();
		
		Index index = Index.createNewIndex(folder.newFolder("index").toString(), "data");
		index.setIndexProperty("num.Documents", ""+docnos.length);
		IndexUtil.forceStructure(index, "meta", new ArrayMetaIndex(docnos));
		Matching rtr = new TRECResultsMatching(index, tmpFile.toString());
		return rtr;
	}

	@Test public void testMissingQuery() throws Exception
	{
		Matching m = getMatching(new String[]{"2 Q0 doc1 1 1.2 bla"}, new String[]{"doc1"});
		ResultSet r = m.match("1", null);
		assertEquals(0, r.getResultSize());
		int[] docids = r.getDocids();
		assertEquals(0, docids.length);
	}
	
	@Test public void testSingleQuerySingleResult() throws Exception
	{
		Matching m = getMatching(new String[]{"1 Q0 doc1 1 1.2 bla"}, new String[]{"doc1"});
		ResultSet r = m.match("1", null);
		assertEquals(1, r.getResultSize());
		int[] docids = r.getDocids();
		assertEquals(1, docids.length);
		assertEquals(0, docids[0]);
	}
	
	@Test public void testSingleQuerySingleResultWithScores() throws Exception
	{
		ApplicationSetup.setProperty("matching.trecresults.scores", "true");
		Matching m = getMatching(new String[]{"1 Q0 doc1 1 1.2 bla"}, new String[]{"doc1"});
		ResultSet r = m.match("1", null);
		assertEquals(1, r.getResultSize());
		int[] docids = r.getDocids();
		assertEquals(1, docids.length);
		assertEquals(0, docids[0]);
		double[] scores = r.getScores();
		assertEquals(1, scores.length);
		assertEquals(1.2d, scores[0], 0.0d);		
	}
	
	@Test public void testSingleQuerySingleResultWithDSM() throws Exception
	{
		ApplicationSetup.setProperty("matching.dsms", ResetScores.class.getName());
		Matching m = getMatching(new String[]{"1 Q0 doc1 1 1.2 bla"}, new String[]{"doc1"});
		ResultSet r = m.match("1", null);
		assertEquals(1, r.getResultSize());
		int[] docids = r.getDocids();
		assertEquals(1, docids.length);
		assertEquals(0, docids[0]);
		double[] scores = r.getScores();
		assertEquals(1, scores.length);
		assertEquals(0.00001d, scores[0], 0.0d);		
	}
	
	@Test public void testSingleQuerySingleResultTwoMatches() throws Exception
	{
		Matching m = getMatching(new String[]{"1 Q0 doc1 1 1.2 bla"}, new String[]{"doc1"});
		ResultSet r;
		int[] docids;
		
		r = m.match("1", null);
		assertEquals(1, r.getResultSize());
		docids = r.getDocids();
		assertEquals(1, docids.length);
		assertEquals(0, docids[0]);
		
		r = m.match("1", null);
		assertEquals(1, r.getResultSize());
		docids = r.getDocids();
		assertEquals(1, docids.length);
		assertEquals(0, docids[0]);
	}
	
	@Test public void testSingleQueryTwoResults() throws Exception
	{
		Matching m = getMatching(
				new String[]{
						"1 Q0 doc2 1 1.2 bla",
						"1 Q0 doc1 2 1.1 bla"}, 
				new String[]{"doc1", "doc2"});
		ResultSet r = m.match("1", null);
		assertEquals(2, r.getResultSize());
		int[] docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(1, docids[0]);
		assertEquals(0, docids[1]);
	}
	
	@Test public void testSingleQueryThreeResultsTwoWanted() throws Exception
	{
		ApplicationSetup.setProperty("matching.trecresults.length", "2");
		Matching m = getMatching(
				new String[]{
						"1 Q0 doc2 1 1.2 bla",
						"1 Q0 doc1 2 1.1 bla",
						"1 Q0 doc3 3 1.0 bla"}, 
				new String[]{"doc1", "doc2", "doc3"});
		ResultSet r = m.match("1", null);
		assertEquals(2, r.getResultSize());
		int[] docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(1, docids[0]);
		assertEquals(0, docids[1]);
	}
	
	
	@Test public void testSingleQueryTwoResultsWithNoMatch() throws Exception
	{
		Matching m = getMatching(
				new String[]{
						"1 Q0 doc2 1 1.2 bla",
						"1 Q0 doc1 2 1.1 bla"}, 
				new String[]{"doc1", "doc2"});
		ResultSet r = m.match("1", null);
		assertEquals(2, r.getResultSize());
		int[] docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(1, docids[0]);
		assertEquals(0, docids[1]);
		r = m.match("2", null);
		assertEquals(0, r.getResultSize());
	}
	
	
	@Test public void testTwoQueryTwoResults() throws Exception
	{
		Matching m = getMatching(
				new String[]{
						"1 Q0 doc2 1 1.2 bla",
						"1 Q0 doc1 2 1.1 bla",
						"2 Q0 doc4 2 1.1 bla",
						"2 Q0 doc3 2 1.1 bla",
						}, 
				new String[]{"doc1", "doc2", "doc3", "doc4"});
		ResultSet r;
		int[] docids;
		
		r = m.match("1", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(1, docids[0]);
		assertEquals(0, docids[1]);
		
		r = m.match("2", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(3, docids[0]);
		assertEquals(2, docids[1]);
	}
	
	@Test public void testTwoInterleavedDuplicatedQueryTwoResults() throws Exception
	{
		Matching m = getMatching(
				new String[]{
						"1 Q0 doc2 1 1.2 bla",
						"1 Q0 doc1 2 1.1 bla",
						"2 Q0 doc4 2 1.1 bla",
						"2 Q0 doc3 2 1.1 bla",
						}, 
				new String[]{"doc1", "doc2", "doc3", "doc4"});
		ResultSet r;
		int[] docids;
		
		r = m.match("1", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(1, docids[0]);
		assertEquals(0, docids[1]);
		
		r = m.match("2", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(3, docids[0]);
		assertEquals(2, docids[1]);
		
		r = m.match("1", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(1, docids[0]);
		assertEquals(0, docids[1]);
		
		r = m.match("2", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(3, docids[0]);
		assertEquals(2, docids[1]);
	}

	@Test public void testTwoNonInterleavedDuplicatedQueryTwoResults() throws Exception
	{
		Matching m = getMatching(
				new String[]{
						"1 Q0 doc2 1 1.2 bla",
						"1 Q0 doc1 2 1.1 bla",
						"2 Q0 doc4 2 1.1 bla",
						"2 Q0 doc3 2 1.1 bla",
						}, 
				new String[]{"doc1", "doc2", "doc3", "doc4"});
		ResultSet r;
		int[] docids;
		
		r = m.match("1", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(1, docids[0]);
		assertEquals(0, docids[1]);
		
		r = m.match("1", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(1, docids[0]);
		assertEquals(0, docids[1]);

		r = m.match("2", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(3, docids[0]);
		assertEquals(2, docids[1]);
		
		r = m.match("2", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(3, docids[0]);
		assertEquals(2, docids[1]);
	}

	
	@Test public void testTwoQueryTwoResultsTwoMatches() throws Exception
	{
		Matching m = getMatching(
				new String[]{
						"1 Q0 doc2 1 1.2 bla",
						"1 Q0 doc1 2 1.1 bla",
						"2 Q0 doc4 2 1.1 bla",
						"2 Q0 doc3 2 1.1 bla",
						}, 
				new String[]{"doc1", "doc2", "doc3", "doc4"});
		ResultSet r;
		int[] docids;
		
		r = m.match("1", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(1, docids[0]);
		assertEquals(0, docids[1]);
		
		r = m.match("2", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(3, docids[0]);
		assertEquals(2, docids[1]);
		
		r = m.match("1", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(1, docids[0]);
		assertEquals(0, docids[1]);
		
		r = m.match("2", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(3, docids[0]);
		assertEquals(2, docids[1]);
	}
	
	@Test public void testThreeQueryTwoResultsTwoMatchesWithMissing() throws Exception
	{
		Matching m = getMatching(
				new String[]{
						"1 Q0 doc2 1 1.2 bla",
						"1 Q0 doc1 2 1.1 bla",
						"2 Q0 doc4 2 1.2 bla",
						"2 Q0 doc3 2 1.1 bla",
						"3 Q0 doc3 2 1.2 bla",
						"3 Q0 doc4 2 1.1 bla",
						}, 
				new String[]{"doc1", "doc2", "doc3", "doc4"});
		ResultSet r;
		int[] docids;
		
		r = m.match("1", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(1, docids[0]);
		assertEquals(0, docids[1]);
		
		r = m.match("2", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(3, docids[0]);
		assertEquals(2, docids[1]);
		
		r = m.match("1", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(1, docids[0]);
		assertEquals(0, docids[1]);
		
		r = m.match("2", null);
		assertEquals(2, r.getResultSize());
		docids = r.getDocids();
		assertEquals(2, docids.length);
		assertEquals(3, docids[0]);
		assertEquals(2, docids[1]);
	}

}
