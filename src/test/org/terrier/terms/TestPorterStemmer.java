/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is TestPorterStemmer.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.terms;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;

import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

/** Test that the Porter Stemmer behaves as described by Martin Porter */
public class TestPorterStemmer extends ApplicationSetupBasedTest {

	static final String[] TEST_EXAMPLES = new String[]{
		"abandoned", "abandon",
		"abandon", "abandon",
		"abergavenny", "abergavenni"
	};
	
	Stemmer stemmer;
	public TestPorterStemmer()
	{
		stemmer = new PorterStemmer(null);
	}
	
	@Test
	public void testSimpleExamples()
	{
		final int l = TEST_EXAMPLES.length;
		for(int i=0;i<(l/2);i++)
		{
			assertEquals(TEST_EXAMPLES[i+1], stemmer.stem(TEST_EXAMPLES[i]));
		}
	}
	
	@Test
	public void testAllExamples() throws Exception
	{
		BufferedReader brVocab = Files.openFileReader(ApplicationSetup.TERRIER_SHARE + "/tests/porterstemmer/voc.txt");
		BufferedReader brTest = Files.openFileReader(ApplicationSetup.TERRIER_SHARE + "/tests/porterstemmer/output.txt");
		String testWord;
		while((testWord = brVocab.readLine()) != null)
		{
			testWord = testWord.trim();
			String targetWord = brTest.readLine();
			if (targetWord == null)
				throw new IllegalStateException("Test files not of same length");
			targetWord = targetWord.trim();
			assertEquals("Incorrect stem for "+ testWord + " -> " + targetWord,
					targetWord, stemmer.stem(testWord));
		}
	}
}
