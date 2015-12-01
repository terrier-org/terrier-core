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
 * The Original Code is BlockFieldPosting.java.
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
 * Class representing a posting list in memory containing fields and block iformation.
 * It keeps the information for <code>tf, df, field</code> and the sequence <code>[doc, idf, bockNo [blockId]]</code>
 * @author Roi Blanco
 *
 */
public class BlockFieldPosting extends BlockPosting{
	/** 
	 * Writes out the first document to the MemorySBOS.
	 */
	public int writeFirstDoc(final int doc, final int frequency, int[] fieldFreqs, int[] blockids) throws IOException{
		super.writeFirstDoc(doc, frequency);
		for(int tff : fieldFreqs)
			docIds.writeUnary(tff+1);
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
	 * Writes the document to the MemorySBOS
	 */
	public int insert(final int doc, final int freq, final int[] fieldFreqs, final int[] blockids) throws IOException{
		final int bytes = docIds.getSize();
		insert(doc, freq);
		for(int tff : fieldFreqs)
			docIds.writeUnary(tff+1);
		final int blockCount = blockids.length;
		
		docIds.writeUnary(blockCount+1);
		if (blockCount > 0)
		{
			docIds.writeGamma(blockids[0]+1);
			for (int i=1; i<blockCount; i++) {
				docIds.writeGamma(blockids[i] - blockids[i-1]);
			}
		}
		return docIds.getSize() - bytes;
	}
}
