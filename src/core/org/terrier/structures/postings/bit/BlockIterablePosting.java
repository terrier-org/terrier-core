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
 * The Original Code is BlockIterablePosting.java
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
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.BlockPostingImpl;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.utility.ArrayUtils;
/** 
 * A writable block iterable posting list
 */
public class BlockIterablePosting extends BasicIterablePosting implements BlockPosting
{
	private static final long serialVersionUID = 1L;
	int[] positions;
	/**
	 * Constructs an instance of the BlockIterablePosting.
	 */
	public BlockIterablePosting(){super();}
	/** 
	 * Constructs an instance of the BlockIterablePosting.
	 * @param _bitFileReader
	 * @param _numEntries
	 * @param doi
	 * @throws IOException
	 */
	public BlockIterablePosting(BitIn _bitFileReader, int _numEntries, DocumentIndex doi) throws IOException {
		super(_bitFileReader, _numEntries, doi);
	}
	/** 
	 * {@inheritDoc} 
	 */
	public int next() throws IOException {
		if (numEntries-- <= 0)
			return EOL;
		id = bitFileReader.readGamma() + id;
		tf = bitFileReader.readUnary();
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

	@Override
	public void readFields(DataInput in) throws IOException {
		super.readFields(in);
		final int blockCount = WritableUtils.readVInt(in);
		positions = new int[blockCount]; 
		for(int i=0;i<blockCount;i++)
			positions[i] = WritableUtils.readVInt(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		super.write(out);
		WritableUtils.writeVInt(out, positions.length);
		for(int pos : positions)
			WritableUtils.writeVInt(out, pos);
	}

	@Override
	public WritablePosting asWritablePosting() {
		int[] newPositions = new int[positions.length];
		System.arraycopy(positions, 0, newPositions, 0, positions.length);
		return new BlockPostingImpl(getId(), getFrequency(), newPositions);
	}

	@Override
	public String toString()
	{
		return "(" + id + "," + tf + ",B[" + ArrayUtils.join(positions, ",") + "])";
	}
}