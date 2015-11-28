

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
 * The Original Code is BitOutputStream.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Roi Blanco
 */


package org.terrier.compression.bit;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.utility.Files;

/**
 * This class provides methods to write compressed integers to an outputstream.<br>
 * The numbers are written into a byte starting from the most significant bit (i.e, left to right).
 * There is an internal int buffer used before writing the bytes to the underlying stream,
 * and the bytes are written into 32-bits integers.
 * 
 * @author Roi Blanco
 *
 */
public class BitOutputStream implements BitOut {
    /** the logger for this class */
    protected static final Logger logger = LoggerFactory.getLogger(BitOutputStream.class);

	/** Writing buffer */
	protected byte[] buffer;
	/** poijnter for the buffer */
	protected int bufferPointer;
	/** size of the buffer it has to be 4 * k*/
	protected int bufferSize;
	/** Default size for the buffer*/
	protected static final int DEFAULT_SIZE = 16 * 1024 ;	
	/** The private output stream used internally.*/		
	protected DataOutputStream dos = null;
	/** The byte offset.*/
	protected long byteOffset;	
	/** The bit offset.*/
	protected int bitOffset;
	/** A int to write to the stream. */
	protected int byteToWrite;

	/**
	 * Initialises the variables in the stream. Used internally.
	 */
	private void init(){
		byteOffset = 0;
		bitOffset = 32;		
		byteToWrite = 0;
		buffer = new byte[DEFAULT_SIZE];
		bufferSize = DEFAULT_SIZE;		
	}
	
	/** sleep for the specified ms */ 
	private static void sleep(long millis)
	{
		try {Thread.sleep(millis); } catch (Exception e) {/* ignore */}
	}
	
	/**
	 * Empty constructor
	 */
	public BitOutputStream(){}

	/**
	 * Constructs an instance of the class for a given OutputSTream
	 * @param os the java.io.OutputStream used for writting 
	 * @throws java.io.IOException if an I/O error occurs
	 */
	public BitOutputStream(OutputStream os) throws IOException {		
		dos = new DataOutputStream(os);		
		init();
	}
		

	/**
	 * Constructs an instance of the class for a given filename
	 * Note that on a FileNotFoundException, this contructor will sleep for 2 seconds
	 * before retrying to open the file.
	 * @param filename String with the name of the underlying file
	 * @throws java.io.IOException if an I/O error occurs
	 */	
	public BitOutputStream(String filename) throws IOException {		
		try{
			dos = new DataOutputStream(Files.writeFileStream(filename));
		} catch(FileNotFoundException fnfe) {
			final String dir = Files.getParent(filename);
			logger.warn("Could not open new BitOutputStream because it alleged file could not be found.", fnfe);
			if (logger.isDebugEnabled())
				logger.debug("File.canRead()="+Files.canWrite(filename)+ "Dir.exists()=" +Files.exists(dir) 
					+ " Dir.canWrite()="+Files.canWrite(dir) +" Dir.contentsSize="+Files.list(dir).length);
			sleep(1000);
			if (logger.isDebugEnabled())
				logger.debug("File.canRead()="+Files.canWrite(filename)+ "Dir.exists()=" +Files.exists(dir) 
					+ " Dir.canWrite()="+Files.canWrite(dir)+" Dir.contentsSize="+Files.list(dir).length);
			logger.warn("Retrying to write BitOutputStream.");
			dos = new DataOutputStream(Files.writeFileStream(filename));
			logger.info("Previous warning can be ignored, BitOutputStream "+filename+" has opened successfully");
		}
		init();
	}

	/**
	 * Returns the byte offset of the stream.
	 * It corresponds to the position of the 
	 * byte in which the next bit will be written.
	 * @return the byte offset in the stream.
	 */
	public long getByteOffset() {
		return byteOffset * 4 + ((32 - bitOffset) / 8);
	}
	
	/**
	 * Returns the bit offset in the last byte.
	 * It corresponds to the position in which
	 * the next bit will be written.
	 * @return the bit offset in the stream.
	 */
	public byte getBitOffset() {				
		return (byte)((32 - bitOffset) % 8);		
	}
	
