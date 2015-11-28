
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
 * The Original Code is BitInSeekable.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.compression.bit;

import java.io.Closeable;
import java.io.IOException;

/** Interface for reading a bit compressed file in a random access manner.
 * Each method returns a {@link org.terrier.compression.bit.BitIn} implementation
 * that allows access to the encoded integers. Several implementations exist:
 * <ul>
 * <li>{@link org.terrier.compression.bit.BitFileBuffered} - buffers an amount of data starting that the specified offset.</li>
 * <li>{@link org.terrier.compression.bit.BitFileInMemory} - reads the entire file into memory. File must be less than Integer.MAX_VALUE (2GB).</li>
 * <li>{@link org.terrier.compression.bit.BitFileInMemoryLarge} - reads the entire file into memory. File size only constrained by available memory.</li>
 * </ul>
 * @author Craig Macdonald
  * @since 2.0
 * @see org.terrier.compression.bit.BitFileBuffered
 * @see org.terrier.compression.bit.BitOut
 * @see org.terrier.compression.bit.BitIn
 */
public interface BitInSeekable extends Closeable {
	/**
	 * Reads from the file a specific number of bytes and after this
	 * call, a sequence of read calls may follow. The offsets given 
	 * as arguments are inclusive. For example, if we call this method
	 * with arguments 0, 2, 1, 7, it will read in a buffer the contents 
	 * of the underlying file from the third bit of the first byte to the 
	 * last bit of the second byte.
	 * @param startByteOffset the starting byte to read from
	 * @param startBitOffset the bit offset in the starting byte
	 * @param endByteOffset the ending byte 
	 * @param endBitOffset the bit offset in the ending byte. 
	 *        This bit is the last bit of this entry.
	 * @return Returns the BitIn object to use to read that data
	 */	
	BitIn readReset(long startByteOffset, byte startBitOffset, long endByteOffset, byte endBitOffset) throws IOException;
	
	/**
	 * Reads from the file a specific number of bytes and after this
	 * call, a sequence of read calls may follow. The offsets given 
	 * as arguments are inclusive. For example, if we call this method
	 * with arguments 0, 2, 1, 7, it will read in a buffer the contents 
	 * of the underlying file from the third bit of the first byte to the 
	 * last bit of the second byte.
	 * @param startByteOffset the starting byte to read from
	 * @param startBitOffset the bit offset in the starting byte
	 * @return Returns the BitIn object to use to read that data
	 */
	BitIn readReset(long startByteOffset, byte startBitOffset) throws IOException;
}
