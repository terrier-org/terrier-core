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
 * The Original Code is ByteFileBuffered.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 */

package org.terrier.compression.integer;

import java.io.IOException;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.compression.bit.BitFileBuffered;
import org.terrier.compression.integer.BufferedDataInput;
import org.terrier.utility.Files;
import org.terrier.utility.io.RandomDataInput;

/**
 * 
 * The bytewise counterpart of {@link BitFileBuffered}
 * 
 * This class uses a "file pool", so pay attention to close unused resources
 * 
 * @author Matteo Catena
 * @since 4.0
 */
public class ByteFileBuffered implements ByteInSeekable {

	protected static final Logger logger = LoggerFactory
			.getLogger(ByteFileBuffered.class);
	
	private final String filename;
	private Stack<RandomDataInput> filePool;
	
	/* please don't go under a 9 bytes buffer size */
	private final static int DEFAULT_BUFFER_SIZE = 8 * 1024;
	private int bufferSize;

	/**
	 * Constructs an instance of the class for a given filename. Default buffer
	 * size
	 * 
	 * @param filename
	 *            java.lang.String the name of the underlying file
	 */
	public ByteFileBuffered(String filename) {

		this(filename, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Constructs an instance of the class for a given filename
	 * 
	 * @param filename
	 *            java.lang.String the name of the underlying file
	 * @param bufSize
	 *            how much of the file to buffer
	 */
	public ByteFileBuffered(String filename, int bufSize) {

		this.filename = filename;
		filePool = new Stack<RandomDataInput>();
        bufferSize = bufSize;
		assert bufferSize > 8;
        
		try {

			filePool.push(Files.openFileRandom(filename));

		} catch (IOException ioe) {

			logger.error(
					"Input/Output exception while creating ByteFileBuffered object.",
					ioe);
		}

	}

	@Override
	public void close() {

		try {

			for (RandomDataInput file : filePool) file.close();

		} catch (IOException ioe) {

			logger.error(
					"Input/Output exception while reading from a random access file. Stack trace follows",
					ioe);
		}

	}

	private RandomDataInput getFile() throws IOException {
		
		if (filePool.isEmpty()) filePool.push(Files.openFileRandom(filename));
		
		RandomDataInput file = filePool.pop();
		
		return file;
	}
	
	@Override
	public ByteIn readReset(long startByteOffset, long endByteOffset) throws IOException {

		RandomDataInput ris = getFile();
		int size = (int) Math.min(bufferSize, endByteOffset - startByteOffset);
		return new BufferedFileByteIn(ris, size, startByteOffset, this);
	}

	@Override
	public ByteIn readReset(long startByteOffset) throws IOException {

		return readReset(startByteOffset, startByteOffset + bufferSize);
	}
	
	public void reclaim(RandomDataInput file) {
		
		if (file != null) filePool.push(file);
	}

	public static final class BufferedFileByteIn extends ByteInputStream implements ByteIn {

		final private RandomDataInput file;
		private ByteFileBuffered father;

		
		@Override
		public void close() {

			father.reclaim(file);
		}

		public BufferedFileByteIn(RandomDataInput file, int bufferSize,
				long startByteOffset, ByteFileBuffered father) throws IOException {
						
			this.file = file;
			this.father = father;
			
			try {

				file.seek(startByteOffset);
				this.di = new BufferedDataInput(file, file.length() - startByteOffset, bufferSize);
				this.byteOffset = startByteOffset;

			} catch (IOException e) {

				logger.error(
						"Input/Output exception while reading from a random access file. Stack trace follows",
						e);
			}
		}		
	}
}