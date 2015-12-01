/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is TestTRECQuery.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import org.junit.Test;

import org.terrier.applications.batchquerying.TRECQuery;
import org.terrier.applications.batchquerying.QuerySource;
import org.terrier.utility.ApplicationSetup;

import static org.junit.Assert.*;
/** Test TRECQuery behaves as expected */
public class TestTRECQuery extends TestQuerySource {

	@Override
	protected QuerySource getQuerySource(String filename) throws Exception
	{
		return new TRECQuery(filename);
	}
		
	@Test public void testOneNoClosing() throws Exception {
		QuerySource source = processString("<top>\n<num> Number: 4\n<title> defination Gravitational\n</top>");
		assertTrue(source.hasNext());
		String query = source.next();
		assertEquals("defination gravitational", query);
		assertEquals("4", source.getQueryId());
		assertFalse(source.hasNext());
	}
	
	@Test public void testOneClosing() throws Exception {
		QuerySource source = processString("<top>\n<num> Number: 4\n</num><title> defination Gravitational\n</title></top>");
		assertTrue(source.hasNext());
		String query = source.next();
		assertEquals("defination gravitational", query);
		assertEquals("4", source.getQueryId());
		assertFalse(source.hasNext());
	}
	
	@Test public void testTwoClosing() throws Exception {
		QuerySource source = processString(
				"<top>\n<num> Number: 4\n</num><title> defination Gravitational\n</title></top>"
			+	"<top>\n<num> Number: 5\n</num><title> another query\n</title></top>"
			);
		String query;
		assertTrue(source.hasNext());
		query = source.next();
		assertEquals("defination gravitational", query);
		assertEquals("4", source.getQueryId());
		
		assertTrue(source.hasNext());
		query = source.next();
		assertEquals("another query", query);
		assertEquals("5", source.getQueryId());
		
		assertFalse(source.hasNext());
	}
	
	@Test public void testTwoNoClosing() throws Exception {
		QuerySource source = processString(
				"<top>\n<num> Number: 4\n<title> defination Gravitational\n</top>"
			+	"<top>\n<num> Number: 5\n<title> another query\n</top>"
			);
		String query;
		assertTrue(source.hasNext());
		query = source.next();
		assertEquals("defination gravitational", query);
		assertEquals("4", source.getQueryId());
		
		assertTrue(source.hasNext());
		query = source.next();
		assertEquals("another query", query);
		assertEquals("5", source.getQueryId());
		
		assertFalse(source.hasNext());
	}
	

	@Test public void testTwoAndEmpty() throws Exception {
		QuerySource source = processString(
				"<top>\n<num> Number: 4\n<title> defination Gravitational\n</top>"
			+	"<top>\n<num> Number: 41\n<title> \n</top>"
			+	"<top>\n<num> Number: 5\n<title> another query\n</top>"
			);
		String query;
		assertTrue(source.hasNext());
		query = source.next();
		assertEquals("defination gravitational", query);
		assertEquals("4", source.getQueryId());
		
		assertTrue(source.hasNext());
		query = source.next();
		assertEquals("another query", query);
		assertEquals("5", source.getQueryId());
		
		assertFalse(source.hasNext());
	}
	
	@Test public void testOneLongQID() throws Exception {
		QuerySource source = processString("<top>\n<num> Number: 4444\n<title> defination Gravitational\n</top>");
		assertTrue(source.hasNext());
		String query = source.next();
		assertEquals("defination gravitational", query);
		assertEquals("4444", source.getQueryId());
		assertFalse(source.hasNext());
	}
	
	@Test public void testNumericDocno() throws Exception {
		
		ApplicationSetup.setProperty("TrecQueryTags.doctag","topic");
		ApplicationSetup.setProperty("TrecQueryTags.idtag","num");
		ApplicationSetup.setProperty("TrecQueryTags.process","title,desc");
		ApplicationSetup.setProperty("TrecQueryTags.skip","narr");
		
		QuerySource source = processString("<topic>"+'\n'+
				"<num>1122</num>"+'\n'+
				"<title>MRSA and wound infection</title>" +'\n'+
				"<desc>What is MRSA infection and is it dangerous?</desc>"+'\n'+
				"<narr>Documents should contain information about sternal wound infection by MRSA. They should describe the causes and the complications."+'\n'+
				"</narr>"+'\n'+ 
				"</topic> ");
		assertTrue(source.hasNext());
		String query = source.next();
		assertEquals("mrsa and wound infection what is mrsa infection and is it dangerous", query);
		assertEquals("1122", source.getQueryId());
		assertFalse(source.hasNext());
	}
	
	
}
