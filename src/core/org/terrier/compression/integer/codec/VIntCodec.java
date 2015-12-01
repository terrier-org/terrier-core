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
 * The Original Code is VIntCodec.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 */

package org.terrier.compression.integer.codec;

import java.io.IOException;

import org.terrier.compression.integer.ByteIn;
import org.terrier.compression.integer.ByteOut;

/**
 * IntegerCoding implementation which uses Varible byte encoding.
 * It leverages Hadoop VInt functions.
 * 
 * @author Matteo Catena
 * @since 4.0
 */
public final class VIntCodec extends IntegerCodec {


	
	@Override
	public final void compress(final int[] in, final int len, ByteOut out) throws IOException {
		
		int usedBytes = 0;
		for (int i = 0; i < len; i++) usedBytes += out.getVSize(in[i]);
		out.writeVInt(usedBytes);
		for (int i = 0; i < len; i++) out.writeVInt(in[i]);	    
	}

	@Override
	public final void decompress(final ByteIn in, final int[] out, final int num) throws IOException {

		in.readVInt(); //just discard it
		for (int i = 0; i < num; i++) out[i] = in.readVInt();
		
	}

	public void compress(long[] in, int len, ByteOut out) throws IOException {
		
		int usedBytes = 0;
		for (int i = 0; i < len; i++) usedBytes += out.getVSize(in[i]);
		out.writeVInt(usedBytes);
		for (int i = 0; i < len; i++) out.writeVLong(in[i]);	 		
	}

	public void decompress(ByteIn in, long[] out, int num)
			throws IOException {
		
		in.readVInt(); //just discard it
		for (int i = 0; i < num; i++) out[i] = in.readVLong();		
	}

	@Override
	public final void skip(final ByteIn in) throws IOException {
		
		in.skipBytes(in.readVInt());
	}

	
}