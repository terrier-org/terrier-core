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
 * The Original Code is MemoryFieldsInvertedIndex.java.
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

import java.io.IOException;
import java.util.Arrays;

import org.terrier.realtime.memory.MemoryInvertedIndex;
import org.terrier.realtime.memory.MemoryPointer;
import org.terrier.realtime.memory.MemoryPostingList;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Lexicon;
import org.terrier.structures.Pointer;
import org.terrier.structures.postings.IterablePosting;

/** Postings list (non-compressed) (fields). 
 * 
 *  @author Stuart Mackie
 * @since 4.0
 * */
public class MemoryFieldsInvertedIndex extends MemoryInvertedIndex {

	private static final long serialVersionUID = -4471360517668596542L;

	/** Constructor. */
    public MemoryFieldsInvertedIndex(Lexicon<String> lexicon, DocumentIndex docindex) {
        super(lexicon, docindex);
    }

    /** {@inheritDoc} */
    public void close() {
    	try {
			super.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        System.gc();
    }

    /** Insert/update posting (docid,freq,(fields)). */
    public void add(int termid, int docid, int freq, int[] fields) {
        if (postings.containsKey(termid))
            ((FieldsMemoryPostingList)postings.get(termid)).add(docid, freq, fields);
        else
            postings.put(termid, new FieldsMemoryPostingList(docid, freq, fields));
    }

    /** {@inheritDoc} */
    @Override
    public IterablePosting getPostings(Pointer _termid) throws IOException {
    	MemoryPointer termid = (MemoryPointer)_termid;
        FieldsMemoryPostingList pl;
        if (postings.containsKey(termid.getPointer()))
            pl = (FieldsMemoryPostingList) postings.get(termid.getPointer());
        else
            pl = new FieldsMemoryPostingList();
        return new MemoryFieldsIterablePosting(doi, pl.docids(), pl.freqs(), pl.fields());
    }

    /* Postings list. */
    private class FieldsMemoryPostingList implements MemoryPostingList{

        // (docid,freq,(fields))
        TIntArrayList            docids;
        TIntArrayList            freqs;
        TIntObjectHashMap<int[]> fields;

        /* Constructor. */
        FieldsMemoryPostingList() {
            docids = new TIntArrayList();
            freqs = new TIntArrayList();
            fields = new TIntObjectHashMap<int[]>();
        }

        /* Constructor. */
        FieldsMemoryPostingList(int docid, int freq, int[] fields) {
            this();
            add(docid, freq, fields);
        }

        /* Add posting. */
        void add(int docid, int freq, int[] fields) {
            docids.add(docid);
            freqs.add(freq);
            this.fields.put(docid, Arrays.copyOf(fields, fields.length));
        }

        /* Get docids. */
        TIntArrayList docids() {
            return new TIntArrayList(docids.toNativeArray());
        }

        /* Get freqs. */
        TIntArrayList freqs() {
            return new TIntArrayList(freqs.toNativeArray());
        }

        /* Get fields. */
        TIntObjectHashMap<int[]> fields() {
            TIntObjectHashMap<int[]> tmp = new TIntObjectHashMap<int[]>(fields.size());
            for (int docid : fields.keys())
                tmp.put(docid, Arrays.copyOf(fields.get(docid), fields.get(docid).length));
            return tmp;
        }
    }
}
