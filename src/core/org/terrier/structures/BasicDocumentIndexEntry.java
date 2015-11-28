/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is BasicDocumentIndexEntry.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.terrier.structures.seralization.FixedSizeWriteableFactory;
/** 
 * A basic document index entry. Allows the creation of a document index entries.
 */
public class BasicDocumentIndexEntry extends DocumentIndexEntry
{
	/** 
	 * Returna a factory for creating document index entries
	 */
	public static class Factory implements FixedSizeWriteableFactory<DocumentIndexEntry>
	{
		/** 
		 * Returns 17? 
		 */
		public int getSize() {
			return 4 + 4 + 8 + 1;
		}
		/** 
		 * Creates a document index entry
		 */
		public DocumentIndexEntry newInstance() {
			return new BasicDocumentIndexEntry();
		}
	}
	/**
	 * Constructs an instance of the BasicDocumentIndexEntry.
	 */
	public BasicDocumentIndexEntry() {	}
	
	/**
	 * Constructs an instance of the BasicDocumentIndexEntry.
	 * @param in
	 */
	public BasicDocumentIndexEntry(DocumentIndexEntry in)
	{
		doclength = in.getDocumentLength();
		entries = in.getNumberOfEntries();
		bytes = in.getOffset();
		bits = in.getOffsetBits();
		bits += in.getFileNumber() << FILE_SHIFT;
	}
	/**
	 * Constructs an instance of the BasicDocumentIndexEntry.
	 * @param length
	 * @param pointer
	 */
	public BasicDocumentIndexEntry(int length, BitIndexPointer pointer)
	{
		doclength = length;
		bytes = pointer.getOffset();
		bits = pointer.getOffsetBits();
		bits += pointer.getFileNumber() << FILE_SHIFT;
		entries = pointer.getNumberOfEntries();
	}
	/**
	 * Constructs an instance of the BasicDocumentIndexEntry. 
	 * @param length
	 * @param fileId
	 * @param byteOffset
	 * @param bitOffset
	 * @param numberOfTerms
	 */
	public BasicDocumentIndexEntry(int length, byte fileId, long byteOffset, byte bitOffset, int numberOfTerms)
	{
		doclength = length;
		bytes = byteOffset;
		bits = bitOffset;
		bits += fileId << FILE_SHIFT;
		entries = numberOfTerms;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void readFields(DataInput in) throws IOException {
		doclength = in.readInt();
		bytes = in.readLong();
		bits = in.readByte();
		entries = in.readInt();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void write(DataOutput out) throws IOException {
		out.writeInt(doclength);
		out.writeLong(bytes);
		out.writeByte(bits);
		out.writeInt(entries);
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void setBitIndexPointer(BitIndexPointer pointer) {
		entries = pointer.getNumberOfEntries();
		setOffset(pointer);
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void setOffset(BitFilePosition pos) {
		bytes = pos.getOffset();
		bits = pos.getOffsetBits();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void setNumberOfEntries(int n) {
		entries = n;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public String pointerToString() {
		return "@{"+bytes+ "," + bits + "}";
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void setPointer(Pointer p) {
		bytes = ((BitIndexPointer)p).getOffset();
		bits = ((BitIndexPointer)p).getOffsetBits();
	}
	
}