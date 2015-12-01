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
 * The Original Code is TestMemoryIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.terrier.indexing.Collection;
import org.terrier.indexing.CollectionDocumentList;
import org.terrier.indexing.Document;
import org.terrier.indexing.FileDocument;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.indexing.tokenisation.EnglishTokeniser;
import org.terrier.matching.ResultSet;
import org.terrier.realtime.MemoryIndexer;
import org.terrier.realtime.TestUtils;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

/** Unit tests for IndexInMemory. */
public class TestMemoryIndex extends ApplicationSetupBasedTest {

	
	@Test
	public void test_DocumentContentUpdates() throws Exception {
		ApplicationSetup.setProperty("termpipelines", "");
		MemoryIndex index = new MemoryIndex();
		Lexicon<String> lex = index.getLexicon();
		LexiconEntry le;
		PostingIndex<?> inv = index.getInvertedIndex();
		IterablePosting ip;
		
		index.indexDocument(IndexTestUtils.makeDocumentFromText("the lazy", new HashMap<String,String>()));
		
		//document index
		assertEquals(2, index.getDocumentIndex().getDocumentLength(0));
		//stats
		assertEquals(1, index.getCollectionStatistics().getNumberOfDocuments());
		assertEquals(2l, index.getCollectionStatistics().getNumberOfTokens());
		assertEquals(2l, index.getCollectionStatistics().getNumberOfUniqueTerms());
		//lexicon
		
		
		le = lex.getLexiconEntry("the");
		assertEquals(1, le.getFrequency());
		le = lex.getLexiconEntry("lazy");
		assertEquals(1, le.getFrequency());
		
		ip = inv.getPostings(le);
		assertEquals(0, ip.next());
		assertEquals(1, ip.getFrequency());
		assertEquals(IterablePosting.EOL, ip.next());
		
		assertNull(lex.getLexiconEntry("brown"));
		
		
		index.addToDocument(0, IndexTestUtils.makeDocumentFromText("lazy brown dog", new HashMap<String,String>()));
	
		//document index
		assertEquals(5, index.getDocumentIndex().getDocumentLength(0));
		//stats
		assertEquals(1, index.getCollectionStatistics().getNumberOfDocuments());
		assertEquals(5l, index.getCollectionStatistics().getNumberOfTokens());
		assertEquals(4l, index.getCollectionStatistics().getNumberOfUniqueTerms());
		//lexicon
		le = lex.getLexiconEntry("the");
		assertEquals(1, le.getFrequency());
		le = lex.getLexiconEntry("lazy");
		assertEquals(2, le.getFrequency());
		
		ip = inv.getPostings(le);
		assertEquals(0, ip.next());
		assertEquals(2, ip.getFrequency());
		assertEquals(IterablePosting.EOL, ip.next());
		
		le = lex.getLexiconEntry("brown");
		assertNotNull(le);
		assertEquals(1, le.getFrequency());
		
		ip = inv.getPostings(le);
		assertEquals(0, ip.next());
		assertEquals(1, ip.getFrequency());
		assertEquals(IterablePosting.EOL, ip.next());
		
		
		index.indexDocument(IndexTestUtils.makeDocumentFromText("jumped", new HashMap<String,String>()));
		index.addToDocument(1, IndexTestUtils.makeDocumentFromText("over the brown brown brown fox", new HashMap<String,String>()));
		
		le = lex.getLexiconEntry("brown");
		assertNotNull(le);
		assertEquals(4, le.getFrequency());
		
		ip = inv.getPostings(le);
		assertEquals(0, ip.next());
		assertEquals(1, ip.getFrequency());
		
		assertEquals(1, ip.next());
		assertEquals(3, ip.getFrequency());
		
		assertEquals(IterablePosting.EOL, ip.next());
		
		index.close();
	}
		
		
	/*
	 * Test IndexInMemory.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void test_IndexInMemory() throws Exception {
		MemoryIndex index = new MemoryIndex();
		assertNotNull(index);

		// Lexicon.
		Lexicon<String> lexicon = (Lexicon<String>) index
				.getIndexStructure("lexicon");
		assertNotNull(lexicon);
		Iterator<Lexicon<String>> lexicon_iterator = (Iterator<Lexicon<String>>) index
				.getIndexStructureInputStream("lexicon");
		assertNotNull(lexicon_iterator);

		// Inverted index.
		PostingIndex<?> inverted = (PostingIndex<?>) index
				.getIndexStructure("inverted");
		assertNotNull(inverted);
		Iterator<PostingIndex<?>> inverted_iterator = (Iterator<PostingIndex<?>>) index
				.getIndexStructureInputStream("inverted");
		assertNotNull(inverted_iterator);

		// Metadata index.
		MetaIndex metaindex = (MetaIndex) index.getIndexStructure("meta");
		assertNotNull(metaindex);
		Iterator<MetaIndex> meta_iterator = (Iterator<MetaIndex>) index
				.getIndexStructureInputStream("meta");
		assertNotNull(meta_iterator);

		// Collection statistics.
		CollectionStatistics stats = (CollectionStatistics) index
				.getIndexStructure("collectionstatistics");
		assertNotNull(stats);

		// Document index.
		DocumentIndex docindex = (DocumentIndex) index
				.getIndexStructure("document");
		assertNotNull(docindex);
		Iterator<DocumentIndex> document_iterator = (Iterator<DocumentIndex>) index
				.getIndexStructureInputStream("document");
		assertNotNull(document_iterator);
	}

	/*
	 * Verify in-memory index against test data.
	 */
	@Test
	public void test_verify1() throws Exception {
		MemoryIndex index = TestUtils.memory(collection);
		assertNotNull(index);
		/*
		 * Verify metadata.
		 */
		// getMetaIndex()
		MemoryMetaIndex metaindex = (MemoryMetaIndex) index.getMetaIndex();
		assertNotNull(metaindex);
		// getKeys()
		assertArrayEquals(new String[] { "filename" }, metaindex.getKeys());
		// getItem(key,docid)
		assertEquals("doc1", metaindex.getItem("filename", 0));
		assertEquals("doc2", metaindex.getItem("filename", 1));
		// getAllItems(docid)
		assertArrayEquals(new String[] { "doc1" }, metaindex.getAllItems(0));
		assertArrayEquals(new String[] { "doc2" }, metaindex.getAllItems(1));
		// getItems(key,docids)
		assertArrayEquals(new String[] { "doc1", "doc2" },
				metaindex.getItems("filename", new int[] { 0, 1 }));
		// getItems(keys,docid)
		assertArrayEquals(new String[] { "doc1" },
				metaindex.getItems(new String[] { "filename" }, 0));
		assertArrayEquals(new String[] { "doc2" },
				metaindex.getItems(new String[] { "filename" }, 1));
		// getItems(keys,docids)
		assertArrayEquals(
				new String[][] { { "doc1" }, { "doc2" } },
				metaindex.getItems(new String[] { "filename" }, new int[] { 0,
						1 }));
		/*
		 * Verify lexicon.
		 */
		// getLexicon()
		MemoryLexicon lexicon = (MemoryLexicon) index.getLexicon();
		assertNotNull(lexicon);
		// numberOfEntries()
		assertEquals(4, lexicon.numberOfEntries());
		// getLexiconEntry(term)
		MemoryLexiconEntry le;
		le = (MemoryLexiconEntry) lexicon.getLexiconEntry("church");
		assertEquals(0, le.getTermId());
		assertEquals(1, le.getDocumentFrequency());
		assertEquals(1, le.getFrequency());
		le = (MemoryLexiconEntry) lexicon.getLexiconEntry("curry");
		assertEquals(1, le.getTermId());
		assertEquals(1, le.getDocumentFrequency());
		assertEquals(1, le.getFrequency());
		le = (MemoryLexiconEntry) lexicon.getLexiconEntry("knuth");
		assertEquals(2, le.getTermId());
		assertEquals(2, le.getDocumentFrequency());
		assertEquals(3, le.getFrequency());
		le = (MemoryLexiconEntry) lexicon.getLexiconEntry("turing");
		assertEquals(3, le.getTermId());
		assertEquals(2, le.getDocumentFrequency());
		assertEquals(3, le.getFrequency());
		// getLexiconEntry(termid)
		Entry<String, LexiconEntry> kv;
		kv = lexicon.getLexiconEntry(0);
		assertEquals("church", kv.getKey());
		assertEquals(0, kv.getValue().getTermId());
		assertEquals(1, kv.getValue().getDocumentFrequency());
		assertEquals(1, kv.getValue().getFrequency());
		kv = lexicon.getLexiconEntry(1);
		assertEquals("curry", kv.getKey());
		assertEquals(1, kv.getValue().getTermId());
		assertEquals(1, kv.getValue().getDocumentFrequency());
		assertEquals(1, kv.getValue().getFrequency());
		kv = lexicon.getLexiconEntry(2);
		assertEquals("knuth", kv.getKey());
		assertEquals(2, kv.getValue().getTermId());
		assertEquals(2, kv.getValue().getDocumentFrequency());
		assertEquals(3, kv.getValue().getFrequency());
		kv = lexicon.getLexiconEntry(3);
		assertEquals("turing", kv.getKey());
		assertEquals(3, kv.getValue().getTermId());
		assertEquals(2, kv.getValue().getDocumentFrequency());
		assertEquals(3, kv.getValue().getFrequency());
		// getIthLexiconEntry(index)
		kv = lexicon.getIthLexiconEntry(0);
		assertEquals("church", kv.getKey());
		assertEquals(0, kv.getValue().getTermId());
		assertEquals(1, kv.getValue().getDocumentFrequency());
		assertEquals(1, kv.getValue().getFrequency());
		kv = lexicon.getIthLexiconEntry(1);
		assertEquals("curry", kv.getKey());
		assertEquals(1, kv.getValue().getTermId());
		assertEquals(1, kv.getValue().getDocumentFrequency());
		assertEquals(1, kv.getValue().getFrequency());
		kv = lexicon.getIthLexiconEntry(2);
		assertEquals("knuth", kv.getKey());
		assertEquals(2, kv.getValue().getTermId());
		assertEquals(2, kv.getValue().getDocumentFrequency());
		assertEquals(3, kv.getValue().getFrequency());
		kv = lexicon.getIthLexiconEntry(3);
		assertEquals("turing", kv.getKey());
		assertEquals(3, kv.getValue().getTermId());
		assertEquals(2, kv.getValue().getDocumentFrequency());
		assertEquals(3, kv.getValue().getFrequency());
		/*
		 * Verify inverted file.
		 */
		MemoryInvertedIndex inverted = (MemoryInvertedIndex) index
				.getInvertedIndex();
		assertNotNull(inverted);
		MemoryIterablePosting posting;
		posting = (MemoryIterablePosting) inverted
				.getPostings(new MemoryLexiconEntry(0));
		assertNotNull(posting);
		posting.next();
		assertEquals(0, posting.getId());
		assertEquals(1, posting.getFrequency());
		assertEquals(4, posting.getDocumentLength());
		posting = (MemoryIterablePosting) inverted
				.getPostings(new MemoryLexiconEntry(1));
		assertNotNull(posting);
		posting.next();
		assertEquals(0, posting.getId());
		assertEquals(1, posting.getFrequency());
		assertEquals(4, posting.getDocumentLength());
		posting = (MemoryIterablePosting) inverted
				.getPostings(new MemoryLexiconEntry(2));
		assertNotNull(posting);
		posting.next();
		assertEquals(0, posting.getId());
		assertEquals(1, posting.getFrequency());
		assertEquals(4, posting.getDocumentLength());
		posting.next();
		assertEquals(1, posting.getId());
		assertEquals(2, posting.getFrequency());
		assertEquals(4, posting.getDocumentLength());
		posting = (MemoryIterablePosting) inverted
				.getPostings(new MemoryLexiconEntry(3));
		assertNotNull(posting);
		posting.next();
		assertEquals(0, posting.getId());
		assertEquals(1, posting.getFrequency());
		assertEquals(4, posting.getDocumentLength());
		posting.next();
		assertEquals(1, posting.getId());
		assertEquals(2, posting.getFrequency());
		assertEquals(4, posting.getDocumentLength());
		/*
		 * Verify collection statistics.
		 */
		CollectionStatistics cs = index.getCollectionStatistics();
		assertEquals(2, cs.getNumberOfDocuments());
		assertEquals(8l, cs.getNumberOfTokens());
		assertEquals(4, cs.getNumberOfUniqueTerms());
		assertEquals(6l, cs.getNumberOfPointers());
		assertEquals(4.0d, cs.getAverageDocumentLength(), 0.0d);
	}

