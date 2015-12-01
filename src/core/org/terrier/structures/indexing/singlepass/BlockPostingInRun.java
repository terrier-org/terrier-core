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
 * The Original Code is BlockPostingInRun.java.
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
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.BlockPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.WritablePosting;
/** Class holding the information for a posting list read
 * from a previously written run at disk. Used in the merging phase of the Single pass inversion method.
 * This class knows how to append itself to a {@link org.terrier.compression.bit.BitOut} and it
 * represents a posting with blocks information <code>(tf, df, [docid, idf, blockFr [blockid]])</code>
 * @author Roi Blanco
 *
 */
public class BlockPostingInRun extends SimplePostingInRun
{
	/**
	 * Constructor for the class.
	 */
	public BlockPostingInRun(){
		super();
	}

	/**
	 * Writes the document data of this posting to a {@link org.terrier.compression.bit.BitOut} 
	 * It encodes the data with the right compression methods.
	 * The stream is written as <code>d1, idf(d1), blockNo(d1), bid1, bid2, ...,  d2 - d1, idf(d2), blockNo(d2), ...</code> etc
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
	
	protected class BlockPIRPostingIterator extends PIRPostingIterator implements BlockPosting
	{
		int blockFreq;
		int[] blockIds;
		public BlockPIRPostingIterator(int runShift) {
			super(runShift);
		}

		@Override
		protected void readPostingNotDocid() throws IOException {
			super.readPostingNotDocid();
			blockIds = new int[postingSource.readUnary() -1];
			blockIds[0] = postingSource.readGamma()-1;
			for(int i=1;i<blockFreq;i++)
				blockIds[i] = postingSource.readGamma() - blockIds[i-1];
		}

		public int[] getPositions() {
			return blockIds;
		}
		
		public WritablePosting asWritablePosting() {
			WritablePosting bp = new BlockPostingImpl(docid, frequency, blockIds);
			return bp;
		}
	}

	@Override
	public IterablePosting getPostingIterator(int runShift) throws IOException {
		return new BlockPIRPostingIterator(runShift);
	}
}
