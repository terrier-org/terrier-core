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
 * The Original Code is TestFSArrayFile.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.collections;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.io.IntWritable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Skipable;
import org.terrier.structures.seralization.FixedSizeIntWritableFactory;

/** Tests FSArrayFile works as expected
 * @since 3.0
 * @author Craig Macdonald
 */
public class TestFSArrayFile {

	final static int[] TEST_INTEGERS = new int[]{0,1000000,5,10};
	
	String arrayFile;
	
	@Rule public TemporaryFolder tmp = new TemporaryFolder();
	@Before public void setup() throws Exception
	{
		arrayFile = tmp.newFile("tmpFile"+ FSArrayFile.USUAL_EXTENSION).toString();
		FSArrayFile.ArrayFileWriter writer = FSArrayFile.writeFSArrayFile(arrayFile);
		for(int i : TEST_INTEGERS)
		{
			writer.write(new IntWritable(i));
		}
		writer.close();
	}
	
	protected void testRandom(List<IntWritable> list) throws Exception
	{
		assertEquals(TEST_INTEGERS.length, list.size());
		for(int i=0;i<TEST_INTEGERS.length;i++)
		{
			assertEquals(TEST_INTEGERS[i], list.get(i).get());
		}
		for(int i=TEST_INTEGERS.length-1; i>=0;i--)
		{
			assertEquals(TEST_INTEGERS[i], list.get(i).get());
		}
		IndexUtil.close(list);
	}
	
	/** Test that random access on one on disk works as expected */
	@Test public void testRandom() throws Exception
	{
		List<IntWritable> list = new FSArrayFile<IntWritable>(arrayFile, false, new FixedSizeIntWritableFactory());
		testRandom(list);
	}
	
	/** Test that random access on one in memory works as expected */
	@Test public void testRandomInMem() throws Exception
	{
		List<IntWritable> list = new FSArrayFileInMem<IntWritable>(arrayFile, false, new FixedSizeIntWritableFactory());
		testRandom(list);
	}
	
	/** Test that the stream works as expected */
	@Test public void testStream() throws Exception
	{
		Iterator<IntWritable> iterator = new FSArrayFile.ArrayFileIterator<IntWritable>(arrayFile, new FixedSizeIntWritableFactory());
		int i=0;
		while(iterator.hasNext())
		{
			assertEquals(TEST_INTEGERS[i], iterator.next().get());
			i++;
		}
		assertEquals(TEST_INTEGERS.length, i);
		IndexUtil.close(iterator);
	}
	
	/** Test that skipping in a stream works as expected */
	@Test public void testStreamSkipping() throws Exception
	{		
		for(int i=0;i<TEST_INTEGERS.length;i++)
		{
			Iterator<IntWritable> iterator = new FSArrayFile.ArrayFileIterator<IntWritable>(arrayFile, new FixedSizeIntWritableFactory());
			((Skipable)iterator).skip(i);
			int j=i;
			while(iterator.hasNext())
			{
				assertEquals(TEST_INTEGERS[j], iterator.next().get());
				j++;
			}
			assertEquals(TEST_INTEGERS.length, j);
			IndexUtil.close(iterator);
		}
		
	}
	
}
