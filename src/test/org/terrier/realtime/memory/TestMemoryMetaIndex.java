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
 * The Original Code is TestMemoryMetaIndex.java.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.terrier.utility.ApplicationSetup;

/** Unit test for InMemoryMetaIndex. */
public class TestMemoryMetaIndex {

	/*
	 * InMemoryMetaIndex(String[] keys)
	 */
	@Test
	public void test_InMemoryMetaIndex() throws Exception {
		MemoryMetaIndex metaindex = new MemoryMetaIndex();
		assertNotNull(metaindex);
	}

	/*
	 * writeDocumentEntry(String[] data)
	 */
//	@Test
//	public void test_writeDocumentEntry1() throws Exception {
//		MemoryMetaIndex metaindex = new MemoryMetaIndex();
//		assertNotNull(metaindex);
//		assertArrayEquals(keys, metaindex.getKeys());
//		for (String[] doc : metadata) metaindex.writeDocumentEntry(doc);
//	}

	/*
	 * writeDocumentEntry(Map<String, String> data)
	 */
	@Test
	public void test_writeDocumentEntry2() throws Exception {
		MemoryMetaIndex metaindex = new MemoryMetaIndex();
		assertNotNull(metaindex);
		assertArrayEquals(keys, metaindex.getKeys());
		metaindex.writeDocumentEntry(doc1);
		metaindex.writeDocumentEntry(doc2);
		metaindex.writeDocumentEntry(doc3);
	}

	/*
	 * getKeys()
	 */
	@Test
	public void test_getKeys() throws Exception {
		MemoryMetaIndex metaindex = new MemoryMetaIndex();
		assertNotNull(metaindex);
		assertArrayEquals(keys, metaindex.getKeys());
	}
	
	/*
	 * getItem(String key, int docid)
	 */
	@Test
	public void test_getItem() throws Exception {
		MemoryMetaIndex metaindex = new MemoryMetaIndex();
		assertNotNull(metaindex);
		assertArrayEquals(keys, metaindex.getKeys());
//		for (String[] doc : metadata) metaindex.writeDocumentEntry(doc);
//		for (int i = 0; i < 10; i++) {
//			assertEquals(metadata[i][0], metaindex.getItem("docno", i));
//			assertEquals(metadata[i][1], metaindex.getItem("title", i));
//			assertEquals(metadata[i][2], metaindex.getItem("url",   i));
//		}
		metaindex.writeDocumentEntry(doc1);
		metaindex.writeDocumentEntry(doc2);
		metaindex.writeDocumentEntry(doc3);
		assertEquals(doc1.get("docno"), metaindex.getItem("docno", 0));
		assertEquals(doc1.get("title"), metaindex.getItem("title", 0));
		assertEquals(doc1.get("url"),   metaindex.getItem("url",   0));
		assertEquals(doc2.get("docno"), metaindex.getItem("docno", 1));
		assertEquals(doc2.get("title"), metaindex.getItem("title", 1));
		assertEquals(doc2.get("url"),   metaindex.getItem("url",   1));
		assertEquals(doc3.get("docno"), metaindex.getItem("docno", 2));
		assertEquals(doc3.get("title"), metaindex.getItem("title", 2));
		assertEquals(doc3.get("url"),   metaindex.getItem("url",   2));
	}

	/*
	 * getAllItems(int docid)
	 */
	@Test
	public void test_getAllItems() throws Exception {
		MemoryMetaIndex metaindex = new MemoryMetaIndex();
		assertNotNull(metaindex);
		assertArrayEquals(keys, metaindex.getKeys());
//		for (String[] doc : metadata) metaindex.writeDocumentEntry(doc);
//		for (int i = 0; i < 10; i++)
//			assertArrayEquals(metadata[i], metaindex.getAllItems(i));
		metaindex.writeDocumentEntry(doc1);
		metaindex.writeDocumentEntry(doc2);
		metaindex.writeDocumentEntry(doc3);
		assertArrayEquals(new String[] {doc1.get("docno"),doc1.get("title"),doc1.get("url")},metaindex.getAllItems(0));
		assertArrayEquals(new String[] {doc2.get("docno"),doc2.get("title"),doc2.get("url")},metaindex.getAllItems(1));
		assertArrayEquals(new String[] {doc3.get("docno"),doc3.get("title"),doc3.get("url")},metaindex.getAllItems(2));
	}

