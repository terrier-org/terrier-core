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
 * The Original Code is MemoryDocumentIndexFieldsMap.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory.fields;


import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import org.terrier.realtime.memory.MemoryDocumentIndexMap;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.FieldDocumentIndex;
import org.terrier.structures.FieldDocumentIndexEntry;

/**
 * A memory document index structure that supports fields and fast lookups via
 * a map structure.
 *  @author Stuart Mackie, Richard McCreadie
 *  @since 4.0
 */
public class MemoryDocumentIndexFieldsMap extends MemoryDocumentIndexMap implements FieldDocumentIndex {

	private static final long serialVersionUID = 5719096024449880656L;
	// Per-field lengths (tokens).
    private TIntObjectHashMap<TIntArrayList> fieldLengths;
    private TIntObjectHashMap<BitIndexPointer> bitpointers;

    /** Constructor. */
    public MemoryDocumentIndexFieldsMap() {
        super();
        fieldLengths = new TIntObjectHashMap<TIntArrayList>();
        bitpointers = new TIntObjectHashMap<BitIndexPointer>();
    }

    /** Add document length and field lengths to document index. */
    public void addDocument(int docid, int length, int[] flengths) {
        addDocument(docid, length);
        fieldLengths.put(docid, new TIntArrayList(flengths));
    }
    
    /** Add document length and field lengths to document index. */
    public void addDocument(int docid, int length, int[] flengths, BitIndexPointer bitbasedpointer) {
        addDocument(docid, length);
        fieldLengths.put(docid, new TIntArrayList(flengths));
        bitpointers.put(docid, bitbasedpointer);
    }

    /** {@inheritDoc} */
    public int[] getFieldLengths(int docid) {
    	if (!fieldLengths.contains(docid)) {
    		System.err.println("Memory Document index does not contain lengths for doc "+docid);
    		return new int[2];
    	}
        return fieldLengths.get(docid).toNativeArray();
    }

    /** {@inheritDoc} */
    public FieldDocumentIndexEntry getDocumentEntry(int docid) {
        FieldDocumentIndexEntry fdie = new FieldDocumentIndexEntry();
		fdie.setDocumentLength(getDocumentLength(docid));
        fdie.setFieldLengths(getFieldLengths(docid));
        if (bitpointers.contains(docid)) fdie.setBitIndexPointer(bitpointers.get(docid));
        return fdie;
    }
    
    public void close() {
    	super.close();
    	fieldLengths.clear();
    	fieldLengths = null;
    	bitpointers.clear();
    	bitpointers = null;
    }
	
}
