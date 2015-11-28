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
 * The Original Code is TestUtils.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.terrier.indexing.Collection;
import org.terrier.indexing.CollectionDocumentList;
import org.terrier.indexing.Document;
import org.terrier.indexing.FileDocument;
import org.terrier.indexing.tokenisation.EnglishTokeniser;
import org.terrier.matching.ResultSet;
import org.terrier.querying.Manager;
import org.terrier.querying.SearchRequest;
import org.terrier.realtime.memory.MemoryIndex;
import org.terrier.realtime.memory.fields.MemoryFieldsIndex;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.ApplicationSetup;

public class TestUtils {

	public static Index makeIndex(String[] docnos, String[] documents) throws Exception
	{
		return makeIndex(docnos, documents, new BasicIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX));
	}
	
	public static Index makeIndex(String[] docnos, String[] documents, Indexer indexer) throws Exception
	{
		assertEquals(docnos.length, documents.length);
		Document[] sourceDocs = new Document[docnos.length];
		for(int i=0;i<docnos.length;i++)
		{
			Map<String,String> docProperties = new HashMap<String,String>();
			docProperties.put("filename", docnos[i]);
			sourceDocs[i] = new FileDocument(new ByteArrayInputStream(documents[i].getBytes()), docProperties, new EnglishTokeniser());
		}
		Collection col = new CollectionDocumentList(sourceDocs, "filename");
		indexer.index(new Collection[]{col});		
		Index index = Index.createIndex();
		assertEquals(sourceDocs.length, index.getCollectionStatistics().getNumberOfDocuments());
		return index;
	}

	/*
	 * Build in-memory index from test data.
	 */
	public static MemoryIndex memory(Collection collection) {
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.reverse.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "32");
		ApplicationSetup.setProperty("termpipelines", "");
		MemoryIndexer indexer = new MemoryIndexer();
		assertNotNull(indexer);
		indexer.createDirectIndex(new Collection[] { collection });
		indexer.createInvertedIndex();
		MemoryIndex index = (MemoryIndex) indexer.getIndex();
		assertNotNull(index);
		return index;
	}
	
	/*
	 * Build in-memory index from test data.
	 */
	public static MemoryFieldsIndex memoryFields(Collection collection) {
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.reverse.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "32");
		ApplicationSetup.setProperty("termpipelines", "");
		ApplicationSetup.setProperty("FieldTags.process", "TITLE,ELSE");
		MemoryIndexer indexer = new MemoryIndexer(true);
		assertNotNull(indexer);
		indexer.createDirectIndex(new Collection[] { collection });
		indexer.createInvertedIndex();
		MemoryIndex index = (MemoryIndex) indexer.getIndex();
		assertTrue("Failed to create a memory fields index",index instanceof MemoryFieldsIndex);
		
		assertNotNull(index);
		return (MemoryFieldsIndex)index;
	}
	
	/*
	 * Run a query against an index.
	 */
	public static ResultSet query(String query,Index index) {
		ApplicationSetup.setProperty("ignore.low.idf.terms", "false");
		Manager mgr = new Manager(index);
		assertNotNull(mgr);
		SearchRequest srq = mgr.newSearchRequest(query, query);
		assertNotNull(srq);
		srq.addMatchingModel("Matching","TF_IDF");
		mgr.runPreProcessing(srq);
		mgr.runMatching(srq);
		mgr.runPostProcessing(srq);
		mgr.runPostFilters(srq);
		ResultSet result = srq.getResultSet();
		assertNotNull(result);
		return result;
	}

	/*
	 * Compare a query against two indices.
	 */
	public static void compareRetrieval(String query,Index index1,Index index2) {
		ResultSet results1 = TestUtils.query(query, index1);
		assertNotNull(results1);
		ResultSet results2 = TestUtils.query(query, index2);
		assertNotNull(results2);
		assertEquals(results1.getResultSize(), results2.getResultSize());
		int[] docids1 = results1.getDocids();
		int[] docids2 = results2.getDocids();
		double[] scores1 = results1.getScores();
		double[] scores2 = results2.getScores();
		assertEquals(docids1.length, docids2.length);
		for (int i = 0; i < docids1.length; i++) {
			assertEquals(docids1[i], docids2[i]);
			assertEquals("Mismatch for score of docid "+docids1[i]+" at rank "+i,scores1[i],scores2[i],0d);
		}
	}
	
	/*
	 * compare index properties.
	 */
	public static void compareProperties(Index index1, Index index2) {
		Properties props1 = index1.getProperties();
		Set<Entry<Object,Object>> props2 = index2.getProperties().entrySet();
		Iterator<Entry<Object, Object>> it = props2.iterator();
		while (it.hasNext()) {
			Entry<Object, Object> property = it.next();
			String key = (String) property.getKey();
			if (props1.containsKey(key)) {
				//System.err.println(key);
				if (key.equals("index.created")) continue;
				if (key.equals("index.document.class")) continue;
				if (key.equalsIgnoreCase("index.lexicon-valuefactory.parameter_values")) continue; // TREC-397: default is to use the direct structure, but only inverted is written
				if (key.equalsIgnoreCase("index.document-factory.parameter_values")) continue; // TREC-397: default is to use the direct structure, but only inverted is written
				if (key.contains("num.field")) continue; // TREC-397: the number of tokens per field are never added to the memory index
				assertEquals(key,props1.get(key),property.getValue());
			}
		}
	}
	
	/*
	 * Compare the contents of two indices.
	 */
	@SuppressWarnings("unchecked")
	public static void compareIndices(Index index1, Index index2) throws IOException {
		/*
		 * Get index structures.
		 */
		Lexicon<String> lexicon1 = index1.getLexicon(); 										assertNotNull(lexicon1);
		Lexicon<String> lexicon2 = index2.getLexicon();											assertNotNull(lexicon2);
		PostingIndex<Pointer> inverted1 = (PostingIndex<Pointer>) index1.getInvertedIndex(); 	assertNotNull(inverted1);
		PostingIndex<Pointer> inverted2 = (PostingIndex<Pointer>) index2.getInvertedIndex(); 	assertNotNull(inverted2);
		MetaIndex meta1 = index1.getMetaIndex(); 												assertNotNull(meta1);
		MetaIndex meta2 = index2.getMetaIndex(); 												assertNotNull(meta2);
		CollectionStatistics stats1 = index1.getCollectionStatistics(); 						assertNotNull(stats1);
		CollectionStatistics stats2 = index2.getCollectionStatistics();							assertNotNull(stats2);
		DocumentIndex docindex1 = index1.getDocumentIndex();									assertNotNull(docindex1);
		DocumentIndex docindex2 = index2.getDocumentIndex();									assertNotNull(docindex2);
		/*
		 * Compare lexicon.
		 */
		// NOT IMP: assertEquals(lexicon1.numberOfEntries(), lexicon2.numberOfEntries());
		for (int i=0;i<lexicon1.numberOfEntries();i++) {
			Entry<String, LexiconEntry> kv1 = lexicon1.getLexiconEntry(i);
			LexiconEntry kv2 = lexicon2.getLexiconEntry(kv1.getKey());
			assertNotNull(kv2);
			assertEquals(kv1.getValue().getDocumentFrequency(), kv2.getDocumentFrequency());
			assertEquals(kv1.getValue().getFrequency(), 		kv2.getFrequency());
		}
		/*
		 * Compare inverted file.
		 */
		for (int i=0;i<lexicon1.numberOfEntries();i++) {
			Entry<String,LexiconEntry> firstEntry = lexicon1.getLexiconEntry(i);
			IterablePosting post1 = inverted1.getPostings(firstEntry.getValue());
			assertNotNull(post1);
			IterablePosting post2 = inverted2.getPostings(lexicon2.getLexiconEntry(firstEntry.getKey()));
			assertNotNull(post2);
			while ((post1.next() != -1) && (post2.next() != -1)) {
				assertEquals(post1.getId(), 			post2.getId());
				assertEquals(post1.getFrequency(), 		post2.getFrequency());
				assertEquals(post1.getDocumentLength(), post2.getDocumentLength());	
			}
			assertEquals(IterablePosting.EOL, post1.next());
			assertEquals(IterablePosting.EOL, post2.next());
		}
		/*
		 * Compare metadata.
		 */
		assertArrayEquals(meta1.getKeys(), meta2.getKeys());
		for (int i=0;i<docindex1.getNumberOfDocuments();i++)
			assertArrayEquals(meta1.getAllItems(i), meta2.getAllItems(i));
		/*
		 * Compare collection statistics.
		 */
		assertEquals(stats1.getNumberOfDocuments(),		stats2.getNumberOfDocuments());
		assertEquals(stats1.getNumberOfTokens(), 		stats2.getNumberOfTokens());
		//NOTDO: assertEquals(stats1.getNumberOfUniqueTerms(), 	stats2.getNumberOfUniqueTerms());
		assertEquals(stats1.getNumberOfPointers(), 		stats2.getNumberOfPointers());
		assertEquals(stats1.getAverageDocumentLength(),	stats2.getAverageDocumentLength(), 0.0d);
		/*
		 * Compare document index.
		 */
		assertEquals(docindex1.getNumberOfDocuments(),docindex2.getNumberOfDocuments());
		for (int i=0;i<docindex1.getNumberOfDocuments();i++)
			assertEquals(docindex1.getDocumentLength(i),docindex2.getDocumentLength(i));
	}
	
	/*
	 * Given an index, check (for term) that TF, DF and postings are correct. 
	 */
	@SuppressWarnings("unchecked")
	public static void checkContents(Index index, String term, int freq, int[] docids, int[] freqs, int[] docLens) throws Exception {
		Lexicon<String> lexicon = index.getLexicon();
		assertNotNull(lexicon);
		LexiconEntry le = lexicon.getLexiconEntry(term);
		assertNotNull(le);
		assertEquals(freq, le.getFrequency());
		assertEquals(docids.length, le.getDocumentFrequency());
		PostingIndex<Pointer> inverted = (PostingIndex<Pointer>) index.getInvertedIndex();
		assertNotNull(inverted);
		IterablePosting ip = inverted.getPostings(le);
		assertNotNull(ip);
		for(int i=0;i<docids.length;i++) {
			assertEquals(docids[i], ip.next());
			assertEquals(docids[i], ip.getId());
			assertEquals(freqs[i],  ip.getFrequency());
			assertEquals(docLens[i],ip.getDocumentLength());
		}
		assertEquals(IterablePosting.EOL, ip.next());
	}
	
}
