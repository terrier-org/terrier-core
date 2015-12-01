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
 * The Original Code is TestMemoryLexicon.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.hadoop.io.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.terrier.realtime.memory.MemoryLexicon;
import org.terrier.realtime.memory.MemoryLexiconEntry;
import org.terrier.structures.LexiconEntry;

/** Unit test for InMemoryLexicon. */
public class TestMemoryLexicon {

	/*
	 * InMemoryLexiconEntry()
	 */
	@Test
	public void test_InMemoryLexiconEntry() throws Exception {
		MemoryLexiconEntry le;
		le = new MemoryLexiconEntry();
		assertEquals(-1, le.getTermId());
		assertEquals(-1, le.getDocumentFrequency());
		assertEquals(-1, le.getFrequency());
		le = new MemoryLexiconEntry(1);
		assertEquals(1, le.getTermId());
		assertEquals(-1, le.getDocumentFrequency());
		assertEquals(-1, le.getFrequency());
		le = new MemoryLexiconEntry(10, 20);
		assertEquals(-1, le.getTermId());
		assertEquals(10, le.getDocumentFrequency());
		assertEquals(20, le.getFrequency());
		le = new MemoryLexiconEntry(1, 10, 20);
		assertEquals(1, le.getTermId());
		assertEquals(10, le.getDocumentFrequency());
		assertEquals(20, le.getFrequency());
		le = new MemoryLexiconEntry(0,0,0);
		assertEquals(0, le.getTermId());
		assertEquals(0, le.getDocumentFrequency());
		assertEquals(0, le.getFrequency());
		le.add(new MemoryLexiconEntry(0,1,2));
		assertEquals(0, le.getTermId());
		assertEquals(1, le.getDocumentFrequency());
		assertEquals(2, le.getFrequency());
		le.subtract(new MemoryLexiconEntry(0,1,2));
		assertEquals(0, le.getTermId());
		assertEquals(0, le.getDocumentFrequency());
		assertEquals(0, le.getFrequency());
		le.setTermId(101);
		le.setStatistics(5, 10);
		assertEquals(101, le.getTermId());
		assertEquals(5, le.getDocumentFrequency());
		assertEquals(10, le.getFrequency());
		assertEquals(101, le.getPointer());
		le.setPointer(new MemoryLexiconEntry(99));
		le.setNumberOfEntries(20);
		assertEquals(99, le.getPointer());
		assertEquals(20, le.getNumberOfEntries());
	}
	
	/*
	 * InMemoryLexicon()
	 */
	@Test
	public void test_InMemoryLexicon() throws Exception {
		MemoryLexicon lexicon = new MemoryLexicon();
		assertNotNull(lexicon);
	}

	/*
	 * incrementTerm(String term, EntryStatistics es)
	 */
	@Test
	public void test_incrementTerm1() throws Exception {
		MemoryLexicon lexicon = new MemoryLexicon();
		assertNotNull(lexicon);
		for (int i = 0; i < 10; i++)
			lexicon.term(terms[i].toString(), entries[i]);
		assertEquals(10, lexicon.numberOfEntries());
	}

	/*
	 * getLexiconEntry(String term)
	 */
	@Test
	public void test_getLexiconEntry1() throws Exception {
		MemoryLexicon lexicon = new MemoryLexicon();
		assertNotNull(lexicon);
		for (int i = 0; i < 10; i++)
			lexicon.term(terms[i].toString(), entries[i]);
		for (int i = 0; i < 10; i++) {
			LexiconEntry le = lexicon.getLexiconEntry(terms[i].toString());
			assertEquals(i, le.getTermId());
			assertEquals(i + 1, le.getDocumentFrequency());
			assertEquals(i + 1, le.getFrequency());
		}
	}

	/*
	 * getLexiconEntry(int termid)
	 */
	@Test
	public void test_getLexiconEntry2() throws Exception {
		MemoryLexicon lexicon = new MemoryLexicon();
		assertNotNull(lexicon);
		for (int i = 0; i < 10; i++)
			lexicon.term(terms[i].toString(), entries[i]);
		for (int i = 0; i < 10; i++) {
			Entry<String, LexiconEntry> kv = lexicon.getLexiconEntry(i);
			assertEquals("t" + i, kv.getKey());
			assertEquals(i, kv.getValue().getTermId());
			assertEquals(i + 1, kv.getValue().getDocumentFrequency());
			assertEquals(i + 1, kv.getValue().getFrequency());
		}
	}