	/**
	 * Flushes the int currently being written into the buffer, and if it is necessary, 
	 * it flush the buffer to the underlying OutputStream
	 * @param writeMe int to be written into the buffer
	 * @throws IOException if an I/O error occurs
	 */
	private void writeIntBuffer(int writeMe) throws IOException{
		buffer[bufferPointer++] = (byte)(writeMe >>> 24);
		buffer[bufferPointer++] = (byte)(writeMe >>> 16);
		buffer[bufferPointer++] = (byte)(writeMe >>> 8);
		buffer[bufferPointer++] = (byte)writeMe;
		byteOffset++;
		if(bufferPointer == bufferSize){			
			dos.write(buffer,0,bufferPointer);	
			bufferPointer = 0;
		}
	}
	
	/**
	 * Writes the current integer used into the buffer, taking into account the number of bits written in that integer so far.
	 * Used when closing the file, to avoid unecessary byte writes.
	 * @param writeMe int to write
	 * @param _bitOffset number of bits written so far in the int
	 */
	private void writeIntBufferToBit(int writeMe, int _bitOffset){
		if(_bitOffset < 32 ) buffer[bufferPointer++] = (byte)(writeMe >>> 24);
		if(_bitOffset < 24 ) buffer[bufferPointer++] = (byte)(writeMe >>> 16);
		if(_bitOffset < 16 ) buffer[bufferPointer++] = (byte)(writeMe >>> 8);
		if(_bitOffset < 8 )  buffer[bufferPointer++] = (byte)(writeMe);		
		byteOffset++;		
	}
	
	/**
	 * Writes a number in the current byte we are using.
	 * @param b the number to write
	 * @param len the length of the number in bits
	 * @return the number of bits written
	 * @throws IOException if an I/O error occurs.
	 */
	private int writeInCurrent( final int b, final int len ) throws IOException {	
		// This check is necessary because x << 32 = x and not 0 
		if(len > 0){ 
			byteToWrite |= b <<  (bitOffset-=len);
			if ( bitOffset == 0 ) {
				writeIntBuffer(byteToWrite);
				bitOffset = 32;
				byteToWrite = 0;			
			}
		}
		return len;
	}
	
	
	
	/**
	 * Appends a byte array to the current stream.
	 * Flushes the current int, the buffer and then writes the new  sequence of bytes.
	 * @param toAppend byte[] it is going to be written to the stream.
	 * @param len length in bytes of the byte buffer (number of elements of the array).
	 * @throws IOException if an I/O exception occurs.
	 */
	public void append(byte[] toAppend, int len) throws IOException{	
		writeIntBufferToBit(byteToWrite,bitOffset);		
		dos.write(buffer,0,bufferPointer);	
		dos.write(toAppend, 0, len);		
		byteToWrite = 0;		
		byteOffset+= (len >> 4) + 1;
		bufferPointer = 0;
		bitOffset = 32;
	}
	
	/**
	 * Appends a byte array to the current stream, where the last byte is not fully written
	 * Flushes the current int, the buffer and then writes the new  sequence of bytes.
	 * @param toAppend byte[] it is going to be written to the stream.
	 * @param len length in bytes of the byte buffer (number of elements of the array).
	 * @param newByte last byte (the one not fully written)
	 * @param bitswritten number of bits written in the last byte
	 * @throws IOException if an I/O exception occurs.
	 */
	public void append(byte[] toAppend, int len, byte newByte, int bitswritten) throws IOException{	
		writeIntBufferToBit(byteToWrite,bitOffset);		
		dos.write(buffer,0,bufferPointer);	
		dos.write(toAppend, 0, len);				
		byteToWrite = ((int)newByte) << 24;
		byteOffset+= (len >> 4);
		bufferPointer = 0;
		bitOffset = 32 - bitswritten;	
	}
	
	/**
	 * Pads the current byte and writes the current int into the buffer.
	 * Then, it flushes the buffer to the underlying OutputStream.
	 * @throws IOException if an I/O error occurs.
	 */
	public void padAndFlush() throws IOException{
		writeIntBufferToBit(byteToWrite,bitOffset);				
		dos.write(buffer,0,bufferPointer);	
		byteToWrite = 0;
		byteOffset++;
		bufferPointer = 0;
		bitOffset = 32;
	}
	
	
	/** @deprecated */
	public void flush()
	{}

