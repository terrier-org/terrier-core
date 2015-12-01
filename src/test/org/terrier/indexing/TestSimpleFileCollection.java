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
 * The Original Code is TestSimpleFileCollection.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.indexing;

import java.io.PrintWriter;

import static org.junit.Assert.*;
import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;


public class TestSimpleFileCollection extends ApplicationSetupBasedTest {

	static final String[] LOREM_IPSUM = ("lorem ipsum dolor sit amet consetetur sadipscing elitr sed diam nonumy eirmod tempor "
		+"invidunt ut labore et dolore magna aliquyam erat sed diam voluptua at vero eos et accusam et justo duo "
		+"dolores et ea rebum stet clita kasd gubergren no sea takimata sanctus est lorem ipsum dolor sit amet").split(" ");
	static final String[] EXTS = new String[]{ "txt", "html", "pdf", "doc", /*"xls",*/ "ppt", /*"docx", "xlsx", "pptx"*/
	};
	
	/* fails: 
	 * xls contains "Sheet1"
	 * xlsx contains "Sheet1"
	 * docx contains text before table, rather than inline
	 */
	
	@SuppressWarnings("unchecked")
	static final Class<? extends Document>[] CLASSES = new Class[]{
		FileDocument.class, TaggedDocument.class, PDFDocument.class, 
		POIDocument.class, //doc
		//POIDocument.class, //xls
		POIDocument.class, //ppt
		//POIDocument.class, //docx	
		//POIDocument.class, //xslx	
		//POIDocument.class, //pptx
	};
	
	
	@Test public void testSimpleFileCollection() throws Exception
	{
		final PrintWriter p = new PrintWriter(Files.writeFileWriter(ApplicationSetup.TERRIER_ETC +  "/collection.spec"));
		for(String ext : EXTS)
		{
			p.println(ApplicationSetup.TERRIER_SHARE + "/tests/simplefilecollection/document." + ext);
		}
		p.close();
		Collection c = new SimpleFileCollection();
		for(int i=0;i<EXTS.length;i++)
		{
			Class<? extends Document> docClass = CLASSES[i];
			assertFalse(c.endOfCollection());
			assertTrue(c.nextDocument());
			Document d = c.getDocument();
			assertNotNull("Did not get a document for extension ."+ EXTS[i], d);
			assertTrue(d.getProperty("filename").endsWith("document."+EXTS[i]));
			assertNotNull(d);
			assertTrue(docClass.isInstance(d));
			BaseTestDocument.testDocument(d, LOREM_IPSUM);
		}
		assertFalse(c.nextDocument());
		assertTrue(c.endOfCollection());
		c.close();
	}
	
}
