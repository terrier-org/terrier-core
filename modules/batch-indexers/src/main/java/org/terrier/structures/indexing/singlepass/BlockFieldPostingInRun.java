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
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
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
class BlockFieldPostingInRun extends BlockPostingInRun{
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

	@Override
	public LexiconEntry getLexiconEntry() {
		FieldLexiconEntry fes = new FieldLexiconEntry(fieldTFs.length);
		fes.setStatistics(termDf, termTF);
		fes.setFieldFrequencies(fieldTFs);
		fes.setMaxFrequencyInDocuments(maxtf);
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

		@Override
		protected void readPostingNotDocid() throws IOException {
			super.readPostingNotDocid();
			for(int fi = 0; fi < fieldTags;fi++)
			{
				fieldFrequencies[fi] = postingSource.readUnary() -1;
			}
			blockFreq = postingSource.readUnary() -1;
			blockIds = new int[blockFreq];
			blockIds[0] = postingSource.readGamma()-1;
			for(int i=1;i<blockFreq;i++)
				blockIds[i] = postingSource.readGamma() + blockIds[i-1];
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
	public IterablePosting getPostingIterator(final int runShift)
	{
		return new bfPIRPostingIterator(runShift);
	}
}
