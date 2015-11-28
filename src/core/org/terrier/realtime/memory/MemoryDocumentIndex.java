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
 * The Original Code is MemoryDocumentIndex.java.
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

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.terrier.structures.BasicDocumentIndexEntry;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.collections.MapEntry;

/**
 * An in-memory version of the Document index. Stores the length
 * of each document.
 * 
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class MemoryDocumentIndex implements DocumentIndex,Serializable {

	private static final long serialVersionUID = -7639008149037297229L;
	/* Document lengths. */
	public TIntArrayList docLengths = new TIntArrayList();

	/**
	 * Constructor.
	 */
	public MemoryDocumentIndex() {
	}

	/**
	 * Add document length to document index.
	 */
	public void addDocument(int length) {
		docLengths.add(length);
	}
	
	public void setLength(int docid, int newLength) {
		docLengths.set(docid, newLength);
	}

	/** {@inheritDoc} */
	public DocumentIndexEntry getDocumentEntry(int docid) throws IOException {
		BasicDocumentIndexEntry die = new BasicDocumentIndexEntry();
		die.setDocumentLength(docLengths.get(docid));
		return die;
	}

	/** {@inheritDoc} */
	public int getDocumentLength(int docid) throws IOException {
		return docLengths.get(docid);
	}

	/** {@inheritDoc} */
	public int getNumberOfDocuments() {
		return docLengths.size();
	}

	/**
	 * Return an iterator over the document index. iterates over Map.Entry, where the
	 * key is the integer docid - only used my the Memory index, not the index-on-disk.
	 */
	public Iterator<Entry<Integer, DocumentIndexEntry>> iteratorOverEntries() {
		return new DocumentIterator();
	}
	
	/**
	 * Return an iterator over the document index.
	 */
	public Iterator<DocumentIndexEntry> iterator() {
		return new DocumentIterator2();
	}

	/**
	 * Document index iterator.
	 */
	public class DocumentIterator implements Iterator<Entry<Integer, DocumentIndexEntry>> {
		private int index = 0;

		// private Iterator<DocumentIndexEntry> iter = null;
		public boolean hasNext() {
			return index < docLengths.size();
		}

		public Entry<Integer, DocumentIndexEntry> next() {
			BasicDocumentIndexEntry die = new BasicDocumentIndexEntry();
			die.setDocumentLength(docLengths.get(index++));
			Entry<Integer, DocumentIndexEntry> e = new MapEntry<Integer, DocumentIndexEntry>(index, die);
			return e;
		}

		public void remove() {
		}
	}
	
	/**
	 * Document index iterator, iterates over DocumentIndexEntry only, not Entry<index, DocumentIndexEntry>.
	 */
	public class DocumentIterator2 implements Iterator<DocumentIndexEntry> {
		private int index = 0;

		// private Iterator<DocumentIndexEntry> iter = null;
		public boolean hasNext() {
			return index < docLengths.size();
		}

		public DocumentIndexEntry next() {
			BasicDocumentIndexEntry die = new BasicDocumentIndexEntry();
			die.setDocumentLength(docLengths.get(index++));
			return die;
		}

		public void remove() {
		}
	}

}
