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
 * The Original Code is BlockDirectIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Douglas Johnson <johnsoda{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> 
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures.bit;
import gnu.trove.TIntArrayList;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.compression.bit.BitIn;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.bit.BlockIterablePosting;
/**
 * Extended direct index that saves both block 
 * and field information about the terms that 
 * appear in a document.
 * @author Douglas Johnson, Vassilis Plachouras
 * @deprecated
 */
public class BlockDirectIndex extends DirectIndex {
    protected int DocumentBlockCountDelta = 1;
    
	/** The logger used */
	private static Logger logger = LoggerFactory.getLogger(BlockDirectIndex.class);
	/**
	 * Constructs an instance of the class with 
	 * the given index, using the specified structure name.
	 * @param index The index to be used
	 * @param structureName the name of this direct index
	 * @throws IOException 
	 */
	public BlockDirectIndex(IndexOnDisk index, String structureName) throws IOException {
		super(index, structureName, BlockIterablePosting.class);
	}
	/**
	 * Constructs an instance of the class with
	 * @param index
	 * @param structureName
	 * @param postingClass
	 * @throws IOException
	 */
	public BlockDirectIndex(IndexOnDisk index, String structureName,
			Class<? extends IterablePosting> postingClass) throws IOException 
	{
		super(index, structureName, postingClass);
	}
	
	/**
	 * Returns a five dimensional array containing the 
	 * term ids and the term frequencies for the given document. 
	 * @return int[][] a five dimensional array containing 
	 *         the term ids, frequencies, field scores, 
	 *         block frequencies and the containing the block ids.
	 * @param docid the id of the document whose terms we are looking for.
	 */
	public int[][] getTerms(int docid) throws IOException
	{
		DocumentIndexEntry de = docIndex.getDocumentEntry(docid);
		if (de == null)
			return null;
		if (de.getNumberOfEntries() == 0)
			return null;
		return getTerms(de);
	}
	
	/** 
	 * {@inheritDoc} 
	 */		
	public int[][] getTerms(BitIndexPointer pointer) throws IOException {
		final long startOffset = pointer.getOffset();
		final byte startBitOffset = pointer.getOffsetBits();
		final int df = pointer.getNumberOfEntries();
		
		final boolean loadTagInformation = fieldCount > 0;
		
		final int[][] documentTerms = new int[4+fieldCount][];
		for(int i=0;i<fieldCount+3;i++)
			documentTerms[i] = new int[df];
		final TIntArrayList blockids = new TIntArrayList(df); //ideally we'd have TF here

		try{
			final BitIn file = this.file[pointer.getFileNumber()].readReset(startOffset, startBitOffset);
	
			if (loadTagInformation) { //if there are tag information to process
				//documentTerms[2] = new int[df]; 
				documentTerms[0][0] = file.readGamma() - 1;				
				documentTerms[1][0] = file.readUnary();
				for(int fi=0;fi < fieldCount;fi++)
					documentTerms[2+fi][0] = file.readUnary() -1;
				int blockfreq = documentTerms[2+fieldCount][0] = file.readUnary() - DocumentBlockCountDelta;
				int tmpBlocks[] = new int[blockfreq];
				int previousBlockId = -1;
				for(int j=0;j<blockfreq;j++)
				{
					tmpBlocks[j] = previousBlockId = file.readGamma() + previousBlockId;
				}
				blockids.add(tmpBlocks);
				
				for (int i = 1; i < df; i++) {					
					documentTerms[0][i]  = file.readGamma() + documentTerms[0][i - 1];
					documentTerms[1][i]  = file.readUnary();
					for(int fi=0;fi < fieldCount;fi++)
						documentTerms[2+fi][i] = file.readUnary() -1;
					blockfreq = documentTerms[2+fieldCount][i] = file.readUnary() - DocumentBlockCountDelta;
					tmpBlocks = new int[blockfreq];
					previousBlockId = -1;
					for(int j=0;j<blockfreq;j++)
					{
						tmpBlocks[j] = previousBlockId = file.readGamma() + previousBlockId;
					}
					blockids.add(tmpBlocks);
				}
			} else { //no tag information to process					
				
				documentTerms[0][0] = file.readGamma() - 1;
				documentTerms[1][0] = file.readUnary();
				
				int blockfreq = documentTerms[2][0] = file.readUnary() - DocumentBlockCountDelta;
				int tmpBlocks[] = new int[blockfreq];
				int previousBlockId = -1;
				for(int j=0;j<blockfreq;j++)
				{
					tmpBlocks[j] = previousBlockId = file.readGamma() + previousBlockId;
				}
				blockids.add(tmpBlocks);
				
				for (int i = 1; i < df; i++) {					
					documentTerms[0][i]  = file.readGamma() + documentTerms[0][i - 1];
					documentTerms[1][i]  = file.readUnary();

					blockfreq = documentTerms[2][i] = file.readUnary() - DocumentBlockCountDelta;
					tmpBlocks = new int[blockfreq];
					previousBlockId = -1;
					for(int j=0;j<blockfreq;j++)
					{
						tmpBlocks[j] = previousBlockId = file.readGamma() + previousBlockId;
					}
					blockids.add(tmpBlocks);
				}
			}
			documentTerms[documentTerms.length-1] = blockids.toNativeArray();
			return documentTerms;
		} catch (IOException ioe) {
			logger.error("Problem reading block inverted index", ioe);
			return null;
		}

	}
}