	/*
	 * getItems(String key, int[] docids)
	 */
	@Test
	public void test_getItems1() throws Exception {
		MemoryMetaIndex metaindex = new MemoryMetaIndex();
		assertNotNull(metaindex);
		assertArrayEquals(keys, metaindex.getKeys());
//		for (String[] doc : metadata) metaindex.writeDocumentEntry(doc);
//		String[] docno = metaindex.getItems("docno", new int[] {0,1,2,3,4,5,6,7,8,9});
//		String[] title = metaindex.getItems("title", new int[] {0,1,2,3,4,5,6,7,8,9});
//		String[] url =   metaindex.getItems("url",   new int[] {0,1,2,3,4,5,6,7,8,9});
//		for (int i = 0; i < 10; i++) {
//			assertEquals(metadata[i][0], docno[i]);
//			assertEquals(metadata[i][1], title[i]);
//			assertEquals(metadata[i][2], url[i]);
//		}
		metaindex.writeDocumentEntry(doc1);
		metaindex.writeDocumentEntry(doc2);
		metaindex.writeDocumentEntry(doc3);
		assertArrayEquals(new String[] {doc1.get("docno"),doc2.get("docno"),doc3.get("docno")},
						  metaindex.getItems("docno", new int[] {0,1,2}));
		assertArrayEquals(new String[] {doc1.get("title"),doc2.get("title"),doc3.get("title")},
						  metaindex.getItems("title", new int[] {0,1,2}));
		assertArrayEquals(new String[] {doc1.get("url"),doc2.get("url"),doc3.get("url")},
				 		  metaindex.getItems("url", new int[] {0,1,2}));
	}

	/*
	 * getItems(String[] keys, int docid)
	 */
	@Test
	public void test_getItems2() throws Exception {
		MemoryMetaIndex metaindex = new MemoryMetaIndex();
		assertNotNull(metaindex);
		assertArrayEquals(keys, metaindex.getKeys());
//		for (String[] doc : metadata) metaindex.writeDocumentEntry(doc);
//		for (int i = 0; i < 10; i++)
//			assertArrayEquals(metadata[i], metaindex.getItems(keys, i));
		metaindex.writeDocumentEntry(doc1);
		metaindex.writeDocumentEntry(doc2);
		metaindex.writeDocumentEntry(doc3);
		assertArrayEquals(new String[] {doc1.get("docno"),doc1.get("title"),doc1.get("url")},metaindex.getItems(keys,0));
		assertArrayEquals(new String[] {doc2.get("docno"),doc2.get("title"),doc2.get("url")},metaindex.getItems(keys,1));
		assertArrayEquals(new String[] {doc3.get("docno"),doc3.get("title"),doc3.get("url")},metaindex.getItems(keys,2));
	}

	/*
	 * getItems(String[] keys, int[] docids)
	 */
	@Test
	public void test_getItems3() throws Exception {
		MemoryMetaIndex metaindex = new MemoryMetaIndex();
		assertNotNull(metaindex);
		assertArrayEquals(keys, metaindex.getKeys());
//		for (String[] doc : metadata) metaindex.writeDocumentEntry(doc);
//		int i = 0;
//		for (String[] meta : metaindex.getItems(keys, new int[] {0,1,2,3,4,5,6,7,8,9})) assertArrayEquals(metadata[i++], meta);
		metaindex.writeDocumentEntry(doc1);
		metaindex.writeDocumentEntry(doc2);
		metaindex.writeDocumentEntry(doc3);
		String[][] meta = metaindex.getItems(keys, new int[] {0,1,2 });
		assertArrayEquals(new String[] {doc1.get("docno"),doc1.get("title"),doc1.get("url")}, meta[0]);
		assertArrayEquals(new String[] {doc2.get("docno"),doc2.get("title"),doc2.get("url")}, meta[1]);
		assertArrayEquals(new String[] {doc3.get("docno"),doc3.get("title"),doc3.get("url")}, meta[2]);
	}

	/*
	 * iterator()
	 */
	@Test
	public void test_iterator() throws Exception {
		MemoryMetaIndex metaindex = new MemoryMetaIndex();
		assertNotNull(metaindex);
		assertArrayEquals(keys, metaindex.getKeys());
//		for (String[] doc : metadata) metaindex.writeDocumentEntry(doc);
		Iterator<String[]> it = metaindex.iterator();
		assertNotNull(it);
		int i = 0;
		while (it.hasNext()) assertArrayEquals(metadata[i++], it.next());
	}

	/*
	 * Test data.
	 */
	String[] keys;
	String[][] metadata;
	Map<String, String> doc1,doc2,doc3;

	@SuppressWarnings("serial")
	@Before
	public void setUp() {
		keys = new String[] {"docno","title","url" };
		ApplicationSetup.setProperty("indexer.meta.forward.keys","docno,title,url");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens","10,100,100");
		metadata = new String[][] {
			{"01","a","b"},{"02","c","d"},{"03","e","f"},
			{"04","g","h"},{"05","i","j"},{"06","k","l"},
			{"07","m","n"},{"08","o","p"},{"09","q","r"},
			{"10","s","t"}
		};
		doc1 = new HashMap<String, String>() {{
			put("docno", "11");
			put("title", "University of Glasgow");
			put("url", "http://www.gla.ac.uk/");
		}};
		doc2 = new HashMap<String, String>() {{
			put("docno", "12");
			put("title", "Computing Science");
			put("url", "http://www.dcs.gla.ac.uk/");
		}};
		doc3 = new HashMap<String, String>() {{
			put("docno", "13");
			put("title", "Terrier IR Platform");
			put("url", "http://terrier.org/");
		}};
	}

	@After
	public void tearDown() {
		keys=null;
		metadata=null;
		doc1=doc2=doc3=null;
	}

}
