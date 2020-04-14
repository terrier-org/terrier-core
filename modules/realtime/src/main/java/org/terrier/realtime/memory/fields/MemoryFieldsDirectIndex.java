package org.terrier.realtime.memory.fields;
import org.terrier.realtime.memory.*;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;

import org.terrier.structures.DocumentIndex;
import org.terrier.structures.FieldDocumentIndex;

import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.BitIndexPointer;

public class MemoryFieldsDirectIndex extends MemoryDirectIndex {

    /*
	 * Inverted file structures.
	 */
	private FieldDocumentIndex fdoi;
    private TIntObjectHashMap<FieldDocumentPostingList> fpostings;
    
    public class FieldDocumentPostingList extends DocumentPostingList {
        private TIntObjectHashMap<int[]> termIds2field_frequencies = new TIntObjectHashMap<>();

        public FieldDocumentPostingList(){}

        public FieldDocumentPostingList(int termid, int freq, int[] fields) {
            super(termid, freq);
            termIds2field_frequencies.put(termid, fields);
        }

        public void add(int termid, int freq, int[] fields) {
            super.add(termid, freq);
            termIds2field_frequencies.put(termid, fields);
        }

        public List<int[]> getPl_fieldfreq() {
			
			List<int[]> fields = new ArrayList<>(pl_termids.size());
			for (int i =0; i<pl_termids.size(); i++) { 
				fields.add(termIds2field_frequencies.get(pl_termids.get(i)));
			}
			
			return fields;
		}
    }

	/**
	 * Constructor.
	 */
	public MemoryFieldsDirectIndex(FieldDocumentIndex doi) {
        super(doi);
		this.fdoi = doi;
        super.postings = (TIntObjectHashMap) (this.fpostings = new TIntObjectHashMap<FieldDocumentPostingList>());
    }

    @Override 
    public IterablePosting getPostings(int docid) throws IOException {
        FieldDocumentPostingList pl = fpostings.get(docid);
		if (pl==null) {
			pl = new FieldDocumentPostingList();
		}
		
		TIntArrayList termidsSorted = pl.getPl_termids(); // this does a sort of the termids
        TIntArrayList frequencies = pl.getPl_freq(); // this is the same order as the termids (so the sort in the previous call affects this)
        List<int[]> fields = pl.getPl_fieldfreq();
		return new MemoryFieldsDirectIterablePosting(termidsSorted, frequencies, fields);
    }

    private class FieldDirectIterator extends DirectIterator {
        FieldDirectIterator(Iterator<Entry<Integer, DocumentIndexEntry>> _doiIter) {
            super(_doiIter);
        }
    }

    /**
	 * Return an iterator over the inverted file.
	 */
	public PostingIndexInputStream iterator() {
		return new FieldDirectIterator(((MemoryDocumentIndexFields)fdoi).iteratorOverEntries());
    }
    
    /** Insert/update posting (docid,freq,(fields)). */
    public void add(int ptr, int termid, int freq, int[] fields) {
        if (fpostings.containsKey(ptr))
			fpostings.get(ptr).add(termid, freq, fields);
		else
			fpostings.put(ptr, new FieldDocumentPostingList(termid, freq, fields));
    }

    @Override
    public void add(int ptr, int termid, int freq) {
        throw new IllegalStateException();
    }
    
}