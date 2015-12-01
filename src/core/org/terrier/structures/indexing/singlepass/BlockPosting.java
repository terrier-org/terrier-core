
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
 * The Original Code is BlockPosting.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Roi Blanco (rblanc{at}@udc.es)
 *   Craig Macdonald (craigm{at}dcs.gla.ac.uk)
 */
package org.terrier.structures.indexing.singlepass;

import java.io.IOException;

/**
 * Class representing a posting list in memory with block information
 * It keeps the information for <code>DF, TF</code>, and the sequence <code>[doc, tf, blockCount, [blockId]] </code>
 * @author Roi Blanco
 *
 */
public class BlockPosting extends Posting{
	
	/**
	 * Writes the first document in the posting list.
	 * @param doc the document identifier.
	 * @param frequency the frequency of the term in the document.
	 * @param blockids the blockids for all the term
	 * @throws IOException if an I/O error ocurrs.
	 */	
	public int writeFirstDoc(final int doc, final int frequency, final int[] blockids) throws IOException{
		super.writeFirstDoc(doc, frequency);
		final int blockCount = blockids.length;
		
		docIds.writeUnary(blockCount+1);
		if (blockCount > 0)
		{	
			docIds.writeGamma(blockids[0]+1);
			for (int i=1; i<blockCount; i++) {
				docIds.writeGamma(blockids[i] - blockids[i-1]);
			}
		}
		return docIds.getSize();
	}
	
	/**
	 * Inserts a new document in the posting list. Document insertions must be done
	 * in order.  
	 * @param doc the document identifier.
	 * @param freq the frequency of the term in the document.
	 * @param blockids the blockids for all the term
	 * @return the updated term frequency.
	 * @throws IOException if and I/O error occurs.
	 */
	public int insert(final int doc, final int freq, final int[] blockids) throws IOException{
		final int c = insert(doc, freq);
		final int blockCount = blockids.length;
		
		docIds.writeUnary(blockCount+1);
		if (blockCount > 0)
		{	
			docIds.writeGamma(blockids[0]+1);
			for (int i=1; i<blockCount; i++) {
				docIds.writeGamma(blockids[i] - blockids[i-1]);
			}
		}
		return c;
	}
	
}
