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
 * The Original Code is TestTRECCollection.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Writer;

import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.tests.ShakespeareEndToEndTest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

public class TestTRECCollection extends ApplicationSetupBasedTest {
	
	public static void checkContents(Document d, String... tokens)
	{
		int i = 0;
		while(! d.endOfDocument())
		{
			String term = d.getNextTerm();
			if (term != null)
			{
				assertEquals("Mismatch at position "+i, tokens[i], term);
				i++;
			}
			
		}
		assertEquals("Unexpected number of found tokens", tokens.length, i);
	}
	
	protected Collection openCollection(String dataFilename) throws Exception
	{
		return new TRECCollection(Files.openFileStream(dataFilename));
	}
	
	//test first Shakespeare
	@Test public void testTwoFileShakespeare() throws Exception
	{
		Writer w = Files.writeFileWriter(ApplicationSetup.COLLECTION_SPEC);
		w.write(ApplicationSetup.TERRIER_SHARE + "/tests/shakespeare/shakespeare-merchant.trec.1");
		w.write("\n");
		w.write(ApplicationSetup.TERRIER_SHARE + "/tests/shakespeare/shakespeare-merchant.trec.2");
		w.write("\n");
		w.close();
		Collection c = new TRECCollection();
		int count =0;
		while(c.nextDocument())
		{
			count++;
		}
		assertEquals(ShakespeareEndToEndTest.DOCUMENT_NAMES.length, count);
		c.close();
	}
	
	//test first Shakespeare
	@Test public void testFirstShakespeare() throws Exception
	{
		Collection c = openCollection(ApplicationSetup.TERRIER_SHARE + "/tests/shakespeare/shakespeare-merchant.trec.1");
		assertTrue(c.nextDocument());
		int i=0;
		
		Document d = c.getDocument();
		assertNotNull(d);
		assertEquals(ShakespeareEndToEndTest.DOCUMENT_NAMES[i], d.getProperty("docno"));
		int tokens = 0;
		while(! d.endOfDocument())
		{
			String t= d.getNextTerm();
			if (t != null)
			{
				tokens++;
				//System.err.println(t);
			}
		}
		assertEquals(131, tokens);
		
		while(c.nextDocument())
		{
			i++;
			d = c.getDocument();
			assertEquals(ShakespeareEndToEndTest.DOCUMENT_NAMES[i], d.getProperty("docno"));
		}
		assertEquals(13, i);
		
		c.close();
	}
	
	//test a basic document
	@Test public void testSingleDocumentSingleTerm() throws Exception
	{
		String dataFilename = writeTemporaryFile("test.trec", new String[]{
				"<DOC>",
				"<DOCNO>doc1</DOCNO>",
				"test",
				"</DOC>"
			});
		Collection c = openCollection(dataFilename);
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc1", d.getProperty("docno"));
		checkContents(d, "test");
		assertFalse(c.nextDocument());
		c.close();
	}
	
	@Test public void testSingleDocumentDocnoSensitivity() throws Exception
	{
		String dataFilename = writeTemporaryFile("test.trec", new String[]{
				"<DOC>",
				"<DOCNO>doc1</DOCNO>",
				"test",
				"</DOC>"
			});
		
		ApplicationSetup.setProperty("TrecDocTags.casesensitive", "true");
		ApplicationSetup.setProperty("TrecDocTags.idtag", "docno");
		Collection c = openCollection(dataFilename);
		assertFalse(c.nextDocument()); // do documents should have been parsed
		
		
		ApplicationSetup.setProperty("TrecDocTags.idtag", "DOCNO");
		c = openCollection(dataFilename);
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc1", d.getProperty("docno")); // case sensitivity in tagged document is always on
		assertEquals("doc1", d.getProperty("DOCNO"));
		assertFalse(c.nextDocument());
		c.close();
	}
	


	
	
	//test a basic document
	@Test 
	public void testSingleDocumentSingleTermDocnoSpaces() throws Exception
	{
		String dataFilename = writeTemporaryFile("test.trec", new String[]{
				"<DOC>",
				"<DOCNO> doc1 </DOCNO>",
				"test",
				"</DOC>"
			});
		Collection c = openCollection(dataFilename);
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc1", d.getProperty("docno"));
		checkContents(d, "test");
		assertFalse(c.nextDocument());
		c.close();
	}

	
	//test a basic document
	@Test public void testSingleDocumentSingleTermMarkedCharacters() throws Exception
	{
		String dataFilename = writeTemporaryFile("test.trec", new String[]{
				"<DOC>",
				"<DOCNO>doc1</DOCNO>",
				"<b>t</b>est",
				"</DOC>"
			});
		Collection c = openCollection(dataFilename);
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc1", d.getProperty("docno"));
		checkContents(d, "t", "est");
		assertFalse(c.nextDocument());
		c.close();
	}
	
