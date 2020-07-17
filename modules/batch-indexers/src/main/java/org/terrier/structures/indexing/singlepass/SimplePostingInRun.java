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
 * The Original Code is SimplePostingInRun.java.
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

import org.apache.commons.lang3.tuple.Pair;
import org.terrier.compression.bit.BitOut;
import org.terrier.structures.Pointer;
import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.IterablePostingImpl;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.structures.AbstractPostingOutputStream;
import org.terrier.structures.Pointer;

/** Class holding the information for a posting list read
 * from a previously written run at disk. Used in the merging phase of the Single pass inversion method.
 * This class knows how to append itself to a {@link org.terrier.compression.bit.BitOut} and it
 * represents the simpler class of posting <code>(TF, df, maxtf, [docid, tf])</code>
 * @author Roi Blanco
 *
 */
class SimplePostingInRun extends PostingInRun {
	
	public SimplePostingInRun() {
		termTF = 0;
	}

	public Pair<Integer,Pointer> append(AbstractPostingOutputStream pos, int last, int runShift) throws IOException {
		IterablePosting postings = this.getPostingIterator(runShift);
		Pointer p = pos.writePostings(postings, last);
		//getId() is still valid.
		return Pair.of(postings.getId(), p);
	}	
	
	protected class PIRPostingIterator extends IterablePostingImpl
	{
		int docid;
		int frequency;
		int i = 0;
		
		public PIRPostingIterator(int runShift)
		{
			docid = runShift -1;
		}
		
		protected void readPostingNotDocid() throws IOException
		{
			frequency = postingSource.readGamma();
		}
		
		public int next() throws IOException 
		{
			if (i>= termDf)
			{
				postingSource.align();
				return EOL;					
			}
			docid = postingSource.readGamma() + docid;
			readPostingNotDocid();
			i++;
			return docid;
		}
		
		public boolean endOfPostings()
		{
			return (i>= termDf);
		}


		public int getDocumentLength() {
			return -1;
		}

		public int getFrequency() {
			return frequency;
		}

		public int getId() {
			return docid;
		}

		public void close() throws IOException {	}

		public WritablePosting asWritablePosting() {
			BasicPostingImpl bp = new BasicPostingImpl(docid, frequency);
			return bp;
		}
		
	}

	@Override
	public IterablePosting getPostingIterator(final int runShift)
	{
		return new PIRPostingIterator(runShift);
	}

}
