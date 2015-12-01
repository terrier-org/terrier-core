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
 * The Original Code is BlockInvertedIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *	 Douglas Johnson <johnsoda{a.}dcs.gla.ac.uk> (original author)
 *	 Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> 
 */
package org.terrier.structures.bit;
import gnu.trove.TIntArrayList;

import java.io.IOException;

import org.terrier.compression.bit.BitIn;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.IndexConfigurable;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.bit.BlockIterablePosting;
/**
 * This class implements the block field inverted 
 * index for performing retrieval.
 * @author Douglas Johnson, Craig Macdonald et al.
 * @deprecated
 */
public class BlockInvertedIndex extends InvertedIndex implements IndexConfigurable {
	protected int DocumentBlockCountDelta = 1;
	/**
	 * Constructs an instance of the class with	
	 * @param index
	 * @param structureName
	 * @param _doi
	 * @param postingClass
	 * @throws IOException
	 */
	public BlockInvertedIndex(IndexOnDisk index, String structureName, DocumentIndex _doi, Class<? extends IterablePosting> postingClass) throws IOException
	{
		super(index, structureName, _doi, postingClass);
	}
	/**
	 * Constructs an instance of the class with
	 * @param index
	 * @param structureName
	 * @param doi
	 * @throws IOException
	 */
	public BlockInvertedIndex(IndexOnDisk index, String structureName, DocumentIndex doi) throws IOException {
		super(index, structureName, doi, BlockIterablePosting.class);
	}
	
	/**
	 * Constructs an instance of the class with
	 * @param index
	 * @param structureName
	 * @throws IOException
	 */
	public BlockInvertedIndex(IndexOnDisk index, String structureName) throws IOException {
		this(index, structureName, index.getDocumentIndex());
	}

	/** let it know which index to use */
	public void setIndex(Index i)
	{
		DocumentBlockCountDelta = i.getIntIndexProperty("blocks.invertedindex.countdelta", 1);
	}

				
	/**
	 * Returns a 2D array containing the document ids, 
	 * the term frequencies, the field scores the block frequencies and 
	 * the block ids for the given documents. 
	 * @return int[][] the five dimensional [5][] array containing 
	 *				 the document ids, frequencies, field scores and block 
	 *				 frequencies, while the last vector contains the 
	 *				 block identifiers and it has a different length from 
	 *				 the document identifiers.
	 * @param pointer start byte and bit offset of the postings in the inverted file,
	 * together with number of postings to expect
	 */

	public int[][] getDocuments(BitIndexPointer pointer) {
		
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
