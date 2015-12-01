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
 * The Original Code is LexiconOutputStream.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author) 
 */
package org.terrier.structures;
import java.io.Closeable;
import java.io.DataOutput;
import java.io.IOException;
/**
 * This class implements an output stream for the lexicon structure.
 * @author Vassilis Plachouras &amp; Craig Macdonald
 * @param <KEY>
 */
public abstract class LexiconOutputStream<KEY> implements Closeable {
	
	/** A data input stream to read from the bufferInput.*/
	protected DataOutput lexiconStream = null;
	/** Pointer written - the sum of the Nts */
	protected long numPointersWritten = 0;
	/** collection length - the sum of the TFs */
	protected long numTokensWritten = 0;
	protected int numTermsWritten = 0;

	protected LexiconOutputStream() { }


	/**
	 * Closes the lexicon stream.
	 * IOException if an I/O error occurs while closing the stream.
	 */
	public void close() {
		try{
			if (lexiconStream instanceof java.io.Closeable)
				((java.io.Closeable)lexiconStream).close();
		} catch (IOException ioe) {}
	}
	/**
	 * Writes a lexicon entry.
	 * @return the number of bytes written to the file. 
	 * @throws java.io.IOException if an I/O error occurs
	 * @param _key the key - usually the term
	 * @param _value the lexicon entry value
	 */
	public abstract int writeNextEntry(KEY _key, LexiconEntry _value) throws IOException;
	
	protected void incrementCounters(EntryStatistics t)
	{
		numTermsWritten++;
		numPointersWritten += t.getDocumentFrequency();
		numTokensWritten += t.getFrequency();
	}

	/** Returns the number of pointers there would be in an inverted index built using this lexicon (thus far).
	  * This is equal to the sum of the Nts written to this lexicon output stream. */
	public long getNumberOfPointersWritten()
	{
		return numPointersWritten;
	}

	/** Returns the number of tokens there are in the entire collection represented by this lexicon (thus far).
	  * This is equal to the sum of the TFs written to this lexicon output stream. */
	public long getNumberOfTokensWritten()
	{
		return numTokensWritten;
	}

	/** Returns the number of terms written so far by this LexiconOutputStream */
	public int getNumberOfTermsWritten()
	{
		return numTermsWritten;
	}
}
