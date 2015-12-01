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
 * The Original Code is BlockInverted2DirectIndexBuilder.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald (craigm{at}dcs.gla.ac.uk)
 */
package org.terrier.structures.indexing.singlepass;

import java.io.IOException;
import java.util.Arrays;

import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.bit.BitPostingIndex;
import org.terrier.structures.bit.BitPostingIndexInputStream;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.bit.BlockFieldIterablePosting;
import org.terrier.structures.postings.bit.BlockIterablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.TerrierTimer;
import org.terrier.utility.UnitUtils;


/** Create a block direct index from a BlockInvertedIndex.
  * <p><b>Properties:</b> 
  * <ol>
  * <li><tt>inverted2direct.processtokens</tt> - total number of tokens to attempt each iteration. Defaults to 50000000. Memory usage would more likely
  * be linked to the number of pointers and the number of blocks, however as the document index does not contain these statistics on a document basis.
  * these are impossible to estimate. Note that the default is less than Inverted2DirectIndexBuilder.</li>
  * </ol>
  * @author Craig Macdonald
    * @since 2.0 */
public class BlockInverted2DirectIndexBuilder extends Inverted2DirectIndexBuilder {
	/**
	 * constructor
	 * @param i
	 */
	public BlockInverted2DirectIndexBuilder(IndexOnDisk i)
	{
		super(i);
		directIndexClass = BitPostingIndex.class.getName();
    	directIndexInputStreamClass = BitPostingIndexInputStream.class.getName();
    	basicDirectIndexPostingIteratorClass = BlockIterablePosting.class.getName();
    	fieldDirectIndexPostingIteratorClass = BlockFieldIterablePosting.class.getName();
		processTokens = UnitUtils.parseLong(ApplicationSetup.getProperty("inverted2direct.processtokens", "10000000"));
	}

    /** get an array of posting object of the specified size. These will be used to hold
      * the postings for a range of documents */
    protected Posting[] getPostings(final int count)
    {
        Posting[] rtr = new Posting[count];
        if (saveTagInformation)
        {
            for(int i=0;i<count;i++)
                rtr[i] = new BlockFieldPosting();
        }
        else
        {
            for(int i=0;i<count;i++)
                rtr[i] = new BlockPosting();
        }
        return rtr;
    }

    /** returns the SPIR implementation that should be used for reading the postings
      * written earlier */
    protected PostingInRun getPostingReader()
    {
        if (saveTagInformation)
        {
            return new BlockFieldPostingInRun(fieldCount);
        }
        return new BlockPostingInRun();
    }
	
	/** traverse the inverted file, looking for all occurrences of documents in the given range */
    @Override
    protected long traverseInvertedFile(final PostingIndexInputStream iiis, int firstDocid, int countDocuments, final Posting[] directPostings)
        throws IOException
    {
        //foreach posting list in the inverted index
            //for each (in range) posting in list
                //add termid->tf tuple to the Posting array
		long tokens = 0; 
        int termId = -1;
        //array recording which of the current set of documents has had any postings written thus far
	    boolean[] prevUse = new boolean[countDocuments];
	    int lastDocid = firstDocid + countDocuments -1;
        Arrays.fill(prevUse, false);
        int[] fieldFs = null;
        
        TerrierTimer tt = new TerrierTimer("Inverted index processing for this iteration", index.getCollectionStatistics().getNumberOfPointers());
		tt.start();
		try{
			while(iiis.hasNext())
			{
				IterablePosting ip = iiis.next();
				org.terrier.structures.postings.FieldPosting fip = null;
				org.terrier.structures.postings.BlockPosting bip = (org.terrier.structures.postings.BlockPosting) ip;
				if (saveTagInformation)
					fip = (org.terrier.structures.postings.FieldPosting) ip;
				//after TR-279, termids are not lexographically assigned in single-pass indexers
				termId = ((LexiconEntry) iiis.getCurrentPointer()).getTermId();
				final int numPostingsForTerm = iiis.getNumberOfCurrentPostings();
				int docid = ip.next(firstDocid);

				//TR-344: check first posting not too great for this pass (c.f. lastDocid)
				if (docid == IterablePosting.EOL || docid > lastDocid)
					continue;
				
				assert docid >= firstDocid;
				assert docid <= firstDocid + countDocuments;
				
				do {
					tokens += ip.getFrequency();
					final int writerOffset = docid - firstDocid;
					final int[] blocks = bip.getPositions();
					if (prevUse[writerOffset])
					{
						
						if (saveTagInformation)
						{
							fieldFs = fip.getFieldFrequencies();
							((BlockFieldPosting)directPostings[writerOffset]).insert(termId, ip.getFrequency(), fieldFs, blocks);
						}
						else
							((BlockPosting)directPostings[writerOffset]).insert(termId, ip.getFrequency(), blocks);
					}
					else
					{
						prevUse[writerOffset] = true;
						if (saveTagInformation)
						{	
							fieldFs = fip.getFieldFrequencies();
							((BlockFieldPosting)directPostings[writerOffset]).writeFirstDoc(termId, ip.getFrequency(), fieldFs, blocks);
						}
						else
							((BlockPosting)directPostings[writerOffset]).writeFirstDoc(termId, ip.getFrequency(), blocks);
					}
					docid = ip.next();
				} while(docid <= lastDocid && docid != IterablePosting.EOL);				
				tt.increment(numPostingsForTerm);
			}
		} finally {
			tt.finished();
		}
		return tokens;
    }

	/**
	 * main
	 * @param args
	 * @throws Exception
	 */
	public static void main (String[] args) throws Exception
	{
		Index.setIndexLoadingProfileAsRetrieval(false);
		IndexOnDisk i = Index.createIndex();
		if (i== null)
		{
			System.err.println("Sorry, no index could be found in default location");
			return;
		}
		new BlockInverted2DirectIndexBuilder(i).createDirectIndex();
		i.close();
	}

}
