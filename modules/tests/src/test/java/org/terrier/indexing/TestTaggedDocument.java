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
 * The Original Code is TestTaggedDocument.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.indexing;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.HashMap;

import org.junit.Test;
import org.terrier.indexing.tokenisation.EnglishTokeniser;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;

public class TestTaggedDocument extends BaseTestDocument {

	protected static Tokeniser ENGLISH_TOKENISER = new EnglishTokeniser();
	protected Document makeDocument(String[] terms, Tokeniser tokeniser)
	{
		return makeDocument(ArrayUtils.join(terms, ' '), tokeniser); 
	}
	
	protected Document makeDocument(String text, Tokeniser tokeniser) {
		return new TaggedDocument(new StringReader(text), new HashMap<String, String>(), tokeniser);
	}
	
	@Test public void testProcess()
	{
		ApplicationSetup.setProperty("TrecDocTags.process", "body");
		testDocument(makeDocument("firstly <body>hello</body>", ENGLISH_TOKENISER), "hello");
	}
	
	@Test public void testSkip()
	{
		testDocument(makeDocument("<DOCHDR>firstly </DOCHDR> <BODY>hello</BODY>", ENGLISH_TOKENISER), "hello");		
		ApplicationSetup.setProperty("TrecDocTags.skip", "body");
		testDocument(makeDocument("firstly<body>hello</body>", ENGLISH_TOKENISER), "firstly");
		testDocument(makeDocument("firstly <body>hello</body>", ENGLISH_TOKENISER), "firstly");
		
		ApplicationSetup.setProperty("TrecDocTags.skip", "script");		
		//non empty: token
		testDocument(makeDocument("firstly <script>hello</script> secondly", ENGLISH_TOKENISER), "firstly", "secondly");
		testDocument(makeDocument("firstly <script src=\"a.js\">hello</script> secondly", ENGLISH_TOKENISER), "firstly", "secondly");

		//non empty: tokens and cdata
		testDocument(makeDocument("firstly <script type=\"text/javascript\">/*<![CDATA[*/ var skin = \"monobook\"; /*]]>*/</script> secondly", ENGLISH_TOKENISER), "firstly", "secondly");
		testDocument(makeDocument("firstly <script type= \"text/javascript\">/*<![CDATA[*/ var skin = \"monobook\"; /*]]>*/</script> secondly", ENGLISH_TOKENISER), "firstly", "secondly");
		
		//empty
		testDocument(makeDocument("firstly <script></script> secondly", ENGLISH_TOKENISER), "firstly", "secondly");
		testDocument(makeDocument("firstly <script src=\"a.js\"></script> secondly", ENGLISH_TOKENISER), "firstly", "secondly");
		
		//empty non-tokens
		testDocument(makeDocument("firstly <script src=\"a.js\"> </script> secondly", ENGLISH_TOKENISER), "firstly", "secondly");
		testDocument(makeDocument("firstly <script><!-- not a script --></script> secondly", ENGLISH_TOKENISER), "firstly", "secondly");
	}	

	@Test public void testFields()
	{
		ApplicationSetup.setProperty("FieldTags.process", "title,body");
		testDocument(makeDocument("<title> a </title> <body> b </body> <b> c </b> ", ENGLISH_TOKENISER), "a:TITLE", "b:BODY", "c");			
	}	
	
	@Test public void testSingleTermNoHTML()
	{
		testDocument(makeDocument("hello", ENGLISH_TOKENISER), "hello");
	}
	
	@Test public void testMulipleTermsNoHTML()
	{
		testDocument(makeDocument("hello there, mr wolf", ENGLISH_TOKENISER), "hello", "there", "mr", "wolf");
	}
	
	@Test public void testSingleTermHTML()
	{
		testDocument(makeDocument("<body>hello</body>", ENGLISH_TOKENISER), "hello");
		testDocument(makeDocument(" <body>hello</body>", ENGLISH_TOKENISER), "hello");
		testDocument(makeDocument("<body style=\"font-style: italic\">hello</body>", ENGLISH_TOKENISER), "hello");	
	}
	
	
	@Test public void testMulipleTermsHTML()
	{
		testDocument(makeDocument("hello there, <b>mr</b> wolf", ENGLISH_TOKENISER), "hello", "there", "mr", "wolf");
		
		testDocument(makeDocument("hello there, <br>mr wolf", ENGLISH_TOKENISER), "hello", "there", "mr", "wolf");
		testDocument(makeDocument("hello there, <br/>mr wolf", ENGLISH_TOKENISER), "hello", "there", "mr", "wolf");

		testDocument(makeDocument("hello there, <B><I>mr</b></i> wolf", ENGLISH_TOKENISER), "hello", "there", "mr", "wolf");
		
		testDocument(makeDocument("hello there, <b>mr</b> <font color=\"red\">wolf</font>", ENGLISH_TOKENISER), "hello", "there", "mr", "wolf");
	}
	
