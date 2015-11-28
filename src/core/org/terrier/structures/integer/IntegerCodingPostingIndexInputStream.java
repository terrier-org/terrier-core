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
 * The Original Code is IntegerCodingPostingIndexInputStream.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 *   Matteo Catena
 */

package org.terrier.structures.integer;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.compression.integer.ByteIn;
import org.terrier.compression.integer.ByteInputStream;
import org.terrier.compression.integer.codec.IntegerCodec;
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
import org.terrier.structures.postings.integer.BasicIntegerCodingIterablePosting;
import org.terrier.structures.postings.integer.BlockFieldIntegerCodingIterablePosting;
import org.terrier.structures.postings.integer.BlockIntegerCodingIterablePosting;
import org.terrier.structures.postings.integer.FieldIntegerCodingIterablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.terrier.utility.io.WrappedIOException;

/**
 * Input stream for an integer coding posting index.
 * 
 * @author Craig Macdonald,  Matteo Catena
 * @since 4.0
 */
public class IntegerCodingPostingIndexInputStream implements
		PostingIndexInputStream, Skipable {

	protected static final Logger logger = LoggerFactory
			.getLogger(IntegerCodingPostingIndexInputStream.class);

	/** the lexicon input stream providing the offsets */
	protected final Iterator<? extends BitIndexPointer> pointerList;
	/** The compressed file containing the terms. */
	protected ByteIn file;

	protected int currentEntryCount;
	protected BitIndexPointer currentPointer;
	
	protected int entriesSkipped = 0;
	protected byte fileCount;
	protected byte currentFile = 0;
	
	protected IndexOnDisk index;
	protected DocumentIndex documentIndex;
	protected String structureName;

	protected final int fieldsCount;
	protected final int hasBlocks;
	protected final int maxBlocks;
	
	protected int chunkSize;
	protected IntegerCodec idsCodec;
	protected IntegerCodec tfsCodec;
	protected IntegerCodec fieldsCodec;
	protected IntegerCodec blocksCodec;

	/**
	 * Return filename
	 * 
	 * @param path
	 * @param prefix
	 * @param structureName
	 * @param fileCount
	 * @param fileId
	 * @return filename
	 */
	public static String getFilename(String path, String prefix,
			String structureName, byte fileCount, byte fileId) {
		return path + "/" + prefix + "." + structureName
				+ ByteIn.USUAL_EXTENSION
				+ (fileCount > 1 ? String.valueOf(fileId) : "");
	}

	/**
	 * Returns filename
	 * 
	 * @param _index
	 * @param structureName
	 * @param fileCount
	 * @param fileId
	 * @return filename
	 */
	public static String getFilename(IndexOnDisk _index, String structureName,
			byte fileCount, byte fileId) {
		return _index.getPath() + "/" + _index.getPrefix() + "."
				+ structureName + ByteIn.USUAL_EXTENSION
				+ (fileCount > 1 ? String.valueOf(fileId) : "");
	}

	/**
	 * Constructs an instance
	 * 
	 * @param _index
	 * @param _structureName
	 * @param _pointerList
	 * @throws IOException
	 */
	public IntegerCodingPostingIndexInputStream(IndexOnDisk _index,
			String _structureName,
			Iterator<? extends BitIndexPointer> _pointerList)
			throws IOException {
		
		this.index = _index;
		this.documentIndex = _index.getDocumentIndex();
		this.structureName = _structureName;
		fileCount = Byte.parseByte(_index.getIndexProperty("index."
				+ structureName + ".data-files", "1"));
		DataInputStream dis = 
				new DataInputStream(
						Files.openFileStream(
								getFilename((IndexOnDisk) _index, structureName, fileCount, (byte) 0)));
		file = new ByteInputStream(dis);

		pointerList = _pointerList;
		fieldsCount = _index.getIntIndexProperty("index." + structureName
				+ ".fields.count", 0);
		hasBlocks = index.getIntIndexProperty("index."
				+ structureName + ".blocks", 0);
		maxBlocks = index.getIntIndexProperty("index."
				+ structureName + ".blocks.max", ApplicationSetup.MAX_BLOCKS);
		
		if (this.hasBlocks > 0 && index.getIndexProperty("index.terrier.version", "").equals("4.0"))
		{
			logger.warn("Integer compession of blocks is unstable and has changed since 4.0, re-indexing is advisable");
		}
		
		String compressionPrefix = "index." + structureName + ".compression.integer";
		chunkSize = index.getIntIndexProperty(
				compressionPrefix + ".chunk-size", -1);
		assert chunkSize != -1;
		
		try {

			String idsPrefix = compressionPrefix + ".ids";
			String idsCodecName = index.getIndexProperty(idsPrefix + ".codec", "");
			this.idsCodec = IntegerCodecCompressionConfiguration.loadCodec(idsCodecName);

			String tfsPrefix = compressionPrefix + ".tfs";
			String tfsCodecName = index.getIndexProperty(tfsPrefix + ".codec", "");
			this.tfsCodec = IntegerCodecCompressionConfiguration.loadCodec(tfsCodecName);
			
			if (fieldsCount > 0) {
				String fieldsPrefix = compressionPrefix + ".fields";
				String fieldsCodecName = index.getIndexProperty(
						fieldsPrefix + ".codec", "");
				this.fieldsCodec =  IntegerCodecCompressionConfiguration.loadCodec(fieldsCodecName);
			}

			if (hasBlocks > 0) {
				String blocksPrefix = compressionPrefix + ".blocks";
				String blocksCodecName = index.getIndexProperty(blocksPrefix + ".codec", "");
				this.blocksCodec = IntegerCodecCompressionConfiguration.loadCodec(blocksCodecName);
			}

		} catch (Exception e) {
			throw new WrappedIOException(e);
		}
	}

	/**
	 * Get the file position
	 */
	public BitFilePosition getPos() {
		return new FilePosition(file.getByteOffset(), (byte) 0);
	}

	/**
	 * {@inheritDoc}
	 */
	public void skip(int numEntries) throws IOException {
		((Skipable) pointerList).skip(numEntries);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getNumberOfCurrentPostings() {
		return currentEntryCount;
	}

	/** {@inheritDoc} */
	public IterablePosting getNextPostings() throws IOException {
		if (!this.hasNext())
			return null;
		BitIndexPointer p = _next();
		if (p == null)
			return null;
		assert p != null;
		return loadPostingIterator(p);
	}

	/** {@inheritDoc} */
	public boolean hasNext() {
		return pointerList.hasNext();
	}

	protected BitIndexPointer _next() {
		if (!pointerList.hasNext())
			return null;
		entriesSkipped = 0;
		BitIndexPointer pointer = (BitIndexPointer) pointerList.next();
		while (pointer.getNumberOfEntries() == 0) {
			entriesSkipped++;
			if (pointerList.hasNext()) {
				pointer = (BitIndexPointer) pointerList.next();
			} else {
				return null;
			}
		}
		return pointer;
	}

	/** {@inheritDoc} */
	public IterablePosting next() {
		BitIndexPointer pointer = _next();
		if (pointer == null)// trailing empty document
			return null;
		try {
			return loadPostingIterator(pointer);
		} catch (IOException ioe) {
			logger.info("Couldn't load posting iterator", ioe);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getEntriesSkipped() {
		return entriesSkipped;
	}

	protected IterablePosting loadPostingIterator(BitIndexPointer pointer)
			throws IOException {
		
		if (pointer==null) return null;
		
		// check to see if file id has changed
		if (pointer.getFileNumber() > currentFile) {
			// file id changed: close current file, open specified file
			file.close();
			DataInputStream dis = 
					new DataInputStream(
							Files.openFileStream(
									getFilename((IndexOnDisk)index, structureName, fileCount, currentFile = pointer.getFileNumber())));
			file = new ByteInputStream(dis);
		}
		//negative skips aren't an option
		assert file.getByteOffset() >= pointer.getOffset();
		if (file.getByteOffset() != pointer.getOffset()) {
			file.skipBytes(pointer.getOffset() - file.getByteOffset());
		}

		currentPointer = pointer;
		currentEntryCount = pointer.getNumberOfEntries();
		//System.err.println(currentPointer);
		IterablePosting rtr = null;
		try {
			DocumentIndex fixedDi = pointer instanceof DocumentIndexEntry
					? new PostingIndex.DocidSpecificDocumentIndex(documentIndex, (DocumentIndexEntry)pointer)
					: documentIndex;
			
			
			if (hasBlocks > 0)
				if (fieldsCount > 0)
					rtr = new BlockFieldIntegerCodingIterablePosting(file, pointer.getNumberOfEntries(), fixedDi, chunkSize, fieldsCount, hasBlocks, maxBlocks, idsCodec, tfsCodec, fieldsCodec, blocksCodec);
				else
					rtr = new BlockIntegerCodingIterablePosting(file, pointer.getNumberOfEntries(), fixedDi, chunkSize, hasBlocks, maxBlocks, idsCodec, tfsCodec, blocksCodec);
			else
				if (fieldsCount > 0)
					rtr = new FieldIntegerCodingIterablePosting(file, pointer.getNumberOfEntries(), fixedDi, chunkSize, fieldsCount, idsCodec, tfsCodec, fieldsCodec);
				else
					rtr = new BasicIntegerCodingIterablePosting(file, pointer.getNumberOfEntries(), fixedDi, chunkSize, idsCodec, tfsCodec);

//			rtr = new IntegerCodingIterablePosting(
//					file, 
//					pointer.getNumberOfEntries(), 
//					documentIndex, 
//					chunkSize, 
//					fieldsCount, hasBlocks, 
//					idsCodec, tfsCodec, fieldsCodec, blocksCodec);

		} catch (Exception e) {
			
			e.printStackTrace();
			throw new WrappedIOException("Problem creating IterablePosting", e);
		}
		return rtr;
	}

	protected DocumentIndex getDocumentIndex(BitIndexPointer pointer) {
		return documentIndex;
	}

	/**
	 * Print a list of the postings to standard out
	 */
	public void print() {
		try {
			int entryIndex = 0;
			while (this.hasNext()) {
				IterablePosting ip = this.next();
				entryIndex += this.getEntriesSkipped();
				System.out.print(entryIndex + " ");
				while (ip.next() != IterablePosting.EOL) {
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
	public void close() throws IOException {
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