/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.uk
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
 * The Original Code is TestCountingInputStream.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.utility.io;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
/** Test CountingInputStream behaves as expected.
 * @since 3.0
 * @author Craig Macdonald */
public class TestCountingInputStream {

	static InputStream NULL_INPUTSTREAM = new InputStream()
	{
		@Override
		public int read() throws IOException {
			return 0;
		}	
	};
	
	static byte[] TEST_ARRAY = new byte[]{0,1,2,3,4,5,6,7};
	
	/** Test that constructor offsets work */
	@Test public void testConstructor() throws Exception
	{
		CountingInputStream cis;
		cis = new CountingInputStream(NULL_INPUTSTREAM, 5l);
		assertEquals(5l, cis.getPos());
		cis.close();
		cis = new CountingInputStream(NULL_INPUTSTREAM, 500000000000l);
		assertEquals(500000000000l, cis.getPos());
		cis.close();
	}
	
	/** Test that single byte reads have the desired effect on counting */
	@Test public void testSingleByteReads() throws Exception
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(TEST_ARRAY);
		CountingInputStream cis = new CountingInputStream(bais);
		assertEquals((long)0, cis.getPos());
		for(long i=0;i < TEST_ARRAY.length;i++)
		{
			assertEquals(i, cis.getPos());
			int b = cis.read();
			assertEquals((byte)b, TEST_ARRAY[(int)i]);
			assertEquals(i+1, cis.getPos());
		}
		assertEquals(TEST_ARRAY.length, cis.getPos());
		assertEquals(-1, cis.read());
		assertEquals(TEST_ARRAY.length, cis.getPos());
		cis.close();
	}
	
	/** Test that two byte reads have the desired effect on counting */
	@Test public void testTwoByteReads() throws Exception
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(TEST_ARRAY);
		CountingInputStream cis = new CountingInputStream(bais);
		assertEquals((long)0, cis.getPos());
		for(long i=0;i < TEST_ARRAY.length / 2;i++)
		{
			assertEquals(i*2, cis.getPos());
			final byte[] read = new byte[2];
			final int rtr = cis.read(read);
			assertEquals(rtr, read.length);
			assertEquals(read[0], TEST_ARRAY[(int)i*2]);
			assertEquals(read[1], TEST_ARRAY[(int)(i*2)+1]);
			assertEquals("offset after read i="+i+"; ", (i*2)+read.length, cis.getPos());
		}
		assertEquals(TEST_ARRAY.length, cis.getPos());
		assertEquals(-1, cis.read());
		assertEquals(TEST_ARRAY.length, cis.getPos());
		cis.close();
	}
	
	/** Test that reads using array offsets work as expected */
	@Test public void testOffsetByteReads() throws Exception
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(TEST_ARRAY);
		CountingInputStream cis = new CountingInputStream(bais);
		assertEquals((long)0, cis.getPos());
		for(long i=0;i < TEST_ARRAY.length / 2;i++)
		{
			assertEquals(i*2, cis.getPos());
			final byte[] read = new byte[2];
			final int rtr = cis.read(read, 0, read.length);
			assertEquals(rtr, read.length);
			assertEquals(read[0], TEST_ARRAY[(int)i*2]);
			assertEquals(read[1], TEST_ARRAY[(int)(i*2)+1]);
			assertEquals("offset after read i="+i+"; ", (i*2)+read.length, cis.getPos());
		}
		assertEquals(TEST_ARRAY.length, cis.getPos());
		assertEquals(-1, cis.read());
		assertEquals(TEST_ARRAY.length, cis.getPos());
		cis.close();
	}
	
}
