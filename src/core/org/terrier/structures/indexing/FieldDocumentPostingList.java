/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is FieldDocumentPostingList.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.indexing;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntProcedure;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.terrier.sorting.HeapSortInt;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.FieldDocumentIndexEntry;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.FieldPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.utility.TermCodes;
/** FieldDocumentPostingList class */
public class FieldDocumentPostingList extends DocumentPostingList {
	
	/** number of fields */
	protected final int fieldCount;	
	/** length of each field */
	protected final int[] fieldLengths;
	/** occurrences of terms in fields */
	protected final TObjectIntHashMap<String>[] field_occurrences;
	/** 
	 * constructor
	 * @param NUM_FIELDS
	 */
	@SuppressWarnings("unchecked")
	public FieldDocumentPostingList(final int NUM_FIELDS)
	{
		super();
		this.fieldCount = NUM_FIELDS;
		fieldLengths = new int[fieldCount];
		field_occurrences = new TObjectIntHashMap[fieldCount];
		for(int i=0;i<fieldCount;i++)
		{
			field_occurrences[i] = new TObjectIntHashMap<String>(AVG_DOCUMENT_UNIQUE_TERMS);
		}
	}
	
	/** Insert a term into the posting list of this document, in the given field, with the given frequency
	  * @param tf frequency of the term in this document
	  * @param term String form of term
	  * @param fieldNum fieldNumber it occurs in */
	public void insert(final int tf, final String term, final int fieldNum)
	{
		occurrences.adjustOrPutValue(term,tf,tf);
		field_occurrences[fieldNum].adjustOrPutValue(term, tf, tf);
		fieldLengths[fieldNum]+=tf;
		documentLength+=tf;
	}
	/**  Insert a term into the posting list of this document, in the given field
	  * @param term the Term being inserted
	  * @param fieldNum the id of the field that the term was found in */
	public void insert(final String term, final int fieldNum)
	{
		occurrences.adjustOrPutValue(term,1,1);
		field_occurrences[fieldNum].adjustOrPutValue(term, 1, 1);
		fieldLengths[fieldNum]++;
		documentLength++;
	}

	/**  Insert a term into the posting list of this document, in the given field
	  * @param term the Term being inserted
	  * @param fieldNums the ids of the fields that the term was found in, starting from 0 */
	public void insert(final String term, final int[] fieldNums)
	{
		occurrences.adjustOrPutValue(term,1,1);
		//System.err.println("t=" + term + "fs=" + Arrays.toString(fieldNums));
		for(int fieldId : fieldNums)
		{
			if (fieldId == -1)
				continue;
			field_occurrences[fieldId].adjustOrPutValue(term, 1, 1);
			fieldLengths[fieldId]++;
		}
		documentLength++;
	}

	/**  Insert a term into the posting list of this document, in the given field
	  * @param tf the frequency of the term
	  * @param term the Term being inserted
	  * @param fieldNums the ids of the fields that the term was found in */
	public void insert(final int tf, final String term, final int[] fieldNums)
	{
		occurrences.adjustOrPutValue(term,tf,tf);
		for(int fieldId : fieldNums)
		{
			field_occurrences[fieldId].adjustOrPutValue(term, tf, tf);
			fieldLengths[fieldId]+=tf;
		}
		documentLength+=tf;
	}
	
	/** Return the frequencies of the specified term in all of the fields */
	public int[] getFieldFrequencies(final String term)
	{
		final int[] rtr = new int[fieldCount];
		for(int i=0;i<fieldCount;i++)
			rtr[i] = field_occurrences[i].get(term);
		return rtr;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public DocumentIndexEntry getDocumentStatistics()
	{
		FieldDocumentIndexEntry fdie = new FieldDocumentIndexEntry(this.fieldCount);
		fdie.setDocumentLength(documentLength);
		fdie.setNumberOfEntries(occurrences.size());
		fdie.setFieldLengths(fieldLengths);
		return fdie;
	}

	@Override
	public void clear() {
		super.clear();
		Arrays.fill(fieldLengths, 0);
	}

	@Override
	public int[][] getPostings() {
		
		final int termCount = occurrences.size();
		final int[][] postings = new int[fieldCount + 2][termCount];
		//final int[] termids = new int[termCount];
		//final int[] tfs = new int[termCount];
		
		occurrences.forEachEntry( new TObjectIntProcedure<String>() {
			int i=0;
			public boolean execute(final String a, final int b)
			{
				postings[0][i] = TermCodes.getCode(a);
				postings[1][i] = b;
				for(int fi=0;fi< fieldCount;fi++)
					postings[2+fi][i] = field_occurrences[fi].get(a);
				//fields[i++] = term_fields.get(a);
				i++;
				return true;
			}
		});	
		HeapSortInt.ascendingHeapSort(postings);
		return postings;
	}

	class fieldPostingIterator 
		extends postingIterator
		implements FieldPosting
	{
		int[] fieldFrequencies = new int[fieldCount];
		
		public fieldPostingIterator(String[] _terms, int[] ids) {
			super(_terms, ids);
		}
		
		/** {@inheritDoc} */
		public int[] getFieldFrequencies()
		{
			for(int fi=0;fi<fieldCount;fi++)
			{
				fieldFrequencies[fi] = field_occurrences[fi].get(terms[i]);
			}
			return fieldFrequencies;
		}

		/** {@inheritDoc}. Not implemented yet. */
		public int[] getFieldLengths() {
			return null;
		}
		
		@Override
		public WritablePosting asWritablePosting() {
			FieldPostingImpl fbp = new FieldPostingImpl(termIds[i],getFrequency(), fieldCount);
			System.arraycopy(getFieldFrequencies(), 0, fbp.getFieldFrequencies(), 0, fieldCount);
			return fbp;
		}

		@Override
		public void setFieldLengths(int[] newLengths) {
			throw new UnsupportedOperationException();
		}
		
	}
	
	@Override 
	protected IterablePosting makePostingIterator(String[] _terms, int[] termIds)
	{
		return new fieldPostingIterator(_terms, termIds);
	}
	
	
	@Override
	public void readFields(DataInput in) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void write(final DataOutput out) throws IOException {
		throw new UnsupportedOperationException();
	}
}