	/*
	 * Verify in-memory retrieval against test data.
	 */
	@Test
	public void test_verify2() throws Exception {
		MemoryIndex index = TestUtils.memory(collection);
		assertNotNull(index);
		ResultSet res;
		int[] docids;
		res = TestUtils.query("curry", index);
		assertNotNull(res);
		assertEquals(1, res.getResultSize());
		docids = res.getDocids();
		assertNotNull(docids);
		assertArrayEquals(new int[] { 0 }, docids);
		res = TestUtils.query("church", index);
		assertNotNull(res);
		assertEquals(1, res.getResultSize());
		docids = res.getDocids();
		assertNotNull(docids);
		assertArrayEquals(new int[] { 0 }, docids);
		res = TestUtils.query("knuth", index);
		assertNotNull(res);
		assertEquals(2, res.getResultSize());
		docids = res.getDocids();
		assertNotNull(docids);
		assertArrayEquals(new int[] { 1, 0 }, docids);
		res = TestUtils.query("turing", index);
		assertNotNull(res);
		assertEquals(2, res.getResultSize());
		docids = res.getDocids();
		assertNotNull(docids);
		assertArrayEquals(new int[] { 1, 0 }, docids);
	}

	/*
	 * Test incremental indexing.
	 */
	@Test
	public void test_incremental() throws Exception {
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.reverse.keys", "filename");
		ApplicationSetup.setProperty("termpipelines", "");
		MemoryIndexer indexer = new MemoryIndexer();
		assertNotNull(indexer);
		MemoryIndex index = (MemoryIndex) indexer.getIndex();
		assertNotNull(index);
		assertEquals(0, indexer.getIndex().getDocumentIndex()
				.getNumberOfDocuments());
		assertEquals(0, indexer.getIndex().getLexicon().numberOfEntries());
		indexer.createDirectIndex(new Collection[] { collection });
		indexer.createInvertedIndex();
		assertEquals(2, indexer.getIndex().getDocumentIndex()
				.getNumberOfDocuments());
		assertEquals(4, indexer.getIndex().getLexicon().numberOfEntries());
		for (Document doc : docs2)
			index.indexDocument(doc);
		assertEquals(5, indexer.getIndex().getDocumentIndex()
				.getNumberOfDocuments());
		assertEquals(12, indexer.getIndex().getLexicon().numberOfEntries());
	}

