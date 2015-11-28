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
 * The Original Code is FilePosition.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures;

import java.io.DataInput;
import java.io.DataOutput;
import java.nio.*;

/**
 * Models a position within a file as the offset in bytes
 * and the offset in bits in that byte. For example, an instance
 * of the class FilePosition that points to the 10th bit of 
 * a file should be constructed with a byte offset of 1 and 
 * a bit offset of 2.
 * @author Craig Macdonald, Vassilis Plachouras &amp; John Kane
 */
public class FilePosition implements BitFilePosition
{
	/** The number of bytes a file position could be converted to
	 * - 8 for the byte's long, 1 for the bits
	 */
	protected static final int sizeInBytes = 9;
	
	/** The offset within a file in bytes. */
	public long Bytes;
	
	/** The offset in bits within the pointed byte. */
	public byte Bits;

	/** Default constructor. Create an uninitialiased FilePosition */
	public FilePosition()
	{ Bytes = 0; Bits = 0;}
	
	/** 
	 * Creates an instance of the class from the given 
	 * byte and bit offsets.
	 * @param bytesPosition long the given byte offset.
	 * @param bitsPosition byte the given bit offset.
	 */
	public FilePosition(long bytesPosition, byte bitsPosition) {
		Bytes = bytesPosition; Bits = bitsPosition;
	}

	/** Create an instance based on a byte buffer */
	public FilePosition(byte[] in)
	{
		ByteBuffer buffer = ByteBuffer.allocate(in.length);
		buffer.put(in);
		buffer.rewind();
		
		Bytes = buffer.getLong();
		Bits = buffer.get();
	}

	/** Create a new FilePosition based on an existing one */	
	public FilePosition(BitFilePosition in)
	{
		Bytes = in.getOffset();
		Bits = in.getOffsetBits();
	}

	/** 
	 * {@inheritDoc} 
	 */
	public long getOffset() { return Bytes; }
	/** 
	 * {@inheritDoc} 
	 */
	public byte getOffsetBits() { return Bits; }
	/** 
	 * {@inheritDoc} 
	 */
	public void setOffset(long bytes, byte bits)
	{
		Bytes = bytes;
		Bits = bits;
	}
	
	/** 
	 * {@inheritDoc} 
	 */
	public void setOffset(BitFilePosition pos) {
		Bytes = pos.getOffset();
		Bits = pos.getOffsetBits();
	}

	/** How large is this object when serialized */	
	public static int sizeInBytes()
	{
		return sizeInBytes;
	}

	/** Seralize this object to the named dataoutput instance */	
	public void toBytes(DataOutput dai)
	{
		try{
		dai.writeLong(Bytes);
		dai.writeByte(Bits);}
		catch(Exception e){e.printStackTrace();}
	}

	/** unseralize this object from the named dataoutput instance */	
	public void build(DataInput di)
	{		
		try{
		Bytes = di.readLong();
		Bits = di.readByte();}
		catch(Exception e){e.printStackTrace();}
	}

	/** Is this FilePosition equal to another? */	
	public boolean equals(Object o)
	{
		if (! (o instanceof FilePosition))
			return false;
		return ((FilePosition)o).Bits == this.Bits && ((FilePosition)o).Bytes == this.Bytes;
	}

	/** Define a hashcode for this - objects which defined equals(Object) should define
	  * hashCode() also */
	public int hashCode() {
		return (int)(this.Bytes * this.Bits);
	}
	
	/** String representation of this object */
	public String toString()
	{
		return "{"+Bytes+","+Bits+"}";
	}
	
}
