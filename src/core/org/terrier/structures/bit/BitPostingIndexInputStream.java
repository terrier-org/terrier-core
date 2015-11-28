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
 * The Original Code is BitPostingIndexInputStream.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.bit;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.compression.bit.BitIn;
import org.terrier.compression.bit.BitInputStream;
import org.terrier.compression.bit.DebuggingBitIn;
import org.terrier.structures.BitFilePosition;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.FilePosition;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.Skipable;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.io.WrappedIOException;
/** 
 * Input stream for a bit posting index.
 */
public class BitPostingIndexInputStream implements PostingIndexInputStream, Skipable {

	/** causes DebuggingBitIn to be wrapped around the BitInputStream */
	private static final boolean DEBUG = false;
	
	protected static final Logger logger = LoggerFactory.getLogger(BitPostingIndexInputStream.class);
	
	/** the lexicon input stream providing the offsets */
	protected final Iterator<? extends BitIndexPointer> pointerList;
	/** The gamma compressed file containing the terms. */
	protected BitIn file; 

	protected Class<? extends IterablePosting> postingIteratorClass;
	protected Constructor<? extends IterablePosting> postingConstructor;
	protected int currentEntryCount;
	protected BitIndexPointer currentPointer;
	protected int fieldCount;
	protected int entriesSkipped = 0;
	protected byte fileCount;
	protected byte currentFile = 0;
	protected IndexOnDisk index;
	protected DocumentIndex doi;
	protected String structureName;
	/** 
	 * Return filename
	 * @param path
	 * @param prefix
	 * @param structureName
	 * @param fileCount
	 * @param fileId
	 * @return filename
	 */
	public static String getFilename(String path, String prefix, String structureName, byte fileCount, byte fileId)
	{
		return path + "/" + prefix +"."+ structureName + BitIn.USUAL_EXTENSION + 
			(fileCount > 1 ? String.valueOf(fileId) : "");
	}
	/**
	 * Returns filename
	 * @param _index
	 * @param structureName
	 * @param fileCount
	 * @param fileId
	 * @return filename
	 */
	public static String getFilename(IndexOnDisk _index, String structureName, byte fileCount, byte fileId)
	{
		return _index.getPath() + "/" + _index.getPrefix() +"."+ structureName + BitIn.USUAL_EXTENSION + 
			(fileCount > 1 ? String.valueOf(fileId) : "");
	}
	
