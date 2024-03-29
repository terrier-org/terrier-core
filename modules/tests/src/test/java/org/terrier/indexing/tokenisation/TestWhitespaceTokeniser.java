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
 * The Original Code is TestWhitespaceTokeniser.java.
 *
 * The Original Code is Copyright (C) 2004-2022 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */

package org.terrier.indexing.tokenisation;



import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Test;

public class TestWhitespaceTokeniser extends BaseTestTokeniser {

	
	public TestWhitespaceTokeniser()
	{
		super.tokeniser = new WhitespaceTokeniser();
	}
	
	@Test public void testVariousTerms() throws Exception
	{
		testTokenisation(tokenise("a"), "a");
		testTokenisation(tokenise("a "), "a");
		testTokenisation(tokenise("hello"), "hello");
		testTokenisation(tokenise("a b"), "a", "b");
		testTokenisation(tokenise("a\tb"), "a", "b");
		testTokenisation(tokenise("a ##b"), "a", "##b");
	}
	
	@Test public void testNull() throws Exception
	{
		TokenStream t = tokeniser.tokenise(null);
		t.next();
		assertFalse(t.hasNext());
	}
	
}
