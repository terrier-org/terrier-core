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
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Roi Blanco (rblanc{at}@udc.es)
 *   Craig Macdonald (craigm{at}dcs.gla.ac.uk)
 */
package org.terrier.structures.indexing.singlepass;

import java.io.IOException;

import org.terrier.compression.bit.BitOut;
import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.IterablePostingImpl;
import org.terrier.structures.postings.WritablePosting;

/** Class holding the information for a posting list read
 * from a previously written run at disk. Used in the merging phase of the Single pass inversion method.
 * This class knows how to append itself to a {@link org.terrier.compression.bit.BitOut} and it
 * represents the simpler class of posting <code>(TF, df, [docid, tf])</code>
 * @author Roi Blanco
 *
 */
public class SimplePostingInRun extends PostingInRun {
	
	/**
	 * Constructor for the class.
	 */
	public SimplePostingInRun() {
		termTF = 0;
	}
	
	/**
	 * Writes the document data of this posting to a {@link org.terrier.compression.bit.BitOut} 
	 * It encodes the data with the right compression methods.
	 * The stream is written as <code>d1, idf(d1) , d2 - d1, idf(d2)</code> etc.
	 * @param bos BitOut to be written.
	 * @param last int representing the last document written in this posting.
	 * @param runShift int representing the last document read document read in this posting stream.
	 * @return The last posting written.
	 */
	public int append(BitOut bos, int last, int runShift) throws IOException {
		int current = runShift - 1;
		for(int i = 0; i < termDf; i++){
			final int docid = postingSource.readGamma() + current;
			bos.writeGamma(docid - last);
			bos.writeUnary(postingSource.readGamma());
			current = last = docid;		
		}
		try{
			postingSource.align();
		}catch(Exception e){
			// last posting
		}
		return last;
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

		public void setId(int id) {}	
		public void close() throws IOException {	}

		public WritablePosting asWritablePosting() {
			BasicPostingImpl bp = new BasicPostingImpl(docid, frequency);
			return bp;
		}
		
	}

	@Override
	public IterablePosting getPostingIterator(final int runShift) throws IOException 
	{
		return new PIRPostingIterator(runShift);
	}

}
