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
 * The Original Code is BitFileInMemory.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk
 */

package org.terrier.compression.bit;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.utility.Files;

/** Class which enables a bit compressed file to be read wholly into memory
 * and accessed from there with lot latency. This class will fail if the file
 * is larger than Integer.MAX_VALUE. Use BitFileInMemoryLarge instead.
 * @author Craig Macdonald
 */
public class BitFileInMemory implements BitInSeekable
{
	/** The logger used */
    protected static final Logger logger = LoggerFactory.getLogger(BitFileInMemory.class);
	/** buffer in memory for the entire compressed file */
	protected final byte[] inBuffer;
	/** Loads the entire file represented by filename into memory 
	 * @param filename Location of bit file to read into memory
	 * @throws IOException if anything goes wrong reading the file */
	public BitFileInMemory(String filename) throws IOException {
		this(Files.openFileStream(filename), Files.length(filename));
	}
	
	/** Loads the entire specified inputstream into memory. Length is assumed to be
	  * as specified.
	  * @param is InputStream containing the compressed bitfile 
	  * @param length Expected length of the input stream
	  * @throws IOException if anything goes wrong reading the inputstream */
	public BitFileInMemory(final InputStream is, final long length) throws IOException
	{
		if (length > Integer.MAX_VALUE)
		{
			logger.error("File too big for BitFileInMemory");
			inBuffer = new byte[0];
			return;
		}
		inBuffer = new byte[(int)length];
		final DataInputStream dis = new DataInputStream(is);
		dis.readFully(inBuffer);
		dis.close();
	}

	/** Create an object using the specified data as the compressed data */
	public BitFileInMemory(final byte[] buffer)
	{
		inBuffer = buffer;
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
	 *  @param endBitOffset the bit offset in the ending byte.
	 *        This bit is the last bit of this entry.
	 * @return Returns the BitIn object to use to read that data
	 */
	public BitIn readReset(long startByteOffset, byte startBitOffset, long endByteOffset, byte endBitOffset) 
	{
		return new BitInReader2(startByteOffset, startBitOffset, endByteOffset, endBitOffset);
	}
	/** 
	 * {@inheritDoc} 
	 */
	public BitIn readReset(long startByteOffset, byte startBitOffset) 
	{
		return new BitInReader2(startByteOffset, startBitOffset);
	}

	/** Close this object. Does nothing. */
	public void close()
	{
		//do nothing
	}
	
	final class BitInReader2 extends BitInBase
	{

		public BitInReader2(long startByteOffset, byte startBitOffset)
		{
			offset = (int)startByteOffset;
			bitOffset = startBitOffset;
			byteRead = inBuffer[(int)offset];
		}
		
		public BitInReader2(long startByteOffset, byte startBitOffset, long endByteOffset, byte endBitOffset)
		{
			this(startByteOffset, startBitOffset);
		}
		
		@Override
		public void skipBytes(long len) throws IOException {
			offset += len;
			bitOffset = 0;
			byteRead = inBuffer[(int)offset];
		}

		@Override
		protected void incrByte() throws IOException {
			offset++;
			byteRead = inBuffer[(int)offset];
		}

		@Override
		protected void incrByte(int i) throws IOException {
			offset += i;
			byteRead = inBuffer[(int)offset];
		}

		/** Nothing to do */
		@Override
		public void close() throws IOException {}
	}
}
