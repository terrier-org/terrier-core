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
 * The Original Code is TestSnowball.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.terms;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestSnowball {
	
	@SuppressWarnings("unchecked")
	Class<? extends SnowballStemmer>[] STEMMERS = new Class[]{
		DanishSnowballStemmer.class,
		DutchSnowballStemmer.class,
		EnglishSnowballStemmer.class,
		FinnishSnowballStemmer.class,
		FrenchSnowballStemmer.class,
		GermanSnowballStemmer.class,
		HungarianSnowballStemmer.class,
		ItalianSnowballStemmer.class,
		NorwegianSnowballStemmer.class,
		PortugueseSnowballStemmer.class,
		RomanianSnowballStemmer.class,
		RussianSnowballStemmer.class,
		SpanishSnowballStemmer.class,
		SwedishSnowballStemmer.class,
		TurkishSnowballStemmer.class
	};
	
	@Test
	public void testAll() throws Exception
	{
		for (Class<? extends SnowballStemmer> stemmer : STEMMERS) {
			Stemmer s = stemmer.getConstructor(TermPipeline.class).newInstance(new Object[]{null});
			assertNotNull(s);
			assertNotNull(s.stem("a"));
		}
	}
	
	@Test
	public void testSimpleExamplesEnglishSnowball()
	{
		Stemmer stemmer = new EnglishSnowballStemmer(null);
		final int l = TestPorterStemmer.TEST_EXAMPLES.length;
		for(int i=0;i<(l/2);i++)
		{
			assertEquals(TestPorterStemmer.TEST_EXAMPLES[i+1], stemmer.stem(TestPorterStemmer.TEST_EXAMPLES[i]));
		}
	}	
}
