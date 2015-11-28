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
 * The Original Code is BasicTermStatsLexiconEntry.java
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
/** A LexiconEntry which only contains EntryStatistics
 * @author Craig Macdonald
 */
public class BasicTermStatsLexiconEntry extends LexiconEntry {
	private static final long serialVersionUID = 1L;
	protected int n_t;
	protected int TF;
	protected int termId;
	/**
	 * Constructs an instance of the BasicTermStatsLexiconEntry.
	 */
	public BasicTermStatsLexiconEntry() {}
	/**
	 * Constructs an instance of the BasicTermStatsLexiconEntry.
	 * @param _TF
	 * @param _n_t
	 * @param _termId
	 */
	public BasicTermStatsLexiconEntry(int _TF, int _n_t, int _termId)
	{
		TF = _TF;
		n_t = _n_t;
		termId = _termId;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public int getDocumentFrequency() {
		return n_t;
	}
	/** 
	 * Sets the document frequency
	 */
	public void setDocumentFrequency(int _n_t) {
		n_t = _n_t;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public int getFrequency() {
		return TF;
	}
	/** 
	 * Sets the frequency for this term
	 */
	public void setFrequency(int _TF) {
		TF = _TF;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public int getTermId() {
		return termId;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void setTermId(int _termId) {
		termId = _termId;
	}
	/** 
	 * Sets the term frequency, document frequency and term id for this term
	 */
	public void setAll(int _TF, int _n_t, int _termId) {
		TF = _TF;
		n_t = _n_t;
		termId = _termId;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public int getNumberOfEntries() {
		return n_t;
	}
	/** 
	 * Get the number of bits for the offset
	 */
	public byte getOffsetBits() {
		return 0;
	}
	/** 
	 * Get the offset (bytes)
	 */
	public long getOffset() {
		return 0;
	}
	/** 
	 * Set the offset in terms of bits and bytes
	 */
	public void setOffset(long bytes, byte bits)
	{
	}
	/** 
	 * Sets the bit index pointer to this LexiconEntry
	 */
	public void setBitIndexPointer(BitIndexPointer pointer) {
		
	}
	/** 
	 * Sets the offset using a BitFilePosition
	 */
	public void setOffset(BitFilePosition pos) {
		
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void readFields(DataInput in) throws IOException {
		TF = in.readInt();
		n_t = in.readInt();
		termId = in.readInt();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void write(DataOutput out) throws IOException {
		out.writeInt(TF);
		out.writeInt(n_t);
		out.writeInt(termId);
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void add(EntryStatistics le) {
		TF += le.getFrequency();
		n_t += le.getDocumentFrequency();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void subtract(EntryStatistics le) {
		this.n_t -= le.getDocumentFrequency();
		this.TF  -= le.getFrequency();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void setNumberOfEntries(int n) {
	}
	/** 
	 * {@inheritDoc} 
	 */
	public String pointerToString() {
		return "";
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void setPointer(Pointer p) {
		return;
	}

	@Override
	public void setStatistics(int _n_t, int _TF) {
		this.n_t = _n_t;
		this.TF = _TF;
	}

}