	//test a single document with a process (whitelist)
	@Test public void testSingleDocumentTwoTermProcess() throws Exception
	{
		String dataFilename = writeTemporaryFile("test.trec", new String[]{
				"<DOC>",
				"<DOCNO>doc1</DOCNO>",
				"NOT",
				"<TEXT> test</TEXT>",				
				"</DOC>"
			});
		ApplicationSetup.setProperty("TrecDocTags.process", "TEXT");
		Collection c = openCollection(dataFilename);
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc1", d.getProperty("docno"));
		checkContents(d, "test");
		assertFalse(c.nextDocument());
		c.close();
	}
	
	
	//test a single document with a property tag
	@Test public void testSingleDocumentSingleTermProperyTags() throws Exception
	{
		String dataFilename = writeTemporaryFile("test.trec", new String[]{
				"<DOC>",
				"<DOCNO>doc1</DOCNO>",
				"<url>url1</url>",				
				"test",
				"</DOC>"
			});
		ApplicationSetup.setProperty("TrecDocTags.propertytags", "url");
		Collection c = openCollection(dataFilename);
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc1", d.getProperty("docno"));
		assertEquals("url1", d.getProperty("url"));
		checkContents(d, "test");
		assertFalse(c.nextDocument());
		c.close();
	}
	
	//test a single document with a property tag
	@Test public void testMultipleDocumentMultipleTermProperyTags() throws Exception
	{
		String dataFilename = writeTemporaryFile("test.trec", new String[]{
				"<DOC>",
				"<DOCNO>doc1</DOCNO>",
				"<url>url1</url>",	
				"<date>03/03/11</date>",	
				"test",
				"</DOC>",
				"<DOC>",
				"<DOCNO>doc2</DOCNO>",
				"<url>http//www.url2.com</url>",
				"<date>01/02/11</date>",
				"test this here now",
				"</DOC>"
			});
		ApplicationSetup.setProperty("TrecDocTags.propertytags", "url,date");
		Collection c = openCollection(dataFilename);
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc1", d.getProperty("docno"));
		assertEquals("url1", d.getProperty("url"));
		assertEquals("03/03/11", d.getProperty("date"));
		checkContents(d, "test");
		assertTrue(c.nextDocument());
		d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc2", d.getProperty("docno"));
		assertEquals("http//www.url2.com", d.getProperty("url"));
		assertEquals("01/02/11", d.getProperty("date"));
		checkContents(d, "test", "this", "here", "now");
		assertFalse(c.nextDocument());
		c.close();
	}
	
	// test multiple documents, including with DOCHDRs
	@Test public void testMultipleDocumentMultipleTermDOCHDRskipped() throws Exception
	{
		String dataFilename = writeTemporaryFile("test.trec", new String[]{
				"<DOC>",
				"<DOCNO>doc1</DOCNO>",
				"<DOCHDR>NOT</DOCHDR>",
				"test",
				"</DOC>",
				"<DOC>",
				"<DOCNO>doc2</DOCNO>",
				"<DOCHDR>NOT</DOCHDR>",
				"test this here now",
				"</DOC>"
				
			});
		Collection c = openCollection(dataFilename);
		Document d;
		assertTrue(c.nextDocument());
		d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc1", d.getProperty("docno"));
		checkContents(d, "test");
		
		assertTrue(c.nextDocument());
		d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc2", d.getProperty("docno"));
		checkContents(d, "test", "this", "here", "now");
		assertFalse(c.nextDocument());
		c.close();
	}
	
	// test multiple documents, including multiple fields
	@Test public void testMultipleDocumentMultipleTermProcessed() throws Exception
	{
		ApplicationSetup.setProperty("TrecDocTags.skip", "");
		ApplicationSetup.setProperty("TrecDocTags.process", "DOCHDR,TEXT");
		String dataFilename = writeTemporaryFile("test.trec", new String[]{
				"<DOC>",
				"<DOCNO>doc1</DOCNO>",
				"<DOCHDR>NOT</DOCHDR>",
				"<TEXT>test</TEXT>",
				"</DOC>",
				"<DOC>",
				"<DOCNO>doc2</DOCNO>",
				"<DOCHDR>NOT</DOCHDR>",
				"<TEXT>test this here now</TEXT>",
				"</DOC>"
				
			});
		Collection c = openCollection(dataFilename);
		Document d;
		assertTrue(c.nextDocument());
		d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc1", d.getProperty("docno"));
		checkContents(d, "not", "test");
		
		assertTrue(c.nextDocument());
		d = c.getDocument();
		assertNotNull(d);
		assertEquals("doc2", d.getProperty("docno"));
		checkContents(d, "not", "test", "this", "here", "now");
		assertFalse(c.nextDocument());
		c.close();
	}

	
		// test multiple documents, including multiple fields
		@Test public void testMultipleDocumentMultipleTermWithAbstracts() throws Exception
		{
			ApplicationSetup.setProperty("TrecDocTags.process", "TITLE,TEXT");
			ApplicationSetup.setProperty("TaggedDocument.abstracts", "TITLE");
			ApplicationSetup.setProperty("TaggedDocument.abstracts.tags", "TITLE");
			ApplicationSetup.setProperty("TaggedDocument.abstracts.lengths", "11");
			String dataFilename = writeTemporaryFile("test.trec", new String[]{
					"<DOC>",
					"<DOCNO>doc1</DOCNO>",
					"<TITLE>No 'title' LIKE THIS title</TITLE>",
					"<TEXT>test</TEXT>",
					"</DOC>",
					"<DOC>",
					"<DOCNO>doc2</DOCNO>",
					"<TITLE>NOT</TITLE>",
					"<TEXT>test this here now</TEXT>",
					"</DOC>"
					
				});
			Collection c = new TRECCollection(Files.openFileStream(dataFilename));
			Document d;
			assertTrue(c.nextDocument());
			d = c.getDocument();
			assertNotNull(d);
			assertEquals("doc1", d.getProperty("docno"));
			checkContents(d, "no", "title", "like", "this", "title", "test");
			assertEquals("No 'title'", d.getProperty("TITLE"));
			//if (d.getProperty("SUPERAWSOMEHEADER").compareTo("no 'header'")==0) assertTrue(false);
			
			assertTrue(c.nextDocument());
			d = c.getDocument();
			assertNotNull(d);
			assertEquals("doc2", d.getProperty("docno"));
			checkContents(d, "not", "test", "this", "here", "now");
			assertEquals("NOT", d.getProperty("TITLE"));
			assertFalse(c.nextDocument());
			c.close();
		}
		
