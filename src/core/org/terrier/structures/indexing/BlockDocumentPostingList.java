
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
 * The Original Code is BlockDocumentPostingList.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
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

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableUtils;
import org.terrier.sorting.HeapSortInt;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.BlockPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.WritablePosting;
/** Represents the postings of one document, and saves block (term position) information. Uses HashMaps internally.
  * <p>
  * <b>Properties:</b><br>
  * <ul><li><tt>indexing.avg.unique.terms.per.doc</tt> - number of unique terms per doc on average, used to tune the initial
  * size of the haashmaps used in this class.</li></ul>
  * @see org.terrier.structures.indexing.DocumentPostingList
  */
public class BlockDocumentPostingList extends DocumentPostingList
{
	/** mapping term to blockids in this document */
	protected final THashMap<String, TIntHashSet> term_blocks = new THashMap<String, TIntHashSet>(AVG_DOCUMENT_UNIQUE_TERMS);
	/** number of blocks in this document. usually equal to document length, but perhaps less */
	protected int blockCount = 0;
	/** Instantiate a new block document posting list. Saves block information, but no fields */
	public BlockDocumentPostingList() {super();} 
	
	/** Insert a term into this document, occurs at given block id */
	public void insert(String t, int blockId)
	{
		insert(t);
		TIntHashSet blockids = null;
		if ((blockids = term_blocks.get(t)) == null)
		{
			term_blocks.put(t, blockids = new TIntHashSet(/*TODO */));
		}
		blockids.add(blockId);
		blockCount++;	
	}

	
	/**
	 * return blocks
	 * @param term
	 * @return int[]
	 */
	public int[] getBlocks(String term)
	{
		int[] rtr = term_blocks.get(term).toArray();
		if (rtr ==  null)
			return new int[0];
		Arrays.sort(rtr);
		return rtr;
	}

	/** returns the postings suitable to be written into the block direct index */
	@Override
	public int[][] getPostings()
	{
		final int termCount = occurrences.size();
		final int[] termids = new int[termCount];
		final int[] tfs = new int[termCount];
		final int[] fields = null;
		final int[] blockfreqs = new int[termCount];
		final TIntObjectHashMap<int[]> term2blockids = new TIntObjectHashMap<int[]>();
		int blockTotal = 0; //TODO we already have blockTotal as this.blockCount, so no need to count?
		class PostingVisitor implements TObjectIntProcedure<String> {
			int i=0;
			int blockTotal = 0;
			public boolean execute(final String a, final int b)
			{
				termids[i] = getTermId(a);
				tfs[i] = b;
				final TIntHashSet ids = term_blocks.get(a);
				blockfreqs[i] = ids.size();
				blockTotal += ids.size();
				final int[] bids = ids.toArray();
				Arrays.sort(bids);
				term2blockids.put(termids[i], bids);
				//System.err.println(a+": tid="+termids[i]+" tf="+tfs[i]+" bf="+blockfreqs[i] +" blocks="+Arrays.toString(bids));
				i++;
				return true;
			}
		}
		PostingVisitor proc = new PostingVisitor();
		occurrences.forEachEntry(proc);
		blockTotal = proc.blockTotal;
		HeapSortInt.ascendingHeapSort(termids, tfs,  blockfreqs);
		final int[] blockids = new int[blockTotal];
		int offset = 0;
		for (int termid : termids)
		{
			final int[] src = term2blockids.get(termid);
			final int src_l = src.length;
			System.arraycopy(src, 0, blockids, offset, src_l);
			offset+= src_l;
		}
		return new int[][]{termids, tfs, fields, blockfreqs, blockids};
	}
	
	protected IterablePosting makePostingIterator(String[] _terms, int[] termIds)
	{
		return new blockPostings(_terms, termIds);
	}
	
	class blockPostings extends postingIterator implements BlockPosting
	{
		public blockPostings(String[] _terms, int[] ids) {
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
			BlockPostingImpl fbp = new BlockPostingImpl(termIds[i], getFrequency(), getPositions());
			return fbp;
		}		
	}
	
	@Override
	public void clear() {
		super.clear();
		blockCount = 0;
		term_blocks.clear();
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		clear();
		final int termCount = WritableUtils.readVInt(in);
		for(int i=0;i<termCount;i++)
		{
			final String term = Text.readString(in);
			final int freq = WritableUtils.readVInt(in);
			final int bf = WritableUtils.readVInt(in);
			insert(freq, term);
			if (bf == 0)
				continue;
			final int[] blocks = new int[bf];
			blocks[0] = WritableUtils.readVInt(in)-1;
			for(int j=1;j<bf;j++)
				blocks[j] = WritableUtils.readVInt(in) - blocks[j-1];
			term_blocks.put(term, new TIntHashSet(blocks));
		}
	}

	@Override
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
						final int[] blocks = term_blocks.get(term).toArray();
						Arrays.sort(blocks);
						final int bf = blocks.length;
						WritableUtils.writeVInt(out, bf);
						if (bf == 0)
							return true;
						WritableUtils.writeVInt(out, blocks[0]+1);
						for(int i=1;i<bf;i++)
							WritableUtils.writeVInt(out, blocks[i] - blocks[i-1]); 
						
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
