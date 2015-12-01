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
 * The Original Code is IntegerCodingPostingOutputStream.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Matteo Catena
 */

package org.terrier.structures.integer;

import java.io.IOException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.compression.integer.ByteOut;
import org.terrier.compression.integer.ByteOutputStream;
import org.terrier.compression.integer.codec.IntegerCodec;
import org.terrier.compression.integer.codec.util.Delta;
import org.terrier.structures.AbstractPostingOutputStream;
import org.terrier.structures.BitFilePosition;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.FilePosition;
import org.terrier.structures.SimpleBitIndexPointer;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.structures.postings.integer.BasicIntegerCodingIterablePosting;
import org.terrier.structures.postings.integer.BlockFieldIntegerCodingIterablePosting;
import org.terrier.structures.postings.integer.BlockIntegerCodingIterablePosting;
import org.terrier.structures.postings.integer.FieldIntegerCodingIterablePosting;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.StaTools;

/**
 * This class is used to store a posting list to the disk, compressing it using some
 * integer coding technique. It can be used to save IterablePosting with (optionally)
 * fields and/or blocks.
 * Note: this class writes the posting list in chunks, whose size is specified by chunkSize.
 * 
 * @author Matteo Catena
 * @since 4.0
 */
public class IntegerCodingPostingOutputStream extends AbstractPostingOutputStream {

	/** The logger used */
	protected static final Logger logger = LoggerFactory
			.getLogger(IntegerCodingPostingOutputStream.class);
	
	protected ByteOut output;
	
	/**
	 * Number of posting in a chunk
	 */
	protected int chunkSize;

	protected int fieldsCount;
	protected int hasBlocks;
	protected int maxBlocks;
	
	protected int[] ids; //the ids in the chunk
	protected int[] tfs; //the tfs in the chunk
	protected int[][] fields; //the ffs in the chunk 
	protected int[] bfs; //block frequencies (i.e. lenght of the posting's blocks)
	protected int[] blocks; //the blocks for every posting in the chunk
		
	protected IntegerCodec idsCodec;
	protected IntegerCodec tfsCodec;
	protected IntegerCodec fieldsCodec;
	protected IntegerCodec blocksCodec;

	
	protected void init(
			int fieldsCount, int hasBlocks, int maxBlocks,
			int chunkSize, 
			IntegerCodec idsCodec,
			IntegerCodec tfsCodec, 
			IntegerCodec fieldsCodec, 
			IntegerCodec blocksCodec) {
		
		this.chunkSize = chunkSize;

		ids = new int[chunkSize];
		tfs = new int[chunkSize];

		if (fieldsCount > 0) {
			this.fieldsCount = fieldsCount;
			fields = new int[fieldsCount][chunkSize];
		}
		if ((this.hasBlocks = hasBlocks) > 0) {
			
			//if (hasBlocks > 1)
				bfs = new int[chunkSize];
			//else
			//	bfs = tfs; //trick!! if hasBlocks == 1 we're dealing with positions
							//since positions.length=tf, just use tfs and save space!
			blocks = new int[chunkSize];
		}
		this.maxBlocks = maxBlocks;
		this.idsCodec = idsCodec;
		this.tfsCodec = tfsCodec;
		this.fieldsCodec = fieldsCodec;
		this.blocksCodec = blocksCodec;
	}
	
	/**
	 * 
	 * @param filename the file where to write
	 * @param chunkSize the chunk size (in term of number of posting)
	 * @param fieldsCount the number of fields, if any. 0 otherwise
	 * @param hasBlocks 0:no blocks, 1:positions, 2:blocks of any size!=1
	 * @param maxBlocks 0:no limit, >0 limited.
	 * @param idsCodec the IntegerCodec to use to compress ids 
	 * @param tfsCodec the IntegerCodec to use to compress tfs
	 * @param fieldsCodec the IntegerCodec to use to compress fields (null if there are no fields)
	 * @param blocksCodec the IntegerCodec to use to compress blocks (null if there are no blocks)
	 * @throws IOException
	 */
	public IntegerCodingPostingOutputStream(
			String filename,
			int chunkSize,
			int fieldsCount, int hasBlocks, int maxBlocks,
			IntegerCodec idsCodec,
			IntegerCodec tfsCodec,
			IntegerCodec fieldsCodec,
			IntegerCodec blocksCodec) throws IOException {
		
		this.output = new ByteOutputStream(filename);
		init(fieldsCount, hasBlocks, maxBlocks, chunkSize, idsCodec, tfsCodec, fieldsCodec, blocksCodec);
	}


