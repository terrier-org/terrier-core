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
 * The Original Code is SimpleDocumentIndexEntry.java
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

/** A document index entry that doesn't write out direct index offset. */
public class SimpleDocumentIndexEntry extends DocumentIndexEntry
{
	/** 
	 * Factory for the creation of DocumentIndexEntry objects
	 */
	public static class Factory implements FixedSizeWriteableFactory<DocumentIndexEntry>
	{
		/** 
		 * {@inheritDoc} 
		 */
		public int getSize() {
			return 4 + 4;
		}
		/** 
		 * {@inheritDoc} 
		 */
		public DocumentIndexEntry newInstance() {
			return new SimpleDocumentIndexEntry();
		}
	}
	//TODO: it doesn't need entries?
	/**
	 * Construct an instance of the class.
	 */
	public SimpleDocumentIndexEntry(){}
	/**
	 * Construct an instance of the class with
	 * @param die
	 */
	public SimpleDocumentIndexEntry(DocumentIndexEntry die) {
		super.entries = die.getNumberOfEntries();
		super.doclength = die.getDocumentLength();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void setNumberOfEntries(int n) {
		super.entries = n;
	}

	/** 
	 * {@inheritDoc} 
	 * Does nothing in this implementation
	 */
	public void setOffset(BitFilePosition pos) {}
	/** 
	 * {@inheritDoc}
	 * Does nothing in this implementation
	 */
	public void setBitIndexPointer(BitIndexPointer pointer) {}
	/** 
	 * {@inheritDoc} 
	 */
	public void readFields(DataInput in) throws IOException {
		super.entries = in.readInt();
		super.doclength = in.readInt();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void write(DataOutput out) throws IOException {
		out.writeInt(super.entries);
		out.writeInt(super.doclength);
	}
	/** 
	 * {@inheritDoc} 
	 */
	public String pointerToString() {
		return super.entries + "@{}"; 
	}
	/** 
	 * {@inheritDoc} 
	 * Does nothing in this implementation
	 */
	public void setPointer(Pointer p) {
		return;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public byte getFileNumber() {
		return 0;
	}
	/** 
	 * {@inheritDoc} 
	 * Does nothing in this implementation
	 */
	public void setFileNumber(byte fileId)
	{
	}
}
