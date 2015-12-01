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
 * The Original Code is LinkedBuffer.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Roi Blanco
 */

package org.terrier.compression.bit;

import java.io.IOException;

/**
 * Implements an element of a linked list that contains a byte array
 * @author Roi Blanco
 *
 */
public class LinkedBuffer {

	/** The internal buffer. */
	protected byte buffer[];
	/** The size of the internal buffer*/
	protected int bufferSize;
	
	/** The default size of the internal buffer in bytes 
	* The buffer has to be big enough to allocate a single write, i.e we cannot write
	* *at once* more than bufferSize bytes
	*/
	public final static int DEFAULT_BUFFER_SIZE = 1024;
	
	/** The next buffer in the list */
	protected LinkedBuffer next = null;
	
	/** The position in the buffer */
	private int position = 0;
	
	/**
	 * Default Constructor. Uses a buffer of DEFAULT_BUFFER_SIZE bytes size.
	 *
	 */
	public LinkedBuffer(){
 		this(DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * Constructor
	 * @param _bufferSize size in bytes of the buffer.
	 */
	public LinkedBuffer(int _bufferSize){
		buffer = new byte[_bufferSize];
		this.bufferSize = _bufferSize;
	}
	
	/**
	 * @return the next linked buffer in the list (or null)
	 */
	public LinkedBuffer getNext(){
		return next;
	}

	/**
	 * Set the next buffer in the list
	 * @param _next next LinkedBuffer in the list
	 */
	public void setNext(LinkedBuffer _next){
		this.next = _next;
	}
	
	/**
	 * Writes a byte in the buffer
	 * @param b int containing the byte to write 
	 * @return true iff the buffer has used all its capacity
	 * @throws IOException
	 */
	public boolean write( final int b ) throws IOException {	
		buffer[ position++ ] = (byte)b;
		return position == bufferSize;
	}

	/**
	 * Writes a byte in the buffer
	 * @param b byte to write
	 * @return true iff the buffer has used all its capacity
	 * @throws IOException
	 */
	public boolean writeByte(final byte b) throws IOException{		
		buffer[ position++ ] = b;
		return position == bufferSize;
	}
	
	/**
	 * @return The size of the buffer
	 */
	public int getBufferSize(){
		return bufferSize;
	}
	
	/**
	 * @return The current position in the buffer
	 */
	public int getPosition(){
		return position;
	}
	
	/**
	 * @return The byte buffer (byte[])
	 */
	public byte[] getBuffer(){
		return buffer;
	}	
}