	/*
	 * Write in-memory index to disk, then load it from disk.
	 */
	@Test
	public void test_todisk() throws Exception {
		MemoryIndex mem = TestUtils.memory(collection);
		assertNotNull(mem);
		mem.write(ApplicationSetup.TERRIER_INDEX_PATH, "memory");
		Index mem2disk = Index.createIndex(ApplicationSetup.TERRIER_INDEX_PATH,
				"memory");
		assertNotNull(mem2disk);
	}

	/*
	 * Comparison of memory and disk indices.
	 */
	@Test
	public void test_compare1() throws Exception {
		MemoryIndex mem = TestUtils.memory(collection);
		assertNotNull(mem);
		Index disk = IndexTestUtils.makeIndex(docids, documents);
		assertNotNull(disk);
		TestUtils.compareIndices(disk, mem);
		TestUtils.compareProperties(disk, mem);
		TestUtils.compareRetrieval("curry", disk, mem);
		TestUtils.compareRetrieval("church", disk, mem);
		TestUtils.compareRetrieval("knuth", disk, mem);
		TestUtils.compareRetrieval("turing", disk, mem);
	}

	/*
	 * Comparison of memory and memory->disk indices.
	 */
	@Test
	public void test_compare2() throws Exception {
		MemoryIndex mem = TestUtils.memory(collection);
		assertNotNull(mem);
		mem.write(ApplicationSetup.TERRIER_INDEX_PATH, "memory");
		Index mem2disk = Index.createIndex(ApplicationSetup.TERRIER_INDEX_PATH,
				"memory");
		assertNotNull(mem2disk);
		TestUtils.compareIndices(mem, mem2disk);
		TestUtils.compareProperties(mem, mem2disk);
		TestUtils.compareRetrieval("curry", mem, mem2disk);
		TestUtils.compareRetrieval("church", mem, mem2disk);
		TestUtils.compareRetrieval("knuth", mem, mem2disk);
		TestUtils.compareRetrieval("turing", mem, mem2disk);
	}

