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
 * The Original Code is FSADocumentIndex.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.collections.FSArrayFile;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.TerrierTimer;
/** 
 * Document Index saved as a fixed size array
 */
public class FSADocumentIndex extends FSArrayFile<DocumentIndexEntry> implements DocumentIndex {
	protected static final Logger logger = LoggerFactory.getLogger(FSADocumentIndex.class);
	
	protected int lastDocid = -1;
	protected DocumentIndexEntry lastEntry = null;
	protected int[] docLengths;
		
	/**
	 * Construct an instance of the class with
	 * @param index
	 * @param structureName
	 * @throws IOException
	 */
	public FSADocumentIndex(IndexOnDisk index, String structureName) throws IOException
	{
		this(index, structureName, true);		
	}
	
	@SuppressWarnings("unchecked")
	protected FSADocumentIndex(IndexOnDisk index, String structureName, boolean initialise) throws IOException
	{
		super(
				index.getPath() + "/" + index.getPrefix() + "."+ structureName + FSArrayFile.USUAL_EXTENSION,
				false,
				(FixedSizeWriteableFactory<DocumentIndexEntry>) index.getIndexStructure(structureName+"-factory")
				);
		if (initialise)
			initialise(index, structureName);
	}
	
	protected void initialise(IndexOnDisk index, String structureName) throws IOException
	{
		logger.debug("Loading document lengths for " + structureName + " structure into memory. NB: The following stacktrace IS NOT AN Exception", new Exception());
		docLengths = new int[this.size()];
		int i=0;
		Iterator<DocumentIndexEntry> iter = new FSADocumentIndexIterator(index, structureName);
		TerrierTimer tt = new TerrierTimer("Loading "+structureName+ " document lengths", this.size());tt.start();
		while(iter.hasNext())
		{
			docLengths[i++] = iter.next().getDocumentLength();
			tt.increment();
		}
		tt.finished();
		IndexUtil.close(iter);
	}
	/** 
	 * {@inheritDoc} 
	 */
	public final int getDocumentLength(int docid) throws IOException
	{
		return docLengths[docid];
	}
	/** 
	 * {@inheritDoc} 
	 */
	public final DocumentIndexEntry getDocumentEntry(int docid) throws IOException 
	{
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
	/** 
	 * Gets an iterator over the documents in this index
	 */
	public static class FSADocumentIndexIterator extends FSArrayFile.ArrayFileIterator<DocumentIndexEntry> implements Iterator<DocumentIndexEntry>
	{
		/**
		 * Construct an instance of the class with
		 * @param index
		 * @param structureName
		 * @throws IOException
		 */
		@SuppressWarnings("unchecked")
		public FSADocumentIndexIterator(IndexOnDisk index, String structureName) throws IOException
		{
			super(
					index.getPath() + "/" + index.getPrefix() + "."+ structureName + FSArrayFile.USUAL_EXTENSION, 
					(FixedSizeWriteableFactory<DocumentIndexEntry>) index.getIndexStructure("document-factory")
					);
		}		
	}
	/** 
	 * {@inheritDoc} 
	 */
	public int getNumberOfDocuments() {
		return super.size();
	}

}
