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
 * The Original Code is BlockDirectInvertedOutputStream.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.bit;

import java.io.IOException;

import org.terrier.compression.bit.BitOut;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.SimpleBitIndexPointer;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.structures.postings.bit.BlockIterablePosting;

/** Writes a block direct or block inverted index, when passed appropriate posting lists.
  * @author Craig Macdonald
  * @since 2.0
  */
public class BlockDirectInvertedOutputStream extends DirectInvertedOutputStream {

	/** Creates a new output stream, writing a BitOutputStream to the specified file. The number of binary bits
	  * for fields must also be specified.
	  * @param filename Location of the file to write to
	  */
	public BlockDirectInvertedOutputStream(String filename) throws IOException
	{
		super(filename);
	}
	/** Creates a new output stream, writing to the specified BitOut implementation.  The number of binary bits
	  * for fields must also be specified.
	  * @param out BitOut implementation to write the file to 
	  */
	public BlockDirectInvertedOutputStream(BitOut out)
	{
		super(out);
	}
	
	@Override
	public Class<? extends IterablePosting> getPostingIteratorClass()
	{
		return BlockIterablePosting.class;
	}
	
	
	/**
	 * Writes the given block postings to the bit file. This method assumes that
	 * field information is not provided.
	 * @param postings the postings list to write.
	 * @param firstId the first identifier to write. This can be 
	 *        an id plus one, or the gap of the current id and the previous one.
	 * @param offset The location of the first posting to write out.
	 * @param length The number of postings to be written out.
	 * @throws IOException if an error occurs during writing to a file.
	 * 
	 */
	@Override
	protected BitIndexPointer writeNoFieldPostings(int[][] postings, int offset, final int length, int firstId) 
		throws IOException {
		BitIndexPointer rtr = new SimpleBitIndexPointer(output.getByteOffset(), output.getBitOffset(), length);
		
		//local variables in order to reduce the number
		//of times we need to access a two-dimensional array
		final int[] postings0 = postings[0];
		final int[] postings1 = postings[1];
		final int[] postings3 = postings[3];
		final int[] postings4 = postings[4];
		
		//write the first posting from the term's postings list
		output.writeGamma(firstId);						//write document id 
		output.writeUnary(postings1[offset]);    			//write frequency
		int blockIndex = 0;								//the index of the current block id
		if (offset != 0)
			for(int i=0;i<offset;i++)
				blockIndex += postings3[i];
				
		int blockFrequency = postings3[offset];				//the number of block ids to write
		output.writeUnary(blockFrequency + 1);    			//write block frequency
		if (blockFrequency  > 0)
		{
			output.writeGamma(postings4[blockIndex]+1);		//write the first block id
			blockIndex++;									//move to the next block id
			for (int i=1; i<blockFrequency; i++) {			//write the next blockFrequency-1 ids
				//write the gap between consequtive block ids
				output.writeGamma(postings4[blockIndex]-postings4[blockIndex-1]);
				blockIndex++;
			}
		}
		offset++;
		
		//write the rest of the postings from the term's postings list
		//final int length = postings0.length;
		for (; offset < length; offset++) {
			output.writeGamma(postings0[offset] - postings0[offset - 1]);	//write gap of document ids
			output.writeUnary(postings1[offset]);					//write term frequency
			blockFrequency = postings3[offset];							//number of block ids to write
			output.writeUnary(blockFrequency + 1);				//write block frequency
			if (blockFrequency > 0)
			{
				output.writeGamma(postings4[blockIndex]+1);		//write the first block id
				blockIndex++;											//move to the next block id
				for (int i=1; i<blockFrequency; i++) {
					//write the gap between consequtive block ids
					output.writeGamma(postings4[blockIndex]-postings4[blockIndex-1]);
					blockIndex++;
				}
			}
		}
		return rtr;
	}
	
	@Override
	protected void writePostingNotDocid(Posting _p) throws IOException
	{
		BlockPosting p = (BlockPosting)_p;
		output.writeUnary(p.getFrequency());
		final int positions[] = p.getPositions();
		final int l = positions.length;
		output.writeUnary(l+1);
		if (l== 0)
			return;
		//System.err.println("posting has " + l + "blocks");
		output.writeGamma(positions[0]+1);
		for(int i=1;i<l;i++)
		{
			output.writeGamma(positions[i] - positions[i-1]);
		}
	}
	

}
