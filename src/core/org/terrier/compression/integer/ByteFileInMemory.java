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
 * The Original Code is ByteFileInMemory.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 */

package org.terrier.compression.integer;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.compression.bit.BitFileInMemory;
import org.terrier.utility.io.RandomDataInputMemory;
import org.terrier.utility.io.WrappedIOException;

/**
 * 
 * The bytewise counterpart of {@link BitFileInMemory}
 * 
 * @author Matteo Catena
 * @since 4.0
 */
public class ByteFileInMemory implements ByteInSeekable {

	protected static final Logger logger = LoggerFactory
			.getLogger(ByteFileInMemory.class);

	private RandomDataInputMemory rdim;

	public ByteFileInMemory(RandomDataInputMemory f) {
		this.rdim = f;
	}

	public ByteFileInMemory(String dataFilename) throws IOException {
		
		this.rdim = new RandomDataInputMemory(dataFilename);
	}

	@Override
	public void close() {

		try {

			rdim.close();

		} catch (IOException ioe) {

			logger.error(
					"Input/Output exception while reading from a random access file. Stack trace follows",
					ioe);
		}

	}

	@Override
	public ByteIn readReset(long startByteOffset, long endByteOffset) throws IOException {

		return readReset(startByteOffset);
	}

	@Override
	public ByteIn readReset(long startByteOffset) throws IOException {

		try{
			
			RandomDataInputMemory clone = (RandomDataInputMemory) rdim.clone();
			clone.seek(startByteOffset);
			ByteIn in = new ByteInputStream(clone, startByteOffset);
			return in;
			
		} catch (CloneNotSupportedException e) {
			
			throw new WrappedIOException(e);
		}
	}
}
