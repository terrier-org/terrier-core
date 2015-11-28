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
 * The Original Code is MemoryInvertedIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.terrier.structures.DocumentIndex;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.postings.IterablePosting;

/**
 * A basic inverted file implementation for use with MemoryIndex structures.
 * This version does not support fields or blocks. Since it is a memory-based
 * structure, access is via a MemoryPointer rather than BitIndexPointer.
 * 
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class MemoryInvertedIndex implements PostingIndex<MemoryPointer>,Serializable {

	private static final long serialVersionUID = -7814322137698041488L;
	/*
	 * Inverted file structures.
	 */
	protected DocumentIndex doi;
	protected Lexicon<String> lex;
	protected TIntObjectHashMap<MemoryPostingList> postings;

	/**
	 * Constructor.
	 */
	public MemoryInvertedIndex(Lexicon<String> lex, DocumentIndex doi) {
		this.lex = lex;
		this.doi = doi;
		postings = new TIntObjectHashMap<MemoryPostingList>();
	}

	/*
	 * Postings list.
	 */
	public class BasicMemoryPostingList implements MemoryPostingList {
		private TIntArrayList pl_doc;
		private TIntArrayList pl_freq;

		public BasicMemoryPostingList() {
			pl_doc = new TIntArrayList();
			pl_freq = new TIntArrayList();
		}
		
		public BasicMemoryPostingList(int[] docids, int[] docfreqs) {
			pl_doc = new TIntArrayList();
			pl_freq = new TIntArrayList();
			pl_doc.add(docids);
			pl_freq.add(docfreqs);
		}

		public BasicMemoryPostingList(int docid, int docfreq) {
			pl_doc = new TIntArrayList();
			pl_freq = new TIntArrayList();
			pl_doc.add(docid);
			pl_freq.add(docfreq);
		}

		public void add(int docid, int docfreq) {
			pl_doc.add(docid);
			pl_freq.add(docfreq);
		}
		
		public int getFreq(int docid) {
			int index = pl_doc.binarySearch(docid);
			if (index >= 0)
			{
				return pl_freq.get(index);
			}
			return -1;
		}
		
		/** Returns true iff we did not already have a posting for this document */
		public boolean addOrUpdateFreq(int docid, int freq) {
			int index = pl_doc.binarySearch(docid);
			if (index >= 0)
			{
				pl_freq.setQuick(index, freq + pl_freq.get(index));	
				return false;
			} else {
				pl_doc.insert( -(index +1), docid);
				pl_freq.insert( -(index +1), freq);	
				return true;
			}
			
		}
		

		public TIntArrayList getPl_doc() {
			return pl_doc;
		}

		public TIntArrayList getPl_freq() {
			return pl_freq;
		}
	}

	/**
	 * Add posting to inverted file.
	 */
	public void add(int ptr, int docid, int freq) {
		if (postings.containsKey(ptr))
			((BasicMemoryPostingList)postings.get(ptr)).add(docid, freq);
		else
			postings.put(ptr, new BasicMemoryPostingList(docid, freq));
	}
	
	/** Adds or updates the frequency of the term denoted by ptr by freq.
	 * @return true if a new posting was created, false if the document
	 * already contained the term */
	public boolean addOrUpdate(int ptr, int docid, int freq) {
		assert freq > 0;
		
		if (postings.containsKey(ptr))
		{
			BasicMemoryPostingList bmpl = (BasicMemoryPostingList) postings.get(ptr);
			return bmpl.addOrUpdateFreq(docid, freq);			
		}
		else
		{				
			postings.put(ptr, new BasicMemoryPostingList(docid, freq));
			return true;
		}
	}
	
	/**
	 * Remove a term posting list from the index, e.g. remove a stopword 
	 * @param ptr
	 */
	public void remove(int ptr) {
		if (postings.containsKey(ptr)) postings.remove(ptr);
	}

	/** {@inheritDoc} */
	@Override
	public IterablePosting getPostings(Pointer pointer) throws IOException {
		BasicMemoryPostingList pl = ((BasicMemoryPostingList)postings.get(((MemoryPointer)pointer).getPointer()));
		if (pl==null) {
			pl = new BasicMemoryPostingList();
		}
		return new MemoryIterablePosting(doi, pl.getPl_doc(), pl.getPl_freq());
	}

	/** {@inheritDoc} */
	public void close() throws IOException {
		postings = null;
	}

	/**
	 * Return an iterator over the inverted file.
	 */
	public PostingIndexInputStream iterator() {
		return new InvertedIterator();
	}

	/**
	 * Inverted index iterator.
	 */
	public class InvertedIterator implements PostingIndexInputStream {
		private Iterator<Entry<String, LexiconEntry>> lexIter = lex.iterator();
		private Entry<String, LexiconEntry> termAndEntry;

		public boolean hasNext() {
			return lexIter.hasNext();
		}

		public IterablePosting next() {
			termAndEntry = lexIter.next();
			try {
				return getPostings((MemoryPointer) termAndEntry.getValue());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public void close() throws IOException {
			IndexUtil.close(lexIter);
		}

		public void remove() {
		}

		public IterablePosting getNextPostings() throws IOException {
			if (!hasNext())
				return null;
			return next();
		}

		public int getNumberOfCurrentPostings() {
			return termAndEntry.getValue().getNumberOfEntries();
		}

		public Pointer getCurrentPointer() {
			return termAndEntry.getValue();
		}

		public int getEntriesSkipped() {
			return 0;
		}

		@Override
		public void print() {
			System.err.println("WARN: MemoryInvertedIndex.InvertedIterator is not implemented.");
		}
	}

	public DocumentIndex getDoi() {
		return doi;
	}

	public void setDoi(DocumentIndex doi) {
		this.doi = doi;
	}
	
	
}
