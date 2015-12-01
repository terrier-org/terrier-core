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
 * The Original Code is TestBitIndexPointer.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

import org.terrier.structures.seralization.FixedSizeWriteableFactory;
/** Tests that various BitIndexPointer implementation behave.
 * In particular, the coding of fileId and bit offset into 8 bits
 * requires some bitwise-operations, and hence requires careful
 * testing with normal and extreme values. It also tests that 
 * they use the correct number of bytes for their FixedSizeWritable
 * implementations.
 * <p>
 * The range of byte offsets, bit offsets and number of entries
 * fields tested are defined by static fields TEST_OFFSETS, 
 * TEST_BITS and TEST_ENTRIES, respectively. Upper (inclusive)
 * bound of fileId fields is determined by BitIndexPointer.MAX_FILE_ID
 * <p>
 * <b>Tested BitIndexPointer implementations:</b>
 * <ul>
 * <li>SimpleBitIndexPointer</li>
 * <li>BasicLexiconEntry</li>
 * <li>FieldLexiconEntry</li>
 * <li>BlockLexiconEntry</li>
 * <li>BlockFieldLexiconEntry</li>
 * <li>BasicDocumentIndexEntry</li>
 * <li>FieldDocumentIndexEntry</li>
 * </ul>
 * 
 * @author Craig Macdonald
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class TestBitIndexPointer {
	
	/** A selection of valid byte offsets, including lower and upper bound extremes */
	final static long[] TEST_OFFSETS = new long[]{
		0, //extreme
		1,
		2,
		4,
		100,
		1000,
		1024,
		Integer.MAX_VALUE, //check that an int isnt being used
		((long)Integer.MAX_VALUE) + 1l, //check that an int isnt being used
		((long)Integer.MAX_VALUE) + 1024l,
		Long.MAX_VALUE //extreme
	};
	
	/** All valid bit offsets */
	final static byte[] TEST_BITS = new byte[]{
		0,1,2,3,4,5,6,7
	};
	
	/** List of some valid test entry counts */
	final static int[] TEST_ENTRIES = new int[]{
		0, 1, 2, 3, 4, 10, 1024, 2048, Integer.MAX_VALUE
	};
	
	protected static void viaWritable(final BitIndexPointer p1, final BitIndexPointer p2, final int SIZE) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		p1.write(dos);
		assertEquals(SIZE, baos.size());
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		p2.readFields(dis);
	}

	protected void test(FixedSizeWriteableFactory<? extends BitIndexPointer> factory) throws Exception
	{
		final int SIZE = factory.getSize();
		//test on p1
		BitIndexPointer p1 = factory.newInstance();
		//check serialization via p2
		BitIndexPointer p2 = factory.newInstance();
	
		//entries and offsets are each independent of all other fields
		for(int entries : TEST_ENTRIES)
		{
			p1.setNumberOfEntries(entries);
			assertEquals(entries, p1.getNumberOfEntries());
			viaWritable(p1,p2, SIZE);
			assertEquals(entries, p2.getNumberOfEntries());
		}
		
		for (long offset : TEST_OFFSETS)
		{
			p1.setOffset(offset, (byte)0);
			assertEquals(offset, p1.getOffset());
			viaWritable(p1,p2, SIZE);
			assertEquals(offset, p2.getOffset());
		}
		
		//file id and bits are ususally combined in implementations, so test
		//in a dependent manner
		for (byte fileId=0; fileId <= BitIndexPointer.MAX_FILE_ID; fileId++)
		{
			for (byte bits: TEST_BITS)
			{
				//first check: set offset first
				p1.setOffset(0l, bits);
				p1.setFileNumber(fileId);
				assertEquals("desired bits="+bits+" file="+fileId, bits, p1.getOffsetBits());
				assertEquals("desired bits="+bits+" file="+fileId, fileId, p1.getFileNumber());
				
				//2nd check: set file id first
				p1.setFileNumber(fileId);
				p1.setOffset(0l, bits);
				assertEquals("desired bits="+bits+" file="+fileId, bits, p1.getOffsetBits());
				assertEquals("desired bits="+bits+" file="+fileId, fileId, p1.getFileNumber());
				
				//3rd check: via writable serialization
				viaWritable(p1, p2, SIZE);
				
				assertEquals("desired bits="+bits+" file="+fileId, bits, p2.getOffsetBits());
				assertEquals("desired bits="+bits+" file="+fileId, fileId, p2.getFileNumber());
				
			}
		}
		
	}
	
	@Test
	public void testSimpleBIPointer() throws Exception
	{
		test(new SimpleBitIndexPointer.Factory());
	}
	
	
	
	@Test
	public void testBasicLE() throws Exception
	{
		test((FixedSizeWriteableFactory<? extends BitIndexPointer>)(FixedSizeWriteableFactory)new BasicLexiconEntry.Factory());
	}
	
	@Test
	public void testFieldLE() throws Exception
	{
		test((FixedSizeWriteableFactory<? extends BitIndexPointer>)(FixedSizeWriteableFactory)new FieldLexiconEntry.Factory(3));
	}

	@Test
	public void testBlockFieldLE() throws Exception
	{
 
		test((FixedSizeWriteableFactory<? extends BitIndexPointer>)(FixedSizeWriteableFactory)new BlockFieldLexiconEntry.Factory(3));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testBlockLE() throws Exception
	{
		test((FixedSizeWriteableFactory<? extends BitIndexPointer>)(FixedSizeWriteableFactory)new BlockLexiconEntry.Factory());
	}
	
	
	@Test
	public void testBasicDE() throws Exception
	{
		test((FixedSizeWriteableFactory<? extends BitIndexPointer>)(FixedSizeWriteableFactory)new BasicDocumentIndexEntry.Factory());
	}
	
	@Test
	public void testFieldDE() throws Exception
	{
		test((FixedSizeWriteableFactory<? extends BitIndexPointer>)(FixedSizeWriteableFactory)new FieldDocumentIndexEntry.Factory(3));
	}
	
}
