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
 * The Original Code is TestCrawlDate.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.indexing;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestCrawlDate {

	@Test public void testTRECWebCollection()
	{
		assertEquals("", TRECWebCollection.parseDate(null));
		assertEquals("852707373", TRECWebCollection.parseDate("19970108070933"));
		assertEquals("852548585", TRECWebCollection.parseDate("19970106110305"));
	}
	
//	@Test public void testWARC019Collection()
//	{
//		assertEquals("", WARC018Collection.parseDate(null));
//		assertEquals("852707373", WARC018Collection.parseDate("2009-03-65T08:43:19-0800"));
//	}
	
	@Test public void testWARC09Collection()
	{
		assertEquals("", WARC09Collection.parseDate(null));
		assertEquals("1158795830", WARC09Collection.parseDate("20060920234350"));
	}
	
}
