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
 * The Original is in 'BitInBase.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.compression.bit;

import java.io.IOException;

/** Base class for various BitIn implementations 
 * @since 3.6
 * @author Craig Macdonald
 */
public abstract class BitInBase implements BitIn {

	/** bit offset in this byte, and in the larger file */
	protected int bitOffset;
	
	/** offset in the larger file */
	protected long offset;
	
	/** current byte */
	protected byte byteRead;
	
	/** {@inheritDoc} **/
	@Override
	public long getByteOffset() {
		return offset;
	}

	/** {@inheritDoc} **/
	@Override
	public byte getBitOffset() {
		return (byte)bitOffset;
	}
	
	/** {@inheritDoc} **/
	@Override
	public int readUnary() throws IOException {
		int x;
		final int leftA = (byteRead << bitOffset) & 0x00FF;		
		if(leftA != 0){			
			x = 8 - BitUtilities.MSB_BYTES[ leftA ];
			bitOffset += x ;
			readIn();
			return x;
		}
		x = 8 - bitOffset;
		incrByte();
		while( (byteRead == 0 )) {
			x += 8;
			incrByte();
		}
		x += (bitOffset =  8 -  BitUtilities.MSB_BYTES[ byteRead & 0x00FF] );
		readIn();
		return x;		
	}
	
	/** {@inheritDoc} **/
	@Override
	public int readBinary(int len) throws IOException {
		if(8 - bitOffset > len){					
			int b = ( ((byteRead << bitOffset) & 0x00FF)) >>> (8-len) ;
			bitOffset += len;
			return b;
		}
		
		int x = byteRead & ( ~ (0xFF << (8-bitOffset) )) &0xFF;
		len +=  bitOffset - 8;
		int i = len >> 3;
		while(i-- != 0){			
			incrByte();
			x = x << 8 | (byteRead & 0xFF); 
		}		
		incrByte();
		bitOffset = len & 7;	
		return (x << bitOffset) | ((byteRead & 0xFF) >>> (8-bitOffset)) ;
	}
	
	/** {@inheritDoc} **/
	@Override
	public int readDelta() throws IOException {		
		final int msb = readGamma();
		return ( ( 1 << msb ) | readBinary( msb ) ) - 1;
	}
	
	/** {@inheritDoc} **/
	@Override
	public int readGamma() throws IOException {
		final int u = readUnary() - 1;		
		return (1 << u) + readBinary(u) ;
	}
	
	/** {@inheritDoc} **/
	@Override
	public int readSkewedGolomb( final int b ) throws IOException {
		
		final int M = ( ( 1 << readUnary()  ) - 1 ) * b;
		final int m = ( M / ( 2 * b ) ) * b;
		return m + readMinimalBinary( M - m ) ;
	}

	/** {@inheritDoc} **/
	@Override
	public int readGolomb( final int b) throws IOException {		
		final int q = (readUnary() - 1 ) * b;
		return q + readMinimalBinary( b ) + 1;
	}
	
	/** {@inheritDoc} **/
	@Override
	public int readMinimalBinary( final int b ) throws IOException {	
		final int log2b = BitUtilities.mostSignificantBit(b);
		final int m = ( 1 << log2b + 1 ) - b; 
		final int x = readBinary( log2b );		
		if ( x < m ) return x;
		else { int temp =  ( x << 1 ) + readBinary(1) - m;
		return temp;
		}
	}
	
	/** {@inheritDoc} **/
	@Override
	public int readMinimalBinaryZero(int b) throws IOException{
		if(b > 0 ) return readMinimalBinary(b);
		else return 0;
	}


	/** {@inheritDoc} **/
	@Override
	public void readInterpolativeCoding( int data[], int localoffset, int len, int lo, int hi ) throws IOException {
		final int h, m;
		
		if ( len == 0 ) return;
		if ( len == 1 ) {
			data[ localoffset ] = readMinimalBinaryZero( hi - lo  ) + lo  ;		
			return;
		}
		
		h = len / 2;
		m = readMinimalBinaryZero( hi - len + h  - ( lo + h ) + 1 ) + lo + h ;
		data[ localoffset + h ] = m ;
		
		readInterpolativeCoding(  data, localoffset, h, lo, m - 1 );
		readInterpolativeCoding(  data, localoffset + h + 1, len - h - 1, m + 1, hi );
	}

	/** Skip a number of bits in the current input stream
 	* @param len The number of bits to skip
 	*/
	@Override
	public void skipBits(int len) throws IOException	
	{
//		if (len == 0)
//			return;
		if(8 - bitOffset > len){
			bitOffset += len;	
			return;
		}
		len +=  bitOffset - 8;
		final int i = len >> 3;
		if (i > 0)
		{
		   incrByte(i);
		}
		incrByte();
		bitOffset = len & 7;
	}

	/** {@inheritDoc} **/
	@Override
  	public void align() throws IOException
  	{
       if ( ( bitOffset & 7 ) == 0 ) return;
       bitOffset = 0;
   	   incrByte();
  	}
	
	/** Move forward one byte. The newly read byte should appear
	 * in the byteRead variable. 
	 * @throws IOException */
	protected abstract void incrByte() throws IOException;
	
	/** Move forward i bytes. The newly read byte should appear
	 * in byteRead variable.
	 * @throws IOException */
	protected abstract void incrByte(int i) throws IOException;

	
	/**
	 * Reads a new byte from the InputStream if we have finished with the current one. 
	 */
	protected void readIn() throws IOException {
		if(bitOffset == 8){					
			bitOffset = 0;
			incrByte();	 						
		}
	}
}
