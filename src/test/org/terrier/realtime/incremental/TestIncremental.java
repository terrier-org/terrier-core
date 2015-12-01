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
 * The Original Code is TestIncremental.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.incremental;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;

import org.junit.Test;
import org.terrier.indexing.Collection;
import org.terrier.indexing.CollectionDocumentList;
import org.terrier.indexing.Document;
import org.terrier.indexing.FileDocument;
import org.terrier.indexing.tokenisation.EnglishTokeniser;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestIncremental extends ApplicationSetupBasedTest {

	/*
	 * Test IndexInMemory.
	 */
	@Test
	public void test_IncrementalIndex1() throws Exception {
		IncrementalIndex index = IncrementalIndex.get(
				ApplicationSetup.TERRIER_INDEX_PATH,
				ApplicationSetup.TERRIER_INDEX_PREFIX);
		assertNotNull(index);
		// assertEquals(1, index.indices.size());
	}

	/*
	 * Test IndexInMemory.
	 */
	@SuppressWarnings("unused")
	@Test
	public void test_IncrementalIndex() throws Exception {
		IndexOnDisk disk1 = disk(1);
		IndexOnDisk disk2 = disk(11);
		IndexOnDisk disk3 = disk(101);
		IncrementalIndex index = IncrementalIndex.get(
				ApplicationSetup.TERRIER_INDEX_PATH,
				ApplicationSetup.TERRIER_INDEX_PREFIX);
		assertNotNull(index);
		// assertEquals(4, index.indices.size());
	}

	/*
	 * make index disk1 with m document make increcmenta index populate
	 * incremental index with same m documents compare indices make index disk2
	 * with n documents merge index disk2 with disk1 (OR make disk2 with m+n
	 * docs OR make multiindex with disk1 & disk 2) add n documents to
	 * incremental index compare indices
	 */

	// protected void testIncrementalIndex(String[][] docnos, String [][]
	// contents) throws Exception
	// {
	// final int batchCount = docnos.length;
	// IncrementalIndex ii;
	// List<Index> currentDisk = new ArrayList<Index>();
	// Index di;
	// for(int b=0;b<batchCount;b++)
	// {
	// Index thisDisk = IndexTestUtils.makeIndex(docnos[b], contents[b]);
	// currentDisk.add(thisDisk);
	// Index mi = new MultiIndex(currentDisk.toArray(new Index[0]));
	// Index di = makeDiskIndexThusFar(docnos, contents, b);
	// //TODO: add docnos[b] and contents[b] to ii;
	//
	// compareIndices(mi, ii);
	// compareIndices(di, ii);
	// }
	// }

	// protected Index makeDiskIndexThusFar(String[][] docnos, String [][]
	// contents, int b)
	// {
	// List<String> _docnos;
	// List<String> _contents;
	// //populate upto b
	// return IndexTestUtils.makeIndex(_docnos, _contents);
	// }

	/*
	 * Build on-disk index from test data.
	 */
	public IndexOnDisk disk(int prefix) {
		System.err.println("disk()");
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "100");
		ApplicationSetup.setProperty("indexer.meta.reverse.keys", "filename");
		ApplicationSetup.setProperty("termpipelines", "");
		Document[] docs1 = new Document[] {
				new FileDocument("doc1", new ByteArrayInputStream(
						"curry church turing knuth".getBytes()),
						new EnglishTokeniser()),
				new FileDocument("doc2", new ByteArrayInputStream(
						"turing knuth knuth turing".getBytes()),
						new EnglishTokeniser()) };
		Collection coll = new CollectionDocumentList(docs1, "filename");
		BasicIndexer indexer = new BasicIndexer(
				ApplicationSetup.TERRIER_INDEX_PATH,
				ApplicationSetup.TERRIER_INDEX_PREFIX + "-"
						+ String.valueOf(prefix));
		assertNotNull(indexer);
		indexer.createDirectIndex(new Collection[] { coll });
		indexer.createInvertedIndex();
		IndexOnDisk index = (IndexOnDisk) Index.createIndex(
				ApplicationSetup.TERRIER_INDEX_PATH,
				ApplicationSetup.TERRIER_INDEX_PREFIX + "-"
						+ String.valueOf(prefix));
		assertNotNull(index);
		System.err.println("done");
		return index;
	}

}
