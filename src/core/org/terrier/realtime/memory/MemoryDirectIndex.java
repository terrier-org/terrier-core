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
 * The Original Code is MemoryDirectIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime.memory;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.terrier.structures.DocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.postings.IterablePosting;

/**
 * This is a DirectIndex structure that is held fully in memory, it 
 * is based upon the MemoryInverted class. It does not currently support
 * blocks or fields.
 * @author Richard McCreadie
 * @since 4.0
 *
 */
public class MemoryDirectIndex implements PostingIndex<MemoryPointer> {

	/*
	 * Inverted file structures.
	 */
	private DocumentIndex doi;
	private TIntObjectHashMap<DocumentPostingList> postings;

	/**
	 * Constructor.
	 */
	public MemoryDirectIndex(DocumentIndex doi) {
		this.doi = doi;
		postings = new TIntObjectHashMap<DocumentPostingList>();
	}

	/*
	 * Postings list.
	 */
	public class DocumentPostingList {
		private TIntArrayList pl_termids;
		private TIntArrayList pl_termfrequencies;

		public DocumentPostingList() {
			pl_termids = new TIntArrayList();
			pl_termfrequencies = new TIntArrayList();
		}
		
		public DocumentPostingList(int[] docids, int[] docfreqs) {
			pl_termids = new TIntArrayList();
			pl_termfrequencies = new TIntArrayList();
			pl_termids.add(docids);
			pl_termfrequencies.add(docfreqs);
		}

		public DocumentPostingList(int docid, int docfreq) {
			pl_termids = new TIntArrayList();
			pl_termfrequencies = new TIntArrayList();
			pl_termids.add(docid);
			pl_termfrequencies.add(docfreq);
		}

		public void add(int docid, int docfreq) {
			pl_termids.add(docid);
			pl_termfrequencies.add(docfreq);
		}

		public TIntArrayList getPl_doc() {
			return pl_termids;
		}

		public TIntArrayList getPl_freq() {
			return pl_termfrequencies;
		}
	}

	/**
	 * Add posting to direct file.
	 */
	public void add(int ptr, int docid, int freq) {
		if (postings.containsKey(ptr))
			postings.get(ptr).add(docid, freq);
		else
			postings.put(ptr, new DocumentPostingList(docid, freq));
	}

	/** {@inheritDoc} */
	public IterablePosting getPostings(Pointer pointer) throws IOException {
		DocumentPostingList pl = postings.get(((MemoryPointer)pointer).getPointer());
		if (pl==null) {
			pl = new DocumentPostingList();
		}
		return new MemoryIterablePosting(doi, pl.getPl_doc(), pl.getPl_freq());
	}
	
	public IterablePosting getPostings(int pointer) throws IOException {
		DocumentPostingList pl = postings.get(pointer);
		if (pl==null) {
			pl = new DocumentPostingList();
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
		return new DirectIterator();
	}

	/**
	 * Direct index iterator.
	 */
	private class DirectIterator implements PostingIndexInputStream {
		// TODO: This cast to a MemoryDocumentIndex is not good. Should the iterator method be in an interface up the chain?
		private Iterator<Entry<Integer, DocumentIndexEntry>> doiIter = ((MemoryDocumentIndex)doi).iteratorOverEntries();
		private Entry<Integer, DocumentIndexEntry> doiEntry;

		public boolean hasNext() {
			return doiIter.hasNext();
		}

		public IterablePosting next() {
			doiEntry = doiIter.next();
			try {
				return getPostings(doiEntry.getKey());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public void close() throws IOException {
			IndexUtil.close(doiIter);
		}

		public void remove() {
		}

		public IterablePosting getNextPostings() throws IOException {
			if (!hasNext())
				return null;
			return next();
		}

		public int getNumberOfCurrentPostings() {
			return doiEntry.getValue().getNumberOfEntries();
		}

		public Pointer getCurrentPointer() {
			return doiEntry.getValue();
		}

		public int getEntriesSkipped() {
			return 0;
		}

		@Override
		public void print() {
			System.err.println("WARN: MemoryDirectIndex.DirectIterator is not implemented.");
		}
	}
}
