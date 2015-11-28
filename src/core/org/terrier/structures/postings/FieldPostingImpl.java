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
 * The Original Code is FieldPostingImpl.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.postings;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableUtils;

import org.terrier.utility.ArrayUtils;
/** 
 * Implementation of a posting containing fields
 */
public class FieldPostingImpl extends BasicPostingImpl implements FieldPosting {
	private static final long serialVersionUID = 1L;
	int[] fieldFrequencies;	
	int[] fieldLengths;
	/**
	 * default constructor
	 */
	public FieldPostingImpl() {}
	/**
	 * constructor
	 * @param id
	 * @param tf
	 * @param _fieldCount
	 */
	public FieldPostingImpl(int id, int tf, int _fieldCount)
	{
		super(id,tf);
		fieldFrequencies = new int[_fieldCount];
		//fieldLengths = new int[_fieldCount];
	}
	/**
	 * constructor
	 * @param id
	 * @param tf
	 * @param _fieldFrequencies
	 */
	public FieldPostingImpl(int id, int tf, int[] _fieldFrequencies)
	{
		super(id,tf);
		this.fieldFrequencies = _fieldFrequencies;
	}
	/**
	 * constructor
	 * @param _fieldFrequencies
	 */
	public FieldPostingImpl(int[] _fieldFrequencies)
	{
		fieldFrequencies = _fieldFrequencies;
	}
	/**
	 * constructor
	 * @param _fieldCount
	 */
	public FieldPostingImpl(int _fieldCount)
	{
		fieldFrequencies = new int[_fieldCount];
	}
	
	/** {@inheritDoc} */
	public int[] getFieldFrequencies() {
		return fieldFrequencies;
	}
	
	/** {@inheritDoc} */
	public int[] getFieldLengths() {
		return fieldLengths;
	}
	
	@Override
	public void setFieldLengths(int[] fl) {
		fieldLengths = fl;
	}
	
	/** {@inheritDoc} */
	@Override
	public void readFields(DataInput in) throws IOException {
		super.readFields(in);
		final int l = WritableUtils.readVInt(in);
		fieldFrequencies = new int[l];
		for(int i=0;i<l;i++)
			fieldFrequencies[i] = WritableUtils.readVInt(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		super.write(out);
		WritableUtils.writeVInt(out, fieldFrequencies.length);
		for(int field_f : fieldFrequencies)
			WritableUtils.writeVInt(out, field_f);
	}
	
	/** {@inheritDoc} */
	@Override
	public WritablePosting asWritablePosting()
	{	
		FieldPostingImpl fbp = new FieldPostingImpl(fieldFrequencies.length);
		fbp.id = id;
		fbp.tf = tf;
		System.arraycopy(fieldFrequencies, 0, fbp.fieldFrequencies, 0, fieldFrequencies.length);
		return fbp;
	}
	
	/** {@inheritDoc} */
	public String toString()
	{
		return "(" + id + "," + tf + ",F[" + ArrayUtils.join(fieldFrequencies, ",") + "])";
	}
}
