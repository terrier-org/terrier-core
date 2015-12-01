/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.uk
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
 * The Original Code is PostingInRun.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.indexing.singlepass;

import java.io.IOException;

import org.terrier.compression.bit.BitIn;
import org.terrier.compression.bit.BitOut;
import org.terrier.structures.BasicLexiconEntry;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.postings.IterablePosting;

/** Base class for PostingInRun classes */
public abstract class PostingInRun {

	/** source for postings to be read from */
	protected BitIn postingSource;
	/** tf for the current posting */
	protected int termTF;
	/** Current term */
	protected String term;
	/** Document frequency */
	protected int termDf;
	/** 
	 * Constructs an instance of the PostingInRun.
	 */
	public PostingInRun() {
		super();
	}
	
	/** Return the LexiconEntry for this Posting */
	public LexiconEntry getLexiconEntry()
	{
		return new BasicLexiconEntry(0, termDf, termTF);
	}
	
	/** Add statistics for this posting onto the given LexiconEntry */
	public void addToLexiconEntry(LexiconEntry le)
	{
		le.setStatistics(le.getDocumentFrequency() + termDf, le.getFrequency() + termTF);
	}
	
	/**
	 * @return the document frequency for the term.
	 */
	public int getDf() {
		return termDf;
	}

	/**
	 * Setter for the document frequency.
	 * @param df int with the new document frequency.
	 */
	public void setDf(int df) {
		this.termDf = df;
	}

	/**
	 * @return The term String in this posting list.
	 */
	public String getTerm() {
		return term;
	}

	/**
	 * Setter for the term.
	 * @param _term String containing the term for this posting list.
	 */
	public void setTerm(String _term) {
		this.term = _term;
	}

	/**
	 * @return the term frequency.
	 */
	public int getTF() {
		return termTF;
	}

	/**
	 * Setter for the term frequency.
	 * @param tf the new term frequency.
	 */
	public void setTF(int tf) {
		this.termTF = tf;
	}

	/** Set where the postings should be read from */
	public void setPostingSource(BitIn source) {
		postingSource = source;
	}

	/**
	 * Writes the document data of this posting to a {@link org.terrier.compression.bit.BitOut} 
	 * It encodes the data with the right compression methods.
	 * The stream is written as <code>d1, idf(d1) , d2 - d1, idf(d2)</code> etc.
	 * @param bos BitOut to be written.
	 * @param last int representing the last document written in this posting.
	 * @return The last posting written.
	 */
	public abstract int append(BitOut bos, int last, int runShift) throws IOException;

	/**
	 * Writes the document data of this posting to a {@link org.terrier.compression.bit.BitOut} 
	 * It encodes the data with the right compression methods.
	 * The stream is written as <code>d1, idf(d1) , d2 - d1, idf(d2)</code> etc.
	 * @param bos BitOut to be written.
	 * @param last int representing the last document written in this posting.
	 * @return The last posting written.
	 */
	public int append(BitOut bos, int last) throws IOException {
		return append(bos, last, 0);
	}

	/** Returns an IterablePosting object for the postings in this run */
	public abstract IterablePosting getPostingIterator(int runShift) throws IOException;
}