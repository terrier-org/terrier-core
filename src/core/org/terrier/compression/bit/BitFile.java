
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
 * The Original Code is BitFile.java.
 *
 * The Original Code is Copyright (C) 2004-2014 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Roi Blanco (original author) 
 */
package org.terrier.compression.bit;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.utility.Files;
import org.terrier.utility.io.RandomDataInput;
import org.terrier.utility.io.RandomDataOutput;

/**
 * This class encapsulates a random access file and provides
 * the functionalities to read and write binary encoded, unary encoded and gamma encoded
 * integers greater than zero, as well as specifying their offset in the file. It
 * is employed by the DirectFile and the InvertedFile classes.
 * Use the getBit/ByteOffset methods only for writting, and not for reading.
 * This class contains the methods in both BitInputStream and BitOutputStream.
 * The numbers are written into a byte starting from the most significant bit (i.e, left to right).
 * The sequence of method calls to write a sequence of gamma encoded
 * and unary encoded numbers is:<br>
 * <pre>
 *  file.writeReset();
 *  long startByte1 = file.getByteOffset();
 *  byte startBit1 = file.getBitOffset();
 *  file.writeGamma(20000);
 *  file.writeUnary(2);
 *  file.writeGamma(35000);
 *  file.writeUnary(1);
 *  file.writeGamma(3);
 *  file.writeUnary(2);
 *  long endByte1 = file.getByteOffset();
 *  byte endBit1 = file.getBitOffset();
 *  if (endBit1 == 0 && endByte1 > 0) {
 *      endBit1 = 7;
 *      endByte1--;
 *  }
 * </pre>
 * while for reading a sequence of numbers the sequence of calls is:<br>
 * <pre>
 *  file.readReset(startByte1, startBit1, endByte1, endBit1);
 *  int gamma = file.readGamma();
 *  int unary = file.readUnary();
 * </pre>
 * @author Roi Blanco
 * @deprecated Use BitFileBuffered and BitOutputStream instead
 */
public class BitFile implements BitInSeekable, BitIn, BitOut {
	/** The logger used */
	protected static final Logger logger = LoggerFactory.getLogger(BitFile.class);
	/** Write buffer */
	protected byte[] buffer;
	/** Pointer for the buffer */
	protected int bufferPointer;
	/** Size of the buffer (it has to be 4 * k) */
	protected int bufferSize;
	/** Default size */
	protected static final int DEFAULT_SIZE = 16 * 1024 ;
	/** Default file mode access for a BitFile object. 
	  * Currently "<tt>rw</tt>". */
	protected static final String DEFAULT_FILE_MODE = "rw";
	/** The byte offset.*/
	protected long byteOffset;
	/** The current byte offset to be read */
	protected int readByteOffset;
	/** The bit offset.*/	
	protected int bitOffset;
	/** A int to write to the stream. */
	protected int byteToWrite;
	/** Indicates if we are writting or reading */
	protected boolean isWriteMode = false;
	/** The underlying file */
	protected RandomDataInput file;
	/** Same object as file, but cast to RandomDataOutput */
	protected RandomDataOutput writeFile = null;
	/** Buffer for reads */
	protected byte[] inBuffer;
	/** Number of bits read so far */
	protected int readBits = 0;
	/**
	 * Initialises the variables, used internally
	 *
	 */
	protected void init(){
		byteOffset = 0;
		bitOffset = 32;		
		byteToWrite = 0;
		buffer = new byte[DEFAULT_SIZE];
		bufferSize = DEFAULT_SIZE;
		
	}
	
	/** 
	 * Constructs an instance of the class for a given RandomDataInput instance accessing a bit compressed file/stream
	 * @param data a RandomDataInput instance containing the bit compressed data
	 */
	public BitFile(RandomDataInput data) {
		this.file = data;
		init();
	}
	
	/** 
	 * Constructs an instance of the class for a given file and an acces method to the file
	 * @param _file File to read/write
	 * @param access String indicating the access permissions of the file
	 */
	public BitFile(File _file, String access) {
		try {
			this.file = (access.indexOf("w") != -1) 
				? Files.writeFileRandom(_file)
				: Files.openFileRandom(_file);
			init();			
		} catch (IOException ioe) {
			logger.error("Input/Output exception while creating BitFile object.", ioe);
		}
		
	}
	
