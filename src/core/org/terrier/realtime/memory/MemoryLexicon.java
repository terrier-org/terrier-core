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
 * The Original Code is MemoryLexicon.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory;

import gnu.trove.TIntObjectHashMap;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.Text;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.MapLexicon;
import org.terrier.structures.collections.MapEntry;
import org.terrier.structures.collections.OrderedMap;
import org.terrier.structures.seralization.FixedSizeTextFactory;
import org.terrier.utility.ApplicationSetup;

/**
 * The lexicon structure for a MemoryIndex. Since this is a memory structure,
 * the lexicon entries are of type MemoryPointers rather than BitIndexPointer.
 * 
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class MemoryLexicon extends MapLexicon implements Serializable {

	private static final long serialVersionUID = 6642638617614776293L;

	/**
	 * Constructor.
	 */
	public MemoryLexicon() {
		super(new WTreeMap<Text, LexiconEntry>());
		super.keyFactory = new FixedSizeTextFactory(
				ApplicationSetup.MAX_TERM_LENGTH);
	}

	/**
	 * Add new term, or update existing term.
	 * 
	 * @return The termid of the term.
	 */
	public int term(String term, EntryStatistics es) {
		synchronized(modificationLock) {
		
		LexiconEntry le = this.getLexiconEntry(term);
		if (le != null) {
			le.add(es);
			return le.getTermId();
		}
		int termid = super.map.size();
		Text key = keyFactory.newInstance();
		key.set(term);
		((LexiconEntry) es).setTermId(termid);
		super.map.put(key, (LexiconEntry) es);
		return termid;
		
		}
	}
	
	public int term(String term, EntryStatistics es, int termid) {
		synchronized(modificationLock) {
		
		LexiconEntry le = this.getLexiconEntry(term);
		if (le != null) {
			le.add(es);
			return le.getTermId();
		}
		Text key = keyFactory.newInstance();
		key.set(term);
		((LexiconEntry) es).setTermId(termid);
		super.map.put(key, (LexiconEntry) es);
		return termid;
		
		}
	}
	
	public int trimLexicon(int cutoff) {
		
		synchronized(modificationLock) {
		
		Object[] terms = super.map.keySet().toArray();
		int removed = 0;
		for (Object t : terms) {
			Text text = (Text)t;
			LexiconEntry le = super.map.get(text);
			if (le.getDocumentFrequency()<cutoff) {
				super.map.remove(text);
				removed++;
			}
		}
		return removed;
		
		}
	}
	
	public TIntObjectHashMap<ArrayList<Map.Entry<String,LexiconEntry>>> getTopTermBins(int binsize, int minDf) {
		synchronized(modificationLock) {
			
			TIntObjectHashMap<ArrayList<Map.Entry<String,LexiconEntry>>> bins = new TIntObjectHashMap<ArrayList<Map.Entry<String,LexiconEntry>>>();
			
			Object[] terms = super.map.keySet().toArray();
			for (Object t : terms) {
				Text text = (Text)t;
				LexiconEntry le = super.map.get(text);
				if (le.getDocumentFrequency()>=minDf) {
					int bin = ((int)Math.ceil(Math.log(le.getDocumentFrequency())))/binsize;
					if(!bins.contains(bin)) bins.put(bin, new ArrayList<Map.Entry<String,LexiconEntry>>());
					Map.Entry<String,LexiconEntry> entry =
						    new AbstractMap.SimpleEntry<String,LexiconEntry>(text.toString(), le);
					bins.get(bin).add(entry);
				}
			}
			return bins;
			
			}
	}

	/**
	 *  Lexicon iterator.
	 */
	public Iterator<Entry<String, LexiconEntry>> iterator() {
		final Iterator<Entry<Text, LexiconEntry>> iter = ((OrderedMap<Text, LexiconEntry>) super.map)
				.entrySet().iterator();
		return new Iterator<Entry<String, LexiconEntry>>() {
			public boolean hasNext() {
				return iter.hasNext();
			}

			public Entry<String, LexiconEntry> next() {
				Entry<Text, LexiconEntry> entry = iter.next();
				Entry<String, LexiconEntry> next = new MapEntry<String, LexiconEntry>(
						entry.getKey().toString(), entry.getValue());
				return next;
			}

			public void remove() {
			}
		};
	}
}