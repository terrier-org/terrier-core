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
 * The Original Code is TestFlatDocument.java.
 *
 * The Original Code is Copyright (C) 2022-2022 the University of Glasgow.
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

public class TestFlatJSONDocument extends BaseTestDocument {

	protected Document makeDocument(String json)
	{
		return new FlatJSONDocument(json);
	}
	
	@Test(expected = RuntimeException.class) public void testInvalidJSON()
	{
		makeDocument(" [ 'a', 'b' ] ");
	}
	
	@Test public void testSingleTerm()
	{
		testDocument(makeDocument("{'docno' : 'd1', 'text' : 'hello'}"), "hello");
	}
}