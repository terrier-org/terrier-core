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
 * The Original Code is TestRandomDataInputMemory.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.utility.io;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;
/** Test that RandomDataInputMemory works as expected */
public class TestRandomDataInputMemory extends TestCase {
	byte[] data = new byte[]{0,1,2,3,4,5,6,127,-127};
	
	@Test public void testSingle()  throws IOException
	{
		System.err.println("testSingle");
		RandomDataInput rdi = new RandomDataInputMemory(data);
		doTest(rdi);			
	}
	
	@Test public void testStream()  throws IOException
	{
		System.err.println("testStream");
		RandomDataInput rdi = new RandomDataInputMemory(new DataInputStream(new ByteArrayInputStream(data)), (long)data.length);
		doTest(rdi);
	}
	
	@Test public void testStreamMulti()  throws IOException
	{
		System.err.println("testStreamMulti");
		final int old = RandomDataInputMemory.MAX_INDIVIDUAL_BUFFER_SIZE;
		
		for(int l : new int[]{2,3,4,5,6})
		{
			RandomDataInputMemory.MAX_INDIVIDUAL_BUFFER_SIZE = l;
			RandomDataInput rdi = new RandomDataInputMemory(new DataInputStream(new ByteArrayInputStream(data)), (long)data.length);
			doTest(rdi);
		}
		RandomDataInputMemory.MAX_INDIVIDUAL_BUFFER_SIZE = old;
	}
	
	protected void doTest(RandomDataInput rdi) throws IOException
	{
		//System.err.println("As stream");
		for(byte b : data)
		{
			byte got = rdi.readByte();
			System.err.println("Got=" + got + " expected " + b);
			assertEquals(b, got);
		}
		rdi.seek(0);
		for(byte b : data)
		{
			byte got = rdi.readByte();
			//System.err.println("Got=" + got + " expected " + b);
			assertEquals(b, got);
		}
		for(int i=0;i<data.length;i++)	
		{
			rdi.seek(i);
			byte got = rdi.readByte();
			//System.err.println("Got=" + got + " expected " + data[i]);
			assertEquals(data[i], got);
		}
	}
}
