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
 * The Original is in 'TestSingleLineTRECQuery.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.structures;

import static org.junit.Assert.*;

import org.junit.Test;
import org.terrier.applications.batchquerying.SingleLineTRECQuery;
import org.terrier.applications.batchquerying.QuerySource;
import org.terrier.utility.ApplicationSetup;

public class TestSingleLineTRECQuery extends TestQuerySource {

	@Test public void testSingleTerm() throws Exception
	{
		for (char join : new char[]{':', ' ', '\t'})
		{
			QuerySource tq = processString("1"+join+"term");
			assertTrue(tq.hasNext());
			assertEquals("term", tq.next());
			assertEquals("1", tq.getQueryId());
			assertFalse(tq.hasNext());
		}
	}
	
	
	@Test public void testSingleTermNoCheckPass() throws Exception
	{
		QuerySource tq;
		ApplicationSetup.setProperty("SingleLineTRECQuery.tokenise", "false");
		tq = processString("1 lillll");
		assertTrue(tq.hasNext());
		assertEquals("lillll", tq.next());
		assertEquals("1", tq.getQueryId());
		assertFalse(tq.hasNext());
		
		//lillll has more than four repeated character - is deleted by the tokeniser
		ApplicationSetup.setProperty("SingleLineTRECQuery.tokenise", "true");
		tq = processString("1 lillll");
		assertTrue(tq.hasNext());
		assertEquals("", tq.next());
		assertEquals("1", tq.getQueryId());
		assertFalse(tq.hasNext());
		ApplicationSetup.setProperty("SingleLineTRECQuery.tokenise", "false");
	}
	
	@Test public void testTwoTerms() throws Exception
	{
		QuerySource tq;
		ApplicationSetup.setProperty("SingleLineTRECQuery.tokenise", "false");
		tq = processString("1 two terms");
		assertTrue(tq.hasNext());
		assertEquals("two terms", tq.next());
		assertEquals("1", tq.getQueryId());
		assertFalse(tq.hasNext());
		
		ApplicationSetup.setProperty("SingleLineTRECQuery.tokenise", "true");
		tq = processString("1 two terms");
		assertTrue(tq.hasNext());
		assertEquals("two terms", tq.next());
		assertEquals("1", tq.getQueryId());
		assertFalse(tq.hasNext());
		ApplicationSetup.setProperty("SingleLineTRECQuery.tokenise", "false");
	}
	
	
	
	@Override
	protected QuerySource getQuerySource(String filename) throws Exception {
		return new SingleLineTRECQuery(filename);
	}

}
