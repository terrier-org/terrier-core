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
 * The Original Code is DebuggingDataOutput.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 *   
 */

package org.terrier.utility.io;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Debugging code to test classes that implement DataOutput 
 * @author Craig Macdonald
 * @since 4.0
 */
public class DebuggingDataOutput implements DataOutput {

	static Logger LOG = LoggerFactory.getLogger(DebuggingDataOutput.class);
	DataOutput parent;
	
	public DebuggingDataOutput(DataOutput parent) {
		super();
		this.parent = parent;
	}

	@Override
	public void write(int arg0) throws IOException {
		LOG.info("write("+arg0+")");
		parent.write(arg0);
	}

	@Override
	public void write(byte[] arg0) throws IOException {
		LOG.info("write("+Arrays.toString(arg0)+")");
		parent.write(arg0);
	}

	@Override
	public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		LOG.info("write("+Arrays.toString(arg0)+","+arg1+","+arg2+")");
		parent.write(arg0);
	}

	@Override
	public void writeBoolean(boolean arg0) throws IOException {
		LOG.info("writeBoolean("+arg0+")");
		parent.writeBoolean(arg0);
	}

	@Override
	public void writeByte(int arg0) throws IOException {
		LOG.info("writeByte("+arg0+")");
		parent.writeByte(arg0);
	}

	@Override
	public void writeBytes(String arg0) throws IOException {
		LOG.info("writeBytes("+arg0+")");
		parent.writeBytes(arg0);
	}

	@Override
	public void writeChar(int arg0) throws IOException {
		LOG.info("writeChar("+arg0+")");
		parent.writeChar(arg0);
	}

	@Override
	public void writeChars(String arg0) throws IOException {
		LOG.info("writeChars("+arg0+")");
		parent.writeChars(arg0);
	}

	@Override
	public void writeDouble(double arg0) throws IOException {
		LOG.info("writeDouble("+arg0+")");
		parent.writeDouble(arg0);
	}

	@Override
	public void writeFloat(float arg0) throws IOException {
		LOG.info("writeFloat("+arg0+")");
		parent.writeFloat(arg0);
	}

	@Override
	public void writeInt(int arg0) throws IOException {
		LOG.info("writeInt("+arg0+")");
		parent.writeInt(arg0);
	}

	@Override
	public void writeLong(long arg0) throws IOException {
		LOG.info("writeLong("+arg0+")");
		parent.writeLong(arg0);
	}

	@Override
	public void writeShort(int arg0) throws IOException {
		LOG.info("writeShort("+arg0+")");
		parent.writeShort(arg0);
	}

	@Override
	public void writeUTF(String arg0) throws IOException {
		LOG.info("writeUTF("+arg0+")");
		parent.writeUTF(arg0);
	}

}
