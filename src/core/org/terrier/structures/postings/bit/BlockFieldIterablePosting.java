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
 * The Original Code is BlockFieldIterablePosting.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
/**
 * 
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
import org.terrier.structures.postings.BlockFieldPostingImpl;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.utility.ArrayUtils;
/** class BlockFieldIterablePosting */
@SuppressWarnings("serial")
public class BlockFieldIterablePosting extends BasicIterablePosting implements BlockPosting, FieldPosting
{
	
	int[] positions;
	final int fieldCount;
	final int[] fieldFrequencies;
	final boolean doiIsFieldDocumentIndex;
	final FieldDocumentIndex fdoi;
	
	/** Make a new posting that can be loaded using Writable methods
	 * @param _fieldCount number of fields to expect */
	public BlockFieldIterablePosting(int _fieldCount){
		super();
		this.fieldCount = _fieldCount;
		this.fieldFrequencies = new int[_fieldCount];
		this.doiIsFieldDocumentIndex = false;
		this.fdoi = null;
	}
	
	/** Make a new posting iterator that is read from a BitIn stream.
	 * @param _bitFileReader BitIn stream containing postings
	 * @param _numEntries number of postings to read from stream
	 * @param doi DocumentIndex to read document and field lengths from
	 * @param _fieldCount number of fields to expect 
	 */
	public BlockFieldIterablePosting(BitIn _bitFileReader, int _numEntries, DocumentIndex doi, int _fieldCount) throws IOException {
		super(_bitFileReader, _numEntries, doi);
		this.fieldCount = _fieldCount;
		this.fieldFrequencies = new int[_fieldCount];
		if (doiIsFieldDocumentIndex = doi instanceof FieldDocumentIndex)
		{
			fdoi = (FieldDocumentIndex)super.doi;
		} else {
			fdoi = null;
		}
	}
	
	/** {@inheritDoc} */
	public int next() throws IOException {
		if (numEntries-- <= 0)
			return EOL;
		id = bitFileReader.readGamma() + id;
		tf = bitFileReader.readUnary();
		for(int i = 0;i<fieldCount;i++)
		{
			fieldFrequencies[i] = bitFileReader.readUnary()-1;
		}
		//TODO: this has a memory allocation for every posting in the posting list. can we reuse an array?
		positions = new int[bitFileReader.readUnary() -1];
		if (positions.length == 0)
			return id;
		positions[0] = bitFileReader.readGamma() -1;
		for(int i=1;i<positions.length;i++)
			positions[i] = positions[i-1] + bitFileReader.readGamma();
		return id;
	}
	
	/** {@inheritDoc} */
	public int[] getPositions() {
		return positions;
	}
	
	/** {@inheritDoc} */
	public int[] getFieldFrequencies() {
		return fieldFrequencies;
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

	/** {@inheritDoc} */
	@Override
	public void readFields(DataInput in) throws IOException {
		super.readFields(in);
		final int blockCount = WritableUtils.readVInt(in);
		final int l = in.readInt();
		for(int i=0;i<l;i++)
			fieldFrequencies[i] = in.readInt();
		positions = new int[blockCount]; 
		for(int i=0;i<blockCount;i++)
			positions[i] = WritableUtils.readVInt(in);
	}

	/** {@inheritDoc} */
	@Override
	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeInt(fieldFrequencies.length);
		for(int field_f : fieldFrequencies)
			out.writeInt(field_f);
		WritableUtils.writeVInt(out, positions.length);
		for(int pos : positions)
			WritableUtils.writeVInt(out, pos);
	}

	/** {@inheritDoc} */
	@Override
	public WritablePosting asWritablePosting() {
		BlockFieldPostingImpl bfpi = new BlockFieldPostingImpl(id, tf, positions, fieldCount);
		System.arraycopy(fieldFrequencies, 0, bfpi.getFieldFrequencies(), 0, fieldCount);
		return bfpi;
	}

	/** Makes a human readable form of this posting */
	@Override
	public String toString()
	{
		return "(" + id + "," + tf + ",F[" + ArrayUtils.join(fieldFrequencies, ",")
			+ "],B[" + ArrayUtils.join(positions, ",") + "])";
	}
}