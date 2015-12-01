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
 * The Original Code is LemireCodec.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 *  Craig Macdonald <craig.macdonald@glasgow.ac.uk> 
 */

package org.terrier.compression.integer.codec;

import java.io.IOException;
import java.nio.ByteBuffer;

import me.lemire.integercompression.Composition;
import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.SkippableComposition;
import me.lemire.integercompression.SkippableIntegerCODEC;

import org.terrier.compression.integer.ByteIn;
import org.terrier.compression.integer.ByteOut;
import org.terrier.utility.ArrayUtils;

/**
 * Generic IntegerCodec implementation which wraps any SkippableIntegerCODEC from the JavaFastPFOR package.
 * A generic set of SkippableIntegerCODECs can also be specified using String parameters to the constructor.
 * 
 * @author Matteo Catena and Craig Macdonald
 * @since 4.0
 */
public class LemireCodec extends IntegerCodec {

	private final IntWrapper inpos;
	private final IntWrapper outpos;
	private final SkippableIntegerCODEC codec;

	public LemireCodec(String[] params) throws Exception {
		this(getCODEC(params));
	}
	
	public LemireCodec(SkippableIntegerCODEC codec) {

		this.codec = codec;
		inpos = new IntWrapper(0);
		outpos = new IntWrapper(0);
	}

	@Override
	public final void compress(final int[] in, final int len, final ByteOut out) throws IOException {

		inpos.set(0);
		outpos.set(0);

		supportArray = ArrayUtils.growOrCreate(supportArray, len * 4 + 1024);
		codec.headlessCompress(in, inpos, len, supportArray, outpos);
		
 		int bytes = outpos.get() * 4;
		if (outBuffer.capacity() < bytes) 
			outBuffer = ByteBuffer.allocate(bytes * 2);
		else
			outBuffer.position(0);

		outBuffer.asIntBuffer().put(supportArray, 0, outpos.get());
		out.writeVInt(outpos.get());
		out.write(outBuffer.array(), 0, bytes);
	}

	@Override
	public final void decompress(final ByteIn in, final int[] out, final int num) throws IOException {

		final int ints = in.readVInt();
		final int len = ints*4;
		//System.err.println("reading " + len + " bytes");
		assert len >= 0;
		
		inpos.set(0);
		outpos.set(0);
				
		if (inBuffer.capacity() < len)
			inBuffer = ByteBuffer.allocate(len * 2);
		else
			inBuffer.position(0);
		in.readFully(inBuffer.array(), 0, len);
		
		supportArray = ArrayUtils.growOrCreate(supportArray, ints);
		inBuffer.asIntBuffer().get(supportArray, 0, ints);

		codec.headlessUncompress(supportArray, inpos, ints, out, outpos, num);
	}

	@Override
	public final void skip(final ByteIn in) throws IOException {

		in.skipBytes(in.readVInt()*4);
	}
	
	public static SkippableIntegerCODEC getCODEC(String[] params) throws Exception
	{
		SkippableIntegerCODEC integerCodec;
		// 1. get SkippableIntegerCODEC
		String integerCodecClassName = params[0];
		assert integerCodecClassName != null;
		
		if (! integerCodecClassName.contains("."))
			integerCodecClassName = SkippableIntegerCODEC.class.getPackage().getName() +"." + integerCodecClassName;
		
		Class<? extends SkippableIntegerCODEC> integerCodecClass = Class.forName(integerCodecClassName).asSubclass(SkippableIntegerCODEC.class);
		
		if (!Composition.class.isAssignableFrom(integerCodecClass)) {
		
			integerCodec = integerCodecClass.newInstance();
		
		} else {
			// 1. get primary codec
			String primaryCodecClassName = params[1];
			assert primaryCodecClassName != null;	
			if (! primaryCodecClassName.contains("."))
				primaryCodecClassName = SkippableIntegerCODEC.class.getPackage().getName() +"." + primaryCodecClassName;
			Class<? extends SkippableIntegerCODEC> primaryCodecClass = Class.forName(primaryCodecClassName).asSubclass(SkippableIntegerCODEC.class);
			// 2. get secondary codec
			String secondaryCodecClassName = params[2];
			assert secondaryCodecClassName != null;
			if (! secondaryCodecClassName.contains("."))
				secondaryCodecClassName = SkippableIntegerCODEC.class.getPackage().getName() +"." + secondaryCodecClassName;
			
			
			Class<? extends SkippableIntegerCODEC> secondaryCodecClass = Class.forName(secondaryCodecClassName).asSubclass(SkippableIntegerCODEC.class);
		
			integerCodec = (SkippableComposition) integerCodecClass.getConstructor(
					SkippableIntegerCODEC.class, SkippableIntegerCODEC.class).newInstance(
					primaryCodecClass.newInstance(),
					secondaryCodecClass.newInstance());
		}
		return integerCodec;
	}
}
