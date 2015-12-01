/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is TestFSOrderedMapFile.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import gnu.trove.TObjectIntHashMap;

import java.io.File;
import java.io.Flushable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.terrier.structures.Skipable;
import org.terrier.structures.collections.FSOrderedMapFile.MapFileWriter;
import org.terrier.structures.seralization.FixedSizeIntWritableFactory;
import org.terrier.structures.seralization.FixedSizeTextFactory;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.io.RandomDataInputMemory;

/** Make some tests on FSOrderedMapFile */
public class TestFSOrderedMapFile {
	@Rule public TemporaryFolder tf = new TemporaryFolder();
	String file;
	String[] testKeys = new String[]{"00", "0000", "1", "a", "z", new String(new byte[]{(byte)0xbb,(byte)0xB6,(byte)0xD3,(byte)0xAD}, "gb2312")};
	String[] testNotInKeys = new String[]{"0", "000", "zz", new String(new byte[]{(byte)0xbb,(byte)0xB6,(byte)0xD3,(byte)0xAF}, "gb2312")};
	protected TObjectIntHashMap<String> key2id = new TObjectIntHashMap<String>();
	
	
	public TestFSOrderedMapFile() throws Exception{}
	
	@Before
	public void setUp() throws Exception {
		file = tf.newFile("testFSOMapfile" + FSOrderedMapFile.USUAL_EXTENSION).toString();
		MapFileWriter w = FSOrderedMapFile.mapFileWrite(file);
		FixedSizeWriteableFactory<Text> keyFactory = new FixedSizeTextFactory(20);
		int offset = 0;
		for(String key : testKeys)
		{
			Text wkey = keyFactory.newInstance();
			IntWritable wvalue = new IntWritable();
			wkey.set(key);
			wvalue.set(offset);

			w.write(wkey, wvalue);
			key2id.put(key, offset);
			offset++;
		}
		w.close();			
	}
	
	protected void checkKeysGetEntry(FixedSizeTextFactory keyFactory, FSOrderedMapFile<Text, IntWritable> mapToCheck) throws Exception
	{
		Text k = keyFactory.newInstance();
		int i=0;
		for(String t : testKeys)
		{
			k.set(t);
			assertEquals(i, mapToCheck.getEntry(k).index);
			i++;
		}
		//check negative indices are comparible with Arrays.binarySearch
		for(String t : testNotInKeys)
		{
			k.set(t);
			assertEquals(Arrays.binarySearch(testKeys, t), mapToCheck.getEntry(k).index);
		}
		
		//before first entry
		k.set("0");
		assertEquals(-1, mapToCheck.getEntry(k).index);
		assertNull(mapToCheck.getEntry(k).getValue());
		//after last entry
		k.set( new String(new byte[]{(byte)0xbb,(byte)0xB6,(byte)0xD3,(byte)0xAF}, "gb2312"));
		assertEquals(-(mapToCheck.size()), mapToCheck.getEntry(k).index);
		assertNull(mapToCheck.getEntry(k).getValue());
	}
	
