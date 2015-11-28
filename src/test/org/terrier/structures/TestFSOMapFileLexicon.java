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
 * The Original is in 'TestFSOMapFileLexicon.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.structures;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Map;

import org.junit.Test;
import org.terrier.structures.indexing.LexiconBuilder;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestFSOMapFileLexicon extends ApplicationSetupBasedTest
{

	Index createLexiconIndex(String[] tokens) throws Exception
	{
		IndexOnDisk index = Index.createNewIndex(
				ApplicationSetup.TERRIER_INDEX_PATH,
				ApplicationSetup.TERRIER_INDEX_PREFIX);
		LexiconBuilder lb = new LexiconBuilder(index, "lexicon");
		for(String tok: tokens)
		{
			lb.addTerm(tok, 1);
		}
		lb.finishedDirectIndexBuild();
		lb.finishedInvertedIndexBuild();
		return index;
	}
	
	
	@Test(expected=IllegalArgumentException.class) public void testNullTermException() throws Exception
	{
		IndexOnDisk index = Index.createNewIndex(
				ApplicationSetup.TERRIER_INDEX_PATH,
				ApplicationSetup.TERRIER_INDEX_PREFIX);
		LexiconBuilder lb = new LexiconBuilder(index, "lexicon");
		lb.addTerm("", 0);
		
	}
	
	@Test public void testOneTermOneOccurrence() throws Exception
	{
		Index index = createLexiconIndex(new String[]{"a"});
		Lexicon<String> lexicon = index.getLexicon();
		assertEquals(1, lexicon.numberOfEntries());
		assertNotNull(lexicon.getLexiconEntry("a"));
		assertEquals(1, lexicon.getLexiconEntry("a").getFrequency());
		
		assertEquals("a", lexicon.getIthLexiconEntry(0).getKey());
		assertEquals("a", lexicon.getLexiconEntry(0).getKey());
	}
	
	@Test public void testOneTermTwoOccurrence() throws Exception
	{
		Index index = createLexiconIndex(new String[]{"a", "a"});
		Lexicon<String> lexicon = index.getLexicon();
		assertEquals(1, lexicon.numberOfEntries());
		assertNotNull(lexicon.getLexiconEntry("a"));
		assertEquals(2, lexicon.getLexiconEntry("a").getFrequency());
	}
	
	@Test public void testTwoTermThreeOccurrence() throws Exception
	{
		Index index = createLexiconIndex(new String[]{"a", "b", "a"});
		Lexicon<String> lexicon = index.getLexicon();
		assertEquals(2, lexicon.numberOfEntries());
		assertNotNull(lexicon.getLexiconEntry("a"));
		assertEquals(2, lexicon.getLexiconEntry("a").getFrequency());
		assertNotNull(lexicon.getLexiconEntry("b"));
		assertEquals(1, lexicon.getLexiconEntry("b").getFrequency());
	}
	
	@Test public void testSubset() throws Exception
	{
		Index index = createLexiconIndex(new String[]{"a", "b", "a", "c", "d", "e", "f", "z"});
		Iterator<Map.Entry<String,LexiconEntry>> iter;
		
		//check non-existent range
		iter = index.getLexicon().getLexiconEntryRange("g", "h");
		assertFalse(iter.hasNext());
		
		//check existent range, with exclusive, existing end point.
		iter = index.getLexicon().getLexiconEntryRange("a", "c");
		assertTrue(iter.hasNext());
		assertEquals("a", iter.next().getKey());
		assertTrue(iter.hasNext());
		assertEquals("b", iter.next().getKey());
		assertFalse(iter.hasNext());
		
		
		//check existent range, with exclusive non-existing end point.
		iter = index.getLexicon().getLexiconEntryRange("f", "g");
		assertTrue(iter.hasNext());
		assertEquals("f", iter.next().getKey());
		assertFalse(iter.hasNext());
	}
}