	/**
	 * 
	 * @param output the output channel
	 * @param chunkSize the chunk size (in term of number of posting)
	 * @param fieldsCount the number of fields, if any. 0 otherwise
	 * @param hasBlocks 0:no blocks, 1:positions, 2:blocks of any size!=1
	 * @param maxBlocks 0:no limit, >0 limited.
	 * @param idsCodec the IntegerCodec to use to compress ids 
	 * @param tfsCodec the IntegerCodec to use to compress tfs
	 * @param fieldsCodec the IntegerCodec to use to compress fields (null if there are no fields)
	 * @param blocksCodec the IntegerCodec to use to compress blocks (null if there are no blocks)
	 * @throws IOException
	 */
	public IntegerCodingPostingOutputStream(
			ByteOut output,
			int chunkSize,
			int fieldsCount, int hasBlocks, int maxBlocks,
			IntegerCodec idsCodec,
			IntegerCodec tfsCodec,
			IntegerCodec fieldsCodec,
			IntegerCodec blocksCodec) throws IOException {
		
		this.output = output;
		init(fieldsCount, hasBlocks, maxBlocks, chunkSize, idsCodec, tfsCodec, fieldsCodec, blocksCodec);
	}	
	
	/**
	 * Returns the IterablePosting class to use for reading structure written by
	 * this class
	 */
	public Class<? extends IterablePosting> getPostingIteratorClass() {
		
		return fieldsCount > 0 
				? hasBlocks > 0 ? BlockFieldIntegerCodingIterablePosting.class : FieldIntegerCodingIterablePosting.class
				: hasBlocks > 0 ? BlockIntegerCodingIterablePosting.class : BasicIntegerCodingIterablePosting.class;
	}

	/**
	 * Write out the specified postings.
	 * 
	 * @param postings
	 *            IterablePosting postings accessed through an IterablePosting
	 *            object
	 */
	@Override
	public BitIndexPointer writePostings(IterablePosting postings)
			throws IOException {
				
		FieldPosting asFieldPosting = null;
		if (fieldsCount > 0) asFieldPosting = (FieldPosting) postings;
		BlockPosting asBlockPosting = null;
		if (hasBlocks > 0) asBlockPosting = (BlockPosting) postings;
				
		BitIndexPointer pointer = new SimpleBitIndexPointer();
		pointer.setOffset(output.getByteOffset(), (byte) 0);

		int numberOfEntries = 0;
		
		while (!postings.endOfPostings()) {
			
			/* fill a chunk */
			int i = 0; //number of postings
			int cnt = 0; //number of blocks
			for (; i < chunkSize && !postings.endOfPostings(); i++) {//for each chunk
			
				postings.next();
				ids[i] = postings.getId();
				tfs[i] = postings.getFrequency();
				if (fieldsCount > 0) {
					
					int[] postingField = asFieldPosting.getFieldFrequencies();
					for (int j = 0; j < fieldsCount; j++) fields[j][i] = postingField[j] + 1; //+1, to deal with gamma and unary coding 
					
				}
				
				if (hasBlocks > 0) {
											
					int[] b = asBlockPosting.getPositions();					
					bfs[i] = b.length;
					
//					if (hasBlocks > 1)
//					{
//						bfs[i] = b.length;
//					}
//					else if (maxBlocks == 0)
//					{//hasBlocks == 1: we are assuming that frequency is a replacement for bfs as there is no upper limit on block size
//						assert b.length == postings.getFrequency();
//					}
//					else
//					{//hasBlocks == 1: we are assuming that frequency is a replacement for bfs as there is no upper limit on block size
//						assert maxBlocks > 0;
//						assert b.length <= maxBlocks;
//						assert b.length == Math.min(maxBlocks, tfs[i])
//								: "Posting=" +postings.asWritablePosting().toString() + " maxBlocks="+maxBlocks;
//						
//						bfs[i] = b.length;
//					}
					Delta.delta(b, b.length);
					
					blocks = ArrayUtils.grow(blocks, cnt + b.length);
					System.arraycopy(b, 0, blocks, cnt, b.length);
					cnt += b.length;
				}
				
				numberOfEntries++;
			}
						
			/* compress its parts */
			write(i, cnt);						
		}

		pointer.setNumberOfEntries(numberOfEntries);
		return pointer;
	}
	
