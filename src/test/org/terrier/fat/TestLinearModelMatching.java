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
 * The Original Code is TestLinearModelMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */


package org.terrier.fat;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.terrier.learning.FeaturedQueryResultSet;
import org.terrier.learning.FeaturedResultSet;
import org.terrier.matching.LearnedModelMatching;
import org.terrier.matching.LinearModelMatching;
import org.terrier.matching.Matching;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.QueryResultSet;
import org.terrier.matching.ResultSet;
import org.terrier.structures.CollectionStatistics;
import org.terrier.utility.ApplicationSetup;

public class TestLinearModelMatching {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();
	
	LearnedModelMatching makeMatching(Matching mock, double[] weights) throws Exception
	{
		return new LinearModelMatching(null, mock, weights);
	}
	
	@Test public void testEmpty() throws Exception
	{
		assertEquals(0, makeMatching(new MockMatching(), new double[]{4,3,2}).match("empty", null).getResultSize());
	}
	
	@Test public void testOneDocumentTwoFeatures() throws Exception
	{
		Map<String,ResultSet> results = new HashMap<String,ResultSet>();
		FeaturedResultSet rs = new FeaturedQueryResultSet(1);
		rs.getDocids()[0] = 10;
		rs.getScores()[0] = 2;
		rs.putFeatureScores("DPH", new double[]{0.5});
		rs.putFeatureScores("PageRank", new double[]{0.25});		
		results.put("query1", rs);
		
		ApplicationSetup.setProperty("fat.matching.model.score_is_feature", "true");
		ApplicationSetup.setProperty("fat.matching.model.normalise", "false");
		ResultSet rtr = makeMatching(new MockMatching(results), new double[]{2,1,0}).match("query1", null);
		//docid 10: 2*2 + 1*0.5 + 0 * 0.25 = 4.5
		
		assertEquals(1, rtr.getResultSize());
		System.err.println("docids="+ java.util.Arrays.toString(rtr.getDocids()));
		System.err.println("scores="+ java.util.Arrays.toString(rtr.getScores()));
		assertEquals(10, rtr.getDocids()[0]);
		assertEquals(4.5d, rtr.getScores()[0], 0.0d);	
	}
	
	@Test public void testOneDocumentThreeFeaturesLastUnused() throws Exception
	{
		Map<String,ResultSet> results = new HashMap<String,ResultSet>();
		FeaturedResultSet rs = new FeaturedQueryResultSet(1);
		rs.getDocids()[0] = 10;
		rs.getScores()[0] = 2;
		rs.putFeatureScores("DPH", new double[]{0.5});
		rs.putFeatureScores("PageRank", new double[]{0.25});
		rs.putFeatureScores("URLLength", new double[]{5});	
		results.put("query1", rs);
		
		ApplicationSetup.setProperty("fat.matching.model.score_is_feature", "true");
		ApplicationSetup.setProperty("fat.matching.model.normalise", "false");
		ResultSet rtr = makeMatching(new MockMatching(results), new double[]{2,1,0}).match("query1", null);
		//docid 10: 2*2 + 1*0.5 + 0 * 0.25 = 4.5
		
		assertEquals(1, rtr.getResultSize());
		System.err.println("docids="+ java.util.Arrays.toString(rtr.getDocids()));
		System.err.println("scores="+ java.util.Arrays.toString(rtr.getScores()));
		assertEquals(10, rtr.getDocids()[0]);
		assertEquals(4.5d, rtr.getScores()[0], 0.0d);	
	}
	
	
	
	
	@Test public void testTwoDocumentsTwoFeatures() throws Exception
	{
		Map<String,ResultSet> results = new HashMap<String,ResultSet>();
		FeaturedResultSet rs = new FeaturedQueryResultSet(2);
		rs.getDocids()[0] = 10;
		rs.getDocids()[1] = 100;
		rs.getScores()[0] = 1;
		rs.getScores()[1] = 0.9;
		rs.putFeatureScores("DPH", new double[]{0.5,0.6});
		rs.putFeatureScores("PageRank", new double[]{0.9,1});		
		results.put("query1", rs);
		
		ApplicationSetup.setProperty("fat.matching.model.score_is_feature", "true");
		ApplicationSetup.setProperty("fat.matching.model.normalise", "false");
		ResultSet rtr = makeMatching(new MockMatching(results), new double[]{4,3,2}).match("query1", null);
		//docid 10: 4*1 + 3*0.5 + 2 * 0.9 = 7.3
		//docid 100: 4 *0.9 + 3*0.6 + 2*1 = 7.4
		
		assertEquals(2, rtr.getResultSize());
		System.err.println("docids="+ java.util.Arrays.toString(rtr.getDocids()));
		System.err.println("scores="+ java.util.Arrays.toString(rtr.getScores()));
		assertEquals(100, rtr.getDocids()[0]);
		assertEquals(10, rtr.getDocids()[1]);
		assertEquals(7.4d, rtr.getScores()[0], 0.0d);
		assertEquals(7.3d, rtr.getScores()[1], 0.0d);
	}
	