		// test multiple documents, including multiple fields
		@Test public void testMultipleDocumentMultipleTermWithMultipleAbstractTags() throws Exception
		{
			ApplicationSetup.setProperty("TrecDocTags.process", "TITLE,TEXT");
			ApplicationSetup.setProperty("TaggedDocument.abstracts", "TITLE");
			ApplicationSetup.setProperty("TaggedDocument.abstracts.tags", "TITLE");
			ApplicationSetup.setProperty("TaggedDocument.abstracts.lengths", "100");
			String dataFilename = writeTemporaryFile("test.trec", new String[]{
					"<DOC>",
					"<DOCNO>doc1</DOCNO>",
					"<TITLE>No 'title'</TITLE>",
					"<TEXT>test</TEXT>",
					"<TITLE>More title</TITLE>",
					"</DOC>",					
				});
			Collection c = new TRECCollection(Files.openFileStream(dataFilename));
			Document d;
			assertTrue(c.nextDocument());
			d = c.getDocument();
			assertNotNull(d);
			assertEquals("doc1", d.getProperty("docno"));
			checkContents(d, "no", "title", "test", "more", "title");
			assertEquals("No 'title' More title", d.getProperty("TITLE"));

			assertFalse(c.nextDocument());
			c.close();
		}
		
		
		// test multiple documents, including multiple fields
		@Test public void testMultipleDocumentMultipleTermWithAbstractsElseCase() throws Exception
		{
			ApplicationSetup.setProperty("TrecDocTags.process", "TITLE,FOREWORD,TEXT");
			ApplicationSetup.setProperty("TaggedDocument.abstracts", "TITLE,SUMMARY");
			ApplicationSetup.setProperty("TaggedDocument.abstracts.tags", "TITLE,ELSE");
			ApplicationSetup.setProperty("TaggedDocument.abstracts.lengths", "11,8");
			String dataFilename = writeTemporaryFile("test.trec", new String[]{
					"<DOC>",
					"<DOCNO>doc1</DOCNO>",
					"<TITLE>No 'title' LIKE THIS title</TITLE>",
					"<FOREWORD>Wazza</FOREWORD>",
					"<TEXT>test</TEXT>",
					"</DOC>",
					"<DOC>",
					"<DOCNO>doc2</DOCNO>",
					"<TITLE>NOT</TITLE>",
					"<FOREWORD>Wazza</FOREWORD>",
					"<TEXT>test this here<br/>now</TEXT>",
					"</DOC>"
					
				});
			Collection c = new TRECCollection(Files.openFileStream(dataFilename));
			Document d;
			assertTrue(c.nextDocument());
			d = c.getDocument();
			assertNotNull(d);
			assertEquals("doc1", d.getProperty("docno"));
			checkContents(d, "no", "title", "like", "this", "title", "wazza", "test");
			assertEquals("No 'title'", d.getProperty("TITLE"));
			assertEquals("Wazza te", d.getProperty("SUMMARY"));
			
			assertTrue(c.nextDocument());
			d = c.getDocument();
			assertNotNull(d);
			assertEquals("doc2", d.getProperty("docno"));
			checkContents(d, "not", "wazza", "test", "this", "here", "now");
			assertEquals("NOT", d.getProperty("TITLE"));
			assertEquals("Wazza te", d.getProperty("SUMMARY"));
			assertFalse(c.nextDocument());
			c.close();
		}
	
}
