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
 * The Original Code is BasicPostingImpl.java
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
/** Implementation of a Posting that is non-iterable.
 * @since 3.0
 * @author Craig Macdonald
 */
public class BasicPostingImpl implements WritablePosting {

	private static final long serialVersionUID = 1L;
	protected int dl = 0;
	
	/** id of the posting */
	protected int id = -1;
	/** frequency of this posting */
	protected int tf;

	/** Empty constructor - needed for Writable */
	public BasicPostingImpl() {
		super();
	}

	/** Constructor that sets both id and frequency */
	public BasicPostingImpl(int docid, int frequency) {
		super();
		id = docid;
		tf = frequency;
	}

	/** {@inheritDoc} */
	public final int getId() {
		return id;
	}

	/** {@inheritDoc} */
	public final int getFrequency() {
		return tf;
	}

	/** Reads the a single posting (not an iterable posting - use BitPostingIndex for that) */
	public void readFields(DataInput in) throws IOException {
		id = WritableUtils.readVInt(in);
		tf = WritableUtils.readVInt(in);
	}

	/** Writes the current posting (not an iterable posting - use DirectInvertedOutputStream for that).
	 * Compression using this method is not expected to be comparable to bit-level compression. */
	public void write(DataOutput out) throws IOException {
		WritableUtils.writeVInt(out, id);
		WritableUtils.writeVInt(out, tf);
	}
	
	/** Set the id of this posting */
	public void setId(int _id) {
		id = _id;
	}

	/** Returns 0 */
	public int getDocumentLength() {
		return dl;
	}
	
	/** {@inheritDoc} */
	public WritablePosting asWritablePosting()
	{
		return new BasicPostingImpl(id, tf);
	}
	
	/** Makes a human readable form of this posting */
	public String toString()
	{
		return "(" + id + "," + tf + ")";
	}

	@Override
	public void setDocumentLength(int l) {
		dl = l;
	}

	/**
	 * Set the term frequency in the document
	 * @param tf
	 */
	public void setTf(int tf) {
		this.tf = tf;
	}

}