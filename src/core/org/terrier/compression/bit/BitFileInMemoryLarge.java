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
 * The Original Code is BitFileInMemoryLarge.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.compression.bit;

import java.io.IOException;

import org.terrier.utility.io.RandomDataInput;
import org.terrier.utility.io.RandomDataInputMemory;
import org.terrier.utility.io.WrappedIOException;

/** 
 * Allows access to bit compressed files that are loaded entirely into memory.
 * Implements a BitInSeekable that uses RandomDataInputMemory as a backing store.
 * Can handle data files larger than those handled by BitFileInMemory (whose limit is Integer.MAX_VALUE).
 * @author Craig Macdonald
 * @since 3.0
 */
public class BitFileInMemoryLarge implements BitInSeekable {
	
	RandomDataInputMemory rdim;
	/**
	 * constructor
	 * @param _rdim
	 * @throws IOException
	 */
	public BitFileInMemoryLarge(RandomDataInputMemory _rdim) throws IOException
	{
		rdim = _rdim;
	}

	/**
	 * load compressed file into memory
	 * @param filename
	 * @throws IOException
	 */
	public BitFileInMemoryLarge(String filename) throws IOException
	{
		this(new RandomDataInputMemory(filename));	
	}
	
	/**
	 * Reads from the file a specific number of bytes and after this
	 * call, a sequence of read calls may follow. The offsets given 
	 * as arguments are inclusive. For example, if we call this method
	 * with arguments 0, 2, 1, 7, it will read in a buffer the contents 
	 * of the underlying file from the third bit of the first byte to the 
	 * last bit of the second byte.
	 * @param startByteOffset the starting byte to read from
	 * @param startBitOffset the bit offset in the starting byte
	 * @param endByteOffset the ending byte 
	 * @param endBitOffset the bit offset in the ending byte. 
	 *        This bit is the last bit of this entry.
	 * @return Returns the BitIn object to use to read that data
	 */	
	public BitIn readReset(long startByteOffset, byte startBitOffset, long endByteOffset, byte endBitOffset) throws IOException
	{
		return readReset(startByteOffset, startBitOffset);
	}
	
	
	/**
	 * Reads from the file from a specific offset. After this
	 * call, a sequence of read calls may follow.
	 * @param startByteOffset the starting byte to read from
	 * @param startBitOffset the bit offset in the starting byte
	 */
	public BitIn readReset(long startByteOffset, byte startBitOffset) throws IOException
	{
		try{
			RandomDataInput rdi = (RandomDataInput)rdim.clone();
			rdi.seek(startByteOffset);
			BitIn in = new BitInputStream(rdi);
			in.skipBits(startBitOffset);
			return in;
		} catch (CloneNotSupportedException e) {
			throw new WrappedIOException(e);
		}
	}
	/** 
	 * {@inheritDoc} 
	 */	
	public void close() throws IOException {
		rdim.close();
	}

}
