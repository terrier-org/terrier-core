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
 * The Original Code is DebuggingDataInput.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 *   
 */

package org.terrier.utility.io;

import java.io.DataInput;
import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Debugging code to test classes that implement DataInput 
 * @author Craig Macdonald
 * @since 4.0
 */
public class DebuggingDataInput implements DataInput {

	static Logger LOG = LoggerFactory.getLogger(DebuggingDataInput.class);
	DataInput parent;
	public DebuggingDataInput(DataInput _parent)
	{
		this.parent = _parent;
	}
	
	@Override
	public boolean readBoolean() throws IOException {
		boolean rtr = parent.readBoolean();
		LOG.info("readBoolean()=" +rtr);
		return rtr;
	}

	@Override
	public byte readByte() throws IOException {
		byte b = parent.readByte();
		LOG.info("readByte()=" +b);
		return b;
	}

	@Override
	public char readChar() throws IOException {
		char c = parent.readChar();
		LOG.info("readChar()=" +c);
		return c;
	}

	@Override
	public double readDouble() throws IOException {
		double d = parent.readDouble();
		LOG.info("readDouble()=" +d);
		return d;
	}

	@Override
	public float readFloat() throws IOException {
		float f = parent.readFloat();
		LOG.info("readFloat()=" +f);
		return f;
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		parent.readFully(b);
		LOG.info("readFully()=" + Arrays.toString(b));
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		parent.readFully(b, off, len);
		LOG.info("readFully("+off+","+len+")=" + Arrays.toString(b));
	}

	@Override
	public int readInt() throws IOException {
		int i = parent.readInt();
		LOG.info("readInt()="+i);
		return i;
	}

	@Override
	public String readLine() throws IOException {
		String rtr = parent.readLine();
		LOG.info("readLine()="+rtr);
		return rtr;
	}

	@Override
	public long readLong() throws IOException {
		long l = parent.readLong();
		LOG.info("readLong()="+l);
		return l;
	}

	@Override
	public short readShort() throws IOException {
		short s = parent.readShort();
		LOG.info("readShort()="+s);
		return s;
	}

	@Override
	public String readUTF() throws IOException {
		String rtr = parent.readUTF();
		LOG.info("readUTF()="+rtr);
		return rtr;
	}

	@Override
	public int readUnsignedByte() throws IOException {
		int i = parent.readUnsignedByte();
		LOG.info("readUnsignedByte()="+i);
		return i;
	}

	@Override
	public int readUnsignedShort() throws IOException {
		int i = parent.readUnsignedShort();
		LOG.info("readUnsignedShort()="+i);
		return i;
	}

	@Override
	public int skipBytes(int n) throws IOException {
		int rtr = parent.skipBytes(n);
		LOG.info("skipBytes("+n+")="+rtr);
		return rtr;
	}

}