	protected void checkKeys(FixedSizeTextFactory keyFactory, SortedMap<Text,IntWritable> mapToCheck) throws Exception
	{
		assertEquals(testKeys.length, mapToCheck.size());
		Set<Text> keySet = mapToCheck.keySet();
		Set<Entry<Text,IntWritable>> entrySet = mapToCheck.entrySet();
		Collection<IntWritable> values = mapToCheck.values();
		assertEquals(testKeys.length, keySet.size());
		assertEquals(testKeys.length, entrySet.size());
		assertEquals(testKeys.length, values.size());
		
		for(String key : testKeys)
		{
			Text testKey = keyFactory.newInstance();
			testKey.set(key);
			IntWritable rtr = mapToCheck.get(testKey);
			assertNotNull("Got null response for key " + key, rtr);
			assertEquals("Response for key " + key + " was wrong", key2id.get(key), rtr.get());
		}
		String[] randomOrderedTestKeys = new String[testKeys.length];
		System.arraycopy(testKeys, 0, randomOrderedTestKeys, 0, testKeys.length);
		Collections.shuffle(Arrays.asList(randomOrderedTestKeys));
		
		int i=0;
		Iterator<Text> kIter = keySet.iterator();
		Iterator<Entry<Text,IntWritable>> eIter = entrySet.iterator();
		Iterator<IntWritable> vIter = values.iterator();
		while(kIter.hasNext())
		{
			assertTrue("mismatch on i="+i, vIter.hasNext());
			assertTrue("mismatch on i="+i, eIter.hasNext());
			assertNotNull("mismatch on i="+i, kIter.next());
			assertNotNull("mismatch on i="+i, vIter.next());
			assertNotNull("mismatch on i="+i, eIter.next());
			i++;
		}
		assertFalse(vIter.hasNext());
		assertFalse(eIter.hasNext());
		assertEquals(testKeys.length, i);
		
		for(String key : randomOrderedTestKeys)
		{
			Text testKey = keyFactory.newInstance();
			testKey.set(key);
			IntWritable rtr = mapToCheck.get(testKey);
			assertNotNull("Got null response for key " + key, rtr);
			assertEquals("Response for key " + key + " was wrong", key2id.get(key), rtr.get());
			assertTrue(keySet.contains(testKey));
		}

		for(String key : testNotInKeys)
		{
			Text testKey = keyFactory.newInstance();
			testKey.set(key);
			IntWritable rtr = mapToCheck.get(testKey);
			assertNull("Got non null response for key " + key, rtr);
		}
		assertEquals(testKeys.length, mapToCheck.size());
		
		checkKeysSortedMap(keyFactory, mapToCheck);
	}
	
