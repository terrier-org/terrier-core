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
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
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

/** Abstract Lexicon implementation. The Lexicon class typically represents
 * the list of terms (dictionary) in the index, together with their statistics
 * (see {@link EntryStatistics}) and the pointer ({@link Pointer}) to the 
 * offset of that term's postings in the {@link PostingIndex} returned by {@link Index#getInvertedIndex()}. The 
 * {@link EntryStatistics} and {@link Pointer} are combined in a single
 * {@link LexiconEntry} object.
 * 
 * @author Craig Macdonald
 * @since 1.0.0
 * @param <KEY> the type of the key. Usually String to denote a term.
 * @see EntryStatistics
 * @see Pointer
 */
public abstract class Lexicon<KEY> implements Closeable, Iterable<Map.Entry<KEY,LexiconEntry>>
{
    static class LexiconFileEntry<KEY2> implements Map.Entry<KEY2,LexiconEntry>
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

    /** Return the number of entries in the lexicon */
    public abstract int numberOfEntries();
    
    /** Get the LexiconEntry (contains statistics and a pointer) for the given term.
     * Returns null if the term is not found in the lexicon.
     * @param term key to lookup the lexicon with
     * @return LexiconEntry for that term, or null if not found
     */
    public abstract LexiconEntry getLexiconEntry(KEY term);
    /** Get the term and LexiconEntry (contains statistics and a pointer) for
     * a given term id.
     * Throws NoSuchElementException is the termid is not found.
     * @param termid the term id to lookup in the lexicon
     * @return Map.Entry tuple containing the term and the LexiconEntry

     */
    public abstract Map.Entry<KEY,LexiconEntry> getLexiconEntry(int termid);
    /** Get the term and LexiconEntry (contains statistics and a pointer) for
     * the entry in the lexicon with the specified index.
     * Throws NoSuchElementException is the termid is not found.
     * @param index the entry number to lookup in the lexicon
     * @return Map.Entry tuple containing the term and the LexiconEntry
     */
    public abstract Map.Entry<KEY,LexiconEntry> getIthLexiconEntry(int index);
    
    /** Returns an iterator over a set of LexiconEntries within a range of
     * entries in the lexicon.
     * @param from low endpoint term in the subset, inclusive.
     * @param to high endpoint term in the subset, exclusive.
     * @return Iterator over the set.
     */
    public abstract Iterator<Map.Entry<KEY,LexiconEntry>> getLexiconEntryRange(KEY from, KEY to);
}
