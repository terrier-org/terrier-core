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
 * The Original Code is DebuggingBitIn.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.compression.bit;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.utility.ArrayUtils;

/** This class provides debugging at the bit stream level. All read*() and skip*() methods
 * log verbosely for each request.
 * @author Craig Macdonald
 * @since 3.5
 * @see org.terrier.compression.bit.BitIn
 */
public class DebuggingBitIn implements BitIn, Closeable {

	private static final Logger logger = LoggerFactory.getLogger(DebuggingBitIn.class);
	
	/** parent BitIn implementation */
	BitIn in;
	/** Wraps a BitIn implementation with logging calls */
	public DebuggingBitIn(BitIn _in)
	{
		this.in = _in;
	}
	
	/** returns the current position as a handy Stirng */
	private String position()
	{
		return "{" + in.getByteOffset() + "," + in.getBitOffset() + "} ";
	}
	
	
	@Override
	public void align() throws IOException {
		String oldpos = position();
		in.align();
		logger.debug(oldpos + "align() " + position());		
	}

	@Override
	public byte getBitOffset() {
		return in.getBitOffset();
	}

	@Override
	public long getByteOffset() {
		return in.getByteOffset();
	}

	@Override
	public int readBinary(int len) throws IOException {
		String oldpos = position();
		int rtr = in.readBinary(len);
		logger.debug(oldpos + "readBinary("+len+")="+rtr+" " + position());	
		return rtr;
	}

	@Override
	public int readDelta() throws IOException {
		String oldpos = position();
		int rtr = in.readDelta();
		logger.debug(oldpos + "readDelta()="+rtr+" " + position());	
		return rtr;
	}

	@Override
	public int readGamma() throws IOException {
		String oldpos = position();
		int rtr = in.readGamma();
		logger.debug(oldpos + "readGamma()="+rtr+" " + position());
		//new Exception().printStackTrace();
		return rtr;
	}

	@Override
	public int readGolomb(int b) throws IOException {
		String oldpos = position();
		int rtr = in.readGolomb(b);
		logger.debug(oldpos + "readGolomb("+b+")="+rtr+" " + position());	
		return rtr;
	}

	@Override
	public void readInterpolativeCoding(int[] data, int offset, int len,
			int lo, int hi) throws IOException {
		String oldpos = position();
		in.readInterpolativeCoding(data, offset, len,lo, hi);
		logger.debug(oldpos + "readInterpolativeCoding("+ArrayUtils.join(new int[]{offset, len, lo, hi}, ",")+")=["+ArrayUtils.join(data, ",")+"] " + position());	
	}

	@Override
	public int readMinimalBinary(int b) throws IOException {
		String oldpos = position();
		int rtr = in.readMinimalBinary(b);
		logger.debug(oldpos + "readMinimalBinary("+b+")="+rtr+" " + position());	
		return rtr;
	}

	@Override
	public int readMinimalBinaryZero(int b) throws IOException {
		String oldpos = position();
		int rtr = in.readMinimalBinaryZero(b);
		logger.debug(oldpos + "readMinimalBinaryZero("+b+")="+rtr+" " + position());	
		return rtr;
	}

	@Override
	public int readSkewedGolomb(int b) throws IOException {
		String oldpos = position();
		int rtr = in.readSkewedGolomb(b);
		logger.debug(oldpos + "readSkewedGolomb("+b+")="+rtr+" " + position());	
		return rtr;
	}

	@Override
	public int readUnary() throws IOException {
		String oldpos = position();
		int rtr = in.readUnary();
		logger.debug(oldpos + "readUnary()="+rtr+" " + position());	
		return rtr;
		
	}

	@Override
	public void skipBits(int len) throws IOException {
		String oldpos = position();
		in.skipBits(len);
		logger.debug(oldpos + "skipBits("+len+") " + position());	
	}

	@Override
	public void skipBytes(long len) throws IOException {
		String oldpos = position();
		logger.debug(oldpos + "skipBytes("+len+") ");
		in.skipBytes(len);
		logger.debug(oldpos + "skipBytes("+len+") " + position());	
	}

	@Override
	public void close() throws IOException {
		in.close();
	}
	

}