	/*
	 * getIthLexiconEntry(int index) 
	 */
	@Test
	public void test_getIthLexiconEntry() throws Exception {
		MemoryLexicon lexicon = new MemoryLexicon();
		assertNotNull(lexicon);
		for (int i = 0; i < 10; i++)
			lexicon.term(terms[i].toString(), entries[i]);
		for (int i = 0; i < 10; i++) {
			Entry<String, LexiconEntry> kv = lexicon.getIthLexiconEntry(i);
			assertEquals("t" + i, kv.getKey());
			assertEquals(i, kv.getValue().getTermId());
			assertEquals(i + 1, kv.getValue().getDocumentFrequency());
			assertEquals(i + 1, kv.getValue().getFrequency());
		}
	}

	/*
	 * incrementTerm(String term, EntryStatistics es)
	 */
	@Test
	public void test_incrementTerm2() throws Exception {
		MemoryLexicon lexicon = new MemoryLexicon();
		assertNotNull(lexicon);
		for (int i = 0; i < 10; i++)
			lexicon.term(terms[i].toString(), entries[i]);
		for (int i = 0; i < 10; i++)
			lexicon.term(terms[i].toString(), entries[i]);
		assertEquals(10, lexicon.numberOfEntries());
		for (int i = 0; i < 10; i++) {
			Entry<String, LexiconEntry> kv = lexicon.getLexiconEntry(i);
			assertEquals("t" + i, kv.getKey());
			assertEquals(i, kv.getValue().getTermId());
			assertEquals((i + 1) * 2, kv.getValue().getDocumentFrequency());
			assertEquals((i + 1) * 2, kv.getValue().getFrequency());
		}
	}
	
	/*
	 * iterator()
	 */
	@Test
	public void test_iterator() throws Exception {
		MemoryLexicon lexicon = new MemoryLexicon();
		assertNotNull(lexicon);
		for (int i = 0; i < 10; i++)
			lexicon.term(terms[i].toString(), entries[i]);
		Iterator<Entry<String, LexiconEntry>> it = lexicon.iterator();
		assertNotNull(it);
		int i = 0;
		while (it.hasNext()) {
			Entry<String, LexiconEntry> kv1 = it.next();
			Entry<String, LexiconEntry> kv2 = lexicon.getLexiconEntry(i++);
			assertEquals(kv1.getKey(), kv2.getKey());
			assertEquals(kv1.getValue().getTermId(), kv2.getValue().getTermId());
			assertEquals(kv1.getValue().getDocumentFrequency(), kv2.getValue().getDocumentFrequency());
			assertEquals(kv1.getValue().getFrequency(), kv2.getValue().getFrequency());
		}
	}

	/*
	 * Test data.
	 */
	Text[] terms;
	LexiconEntry[] entries;

	@Before
	public void setUp() {
		terms = new Text[] { 
			new Text("t0"), new Text("t1"), new Text("t2"),
			new Text("t3"), new Text("t4"), new Text("t5"),
			new Text("t6"),	new Text("t7"), new Text("t8"),
			new Text("t9")
		};
		entries = new LexiconEntry[] {
			new MemoryLexiconEntry(0, 1, 1),
			new MemoryLexiconEntry(1, 2, 2),
			new MemoryLexiconEntry(2, 3, 3),
			new MemoryLexiconEntry(3, 4, 4),
			new MemoryLexiconEntry(4, 5, 5),
			new MemoryLexiconEntry(5, 6, 6),
			new MemoryLexiconEntry(6, 7, 7),
			new MemoryLexiconEntry(7, 8, 8),
			new MemoryLexiconEntry(8, 9, 9),
			new MemoryLexiconEntry(9, 10, 10)
		};
	}

	@After
	public void tearDown() {
		terms=null;
		entries=null;
	}

}