	/**
	 * compress and write down the current chunk of postings
	 * 
	 * @param i number of postings
	 * @param cnt number of blocks
	 * @throws IOException
	 */
	protected void write(int i, int cnt) throws IOException {
		
		Delta.delta(ids, i);
		idsCodec.compress(ids, i, output);
		//System.err.println("tfs="+ Arrays.toString(Arrays.copyOf(tfs, i)));
		tfsCodec.compress(tfs, i, output);
		 
		if (fieldsCount > 0)
		{						
			for (int j = 0; j < fieldsCount; j++)
				fieldsCodec.compress(fields[j], i, output);					
		}			
		if (hasBlocks > 0) 
		{	
//			if (hasBlocks == 1)
//			{
//				//don't do anything, we can rely on the tfs, even if maxBlocks is set.
//				assert maxBlocks == 0 || StaTools.sum(bfs, i) == arraySumMin(maxBlocks, tfs, i)
//						: "maxBlocks="+maxBlocks 
//								+" i=" + i
//								+ " sumBfs=" + StaTools.sum(bfs, i)  
//								+ " maxBlockTFSum="+ arraySumMin(maxBlocks, tfs, i)
//								+ " tfs="+Arrays.toString(Arrays.copyOf(tfs, i)) 
//								+ " bfs="+Arrays.toString(Arrays.copyOf(bfs, i)); 
//				assert maxBlocks > 0 || arrayEquals(tfs, bfs, i);				
//			}
//			else
//			{
//				assert cnt == StaTools.sum(bfs, i);
//				tfsCodec.compress(bfs, i, output);
//			}
			
			assert cnt == StaTools.sum(bfs, i);
			tfsCodec.compress(bfs, i, output);
			blocksCodec.compress(blocks, cnt, output);
		}				
	}
	
	static boolean arrayEquals(final int[] a, final int[] b, final int l)
	{
		for(int i=0;i<l;i++)
		{
			if (a[i] != b[i])
				return false;
		}
		return true;
	}
	
	static int arraySumMin(final int min, final int[] ar, final int length)
	{
		int sum = 0;
		int i = 0;
		for(int a : ar)
		{
			sum += Math.min(a, min);
			if (++i == length)
				break;
		}
		return sum;
	}

	/** close this object. suppresses any exception */
	public void close() {
		
		try {

			output.close();

		} catch (IOException ioe) {
			
			logger.error("Problem closing the output stream", ioe);
		}
	}

	/** What is current offset? */
	public BitFilePosition getOffset() {
				
		return new FilePosition(output.getByteOffset(), (byte)0);
	}

	@Override
	public BitIndexPointer writePostings(int[][] postings, int startOffset,
			int Length, int firstId) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public BitIndexPointer writePostings(IterablePosting postings,
			int previousId) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public BitIndexPointer writePostings(Iterator<Posting> iterator,
			int previousId) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public BitIndexPointer writePostings(Iterator<Posting> iterator)
			throws IOException {
		throw new UnsupportedOperationException();
	}
}
