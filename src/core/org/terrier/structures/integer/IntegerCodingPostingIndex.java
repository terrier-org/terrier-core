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
 * The Original Code is IntegerCodingPostingIndex.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 *   Matteo Catena
 */

package org.terrier.structures.integer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.compression.integer.ByteFileBuffered;
import org.terrier.compression.integer.ByteFileInMemory;
import org.terrier.compression.integer.ByteIn;
import org.terrier.compression.integer.ByteInSeekable;
import org.terrier.compression.integer.codec.IntegerCodec;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.integer.BasicIntegerCodingIterablePosting;
import org.terrier.structures.postings.integer.BlockFieldIntegerCodingIterablePosting;
import org.terrier.structures.postings.integer.BlockIntegerCodingIterablePosting;
import org.terrier.structures.postings.integer.FieldIntegerCodingIterablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.io.WrappedIOException;

/**
 * This implementation of {@link PostingIndex} provides access to an index of
 * {@link BasicIntegerCodingIterablePosting} (or one of it subclasses).
 * 
 * This expects in the data.properties file some information, such as
 * 
 * index.structureName.fields.count=the number of fields in the postings, if any
 * index.structureName.blocks=0 (no blocks) or 1 (positions) or >1 (blocks of any size)
 * index.structureName.blocks.max=0 (no limit) or >1 (position information limited) -- default is ApplicationSetup.MAX_BLOCKS
 * index.structureName.compression.integer.chunk-size=the maximum number of posting in a chunk
 * index.structureName.compression.integer.ids.codec=the {@link IntegerCodec} implementation to use for docIds
 * index.structureName.compression.integer.tfs.codec=the {@link IntegerCodec} implementation to use for tfs
 * index.structureName.compression.integer.fields.codec=the {@link IntegerCodec} implementation to use for fields (optional)
 * index.structureName.compression.integer.blocks.codec=the {@link IntegerCodec} implementation to use for blocks (optional)
 * 
 * @author Matteo Catena, Craig Macdonald
 * @since 4.0
 */
public class IntegerCodingPostingIndex implements PostingIndex<BitIndexPointer> {
	
	Logger log = LoggerFactory.getLogger(IntegerCodingPostingIndex.class);

	protected IndexOnDisk index;
	protected ByteInSeekable[] file;
	protected DocumentIndex documentIndex;
	protected int fieldsCount;
	protected int hasBlocks;
	protected int maxBlocks;
	protected int chunkSize;
	protected IntegerCodec idsCodec;
	protected IntegerCodec tfsCodec;
	protected IntegerCodec fieldsCodec;
	protected IntegerCodec blocksCodec;

	public IntegerCodingPostingIndex(IndexOnDisk index, String structureName)
			throws IOException {
		
		this.index = index;
		openFile(structureName);

		this.fieldsCount = index.getIntIndexProperty("index." + structureName
				+ ".fields.count", 0);
		this.hasBlocks = index.getIntIndexProperty(
				"index." + structureName + ".blocks", 0);
		this.maxBlocks = index.getIntIndexProperty(
				"index." + structureName + ".blocks.max", ApplicationSetup.MAX_BLOCKS);
		
		if (this.hasBlocks > 0 && index.getIndexProperty("index.terrier.version", "").equals("4.0"))
		{
			log.warn("Integer compession of blocks is unstable and has changed since 4.0, re-indexing is advisable");
		}
		
		try {
			this.documentIndex = index.getDocumentIndex();
			
			String compressionPrefix = "index." + structureName
					+ ".compression.integer";
			this.chunkSize = index.getIntIndexProperty(compressionPrefix
					+ ".chunk-size", -1);

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

	protected void openFile(String structureName) throws IOException {
		
		IndexOnDisk _index = index;
		String filename = 
				_index.getPath() + "/" + _index.getPrefix() + "." + structureName + ByteIn.USUAL_EXTENSION;
		
		byte fileCount = Byte.parseByte(
				index.getIndexProperty("index." + structureName + ".data-files", "1"));
		file = new ByteInSeekable[fileCount];
		
		String dataSource = index.getIndexProperty(
				"index."+structureName+".data-source", "file");
		if ("file".equals(dataSource)) {
			
			for (int i = 0; i < fileCount; i++) {
				
				String dataFilename = fileCount == 1 ? filename : filename
						+ String.valueOf(i);
				this.file[i] = new ByteFileBuffered(dataFilename);
			}			
			
		} else if ("fileinmem".equals(dataSource)) {
			
			
			for (int i = 0; i < fileCount; i++) {
				
				String dataFilename = fileCount == 1 ? filename : filename
						+ String.valueOf(i);
				this.file[i] = new ByteFileInMemory(dataFilename);
			}			
		}
	}
	
	@Override
	public void close() throws IOException {

		try {

			for (java.io.Closeable c : file)
				c.close();

		} catch (IOException ioe) {

		}

	}
	
	@Override
	public IterablePosting getPostings(Pointer _pointer)
			throws IOException {
		BitIndexPointer pointer = (BitIndexPointer)_pointer;
		ByteIn in = this.file[pointer.getFileNumber()].readReset(pointer.getOffset());		
		DocumentIndex fixedDi = pointer instanceof DocumentIndexEntry
				? new PostingIndex.DocidSpecificDocumentIndex(documentIndex, (DocumentIndexEntry)pointer)
				: documentIndex;
			
		if (hasBlocks > 0)
			if (fieldsCount > 0)
				return new BlockFieldIntegerCodingIterablePosting(in, pointer.getNumberOfEntries(), fixedDi, chunkSize, fieldsCount, hasBlocks, maxBlocks, idsCodec, tfsCodec, fieldsCodec, blocksCodec);
			else
				return new BlockIntegerCodingIterablePosting(in, pointer.getNumberOfEntries(), fixedDi, chunkSize, hasBlocks, maxBlocks, idsCodec, tfsCodec, blocksCodec);
		else
			if (fieldsCount > 0)
				return new FieldIntegerCodingIterablePosting(in, pointer.getNumberOfEntries(), fixedDi, chunkSize, fieldsCount, idsCodec, tfsCodec, fieldsCodec);
			else
				return new BasicIntegerCodingIterablePosting(in, pointer.getNumberOfEntries(), fixedDi, chunkSize, idsCodec, tfsCodec);
//		return new IntegerCodingIterablePosting(in,
//				lEntry.getNumberOfEntries(), documentIndex, chunkSize,
//				fieldsCount, hasBlocks, idsCodec, tfsCodec, fieldsCodec,
//				blocksCodec);
	}

}