	/** 
	 * Constructs an instance of the class for a given filename and an acces method to the file
	 * @param filename java.lang.String the name of the underlying file
	 * @param access String indicating the access permissions of the file
	 */
	public BitFile(String filename, String access) {
		try {
			this.file = (access.indexOf("w") != -1)
                ? Files.writeFileRandom(filename)
                : Files.openFileRandom(filename);
			init();					
		} catch (IOException ioe) {
			logger.error("Input/Output exception while creating BitFile object.", ioe);
		}	
	}
	
	
	/** 
	 * Constructs an instance of the class for a given filename, "rw" permissions
	 * @param filename java.lang.String the name of the underlying file
	 */
	public BitFile(String filename){
		this(filename, DEFAULT_FILE_MODE);
	}
	/** 
	 * Constructs an instance of the class for a given filename, "rw" permissions
	 * @param _file java.io.File
	 */
	public BitFile(File _file) {
		this(_file, DEFAULT_FILE_MODE);
	}
	
	/** do nothing constructor */
	protected BitFile() {}
	
	
	/**
	 * Returns the byte offset of the stream.
	 * It corresponds to the position of the 
	 * byte in which the next bit will be written.
	 * Use only when writting
	 * @return the byte offset in the stream.
	 */
	public long getByteOffset() {	
		return this.isWriteMode
			? byteOffset * 4 + ((32 - bitOffset) / 8)
			: readByteOffset;
	}
	/**
	 * Returns the bit offset in the last byte.
	 * It corresponds to the position in which
	 * the next bit will be written.
	 * Use only when writting.
	 * @return the bit offset in the stream.
	 */
	public byte getBitOffset() {	
		//System.out.println("bitOffset="+bitOffset + " calculated="+((32 - bitOffset) % 8) );
		return this.isWriteMode 
			? (byte)((32 - bitOffset) % 8)
			: (byte)bitOffset;
			/*: (byte)( 8-(( (32 - bitOffset) % 8)%7) )*/
	}
	
	/**
	 * Flushes the int currently being written into the buffer, and if it is necessary, 
	 * it flush the buffer to the underlying OutputStream
	 * @param writeMe int to be written into the buffer
	 * @throws IOException if an I/O error occurs
	 */
	protected void writeIntBuffer(int writeMe) throws IOException{
		// at least there is one empty gap		
		buffer[bufferPointer++] = (byte)(writeMe >>> 24);
		buffer[bufferPointer++] = (byte)(writeMe >>> 16);
		buffer[bufferPointer++] = (byte)(writeMe >>> 8);
		buffer[bufferPointer++] = (byte)writeMe;
		byteOffset++;
		if(bufferPointer == bufferSize){			
			writeFile.write(buffer,0,bufferPointer);		
			bufferPointer = 0;
		}
	}
	
