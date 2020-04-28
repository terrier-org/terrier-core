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
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;
import org.apache.hadoop.io.Writable;

/** 
 * Base class for all LexiconEntry implementations. A LexiconEntry
 * represents the statistics of a term in the {@link Lexicon}, and
 * a pointer to the term's location in a {@link PostingIndex}. For
 * these reasons, this class implements {@link Pointer} and {@link EntryStatistics}.
 * 
 * @see Lexicon
 * @see Pointer
 * @see EntryStatistics
 *
 * @author Craig Macdonald
 */
//TODO [NIC]: This class can easily transformed into an interface with default methods. 
//            Javadoc must be seriously improved.
public abstract class LexiconEntry implements EntryStatistics, Pointer, Writable
{
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc} 
     */
    @Override
    public String toString()
    {
        return '(' + getDocumentFrequency() + "," + getFrequency() + ')' + pointerToString();
    }

    /** 
     * Set the term ID, the integer representation of the term in the index,
     * e.g. as used in direct index posting structures.
     */
    public abstract void setTermId(int newTermId);

    /** 
     * Update the document frequency and term frequency
     */
    public abstract void setStatistics(int n_t, int TF);
   
    /** Pointer implementation: how many entries in the inverted index.
     * Usually the same as getDocumentFrequency().
     */
    @Override
    public int getNumberOfEntries() 
    {
        return 0;
    }
    
    /** Update the number of entries in the pointer */
    @Override
    public void setNumberOfEntries(int n) 
    {
    }
    
    @Override
    public String pointerToString() 
    {
        return null;
    }
    
    /** Update the pointer */
    @Override
    public void setPointer(Pointer p) 
    {
    }
    
    /** Get a writable copy of the EntryStatistics. Just returns itself. */
    @Override
    public EntryStatistics getWritableEntryStatistics() 
    {
        return this;
    }
    
    /** Does this refer to the same term */
    @Override
    public boolean equals(Object obj) 
    {
        if (! (obj instanceof LexiconEntry))
            return false;
        LexiconEntry o = (LexiconEntry)obj;
        return o.getTermId() == this.getTermId();
    }

    /** hash this object based on termid */
    @Override
    public int hashCode() 
    {
        return this.getTermId();
    }
}