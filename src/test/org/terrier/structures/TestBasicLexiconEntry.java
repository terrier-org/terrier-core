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
 * The Original Code is TestBasicLexiconEntry.java
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.hadoop.io.Writable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.terrier.structures.BasicLexiconEntry.Factory;
/** Tests that BasicLexiconEntry behaves as expected */
public class TestBasicLexiconEntry {
	
	@Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();

	
	@Test public void testBasic() throws Exception
	{
		BasicLexiconEntry le = new BasicLexiconEntry();
		//term id
		le.setTermId(1);
		assertEquals(le.getTermId(), 1);
		//position
		le.setOffset(0, (byte)0);
		assertEquals(le.getOffset(), 0);
		assertEquals(le.getOffsetBits(), (byte)0);
		
		le = new BasicLexiconEntry(2, 2, 5);
		assertEquals(le.getDocumentFrequency(), 2);
		assertEquals(le.getFrequency(), 5);
		le.add(new BasicLexiconEntry(0,1,10));
		assertEquals(le.getDocumentFrequency(), 3);
		assertEquals(le.getFrequency(), 15);
	}
	
	@Test public void testWritable() throws Exception
	{
		Factory f = new Factory();
		BasicLexiconEntry le = (BasicLexiconEntry)f.newInstance();
		le.setTermId(100);
		le.setOffset(10, (byte)11);
		byte[] b = getBytes(le);
		System.err.println("le written in "+b.length+" bytes");
		assertEquals(b.length, f.getSize());
		BasicLexiconEntry leReader = (BasicLexiconEntry)	f.newInstance();
		populateEntry(leReader, b);
		assertEquals(le.getTermId(), leReader.getTermId());
		assertEquals(le.getFrequency(), leReader.getFrequency());
		assertEquals(le.getDocumentFrequency(), leReader.getDocumentFrequency());
		assertEquals(le.getOffset(), leReader.getOffset());
		assertEquals(le.getOffsetBits(), leReader.getOffsetBits());
	}
	
	@Test public void testWritableFile() throws Exception
	{
		Factory f = new Factory();
		BasicLexiconEntry le = (BasicLexiconEntry)f.newInstance();
		le.setTermId(100);
		le.setOffset(10, (byte)11);
		File tmpFile = tmpfolder.newFile("test.writable");
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(tmpFile));
		le.write(dos);
		dos.close();
		BasicLexiconEntry leReader = (BasicLexiconEntry)f.newInstance();
		leReader.readFields(new DataInputStream(new FileInputStream(tmpFile)));
		assertEquals(le.getTermId(), leReader.getTermId());
		assertEquals(le.getFrequency(), leReader.getFrequency());
		assertEquals(le.getDocumentFrequency(), leReader.getDocumentFrequency());
		assertEquals(le.getOffset(), leReader.getOffset());
		assertEquals(le.getOffsetBits(), leReader.getOffsetBits());
	}
	
	static void populateEntry(LexiconEntry le, byte[] b) throws Exception
	{
		le.readFields(new DataInputStream(new ByteArrayInputStream(b)));
	}
	
    static byte[] getBytes(Writable w) throws Exception
	{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(buffer);
		w.write(dos);
		return buffer.toByteArray();
	}
}