	BitPostingIndexInputStream(String _filename, byte _fileCount, 
			Iterator<? extends BitIndexPointer> _pointerList, 
			Class<? extends IterablePosting> _postingIteratorClass, int _fieldCount) throws IOException
	{
		fileCount = 0;
		file = new BitInputStream(_filename);
		//file = new org.terrier.compression.BitFileBuffered(_filename).readReset(0l, (byte)0);
		if (DEBUG)
			file = new DebuggingBitIn(file);
		pointerList = _pointerList;
		postingIteratorClass = _postingIteratorClass;
		fieldCount = _fieldCount;
		try{ 
			postingConstructor = fieldCount > 0
				? postingIteratorClass.getConstructor(BitIn.class, Integer.TYPE, DocumentIndex.class, Integer.TYPE)
				: postingIteratorClass.getConstructor(BitIn.class, Integer.TYPE, DocumentIndex.class);
		}catch (Exception e) {
			throw new WrappedIOException(e);
		}
	}
	/**
	 * Constructs an instance of BitPostingIndexInputStream.
	 * @param _index
	 * @param _structureName
	 * @param _pointerList
	 * @param _postingIteratorClass
	 * @throws IOException
	 */
	public BitPostingIndexInputStream(
			IndexOnDisk _index, String _structureName, 
			Iterator<? extends BitIndexPointer> _pointerList,
			Class<? extends IterablePosting> _postingIteratorClass) throws IOException
	{
		this.index = _index;
		this.doi = _index.getDocumentIndex();
		this.structureName = _structureName;
		fileCount = Byte.parseByte(_index.getIndexProperty("index."+structureName+".data-files", "1"));
		file = new BitInputStream(getFilename(_index, structureName, fileCount, (byte)0));
		if (DEBUG)
			file = new DebuggingBitIn(file);
		pointerList = _pointerList;
		postingIteratorClass = _postingIteratorClass;
		fieldCount = _index.getIntIndexProperty("index."+structureName+".fields.count", currentFile = 0);
		try{ 
			postingConstructor = fieldCount > 0
				? postingIteratorClass.getConstructor(BitIn.class, Integer.TYPE, DocumentIndex.class, Integer.TYPE)
				: postingIteratorClass.getConstructor(BitIn.class, Integer.TYPE, DocumentIndex.class);
		}catch (Exception e) {
			throw new WrappedIOException(e);
		}
	}
	/** 
	 * Get the file position
	 */
	public BitFilePosition getPos()
	{
		return new FilePosition(file.getByteOffset(), file.getBitOffset());
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void skip(int numEntries) throws IOException
	{
		((Skipable)pointerList).skip(numEntries);
	}
	/** 
	 * {@inheritDoc} 
	 */
	public int getNumberOfCurrentPostings()
	{
		return currentEntryCount;
	}
	
	/** {@inheritDoc} */
	public IterablePosting getNextPostings() throws IOException {
		if (! this.hasNext())
			return null;
		BitIndexPointer p = _next();
		if (p == null)//trailing empty document
			return null;
		assert p != null;
		return loadPostingIterator(p);
	}
	
	/** {@inheritDoc} */
	public boolean hasNext() {
		return pointerList.hasNext();
	}

	protected BitIndexPointer _next()
	{
		if (! pointerList.hasNext())
			return null;
		entriesSkipped = 0;
		BitIndexPointer pointer = (BitIndexPointer)pointerList.next();
		while(pointer.getNumberOfEntries() == 0)
		{
			entriesSkipped++;
			if (pointerList.hasNext())
			{	
				pointer = (BitIndexPointer)pointerList.next();
			}
			else
			{
				return null;
			}
		}
		return pointer;
	}
	
	/** {@inheritDoc} */
	public IterablePosting next()
	{
		BitIndexPointer pointer = _next();
		if (pointer == null)//trailing empty document
			return null;
		//inline empty document
		if (pointer.getNumberOfEntries() == 0)
			return null;
		try{
			return loadPostingIterator(pointer);
		} catch (IOException ioe) {
			logger.info("Couldn't load posting iterator", ioe);
			return null;
		}
	}
	/** 
	 * {@inheritDoc} 
	 */
	public int getEntriesSkipped()
	{
		return entriesSkipped;
	}
	
	protected IterablePosting loadPostingIterator(BitIndexPointer pointer) throws IOException
	{
		assert pointer.getNumberOfEntries() > 0;
		if(DEBUG) System.err.println("pointer="+pointer.toString() + " file="+currentFile+" actual=@{"+file.getByteOffset() + ","+ file.getBitOffset()+ "}");
		
		//check to see if file id has changed
		if (pointer.getFileNumber() > currentFile)
		{
			//file id changed: close current file, open specified file
			file.close();
			file = new BitInputStream(getFilename(index, structureName, fileCount, currentFile = pointer.getFileNumber()));
			if (DEBUG)
				file = new DebuggingBitIn(file);
		}
		if (file.getByteOffset() != pointer.getOffset())
		{
			assert (pointer.getOffset() - file.getByteOffset()) > 0;
			if(DEBUG) System.err.println("skipping " + (pointer.getOffset() - file.getByteOffset()) + " bytes");
			file.skipBytes(pointer.getOffset() - file.getByteOffset());
		}
		if (file.getBitOffset() != pointer.getOffsetBits())
		{
			assert (pointer.getOffsetBits() - file.getBitOffset()) > 0;
			if(DEBUG) System.err.println("skipping "+ (pointer.getOffsetBits() - file.getBitOffset()) + "bits");
			file.skipBits(pointer.getOffsetBits() - file.getBitOffset());
		}
		currentPointer = pointer;
		currentEntryCount = pointer.getNumberOfEntries();
		IterablePosting rtr = null;
		try{
			rtr = (fieldCount > 0)
				? postingConstructor.newInstance(file, pointer.getNumberOfEntries(), getDocumentIndex(pointer), fieldCount)
				: postingConstructor.newInstance(file, pointer.getNumberOfEntries(), getDocumentIndex(pointer));
		} catch (Exception e) {
			throw new WrappedIOException("Problem creating IterablePosting", e);
		}
		return rtr;
	}
	
	protected DocumentIndex getDocumentIndex(BitIndexPointer pointer) {
		//this is the hack: only a direct index has a pointer type of DocumentIndexEntry
		if(pointer instanceof DocumentIndexEntry)
		{
			return new PostingIndex.DocidSpecificDocumentIndex(doi, (DocumentIndexEntry)pointer);
		}
		return doi;
	}
	/** 
	 * Print a list of the postings to standard out
	 */
	@Override
	public void print()
	{	
		try{
			int entryIndex = 0;
			while(this.hasNext())
			{
				IterablePosting ip = this.next();
				entryIndex += this.getEntriesSkipped();
				System.out.print(entryIndex + " ");
				while(ip.next() != IterablePosting.EOL)
				{
					System.out.print(ip.toString());
					System.out.print(" ");
				}
				System.out.println();
				entryIndex++;
			}
		} catch (Exception e) {
			logger.error("Error during print()", e);
		}
	}
	
	/** {@inheritDoc} */
	public void close() throws IOException
	{
		file.close();
		IndexUtil.close(pointerList);
	}

	/** Not supported */
	public void remove() {
		throw new UnsupportedOperationException();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public Pointer getCurrentPointer() {
		return currentPointer;
	}

}
