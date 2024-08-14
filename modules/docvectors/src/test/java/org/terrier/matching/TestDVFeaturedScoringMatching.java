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
 * The Original Code is TestFatFeaturedScoringMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */

package org.terrier.matching;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gnu.trove.TIntIntHashMap;

import java.util.Arrays;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.learning.FeaturedResultSet;
import org.terrier.matching.daat.Full;
import org.terrier.matching.matchops.PhraseOp;
import org.terrier.matching.matchops.UnorderedWindowOp;
import org.terrier.matching.models.BM25;
import org.terrier.matching.models.TF_IDF;
import org.terrier.matching.models.Tf;
import org.terrier.querying.DependenceModelPreProcess;
import org.terrier.querying.MQTRewritingProcess;
import org.terrier.querying.QueryExpansion;
import org.terrier.querying.parser.Query.QTPBuilder;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestDVFeaturedScoringMatching extends ApplicationSetupBasedTest {
	
	@Test 
	public void rewritingExpansionsParsing() throws Exception
	{
		String f = super.writeTemporaryFile("temp.rewriters", 
			new String[]{
				"qeBo1:QueryExpansion(DFRBagExpansionTerms,Bo1)",
				"qeKL:QueryExpansion(DFRBagExpansionTerms,KL)"
		});
		ApplicationSetup.setProperty("dv.featured.scoring.matching.rewriters", "FILE");
		ApplicationSetup.setProperty("dv.featured.scoring.matching.rewriters.file", f);
		Index index = IndexOnDisk.createIndex("/Users/craigm/wt2g_index/index/", "data");
		DVFeaturedScoringMatching ffsm = new DVFeaturedScoringMatching(index, null, 
				new String[]{"SAMPLE", "WMODEL$qeBo1:BM25", "WMODEL$qeKL:BM25"}
		);
		assertEquals(2, ffsm.processes.length);
		
	}
	
	// @Test public void rewritingExpansions() throws Exception
	// {
	// 	ApplicationSetup.setProperty("termpipelines", "");
	// 	ApplicationSetup.setProperty("ignore.low.idf.terms", "false");
	// 	ApplicationSetup.setProperty("proximity.dependency.type","SD");
	// 	Index index = IndexOnDisk.createIndex("/Users/craigm/wt2g_index/index/", "data");
		
				
	// 	MatchingQueryTerms mqt = new MatchingQueryTerms();
	// 	mqt.setQueryId("test");
	// 	mqt.setTermProperty("behavioral", 1.0d);
	// 	mqt.setTermProperty("genetics", 1.0d);
	// 	mqt.setDefaultTermWeightingModel(new BM25());
	// 	mqt.stream().forEach(me -> me.getValue().getTags().add("firstmatchscore"));
	// 	Matching m = new Full(index);
		
	// 	ResultSet frInput = m.match("test", mqt);
	// 	assertTrue(frInput.getResultSize() > 1);
	// 	assertTrue(frInput.getScores()[0] >= 0);
	// 	final int[] origDocids = frInput.getDocids().clone();
			
	// 	DVFeaturedScoringMatching ffsm = new DVFeaturedScoringMatching(index, null, 
	// 			new String[]{"SAMPLE", "WMODEL$qe:BM25"}
	// 	);
	// 	ffsm.processes = new MQTRewritingProcess[]{
	// 		new QueryExpansion(),
	// 		new DependenceModelPreProcess()
	// 	};
	// 	ffsm.processTags = new String[]{"qe", "sdm"};
		
	// 	ResultSet r2 = ffsm.doMatch("test", mqt, frInput);
		
	// 	assertTrue(r2.getResultSize() > 1);
	// 	assertTrue(r2.getScores()[0] >= 0);
	// 	assertEquals(frInput.getScores()[0], r2.getScores()[0], 0.0d);
		
	// 	FeaturedResultSet featRes = (FeaturedResultSet)r2;
	// 	final double[] sample = featRes.getFeatureScores("SAMPLE");
	// 	assertTrue(sample.length > 1);
		
	// 	final double[] qe = featRes.getFeatureScores("WMODEL$qe:BM25");
	// 	assertTrue(qe.length > 1);

	// 	assertArrayEquals(origDocids, featRes.getDocids());
	// }
	
	@Test 
	public void singleDocumentSingleTerm() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		ApplicationSetup.setProperty("ignore.low.idf.terms", "false");
		ApplicationSetup.setProperty("proximity.dependency.type","SD");
		Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1"}, 
				new String[]{"term"});
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setTermProperty("term", 1.0d);
		mqt.setDefaultTermWeightingModel(new TF_IDF());
		Matching m = new Full(index);
		
		ResultSet frInput = m.match("singleDocumentSingleTerm-Q1", mqt);
		assertEquals(1, frInput.getResultSize());
		assertEquals(0, frInput.getDocids()[0]);
		assertTrue(frInput.getScores()[0] > 0);
			
		DVFeaturedScoringMatching ffsm = new DVFeaturedScoringMatching(index, null, 
				new String[]{"WMODEL:Tf", "WMODEL:Dl", "DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier"}
		);
		ResultSet r2 = ffsm.doMatch("singleDocumentSingleTerm-Q2", mqt, frInput);
		
		assertEquals(1, r2.getResultSize());
		assertEquals(0, r2.getDocids()[0]);
		assertEquals(frInput.getScores()[0], r2.getScores()[0], 0.0d);
		
		FeaturedResultSet featRes = (FeaturedResultSet)r2;
		final double[] tfs = featRes.getFeatureScores("WMODEL:Tf");
		assertEquals(1, tfs.length);
		assertEquals(1, tfs[0], 0.0d);
		
		final double[] dls = featRes.getFeatureScores("WMODEL:Dl");
		assertEquals(1, dls.length);
		assertEquals(1, dls[0], 0.0d);
		
		final double[] prox = featRes.getFeatureScores("DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier");
		assertNotNull(prox);
		//single term query can have no proximity, however, feature MUST be defined
		assertEquals(0.0d, prox[0], 0.0d);
	}
	
	@Test public void multiDocMultipleTermsWithTags() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		ApplicationSetup.setProperty("ignore.low.idf.terms", "false");
		ApplicationSetup.setProperty("proximity.dependency.type","SD");
		Index index = IndexTestUtils.makeIndexBlocks(
				new String[]{"doc1", "dox2"}, 
				new String[]{"the lazy lazy dog jumped over the quick fox", "lazy fox fox fox"});
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setQueryId("test2");
		mqt.setTermProperty("lazy", 1.0d);
		mqt.get("lazy").getValue().setTag(BaseMatching.BASE_MATCHING_TAG);
		
		mqt.setTermProperty("fox", 1.0d);
		mqt.get("fox").getValue().setTag("second");
		mqt.get("fox").getValue().tags.remove(BaseMatching.BASE_MATCHING_TAG);
		mqt.setDefaultTermWeightingModel(new Tf());
		//mqt.matchOnTags.add("first");
		Matching m = new Full(index);
		ResultSet frInput = m.match("query1", mqt);
		
		assertEquals(2, frInput.getResultSize());
		assertEquals(0, frInput.getDocids()[0]);
		assertEquals(2, frInput.getScores()[0], 0d);//tf for lazy in docid0
		assertEquals(1, frInput.getDocids()[1]);
		assertEquals(1, frInput.getScores()[1], 0d);//tf for lazy in docid1
	
		
		DVFeaturedScoringMatching fsm = new DVFeaturedScoringMatching(index, null, 
				new String[]{
				"WMODELt:Tf",
				"WMODEL$second:Tf"
				}
		);
		ResultSet r2 = fsm.doMatch("test", mqt, frInput);
		
		//the order of the resultset from fsm is based on the original scores.
		
		TIntIntHashMap id2rank = new TIntIntHashMap();
		for(int i=0;i<r2.getDocids().length;i++)
		{
			id2rank.put(r2.getDocids()[i], i);	
		}
		assertTrue(id2rank.contains(0));
		assertTrue(id2rank.contains(1));
		
		assertEquals(2, r2.getResultSize());
		assertEquals(frInput.getScores()[id2rank.get(0)], r2.getScores()[id2rank.get(0)], 0.0d);
		assertEquals(frInput.getScores()[id2rank.get(1)], r2.getScores()[id2rank.get(1)], 0.0d);
		
		FeaturedResultSet featRes = (FeaturedResultSet)r2;
		// org.terrier.structures.outputformat.LETOROutputFormat of = new org.terrier.structures.outputformat.LETOROutputFormat(index);
		// org.terrier.querying.Request rq = new org.terrier.querying.Request();
		// rq.setIndex(index);
		// rq.setResultSet(featRes);
		// of.printResults(new java.io.PrintWriter(System.out), rq, "ltr", "Q0", 100);
		
		final double[] tfs = featRes.getFeatureScores("WMODELt:Tf");
		assertEquals(2, tfs.length);
		assertEquals(3, tfs[id2rank.get(0)], 0.0d); //tf for lazy and fox in docid0
		assertEquals(4, tfs[id2rank.get(1)], 0.0d); //tf for lazy and fox in docid1
		
		final double[] tfs2 = featRes.getFeatureScores("WMODEL$second:Tf");
		assertEquals(2, tfs2.length);
		assertEquals(1, tfs2[id2rank.get(0)], 0.0d);//tf for fox in docid0
		assertEquals(3, tfs2[id2rank.get(1)], 0.0d);//tf for fox in docid1
	}
	
	@Test 
	public void singleDocumentMultipleTermsWithProx() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		ApplicationSetup.setProperty("ignore.low.idf.terms", "false");
		ApplicationSetup.setProperty("proximity.dependency.type","SD");
		Index index = IndexTestUtils.makeIndexBlocks(
				new String[]{"doc1"}, 
				new String[]{"the lazy dog jumped over the quick fox"});
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setQueryId("test2");
		mqt.setTermProperty("lazy", 1.0d);
		mqt.setTermProperty("fox", 1.0d);		
		mqt.add(QTPBuilder
				.of(new PhraseOp(new String[]{"lazy", "dog"}))
				.setWeight(0.1).setTag("sdm")
				.setWeightingModels(Arrays.asList(new Tf()))
				.build());
		mqt.add(QTPBuilder
				.of(new UnorderedWindowOp(new String[]{"lazy", "dog"}, 8))
				.setWeight(0.1).setTag("sdm")
				.setWeightingModels(Arrays.asList(new Tf()))
				.build());
		mqt.setDefaultTermWeightingModel(new Tf());
		Matching m = new Full(index);
		
		ResultSet frInput = m.match("query1", mqt);
			
		
		assertEquals(1, frInput.getResultSize());
		assertEquals(0, frInput.getDocids()[0]);
		assertTrue(frInput.getScores()[0] > 0);
	
		
		DVFeaturedScoringMatching ffsm = new DVFeaturedScoringMatching(index, null, 
				new String[]{
				"WMODELt:Tf", 
				"WMODELp:org.terrier.matching.models.dependence.pBiL", 
				"DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier%proximity.ngram.length=2 proximity.plm.split.synonyms=false",
				"DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier%proximity.ngram.length=8 proximity.plm.split.synonyms=false",
				"WMODEL$sdm:org.terrier.matching.models.dependence.pBiL"}
		);
		ResultSet r2 = ffsm.doMatch("test", mqt, frInput);
		
		assertEquals(1, r2.getResultSize());
		assertEquals(0, r2.getDocids()[0]);
		assertEquals(frInput.getScores()[0], r2.getScores()[0], 0.0d);
		
		FeaturedResultSet featRes = (FeaturedResultSet)r2;
		final double[] tfs = featRes.getFeatureScores("WMODELt:Tf");
		assertEquals(1, tfs.length);
		assertEquals(2, tfs[0], 0.0d);
		
		final double[] dls = featRes.getFeatureScores("WMODELp:org.terrier.matching.models.dependence.pBiL");
		assertEquals(1, dls.length);
		assertTrue(dls[0] > 0);
		
		final double[] proxSmall = featRes.getFeatureScores("DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier%proximity.ngram.length=2 proximity.plm.split.synonyms=false");
		assertNotNull(proxSmall);
		assertTrue(proxSmall[0] == 0);
		
		final double[] proxLarge = featRes.getFeatureScores("DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier%proximity.ngram.length=8 proximity.plm.split.synonyms=false");
		assertNotNull(proxLarge);
		assertTrue(proxLarge[0] > 0);
		
		final double[] proxNew = featRes.getFeatureScores("WMODEL$sdm:org.terrier.matching.models.dependence.pBiL");
		assertNotNull(proxNew);
		assertTrue(proxNew[0] > 0);
			
	}
	
}