	/**
	 * Closes the BitOutputStream. It flushes the variables and buffer first.
	 * @throws IOException if an I/O error occurs when closing the underlying OutputStream
	 */
	public void close() throws IOException{	
		writeIntBufferToBit(byteToWrite,bitOffset);
		dos.write(buffer,0,bufferPointer);	
		dos.write(0);		
		dos.close();		
	}
	
	/** {@inheritDoc} **/
	public int writeUnary( int x ) throws IOException{			
		if(bitOffset >= x) return writeInCurrent(1, x);//+1
		final int shift = bitOffset;
		x -= shift;			
		writeIntBuffer(byteToWrite);
		bitOffset = 32;
		byteToWrite = 0;
		int i = x -1  >> 5;		
		while( i-- != 0 )  writeIntBuffer( 0 );
		writeInCurrent( 1, ( (x-1) & 31) + 1  );		
		return x + shift ;		
	}
	
	/** {@inheritDoc} **/
	public int writeGamma( int x ) throws IOException {			
		final int msb = BitUtilities.mostSignificantBit( x ) ;
		final int l = writeUnary( msb + 1 );		
		return l + ( writeInt( x , msb   ) );		
	}

	/** {@inheritDoc} **/
	public int writeDelta( int x ) throws IOException {
		final int msb = BitUtilities.mostSignificantBit( ++x );
		final int l = writeGamma( msb );
		return l + ( msb != 0 ? writeInt( x & ((2 << (msb - 1)) - 1), msb ) : 0 );
	}

	
	
	/** {@inheritDoc} **/
	public int writeInt( int x, final int len ) throws IOException {	
		if ( bitOffset >= len  ) return writeInCurrent( x, len );

		// number of bits to be written in the last int
		final int queue = ( len - bitOffset ) & 31; 
	
		writeInCurrent( x >> queue, bitOffset );
	
		writeInCurrent( x , queue);
		return len;
	}
	
	/** {@inheritDoc} **/
	public int writeSkewedGolomb( final int x, final int b ) throws IOException {	
		final int i = BitUtilities.mostSignificantBit( x / b + 1 );
		final int l = writeUnary( i + 1 );
		final int M = ( ( 1 << i + 1 ) - 1 ) * b;
		final int m = ( M / ( 2 * b ) ) * b;
		
		return l + writeMinimalBinary( x - m , M - m );
	}
	
	
	/** {@inheritDoc} **/
    public int writeInterpolativeCode( int data[], int offset, int len, int lo, int hi ) throws IOException {
		final int h, m;
		int l;

		if ( len == 0 ) return 0;
		if ( len == 1 ) return writeMinimalBinary( data[offset] - lo  , hi - lo  );		  
		h = len / 2;
		m = data[ offset + h ];		 		
		l = writeMinimalBinary( m - ( lo + h) , hi - len + h + 1 - ( lo + h ) );
		l += writeInterpolativeCode(  data, offset, h, lo, m - 1 );
		return l + writeInterpolativeCode( data, offset + h + 1, len - h - 1, m + 1, hi );
    }
    
    /** {@inheritDoc} **/
	public int writeGolomb( final int x, final int b ) throws IOException {	
		final int q = (x - 1) / b;
		final int l = writeUnary( q + 1 );	
		return l + writeMinimalBinary( x - q*b - 1, b );	
	}
	
	/** {@inheritDoc} **/
	public int writeMinimalBinary( final int x, final int b ) throws IOException {	
		final int log2b = BitUtilities.mostSignificantBit(b);
		// Numbers smaller than m are encoded in log2b bits.
		final int m = ( 1 << log2b + 1 ) - b; 

		if ( x < m ) 			
			return writeInt( x, log2b );		
		else			
			return writeInt( m + x, log2b + 1 );		
	}
	
	/** {@inheritDoc} **/
	public int writeBinary(int len, int x) throws IOException{
		return writeInt(x,len);
	}
	
	

}
