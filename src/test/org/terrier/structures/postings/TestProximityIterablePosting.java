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
 * The Original is in 'TestProximityIterablePosting.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.structures.postings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.structures.Index;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestProximityIterablePosting extends ApplicationSetupBasedTest {

	@SuppressWarnings("unchecked")
	@Test public void testMultipleDocumentsTwoTerms() throws Exception
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
		IterablePosting ip = ProximityIterablePosting.createProximityPostingList(
				new String[]{"dramatis", "personae"}, 
				index.getLexicon(), 
				(PostingIndex<Pointer>) index.getInvertedIndex(),
				2);

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
		ip = ProximityIterablePosting.createProximityPostingList(
				new String[]{"dramatis", "personae"}, 
				index.getLexicon(), 
				(PostingIndex<Pointer>) index.getInvertedIndex(),
				2);
		assertNotNull(ip);
		assertEquals(1, ip.next(1));
		assertEquals(1, ip.getFrequency());
		
		assertEquals(4, ip.next(4));
		assertEquals(1, ip.getFrequency());
		
		assertEquals(5, ip.next(5));
		assertEquals(1, ip.getFrequency());
		
		assertEquals(IterablePosting.EOL, ip.next(10));
		
		System.err.println("test duplicate");
		
		//one match
		ip = ProximityIterablePosting.createProximityPostingList(
				new String[]{"dramatis", "dramatis"}, 
				index.getLexicon(), 
				(PostingIndex<Pointer>) index.getInvertedIndex(),
				2);
		assertNotNull(ip);
		assertEquals(4, ip.next());
		assertEquals(IterablePosting.EOL, ip.next());
		
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testMultipleDocumentsTwoThreeTerms() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		Index index = IndexTestUtils.makeIndexBlocks(
				new String[]{"doc0", "doc1", "doc2", "doc3", "doc4", "doc5"}, 
				new String[]{
						"Mana Screw", //0
						"Flip a coin. If you win the flip, add 2 to your mana pool. Play this ability only any time you could play an instant.", //1
						"There was no darker or more evil creation in all the multiverse than that of the mana screw.", //2
						"Chance Encounter", //3
						"Whenever you win a coin flip, put a luck counter on Chance Encounter.", //4
						"At the beginning of your upkeep, if Chance Encounter has ten or more luck counters on it, you win the game." //5
						});
		IterablePosting ip = ProximityIterablePosting.createProximityPostingList(
				new String[]{"flip", "coin"}, 
				index.getLexicon(), 
				(PostingIndex<Pointer>) index.getInvertedIndex(),
				3);

		//check next()
		assertNotNull(ip);
		assertEquals(1, ip.next());
		assertEquals(1, ip.getFrequency());
		
		assertEquals(4, ip.next());
		assertEquals(1, ip.getFrequency());
		
		assertEquals(IterablePosting.EOL, ip.next());
		
		//check next(int)
		ip = ProximityIterablePosting.createProximityPostingList(
				new String[]{"flip", "coin"}, 
				index.getLexicon(), 
				(PostingIndex<Pointer>) index.getInvertedIndex(),
				3);
		assertNotNull(ip);
		assertEquals(1, ip.next(1));
		assertEquals(1, ip.getFrequency());
		
		assertEquals(4, ip.next(4));
		assertEquals(1, ip.getFrequency());
		
		assertEquals(IterablePosting.EOL, ip.next());
		
		//one dist  match
		ip = ProximityIterablePosting.createProximityPostingList(
				new String[]{"flip", "coin"}, 
				index.getLexicon(), 
				(PostingIndex<Pointer>) index.getInvertedIndex(),
				1);
		assertNotNull(ip);
		assertEquals(4, ip.next());
		assertEquals(IterablePosting.EOL, ip.next());
		
		
		assertEquals(IterablePosting.EOL, ip.next(10));
		
		//no match with window of size 1
		ip = ProximityIterablePosting.createProximityPostingList(
				new String[]{"multiverse", "mana", "screw"}, 
				index.getLexicon(), 
				(PostingIndex<Pointer>) index.getInvertedIndex(),
				1);
		assertNotNull(ip);
		assertEquals(IterablePosting.EOL, ip.next());
		
		//no match with window of size 6
		ip = ProximityIterablePosting.createProximityPostingList(
				new String[]{"multiverse", "mana", "screw"}, 
				index.getLexicon(), 
				(PostingIndex<Pointer>) index.getInvertedIndex(),
				6);
		assertNotNull(ip);
		assertEquals(IterablePosting.EOL, ip.next());
		
		//one match with window of size 7
		ip = ProximityIterablePosting.createProximityPostingList(
				new String[]{"multiverse", "mana", "screw"}, 
				index.getLexicon(), 
				(PostingIndex<Pointer>) index.getInvertedIndex(),
				7);
		assertNotNull(ip);
		assertEquals(2, ip.next());
		assertEquals(IterablePosting.EOL, ip.next());
		
		//one dist  match
				ip = ProximityIterablePosting.createProximityPostingList(
						new String[]{"flip", "coin"}, 
						index.getLexicon(), 
						(PostingIndex<Pointer>) index.getInvertedIndex(),
						0);
				assertNotNull(ip);
				assertEquals(4, ip.next());
				assertEquals(IterablePosting.EOL, ip.next());
		
	}
	
}
