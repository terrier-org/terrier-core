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
 * The Original Code is BlockFieldDocumentPostingList.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.indexing;

import gnu.trove.THashMap;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntProcedure;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.terrier.sorting.HeapSortInt;
import org.terrier.structures.postings.BlockFieldPostingImpl;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.utility.TermCodes;
/** BlockFieldDocumentPostingList class */
public class BlockFieldDocumentPostingList extends FieldDocumentPostingList {
	/** mapping term to blockids in this document */
	protected final THashMap<String, TIntHashSet> term_blocks = new THashMap<String, TIntHashSet>(AVG_DOCUMENT_UNIQUE_TERMS);
	/** number of blocks in this document. usually equal to document length, but perhaps less */
	protected int blockCount = 0;
		
	/**
	 * Constructs an instance of BlockFieldDocumentPostingList.
	 * @param NUM_FIELDS
	 */
	public BlockFieldDocumentPostingList(int NUM_FIELDS) {
		super(NUM_FIELDS);
	}
	/** 
	 * Get the blocks for the specified term
	 */
	public int[] getBlocks(String term)
	{
		int[] rtr = term_blocks.get(term).toArray();
		if (rtr ==  null)
			return new int[0];
		Arrays.sort(rtr);
		return rtr;
	}

	/** Insert a term into this document, occurs at given block id, and in the given field */
	public void insert(String t, int fieldId, int blockId)
	{
		super.insert(t, fieldId);
		TIntHashSet blockids = null;
		if ((blockids = term_blocks.get(t)) == null)
		{
			term_blocks.put(t, blockids = new TIntHashSet(/*TODO */));
		}
		blockids.add(blockId);
		blockCount++;
	}

	/** Insert a term into this document, occurs at given block id, and in the given fields */
	public void insert(String t, int[] fieldIds, int blockId)
	{
		super.insert(t, fieldIds);
		TIntHashSet blockids = null;
		if ((blockids = term_blocks.get(t)) == null)
		{
			term_blocks.put(t, blockids = new TIntHashSet(/*TODO */));
		}
		blockids.add(blockId);
		blockCount++;
	}

	/** Insert a term into this document tf times, occurs at given block id, and in the given fields */
	public void insert(int tf, String t, int[] fieldIds, int blockId)
	{
		super.insert(tf, t, fieldIds);
		TIntHashSet blockids = null;
		if ((blockids = term_blocks.get(t)) == null)
		{
			term_blocks.put(t, blockids = new TIntHashSet(/*TODO */));
		}
		blockids.add(blockId);
		blockCount++;
	}
	
	class blockFieldPostings extends fieldPostingIterator implements BlockPosting
	{
		public blockFieldPostings(String[] _terms, int[] ids) {
			super(_terms, ids);
		}		
		
		/** {@inheritDoc} */
		public int[] getPositions() {
			int[] blockIds = term_blocks.get(terms[i]).toArray();
			Arrays.sort(blockIds);
			return blockIds;
		}

		@Override
		public WritablePosting asWritablePosting() {
			BlockFieldPostingImpl fbp = new BlockFieldPostingImpl(termIds[i], getFrequency(), getPositions(), fieldCount);
			System.arraycopy(getFieldFrequencies(), 0, fbp.getFieldFrequencies(), 0, fieldCount);
			return fbp;
		}		
	}
	
	/** returns the postings suitable to be written into the block direct index */
	public int[][] getPostings()
	{
		final int termCount = occurrences.size();
		final int[] termids = new int[termCount];
		final int[] tfs = new int[termCount];
		
		final int[][] fields = new int[fieldCount][termCount];
		final int[] blockfreqs = new int[termCount];
		final TIntObjectHashMap<int[]> term2blockids = new TIntObjectHashMap<int[]>();
		int blockTotal = 0; //TODO we already have blockTotal as this.blockCount, so no need to count?
		
		class PostingVisitor implements TObjectIntProcedure<String> {
			int blockTotal = 0;
			int i=0;
			public boolean execute(final String a, final int b)
			{
				termids[i] = TermCodes.getCode(a);
				tfs[i] = b;
				for(int fi=0;fi<fieldCount;fi++)
					fields[fi][i] = field_occurrences[fi].get(a);
				final TIntHashSet ids = term_blocks.get(a);
				blockfreqs[i] = ids.size();
				this.blockTotal += ids.size();
				final int[] bids = ids.toArray();
				Arrays.sort(bids);
				term2blockids.put(termids[i], bids);
				i++;
				return true;
			}
		}
		PostingVisitor proc = new PostingVisitor();
		occurrences.forEachEntry(proc);
		blockTotal = proc.blockTotal;
		int[][] tmppostings = new int[3+fieldCount][];
		tmppostings[0] = termids;
		tmppostings[1] = tfs;
		for(int fi=0;fi<fieldCount;fi++)
			tmppostings[fi+2] = fields[fi];
		tmppostings[fieldCount+2] = blockfreqs;
		HeapSortInt.ascendingHeapSort(tmppostings);
		final int[] blockids = new int[blockTotal];
		int offset = 0;
		for (int termid : termids)
		{
			final int[] src = term2blockids.get(termid);
			final int src_l = src.length;
			System.arraycopy(src, 0, blockids, offset, src_l);
			offset+= src_l;
		}
		int[][] postings = new int[4+fieldCount][];
		postings[0] = tmppostings[0];
		postings[1] = tmppostings[1];
		for(int fi=0;fi<fieldCount;fi++)
			postings[fi+2] = fields[fi];
		postings[fieldCount+2] = blockfreqs;
		postings[fieldCount+3] = blockids;
		return postings;
	}

	@Override 
	protected IterablePosting makePostingIterator(String[] _terms, int[] termIds)
	{
		return new blockFieldPostings(_terms, termIds);
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