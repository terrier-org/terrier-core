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
 * The Original Code is TestManager.java.
 *
 * The Original Code is Copyright (C) 2004-2016 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.querying;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.daat.Full;
import org.terrier.matching.models.PL2;
import org.terrier.structures.Index;
import org.terrier.tests.ApplicationSetupBasedTest;

public class TestManager extends ApplicationSetupBasedTest {

	@Test(expected=IllegalArgumentException.class)
	public void testNullIndexException() throws Exception
	{
		new Manager(null);
	}
	
	@Test
	public void testSimpleRetrieval() throws Exception
	{
		Index index = IndexTestUtils.makeIndex(new String[]{"doc1"}, new String[]{"The quick brown fox jumps over the lazy dog"});
		Manager m = new Manager(index);
		SearchRequest srq;
		srq = m.newSearchRequest("testQuery", "fox fox dog");
		srq.addMatchingModel(Full.class.getName(), PL2.class.getName());
		m.runSearchRequest(srq);
		assertNotNull(srq.getResultSet());
		assertEquals(1, srq.getResultSet().getResultSize());
		assertEquals(0, srq.getResultSet().getDocids()[0]);
		
	}
	
	@Test
	public void testCountingQueryTerms() throws Exception
	{
		Index index = IndexTestUtils.makeIndex(new String[]{"doc1"}, new String[]{"The quick brown fox jumps over the lazy dog"});
		Manager m = new Manager(index);
		SearchRequest srq;
		MatchingQueryTerms mqt;
		srq = m.newSearchRequest("testQuery", "fox fox dog");
		srq.addMatchingModel(Full.class.getName(), PL2.class.getName());
		m.runSearchRequest(srq);
		mqt = ((Request)srq).getMatchingQueryTerms();
		assertEquals(2.0d, mqt.getTermWeight("fox"), 0.0d);
		assertEquals(1.0d, mqt.getTermWeight("dog"), 0.0d);
		
		
		srq = m.newSearchRequest("testQuery", "fox fox dog^1.3");
		srq.addMatchingModel(Full.class.getName(), PL2.class.getName());
		m.runSearchRequest(srq);
		mqt = ((Request)srq).getMatchingQueryTerms();
		assertEquals(2.0d, mqt.getTermWeight("fox"), 0.0d);
		assertEquals(1.3d, mqt.getTermWeight("dog"), 0.0d);
		
	}
	
}
