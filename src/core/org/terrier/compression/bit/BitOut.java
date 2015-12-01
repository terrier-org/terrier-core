
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
 * The Original Code is BitOut.java.
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

/** Interface describing the writing compression methods supported
 * by the BitOutputStream classes. Integers written through these
 * compression methods must be greater than 0.
 * @author Craig Macdonald
  * @since 2.0
 */
public interface BitOut extends Closeable {
    /**
     * Returns the byte offset of the stream.
     * It corresponds to the position of the
     * byte in which the next bit will be written.
     * @return the byte offset in the stream.
     */
    long getByteOffset();
    /**
     * Returns the bit offset in the last byte.
     * It corresponds to the position in which
     * the next bit will be written.
     * @return the bit offset in the stream.
     */
    byte getBitOffset();

	/**
	 * Writes an integer x using unary encoding. The encoding is a sequence of x -1 zeros and 1 one:
	 * 1, 01, 001, 0001, etc ..
	 * This method is not failsafe, it doesn't check if the argument is 0 or negative. 
	 * @param x the number to write
	 * @return the number of bits written
	 * @throws IOException if an I/O error occurs.
	 */
	int writeUnary( int x ) throws IOException;
	/**
	 * Writes an integer x into the stream using gamma encoding.
	 * This method is not failsafe, it doesn't check if the argument is 0 or negative.
	 * @param x the int number to write
	 * @return the number of bits written
	 * @throws IOException if an I/O error occurs.
	 */
	int writeGamma( int x ) throws IOException;
	/**
	 * Writes an integer in binary format to the stream.
	 * @param len size in bits of the number.
	 * @param x the integer to write.
	 * @return the number of bits written.
	 * @throws IOException if an I/O error occurs.
	 */
	int writeBinary(int len, int x) throws IOException;
	
	/** Writes a sequence of integers using interpolative coding. The data must be sorted (increasing order).	
	 *	
	 * @param data the vector containing the integer sequence.
	 * @param offset the offset into <code>data</code> where the sequence starts.
	 * @param len the number of integers to code.
	 * @param lo a lower bound (must be smaller than or equal to the first integer in the sequence). 
	 * @param hi an upper bound (must be greater than or equal to the last integer in the sequence).
	 * @return the number of written bits.
	 * @throws IOException if an I/O error occurs.
	 */
	int writeInterpolativeCode( int data[], int offset, int len, int lo, int hi ) throws IOException;
	
	/**
	 * Writes and integer x into the stream using skewed-golomb coding.
	 *  Consider a bucket-vector <code>v = &lt;b, 2b, 4b, ... , 2^i b, ...&gt; </code> 
	 * an integer <code>x</code> is coded as <code>unary(k+1)</code> where <code>k</code> is the index
	 * <code>sum(i=0)(k) v_i < x &lt;= sum(i=0)(k+1)</code> <br>, so <code>k = log(x/b + 1)</code>
	 * <code>sum_i = b(2^n -1)</code> (geometric progression)
	 * and the remainder with <code>log(v_k)</code> bits in binary
	 * if <code> lower = ceil(x/b) -&gt; lower = 2^i * b -&gt; i = log(ceil(x/b)) + 1</code>
	 * the remainder <code>x - sum_i 2^i*b - 1 = x - b(2^n - 1) - 1</code> is coded with <code>floor(log(v_k))</code> bits	
	 *  
	 * This method is not failsafe, it doesn't check if the argument or the modulus is 0 or negative.
	 * @param x the number to write
	 * @param b the parameter for golomb coding
	 * @return the number of bits written
	 * @throws IOException if and I/O error occurs
	 */		
	int writeSkewedGolomb( final int x, final int b ) throws IOException;
	
	/**
	 * Writes and integer x into the stream using golomb coding. 
	 * This method is not failsafe, it doesn't check if the argument or the modulus is 0 or negative.
	 * @param x the number to write
	 * @param b the parameter for golomb coding
	 * @return the number of bits written
	 * @throws IOException if and I/O error occurs
	 */
	int writeGolomb( final int x, final int b ) throws IOException;
	
	/**
	 * Writes an integer x using minimal binary encoding, given an upper bound.
	 * This method is not failsafe, it doesn't check if the argument is 0 or negative.
	 * @param x the number to write
	 * @param b and strict bound for <code>x</code>
	 * @return the number of bits written
	 * @throws IOException if an I/O error occurs.
	 */
	int writeMinimalBinary( final int x, final int b ) throws IOException;
	
	/**
	 * Writes an integer x into the stream using delta encoding.
	 * This method is not failsafe, it doesn't check if the argument is 0 or negative.
	 * @param x the int number to write
	 * @return the number of bits written
	 * @throws IOException if an I/O error occurs.
	 */
	int writeDelta( int x ) throws IOException;
	
	/**
	 * Writes an integer x into the underlying OutputStream. First, it checks if it fits into the current
	 * byte we are using for writing, and then it writes as many bytes as necessary
	 * @param x the int to write
	 * @param len length of the int in bits
	 * @return the number of bits written
	 * @throws IOException if an I/O error occurs.
	 */
	int writeInt( int x, final int len ) throws IOException;
}
