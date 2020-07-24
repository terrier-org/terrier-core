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
 * The Original Code is FieldDocumentIndexEntry.java
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.terrier.utility.ArrayUtils;

/** A document index entry for use with fields */
public class FieldDocumentIndexEntry extends FieldedDocumentIndexEntry implements BitIndexPointer {

	
	/** 
	 * Factory for creating a field document index entry 
	 */
	public static class Factory extends BasicDocumentIndexEntry.Factory
	{	
		protected int fieldCount;
		/**
		 * Constructs an instance of the class with
		 * @param _fieldCount
		 */
		public Factory(int _fieldCount)
		{
			super();
			this.fieldCount = _fieldCount;
		}
		/**
		 * Constructs an instance of the class with
		 * @param _fieldCount
		 */
		public Factory(String _fieldCount)
		{
			this(Integer.parseInt(_fieldCount));
		}

		@Override
		public int getSize() {
			return super.getSize() + fieldCount * 4;
		}

		@Override
		public DocumentIndexEntry newInstance() {
			return new FieldDocumentIndexEntry(fieldCount);
		}

		@Override
		public void writeProperties(PropertiesIndex index, String structureName) {
			String fieldProp = "index." + structureName + ".fields.count";
			index.setIndexProperty(fieldProp, String.valueOf(fieldCount));
			index.addIndexStructure(structureName, this.getClass().getName(), "java.lang.String", "${"+fieldProp+"}");
		}
		
	}
	
	long bytes;
    byte bits;
	protected int[] fieldLengths;
	
	//seldom used constructor
	/**
	 * Constructs an instance of the class.
	 */
	public FieldDocumentIndexEntry()
	{
		fieldLengths = new int[0];
	}
	/**
	 * Constructs an instance of the class with
	 * @param die
	 */
	public FieldDocumentIndexEntry(DocumentIndexEntry _die)
	{
		FieldDocumentIndexEntry die = (FieldDocumentIndexEntry)_die;
		doclength = die.getDocumentLength();
		bytes = die.getOffset();
		bits = die.getOffsetBits();
		bits += die.getFileNumber() << FILE_SHIFT;
		entries = die.getNumberOfEntries();
		if (_die instanceof FieldedDocumentIndexEntry)
			this.fieldLengths = ((FieldedDocumentIndexEntry)die).getFieldLengths();
	}
	/**
	 * Constructs an instance of the class with
	 * @param fieldCount
	 */
	public FieldDocumentIndexEntry(int fieldCount) 
	{
		this.fieldLengths = new int[fieldCount];
	}
	/** 
	 * Get the lengths of each field
	 */
	public int[] getFieldLengths()
	{
		return this.fieldLengths;
	}
	/** 
	 * Set the lengths of each field
	 */
	public void setFieldLengths(int[] f_lens)
	{
		this.fieldLengths = f_lens;
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

	/** 
     * {@inheritDoc} 
     */
    @Override
    public byte getOffsetBits() 
    {
        return (byte) (bits & BIT_MASK);
    }

    /** 
     * {@inheritDoc} 
     */
    @Override
    public long getOffset() 
    {
        return bytes;
    }
    
    /** 
     * {@inheritDoc} 
     */
    @Override
    public byte getFileNumber() 
    {
        return (byte) ( (0xFF & bits) >> FILE_SHIFT);
    }
    
    /** 
     * {@inheritDoc} 
     */
    @Override
    public void setFileNumber(byte fileId)
    {
        bits = getOffsetBits();
        bits += (fileId << FILE_SHIFT);
    }

    /** 
     * {@inheritDoc} 
     */
    @Override
    public void setOffset(long _bytes, byte _bits) 
    {
        bytes = _bytes;
        byte fileId = this.getFileNumber();
        bits = _bits;
        bits += (fileId << FILE_SHIFT);
    }
    
    /** 
     * {@inheritDoc} 
     */
    @Override
    public String toString()
    {
        return getDocumentLength() + " F=" + java.util.Arrays.toString(getFieldLengths())+ " " + getNumberOfEntries()  + "@{" + getFileNumber() + "," + getOffset() + "," + getOffsetBits() + "}";
    }

	@Override
	public void readFields(DataInput in) throws IOException {
		basic_readFields(in);
		final int l = fieldLengths.length;
		for(int i=0;i<l;i++)
			fieldLengths[i] = in.readInt();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		basic_write(out);
		for(int field_l : fieldLengths)
			out.writeInt(field_l);
	}

   void basic_readFields(DataInput in) throws IOException {
	   doclength = in.readInt();
	   bytes = in.readLong();
	   bits = in.readByte();
	   entries = in.readInt();
   }

   void basic_write(DataOutput out) throws IOException {
	   out.writeInt(doclength);
	   out.writeLong(bytes);
	   out.writeByte(bits);
	   out.writeInt(entries);
   }
}
