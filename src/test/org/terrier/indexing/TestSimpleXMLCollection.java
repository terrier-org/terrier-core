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
 * The Original Code is TestSimpleXMLCollection.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Nut Limsopatham
 *   
 */
package org.terrier.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

public class TestSimpleXMLCollection extends ApplicationSetupBasedTest {

	protected SimpleXMLCollection getCollection(String xml) throws Exception
	{
		File f = super.tmpfolder.newFile("testFile.xml");
		if (f.exists())
			f.delete();
		Writer w = Files.writeFileWriter(f);
		w.write(xml);
		w.close();
		List<String> l = new ArrayList<String>();
		l.add(f.toString());
		
		return new SimpleXMLCollection(l);
	}
	
	@Test public void testLisi1SingleDocument() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag","patent-document");
		ApplicationSetup.setProperty("xml.idtag","patent-document.ucid");
		ApplicationSetup.setProperty("xml.terms", "abstract");
		SimpleXMLCollection c = getCollection(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+"<patent-document ucid=\"2\">"
			+"<abstract lang=\"EN\" load-source=\"us\" status=\"new\">"
			+"Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
			+"</abstract></patent-document> ");
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertFalse(d.endOfDocument());
		String t = d.getNextTerm();
		assertEquals("lorem", t);
		t = d.getNextTerm();
		assertEquals("ipsum", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("dolor", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("sit", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("amet", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("consectetur", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("adipiscing", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("elit", t);
		assertTrue(d.endOfDocument());
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
	@Test public void testLisi1SingleDocumentWithDTD() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag","patent-document");
		ApplicationSetup.setProperty("xml.idtag","patent-document.ucid");
		ApplicationSetup.setProperty("xml.terms", "abstract");
		//write DTD
		File f = super.tmpfolder.newFile("mydtd.dtd");
		if (f.exists())
			f.delete();
		Writer w = Files.writeFileWriter(f);
		w.write(
				"<!ELEMENT patent-document ( text ) > "
				+"<!ATTLIST patent-document ucid NMTOKEN #REQUIRED >"
				+"<!ELEMENT abstract ( #PCDATA ) > ");
		w.close();
		SimpleXMLCollection c = getCollection(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+"<!DOCTYPE document SYSTEM \""+f.toString()+"\">"
			+"<patent-document ucid=\"2\">"
			+"<abstract lang=\"EN\" load-source=\"us\" status=\"new\">"
			+"Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
			+"</abstract></patent-document> ");
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertFalse(d.endOfDocument());
		String t = d.getNextTerm();
		assertEquals("lorem", t);
		t = d.getNextTerm();
		assertEquals("ipsum", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("dolor", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("sit", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("amet", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("consectetur", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("adipiscing", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("elit", t);
		assertTrue(d.endOfDocument());
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
	
	@Test public void testSingleTermSingleDocument() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag", "doc");
		ApplicationSetup.setProperty("xml.terms", "doc");
		SimpleXMLCollection c = getCollection("<?xml version=\"1.0\"?><doc>test </doc>");
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertFalse(d.endOfDocument());
		String t = d.getNextTerm();
		assertEquals("test", t);
		assertTrue(d.endOfDocument());
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
	@Test public void testSingleTermSingleDocumentWithDocType() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag", "test-doctype");
		ApplicationSetup.setProperty("xml.terms", "test-doctype");
		SimpleXMLCollection c = getCollection("<?xml version=\"1.0\"?><!DOCTYPE test-doctype><test-doctype>test</test-doctype>");
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertFalse(d.endOfDocument());
		String t = d.getNextTerm();
		assertEquals("test", t);
		assertTrue(d.endOfDocument());
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
	@Test public void testSingleTermSingleDocumentWithDTD() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag", "doc");
		ApplicationSetup.setProperty("xml.terms", "doc");
		//write DTD
		File f = super.tmpfolder.newFile("mydtd.dtd");
		if (f.exists())
			f.delete();
		Writer w = Files.writeFileWriter(f);
		w.write("<!ELEMENT doc ( text ) > ");
		w.close();
		//write and open XML file
		SimpleXMLCollection c = getCollection(
				"<?xml version=\"1.0\"?>"
				+"<!DOCTYPE document SYSTEM \""+f.toString()+"\">"
				+"<doc>test</doc>");
		
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertFalse(d.endOfDocument());
		String t = d.getNextTerm();
		assertEquals("test", t);
		assertTrue(d.endOfDocument());
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
	@Test public void testSingleTermNoSpaceSingleDocument() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag", "doc");
		ApplicationSetup.setProperty("xml.terms", "doc");
		SimpleXMLCollection c = getCollection("<?xml version=\"1.0\"?><doc>test</doc>");
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertFalse(d.endOfDocument());
		String t = d.getNextTerm();
		assertEquals("test", t);
		assertTrue(d.endOfDocument());
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
	@Test public void testMultipleTermSingleDocument() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag", "doc");
		ApplicationSetup.setProperty("xml.terms", "doc");
//		SimpleXMLCollection c = getCollection("<?xml version=\"1.0\"?><doc>test1 test2 test3 </doc>");
		SimpleXMLCollection c = getCollection("<?xml version=\"1.0\"?><doc>test1 <p>test2</p> test3 </doc>");
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		String t;
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test1", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test2", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test3", t);
		assertTrue(d.endOfDocument());		
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
	@Test public void testMultipleTermSingleDocumentFields() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag", "doc");
		ApplicationSetup.setProperty("xml.terms", "doc");
//		SimpleXMLCollection c = getCollection("<?xml version=\"1.0\"?><doc>test1 test2 test3 </doc>");
		SimpleXMLCollection c = getCollection("<?xml version=\"1.0\"?><doc>test1 <p>test2</p> test3 </doc>");
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		String t;
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test1", t);
		assertTrue(d.getFields().contains("doc"));
		assertFalse(d.getFields().contains("p"));
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test2", t);
		assertTrue(d.getFields().contains("doc"));
		assertTrue(d.getFields().contains("p"));
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test3", t);
		assertTrue(d.getFields().contains("doc"));
		assertFalse(d.getFields().contains("p"));
		assertTrue(d.endOfDocument());		
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
	@Test public void testMultipleTermMultipleDocumentAttrDocno() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag", "doc");
		ApplicationSetup.setProperty("xml.terms", "doc");
		ApplicationSetup.setProperty("xml.idtag", "doc.id");
//		SimpleXMLCollection c = getCollection("<?xml version=\"1.0\"?><docs><doc id=\"1\">test1 test2 test3 </doc><doc id=\"2\">test4 test5 test6 </doc></docs>");
		SimpleXMLCollection c = getCollection("<?xml version=\"1.0\"?><docs><doc id=\"1\">test1 <p>test2</p> test3 </doc><doc id=\"2\">test4 test5 test6 </doc></docs>");
		Document d;
		String t;
		
		//first doc
		assertTrue(c.nextDocument());
		d = c.getDocument();
		assertNotNull(d);
		assertEquals("1", d.getProperty("docno"));
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test1", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test2", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test3", t);
		assertTrue(d.endOfDocument());	
		
		//2nd doc
		assertTrue(c.nextDocument());
		d = c.getDocument();
		assertNotNull(d);
		assertEquals("2", d.getProperty("docno"));
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test4", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test5", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test6", t);
		assertTrue(d.endOfDocument());	
		
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
	@Test public void testMultipleTermMultipleDocumentElementDocno() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag", "doc");
		ApplicationSetup.setProperty("xml.terms", "doc");
		ApplicationSetup.setProperty("xml.idtag", "id");
//		SimpleXMLCollection c = getCollection("<?xml version=\"1.0\"?><docs><doc><id>1</id>test1 test2 test3 </doc><doc><id>2</id>test4 test5 test6</doc></docs>");
		SimpleXMLCollection c = getCollection("<?xml version=\"1.0\"?><docs><doc><id>1</id>test1 <p>test2</p> test3 </doc><doc><id>2</id>test4 <p>test5</p> <x>test6</x></doc></docs>");
		Document d;
		String t;
		
		//first doc
		assertTrue(c.nextDocument());
		d = c.getDocument();
		assertNotNull(d);
		assertEquals("1", d.getProperty("docno"));
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("1", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test1", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test2", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test3", t);
		assertTrue(d.endOfDocument());	
		
		//2nd doc
		assertTrue(c.nextDocument());
		d = c.getDocument();
		assertNotNull(d);
		assertEquals("2", d.getProperty("docno"));
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("2", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test4", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test5", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("test6", t);
		assertTrue(d.endOfDocument());	
		
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
	@Test public void testLisi1SingleDocumentWithInnerTag() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag","patent-document");
		ApplicationSetup.setProperty("xml.idtag","patent-document.ucid");
		ApplicationSetup.setProperty("xml.terms", "abstract");
		SimpleXMLCollection c = getCollection(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+"<patent-document ucid=\"2\">"
			+"<abstract lang=\"EN\" load-source=\"us\" status=\"new\">"
			+"Lorem ipsum dolor sit <p>amet</p>, <i><u>consectetur</u> adipiscing</i> elit. "
			+"</abstract></patent-document> ");
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertFalse(d.endOfDocument());
		String t = d.getNextTerm();
		assertEquals("lorem", t);
		t = d.getNextTerm();
		assertEquals("ipsum", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("dolor", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("sit", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("amet", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("consectetur", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("adipiscing", t);
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("elit", t);
		assertTrue(d.endOfDocument());
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
	@Test public void testLisi1SingleDocumentWithInnerTagFields() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag","patent-document");
		ApplicationSetup.setProperty("xml.idtag","patent-document.ucid");
		ApplicationSetup.setProperty("xml.terms", "abstract");
		SimpleXMLCollection c = getCollection(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+"<patent-document ucid=\"2\">"
			+"<abstract lang=\"EN\" load-source=\"us\" status=\"new\">"
			+"Lorem ipsum dolor sit <p>amet</p>, <i><u>consectetur</u> adipiscing</i> elit. "
			+"</abstract></patent-document> ");
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertFalse(d.endOfDocument());
		String t = d.getNextTerm();
		assertEquals("lorem", t);
		assertTrue(d.getFields().contains("abstract"));
		assertFalse(d.getFields().contains("i"));
		t = d.getNextTerm();
		assertEquals("ipsum", t);
		assertFalse(d.endOfDocument());
		assertTrue(d.getFields().contains("abstract"));
		assertFalse(d.getFields().contains("i"));
		t = d.getNextTerm();
		assertEquals("dolor", t);
		assertFalse(d.endOfDocument());
		assertTrue(d.getFields().contains("abstract"));
		assertFalse(d.getFields().contains("i"));
		t = d.getNextTerm();
		assertEquals("sit", t);
		assertFalse(d.endOfDocument());
		assertTrue(d.getFields().contains("abstract"));
		assertFalse(d.getFields().contains("i"));
		t = d.getNextTerm();
		assertEquals("amet", t);
		assertFalse(d.endOfDocument());
		assertTrue(d.getFields().contains("p"));
		assertTrue(d.getFields().contains("abstract"));
		assertFalse(d.getFields().contains("i"));
		t = d.getNextTerm();
		assertEquals("consectetur", t);
		assertFalse(d.endOfDocument());
		assertTrue(d.getFields().contains("u"));
		assertTrue(d.getFields().contains("abstract"));
		assertTrue(d.getFields().contains("i"));
		t = d.getNextTerm();
		assertEquals("adipiscing", t);
		assertTrue(d.getFields().contains("i"));
		assertTrue(d.getFields().contains("abstract"));
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("elit", t);
		assertTrue(d.getFields().contains("abstract"));
		assertTrue(d.endOfDocument());
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
	@Test public void testLisi1SingleDocumentWithMultipleInnerTagFields() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag","patent-document");
		ApplicationSetup.setProperty("xml.idtag","patent-document.ucid");
		ApplicationSetup.setProperty("xml.terms", "abstract");
		SimpleXMLCollection c = getCollection(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+"<patent-document ucid=\"2\">"
			+"<abstract lang=\"EN\" load-source=\"us\" status=\"new\">"
			+"Lorem <p>ipsum</p> dolor sit <i><p>amet</p>, </i><i><u>consectetur</u> adipiscing</i> elit. "
			+"</abstract></patent-document> ");
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertFalse(d.endOfDocument());
		String t = d.getNextTerm();
		assertEquals("lorem", t);
		assertTrue(d.getFields().contains("abstract"));
		assertFalse(d.getFields().contains("i"));
		assertTrue(!d.getFields().contains("p"));
		t = d.getNextTerm();
		assertEquals("ipsum", t);
		assertFalse(d.endOfDocument());
		assertTrue(d.getFields().contains("abstract"));
		assertTrue(d.getFields().contains("p"));
		assertFalse(d.getFields().contains("i"));
		t = d.getNextTerm();
		assertEquals("dolor", t);
		assertFalse(d.endOfDocument());
		assertTrue(d.getFields().contains("abstract"));
		assertFalse(d.getFields().contains("i"));
		assertTrue(!d.getFields().contains("p"));
		t = d.getNextTerm();
		assertEquals("sit", t);
		assertFalse(d.endOfDocument());
		assertTrue(d.getFields().contains("abstract"));
		assertFalse(d.getFields().contains("i"));
		t = d.getNextTerm();
		assertEquals("amet", t);
		assertFalse(d.endOfDocument());
		assertTrue(d.getFields().contains("p"));
		assertTrue(d.getFields().contains("abstract"));
		assertTrue(d.getFields().contains("i"));
		t = d.getNextTerm();
		assertEquals("consectetur", t);
		assertFalse(d.endOfDocument());
		assertTrue(d.getFields().contains("u"));
		assertTrue(d.getFields().contains("abstract"));
		assertTrue(d.getFields().contains("i"));
		assertTrue(!d.getFields().contains("p"));
		t = d.getNextTerm();
		assertEquals("adipiscing", t);
		assertTrue(d.getFields().contains("i"));
		assertTrue(d.getFields().contains("abstract"));
		assertTrue(!d.getFields().contains("u"));
		assertFalse(d.endOfDocument());
		t = d.getNextTerm();
		assertEquals("elit", t);
		assertTrue(d.getFields().contains("abstract"));
		assertTrue(!d.getFields().contains("i"));
		assertTrue(d.endOfDocument());
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
	@Test public void testLisi1SingleDocumentWithSelectiveMultipleInnerTagFields() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag","patent-document");
		ApplicationSetup.setProperty("xml.idtag","patent-document.ucid");
		ApplicationSetup.setProperty("xml.terms", "doc,sub");
		SimpleXMLCollection c = getCollection(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+"<patent-document ucid=\"2\">"
			+"<abstract lang=\"EN\" load-source=\"us\" status=\"new\">"
			+"Lorem <doc><a><p>ipsum</p></a> dolor sit <i><p>amet</p>, </i></doc><i><sub><u>consectetur</u> adipiscing</sub></i> elit. "
			+"</abstract></patent-document> ");
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertFalse(d.endOfDocument());
		String t = d.getNextTerm();
		assertEquals("ipsum", t);
		assertFalse(d.endOfDocument());
		assertTrue(!d.getFields().contains("abstract"));
		assertTrue(d.getFields().contains("doc"));
		assertTrue(d.getFields().contains("p"));
		assertFalse(d.getFields().contains("i"));
		t = d.getNextTerm();
		assertEquals("dolor", t);
		assertFalse(d.endOfDocument());
		assertTrue(!d.getFields().contains("abstract"));
		assertTrue(d.getFields().contains("doc"));
		assertFalse(d.getFields().contains("i"));
		assertTrue(!d.getFields().contains("p"));
		t = d.getNextTerm();
		assertEquals("sit", t);
		assertFalse(d.endOfDocument());
		assertTrue(!d.getFields().contains("abstract"));
		assertFalse(d.getFields().contains("i"));
		t = d.getNextTerm();
		assertEquals("amet", t);
		assertFalse(d.endOfDocument());
		assertTrue(d.getFields().contains("p"));
		assertTrue(!d.getFields().contains("abstract"));
		assertTrue(d.getFields().contains("doc"));
		assertTrue(d.getFields().contains("i"));
		t = d.getNextTerm();
		assertEquals("consectetur", t);
		assertFalse(d.endOfDocument());
		assertTrue(d.getFields().contains("u"));
		assertTrue(!d.getFields().contains("abstract"));
		assertTrue(!d.getFields().contains("i"));
		assertTrue(!d.getFields().contains("p"));
		t = d.getNextTerm();
		assertEquals("adipiscing", t);
		assertTrue(!d.getFields().contains("i"));
		assertTrue(d.getFields().contains("sub"));
		assertTrue(!d.getFields().contains("abstract"));
		assertTrue(!d.getFields().contains("u"));
		assertFalse(!d.endOfDocument());
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
		assertTrue(d.endOfDocument());
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
	@Test public void testSingleTermSingleDocumentWithDTDMetaTags() throws Exception
	{
		ApplicationSetup.setProperty("xml.doctag", "doc");
		ApplicationSetup.setProperty("xml.terms", "doc");
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "meta.name");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "20");

		//write DTD
		File f = super.tmpfolder.newFile("mydtd.dtd");
		if (f.exists())
			f.delete();
		Writer w = Files.writeFileWriter(f);
		w.write("<!ELEMENT doc ( text ) > ");
		w.close();
		//write and open XML file
		SimpleXMLCollection c = getCollection(
				"<?xml version=\"1.0\"?>"
				+"<!DOCTYPE document SYSTEM \""+f.toString()+"\">"
				+"<doc><head>"
				+"<meta name=\"keywords\" content=\"HTML,CSS,XML,JavaScript\"/>"
				+"</head>"
				+"test</doc>");
		assertTrue(c.nextDocument());
		Document d = c.getDocument();
		assertNotNull(d);
		assertFalse(d.endOfDocument());
		String t = d.getNextTerm();
		assertEquals("test", t);
		assertTrue(d.getProperty("meta.name").equalsIgnoreCase("keywords"));
		assertTrue(d.endOfDocument());
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
	}
	
}

