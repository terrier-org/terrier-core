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
 * The Original Code is Lexicon.java
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original contributor)
  *   Ben He <ben{a.}dcs.gla.ac.uk>
  *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures;
import java.io.Closeable;
import java.util.Iterator;
import java.util.Map;

/** 
 * A lexicon (a.k.a. dictionary, vocabulary) typically represents the list of terms
 * in the index, together with their statistics (see {@link EntryStatistics}) and 
 * the pointer ({@link Pointer}) to the offset of that term's postings in the 
 * {@link PostingIndex} returned by {@link Index#getInvertedIndex()}. 
 * The {@link EntryStatistics} and {@link Pointer} are combined in a single
 * {@link LexiconEntry} object.
 * 
 * This class is an abstract class implementing a lexicon.
 * @param <KEY> the type of the key. Usually String to denote a term.
 * 
 * @see EntryStatistics
 * @see Pointer

 * @author Craig Macdonald
 * @since 1.0.0
 */
public abstract class Lexicon<KEY> implements Closeable, Iterable<Map.Entry<KEY,LexiconEntry>>
{
    // TODO [NIC]: Should be not exposed. It is mainly used in Lexicon builder for its own stuff and some other classes.
    public static class LexiconFileEntry<KEY2> implements Map.Entry<KEY2,LexiconEntry>
    {
        KEY2 key;
        LexiconEntry value;
                
        public LexiconFileEntry(KEY2 k, LexiconEntry v)
        {
            this.key = k;
            this.value = v;
        }
            
        public int hashCode()
        {
            LexiconFileEntry<KEY2> e = this;
            return (e.getKey()==null   ? 0 : e.getKey().hashCode()) ^
                (e.getValue()==null ? 0 : e.getValue().hashCode());
        }

        public LexiconEntry setValue(LexiconEntry v)
        {
            LexiconEntry old = value;
            value = v;
            return old;
        }

        public KEY2 getKey()
        {
            return key;
        }

        public LexiconEntry getValue()
        {
            return value;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public boolean equals(Object o)
        {
            if (! (o instanceof Map.Entry))
                    return false;
            LexiconFileEntry e1 = this;
            Map.Entry<String,LexiconEntry> e2 = (Map.Entry)o;
            return (e1.getKey()==null ?
              e2.getKey()==null : e1.getKey().equals(e2.getKey()))  &&
             (e1.getValue()==null ?
              e2.getValue()==null : e1.getValue().equals(e2.getValue()));
        }
    }

    /** 
     * Return the number of terms in the lexicon.
     * 
     * @return the number of terms in the lexicon.
     */
    public abstract int numberOfEntries();
    
    /** 
     * Returns the {@link LexiconEntry} (containing statistics and a pointer) for the given term.
     * Returns <code>null</code> if the term is not present in the lexicon.
     * 
     * @param term the key to lookup the lexicon with
     * @return LexiconEntry for that term, or <code>null</code> 
     *         if the term is not present in the lexicon.
     */
    public abstract LexiconEntry getLexiconEntry(KEY term);
    
    // TODO [NIC]: Should not throw an exception, as the previous method does not throw any exception but returns null.
    //             Both behaviours should be consistent.
    //             Moreover, throwing an exception not specified in the signature is an implementation detail.
    //             This comment applies to all other following methods.
    /** 
     * Returns the term and {@link LexiconEntry} (containing statistics and a pointer) for the given term id.
     * 
     * Throws NoSuchElementException is the termid is not found. TODO [NIC]: check nic comment above
     * 
     * @param termid the term id to lookup in the lexicon.
     * @return the {@link java.util.Map.Entry} containing the term and the {@link LexiconEntry}.

     */
    public abstract Map.Entry<KEY,LexiconEntry> getLexiconEntry(int termid);
    
    /** 
     * Returns the term and {@link LexiconEntry} (containing statistics and a pointer) for
     * the entry in the lexicon with the specified index.
     * 
     * Throws NoSuchElementException is the termid is not found. TODO [NIC]: check nic comment above
     * 
     * @param index the entry number to lookup in the lexicon.
     * @return the {@link java.util.Map.Entry} containing the term and the {@link LexiconEntry}.
     */
    public abstract Map.Entry<KEY,LexiconEntry> getIthLexiconEntry(int index);
    
    /** 
     * Returns an iterator over a set of LexiconEntries within a range of
     * entries in the lexicon.
     * 
     * @param from low endpoint term in the subset, inclusive.
     * @param to high endpoint term in the subset, exclusive.
     * 
     * @return a {@link java.util.Iterator} over the set of {@link java.util.Map.Entry}s.
     */
    public abstract Iterator<Map.Entry<KEY,LexiconEntry>> getLexiconEntryRange(KEY from, KEY to);
}
