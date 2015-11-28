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
 * The Original Code is FieldIterablePosting.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.postings.bit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableUtils;

import org.terrier.compression.bit.BitIn;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.FieldDocumentIndex;
import org.terrier.structures.FieldDocumentIndexEntry;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.FieldPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.utility.ArrayUtils;

/** A posting iterator for field postings.
 * @see Posting
 * @see IterablePosting
 * @see FieldPosting
 * @since 3.0
 * @author Craig Macdonald
 */
public class FieldIterablePosting extends BasicIterablePosting implements FieldPosting {

	private static final long serialVersionUID = 1L;
	/** number of fields */
	final int fieldCount;
	/** frequency in each field */
	final int[] fieldFrequencies;
	/** can we lookup fields lengths in a cheap manner? */
	final boolean doiIsFieldDocumentIndex;
	/** pre-cast DocumentIndex to FieldDocumentIndex */
	final FieldDocumentIndex fdoi;
	/**
	 * contructor
	 * @param _fieldCount
	 */
	public FieldIterablePosting(int _fieldCount) {
		super();
		this.fieldCount = _fieldCount;
		this.fieldFrequencies = new int[_fieldCount];
		this.doiIsFieldDocumentIndex = false;
		this.fdoi = null;
	}

	/**
	 * constructor
	 * @param fileReader
	 * @param entries
	 * @param _doi
	 * @param _fieldCount
	 * @throws IOException
	 */
	public FieldIterablePosting(BitIn fileReader, int entries, DocumentIndex _doi, int _fieldCount) throws IOException {
		super(fileReader, entries, _doi);
		this.fieldCount = _fieldCount;
		this.fieldFrequencies = new int[_fieldCount];
		if (doiIsFieldDocumentIndex = _doi instanceof FieldDocumentIndex)
		{
			fdoi = (FieldDocumentIndex)super.doi;
		} else {
			fdoi = null;
		}
	}

	/** {@inheritDoc} */
	public int[] getFieldFrequencies() {
		return fieldFrequencies;
	}

	/** {@inheritDoc} */
	@Override
	public int next() throws IOException {
		if (numEntries-- <= 0)
			return EOL;
		id = bitFileReader.readGamma() + id;
		tf = bitFileReader.readUnary();
		for(int i = 0;i<fieldCount;i++)
		{
			fieldFrequencies[i] = bitFileReader.readUnary()-1;
		}
		return id;
	}
	
	/** {@inheritDoc}.
	 * This operation is unsupported. */
	@Override
	public void setFieldLengths(int[] fl) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	public int[] getFieldLengths() {
		if (doiIsFieldDocumentIndex)
		{
			try{
				return fdoi.getFieldLengths(id);
			} catch (IOException ioe) {
				System.err.println("Problem looking for doclength for document "+ id);
				ioe.printStackTrace();
				return new int[0];
			}
		}
		else
		{
			FieldDocumentIndexEntry fdie = null;
			try{
				fdie = ((FieldDocumentIndexEntry)doi.getDocumentEntry(id));
			} catch (IOException ioe) {
				//TODO log?
				System.err.println("Problem looking for doclength for document "+ id);
				ioe.printStackTrace();
				return new int[0];
			}
			return fdie.getFieldLengths();
		}
	}

	/** Read this posting from specified inputstream */
	@Override
	public void readFields(DataInput in) throws IOException {
		super.readFields(in);
		final int l = WritableUtils.readVInt(in);
		//fieldFrequencies = new int[l];
		for(int i=0;i<l;i++)
			fieldFrequencies[i] = WritableUtils.readVInt(in);
	}

	/** Write this posting to specified outputstream */
	@Override
	public void write(DataOutput out) throws IOException {
		super.write(out);
		WritableUtils.writeVInt(out, fieldFrequencies.length);
		for(int field_f : fieldFrequencies)
			WritableUtils.writeVInt(out, field_f);
	}
	

	/** Get this posting as a WritablePosting */
	@Override
	public WritablePosting asWritablePosting()
	{	
		FieldPostingImpl fbp = new FieldPostingImpl(id, tf, fieldCount);
		System.arraycopy(fieldFrequencies, 0, fbp.getFieldFrequencies(), 0, fieldCount);
		return fbp;
	}

	@Override
	public String toString()
	{
		return "(" + id + "," + tf + ",F[" + ArrayUtils.join(fieldFrequencies, ",") + "])";
	}
}
