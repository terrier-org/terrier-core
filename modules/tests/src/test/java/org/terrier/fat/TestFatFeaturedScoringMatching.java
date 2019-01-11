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
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */

package org.terrier.fat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.learning.FeaturedResultSet;
import org.terrier.matching.BaseMatching;
import org.terrier.matching.FatFeaturedScoringMatching;
import org.terrier.matching.FatResultSet;
import org.terrier.matching.FatUtils;
import org.terrier.matching.FeaturedScoringMatching;
import org.terrier.matching.Matching;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.ResultSet;
import org.terrier.matching.daat.FatFull;
import org.terrier.matching.matchops.PhraseOp;
import org.terrier.matching.matchops.UnorderedWindowOp;
import org.terrier.matching.models.TF_IDF;
import org.terrier.matching.models.Tf;
import org.terrier.querying.parser.Query.QTPBuilder;
import org.terrier.structures.Index;
import org.terrier.structures.NgramEntryStatistics;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestFatFeaturedScoringMatching extends ApplicationSetupBasedTest {

	@Test public void singleDocumentSingleTerm() throws Exception
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
		Matching m = new FatFull(index);
		
		ResultSet r1 = m.match("test", mqt);
		FatResultSet fr1 = (FatResultSet)r1;
		
		for (FatResultSet frInput : new FatResultSet[]{fr1, FatUtils.recreate(fr1)})
		{
			assertEquals(1, frInput.getResultSize());
			assertEquals(0, frInput.getDocids()[0]);
			assertTrue(frInput.getScores()[0] > 0);
			assertEquals(0, frInput.getPostings()[0][0].getId());
			assertEquals(1, frInput.getPostings()[0][0].getFrequency());
			assertEquals(1, frInput.getPostings()[0][0].getDocumentLength());
			
			assertEquals(1, frInput.getCollectionStatistics().getNumberOfDocuments());
			assertEquals(1, frInput.getCollectionStatistics().getNumberOfPointers());
			assertEquals(1, frInput.getCollectionStatistics().getNumberOfTokens());
			assertEquals(1, frInput.getCollectionStatistics().getNumberOfUniqueTerms());
			
			FatFeaturedScoringMatching ffsm = new FatFeaturedScoringMatching(null, null, 
					new String[]{"WMODEL:Tf", "WMODEL:Dl", "DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier"}
			);
			ResultSet r2 = ffsm.doMatch("test", mqt, fr1);
			
			assertEquals(1, r2.getResultSize());
			assertEquals(0, r2.getDocids()[0]);
			assertEquals(r1.getScores()[0], r2.getScores()[0], 0.0d);
			
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
	}
	
	@Test public void singleDocumentMultipleTermsWithProx() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		ApplicationSetup.setProperty("ignore.low.idf.terms", "false");
		ApplicationSetup.setProperty("proximity.dependency.type","SD");
		Index index = IndexTestUtils.makeIndexBlocks(
				new String[]{"doc1"}, 
				new String[]{"the lazy dog jumped over the quick fox"});
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setTermProperty("lazy", 1.0d);
		mqt.setTermProperty("fox", 1.0d);		
		mqt.add(QTPBuilder
				.of(new PhraseOp(new String[]{"lazy", "dog"}))
				.setWeight(0.1).setTag("sdm").setTag(BaseMatching.BASE_MATCHING_TAG)
				.setWeightingModels(Arrays.asList(new Tf()))
				.build());
		mqt.add(QTPBuilder
				.of(new UnorderedWindowOp(new String[]{"lazy", "dog"}, 8))
				.setWeight(0.1).setTag("sdm").setTag(BaseMatching.BASE_MATCHING_TAG)
				.setWeightingModels(Arrays.asList(new Tf()))
				.build());
		mqt.setDefaultTermWeightingModel(new Tf());
		Matching m = new FatFull(index);
		
		ResultSet r1 = m.match("query1", mqt);
		FatResultSet fr1 = (FatResultSet)r1;

		for (FatResultSet frInput : new FatResultSet[]{fr1, FatUtils.recreate(fr1)})
		{
			System.err.println("Processing frInput="+frInput);
			
			assertEquals(4, frInput.getEntryStatistics().length);
			assertFalse(frInput.getEntryStatistics()[0] instanceof NgramEntryStatistics);
			assertFalse(frInput.getEntryStatistics()[1] instanceof NgramEntryStatistics);
			assertTrue(frInput.getEntryStatistics()[2] instanceof NgramEntryStatistics);
			assertTrue(frInput.getEntryStatistics()[3] instanceof NgramEntryStatistics);
			
			
			assertEquals(1, frInput.getResultSize());
			assertEquals(0, frInput.getDocids()[0]);
			System.out.println(frInput.getScores()[0]);
			assertTrue(frInput.getScores()[0] > 0);
			assertEquals(0, frInput.getPostings()[0][0].getId());
			assertEquals(1, frInput.getPostings()[0][0].getFrequency());
			assertEquals(8, frInput.getPostings()[0][0].getDocumentLength());
			
			assertEquals(1, frInput.getCollectionStatistics().getNumberOfDocuments());
			assertEquals(8, frInput.getCollectionStatistics().getNumberOfTokens());
			
			FatFeaturedScoringMatching ffsm = new FatFeaturedScoringMatching(null, null, 
					new String[]{
					"WMODELt:Tf", 
					"WMODELp:org.terrier.matching.models.dependence.pBiL", 
					"DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier%proximity.ngram.length=2 proximity.plm.split.synonyms=false",
					"DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier%proximity.ngram.length=8 proximity.plm.split.synonyms=false",
					"WMODEL$sdm:org.terrier.matching.models.dependence.pBiL"}
			);
			ResultSet r2 = ffsm.doMatch("test", mqt, fr1);
			
			assertEquals(1, r2.getResultSize());
			assertEquals(0, r2.getDocids()[0]);
			assertEquals(r1.getScores()[0], r2.getScores()[0], 0.0d);
			
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
	
	@Test public void singleDocumentMultipleTermsWithProxOne() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		ApplicationSetup.setProperty("ignore.low.idf.terms", "false");
		ApplicationSetup.setProperty("proximity.dependency.type","SD");
		Index index = IndexTestUtils.makeIndexBlocks(
				new String[]{"doc1"}, 
				new String[]{"the lazy dog jumped over the quick fox"});
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setTermProperty("lazy", 1.0d);
		mqt.setTermProperty("fox", 1.0d);		
		mqt.add(QTPBuilder
				.of(new PhraseOp(new String[]{"lazy", "dog"}))
				.setWeight(0.1).setTag("sdm").setTag(BaseMatching.BASE_MATCHING_TAG)
				.setWeightingModels(Arrays.asList(new Tf()))
				.build());
		mqt.add(QTPBuilder
				.of(new UnorderedWindowOp(new String[]{"lazy", "dog"}, 8))
				.setWeight(0.1).setTag("sdm").setTag(BaseMatching.BASE_MATCHING_TAG)
				.setWeightingModels(Arrays.asList(new Tf()))
				.build());
		mqt.setDefaultTermWeightingModel(new Tf());
		Matching m = new FatFull(index);
		
		ResultSet r1 = m.match("query1", mqt);
		FatResultSet fr1 = (FatResultSet)r1;

		for (FatResultSet frInput : new FatResultSet[]{fr1, FatUtils.recreate(fr1)})
		{
			System.err.println("Processing frInput="+frInput);
			
			assertEquals(4, frInput.getEntryStatistics().length);
			assertFalse(frInput.getEntryStatistics()[0] instanceof NgramEntryStatistics);
			assertFalse(frInput.getEntryStatistics()[1] instanceof NgramEntryStatistics);
			assertTrue(frInput.getEntryStatistics()[2] instanceof NgramEntryStatistics);
			assertTrue(frInput.getEntryStatistics()[3] instanceof NgramEntryStatistics);
			
			
			assertEquals(1, frInput.getResultSize());
			assertEquals(0, frInput.getDocids()[0]);
			System.out.println(frInput.getScores()[0]);
			assertTrue(frInput.getScores()[0] > 0);
			assertEquals(0, frInput.getPostings()[0][0].getId());
			assertEquals(1, frInput.getPostings()[0][0].getFrequency());
			assertEquals(8, frInput.getPostings()[0][0].getDocumentLength());
			
			assertEquals(1, frInput.getCollectionStatistics().getNumberOfDocuments());
			assertEquals(8, frInput.getCollectionStatistics().getNumberOfTokens());
			
			FatFeaturedScoringMatching ffsm = new FatFeaturedScoringMatching(null, null, 
					new String[]{
					//"WMODELt:Tf", 
					"WMODELp:org.terrier.matching.models.dependence.pBiL"//, 
					//"DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier%proximity.ngram.length=2 proximity.plm.split.synonyms=false",
					//"DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier%proximity.ngram.length=8 proximity.plm.split.synonyms=false",
					//"WMODEL$sdm:org.terrier.matching.models.dependence.pBiL"
					}
			);
			ResultSet r2 = ffsm.doMatch("test", mqt, fr1);
			
			assertEquals(1, r2.getResultSize());
			assertEquals(0, r2.getDocids()[0]);
			assertEquals(r1.getScores()[0], r2.getScores()[0], 0.0d);
			
			FeaturedResultSet featRes = (FeaturedResultSet)r2;
//			final double[] tfs = featRes.getFeatureScores("WMODELt:Tf");
//			assertEquals(1, tfs.length);
//			assertEquals(2, tfs[0], 0.0d);
			
			final double[] dls = featRes.getFeatureScores("WMODELp:org.terrier.matching.models.dependence.pBiL");
			assertEquals(1, dls.length);
			assertTrue(dls[0] > 0);
			
//			final double[] proxSmall = featRes.getFeatureScores("DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier%proximity.ngram.length=2 proximity.plm.split.synonyms=false");
//			assertNotNull(proxSmall);
//			assertTrue(proxSmall[0] == 0);
//			
//			final double[] proxLarge = featRes.getFeatureScores("DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier%proximity.ngram.length=8 proximity.plm.split.synonyms=false");
//			assertNotNull(proxLarge);
//			assertTrue(proxLarge[0] > 0);
//			
//			final double[] proxNew = featRes.getFeatureScores("WMODEL$sdm:org.terrier.matching.models.dependence.pBiL");
//			assertNotNull(proxNew);
//			assertTrue(proxNew[0] > 0);
			
		}
	}
	
	@Test public void testFilters()
	{
		Set<String> NS = new HashSet<>();
		assertTrue(FeaturedScoringMatching.filterTerm.test(Pair.of("term1", NS)));
		assertFalse(FeaturedScoringMatching.filterTerm.test(Pair.of("#1(term1 term2)", NS)));
		assertFalse(FeaturedScoringMatching.filterTerm.test(Pair.of("#uw8(term1 term2)", NS)));
		
		assertFalse(FeaturedScoringMatching.filterProx.test(Pair.of("term1", NS)));
		assertTrue(FeaturedScoringMatching.filterProx.test(Pair.of("#1(term1 term2)", NS)));
		assertTrue(FeaturedScoringMatching.filterProx.test(Pair.of("#uw8(term1 term2)", NS)));
		
		
		
	}
}
