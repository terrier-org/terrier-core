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
 * The Original Code is TestByteInByteOut.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */

package org.terrier.integer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.terrier.compression.integer.ByteFileBuffered;
import org.terrier.compression.integer.ByteFileInMemory;
import org.terrier.compression.integer.ByteIn;
import org.terrier.compression.integer.ByteInputStream;
import org.terrier.compression.integer.ByteOutputStream;
import org.terrier.utility.io.RandomDataInputMemory;
//import org.junit.Assert.*;

public class TestByteInByteOut {
	
	//private ByteArrayOutputStream baos;
	private byte[] arr = {1, 2, 3, -4, 0, 127};
	private String tmpFilename;

	public TestByteInByteOut() throws IOException {
				
		File tmpFile = File.createTempFile("tmp", ".txt");
		tmpFilename = tmpFile.toString();
		ByteOutputStream out = new ByteOutputStream(tmpFilename);
		out.writeVInt(18348);
		out.writeVInt(1000);
		out.writeVInt(-1);
		out.writeVInt(80);
		out.writeVInt(3000);
		out.writeVInt(-1000);
		out.write(arr, 0, arr.length);
		out.write(arr, 0, arr.length);
		out.writeVInt(314);
		
		out.close();		
		
		
	}

	@Test
	public void test0() throws IOException {
		
		//test various output/input method
				
		//RandomDataInputMemory inData = new RandomDataInputMemory(baos.toByteArray());
		
		ByteIn in = (new ByteFileBuffered(tmpFilename)).readReset(0);
		assertEquals(18348, in.readVInt());
		assertEquals(1000, in.readVInt());
		assertEquals(-1, in.readVInt());
		assertEquals(80, in.readVInt());
		assertEquals(3000, in.readVInt());
		assertEquals(-1000, in.readVInt());
		byte[] arr2 = new byte[6];
		in.readFully(arr2, 0, 6);
		assertArrayEquals(arr, arr2);
		in.skipBytes(6);
		assertEquals(314, in.readVInt());
		in.close();
	}
	
	@Test
	public void test1() throws IOException {
		
		//test various output/input method
								
		RandomDataInputMemory inData = new RandomDataInputMemory(tmpFilename);
		ByteIn in = (new ByteFileInMemory(inData)).readReset(0);
		assertEquals(18348, in.readVInt());
		assertEquals(1000, in.readVInt());
		assertEquals(-1, in.readVInt());
		assertEquals(80, in.readVInt());
		assertEquals(3000, in.readVInt());
		assertEquals(-1000, in.readVInt());
		byte[] arr2 = new byte[6];
		in.readFully(arr2, 0, 6);
		assertArrayEquals(arr, arr2);
		in.skipBytes(6);
		assertEquals(314, in.readVInt());		
		in.close();
	}	
	
//	@Test
//	public void test2() throws IOException {
//		
//		//test reading from the middle of the stream
//		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		
//		ByteOutputStream out = new ByteOutputStream(baos);
//		int[] arr = {5, 2, 3, 4, 5};
//		out.writeInt(arr, arr.length);
//		out.close();
//		
//		RandomDataInputMemory inData = new RandomDataInputMemory(baos.toByteArray());
//		ByteIn in = (new CopyOfByteFileBuffered(tmpFilename)).readReset(4);
//		assertEquals(2, in.readInt());
//		in.skipBytes(4);
//		assertEquals(4, in.readInt());
//		in.close();
//	}
	
	@Test
	public void test3() throws IOException {
		
		//test reading from the middle of the stream
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		ByteOutputStream out = new ByteOutputStream(baos);
		byte[] arr = {1, 2, 3, 4, 5};
		out.write(arr, 0, arr.length);
		out.writeVInt(314);
		out.write(arr, 0, arr.length);
		out.close();
		
		byte[][] bytes = new byte[1][];
		bytes[0] = baos.toByteArray();
		RandomDataInputMemory inData = new RandomDataInputMemory(baos.toByteArray());
		ByteIn in = (new ByteFileInMemory(inData)).readReset(4);
		in.skipBytes(1);
		assertEquals(314, in.readVInt());		
		in.close();
	}	
	
	@Test
	public void test4() throws IOException {
		
		//test reading from the middle of the stream
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		ByteOutputStream out = new ByteOutputStream(baos);
		byte[] arr = {1, 2, 3, 4, 5};
		out.write(arr, 0, arr.length);
		out.writeVInt(314);
		out.write(arr, 0, arr.length);
		out.close();
		
		ByteIn in = new ByteInputStream(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
		in.skipBytes(5);
		assertEquals(314, in.readVInt());		
		in.close();
	}	
}
