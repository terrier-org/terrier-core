/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
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
 * The Original Code is MemoryLexiconEntry.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.terrier.structures.EntryStatistics;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;

/**
 * A lexicon entry stored in a MemoryLexicon. Implements MemoryPointer
 * such that it can be used to look up posting lists in a MemoryInvertedIndex.
 * 
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
@SuppressWarnings("serial")
public class MemoryLexiconEntry extends LexiconEntry implements MemoryPointer,Serializable {

	/*
	 * Lexicon entry data structures.
	 */
	private int termid;
	private int df, tf;

	/**
	 * Constructor.
	 */
	public MemoryLexiconEntry() {
		termid = df = tf = -1;
	}

	/**
	 * Constructor (termid).
	 */
	public MemoryLexiconEntry(int termid) {
		this.termid = termid;
		this.df = this.tf = -1;
	}

	/**
	 * Constructor (df, tf).
	 */
	public MemoryLexiconEntry(int df, int tf) {
		this.termid = -1;
		this.df = df;
		this.tf = tf;
	}

	/**
	 * Constructor (termid, df, tf).
	 */
	public MemoryLexiconEntry(int termid, int df, int tf) {
		this.termid = termid;
		this.df = df;
		this.tf = tf;
	}

	/** {@inheritDoc} */
	public int getTermId() {
		return termid;
	}

	/** {@inheritDoc} */
	public void setTermId(int termid) {
		this.termid = termid;
	}

	/** {@inheritDoc} */
	public int getDocumentFrequency() {
		return df;
	}

	/** {@inheritDoc} */
	public int getFrequency() {
		return tf;
	}

	/** {@inheritDoc} */
	public void setStatistics(int df, int tf) {
		this.df = df;
		this.tf = tf;
	}

	/** {@inheritDoc} */
	public void add(EntryStatistics es) {
		df += es.getDocumentFrequency();
		tf += es.getFrequency();
	}

	/** {@inheritDoc} */
	public void subtract(EntryStatistics es) {
		df -= es.getDocumentFrequency();
		tf -= es.getFrequency();
	}

	/** {@inheritDoc} */
	public int getNumberOfEntries() {
		return getDocumentFrequency();
	}

	/** {@inheritDoc} */
	public void setNumberOfEntries(int df) {
		this.df = df;
	}

	/** Get pointer value (termid). */
	public int getPointer() {
		return getTermId();
	}

	/** {@inheritDoc} */
	public void setPointer(Pointer p) {
		termid = Integer.parseInt(p.pointerToString());
		df = p.getNumberOfEntries();
	}

	/** {@inheritDoc} */
	public String pointerToString() {
		return String.valueOf(termid);
	}

	/** Not implemented. */
	public void readFields(DataInput arg0) throws IOException {
	}

	/** Not implemented. */
	public void write(DataOutput out) throws IOException {
	}

	public MemoryLexiconEntry clone() {
		MemoryLexiconEntry mle = new MemoryLexiconEntry(df,tf);
		mle.setTermId(termid);
		return mle;
	}
	
}
