/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.uk
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
 * The Original Code is DocumentPostingList.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */

package org.terrier.structures.indexing;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntProcedure;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;
import org.terrier.sorting.HeapSortInt;
import org.terrier.structures.BasicDocumentIndexEntry;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.IterablePostingImpl;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.TermCodes;
/** Represents the postings of one document. Uses HashMaps internally.
  * <p>
  * <b>Properties:</b><br>
  * <ul><li><tt>indexing.avg.unique.terms.per.doc</tt> - number of unique terms per doc on average, used to tune the initial 
  * size of the hashmaps used in this class.</li></ul>
  */
public class DocumentPostingList implements Writable{
	/** number of unique terms per doc on average, used to tune the initial size of the hashmaps used in this class. */
	protected static final int AVG_DOCUMENT_UNIQUE_TERMS =
		Integer.parseInt(ApplicationSetup.getProperty("indexing.avg.unique.terms.per.doc", "120"));

	/** length of the document so far. Sum of the term frequencies inserted so far. */
	protected int documentLength = 0;

	/** mapping term to tf mapping */	
	protected final TObjectIntHashMap<String> occurrences = new TObjectIntHashMap<String>(AVG_DOCUMENT_UNIQUE_TERMS);
	
	/** Create a new DocumentPostingList object */
	public DocumentPostingList()
	{}
	
	/** Returns all terms in this posting list */
	public String[] termSet()
	{
		return occurrences.keys(new String[0]);
	}
	
	/** Return the frequency of the specified term in this document */
	public int getFrequency(String term)
	{
		return occurrences.get(term);
	}	

	/** Removes all postings from this document */
	public void clear()
	{
		occurrences.clear();
		documentLength = 0;
	}

	/** Returns the total number of tokens in this document */	
	public int getDocumentLength()
	{
		return documentLength;
	}

	/** Returns the number of unique terms in this document. */
	public int getNumberOfPointers()
	{
		return occurrences.size();
	}
	/** Insert a term into the posting list of this document 
	  * @param term the Term being inserted */
	public void insert(final String term)
	{
		occurrences.adjustOrPutValue(term,1,1);
		documentLength++;
	}
	
	/** Insert a term into the posting list of this document
	  * @param tf frequency
      * @param term the Term being inserted */
    public void insert(final int tf, final String term)
    {
        occurrences.adjustOrPutValue(term,tf,tf);
        documentLength++;
    }

    /** Return a DocumentIndexEntry for this document */ 
    public DocumentIndexEntry getDocumentStatistics()
	{
		DocumentIndexEntry die = new BasicDocumentIndexEntry();
		die.setDocumentLength(this.getDocumentLength());
		die.setNumberOfEntries(this.getNumberOfPointers());
		return die;
	}
    
    /** Execute the specifed method for each term. */
    public void forEachTerm(TObjectIntProcedure<String> proc)
    {
    	this.occurrences.forEachEntry(proc);
    }
    
    /** Used by getPostings() and getPostings2() to obtain the term id of the term.
     * This implementation uses the TermCodes class. */
    protected int getTermId(String term)
    {
    	return TermCodes.getCode(term);
    }

	/** Returns the postings suitable to be written into the direct index.
	 * During this, TermIds are assigned. */
	public int[][] getPostings()
	{
		final int termCount = occurrences.size();
		final int[] termids = new int[termCount];
		final int[] tfs = new int[termCount];
		occurrences.forEachEntry( new TObjectIntProcedure<String>() { 
			int i=0;
			public boolean execute(final String a, final int b)
			{
				termids[i] = getTermId(a);
				tfs[i++] = b;
				return true;
			}
		});
		HeapSortInt.ascendingHeapSort(termids, tfs);
		return new int[][]{termids, tfs};
	}
	
	/** Returns a posting iterator suitable to be written into the direct index.
	 * During this, TermIds are assigned, using getTermId() method. */
	public IterablePosting getPostings2()
	{
		//obtain and sort termids by id
		
		final int termCount = occurrences.size();
		final TObjectIntHashMap<String> cache_termids = new TObjectIntHashMap<String>(termCount);	
		
		occurrences.forEachEntry( new TObjectIntProcedure<String>() { 
			public boolean execute(final String a, final int b)
			{
				cache_termids.put(a, getTermId(a));
				return true;
			}
		});
		
		final String[] terms = cache_termids.keys(new String[termCount]);
		Arrays.sort(terms, new Comparator<String>(){
			public int compare(String o1, String o2) {
				return cache_termids.get(o1) - cache_termids.get(o2);
			}			
		});
		final int[] termIds = new int[termCount];
		int i=0;
		for(String t : terms)
		{
			termIds[i++] = cache_termids.get(t);
		}
		return makePostingIterator(terms, termIds);
	}
	
	protected IterablePosting makePostingIterator(String[] _terms, int[] termIds)
	{
		return new postingIterator(_terms, termIds);
	}
	
	protected class postingIterator extends IterablePostingImpl
	{
		String[] terms;
		int[] termIds;
		int i = -1;
		
		public postingIterator(String[] _terms, int[] _termIds)
		{
			terms = _terms;
			termIds = _termIds;
		}
		
		public WritablePosting asWritablePosting() {
			return new BasicPostingImpl(termIds[i], getFrequency());
		}

		public int getDocumentLength() {
			return documentLength;
		}

		public int getFrequency() {
			return occurrences.get(terms[i]);
		}

		public int getId() {
			return termIds[i];
		}

		public void setId(int id) {
			termIds[i] = id;
		}

		public int next() throws IOException {
			if (i >= termIds.length -1)
				return EOL;
			i++;
			return termIds[i];
		}
		
		/** {@inheritDoc} */
		public boolean endOfPostings() {
			return (i >= termIds.length -1);
		}

		public void close() throws IOException {
			terms = null;
			termIds = null;
		}
	}

	public void readFields(DataInput in) throws IOException {
		clear();
		final int termCount = WritableUtils.readVInt(in);
		for(int i=0;i<termCount;i++)
		{
			String term = Text.readString(in);
			int freq = WritableUtils.readVInt(in);
			insert(freq, term);
		}
	}

	public void write(final DataOutput out) throws IOException {
		WritableUtils.writeVInt(out, getNumberOfPointers());
		try
		{
			this.forEachTerm(new TObjectIntProcedure<String>()
			{
				public boolean execute(String term, int freq) {
					try{
					Text.writeString(out, term);
					WritableUtils.writeVInt(out, freq);
					} catch (IOException e) {
						throw new Error(e);
					}
					return true;
				}
			});
		} catch (Error e) {
			throw (IOException)e.getCause();
		}
	}
	
}
