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
 * The Original Code is TestFileDocument.java.
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

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.terrier.indexing.tokenisation.EnglishTokeniser;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;

public class TestFileDocument extends BaseTestDocument {

	protected static Tokeniser ENGLISH_TOKENISER = new EnglishTokeniser();
	protected Document makeDocument(String[] terms, Tokeniser tokeniser)
	{
		return makeDocument(ArrayUtils.join(terms, ' '), tokeniser); 
	}
	
	protected Document makeDocument(String text, Tokeniser tokeniser)
	{
		Map<String,String> docProperties = new HashMap<String,String>();
		docProperties.put("filename", "test.txt");
		return new FileDocument(new ByteArrayInputStream(text.getBytes()), docProperties, tokeniser); 
	}
	
	@Test public void testSingleTerm()
	{
		testDocument(makeDocument("hello", ENGLISH_TOKENISER), "hello");
	}
	
	@Test public void testMulipleTerms()
	{
		testDocument(makeDocument("hello there, mr wolf", ENGLISH_TOKENISER), "hello", "there", "mr", "wolf");
	}
	
	@Test public void testMulipleTermsAbstracts()
	{
		//System.err.println("testMulipleTermsAbstracts");
		ApplicationSetup.setProperty("FileDocument.abstract", "SUPERAWSOMEHEADER");
		ApplicationSetup.setProperty("FileDocument.abstract.length", "12");
		Document d = makeDocument("Hello there, mr wolf", ENGLISH_TOKENISER);
		assertFalse(d.endOfDocument());
		while(! d.endOfDocument())
		{
			d.getNextTerm();
		}
		assertEquals("Hello there,", d.getProperty("SUPERAWSOMEHEADER"));
		
	}
	
}
