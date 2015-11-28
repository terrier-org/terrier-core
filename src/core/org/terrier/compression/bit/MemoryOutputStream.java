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
 * The Original Code is BitIn.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Roi Blanco
 */

package org.terrier.compression.bit;

import java.io.IOException;
import java.io.OutputStream;

import org.terrier.utility.ArrayUtils;

/**
 * This class extends an ordinary OutputStream to handle transparently writes in memory. This means that 
 * this class doesn't flush to disk anything, that should be done separately (@see MSBOStream).
 * It allocates a byte[] array of original size, that can grow and be reallocated dynamically if it is needed.
 * The class used for handling the reallocations is @see ArrayUtils.
 * @author Roi Blanco
 *
 */
public class MemoryOutputStream extends OutputStream {	
	
	/**
	 * Default constructor. Instanciates a buffer of DEFAULT_BUFFER_SIZE size
	 *
	 */
	public MemoryOutputStream(){
		this(DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * Creates a new instance of this class setting the buffer size. 
	 * @param bufferSize size of the buffer (in bytes)
	 */
	public MemoryOutputStream(int bufferSize){
		this(bufferSize, bufferSize*2);
	}
	
	/**
	 * Creates a new instance of this class setting the buffer size and the growing ratio
	 * @param bufferSize size of the buffer (in bytes)
	 * @param enlargeSize size the buffer is going to grow if needed (argument to ByteArrays)
	 */
	public MemoryOutputStream(int bufferSize, int enlargeSize){
		buffer = new byte[bufferSize];
		avail = bufferSize;
		enlargeQ = enlargeSize;
		pos = 0;
	}
	
	/** The default size of the internal buffer in bytes */
	public final static int DEFAULT_BUFFER_SIZE = 1024;
	
	/** The internal buffer. */
	protected byte buffer[];
	
	/** The current position in the buffer. */
	protected int pos;
	
	/** Number of free bytes before having to enlarge */
	protected int avail;
	
	/** Enlarge size of the array  */
	protected final int enlargeQ;
	
	/**
	 * Checks if the buffer needs to be reallocated, and if it is the case, it calls to ArrayUtils to enlarge it and copy the
	 * data to a new location
	 * @throws IOException if an I/O error occurs
	 */
	private void enlargeBuffer() throws IOException {
		if(avail == 0){
			int preLen = buffer.length;
			buffer = ArrayUtils.grow(buffer, buffer.length + enlargeQ);
			avail = buffer.length - preLen;
		}
	}
	
	/**
	 * Calls to ArrayUtils to enlarge the buffer in a specified amount, and copy the
	 * data to a new location.
	 * @param enlarge amount the buffer expects to grow.
	 * @throws IOException if an I/O error occurs.
	 */	
	private void enlargeBuffer(final int enlarge) throws IOException{
		int preLen = buffer.length;
		buffer = ArrayUtils.grow(buffer, buffer.length + enlarge);
		avail += buffer.length - preLen;
	}
	
	/**
	 * @return the position in the buffer
	 */
	public int getPos(){
		return pos;
	}
	
	/**
	 * Writes a byte into the buffer.
	 * @param b int containing the byte to write.
	 * @throws IOException if an I/O error occurs.
	 */
	public void write( final int b ) throws IOException {
		enlargeBuffer();
		avail--;
		buffer[ pos++ ] = (byte)b;
	}
	
	/**
	 * Writes a byte into the buffer.
	 * @param b byte to write.
	 * @throws IOException if an I/O error occurs.
	 */
	public void writeByte(final byte b) throws IOException{
		enlargeBuffer();
		avail--;
		buffer[ pos++ ] = b;		
	}
	
	/**
	 * Writes a sequence of bytes to the buffer.
	 * @param  b byte[] array containing the bytes to write
	 * @param  offset int offset where the data to write begins
	 * @param  length int number of elements to write
	 * @throws IOException if an I/O error occurs 
	 */
	public void write( final byte b[], int offset, int length ) throws IOException { 
		if ( length > avail ) 
			enlargeBuffer( length );
		System.arraycopy( b, offset, buffer, pos, length );
		pos += length;
		avail -= length;		
	}

	/** Get size */
	public int getSize() {
		return buffer.length;
	}
	
	/**
	 * @return the underlying byte[] buffer
	 */
	public byte[] getBuffer(){
		return buffer;
	}
	
	/**
	 * Empty method
	 */
	public void flush() throws IOException {
	}
	
	/**
	 * Empty method
	 */
	public void close() throws IOException {
	}
		
	/**
	 * Writes the sequence of bytes in the byte[] into String format
	 */
	public String toString(){
		StringBuilder fin = new StringBuilder();
		for(int i = 0; i < pos+1 ; i++){
			for(int j = 0; j < 8; j++){
				fin.append( (buffer[i]&(1 << j))>>j ); 
			}
			fin.append(" ");// += " ";
		}
		return fin.toString();
	}	
}
