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
 * The Original Code is FieldPostingInRun.java.
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
import org.terrier.structures.FieldEntryStatistics;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.postings.FieldPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.WritablePosting;
/** Class holding the information for a posting list read
 * from a previously written run at disk. Used in the merging phase of the Single pass inversion method.
 * This class knows how to append itself to a {@link org.terrier.compression.bit.BitOut} and it
 * represents a posting with field information <code>(tf, df, [docid, tf, [tff]])</code>
 * @author Roi Blanco
 *
 */
class FieldPostingInRun extends SimplePostingInRun {
		
	protected final int[] fieldTFs;
	
	/** The number of different fields that are used for indexing field information.*/	
	protected final int fieldTags;
	
	/**
	 * Constructor for the class.
	 */
	public FieldPostingInRun(int _fieldTags) {
		super();
		fieldTags = _fieldTags;
		fieldTFs = new int[fieldTags];
	}	

	
	@Override
	public void addToLexiconEntry(LexiconEntry _le)
	{
		super.addToLexiconEntry(_le);
		FieldEntryStatistics le = (FieldEntryStatistics)_le;
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
	

	protected class fPIRPostingIterator extends PIRPostingIterator implements org.terrier.structures.postings.FieldPosting
	{
		protected int[] fieldFrequencies = new int[fieldTags];
		public fPIRPostingIterator(int runShift) {
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
				fieldTFs[fi] += fieldFrequencies[fi];
			}
		}
		
		public WritablePosting asWritablePosting() {
			FieldPostingImpl bp = new FieldPostingImpl(docid, frequency, fieldFrequencies.length);
			System.arraycopy(fieldFrequencies, 0, bp.getFieldFrequencies(), 0, fieldFrequencies.length);
			return bp;
		}				
	}
	
	@Override
	public IterablePosting getPostingIterator(final int runShift)
	{
		return new fPIRPostingIterator(runShift);
	}
	
	@Override
	public void setTerm(String term) {
		super.setTerm(term);
		Arrays.fill(fieldTFs, 0);
	}
	
}
