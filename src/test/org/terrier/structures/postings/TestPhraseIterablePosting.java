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
 * The Original is in 'TestPhraseIterablePosting.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.structures.postings;

import static org.junit.Assert.*;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.structures.Index;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestPhraseIterablePosting extends ApplicationSetupBasedTest {
	@SuppressWarnings("unchecked")
	@Test public void testOnePhraseOneDocument() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		Index index = IndexTestUtils.makeIndexBlocks(
				new String[]{"doc1"}, 
				new String[]{"dramatis personae"});
		IterablePosting ip = PhraseIterablePosting.createPhrasePostingList(
				new String[]{"dramatis", "personae"}, 
				index.getLexicon(), 
				(PostingIndex<Pointer>) index.getInvertedIndex(),
				true);
		assertNotNull(ip);
		assertEquals(0, ip.next());
		assertEquals(1, ip.getFrequency());
		assertEquals(IterablePosting.EOL, ip.next());
	}
	
	
	@SuppressWarnings("unchecked")
	@Test public void testMultipleDocuments() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		Index index = IndexTestUtils.makeIndexBlocks(
				new String[]{"doc0", "doc1", "doc2", "doc3", "doc4", "doc5"}, 
				new String[]{
						"dramatis", //0
						"DRAMATIS personae", //1
						"personae", //2
						"dramatis isnae personae", //3
						"dramatis dramatis personae personae", //4
						"another term dramatis PeRsOnAe term" //5
						});
		IterablePosting ip = PhraseIterablePosting.createPhrasePostingList(
				new String[]{"dramatis", "personae"}, 
				index.getLexicon(), 
				(PostingIndex<Pointer>) index.getInvertedIndex(),
				true);

		//check next()
		assertNotNull(ip);
		assertEquals(1, ip.next());
		assertEquals(1, ip.getFrequency());
		
		assertEquals(4, ip.next());
		assertEquals(1, ip.getFrequency());
		
		assertEquals(5, ip.next());
		assertEquals(1, ip.getFrequency());
		
		assertEquals(IterablePosting.EOL, ip.next());
		
		//check next(int)
		ip = PhraseIterablePosting.createPhrasePostingList(
				new String[]{"dramatis", "personae"}, 
				index.getLexicon(), 
				(PostingIndex<Pointer>) index.getInvertedIndex(),
				true);
		assertNotNull(ip);
		assertEquals(1, ip.next(1));
		assertEquals(1, ip.getFrequency());
		
		assertEquals(4, ip.next(4));
		assertEquals(1, ip.getFrequency());
		
		assertEquals(5, ip.next(5));
		assertEquals(1, ip.getFrequency());
		
		assertEquals(IterablePosting.EOL, ip.next(10));
		
		//no match
		ip = PhraseIterablePosting.createPhrasePostingList(
				new String[]{"dramatis", "term"}, 
				index.getLexicon(), 
				(PostingIndex<Pointer>) index.getInvertedIndex(),
				true);
		assertNotNull(ip);
		assertEquals(IterablePosting.EOL, ip.next());
		
		//one match
		ip = PhraseIterablePosting.createPhrasePostingList(
				new String[]{"dramatis", "dramatis"}, 
				index.getLexicon(), 
				(PostingIndex<Pointer>) index.getInvertedIndex(),
				true);
		assertNotNull(ip);
		assertEquals(4, ip.next());
		assertEquals(IterablePosting.EOL, ip.next());
		
	}
}
