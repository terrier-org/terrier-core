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
 * The Original Code is FieldLexiconEntry.java
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
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.FieldScore;

/** A LexiconEntry with field support */
public class FieldLexiconEntry extends BasicLexiconEntry implements
		FieldEntryStatistics {
	private static final long serialVersionUID = 1L;
	/** 
	 * Factory for a LexiconEntry
	 */
	public static class Factory implements FixedSizeWriteableFactory<LexiconEntry>
	{	
		protected int fieldCount;
		/**
		 * Construct an instance of the class.
		 */
		public Factory()
		{
			this(FieldScore.FIELDS_COUNT); //TODO this is a hack
			System.err.println(this.getClass().getName() + "- default constructor should not be used - fields are " + FieldScore.FIELDS_COUNT);
			//new Exception().printStackTrace(System.err);
			//throw new RuntimeException();
		}
		/**
		 * Construct an instance of the class with
		 * @param _fieldCount
		 */
		public Factory(int _fieldCount)
		{
			this.fieldCount = _fieldCount;
		}
		/**
		 * Construct an instance of the class with
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
			return new FieldLexiconEntry(fieldCount);
		}
	}	
	
	protected int[] fieldFrequencies;
	/**
	 * Construct an instance of the class with
	 * @param fieldCount
	 */
	public FieldLexiconEntry(int fieldCount)
	{
		fieldFrequencies = new int[fieldCount];
	}
	/**
	 * Construct an instance of the class with
	 * @param _fieldFrequencies
	 */
	public FieldLexiconEntry(int[] _fieldFrequencies)
	{
		fieldFrequencies = _fieldFrequencies;
	}
		
	/** {@inheritDoc} */
	public int[] getFieldFrequencies() {
		return fieldFrequencies;
	}
	/** 
	 * Set the frequencies for each field
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

	@Override
	public String toString() {
		return super.toString() + " TFf=" + ArrayUtils.join(this.getFieldFrequencies(), ",");
	}

	@Override
	public void add(EntryStatistics le)
	{
		super.add(le);
		if (le instanceof FieldEntryStatistics)
		{
			FieldEntryStatistics fle = (FieldEntryStatistics)le;
			final int[] fieldTFs = fle.getFieldFrequencies();
			final int l = fieldTFs.length;
			for(int fi=0;fi<l;fi++)
				fieldFrequencies[fi] += fieldTFs[fi];
		}
	}	

}
