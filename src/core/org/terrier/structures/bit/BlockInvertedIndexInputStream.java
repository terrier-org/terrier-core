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
 * The Original Code is BlockInvertedIndexInputStream.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.bit;

import gnu.trove.TIntArrayList;

import java.io.IOException;
import java.util.Iterator;

import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.bit.BlockIterablePosting;

/** Reads a BlockInvertedIndex as a stream
  * @author Craig Macdonald
  * @since 2.0
  * @deprecated
  */
public class BlockInvertedIndexInputStream extends InvertedIndexInputStream 
{
    protected int DocumentBlockCountDelta = 1;

    /**
     * Construct an instance of the class with
     * @param _index
     * @param _structureName
     * @param _postingImplementation
     * @throws IOException
     */
    public BlockInvertedIndexInputStream(
    		IndexOnDisk _index, 
			String _structureName, 
			Class<? extends IterablePosting> _postingImplementation)
		throws IOException
	{
    	super(_index, _structureName, _postingImplementation);
	}
    /**
     * Construct an instance of the class with
     * @param _index
     * @param structureName
     * @param lexInputStream
     * @param _postingIteratorClass
     * @throws IOException
     */
    public BlockInvertedIndexInputStream(IndexOnDisk _index, String structureName, Iterator<? extends LexiconEntry> lexInputStream, Class<? extends IterablePosting> _postingIteratorClass) throws IOException
	{
		super(_index, structureName, lexInputStream, _postingIteratorClass);
	}
    /**
     * Construct an instance of the class with
     * @param _index
     * @param structureName
     * @param lexInputStream
     * @throws IOException
     */
    public BlockInvertedIndexInputStream(IndexOnDisk _index, String structureName, Iterator<? extends LexiconEntry> lexInputStream) throws IOException
    {
    	super(_index, structureName, lexInputStream, BlockIterablePosting.class);
    }
    /**
     * Construct an instance of the class with
     * @param _index
     * @param _structureName
     * @throws IOException
     */
	public BlockInvertedIndexInputStream(IndexOnDisk _index, String _structureName) throws IOException
    {
    	super(_index, _structureName, BlockIterablePosting.class);
    }

    protected int[][] getNextDocuments(BitIndexPointer pointer) throws IOException {
    	//System.err.println("pointer="+pointer.toString() + " actual=@{"+file.getByteOffset() + ","+ file.getBitOffset()+ "}");
		if (file.getByteOffset() != pointer.getOffset())
		{
			//System.err.println("skipping " + (pointer.getOffset() - file.getByteOffset()) + " bytes");
			file.skipBytes(pointer.getOffset() - file.getByteOffset());
		}
		if (file.getBitOffset() != pointer.getOffsetBits())
		{
			//System.err.println("skipping "+ (pointer.getOffsetBits() - file.getBitOffset()) + "bits");
			file.skipBits(pointer.getOffsetBits() - file.getBitOffset());
		}
    	
    	final int df = pointer.getNumberOfEntries();
		final int fieldCount = super.fieldCount;
		final boolean loadTagInformation = fieldCount > 0;
		
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

}
