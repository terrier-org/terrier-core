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
 * The Original Code is BufferedDataInput.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 */

package org.terrier.compression.integer;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A buffered DataInput implementation
 * 
 * @author Matteo Catena
 * @since 4.0
 */
public final class BufferedDataInput implements DataInput {
	
	private final DataInput in;
	private long remaining;
	
	private final ByteBuffer buffer;
	private final byte[] arr;
	
	/**
	 * 
	 * @param in
	 * @param length expected size of the input
	 * @param bufferSize
	 * @throws IOException
	 */
	public BufferedDataInput(DataInput in, long length, int bufferSize) throws IOException {
		
		this.in = in;
		remaining = length;
		arr = new byte[bufferSize];
		buffer = ByteBuffer.wrap(arr);
		buffer();
	}
	
	private final void buffer() throws IOException {
		
		int limit = (int)Math.min(remaining, arr.length);
				
		if (remaining <= 0)  {
			
			throw new EOFException();
			
		} else {
							
			in.readFully(arr, 0, limit);				
		}
		
		remaining -= limit;
		
		buffer.rewind();
		buffer.limit(limit);
	}
	

	@Override
	public final void readFully(byte[] b) throws IOException {

		readFully(b, 0, b.length);
	}

	@Override
	public final void readFully(byte[] b, int off, int len) throws IOException {
		
		while (true) {
		
			int r = buffer.remaining();
			int x = (r < len) ? r : len;
			
			buffer.get(b, off, x);
			
			if (x != len) {
				
				off += x;
				len -= x;
				buffer();
				 
			} else {
				
				break;
			}
		}
	}

	@Override
	public final int skipBytes(int n) throws IOException {

		int skipped = 0;
		
		while (true) {
			
			int r = buffer.remaining();
			int x = (r < n) ? r : n;
			
			buffer.position(buffer.position() + x);
			skipped += x;
			
			if (x != n) {
				
				n -= x;
				buffer();
				
			} else {
				
				break;
			}
		}		
		
		return skipped;
	}

	@Override
	public boolean readBoolean() throws IOException {
		
		throw new IOException(new UnsupportedOperationException());
	}

	@Override
	public final byte readByte() throws IOException {
		
		if (buffer.remaining() < 1) buffer();
		return buffer.get();
	}

	@Override
	public final int readUnsignedByte() throws IOException {
		
		return readByte();
	}

	@Override
	public final short readShort() throws IOException {
		
		throw new IOException(new UnsupportedOperationException());	
	}

	@Override
	public final int readUnsignedShort() throws IOException {
		
		throw new IOException(new UnsupportedOperationException());
	}

	@Override
	public final char readChar() throws IOException {

		throw new IOException(new UnsupportedOperationException());
	}

	@Override
	public final int readInt() throws IOException {
		
		throw new IOException(new UnsupportedOperationException());
	}

	@Override
	public final long readLong() throws IOException {
		
		throw new IOException(new UnsupportedOperationException());
	}

	@Override
	public final float readFloat() throws IOException {
		
		throw new IOException(new UnsupportedOperationException());
	}

	@Override
	public final double readDouble() throws IOException {
		
		throw new IOException(new UnsupportedOperationException());
	}

	@Override
	public String readLine() throws IOException {
		
		throw new IOException(new UnsupportedOperationException());
	}

	@Override
	public String readUTF() throws IOException {
		
		throw new IOException(new UnsupportedOperationException());
	}

}