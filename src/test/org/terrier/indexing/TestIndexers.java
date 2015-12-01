/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - Department of Computing Science
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
 * The Original Code is TestIndexers.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gnu.trove.TObjectIntHashMap;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.terrier.indexing.tokenisation.EnglishTokeniser;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.FieldDocumentIndex;
import org.terrier.structures.FieldDocumentIndexEntry;
import org.terrier.structures.FieldEntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.bit.BitPostingIndexInputStream;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.structures.indexing.classical.BlockIndexer;
import org.terrier.structures.indexing.singlepass.BasicSinglePassIndexer;
import org.terrier.structures.indexing.singlepass.BlockSinglePassIndexer;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.bit.BlockFieldIterablePosting;
import org.terrier.structures.postings.bit.FieldIterablePosting;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.realtime.MemoryIndexer;
import org.terrier.realtime.memory.MemoryInvertedIndex;

//TODO: does not check block positions
public class TestIndexers extends ApplicationSetupBasedTest {

	@Before public void setIndexerProperties() {
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "100");
		ApplicationSetup.setProperty("indexer.meta.reverse.keys", "");
		ApplicationSetup.setProperty("termpipelines", "");
	}

	
	protected void testIndexer(Indexer indexer, boolean directExpected, boolean fieldsExpected) throws Exception {
		testIndexer(indexer,directExpected,fieldsExpected, false);
		
	}
	
	@SuppressWarnings("unchecked")
	protected void testIndexer(Indexer indexer, boolean directExpected, boolean fieldsExpected, boolean memoryIndexer) throws Exception {
		
		Map<String,String> doc1Props = new HashMap<String,String>();doc1Props.put("filename", "doc1");
		Map<String,String> doc2Props = new HashMap<String,String>();doc2Props.put("filename", "doc2");
		
		Document[] sourceDocs = !fieldsExpected ?
				new Document[]{
						new FileDocument("doc1", new ByteArrayInputStream("cats dogs horses".getBytes()), new EnglishTokeniser()),
						new FileDocument("doc2", new ByteArrayInputStream("chicken cats chicken chicken".getBytes()), new EnglishTokeniser())
					}
				: new Document[]{
						new TaggedDocument(new ByteArrayInputStream("<title>cats</title> dogs horses".getBytes()), doc1Props, new EnglishTokeniser()),
						new TaggedDocument(new ByteArrayInputStream("<title>chicken</title> cats chicken chicken".getBytes()), doc2Props, new EnglishTokeniser())
					};
						
		int[] doclens = new int[]{3, 4};
		
		
		// INVERTED ----------
		// [num_terms][num_posts]
		int[][] invIds = new int[4][];
		// [num_terms][num_posts]
		int[][] invTfs = new int[4][];
		// [num_terms][num_fields][num_posts]
		int[][][] invFfs = new int[4][2][];
		
		// DIRECT ----------
		Map<String,int[]>[] dirFfs = new Map[2];
		TObjectIntHashMap<String>dirTfs[] = new TObjectIntHashMap[2];
		
		String[] termStrings = new String[] { "cats", "chicken", "dogs", "horses" };
		
		// populate inverted
		
		// 0 (0,1) // dogs
			// "dogs" occur in docid 0	
		invIds[2] = new int[] { 0 };
			// "dogs" has TF 1 in docid 0
		invTfs[2] = new int[] { 1 };
			// "dogs" has (TF_TITLE=0,TF_ELSE=1) for docid 0
		invFfs[2] = new int[][] { {0,1} };
		// 1 (0,1) // horses
		invIds[3] = new int[] { 0 };
		invTfs[3] = new int[] { 1 };
		invFfs[3] = new int[][] { {0,1} };
		// 2 (0,1) (1,1) // cats
			// "cats" occur in docids 0 and 1
		invIds[0] = new int[] { 0, 1 };
			// "cats" has TF 1 in docid 0, 1 in docid 1
		invTfs[0] = new int[] { 1, 1 };
			// "cats" has (TF_TITLE=1,TF_ELSE=0) for docid 0, (TF_TITLE=0,TF_ELSE=1) for docid 1
		invFfs[0] = new int[][] { {1,0}, {0,1} };
		// 3 (1,3) // chicken		
		invIds[1] = new int[] { 1 };
		invTfs[1] = new int[] { 3 };
		invFfs[1] = new int[][] { {1,2} };
		
		// populate direct
		
		// 0 (0,1) (1,1) (2,1) // doc1
		// 0=dogs, 1=horses, 2=cats
		// <title>cats</title> dogs horses
		dirTfs[0] = new TObjectIntHashMap<String>();
		dirTfs[0].put("cats", 1);
		dirTfs[0].put("dogs", 1);
		dirTfs[0].put("horses", 1);
		dirFfs[0] = new HashMap<String,int[]>();
		dirFfs[0].put("cats", new int[]{1,0});
		dirFfs[0].put("dogs", new int[]{0,1});
		dirFfs[0].put("horses", new int[]{0,1});
		
		
		dirTfs[1] = new TObjectIntHashMap<String>();
		dirTfs[1].put("cats", 1);
		dirTfs[1].put("chicken", 3);
		dirFfs[1] = new HashMap<String,int[]>();
		dirFfs[1].put("cats", new int[]{0,1});
		dirFfs[1].put("chicken", new int[]{1,2});
		
		
		Collection col = new CollectionDocumentList(sourceDocs, "filename");
		indexer.createDirectIndex(new Collection[]{col});
		indexer.createInvertedIndex();
		
		Index index = null;
		
		if (!memoryIndexer) {
			index = !fieldsExpected ?
					Index.createIndex(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX)
					: Index.createIndex(ApplicationSetup.TERRIER_INDEX_PATH, "fields");
		} else {
			index = ((MemoryIndexer)indexer).getIndex();
		}
				
		assertNotNull(index);
		
		MetaIndex meta = index.getMetaIndex();
		assertNotNull(meta);
		assertEquals("doc1", index.getMetaIndex().getItem("filename", 0));
		assertEquals("doc2", index.getMetaIndex().getItem("filename", 1));
		
		IterablePosting ip = null;
		PostingIndexInputStream bpiis = null;
		
		/** DOCUMENT INDEX */
		DocumentIndex doi = index.getDocumentIndex();
		assertEquals(3, doi.getDocumentLength(0));
		assertEquals(4, doi.getDocumentLength(1));
		if (fieldsExpected)
		{
			if (doi instanceof FieldDocumentIndex)
			{
				FieldDocumentIndex fDoi = (FieldDocumentIndex)doi;
				assertEquals(1, fDoi.getFieldLengths(0)[0]);
				assertEquals(2, fDoi.getFieldLengths(0)[1]);
				
				assertEquals(1, fDoi.getFieldLengths(1)[0]);
				assertEquals(3, fDoi.getFieldLengths(1)[1]);
			} else {
				FieldDocumentIndexEntry fdie;
				fdie = (FieldDocumentIndexEntry) doi.getDocumentEntry(0);
				assertEquals(1, fdie.getFieldLengths()[0]);
				assertEquals(2, fdie.getFieldLengths()[1]);
				fdie = (FieldDocumentIndexEntry) doi.getDocumentEntry(1);
				assertEquals(1, fdie.getFieldLengths()[0]);
				assertEquals(3, fdie.getFieldLengths()[1]);
			}
		}
		
		/** LEXICON */
		Lexicon<String> lexicon = index.getLexicon();

		LexiconEntry le = null;
		FieldEntryStatistics fe = null;
		le = lexicon.getLexiconEntry("cats");
		assertNotNull(le);
		assertEquals(2, le.getFrequency());
		assertEquals(2, le.getNumberOfEntries());
		if (fieldsExpected)
		{
			assertTrue(le instanceof FieldEntryStatistics);
			fe = (FieldEntryStatistics)le;
			assertEquals(1, fe.getFieldFrequencies()[0]);
			assertEquals(1, fe.getFieldFrequencies()[1]);
		}
		le = lexicon.getLexiconEntry("chicken");
		assertNotNull(le);
		assertEquals(3, le.getFrequency());
		assertEquals(1, le.getNumberOfEntries());
		if (fieldsExpected)
		{
			assertTrue(le instanceof FieldEntryStatistics);
			fe = (FieldEntryStatistics)le;
			assertEquals(1, fe.getFieldFrequencies()[0]);
			assertEquals(2, fe.getFieldFrequencies()[1]);
		}
		
		for (String t : new String[]{"dogs", "horses"})
		{
			le = lexicon.getLexiconEntry(t);
			assertNotNull(le);
			assertEquals(1, le.getFrequency());
			assertEquals(1, le.getNumberOfEntries());
			if (fieldsExpected)
			{
				assertTrue(le instanceof FieldEntryStatistics);
				fe = (FieldEntryStatistics)le;
				assertEquals(0, fe.getFieldFrequencies()[0]);
				assertEquals(1, fe.getFieldFrequencies()[1]);
			}
		}
		
		/** INVERTED FILE */		
		
		/**
		 * Test {@link IterablePosting} entries from a {@link InvertedIndex}
		 */
		PostingIndex<Pointer> invertedIndex = (PostingIndex<Pointer>) index.getInvertedIndex();
		assertNotNull(invertedIndex);
		// for each term
		for (int t = 0; t < termStrings.length; t++) {
			le = lexicon.getLexiconEntry(termStrings[t]);
			assertNotNull(le);
			if (memoryIndexer) ip = invertedIndex.getPostings(le);
			else ip = invertedIndex.getPostings(le);
			// for each document
			int d = 0;
			while (ip.next() != IterablePosting.EOL) {
				assertEquals(invIds[t][d], ip.getId());
				assertEquals(invTfs[t][d], ip.getFrequency());
				assertEquals(doclens[invIds[t][d]], ip.getDocumentLength());
				if (fieldsExpected) {
					assertEquals(2, invFfs[t][d].length);
					for (int f = 0; f < 2; f++) {
						if (ip instanceof BlockFieldIterablePosting) assertEquals(invFfs[t][d][f], ((BlockFieldIterablePosting) ip).getFieldFrequencies()[f]); 
						else assertEquals(invFfs[t][d][f], ((FieldIterablePosting) ip).getFieldFrequencies()[f]); 
					}
				}
				d++;
			}
			ip.close();
		}
		// post-check
		assertEquals(IterablePosting.EOL, ip.next());

		/**
		 * Test {@link IterablePosting} entries from a {@link InvertedIndexInputStream}
		 */
		if (memoryIndexer) bpiis = (MemoryInvertedIndex.InvertedIterator) index.getIndexStructureInputStream("inverted");
		else bpiis = (BitPostingIndexInputStream) index.getIndexStructureInputStream("inverted");
		assertNotNull(bpiis);
		// for each term
		for (int t = 0; t < invIds.length; t++) {
			assertTrue(bpiis.hasNext());
			ip = bpiis.next();
			assertNotNull(ip);
			// for each document
			int d = 0;
			while (ip.next() != IterablePosting.EOL) {
				assertEquals(invIds[t][d], ip.getId());
				assertEquals(invTfs[t][d], ip.getFrequency());
				assertEquals(doclens[invIds[t][d]], ip.getDocumentLength());
				if (fieldsExpected) {
					assertEquals(2, invFfs[t][d].length);
					for (int f = 0; f < 2; f++) {
						assertEquals(invFfs[t][d][f], ((FieldPosting) ip).getFieldFrequencies()[f]); 
					}
				}
				d++;
			}
		}
		// post-check
		assertFalse(bpiis.hasNext());
		bpiis.close();

		
		/** DIRECT FILE */
		
		if (directExpected) {
			DocumentIndex documentIndex = index.getDocumentIndex();

			/**
			 * Test {@link IterablePosting} entries from a {@link DirectIndex}
			 */
			PostingIndex<Pointer> directIndex = (PostingIndex<Pointer>) index.getDirectIndex();
			assertNotNull(directIndex);
			// for each document
			for (int d = 0; d < dirTfs.length; d++) {
				DocumentIndexEntry de = documentIndex.getDocumentEntry(d);
				assertNotNull(de);
				ip = directIndex.getPostings(de);
				FieldPosting fp = fieldsExpected ? (FieldPosting)ip : null;
				// for each term
				//int t = 0;
				int countFoundTerms = 0;
				while (ip.next() != IterablePosting.EOL) {
					int termid = ip.getId();
					assertTrue(termid >= 0);
					String term = lexicon.getLexiconEntry(termid).getKey();
					assertNotNull(term);
					countFoundTerms++;
					assertTrue(dirTfs[d].containsKey(term));
					assertEquals(dirTfs[d].get(term), ip.getFrequency());
					assertEquals(doclens[d], ip.getDocumentLength());					
					
					if (fieldsExpected) {
						assertEquals(2, fp.getFieldFrequencies().length);
						for (int f = 0; f < 2; f++) {
							assertEquals(dirFfs[d].get(term)[f], fp.getFieldFrequencies()[f]); 
						}
					}
					//t++;
				}
				assertEquals(dirTfs[d].size() ,countFoundTerms);
				ip.close();
			}
			// post-check
			assertEquals(IterablePosting.EOL, ip.next());

			/**
			 * Test {@link IterablePosting} entries from a {@link DirectIndexInputStream}
			 */
			bpiis = (BitPostingIndexInputStream) index.getIndexStructureInputStream("direct");
			assertNotNull(bpiis);
			// for each document
			for (int d = 0; d < dirTfs.length; d++) {
				assertTrue(bpiis.hasNext());
				ip = bpiis.next();
				assertNotNull(ip);
				FieldPosting fp = fieldsExpected ? (FieldPosting)ip : null;
				// for each term
				//int t = 0;
				int countFoundTerms = 0;
				while (ip.next() != IterablePosting.EOL) {
					int termid = ip.getId();
					assertTrue(termid >= 0);
					String term = lexicon.getLexiconEntry(termid).getKey();
					assertNotNull(term);
					countFoundTerms++;
					assertTrue(dirTfs[d].containsKey(term));
					assertEquals(dirTfs[d].get(term), ip.getFrequency());
					assertEquals("document length was wrong for docid " + d, doclens[d], ip.getDocumentLength());					
					
					if (fieldsExpected) {
						assertEquals(2, fp.getFieldFrequencies().length);
						for (int f = 0; f < 2; f++) {
							assertEquals(dirFfs[d].get(term)[f], fp.getFieldFrequencies()[f]); 
						}
					}
					//t++;
				}
				assertEquals(dirTfs[d].size() ,countFoundTerms);
			}
			// post-check
			assertFalse(bpiis.hasNext());
			bpiis.close();

			
		}
		index.close();
	}
	
	@Test
	public void testBasicNoFields() throws Exception
	{
		ApplicationSetup.setProperty("FieldTags.process", "");
		testIndexer(new BasicIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX), true, false);
	}
	
	@Test
	public void testBasicFields() throws Exception
	{
		ApplicationSetup.setProperty("FieldTags.process", "TITLE,ELSE");
		testIndexer(new BasicIndexer(ApplicationSetup.TERRIER_INDEX_PATH, "fields"), true, true);
	}
	
	@Test
	public void testBlockNoFields() throws Exception
	{
		ApplicationSetup.setProperty("FieldTags.process", "");
		testIndexer(new BlockIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX), true, false);
	}
	@Test
	public void testBlockFields() throws Exception
	{
		ApplicationSetup.setProperty("FieldTags.process", "TITLE,ELSE");
		testIndexer(new BlockIndexer(ApplicationSetup.TERRIER_INDEX_PATH, "fields"), true, true);
	}
	
	@Test
	public void testBasicSPNoFields() throws Exception
	{
		ApplicationSetup.setProperty("FieldTags.process", "");
		testIndexer(new BasicSinglePassIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX), false, false);
	}
	@Test
	public void testBasicSPFields() throws Exception
	{
		ApplicationSetup.setProperty("FieldTags.process", "TITLE,ELSE");
		testIndexer(new BasicSinglePassIndexer(ApplicationSetup.TERRIER_INDEX_PATH, "fields"), false, true);
	}
	
	@Test
	public void testBlockSPNoFields() throws Exception
	{
		ApplicationSetup.setProperty("FieldTags.process", "");
		testIndexer(new BlockSinglePassIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX), false, false);
	}
	@Test
	public void testBlockSPFields() throws Exception
	{
		ApplicationSetup.setProperty("FieldTags.process", "TITLE,ELSE");
		testIndexer(new BlockSinglePassIndexer(ApplicationSetup.TERRIER_INDEX_PATH, "fields"), false, true);
	}

}
