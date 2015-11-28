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
 * The Original Code is BaseTestTokeniser.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.indexing.tokenisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.terrier.tests.ApplicationSetupBasedTest;

public class BaseTestTokeniser extends ApplicationSetupBasedTest {

	protected Tokeniser tokeniser;

	protected static void testTokenisation(TokenStream stream, String... terms) {
		assertTrue(stream.hasNext());
		int i = 0;
		while(i < terms.length)
		{
			String t = stream.next();
			if (t != null)
			{
				assertEquals(terms[i], t);
				i++;
			}
			assertTrue("Unexpected end of token stream after " + i + " tokens.", stream.hasNext());
		}
		while(stream.hasNext())
		{
			assertNull(stream.next());
		}
		assertFalse(stream.hasNext());
	}

	protected TokenStream tokenise(String text) throws IOException {
		return tokeniser.tokenise(new StringReader(text));
	}

	public BaseTestTokeniser() {
		super();
	}

}