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
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
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
import com.jakewharton.byteunits.BinaryByteUnit;

/** 
 * Document Index saved as a fixed size array
 */
public class FSADocumentIndex extends FSArrayFile<DocumentIndexEntry> implements DocumentIndex {
	
	protected static final Logger logger = LoggerFactory.getLogger(FSADocumentIndex.class);
	
	protected int lastDocid = -1;
	protected DocumentIndexEntry lastEntry = null;
	protected int[] docLengths;

	static long freeMem()
	{
		Runtime runtime = Runtime.getRuntime();
		long free;
		if (runtime.maxMemory() == Long.MAX_VALUE)
		{
			free = runtime.freeMemory();
		}
		else
		{
			long localAllocated =  runtime.totalMemory()-runtime.freeMemory();
			//logger.debug("Memory: already allocated in use is " + BinaryByteUnit.format(localAllocated));
			free = runtime.maxMemory() - localAllocated;
		}
		return free;
	}
		
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
		logger.debug("Loading document lengths for " + structureName + " structure into memory. NB: The following stacktrace IS NOT AN Exception", new Exception("THIS IS **NOT** AN EXCEPTION"));
		int numEntries = this.size();

		// warn if this index has >1 fields (BM25F is pointless for 1 field)
		if (index.getCollectionStatistics().getNumberOfFields() > 1) {
			logger.warn("This index has fields, but FSADocumentIndex is used (which stores fields lengths on disk); "
				+"If using field-based models such as BM25F, change to index."+structureName+".class in the index "
				+" properties file to FSAFieldDocumentIndex or FSADocumentIndexInMemFields to support efficient retrieval." 
				+" If you don't use (e.g.) BM25F, this warning can be ignored");
		}
		
		final long size = (long) numEntries * (long) Integer.BYTES;
		final long free = freeMem();
		logger.info("Document index requires "+  BinaryByteUnit.format(size) +" remaining heap is " +  BinaryByteUnit.format(free));
		if (free < size)
		{
			logger.warn("Insufficient memory to load document index - use TERRIER_HEAP_MEM env var to increase available heap space");
		}
		docLengths = new int[numEntries];
		int i=0;
		Iterator<DocumentIndexEntry> iter = new FSADocumentIndexIterator(index, structureName);
		TerrierTimer tt = new TerrierTimer("Loading "+structureName+ " document lengths", numEntries);tt.start();
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
