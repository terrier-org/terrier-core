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
 * The Original Code is ByteInputStream.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 */

package org.terrier.compression.integer;

import java.io.DataInput;
import java.io.IOException;

import org.apache.hadoop.io.WritableUtils;
import org.terrier.compression.bit.BitInputStream;
import org.terrier.structures.IndexUtil;

/**
 * Byte wise counterpart of {@link BitInputStream}
 * 
 * @author Matteo Catena
 * @since 4.0
 */
public class ByteInputStream implements ByteIn {
	
	protected DataInput di;
	protected long byteOffset;
	
	protected ByteInputStream() {}
	
	public ByteInputStream(DataInput di) throws IOException {
		
		this(di, 0);
	}
	
	public ByteInputStream(DataInput di, long offset) throws IOException {
		
		this.di = di;
		byteOffset = offset;
	}		
		
	@Override
	public final long getByteOffset() {
		
		return byteOffset;
	}

	@Override
	public void close() throws IOException  {		
		
		IndexUtil.close(di);
	}

	@Override
	public final void skipBytes(final long l) throws IOException {
				
		long skipped = 0;
		do{			
			int toSkip = (int) Math.min(l-skipped, (long)Integer.MAX_VALUE);
			skipped += di.skipBytes(toSkip);
		} while (skipped < l);
		byteOffset += skipped;
	}


	@Override
	public final int readVInt() throws IOException {
		
		int i = WritableUtils.readVInt(di);
		byteOffset += WritableUtils.getVIntSize(i);

		return i;
	}
	
	@Override
	public final long readVLong() throws IOException {
		
		long i = WritableUtils.readVLong(di);
		byteOffset += WritableUtils.getVIntSize(i);
		return i;
	}	

	@Override
	public final int readFully(final byte[] arr, final int off, final int len) throws IOException {
		
		di.readFully(arr, off, len);
		byteOffset += len;

		return len;
	}

	@Override
	public int getVSize(long x) throws IOException {
		
		return WritableUtils.getVIntSize(x);
	}

}
