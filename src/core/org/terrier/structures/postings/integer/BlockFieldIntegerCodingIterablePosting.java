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
 * The Original Code is BlockFieldIntegerCodingIterablePosting.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 */

package org.terrier.structures.postings.integer;

import java.io.IOException;
import java.util.Arrays;

import org.terrier.compression.integer.ByteIn;
import org.terrier.compression.integer.codec.IntegerCodec;
import org.terrier.compression.integer.codec.util.Delta;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.postings.BlockFieldPostingImpl;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.utility.ArrayUtils;

/**
 * An IterablePosting implementation, which can optionally support fields and/or blocks.
 * Its content is compressed using some integer coding technique.
 * Note: this class reads the input in chunks of posting. The chunk size is specified in chunkSize
 * 
 * @author Matteo Catena
 * @since 4.0
 */
public class BlockFieldIntegerCodingIterablePosting extends FieldIntegerCodingIterablePosting implements BlockPosting{

	
	protected int bf = -1;//length of the current blocks arrays
	protected int[] blocks;//current blocks
		
	protected int[] bfs;//the lengths of the blocks array for every posting 
						//(required if the block size != 1 i.e. blocks are not positions)
	protected int[] blocksMatrix;//all the blocks in the current chunk (uncompressed)

	protected final IntegerCodec blocksCodec;
	protected final int hasBlocks;
	protected final int maxBlocks;
	/**
	 * 
	 * @param input the input channel
	 * @param numberOfEntries the number of postings
	 * @param documentIndex the document index
	 * @param chunkSize the size of the chunk
	 * @param fieldCount the number of fields (0 if the posting list has no fields)
	 * @param hasBlocks has this posting list posting positions? (0: no, 1:has positions, >1:has blocks)
	 * @param idsCodec the IntegerCodec to use to decode docIds
	 * @param tfsCodec the IntegerCodec to use to decode term frequencies
	 * @param fieldsCodec the IntegerCodec to use to decode field frequencies (if any, null otherwise)
	 * @param blocksCodec the IntegerCodec to use to decode blocks (if any, null otherwise)
	 * @throws IOException
	 */
	public BlockFieldIntegerCodingIterablePosting(
			ByteIn input, 
			int numberOfEntries, 
			DocumentIndex documentIndex,
			int chunkSize, 
			int fieldCount,
			int hasBlocks,
			int maxBlocks,
			IntegerCodec idsCodec,
			IntegerCodec tfsCodec, 
			IntegerCodec fieldsCodec, 
			IntegerCodec blocksCodec) throws IOException {
		
		super(input,numberOfEntries, documentIndex, chunkSize, fieldCount, idsCodec, tfsCodec, fieldsCodec);

		this.blocksCodec = blocksCodec;
		this.hasBlocks = hasBlocks;
		this.maxBlocks = maxBlocks;
		if (hasBlocks > 0) {
			
			bfs = new int[chunkSize];
			
			//if (hasBlocks > 1)
			//	bfs = new int[chunkSize];
			//else
			//	bfs = tfs; //this a trick: if block size == 1, we have positions.
			//				//#positions == tf, so just reuse that and save space
			blocksMatrix = new int[chunkSize];
		}
	}


	@Override
	public WritablePosting asWritablePosting() {

		return new BlockFieldPostingImpl(id, tf, blocks, fields);
	}

	@Override
	public void close() throws IOException {
		
		input.close();		
	}
	
	
	
	/**
	 * This load the a chunk (tfs, fields and blocks (optionally)) and decompress it
	 * @throws IOException
	 */
	protected final void decompress() throws IOException {
		super.decompress();
		
		
		if (hasBlocks > 0)
		{
			tfsCodec.decompress(input, bfs, chunkSize);
			int numBlocks = 0;
			for (int i = 0; i < chunkSize; i++) numBlocks += bfs[i];
			
//			if (hasBlocks > 1) {
//				tfsCodec.decompress(input, bfs, chunkSize);
//			}
//			
//			int numBlocks = 0; 
//			if (maxBlocks > 0)
//				for (int i = 0; i < chunkSize; i++) numBlocks += Math.min(bfs[i], maxBlocks); 
//			else
//				for (int i = 0; i < chunkSize; i++) numBlocks += bfs[i]; 
			blocksMatrix = ArrayUtils.growOrCreate(blocksMatrix, numBlocks);
			blocksCodec.decompress(input, blocksMatrix, numBlocks);
		}		

	}	
	
	/**
	 * If tfs, fields frequencies and blocks are not required, skip that
	 * part of the chunk
	 * @throws IOException
	 */
	protected final void skip() throws IOException {
		super.skip();
		blocksCodec.skip(input);
	}
	
	/**
	 * Read the posting components from the chunk
	 * @param pos the posting to load (as index in the internal arrays)
	 * @throws IOException
	 */
	protected final void get(final int pos) throws IOException {
		super.get(pos);
		
		bf = bfs[pos];			
		for (int i = currentPosting + 1; i < pos; i++) blkCnt += bfs[i]; 
		blocks = Arrays.copyOfRange(blocksMatrix, blkCnt, blkCnt + bf);
		Delta.inverseDelta(blocks, blocks.length);
		blkCnt += bf; //<-- because currentPosting may start from -1
		
		numberOfEntries -= pos - currentPosting;
		currentPosting = pos;
		
	}

	

	@Override
	public int[] getPositions() {
		
		if (hasBlocks==0)
			throw new UnsupportedOperationException();

		return blocks;
	}

	
	
	/** Makes a human readable form of this posting */
	@Override
	public String toString()
	{
		String F = (fieldsCount > 0) ? ",F[" + ArrayUtils.join(fields, ",") + "]" : "";
		String B = (hasBlocks > 0) ? ",B[" + ArrayUtils.join(blocks, ",") + "]" : "";
		
		return "(" + id + "," + tf + F + B + ")";
	}


	public long getCurrentAddress() {

		return input.getByteOffset();
	}	
}
