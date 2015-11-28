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
 * The Original Code is TestUTFTokeniser.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.indexing.tokenisation;

import java.io.IOException;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;
import org.terrier.utility.ArrayUtils;

public class TestUTFTokeniser extends TestEnglishTokeniser {

	public static String decode(final String word)
	{
		return StringEscapeUtils.unescapeHtml(word);
	}
	
	public static String[] decode(final String[] words)
	{
		String[] rtr = new String[words.length];
		for(int i=0;i<words.length;i++)
			rtr[i] = StringEscapeUtils.unescapeHtml(words[i]);
		return rtr;
	}
	
	public TestUTFTokeniser()
	{
		super.tokeniser = new UTFTokeniser();
	}
	
	@Test public void testMulipleLongTerms() throws IOException
	{
		testTokenisation(tokenise("hello there mr wolf thisisareallylongword aye"), "hello", "there", "mr", "wolf", "aye");
	}
	
	@Override @Test public void testSingleLatinTerms() throws Exception
	{
		testTokenisation(tokenise("a\u0133a"), "a\u0133a");
		testTokenisation(tokenise("\u00C0\u00C8\u00CC"), "\u00C0\u00C8\u00CC".toLowerCase());
	}
	
	@Test public void testSingleHindiTerms() throws Exception
	{
		String word = decode("&#2327;&#2369;");
		testTokenisation(tokenise(word), word);
	}
	
	
	@Test public void testFIRETerms() throws Exception
	{
		String[] words = decode(new String[]{
				"&#2327;&#2369;&#2332;&#2381;&#2332;&#2352;&#2379;&#2306;",
				"&#2324;&#2352;&#2350;&#2368;&#2339;&#2366;",
				"&#2360;&#2350;&#2369;&#2342;&#2366;&#2351;",
				"&#2325;&#2375;&#2348;&#2368;&#2330;",
				"&#2360;&#2306;&#2328;&#2352;&#2381;&#2359;"
		});
		testTokenisation(tokenise(ArrayUtils.join(words, " ")), words);
	}
	
	
	
}
