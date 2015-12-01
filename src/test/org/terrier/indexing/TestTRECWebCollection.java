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
 * The Original Code is TestTRECWebCollection.java.
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
import static org.terrier.indexing.TestTRECCollection.checkContents;

import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.Files;

public class TestTRECWebCollection extends ApplicationSetupBasedTest  {

	protected Collection openCollection(String dataFilename) throws Exception {
		return new TRECWebCollection(Files.openFileStream(dataFilename));
	}
	
	//test a document with GOV style DOCHDR tag
	@Test public void testSingleDocumentSingleTermGOV() throws Exception
	{
		String dataFilename = writeTemporaryFile("test.trec", new String[]{
				"<DOC>",
				"<DOCNO>doc1</DOCNO>",
				"<DOCHDR>http://host/document/</DOCHDR>",
				"test",
				"</DOC>"
			});
		Collection c = openCollection(dataFilename);
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc1", d.getProperty("docno"));
		assertEquals("http://host/document/", d.getProperty("url"));		
		checkContents(d, "test");
		assertFalse(c.nextDocument());
		c.close();
	}
	
	//test a document with WT2G style DOCHDR tag
	@Test public void testSingleDocumentSingleTermWT2G() throws Exception
	{
		String dataFilename = writeTemporaryFile("test.trec", new String[]{
				"<DOC>",
				"<DOCNO>doc1</DOCNO>",
				"<DOCHDR>",
				"http://www.social.com:80/social/hypermail/news/online_news/Jan_21_Jan_27_1996/0014.html 192.131.22.19 19970114120121 text/html 2195",
				"HTTP/1.0 200 OK",
				"Server: Netscape-Commerce/1.12",
				"Date: Tuesday, 14-Jan-97 11:57:52 GMT",
				"Last-modified: Sunday, 28-Jan-96 05:49:42 GMT",
				"Content-length: 2011",
				"Content-type: text/html",
				"</DOCHDR>",
				"test",
				"</DOC>"
			});
		Collection c = openCollection(dataFilename);
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc1", d.getProperty("docno"));
		assertEquals("http://www.social.com:80/social/hypermail/news/online_news/Jan_21_Jan_27_1996/0014.html", d.getProperty("url"));
		assertEquals("text/html", d.getProperty("contenttype"));
		assertEquals("Sunday, 28-Jan-96 05:49:42 GMT", d.getProperty("lastmodified"));
		assertEquals("2195", d.getProperty("docbytelength"));
		assertEquals("853243281", d.getProperty("crawldate"));
		checkContents(d, "test");
		assertFalse(c.nextDocument());
		c.close();
	}
	
	//test a document with Blogs06 style DOCHDR tag
	@Test public void testSingleDocumentSingleTermBlogs06() throws Exception
	{
		String dataFilename = writeTemporaryFile("test.trec", new String[]{
				"<DOC>",
				"<DOCNO>doc1</DOCNO>",
				"<DOCHDR>",
				"http://anotherblog.blogspot.com/2008/07/hello.html 0.0.0.0 200893030031 29410",
				"Cache-Control: max-age=0 private",
				"Date: Tue, 30 Sep 2008 20:00:27 GMT",
				"Via: 1.0 bottle:8080 (squid/2.6.STABLE18)",
				"ETag: \"6b85396f-e500-49d4-b498-f51ee4cb46ba\"",
				"Server: GFE/1.3",
				"Content-Length: 28950",
				"Content-Range: bytes 0-28949/28950",
				"Content-Type: text/html; charset=UTF-8",
				"Last-Modified: Tue, 30 Sep 2008 12:42:16 GMT",
				"Client-Date: Tue, 30 Sep 2008 20:00:28 GMT",
				"Client-Peer: 130.209.6.42:8080",
				"Client-Response-Num: 1",
				"Proxy-Connection: close",
				"X-Cache: MISS from bottle",
				"",
				"",
				"</DOCHDR>",
				"test",
				"</DOC>"
			});
		Collection c = openCollection(dataFilename);
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc1", d.getProperty("docno"));
		assertEquals("http://anotherblog.blogspot.com/2008/07/hello.html", d.getProperty("url"));
		assertEquals("text/html; charset=UTF-8", d.getProperty("contenttype"));
		assertEquals("UTF-8", d.getProperty("charset"));
		assertEquals("Tue, 30 Sep 2008 12:42:16 GMT", d.getProperty("lastmodified"));
		assertEquals("29410", d.getProperty("docbytelength"));
		checkContents(d, "test");
		assertFalse(c.nextDocument());
		c.close();
	}

}
