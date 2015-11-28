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
 * The Original Code is BlockFieldPostingInRun.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Roi Blanco (rblanc{at}@udc.es)
 *   Craig Macdonald (craigm{at}dcs.gla.ac.uk)
 */
package org.terrier.structures.indexing.singlepass;

import java.io.IOException;
import java.util.Arrays;

import org.terrier.compression.bit.BitOut;
import org.terrier.structures.FieldLexiconEntry;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.postings.BlockFieldPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.WritablePosting;

/** Class holding the information for a posting list read
 * from a previously written run at disk. Used in the merging phase of the Single pass inversion method.
 * This class knows how to append itself to a {@link org.terrier.compression.bit.BitOut} and it
 * represents a posting with blocks and field information <code>(tf, df, [docid, idf, fieldScore, blockFr [blockid]])</code>
 * @author Roi Blanco
 *
 */
public class BlockFieldPostingInRun extends BlockPostingInRun{
	/** The number of different fields that are used for indexing field information.*/	
	protected final int fieldTags;
	
	protected final int[] fieldTFs;
	
	/**
	 * Constructor for the class.
	 */
	public BlockFieldPostingInRun(int _fieldTags) {
		super();
		fieldTags = _fieldTags;
		fieldTFs = new int[fieldTags];
	}

	/**
	 * Writes the document data of this posting to a {@link org.terrier.compression.bit.BitOut} 
	 * It encodes the data with the right compression methods.
	 * The stream is written as <code>d1, idf(d1), fields, blockNo(d1), bid1, bid2, ...,  d2 - d1, idf(d2), fields, blockNo(d2), ...</code> etc
	 * @param bos BitOut to be written.
	 * @param last int representing the last document written in this posting.
	 * @param runShift amount of delta to apply to the first posting read.
	 * @return The last posting written.
	 */	
	public int append(BitOut bos, int last, int runShift)  throws IOException{
		int current = runShift - 1;
		for(int i = 0; i < termDf; i++){
			int docid = postingSource.readGamma() + current;
			bos.writeGamma(docid - last);
			bos.writeUnary(postingSource.readGamma());
			current = last = docid;
			//deal with fields
			for(int f=0;f<fieldTags;f++)
			{
				int tff = postingSource.readUnary() -1;
				fieldTFs[f] += tff;
				bos.writeUnary(tff +1);
			}
			//now deal with blocks
			final int numOfBlocks = postingSource.readUnary() -1;
			bos.writeUnary(numOfBlocks+1);
			if (numOfBlocks > 0)
				for(int j = 0; j < numOfBlocks; j++){
					/* we're reading and saving gaps here, not blockids */
					bos.writeGamma(postingSource.readGamma());
				}
		}
		try{
			postingSource.align();
		}catch(Exception e){
			// last posting
		}
		return last;
	}
	
	@Override
	public LexiconEntry getLexiconEntry() {
		FieldLexiconEntry fes = new FieldLexiconEntry(fieldTFs.length);
		fes.setStatistics(termDf, termTF);
		fes.setFieldFrequencies(fieldTFs);
		return fes;
	}
	
	@Override
	public void addToLexiconEntry(LexiconEntry _le)
	{
		super.addToLexiconEntry(_le);
		FieldLexiconEntry le = (FieldLexiconEntry)_le;
		int[] tffs = le.getFieldFrequencies();
		addTo(tffs, fieldTFs);
	}
	
	protected static void addTo(int[] target, int[] additions)
	{
		for(int i=0;i<target.length;i++)
		{
			target[i] += additions[i];
		}
	}
	
	protected class bfPIRPostingIterator extends PIRPostingIterator implements 
		org.terrier.structures.postings.FieldPosting, 
		org.terrier.structures.postings.BlockPosting
	{
		protected int[] fieldFrequencies = new int[fieldTags];
		protected int blockFreq;
		protected int[] blockIds;
		
		public bfPIRPostingIterator(int runShift) {
			super(runShift);
		}

		public int[] getFieldFrequencies() {
			return fieldFrequencies;
		}

		public int[] getFieldLengths() {
			return null;
		}
		
		/** {@inheritDoc}.
		 * This operation is unsupported. */
		@Override
		public void setFieldLengths(int[] fl) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void readPostingNotDocid() throws IOException {
			super.readPostingNotDocid();
			for(int fi = 0; fi < fieldTags;fi++)
			{
				fieldFrequencies[fi] = postingSource.readUnary() -1;
			}
			blockIds = new int[postingSource.readUnary() -1];
			blockIds[0] = postingSource.readGamma()-1;
			for(int i=1;i<blockFreq;i++)
				blockIds[i] = postingSource.readGamma() - blockIds[i-1];
		}

		public int[] getPositions() {
			return blockIds;
		}
		
		public WritablePosting asWritablePosting() {
			BlockFieldPostingImpl bp = new BlockFieldPostingImpl(docid, frequency, blockIds, fieldFrequencies.length);
			System.arraycopy(fieldFrequencies, 0, bp.getFieldFrequencies(), 0, fieldFrequencies.length);
			return bp;
		}
				
	}
	
	@Override
	public void setTerm(String term) {
		super.setTerm(term);
		Arrays.fill(fieldTFs, 0);
	}
	
	@Override
	public IterablePosting getPostingIterator(final int runShift) throws IOException 
	{
		return new bfPIRPostingIterator(runShift);
	}
}
