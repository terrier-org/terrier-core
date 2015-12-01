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
 * The Original Code is MultiLexiconEntry.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.multi;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.terrier.structures.EntryStatistics;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;

/**
 * A Lexicon entry that spans multiple index shards. It wraps around multiple
 * lexicon entries from different index shards.
 * 
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
@SuppressWarnings("serial")
public class MultiLexiconEntry extends LexiconEntry {

	private LexiconEntry[] children;

	/**
	 * Constructor.
	 */
	public MultiLexiconEntry(LexiconEntry[] le) {
		this.children = le;
	}

	/**
	 * Return LexiconEntry's.
	 */
	public LexiconEntry[] getChildren() {
		return children;
	}

	/** {@inheritDoc} */
	public int getFrequency() {
		int freq = 0;
		for (LexiconEntry le : children)
			if (le != null)
				freq += le.getFrequency();
		return freq;
	}

	/** {@inheritDoc} */
	public int getDocumentFrequency() {
		int docFreq = 0;
		for (LexiconEntry le : children)
			if (le != null)
				docFreq += le.getDocumentFrequency();
		return docFreq;
	}

	/** {@inheritDoc} */
	public int getNumberOfEntries() {
		int entries = 0;
		for (LexiconEntry le : children)
			if (le != null)
				entries += le.getNumberOfEntries();
		return entries;
	}

	/** Not implemented. */
	public int getTermId() {
		return -1;
	}

	/** Not implemented. */
	public void add(EntryStatistics e) {
	}

	/** Not implemented. */
	public void subtract(EntryStatistics e) {
	}

	/** Not implemented. */
	public void setNumberOfEntries(int n) {
	}

	/** Not implemented. */
	public String pointerToString() {
		return null;
	}

	/** Not implemented. */
	public void setPointer(Pointer p) {
	}

	/** Not implemented. */
	public void readFields(DataInput arg0) throws IOException {
	}

	/** Not implemented. */
	public void write(DataOutput arg0) throws IOException {
	}

	/** Not implemented. */
	public void setTermId(int newTermId) {
	}

	/** Not implemented. */
	public void setStatistics(int n_t, int TF) {
	}

}
