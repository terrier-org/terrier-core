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
 * The Original Code is IntegerCodingIterablePosting.java.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.compression.integer.ByteIn;
import org.terrier.compression.integer.codec.IntegerCodec;
import org.terrier.compression.integer.codec.util.Delta;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.FieldDocumentIndex;
import org.terrier.structures.FieldDocumentIndexEntry;
import org.terrier.structures.postings.BlockFieldPostingImpl;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.utility.ArrayUtils;

/**
 * An IterablePosting implementation, which can optionally support fields and/or blocks.
 * Its content is compressed using some integer coding technique.
 * Note: this class read the input in chunks of posting. The chunk size is specified in chunkSize
 * 
 * @author Matteo Catena
 * @since 4.0
 * @deprecated
 */
public class IntegerCodingIterablePosting implements IterablePosting, FieldPosting, BlockPosting{

	/** The logger used */
	protected static final Logger logger = LoggerFactory
			.getLogger(IntegerCodingIterablePosting.class);	
	
	protected DocumentIndex documentIndex;
	
	/**
	 * Number of postings in a chunk
	 */
	protected int chunkSize;
	
	protected int numberOfEntries;
	protected int currentPosting = -1;
	
	protected int id = -1; //current id
	protected int tf = -1; //current tf
	protected int[] fields;//current fields
	protected int bf = -1;//lenght of the current blocks arrays
	protected int[] blocks;//current blocks
		
	protected final int[] ids;//all the ids in the current chunk (uncompressed)
	protected final int[] tfs;//all the tfs in the current chunk (uncompressed)
	protected final int[][] fieldsMatrix;//all the ffs in the current chunk (uncompressed)
	protected int[] bfs;//the lenghts of the blocks array for every posting 
						//(required if the block size != 1 i.e. blocks are not positions)
	protected int[] blocksMatrix;//all the blocks in the current chunk (uncompressed)

	protected final int fieldsCount;
	protected FieldDocumentIndex asFieldDocumentIndex;
	protected final int hasBlocks;

	protected final ByteIn input;
	
	protected final IntegerCodec idsCodec;
	protected final IntegerCodec tfsCodec;
	protected final IntegerCodec fieldsCodec;
	protected final IntegerCodec blocksCodec;

	protected int blkCnt = 0;

	protected boolean decompressed = false;
	
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
	public IntegerCodingIterablePosting(
			ByteIn input, 
			int numberOfEntries, 
			DocumentIndex documentIndex,
			int chunkSize, 
			int fieldCount, int hasBlocks,
			IntegerCodec idsCodec,
			IntegerCodec tfsCodec, 
			IntegerCodec fieldsCodec, 
			IntegerCodec blocksCodec) throws IOException {
		
		this.input = input;
		this.numberOfEntries = numberOfEntries;
		this.documentIndex = documentIndex;
		asFieldDocumentIndex = (documentIndex instanceof FieldDocumentIndex) ? (FieldDocumentIndex) documentIndex : null;
		
		this.chunkSize = (numberOfEntries < chunkSize) ? numberOfEntries : chunkSize;
				
		this.fieldsCount = fieldCount;
		this.hasBlocks = hasBlocks;
		
		this.idsCodec = idsCodec;
		this.tfsCodec = tfsCodec;
		this.fieldsCodec = fieldsCodec;
		this.blocksCodec = blocksCodec;
		
		ids = new int[chunkSize];
		tfs = new int[chunkSize];
		if (fieldsCount > 0) {	
			
			fieldsMatrix = new int[fieldsCount][chunkSize];
			fields = new int[fieldsCount];
			
		} else {
			
			fieldsMatrix = null;
		}
		if (hasBlocks > 0) {
			
			if (hasBlocks > 1)
				bfs = new int[chunkSize];
			else
				bfs = tfs; //this a trick: if block size == 1, we have positions.
							//#positions == tf, so just reuse that and save space
			blocksMatrix = new int[chunkSize];
		}
		
		load();
	}

	@Override
	public int getId() {
		
		return id;
	}

	@Override
	public int getFrequency() {
		
		return tf;
	}

	@Override
	public int getDocumentLength() {
		
		try{
			
			return documentIndex.getDocumentLength(id);
			
		} catch (Exception e) {
			
			logger.error("Problem looking for doclength for document "+ id, e);
			return -1;
		}		
	}

