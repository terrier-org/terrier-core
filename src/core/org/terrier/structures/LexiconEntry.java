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
 * The Original Code is LexiconEntry.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;
import org.apache.hadoop.io.Writable;

/** Represents the statistics of a term in the {@link Lexicon}, and
 * a pointer to the term's location in a {@link PostingIndex}. For
 * these reasons, this class implements {@link Pointer} and {@link EntryStatistics}.
 * @see Lexicon
 * @see Pointer
 * @see EntryStatistics
 * @author Craig Macdonald
 */
public abstract class LexiconEntry implements EntryStatistics, Pointer, Writable
{
	private static final long serialVersionUID = 1L;
	/** 
	 * {@inheritDoc} 
	 */
    public String toString()
	{
        return '('+getDocumentFrequency()+","+getFrequency()+')'
            + pointerToString();
	}
	/** 
	 * Set the term ID
	 */
    public abstract void setTermId(int newTermId);
	/** 
	 * Set the document frequency and term frequency
	 */
    public abstract void setStatistics(int n_t, int TF);
   
	@Override
	public int getNumberOfEntries() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void setNumberOfEntries(int n) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String pointerToString() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setPointer(Pointer p) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public EntryStatistics getWritableEntryStatistics() {
		return this;
	}
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof LexiconEntry))
			return false;
		LexiconEntry o = (LexiconEntry)obj;
		return o.getTermId() == this.getTermId();
	}

	@Override
	public int hashCode() {
		return this.getTermId();
	}
}
