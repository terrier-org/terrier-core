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
 * The Original Code is MemoryDocumentIndexMap.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Dyaa Albakour <dyaa.albakour@glasgow.ac.uk>
 */

package org.terrier.realtime.memory;

import gnu.trove.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.terrier.realtime.memory.MemoryDocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.NonIncrementalDocumentIndexEntry;
import org.terrier.structures.collections.MapEntry;

/**
 * This class is a special variant of the Memory index class where the
 * Document index is backed by a fast mapping structure in memory.
 * 
 * @author Richard McCreadie, Dyaa Albakour
 * @since 4.0
 */
public class MemoryDocumentIndexMap extends MemoryDocumentIndex {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/* Document lengths. */
	private TIntIntHashMap docids2lengths = new TIntIntHashMap();

	/**
	 * Constructor.
	 */
	public MemoryDocumentIndexMap() {
		super();
	}

	/**
	 * Add document length to document index.
	 */
	public void addDocument(int docid, int length) {
		docids2lengths.put(docid, length);
	}

	
	/** {@inheritDoc} */
	@Override
	public DocumentIndexEntry getDocumentEntry(int docid) {
		//BasicDocumentIndexEntry die = new BasicDocumentIndexEntry();
		
		// refactored by Dyaa
		NonIncrementalDocumentIndexEntry die = new NonIncrementalDocumentIndexEntry();			
		int diedocid= -1;
		if(docids2lengths.containsKey(docid))
			diedocid = docid;
		die.setDocid(diedocid);		
		die.setDocumentLength(docids2lengths.get(docid));
		return die;
	}

	
	/** {@inheritDoc} */
	@Override
	public int getDocumentLength(int docid) {
		return docids2lengths.get(docid);
	}

	@Override
	/** {@inheritDoc} */
	public int getNumberOfDocuments() {
		return docids2lengths.size();
	}

	@Override
	/**
	 * Return an iterator over the document index.
	 */
	public Iterator<Entry<Integer, DocumentIndexEntry>> iteratorOverEntries() {
		return new DocumentMapIterator();
	}

	
	
	
	@Override
	public Iterator<DocumentIndexEntry> iterator() {
		
		return new DocumentMapIterator2();
	}

	

	
	/**
	 * Document index iterator, iterates over DocumentIndexEntry only, not Entry<index, DocumentIndexEntry>.
	 */
	public class DocumentMapIterator2 implements Iterator<DocumentIndexEntry> {
		private int index = 0;
		private List<Integer> docids;

		
		public DocumentMapIterator2(){
			docids = new ArrayList<Integer>(docids2lengths.size());
			for (int docid : docids2lengths.keys()) {
				docids.add(docid);
			}
			Collections.sort(docids);
			Collections.reverse(docids);
		}
		
		// private Iterator<DocumentIndexEntry> iter = null;
		public boolean hasNext() {
			return index < docids.size();
		}

		public DocumentIndexEntry next() {
			NonIncrementalDocumentIndexEntry die = new NonIncrementalDocumentIndexEntry();		
			int docid = docids.get(index++);
			die.setDocumentLength(docids2lengths.get(docid));
			die.setDocid(docid);
			
			return die;
		}

		public void remove() {
		}
	}

	/**
	 * Document index iterator.
	 */
	private class DocumentMapIterator implements Iterator<Entry<Integer, DocumentIndexEntry>> {
		private int index = 0;
		private List<Integer> docids;

		public DocumentMapIterator() {
			docids = new ArrayList<Integer>(docids2lengths.size());
			for (int docid : docids2lengths.keys()) {
				docids.add(docid);
			}
			Collections.sort(docids);
			Collections.reverse(docids);
		}
		
		// private Iterator<DocumentIndexEntry> iter = null;
		public boolean hasNext() {
			return index < docids.size();
		}

		public Entry<Integer, DocumentIndexEntry> next() {
			//BasicDocumentIndexEntry die = new BasicDocumentIndexEntry();
			
			NonIncrementalDocumentIndexEntry die = new NonIncrementalDocumentIndexEntry();		
			int docid = docids.get(index++);
			die.setDocumentLength(docids2lengths.get(docid));
			die.setDocid(docid);
			Entry<Integer, DocumentIndexEntry> e = new MapEntry<Integer, DocumentIndexEntry>(docid, die);
			return e;
		}

		public void remove() {
		}
	}
	
	public void close() {
		docids2lengths.clear();
		docids2lengths = null;
    }
	
}
