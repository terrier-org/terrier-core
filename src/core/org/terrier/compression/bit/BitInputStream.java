
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
 * The Original Code is BitInputStream.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Roi Blanco
 */

package org.terrier.compression.bit;


import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.terrier.structures.IndexUtil;
import org.terrier.utility.Files;
/**
 * This class reads from a file or an InputStream integers that can be coded with different encoding algorithms.
 * It does not use any internal buffering, and operates with bytes.
 * @author Roi Blanco
 *
 */
public class BitInputStream extends BitInBase {
	
	/** The private input stream used internaly.*/
	protected DataInput dis = null;
	/** 
	 * A byte read from the stream. This byte should be 
	 * initialised during the construction of the class.
	 */
	//protected byte byteRead;


	/** Do nothing constructor used by child classes which override all methods, eg OldBitInputStream */
	protected BitInputStream(){} 

	/**
	 * Constructs an instance of the class for a given stream
	 * @param in java.io.DataInput the underlying input stream
	 * @throws java.io.IOException if an I/O error occurs
	 */
	public BitInputStream(DataInput in) throws IOException {
		dis = in;
		offset = 0;
		bitOffset = 0;
		byteRead = dis.readByte();
	}
	
	/**
	 * Constructs an instance of the class for a given stream
	 * @param is java.io.InputStream the underlying input stream
	 * @throws java.io.IOException if an I/O error occurs
	 */
	public BitInputStream(InputStream is) throws IOException {
		dis = new DataInputStream(is);
		offset = 0;
		bitOffset = 0;
		byteRead = dis.readByte();
	}
	/** 
	 * Constructs an instance of the class for a given filename
	 * @param filename java.lang.String the name of the undelying file
	 * @throws java.io.IOException if an I/O error occurs
	 */
	public BitInputStream(String filename) throws IOException {
		dis = new DataInputStream(Files.openFileStream(filename));
		offset = 0;
		bitOffset = 0;
		byteRead = dis.readByte();
	}
	/**
	 * Constructs an instance of the class for a given file
	 * @param file java.io.File the underlying file
	 * @throws java.io.IOException if an I/O error occurs
	 */
	public BitInputStream(File file) throws IOException {
		dis = new DataInputStream(Files.openFileStream(file));
		offset = 0;
		bitOffset = 0;
		byteRead = dis.readByte();
	}
	/** 
	 * Closes the stream.
	 * @throws java.io.IOException if an I/O error occurs
	 */
	public void close() throws IOException {
		IndexUtil.close(dis);
	}
	
	/** {@inheritDoc} */
	public void skipBytes(long len) throws IOException {
		
		if (len == 0)
		{
			bitOffset = 0;
			return;
		}
		
		len--;
		
		long skipped = 0;
		do{
			int toSkip = (int) Math.min((long)Integer.MAX_VALUE, len - skipped);
			skipped += dis.skipBytes(toSkip);
			//System.err.println("skipped "+ skipped + " bytes out of desired "+ len);
		} while(skipped < len);
		offset += skipped +1;
		bitOffset = 0;
		byteRead = dis.readByte();
	}
	
	@Override
	protected void incrByte() throws IOException {
		offset++;
		byteRead = dis.readByte();		
	}

	@Override
	protected void incrByte(int i) throws IOException {
		offset+=i;
		dis.skipBytes(i);
	}
}
