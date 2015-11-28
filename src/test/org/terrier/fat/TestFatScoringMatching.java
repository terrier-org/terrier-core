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
 * The Original Code is TestFatScoringMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */


package org.terrier.fat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.matching.FatResultSet;
import org.terrier.matching.FatScoringMatching;
import org.terrier.matching.FatUtils;
import org.terrier.matching.Matching;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.ResultSet;
import org.terrier.matching.daat.FatFull;
import org.terrier.matching.models.LGD;
import org.terrier.matching.models.TF_IDF;
import org.terrier.structures.Index;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestFatScoringMatching extends ApplicationSetupBasedTest {

	@Test public void singleDocumentSingleTerm() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		ApplicationSetup.setProperty("ignore.low.idf.terms", "false");
		Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1"}, 
				new String[]{"term"});
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setQueryId("test");
		mqt.setTermProperty("term", 1.0d);
		mqt.setDefaultTermWeightingModel(new TF_IDF());
		Matching m = new FatFull(index);
		
		ResultSet r1 = m.match("test", mqt);
		FatResultSet fr1 = (FatResultSet)r1;
		
		for (FatResultSet frInput : new FatResultSet[]{fr1, FatUtils.recreate(fr1)})
		{
			assertEquals(1, frInput.getResultSize());
			assertEquals(0, frInput.getDocids()[0]);
			double score = 0;
			assertTrue( (score = frInput.getScores()[0]) > 0);
			frInput.getScores()[0] = 0.1d;
			
			assertEquals(0, frInput.getPostings()[0][0].getId());
			assertEquals(1, frInput.getPostings()[0][0].getFrequency());
			assertEquals(1, frInput.getPostings()[0][0].getDocumentLength());
			
			assertEquals(1, frInput.getCollectionStatistics().getNumberOfDocuments());
			assertEquals(1, frInput.getCollectionStatistics().getNumberOfPointers());
			assertEquals(1, frInput.getCollectionStatistics().getNumberOfTokens());
			assertEquals(1, frInput.getCollectionStatistics().getNumberOfUniqueTerms());
			
			FatScoringMatching fsm = new FatScoringMatching(null, null, new TF_IDF());
			ResultSet r2 = fsm.doMatch("test", mqt, frInput);
			
			assertEquals(1, r2.getResultSize());
			assertEquals(0, r2.getDocids()[0]);
			assertEquals(score, r2.getScores()[0], 0.0d);
			assertEquals(0.1d, frInput.getScores()[0], 0.0d);
			
			FatScoringMatching fsm2 = new FatScoringMatching(null, null, new LGD());
			ResultSet r3 = fsm2.doMatch("test", mqt, frInput);
			
			
			assertEquals(1, r3.getResultSize());
			assertEquals(0, r3.getDocids()[0]);
			assertTrue(r3.getScores()[0] > 0);
			assertTrue(score != r3.getScores()[0]);
			
			assertEquals(0.1d, frInput.getScores()[0], 0.0d);
			assertEquals(score, r2.getScores()[0], 0.0d);
		}
	}
	
}