	protected void checkKeysSortedMap(FixedSizeTextFactory keyFactory, SortedMap<Text,IntWritable> mapToCheck) throws Exception
	{
		SortedMap<Text,IntWritable> m1;
		Text testKey = keyFactory.newInstance();
		Text testKey2 = keyFactory.newInstance();
		
		//TAIL MAP
		//this key IS in the map
		testKey.set("z");
		m1 = mapToCheck.tailMap(testKey);
		assertEquals(2, m1.size());
		assertEquals("z", m1.firstKey().toString());
		assertEquals(testKeys[testKeys.length-1], m1.lastKey().toString());
		for(String t : testKeys)
		{
			String m;
			if (t.compareTo("z") < 0)
			{
				testKey2.set(t);
				assertFalse(m = "Did not expect to find key " + t + " in submap", 
						m1.containsKey(testKey2));
				assertNull(m, m1.get(testKey2));
			}
			else
			{		
				testKey2.set(t);
				assertTrue(m = "Expected to find key " +t + " in submap", 
						m1.containsKey(testKey2));
				assertNotNull(m, m1.get(testKey2));
			}
		}
		for(String t : testNotInKeys)
		{
			testKey2.set(t);
			String m;
			assertFalse(m = "Expected to find key " + t + " in submap", 
					m1.containsKey(testKey2));
			assertNull(m, m1.get(testKey2));
		}
		
		//this KEY is not in the map
		testKey.set("zz");
		m1 = mapToCheck.tailMap(testKey);
		assertEquals(1, m1.size());
		assertEquals(testKeys[testKeys.length-1], m1.firstKey().toString());
		assertEquals(testKeys[testKeys.length-1], m1.lastKey().toString());

		for(String t : testKeys)
		{
			String m;
			if (t.compareTo("zz") < 0)
			{
				testKey2.set(t);
				assertFalse(m = "Did not expect to find key " + t + " in submap", 
						m1.containsKey(testKey2));
				assertNull(m, m1.get(testKey2));
			}
			else
			{		
				testKey2.set(t);
				assertTrue(m = "Expected to find key " +t + " in submap", 
						m1.containsKey(testKey2));
				assertNotNull(m, m1.get(testKey2));
			}
		}
		for(String t: testNotInKeys)
		{
			testKey2.set(t);
			String m;
			assertFalse(m = "Did not expect to find key " + t + " in submap", 
					m1.containsKey(testKey2));
			assertNull(m, m1.get(testKey2));
		}
		
		//HEAD MAP
		//this key IS in the map
		testKey.set("z");
		m1 = mapToCheck.headMap(testKey);
		assertEquals(4, m1.size());
		assertEquals("00", m1.firstKey().toString());
		assertEquals(testKeys[4-1], m1.lastKey().toString());
		for(String t : testKeys)
		{
			String m;
			if (t.compareTo("z") < 0)
			{
				testKey2.set(t);
				assertTrue(m = "Expected to find key " +t + " in submap", 
						m1.containsKey(testKey2));
				assertNotNull(m, m1.get(testKey2));
			}
			else
			{
				testKey2.set(t);
				assertFalse(m = "Did not expect to find key " + t + " in submap", 
						m1.containsKey(testKey2));
				assertNull(m, m1.get(testKey2));
			}
		}
		for(String t : testNotInKeys)
		{
			testKey2.set(t);
			String m;
			assertFalse(m = "Expected to find key " + t + " in submap", 
					m1.containsKey(testKey2));
			assertNull(m, m1.get(testKey2));
		}
		
		//this KEY is not in the map
		testKey.set("zz");
		m1 = mapToCheck.headMap(testKey);
		assertEquals(5, m1.size());
		
		assertEquals(testKeys[0], m1.firstKey().toString());
		assertEquals(testKeys[4], m1.lastKey().toString());

		for(String t : testKeys)
		{
			String m;
			if (t.compareTo("zz") < 0)
			{
				testKey2.set(t);
				assertTrue(m = "Expected to find key " +t + " in submap", 
						m1.containsKey(testKey2));
				assertNotNull(m, m1.get(testKey2));
			}
			else
			{		
				testKey2.set(t);
				assertFalse(m = "Did not expect to find key " + t + " in submap", 
						m1.containsKey(testKey2));
				assertNull(m, m1.get(testKey2));
			}
		}
		for(String t: testNotInKeys)
		{
			testKey2.set(t);
			String m;
			assertFalse(m = "Did not expect to find key " + t + " in submap", 
					m1.containsKey(testKey2));
			assertNull(m, m1.get(testKey2));
		}
		
		
		//SUBMAP: 
		//first not in, last in 
		testKey.set("0");
		Text testKey3 = keyFactory.newInstance();
		testKey3.set("1");
		m1 = mapToCheck.subMap(testKey, testKey3);
		assertEquals(2, m1.size());
		
		for(String t : testKeys)
		{
			String m;
			if (t.compareTo("0") >= 0 && t.compareTo("1") < 0)
			{
				testKey2.set(t);
				assertTrue(m = "Expected to find key " +t + " in submap", 
						m1.containsKey(testKey2));
				assertNotNull(m, m1.get(testKey2));
			}
			else
			{		
				testKey2.set(t);
				assertFalse(m = "Did not expect to find key " + t + " in submap", 
						m1.containsKey(testKey2));
				assertNull(m, m1.get(testKey2));
			}
		}
		
		for(String t: testNotInKeys)
		{
			testKey2.set(t);
			String m;
			assertFalse(m = "Did not expect to find key " + t + " in submap", 
					m1.containsKey(testKey2));
			assertNull(m, m1.get(testKey2));
		}
		
		
		//first in, last in 
		testKey.set("a");
		testKey3 = keyFactory.newInstance();
		testKey3.set("z");
		m1 = mapToCheck.subMap(testKey, testKey3);
		assertEquals(1, m1.size());
		
		for(String t : testKeys)
		{
			String m;
			if (t.compareTo("a") >= 0 && t.compareTo("z") < 0)
			{
				testKey2.set(t);
				assertTrue(m = "Expected to find key " +t + " in submap", 
						m1.containsKey(testKey2));
				assertNotNull(m, m1.get(testKey2));
			}
			else
			{		
				testKey2.set(t);
				assertFalse(m = "Did not expect to find key " + t + " in submap", 
						m1.containsKey(testKey2));
				assertNull(m, m1.get(testKey2));
			}
		}
		
		for(String t: testNotInKeys)
		{
			testKey2.set(t);
			String m;
			assertFalse(m = "Did not expect to find key " + t + " in submap", 
					m1.containsKey(testKey2));
			assertNull(m, m1.get(testKey2));
		}
		
		Iterator<Entry<Text, IntWritable>> eiter;
		Iterator<Text> kiter;
		
		//check submap iterators
		//{"00", "0000", "1", "a", "z", new String(new byte[]{(byte)0xbb,(byte)0xB6,(byte)0xD3,(byte)0xAD}, "gb2312")}
		testKey.set("1");
		testKey3 = keyFactory.newInstance();
		testKey3.set("a");
		m1 = mapToCheck.subMap(testKey, testKey3);
		assertEquals(1, m1.size());
		assertEquals(1, m1.entrySet().size());
		assertEquals(1, m1.keySet().size());
		assertEquals(1, m1.values().size());
		
		kiter = m1.keySet().iterator();
		assertTrue(kiter.hasNext());
		assertEquals("1", kiter.next().toString());
		assertFalse(kiter.hasNext());
		
		
		eiter = m1.entrySet().iterator();
		assertTrue(eiter.hasNext());
		assertEquals("1", eiter.next().getKey().toString());
		assertFalse(eiter.hasNext());
		
		
		
		testKey.set("1");
		testKey3 = keyFactory.newInstance();
		testKey3.set("b");
		m1 = mapToCheck.subMap(testKey, testKey3);
		assertEquals(2, m1.size());
		assertEquals(2, m1.keySet().size());
		assertEquals(2, m1.values().size());
		assertEquals(2, m1.entrySet().size());
		
		kiter = m1.keySet().iterator();
		assertTrue(kiter.hasNext());
		assertEquals("1", kiter.next().toString());
		assertTrue(kiter.hasNext());
		assertEquals("a", kiter.next().toString());
		assertFalse(kiter.hasNext());
		
		eiter = m1.entrySet().iterator();
		assertTrue(eiter.hasNext());
		assertEquals("1", eiter.next().getKey().toString());
		assertTrue(eiter.hasNext());
		assertEquals("a", eiter.next().getKey().toString());
		assertFalse(eiter.hasNext());
		
		

	}
	