	/*
	 * Comparison of disk and memory->disk indices.
	 */
	@Test
	public void test_compare3() throws Exception {
		MemoryIndex mem = TestUtils.memory(collection);
		assertNotNull(mem);
		mem.write(ApplicationSetup.TERRIER_INDEX_PATH, "memory");
		Index mem2disk = Index.createIndex(ApplicationSetup.TERRIER_INDEX_PATH,
				"memory");
		assertNotNull(mem2disk);
		Index disk = IndexTestUtils.makeIndex(docids, documents);
		assertNotNull(disk);
		TestUtils.compareIndices(disk, mem2disk);
		TestUtils.compareProperties(disk, mem2disk);
		TestUtils.compareRetrieval("curry", disk, mem2disk);
		TestUtils.compareRetrieval("church", disk, mem2disk);
		TestUtils.compareRetrieval("knuth", disk, mem2disk);
		TestUtils.compareRetrieval("turing", disk, mem2disk);
	}

	/*
	 * Test data.
	 */
	private String[] docids, documents;
	private Document[] docs1, docs2;
	private Collection collection;

	@Before
	public void setUp() throws Exception {
		docids = new String[] { "doc1", "doc2" };
		documents = new String[] { "curry church turing knuth",
				"turing knuth knuth turing" };
		docs1 = new Document[] {
				new FileDocument("doc1", new ByteArrayInputStream(
						"curry church turing knuth".getBytes()),
						new EnglishTokeniser()),
				new FileDocument("doc2", new ByteArrayInputStream(
						"turing knuth knuth turing".getBytes()),
						new EnglishTokeniser()) };
		collection = new CollectionDocumentList(docs1, "filename");
		docs2 = new Document[] {
				new FileDocument("doc3", new ByteArrayInputStream(
						"foo bar".getBytes()), new EnglishTokeniser()),
				new FileDocument("doc4", new ByteArrayInputStream(
						"one two three".getBytes()), new EnglishTokeniser()),
				new FileDocument("doc5", new ByteArrayInputStream(
						"ten twenty thirty".getBytes()), new EnglishTokeniser()) };
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "10");
	}

	@After
	public void tearDown() {
		docids = documents = null;
		docs1 = docs2 = null;
		collection = null;
	}
}