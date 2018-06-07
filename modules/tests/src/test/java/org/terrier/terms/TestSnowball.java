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