	protected void readStream(Iterator<Map.Entry<Text, IntWritable>> iterator) throws Exception
	{
		TObjectIntHashMap<String> copyKey2Id = key2id.clone();
		while(iterator.hasNext())
		{
			Map.Entry<Text, IntWritable> e = iterator.next();
			assertNotNull(e);
			assertNotNull(e.getKey());
			if (copyKey2Id.containsKey(e.getKey().toString()))
			{
				assertEquals(copyKey2Id.get(e.getKey().toString()), e.getValue().get());
				copyKey2Id.remove(e.getKey().toString());
			}				
		}
		assertTrue(copyKey2Id.size() == 0);
	}
	
	protected void readStreamSkip(Iterator<Map.Entry<Text, IntWritable>> iterator, int totalNumEntries) throws Exception
	{
		int skip = 3;
		int entryIndex = 0;
		((Skipable)iterator).skip(skip);
		entryIndex += skip;
		while(iterator.hasNext())
		{
			Map.Entry<Text, IntWritable> e = iterator.next();
			assertNotNull(e);
			assertNotNull(e.getKey());
			assertEquals(testKeys[entryIndex], e.getKey().toString());
			entryIndex++;
		}
		assertEquals(testKeys.length, entryIndex);
	}
	
	
	@Test public void testDupSuppres() throws Exception
	{
		String file = tf.newFile("testDupSuppres.fsomapfile").toString();
		FixedSizeTextFactory keyFactory = new FixedSizeTextFactory(20);
		FixedSizeWriteableFactory<IntWritable> valueFactory = new FixedSizeIntWritableFactory();
		MapFileWriter m = new FSOrderedMapFile.MultiFSOMapWriter(file, 2, keyFactory, valueFactory, true);
		Text k;
		IntWritable v;
		k = keyFactory.newInstance(); k.set("a");
		v = valueFactory.newInstance(); v.set(0);
		m.write(k, v);
		k = keyFactory.newInstance(); k.set("b");
		v = valueFactory.newInstance(); v.set(1);
		m.write(k, v);
		((Flushable)m).flush();
		k = keyFactory.newInstance(); k.set("a");
		v = valueFactory.newInstance(); v.set(2);
		m.write(k, v);
		k = keyFactory.newInstance(); k.set("c");
		v = valueFactory.newInstance(); v.set(3);
		m.write(k, v);
		m.close();
		
		FSOrderedMapFile<Text, IntWritable> map = new FSOrderedMapFile<Text, IntWritable>(file,false, keyFactory, valueFactory);
		assertEquals(3, map.size());
		k = keyFactory.newInstance(); k.set("a");
		assertEquals(0, map.get(k).get());
		
		k = keyFactory.newInstance(); k.set("b");
		assertEquals(1, map.get(k).get());
		
		k = keyFactory.newInstance(); k.set("c");
		assertEquals(3, map.get(k).get());
		map.close();
		
	}
	
