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
 * The Original Code is BlockDirectIndexInputStream.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Douglas Johnson <johnsoda{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> 
 */
package org.terrier.structures.bit;
import gnu.trove.TIntArrayList;

import java.io.IOException;
import java.util.Iterator;

import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.bit.BlockIterablePosting;
/**
 * This class reads the block field direct index structure
 * sequentially, as an input stream.
 * @author Douglas Johnson, Vassilis Plachouras
 * @see org.terrier.structures.bit.BlockDirectIndex
 * @deprecated
 */
public class BlockDirectIndexInputStream extends DirectIndexInputStream 
{
	protected int DocumentBlockCountDelta = 1;
	/**
	 * Constructs an index of the class with
	 * @param index
	 * @param structureName
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public BlockDirectIndexInputStream(IndexOnDisk index, String structureName) throws IOException
	{
		super(index, structureName, (Iterator<DocumentIndexEntry>)index.getIndexStructureInputStream("document"), BlockIterablePosting.class);
	}
	/**
	 * Constructs an index of the class with
	 * @param index
	 * @param structureName
	 * @param postingClass
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public BlockDirectIndexInputStream(IndexOnDisk index, String structureName, Class<? extends IterablePosting> postingClass) throws IOException
	{
		super(index, structureName, (Iterator<DocumentIndexEntry>)index.getIndexStructureInputStream("document"), postingClass);
	}
	
	/** 
	 * {@inheritDoc} 
	 */
	public int[][] getNextTerms(BitIndexPointer pointer) throws IOException {
    	final int df = pointer.getNumberOfEntries();
		final int fieldCount = super.fieldCount;
		final boolean loadTagInformation = fieldCount > 0;
		//System.err.println("Pointer: " + pointer);
		final int[][] documentTerms = new int[fieldCount+4][];
		for(int i=0;i<fieldCount+3;i++)
			documentTerms[i] = new int[df];
		final TIntArrayList blockids = new TIntArrayList(df); //ideally we'd have TF here
	
		if (loadTagInformation) { //if there are tag information to process
			documentTerms[0][0] = file.readGamma() - 1;
			documentTerms[1][0] = file.readUnary();
			for(int fi=0;fi < fieldCount;fi++)
				documentTerms[2+fi][0] = file.readUnary() -1;
			int blockfreq = documentTerms[fieldCount+2][0] = file.readUnary() - DocumentBlockCountDelta;
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
		documentTerms[documentTerms.length -1] = blockids.toNativeArray();
		return documentTerms;
	}
		
	/*public int[][] getNextTerms(BitIndexPointer pointer) throws IOException
	{
		final int FIELDS_COUNT = super.fieldCount;
		ArrayList<int[]> temporaryTerms = new ArrayList<int[]>();
		ArrayList<int[]> temporaryBlockids = new ArrayList<int[]>(pointer.getNumberOfEntries());
		//boolean hasMore = false;
		int blockCount=0;
		//while ((endByteOffset > gammaInputStream.getByteOffset())
		//	|| (endByteOffset == gammaInputStream.getByteOffset()
		//		&& endBitOffset > gammaInputStream.getBitOffset())) {
		for(int j=0;j<pointer.getNumberOfEntries();j++)
		{
			int[] tmp = new int[4+FIELDS_COUNT];
			tmp[0] = file.readGamma();
			tmp[1] = file.readUnary();
			for(int fi=0;fi < fieldCount;fi++)
				tmp[2+fi] = file.readUnary() -1;
			int blockfreq = file.readUnary() - DocumentBlockCountDelta;
			tmp[2+fieldCount] = blockfreq;
			int[] tmp2 = new int[blockfreq];
			if (blockfreq > 0)
			{
				for (int i = 0; i < blockfreq; i++) {
					tmp2[i] = file.readGamma();
					blockCount++;
				}
			}
			temporaryTerms.add(tmp);
			temporaryBlockids.add(tmp2);
		}
		int[][] documentTerms = new int[4+fieldCount][];
		for(int i=0;i<fieldCount+1;i++)
		{
			documentTerms[i] = new int[temporaryTerms.size()];
		}
		documentTerms[0][0] = ((int[]) temporaryTerms.get(0))[0] - 1;
		documentTerms[1][0] = ((int[]) temporaryTerms.get(0))[1];
		for(int fi=0;fi< fieldCount;fi++)
		{
			documentTerms[fi+2][0] = ((int[]) temporaryTerms.get(0))[2];
		}
		documentTerms[fieldCount+2][0] = ((int[]) temporaryTerms.get(0))[3];
		int[] blockids = ((int[])temporaryBlockids.get(0));
		
		documentTerms[fieldCount+3][0] = blockids[0] - 1;
		final int blockids_length = blockids.length;
		for(int i=1; i<blockids_length; i++){
			documentTerms[4][i]= blockids[i] + documentTerms[4][i-1];
		}
		
		int blockindex = blockids.length;
		if (documentTerms[0].length > 1) {
			final int documentTerms0_length = documentTerms[0].length;
			for (int i = 1; i < documentTerms0_length; i++) {
				int[] tmpMatrix = (int[]) temporaryTerms.get(i);
				documentTerms[0][i] = tmpMatrix[0] + documentTerms[0][i - 1];
				documentTerms[1][i] = tmpMatrix[1];
				for(int fi=0;fi< fieldCount;fi++)
				{
					documentTerms[fi+2][i] = tmpMatrix[fi+2];
				}
				documentTerms[fieldCount+3][i] = tmpMatrix[fieldCount+3];
				blockids = ((int[])temporaryBlockids.get(i));
				if (blockids.length > 0)
				{
					documentTerms[fieldCount+3][blockindex] = blockids[0] - 1;
					blockindex++;
					for(int j=1; j<blockids.length; j++){
						documentTerms[fieldCount+3][blockindex] = blockids[j] + documentTerms[fieldCount+3][blockindex-1];
						blockindex++;
					}
				}
			}
		}
		return documentTerms;
	}*/
}
