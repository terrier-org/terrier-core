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
 * The Original Code is BlockFieldLexiconEntry.java
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

import org.slf4j.LoggerFactory;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;

/** A LexiconEntry with field support */
public class BlockFieldLexiconEntry extends BasicLexiconEntry implements
		FieldEntryStatistics, BlockEntryStatistics {
	private static final long serialVersionUID = 1L;
	/** 
	 * Factory for creating LexiconEntries
	 */
	public static class Factory implements FixedSizeWriteableFactory<LexiconEntry>
	{	
		protected int fieldCount;
		
		/**
		 * Constructs an instance of the class with
		 * @param _fieldCount
		 */
		public Factory(int _fieldCount)
		{
			this.fieldCount = _fieldCount;
			LoggerFactory.getLogger(Factory.class).warn(BlockFieldLexiconEntry.class.getSimpleName() + " has now been deprecated and will be removed in a future release");
			
		}
		/**
		 * Constructs an instance of the class with
		 * @param _fieldCount
		 */
		public Factory(String _fieldCount)
		{
			this(Integer.parseInt(_fieldCount));
		}
		/** 
		 * {@inheritDoc} 
		 */
		public int getSize() {
			return (3*4) + 8 + 1 + this.fieldCount * 4;
		}
		/** 
		 * {@inheritDoc} 
		 */
		public LexiconEntry newInstance() {
			return new BlockFieldLexiconEntry(fieldCount);
		}
	}	
	
	protected final int[] fieldFrequencies;
	protected int blockCount;
	/**
	 * Constructs an instance of the class with
	 * @param fieldCount
	 */
	public BlockFieldLexiconEntry(int fieldCount)
	{
		fieldFrequencies = new int[fieldCount];
	}
	/**
	 * Construct an instance of the class with
	 * @param _fieldFrequencies
	 * @param _blockCount
	 */
	public BlockFieldLexiconEntry(int[] _fieldFrequencies, int _blockCount)
	{
		fieldFrequencies = _fieldFrequencies;
		blockCount = _blockCount;
	}
	
	/** {@inheritDoc} */
	public int[] getFieldFrequencies() {
		return fieldFrequencies;
	}
	/** 
	 * Sets the frequencies for each field
	 */
	public void setFieldFrequencies(int[] _fieldFrequencices) {
		System.arraycopy(_fieldFrequencices, 0, fieldFrequencies, 0, fieldFrequencies.length);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		super.readFields(in);
		final int l = fieldFrequencies.length;
		for(int i=0;i<l;i++)
			fieldFrequencies[i] = in.readInt();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		super.write(out);
		for(int field_f : fieldFrequencies)
			out.writeInt(field_f);
	}

	/** {@inheritDoc} */
	public int getBlockCount()
	{
		return blockCount;
	}	

}
