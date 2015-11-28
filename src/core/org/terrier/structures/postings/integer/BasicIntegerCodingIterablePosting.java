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
 * The Original Code is BasicIntegerCodingIterablePosting.java.
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
import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.WritablePosting;

/**
 * An IterablePosting implementation, which can optionally support fields and/or blocks.
 * Its content is compressed using some integer coding technique.
 * Note: this class read the input in chunks of posting. The chunk size is specified in chunkSize
 * 
 * @author Matteo Catena
 * @since 4.0
 */
public class BasicIntegerCodingIterablePosting implements IterablePosting{

	/** The logger used */
	protected static final Logger logger = LoggerFactory
			.getLogger(BasicIntegerCodingIterablePosting.class);	
	
	protected DocumentIndex documentIndex;
	
	/**
	 * Number of postings in a chunk
	 */
	protected int chunkSize;
	
	protected int numberOfEntries;
	protected int currentPosting = -1;
	
	protected int id = -1; //current id
	protected int tf = -1; //current tf
		
	protected final int[] ids;//all the ids in the current chunk (uncompressed)
	protected final int[] tfs;//all the tfs in the current chunk (uncompressed)

	protected final ByteIn input;
	
	protected final IntegerCodec idsCodec;
	protected final IntegerCodec tfsCodec;
	
	protected int blkCnt = 0;

	protected boolean decompressed = false;
	
	/**
	 * 
	 * @param input the input channel
	 * @param numberOfEntries the number of postings
	 * @param documentIndex the document index
	 * @param chunkSize the size of the chunk
	 * @param idsCodec the IntegerCodec to use to decode docIds
	 * @param tfsCodec the IntegerCodec to use to decode term frequencies
	 * @throws IOException
	 */
	public BasicIntegerCodingIterablePosting(
			ByteIn input, 
			int numberOfEntries, 
			DocumentIndex documentIndex,
			int chunkSize, 
			IntegerCodec idsCodec,
			IntegerCodec tfsCodec) throws IOException {
		
		this.input = input;
		this.numberOfEntries = numberOfEntries;
		this.documentIndex = documentIndex;
		
		this.chunkSize = (numberOfEntries < chunkSize) ? numberOfEntries : chunkSize;
				
		
		this.idsCodec = idsCodec;
		this.tfsCodec = tfsCodec;
		
		ids = new int[chunkSize];
		tfs = new int[chunkSize];
				
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

		return new BasicPostingImpl(id, tf);
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
	 * This load the a chunk (tfs) and decompress it
	 * @throws IOException
	 */
	protected void decompress() throws IOException {
		
		tfsCodec.decompress(input, tfs, chunkSize);	
		decompressed = true;
	}	
	
	/**
	 * If tfs, fields frequencies and blocks are not required, skip that
	 * part of the chunk
	 * @throws IOException
	 */
	protected void skip() throws IOException {
				
		tfsCodec.skip(input);
	}
	
	/**
	 * Read the posting components from the chunk
	 * @param pos the posting to load (as index in the internal arrays)
	 * @throws IOException
	 */
	protected void get(final int pos) throws IOException {
				
		if (!decompressed) decompress();
		
		id = ids[pos];
		tf = tfs[pos];				
		
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

	
	
	/** Makes a human readable form of this posting */
	@Override
	public String toString()
	{
		return "(" + id + "," + tf + ")";
	}


	public long getCurrentAddress() {

		return input.getByteOffset();
	}	
}
