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
 * The Original Code is MemoryDocumentIndexFields.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory.fields;

import gnu.trove.TIntArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.terrier.realtime.memory.MemoryDocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.FieldDocumentIndex;
import org.terrier.structures.FieldDocumentIndexEntry;
import org.terrier.structures.collections.MapEntry;

/** Document index (fields). 
 * @author Stuart Mackie
 * @since 4.0
 */
public class MemoryDocumentIndexFields extends MemoryDocumentIndex implements FieldDocumentIndex {

	private static final long serialVersionUID = -3154305694924339094L;
	// Per-field lengths (tokens).
    private List<TIntArrayList> fieldLengths;

    /** Constructor. */
    public MemoryDocumentIndexFields() {
        super();
        fieldLengths = new ArrayList<TIntArrayList>();
    }

    /** Add document length and field lengths to document index. */
    public void addDocument(int length, int[] flengths) {
        docLengths.add(length);
        fieldLengths.add(new TIntArrayList(flengths));
    }

    /** {@inheritDoc} */
    public int[] getFieldLengths(int docid) {
        return fieldLengths.get(docid).toNativeArray();
    }

    /** {@inheritDoc} */
    @Override
    public DocumentIndexEntry getDocumentEntry(int docid) {
        FieldDocumentIndexEntry fdie = new FieldDocumentIndexEntry();
        try {
			fdie.setDocumentLength(getDocumentLength(docid));
		} catch (IOException e) {
			e.printStackTrace();
		}
        fdie.setFieldLengths(getFieldLengths(docid));
        return fdie;
    }
    
    /** {@inheritDoc} */
    @Override
	public Iterator<Entry<Integer, DocumentIndexEntry>> iteratorOverEntries() {
		return new DocumentIterator();
	}
	
    /** {@inheritDoc} */
    @Override
	public Iterator<DocumentIndexEntry> iterator() {
		return new DocumentIterator2();
	}
    
	public class DocumentIterator implements Iterator<Entry<Integer, DocumentIndexEntry>> {
		private int index = 0;

		// private Iterator<DocumentIndexEntry> iter = null;
		public boolean hasNext() {
			return index < docLengths.size();
		}

		public Entry<Integer, DocumentIndexEntry> next() {
			FieldDocumentIndexEntry die = new FieldDocumentIndexEntry();
			die.setDocumentLength(docLengths.get(index));
			die.setFieldLengths(fieldLengths.get(index++).toNativeArray());
			Entry<Integer, DocumentIndexEntry> e = new MapEntry<Integer, DocumentIndexEntry>(index, die);
			return e;
		}

		public void remove() {
		}
	}
	
	public class DocumentIterator2 implements Iterator<DocumentIndexEntry> {
		private int index = 0;

		// private Iterator<DocumentIndexEntry> iter = null;
		public boolean hasNext() {
			return index < docLengths.size();
		}

		public DocumentIndexEntry next() {
			FieldDocumentIndexEntry die = new FieldDocumentIndexEntry();
			die.setDocumentLength(docLengths.get(index));
			die.setFieldLengths(fieldLengths.get(index++).toNativeArray());
			return die;
		}

		public void remove() {
		}
	}

   
}
