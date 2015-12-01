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
 * The Original is in 'TestPhraseScoreModifier.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.matching.dsms;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.matching.AccumulatorResultSet;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.querying.parser.Query;
import org.terrier.querying.parser.SingleTermQuery;
import org.terrier.structures.Index;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestPhraseScoreModifier extends ApplicationSetupBasedTest {

	@Test public void testFirstDocumentMatchingPhrase() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		Index index = IndexTestUtils.makeIndexBlocks(
				new String[]{"doc1", "doc2"}, 
				new String[]{"dramatis personae", "dramatis nae personae"});
		AccumulatorResultSet rs = new AccumulatorResultSet(2);
		rs.scoresMap.put(0, 1.0);
		rs.scoresMap.put(1, 0.9);
		rs.occurrencesMap.put(0, (short) 2);
		rs.occurrencesMap.put(1, (short) 2);
		rs.initialise();
		PhraseScoreModifier psm = new PhraseScoreModifier(Arrays.asList((Query)new SingleTermQuery("dramatis"), (Query)new SingleTermQuery("personae")));
		psm.modifyScores(index, new MatchingQueryTerms(), rs);
		assertEquals(0, rs.getDocids()[0]);
		assertEquals(1.0d, rs.getScores()[0], 0.0d);
		assertEquals(Double.NEGATIVE_INFINITY, rs.getScores()[1], 0.0d);
		assertEquals(1, rs.getDocids()[1]);
	}
	
	@Test public void testSecondDocumentMatchingPhrase() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		Index index = IndexTestUtils.makeIndexBlocks(
				new String[]{"doc1", "doc2"}, 
				new String[]{"dramatis nae personae", "dramatis personae"});
		AccumulatorResultSet rs = new AccumulatorResultSet(2);
		rs.scoresMap.put(0, 1.0);
		rs.scoresMap.put(1, 0.9);
		rs.occurrencesMap.put(0, (short) 2);
		rs.occurrencesMap.put(1, (short) 2);
		rs.initialise();
		PhraseScoreModifier psm = new PhraseScoreModifier(Arrays.asList((Query)new SingleTermQuery("dramatis"), (Query)new SingleTermQuery("personae")));
		psm.modifyScores(index, new MatchingQueryTerms(), rs);
		assertEquals(0, rs.getDocids()[0]);
		assertEquals(Double.NEGATIVE_INFINITY, rs.getScores()[0], 0.0d);
		assertEquals(0.9d, rs.getScores()[1], 0.0d);
		assertEquals(1, rs.getDocids()[1]);
	}
	
}
