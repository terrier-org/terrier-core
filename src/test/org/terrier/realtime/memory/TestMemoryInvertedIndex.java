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
 * The Original Code is TestMemoryInvertedIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import gnu.trove.TIntArrayList;

import org.apache.hadoop.io.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.terrier.realtime.memory.MemoryDocumentIndex;
import org.terrier.realtime.memory.MemoryInvertedIndex;
import org.terrier.realtime.memory.MemoryIterablePosting;
import org.terrier.realtime.memory.MemoryLexicon;
import org.terrier.realtime.memory.MemoryLexiconEntry;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.postings.IterablePosting;

/** Unit test for InMemoryInvertedIndex. */
public class TestMemoryInvertedIndex {

	/*
	 * InMemoryIterablePosting(DocumentIndex doi, TIntArrayList pl_doc, TIntArrayList pl_freq)
	 */
	@Test
	public void test_InMemoryIterablePosting() throws Exception {
		MemoryDocumentIndex docindex = new MemoryDocumentIndex();
		assertNotNull(docindex);
		for (int i = 0; i < 10; i++)
			docindex.addDocument(i);
		MemoryIterablePosting post = new MemoryIterablePosting(docindex,new TIntArrayList(docids),new TIntArrayList(docfreqs));
		assertNotNull(post);
		for (int i = 0; i < 10; i++) {
			assertThat(post.next(), is(not(-1)));
			assertEquals(docids[i], post.getId());
			assertEquals(docfreqs[i], post.getFrequency());
			assertEquals(i, post.getDocumentLength());
		}
		assertEquals(IterablePosting.EOL, post.next());
	}
	
	/*
	 * InMemoryInvertedIndex(DocumentIndex doi, Lexicon<String> lex)
	 */
	@Test
	public void test_InMemoryInvertedIndex() throws Exception {
		MemoryInvertedIndex inverted = new MemoryInvertedIndex(new MemoryLexicon(),new MemoryDocumentIndex());
		assertNotNull(inverted);
	}

	/*
	 * addDocument(int ptr, int docid, int freq)
	 */
	@Test
	public void test_addDocument() throws Exception {
		MemoryInvertedIndex inverted = new MemoryInvertedIndex(new MemoryLexicon(),new MemoryDocumentIndex());
		assertNotNull(inverted);
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				inverted.add(i, docids[j], docfreqs[j]);
	}

	/*
	 * getPostings(IntPointer pointer)
	 */
	@Test
	public void test_getPostings() throws Exception {
		MemoryLexicon lexicon = new MemoryLexicon();
		assertNotNull(lexicon);
		for (int i = 0; i < 10; i++)
			lexicon.term(terms[i].toString(), entries[i]);
		MemoryDocumentIndex docindex = new MemoryDocumentIndex();
		assertNotNull(docindex);
		for (int i = 0; i < 10; i++)
			docindex.addDocument(i);
		MemoryInvertedIndex inverted = new MemoryInvertedIndex(lexicon,docindex);
		assertNotNull(inverted);
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				inverted.add(i, docids[j], docfreqs[j]);
		for (int i = 0; i < 10; i++) {
			IterablePosting post = inverted.getPostings((MemoryLexiconEntry) entries[i]);
			assertNotNull(post);
			for (int j = 0; j < 10; j++) {
				assertEquals(docids[j], post.next());
				assertEquals(docids[j], post.getId());
				assertEquals(docfreqs[j], post.getFrequency());
			}
			assertEquals(IterablePosting.EOL, post.next());
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
		MemoryDocumentIndex docindex = new MemoryDocumentIndex();
		assertNotNull(docindex);
		for (int i = 0; i < 10; i++)
			docindex.addDocument(i);
		MemoryInvertedIndex inverted = new MemoryInvertedIndex(lexicon,docindex);
		assertNotNull(inverted);
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				inverted.add(i, docids[j], docfreqs[j]);
		PostingIndexInputStream iter = inverted.iterator();
		assertNotNull(iter);
		while (iter.hasNext()) {
			IterablePosting post = iter.getNextPostings();
			assertNotNull(post);
			for (int j = 0; j < 10; j++) {
				assertEquals(docids[j], post.next());
				assertEquals(docids[j], post.getId());
				assertEquals(docfreqs[j], post.getFrequency());
			}
			assertEquals(IterablePosting.EOL, post.next());
		}
	}

	/*
	 * Test data.
	 */
	int[] docids, docfreqs;
	LexiconEntry[] entries;
	Text[] terms;

	@Before
	public void setUp() {
		docids = new int[] {0,1,2,3,4,5,6,7,8,9};
		docfreqs = new int[] {0,1,2,3,4,5,6,7,8,9};
		terms = new Text[] {
			new Text("t0"),new Text("t1"),new Text("t2"),
			new Text("t3"),new Text("t4"),new Text("t5"),
			new Text("t6"),new Text("t7"),new Text("t8"),
			new Text("t9")
		};
		entries = new LexiconEntry[] { 
			new MemoryLexiconEntry(0,1,1),
			new MemoryLexiconEntry(1,2,2),
			new MemoryLexiconEntry(2,3,3),
			new MemoryLexiconEntry(3,4,4),
			new MemoryLexiconEntry(4,5,5),
			new MemoryLexiconEntry(5,6,6),
			new MemoryLexiconEntry(6,7,7),
			new MemoryLexiconEntry(7,8,8),
			new MemoryLexiconEntry(8,9,9),
			new MemoryLexiconEntry(9,10,10)
		};
	}

	@After
	public void tearDown() {
		docids=docfreqs=null;
		entries=null;
		terms=null;
	}

}