	@Test public void testStream() throws Exception
	{
		FixedSizeTextFactory keyFactory = new FixedSizeTextFactory(20);
		FSOrderedMapFile.EntryIterator<Text, IntWritable> inputStream = new FSOrderedMapFile.EntryIterator<Text, IntWritable>(
				file, keyFactory, new FixedSizeIntWritableFactory());
		readStream(inputStream);
		inputStream.close();
	}
	
	@Test public void testStreamSkip() throws Exception
	{
		FixedSizeTextFactory keyFactory = new FixedSizeTextFactory(20);
		FSOrderedMapFile.EntryIterator<Text, IntWritable> inputStream = new FSOrderedMapFile.EntryIterator<Text, IntWritable>(
				file, keyFactory, new FixedSizeIntWritableFactory());
		readStreamSkip(inputStream, testKeys.length);
		inputStream.close();
	}
	
	@Test public void testOnDisk() throws Exception
	{
		FixedSizeTextFactory keyFactory = new FixedSizeTextFactory(20);
		FSOrderedMapFile<Text, IntWritable> mapfile = new FSOrderedMapFile<Text, IntWritable>(file, false, keyFactory, new FixedSizeIntWritableFactory());
		checkKeysGetEntry(keyFactory, mapfile);
		checkKeys(keyFactory, mapfile);
	}
	
	@Test public void testInMemory() throws Exception
	{
		FixedSizeTextFactory keyFactory = new FixedSizeTextFactory(20);
		SortedMap<Text, IntWritable> mapfileInMem = new FSOrderedMapFile.MapFileInMemory<Text, IntWritable>(file, keyFactory, new FixedSizeIntWritableFactory());
		checkKeys(keyFactory, mapfileInMem);
	}
	
	@Test public void testInMemoryJDKCollection() throws Exception
	{
		FixedSizeTextFactory keyFactory = new FixedSizeTextFactory(20);
		SortedMap<Text, IntWritable> map = new TreeMap<Text,IntWritable>();
		int offset =0;
		for(String key : testKeys)
		{
			Text wkey = keyFactory.newInstance();
			IntWritable wvalue = new IntWritable();
			wkey.set(key);
			wvalue.set(offset);

			map.put(wkey, wvalue);
			offset++;
		}
		checkKeys(keyFactory, map);
	}
	
	@Test public void testInMemoryRandomDataInputMemory() throws Exception
	{
		FixedSizeTextFactory keyFactory = new FixedSizeTextFactory(20);
		FSOrderedMapFile<Text, IntWritable> mapfileInMem = new FSOrderedMapFile<Text, IntWritable>(
				new RandomDataInputMemory(file), file,
				keyFactory, new FixedSizeIntWritableFactory());
		checkKeysGetEntry(keyFactory, mapfileInMem);
		checkKeys(keyFactory, mapfileInMem);
	}
	
	@After
	public void tearDown() throws Exception {
		if (! new File(file).delete())
			System.err.println("Could not delete file " + file);
	}
}