	/**
	 * Writes a number in the current byte we are using.
	 * @param b the number to write
	 * @param len the length of the number in bits
	 * @return the number of bits written
	 * @throws IOException if an I/O error occurs.
	 */
	protected int writeInCurrent( final int b, final int len )  throws IOException{	
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
	
	/** {@inheritDoc} **/
	public int writeUnary( int x ) throws IOException{			
		if(bitOffset >= x) return writeInCurrent(1, x);
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
	public int writeDelta( int x ) throws IOException {
		final int msb = BitUtilities.mostSignificantBit( ++x );
		final int l = writeGamma( msb );
		return l + ( msb != 0 ? writeInt( x, msb ) : 0 );
	}
	
	/** {@inheritDoc} **/
	public int writeGamma( int x ) throws IOException {	
		final int msb = BitUtilities.mostSignificantBit( x ) ;
		final int l = writeUnary( msb + 1 );		
		return l + ( writeInt( x , msb   ) );		
	}
	
	/** {@inheritDoc} **/
	public int writeInt( int x, final int len ) throws IOException {	
		if ( bitOffset >= len  ) return writeInCurrent( x, len );
		final int queue = ( len - bitOffset ) & 31; 		
		writeInCurrent( x >> queue, bitOffset );		
		writeInCurrent( x , queue);
		return len;
	}
	
	
	/**
	 * Flushes the OuputStream
	 * (empty method)
	 */
	public void writeFlush(){}
	
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
	public BitIn readReset(long startByteOffset, byte startBitOffset, long endByteOffset, byte endBitOffset) {
		try {
			this.isWriteMode = false;
			file.seek(startByteOffset);
			inBuffer = new byte[(int)(endByteOffset - startByteOffset + 2)];		
			file.readFully(inBuffer);
			readByteOffset = 0;
			bitOffset = startBitOffset;
		} catch(IOException ioe) {
			logger.error("Input/Output exception while reading from a random access file. Stack trace follows", ioe);
		}
		return this;
	}
	
	/** 
	 * {@inheritDoc} 
	 */
	public BitIn readReset(long startByteOffset, byte startBitOffset) throws IOException {
		throw new IOException("Unsupported");
	}
	
	/** {@inheritDoc} **/
	public int readGamma()  {
		int u = readUnary() - 1;		
		return (1 << u) + readBinary(u) ;
	}
	
	/** {@inheritDoc} **/
	public int readUnary()  {
		int x;
		final int leftA = (inBuffer[readByteOffset] << bitOffset) & 0x00FF;		
		if(leftA != 0){			
			x = 8 - BitUtilities.MSB_BYTES[ leftA ];
			bitOffset += x ;
			readIn();
			return x;
		}
		x = 8 - bitOffset;
		readByteOffset++;
		while( (inBuffer[readByteOffset]== 0 )) {
			x += 8;
			readByteOffset++;
		}
		x += (bitOffset =  8 -  BitUtilities.MSB_BYTES[ inBuffer[readByteOffset] & 0x00FF] );
		readIn();
		return x;		
	}
	
	/** {@inheritDoc} **/
	public int readDelta() throws IOException {		
		final int msb = readGamma();
		return ( ( 1 << msb ) | readBinary( msb ) ) - 1;
	}
	
	/**
	 * Reads a new byte from the InputStream if we have finished with the current one. 
	 * @throws IOException if we have reached the end of the file
	 */
	protected void readIn(){
		if(bitOffset == 8){					
			bitOffset = 0;
			readByteOffset++;		 						
		}
	}


	/** {@inheritDoc} **/
    public void align() {
        if ( ( bitOffset & 7 ) == 0 ) return;
        bitOffset = 0;
        readByteOffset++;
    }
	
	/**
	 * Reads a binary integer from the already read buffer.
	 * @param len is the number of binary bits to read
	 * @return the decoded integer
	 */
	public int readBinary(int len) {
		if(8 - bitOffset > len){					
			int b = ( ((inBuffer[readByteOffset] << bitOffset) & 0x00FF)) >>> (8-len) ;
			bitOffset += len;
			return b;
		}
		
		int x = inBuffer[readByteOffset] & ( ~ (0xFF << (8-bitOffset) )) &0xFF;
		len +=  bitOffset - 8;
		int i = len >> 3;
		while(i-- != 0){			
			readByteOffset++;
			x = x << 8 | (inBuffer[readByteOffset] & 0xFF); 
		}		
		readByteOffset++;
		bitOffset = len & 7;	
		return (x << bitOffset) | ((inBuffer[readByteOffset] & 0xFF) >>> (8-bitOffset)) ;
	}

	/** Skip a number of bits in the current input stream
	 * @param len The number of bits to skip
	 */
    public void skipBits(int len)
    {
		if(8 - bitOffset > len){
			bitOffset += len;	
			return;
		}
		len +=  bitOffset - 8;
        final int i = len >> 3;
        if (i > 0)
        {
            readByteOffset+= i;
        }
		readByteOffset++;
		bitOffset = len & 7;
	}
    
    /** {@inheritDoc} */
	public void skipBytes(long len) throws IOException {
		if (readByteOffset + len > inBuffer.length)
			throw new EOFException();
		readByteOffset+= len;
		bitOffset = 0;
	}
	
	/**
	 * Closes the file. If the file has been written, it is also flushed to disk. 
	 */
	
	public void close(){
		try{
		if(isWriteMode){			
			writeIntBufferToBit(byteToWrite,bitOffset);
			writeFile.write(buffer,0,bufferPointer);				
		}			
		file.close();
		}catch(IOException ioe){
			logger.error("Input/Output exception while closing BitFile object.", ioe);
			
		}
	}
	

	/**
	 * Writes the current integer used into the buffer, taking into account the number of bits written.
	 * Used when closing the file, to avoid unecessary byte writes.
	 * in that integer so far.
	 * @param writeMe int to write
	 * @param _bitOffset number of bits written so far in the int
	 */
	protected void writeIntBufferToBit(int writeMe, int _bitOffset){
		if(_bitOffset < 32 ) buffer[bufferPointer++] = (byte)(writeMe >>> 24);
		if(_bitOffset < 24 ) buffer[bufferPointer++] = (byte)(writeMe >>> 16);
		if(_bitOffset < 16 ) buffer[bufferPointer++] = (byte)(writeMe >>> 8);
		if(_bitOffset < 8 )  buffer[bufferPointer++] = (byte)(writeMe);		
		byteOffset++;		
	}
	
	/**
	 * Set the write mode to true
	 *
	 */
	public void writeReset() throws IOException {
		if (!( file instanceof RandomDataOutput))
			throw new IOException("Cannot write to read only BitFile file");
		writeFile = (RandomDataOutput)file;	
		this.isWriteMode = true;
	}
	
	/**
	 * Writes an integer in binary format to the stream.
	 * @param len size in bits of the number.
	 * @param x the integer to write.
	 * @return the number of bits written.
	 * @throws IOException if an I/O error occurs.
	 */
	public int writeBinary(int len, int x) throws IOException{
		return writeInt(x,len);
	}
	
	/**
	 * Writes an integer x using minimal binary encoding, given an upper bound.
	 * This method is not failsafe, it doesn't check if the argument is 0 or negative.
	 * @param x the number to write
	 * @param b and strict bound for <code>x</code>
	 * @return the number of bits written
	 * @throws IOException if an I/O error occurs.
	 */
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
	public int readMinimalBinary( final int b ) throws IOException {	
		final int log2b = BitUtilities.mostSignificantBit(b);
		final int m = ( 1 << log2b + 1 ) - b; 
		final int x = readBinary( log2b );		
		if ( x < m ) return x + 1;
		else { int temp =  ( x << 1 ) + readBinary(1)  ;
		return temp;
		}
	}
	
	/**
	 * Writes and integer x into the stream using golomb coding. 
	 * This method is not failsafe, it doesn't check if the argument or the modulus is 0 or negative.
	 * @param x the number to write
	 * @param b the parameter for golomb coding
	 * @return the number of bits written
	 * @throws IOException if and I/O error occurs
	 */
	public int writeGolomb( final int x, final int b ) throws IOException {	
		final int q = (x - 1) / b;
		final int l = writeUnary( q + 1 );	
		return l + writeMinimalBinary( x - q*b - 1, b );	
	}
	
	/** {@inheritDoc} **/
	public int readGolomb( final int b) throws IOException {		
		final int q = (readUnary() - 1 ) * b;
		return q + readMinimalBinary( b ) + 1;
	}
	
	/**
	 * Writes and integer x into the stream using skewed-golomb coding.
	 *  Consider a bucket-vector <code>v = &lt;b, 2b, 4b, ... , 2^i b, ...&gt; </code> 
	 * an integer <code>x</code> is coded as <code>unary(k+1)</code> where <code>k</code> is the index
	 * <code>sum(i=0)(k) v_i < x &lt;= sum(i=0)(k+1)</code> <br>, so <code>k = log(x/b + 1)</code>
	 * <code>sum_i = b(2^n -1)</code> (geometric progression)
	 * and the remainder with <code>log(v_k)</code> bits in binary
	 * if <code> lower = ceil(x/b) -&gt; lower = 2^i * b -&gt; i = log(ceil(x/b)) + 1</code>
	 * the remainder <code>x - sum_i 2^i*b - 1 = x - b(2^n - 1) - 1</code> is coded with floor(log(v_k)) bits	
	 *  
	 * This method is not failsafe, it doesn't check if the argument or the modulus is 0 or negative.
	 * @param x the number to write
	 * @param b the parameter for golomb coding
	 * @return the number of bits written
	 * @throws IOException if and I/O error occurs
	 */		
	public int writeSkewedGolomb( final int x, final int b ) throws IOException {	
		final int i = BitUtilities.mostSignificantBit( x / b + 1 );
		final int l = writeUnary( i + 1 );
		final int M = ( ( 1 << i + 1 ) - 1 ) * b;
		final int m = ( M / ( 2 * b ) ) * b;
		
		return l + writeMinimalBinary( x - m , M - m );
	}
	
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
	public int readSkewedGolomb( final int b ) throws IOException {
		
		final int M = ( ( 1 << readUnary()  ) - 1 ) * b;
		final int m = ( M / ( 2 * b ) ) * b;
		return m + readMinimalBinary( M - m ) ;
	}
	
	/** {@inheritDoc} **/
	public void readInterpolativeCoding( int data[], int offset, int len, int lo, int hi ) throws IOException {
		final int h, m;
		
		if ( len == 0 ) return;
		if ( len == 1 ) {
			data[ offset ] = readMinimalBinaryZero( hi - lo  ) + lo  ;		
			return;
		}
		
		h = len / 2;
		m = readMinimalBinaryZero( hi - len + h  - ( lo + h ) + 1 ) + lo + h ;
		data[ offset + h ] = m ;
		
		readInterpolativeCoding(  data, offset, h, lo, m - 1 );
		readInterpolativeCoding(  data, offset + h + 1, len - h - 1, m + 1, hi );
	}
	
	/** {@inheritDoc} **/
	public int readMinimalBinaryZero(int b) throws IOException{
		if(b > 0 ) return readMinimalBinary(b);
		else return 0;
	}

}
