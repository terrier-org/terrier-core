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
 * The Original Code is ByteIn.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 */

package org.terrier.compression.integer;

import java.io.IOException;

import org.terrier.compression.bit.BitIn;

/**
 * 
 * The bytewise counterpart of {@link BitIn}
 * 
 * @author Matteo Catena
 * @since 4.0
 */
public interface ByteIn {

	/**
	 * file extension
	 */
	String USUAL_EXTENSION = ".if";
			
	/**
	 * Returns the byte offset of the stream.
	 * It corresponds to the position of the 
	 * byte in which the next bit will be written.
	 * Use only when writing
	 * @return the byte offset in the stream.
	 */
	long getByteOffset();
		
	void close() throws IOException;

	void skipBytes(long l) throws IOException;
			
	int readFully(byte[] arr, int off, int len) throws IOException;
	
	int readVInt() throws IOException;

	long readVLong() throws IOException;
	
	int getVSize(long x) throws IOException;
//	byte readByte() throws IOException;
}
