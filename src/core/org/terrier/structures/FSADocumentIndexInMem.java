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
 * The Original Code is FSADocumentIndexInMem.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.terrier.structures.collections.FSArrayFile;
import org.terrier.structures.collections.FSArrayFileInMem;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;

class FSADocumentIndexInMem extends FSArrayFileInMem<DocumentIndexEntry> implements DocumentIndex 
{
	protected int lastDocid = -1;
	protected DocumentIndexEntry lastEntry = null;
	protected int[] docLengths;
	@SuppressWarnings("unchecked")
	public FSADocumentIndexInMem(IndexOnDisk index, String structureName) throws IOException
	{
		super(	index.getPath() + "/" + index.getPrefix() + "."+ structureName + FSArrayFile.USUAL_EXTENSION,
				false,
				(FixedSizeWriteableFactory<DocumentIndexEntry>) index.getIndexStructure(structureName+"-factory")
				);
		docLengths = new int[this.size()];
		for(int i=0;i<this.size();i++)
		{
			docLengths[i] = this.get(i).getDocumentLength();
		}
	}

	public final int getDocumentLength(int docid) throws IOException
	{
		return docLengths[docid];
	}

	public DocumentIndexEntry getDocumentEntry(int docid) throws IOException {
		if (docid == lastDocid)
		{
			return lastEntry;
		}
		try{
			lastEntry = null;
			return lastEntry = get(lastDocid = docid);
		} catch (NoSuchElementException nsee) {
			return null;
		}
	}
	
	public int getNumberOfDocuments() {
		return super.size();
	}

}
