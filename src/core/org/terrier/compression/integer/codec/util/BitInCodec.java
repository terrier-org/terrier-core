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
 * The Original Code is BitInCodec.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 *   
 */

package org.terrier.compression.integer.codec.util;

import java.io.IOException;

import org.terrier.compression.bit.BitInBase;
import org.terrier.compression.integer.ByteIn;
import org.terrier.utility.ArrayUtils;

/**
 * Utility class, used to implement bitwise IntegerCodec implementations
 * 
 * @author Matteo Catena
 * @since 4.0
 */
public class BitInCodec extends BitInBase {

	private byte[] buffer;
	private int pos = 0;

	public BitInCodec()
	{
		buffer = new byte[1024];
	}
	
	public BitInCodec(byte[] input)
	{
		buffer = input;
	}
	
	@Override
	public void skipBytes(long len) throws IOException {
		throw new IOException(new UnsupportedOperationException());
	}

	@Override
	public void close() throws IOException {

	}

	@Override
	protected final void incrByte() throws IOException {
		byteRead = buffer[pos];
		pos++;
	}

	@Override
	protected final void incrByte(int i) throws IOException {
		throw new IOException(new UnsupportedOperationException());
	}
	
	public final void setup(ByteIn in, int bytes) throws IOException {
		
		buffer = ArrayUtils.grow(buffer, bytes+1);
		in.readFully(buffer, 0, bytes);
		//System.err.println("decompressing" + Arrays.toString(Arrays.copyOf(buffer, bytes)));
		pos = 0;
		
		offset = 0;
		bitOffset = 0;
		byteRead = buffer[pos];
		pos++;
	}

}