	@Override
	public void setId(int id) {
		
		this.id = id;
		
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
	 * This load a chunk (just the document ids!) and decompress it
	 * @throws IOException
	 */
	protected final void load() throws IOException {
		
		chunkSize = (numberOfEntries > chunkSize) ? chunkSize : numberOfEntries;
		
		idsCodec.decompress(input, ids, chunkSize);
		Delta.inverseDelta(ids, chunkSize);
				
		currentPosting = -1;
		blkCnt = 0;
		
		decompressed = false;
	}
	
	/**
	 * This load the a chunk (tfs, fields and blocks (optionally)) and decompress it
	 * @throws IOException
	 */
	protected final void decompress() throws IOException {
		
		tfsCodec.decompress(input, tfs, chunkSize);
		
		if (fieldsCount > 0)
		{
			for (int j = 0; j < fieldsCount; j++) {
				fieldsCodec.decompress(input, fieldsMatrix[j], chunkSize);
			}
		}
		
		if (hasBlocks > 0)
		{
//			if (hasBlocks > 1) {
//				tfsCodec.decompress(input, bfs, chunkSize);
//			}
			tfsCodec.decompress(input, bfs, chunkSize);
			
			int numBlocks = 0; for (int i = 0; i < chunkSize; i++) numBlocks += bfs[i]; 
			blocksMatrix = ArrayUtils.growOrCreate(blocksMatrix, numBlocks);
			blocksCodec.decompress(input, blocksMatrix, numBlocks);
		}		
		
		decompressed = true;
	}	
	
	/**
	 * If tfs, fields frequencies and blocks are not required, skip that
	 * part of the chunk
	 * @throws IOException
	 */
	protected final void skip() throws IOException {
				
		tfsCodec.skip(input);
		
		if (fieldsCount > 0)
		{
			for (int j = 0; j < fieldsCount; j++) {
				fieldsCodec.skip(input);
			}
		}
		
		if (hasBlocks > 0)
		{			 
			blocksCodec.skip(input);
		}
	}
	
	/**
	 * Read the posting components from the chunk
	 * @param pos the posting to load (as index in the internal arrays)
	 * @throws IOException
	 */
	protected final void get(final int pos) throws IOException {
				
		if (!decompressed) decompress();
		
		id = ids[pos];
		tf = tfs[pos];				
		
		if (fieldsCount > 0) {

			for (int j = 0; j < fieldsCount; j++) fields[j] = fieldsMatrix[j][pos] - 1; //-1, to deal with gamma and unary codec
			
		}
		if (hasBlocks > 0) { 
		
			bf = bfs[pos];			
			for (int i = currentPosting + 1; i < pos; i++) blkCnt += bfs[i]; 
			blocks = Arrays.copyOfRange(blocksMatrix, blkCnt, blkCnt + bf);
			Delta.inverseDelta(blocks, blocks.length);
			blkCnt += bf; //<-- because currentPosting may start from -1
		}				
		
		numberOfEntries -= pos - currentPosting;
		currentPosting = pos;
		
	}

	@Override
	public int next() throws IOException {
						
		if (endOfPostings()) {
			
			return id = EOL;
			
		} else {
			
			if (currentPosting + 1 >= chunkSize) {
				load();
				decompress();
			}
			
		}
			
		get(currentPosting + 1);
		
		return id;
	}	
	
	@Override
	public int next(int targetId) throws IOException {
		
		if (targetId <= id) {
			
			logger.warn("You've tried to access a posting you've already iterated over");
			return id;
		}
				
		while (true) {
			
			int pos = Arrays.binarySearch(ids, currentPosting + 1, chunkSize, targetId);
									
			//pos is > 0: docid in chunk
			//pos is < 0 && -pos <= chunkSize: some docid > target is in chunk
			//-pos is > chunkSize: check next chunk
			if (pos >= 0 || (pos < 0 && -pos <= chunkSize)) {
								
				pos = (pos >= 0) ? pos : -(pos + 1);
				
				get(pos);
										
				return id;
				
			} else {
				
				numberOfEntries -= chunkSize - (currentPosting + 1);
				
				if (endOfPostings()) {
					
					return id = EOL;
				}
				else {
					
					if (!decompressed) skip();
					load();
				}
			}
		}
		
	}

	@Override
	public boolean endOfPostings() {

		return numberOfEntries <= 0;
	}

	@Override
	public int[] getPositions() {
		
		if (hasBlocks==0)
			throw new UnsupportedOperationException();

		return blocks;
	}

	@Override
	public int[] getFieldFrequencies() {

		if (fieldsCount <= 0)
			throw new UnsupportedOperationException();
		
		return fields;
	}

	@Override
	public int[] getFieldLengths() {
		
		if (fieldsCount <= 0)
			throw new UnsupportedOperationException();
		
		try {
			
			if (asFieldDocumentIndex != null) {
				
				return asFieldDocumentIndex.getFieldLengths(id);
			}
			else
			{
				FieldDocumentIndexEntry fdie = 
						((FieldDocumentIndexEntry)documentIndex.getDocumentEntry(id));
				return fdie.getFieldLengths();
				
			}	
			
		} catch (IOException ioe) {

			logger.error("Problem looking for doclength for document "+ id, ioe);			
			return new int[0];
		}
		
	}

	@Override
	public void setFieldLengths(int[] newLengths) {
		
		//TODO: what should we do here?
		throw new UnsupportedOperationException();
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