	@Test public void testMulipleTermsHTMLComment()
	{
		testDocument(makeDocument("hello there, <!-- mr --> wolfie", ENGLISH_TOKENISER), "hello", "there", "wolfie");
	}

	@Test public void testMulipleTermsHTMLEntities()
	{
		testDocument(makeDocument("hello there, <!-- mr --> wolfie&amp;man", ENGLISH_TOKENISER), "hello", "there", "wolfie", "man");
		testDocument(makeDocument("hello there, <!-- mr --> wolfie&amp<b>man</b>", ENGLISH_TOKENISER), "hello", "there", "wolfie", "man");
	}
	
	@Test
	public void testSpaceInTagBodies()
	{
		ApplicationSetup.setProperty("TrecDocTags.process", "body" );
		testDocument(makeDocument("<reda><body>hello</body></reda>", ENGLISH_TOKENISER), "hello" ); 
		testDocument(makeDocument("<reda ><body>hello</body></reda>", ENGLISH_TOKENISER), "hello" );
	}
	
	@Test public void testAbstractCreationTitleCropped() {
		
		//ApplicationSetup.setProperty("TrecDocTags.process", "TITLE,TEXT");
		ApplicationSetup.setProperty("TaggedDocument.abstracts", "TITLE");
		ApplicationSetup.setProperty("TaggedDocument.abstracts.tags", "TITLE");
		ApplicationSetup.setProperty("TaggedDocument.abstracts.lengths", "11");
		
		Document d = makeDocument(
				"<TITLE>No 'title' LIKE THIS title</TITLE>\n"+
				"<TEXT>test</TEXT>\n", 
				ENGLISH_TOKENISER);
		while(! d.endOfDocument()) d.getNextTerm();
		assertEquals("No 'title'", d.getProperty("TITLE"));
	}
	
	@Test public void testAbstractCreationTitle() {	
		
		ApplicationSetup.setProperty("TaggedDocument.abstracts", "TITLE");
		ApplicationSetup.setProperty("TaggedDocument.abstracts.tags", "TITLE");
		ApplicationSetup.setProperty("TaggedDocument.abstracts.lengths", "11");
		
		Document d = makeDocument(
					"<TITLE>NOT</TITLE>\n"+
					"<TEXT>test this here now</TEXT>\n", ENGLISH_TOKENISER);
		while(! d.endOfDocument()) d.getNextTerm();
		assertEquals("NOT", d.getProperty("TITLE"));
	}
	
	@Test public void testAbstractCreation_roottag() {
		
		ApplicationSetup.setProperty("TrecDocTags.process", "");
		ApplicationSetup.setProperty("TaggedDocument.abstracts", "body");
		ApplicationSetup.setProperty("TaggedDocument.abstracts.tags", "ELSE");
		ApplicationSetup.setProperty("TaggedDocument.abstracts.lengths", "11");
		
		Document d;
		
		d = makeDocument(
				"<TEXT>test</TEXT>\n", 
				ENGLISH_TOKENISER);
		while(! d.endOfDocument()) d.getNextTerm();
		assertEquals("test", d.getProperty("body"));
		
	}
	
	
	@Test public void testAbstractCreation_noroottag() {
		
		ApplicationSetup.setProperty("TrecDocTags.process", "");
		ApplicationSetup.setProperty("TaggedDocument.abstracts", "body");
		ApplicationSetup.setProperty("TaggedDocument.abstracts.tags", "ELSE");
		ApplicationSetup.setProperty("TaggedDocument.abstracts.lengths", "11");
		
		Document d;
		
		d = makeDocument(
				"test\n", 
				ENGLISH_TOKENISER);
		while(! d.endOfDocument()) d.getNextTerm();
		assertEquals("test", d.getProperty("body"));
		
		
	}
	
}
