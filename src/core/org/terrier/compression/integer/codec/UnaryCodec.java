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
 * The Original Code is UnaryCodec.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 */

package org.terrier.compression.integer.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.terrier.compression.bit.BitByteOutputStream;
import org.terrier.compression.integer.ByteIn;
import org.terrier.compression.integer.ByteOut;
import org.terrier.compression.integer.codec.util.BitInCodec;

/**
 * IntegerCodec implementation which uses unary coding
 * 
 * @author Matteo Catena
 * @since 4.0
 */
public final class UnaryCodec extends IntegerCodec {

	private final BitInCodec bitIn = new BitInCodec();
	
	@Override
	public final void compress(final int[] in, final int len, final ByteOut out) throws IOException {
		// we're not interested in compression speed for now (21/03/2013)
		// so this code is not optimized
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BitByteOutputStream bbos = new BitByteOutputStream(baos);
		for (int i = 0; i < len; i++) bbos.writeUnary(in[i]);
		bbos.close();
		byte[] baosArray = baos.toByteArray();
		
		//System.err.println("compressing" + Arrays.toString(Arrays.copyOf(in, len)) + " ints (len = "+ len + ")");
		//System.err.println("writing " + baosArray.length + " bytes for "  + len + " ints");
		
		out.writeVInt(baosArray.length);
		
		//System.err.println("writing " + Arrays.toString(baosArray));
		out.write(baosArray, 0, baosArray.length);
	}

	@Override
	public final void decompress(final ByteIn in, final int[] out, final int num)
			throws IOException {
		
		final int length = in.readVInt();
		assert length >= 0 : "number of bytes is negative ("+length+")";
		
		//System.err.println("reading " + length + " bytes for " + num);
		//new Exception().printStackTrace();
		
		bitIn.setup(in, length);
		
		for (int i = 0; i < num; i++) out[i] = bitIn.readUnary();
		//System.err.println("read " + Arrays.toString(Arrays.copyOf(out, num)));
	}

	@Override
	public final void skip(final ByteIn in) throws IOException {
		
		in.skipBytes(in.readVInt());
	}

}