	@Test public void testTwoDocumentsTwoFeaturesNoScore() throws Exception
	{
		Map<String,ResultSet> results = new HashMap<String,ResultSet>();
		FeaturedResultSet rs = new FeaturedQueryResultSet(2);
		rs.getDocids()[0] = 10;
		rs.getDocids()[1] = 100;
		rs.getScores()[0] = 1;
		rs.getScores()[1] = 0.9;
		rs.putFeatureScores("DPH", new double[]{0.5,0.6});
		rs.putFeatureScores("PageRank", new double[]{0.9,1});		
		results.put("query1", rs);
		
		ApplicationSetup.setProperty("fat.matching.model.score_is_feature", "false");
		ApplicationSetup.setProperty("fat.matching.model.normalise", "false");
		ResultSet rtr = makeMatching(new MockMatching(results), new double[]{3,2}).match("query1", null);
		//docid 10: 3*0.5 + 2 * 0.9 = 3.3
		//docid 100: 3*0.6 + 2*1 = 3.8
		
		assertEquals(2, rtr.getResultSize());
		System.err.println("docids="+ java.util.Arrays.toString(rtr.getDocids()));
		System.err.println("scores="+ java.util.Arrays.toString(rtr.getScores()));
		assertEquals(100, rtr.getDocids()[0]);
		assertEquals(10, rtr.getDocids()[1]);
		assertEquals(3.8d, rtr.getScores()[0], 0.0d);
		assertEquals(3.3d, rtr.getScores()[1], 0.0d);
	}
	
	protected void testWeightsParse(String input, double[] expected) throws Exception
	{
		File tempFile = testFolder.newFile("tmp.model");
		Writer w = new FileWriter(tempFile);
		w.write(input);
		w.close();
		double[] found = LinearModelMatching.loadFeatureWeights(tempFile.toString());
		assertEquals(expected.length, found.length);
		//System.err.println("expected=" + java.util.Arrays.toString(expected));
		//System.err.println("found=" + java.util.Arrays.toString(found));
		
		for(int i=0;i<expected.length;i++)
		{
			assertEquals(expected[i], found[i], 0.0d);
		}
		for(int i=expected.length;i<found.length;i++)
		{
			assertEquals(0.0d, found[i], 0.0d);
		}
	}
	
	@Test public void testWeightsParsing() throws Exception
	{
		testWeightsParse("1:1.1 2:2 3:3 4:5", new double[]{1.1, 2, 3, 5});
														  //1 2  3  4  5
		testWeightsParse("5:1.1 3:2 3:3 4:5", new double[]{0, 0, 3, 5, 1.1});
	}
	
	static class MockMatching implements Matching
	{
		Map<String,ResultSet> results;
		
		public MockMatching() {
			results = new HashMap<String,ResultSet>(0);
		}
		
		public MockMatching(Map<String,ResultSet> _results)
		{
			results = _results;
		}		
		
		@Override
		public String getInfo() {
			return this.getClass().getName();
		}

		@Override
		public ResultSet match(String queryNumber, MatchingQueryTerms queryTerms)
				throws IOException {
			ResultSet rtr = results.get(queryNumber);
			if (rtr == null)
				return new QueryResultSet(0);
			return rtr;
		}

		@Override
		public void setCollectionStatistics(CollectionStatistics cs) {}
		
	}
	
}
