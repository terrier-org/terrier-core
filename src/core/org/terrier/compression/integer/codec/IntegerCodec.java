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
 * The Original Code is IntegerCodec.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 *   
 */

package org.terrier.compression.integer.codec;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.terrier.compression.integer.ByteIn;
import org.terrier.compression.integer.ByteOut;

/**
 * Abstract class representing an integer codec
 * 
 * @author Matteo Catena
 * @since 4.0
 */
public abstract class IntegerCodec {
	
	private static final int DEFAULT_SIZE = 1024;
	
	protected ByteBuffer inBuffer;
	protected ByteBuffer outBuffer;
	protected int[] supportArray;
		
	public IntegerCodec() {
		
		inBuffer = ByteBuffer.wrap(new byte[DEFAULT_SIZE]);
		outBuffer = ByteBuffer.wrap(new byte[DEFAULT_SIZE]);
	}	

	/**
	 * Compress and write down an integer array
	 * 
	 * @param in the array to compress
	 * @param len number of in's elements to compress
	 * @param out the output channel
	 * @throws IOException 
	 */
	public abstract void compress(int[] in, int len, ByteOut out) throws IOException;
	
	
	/**
	 * Read and decompress an integer array
	 * @param in the input channel
	 * @param out the decompressed array
	 * @param num expected number of decompressed elements
	 * @throws IOException 
	 */
	public abstract void decompress(ByteIn in, int out[], int num) throws IOException;
	
	/**
	 * avoid to decompress the next block (the size of the block is written 
	 * on the input stream)
	 * @param in the input channel
	 * @throws IOException
	 */
	public abstract void skip(ByteIn in) throws IOException;
	
	/** returns a textual description of the codec */
	@Override
	public String toString()
	{
		return this.getClass().getSimpleName();
	}
}
