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
 * The Original Code is CompressionFactory.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */


package org.terrier.structures.indexing;

import java.util.Iterator;

import org.terrier.compression.bit.BitIn;
import org.terrier.structures.AbstractPostingOutputStream;
import org.terrier.structures.Index;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.bit.BitPostingIndex;
import org.terrier.structures.bit.BitPostingIndexInputStream;
import org.terrier.structures.bit.BlockDirectInvertedOutputStream;
import org.terrier.structures.bit.BlockFieldDirectInvertedOutputStream;
import org.terrier.structures.bit.DirectInvertedDocidOnlyOuptutStream;
import org.terrier.structures.bit.DirectInvertedOutputStream;
import org.terrier.structures.bit.FieldDirectInvertedOutputStream;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.bit.BasicIterablePosting;
import org.terrier.structures.postings.bit.BasicIterablePostingDocidOnly;
import org.terrier.structures.postings.bit.BlockFieldIterablePosting;
import org.terrier.structures.postings.bit.BlockIterablePosting;
import org.terrier.structures.postings.bit.FieldIterablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
/** Configures the compression to be used when creating an IndexOnDisk.
 * In particular, the property <tt>indexing.compression.configuration</tt> defines
 * the name of a class of type CompressionConfiguration, which is used to configure
 * define the classes to use for writing and reading of the compressed index structures.
 * The default CompressionConfiguration is {@link BitCompressionConfiguration}.
 * You can change the CompressionConfiguration for either the direct or inverted index
 * by using the properties (other posting index structures are likewise supported):
 * <ul>
 * <li><tt>indexing.direct.compression.configuration</tt> - name of the {@link CompressionConfiguration} 
 * class to use for compressing the direct index</li>
 * <li><tt>indexing.inverted.compression.configuration</tt> - name of the {@link CompressionConfiguration} 
 * class to use for compressing the inverted index</li>
 * </ul>
 * @since 4.0
 */
public class CompressionFactory {

	/** A configuration object used by the disk indexers for configuring the compression for direct and inverted files.
	 * 
	 * @author Craig Macdonald
	 * @since 4.0
	 *
	 */
	static public abstract class CompressionConfiguration
	{	
		protected int fieldCount;
		protected String[] fieldNames;
		protected int hasBlocks;
		protected int maxBlocks;
		protected String structureName;
		
		public CompressionConfiguration(
				String structureName, String[] fieldNames, int hasBlocks, int maxBlocks)
		{
			this.structureName = structureName;
			this.fieldCount = fieldNames.length;
			this.fieldNames = fieldNames;
			this.hasBlocks = hasBlocks;
			this.maxBlocks = maxBlocks;
		}
		
		/** Write a file of postings to the given location */
		public abstract AbstractPostingOutputStream getPostingOutputStream(String filename);
		/** What is the posting iterator class for this structure */
		public abstract Class<? extends IterablePosting> getPostingIteratorClass();
		/** What is the structure class for this structure */
		public abstract Class<? extends PostingIndex<?>> getStructureClass();
		/** What is the input stream class for this structure */
		public abstract Class<? extends Iterator<IterablePosting>> getStructureInputStreamClass();
		/** What is the file extension for this structure. Usually ".bf" for BitFile and ".if" for files containing compressed integers */
		public abstract String getStructureFileExtension();
		
		/** Update the index's properties for this structure */
		public void writeIndexProperties(Index index, String pointerSourceStream)
		{
			index.addIndexStructure(
					this.structureName, 
					this.getStructureClass().getName(), 
					"org.terrier.structures.IndexOnDisk,java.lang.String,java.lang.Class", 
					"index,structureName,"+ 
						this.getPostingIteratorClass().getName() );
			index.addIndexStructureInputStream(
					this.structureName,
					this.getStructureInputStreamClass().getName(), 
					"org.terrier.structures.IndexOnDisk,java.lang.String,java.util.Iterator,java.lang.Class",
					"index,structureName,"+pointerSourceStream+","+ 
						this.getPostingIteratorClass().getName() );
			index.setIndexProperty("index."+this.structureName+".fields.count", String.valueOf(this.fieldCount));
			index.setIndexProperty("index."+this.structureName+".fields.names", ArrayUtils.join(this.fieldNames, ","));
		}
	}
	
	static class SpecificCompressionConfiguration extends CompressionConfiguration
	{
		final Class<? extends AbstractPostingOutputStream> outputStream;
		final Class<? extends IterablePosting> postingIterator;
		final Class<? extends PostingIndex<?>> structureClass;
		final Class<? extends Iterator<IterablePosting>> inputStream;
		final String fileExtension;
		
		public SpecificCompressionConfiguration(
				String structureName, String[] fieldNames, int hasBlocks, int maxBlocks,
				Class<? extends AbstractPostingOutputStream> outputStream,
				Class<? extends IterablePosting> postingIterator,
				Class<? extends PostingIndex<?>> structureClass,
				Class<? extends Iterator<IterablePosting>> inputStream,
				String fileExtension) {
			super(structureName, fieldNames, hasBlocks, maxBlocks);
			this.outputStream = outputStream;
			this.postingIterator = postingIterator;
			this.structureClass = structureClass;
			this.inputStream = inputStream;
			this.fileExtension = fileExtension;
		}
		@Override
		public AbstractPostingOutputStream getPostingOutputStream(String filename) {
			AbstractPostingOutputStream rtr;
			try{
				rtr = outputStream.getConstructor(String.class).newInstance(filename);
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
			return rtr;
		}
		@Override
		public Class<? extends IterablePosting> getPostingIteratorClass() {
			return postingIterator;
		}
		@Override
		public Class<? extends PostingIndex<?>> getStructureClass() {
			return structureClass;
		}
		@Override
		public Class<? extends Iterator<IterablePosting>> getStructureInputStreamClass() {
			return inputStream;
		}
		@Override
		public String getStructureFileExtension() {
			return fileExtension;
		}
	}
	
	public static class BitCompressionConfiguration extends SpecificCompressionConfiguration
	{
		public BitCompressionConfiguration(String structureName, String[] fieldNames, int hasBlocks, int maxBlocks)
		{
			super(
				structureName, fieldNames, hasBlocks, maxBlocks,
				fieldNames.length > 0 ? hasBlocks > 0 ? BlockFieldDirectInvertedOutputStream.class : FieldDirectInvertedOutputStream.class : hasBlocks > 0 ? BlockDirectInvertedOutputStream.class : DirectInvertedOutputStream.class,
				fieldNames.length > 0 ? hasBlocks > 0 ? BlockFieldIterablePosting.class : FieldIterablePosting.class : hasBlocks > 0 ? BlockIterablePosting.class : BasicIterablePosting.class,
				BitPostingIndex.class, 
				BitPostingIndexInputStream.class,
				BitIn.USUAL_EXTENSION
			);
		}
	}
	
	public static class BitIdOnlyCompressionConfiguration extends SpecificCompressionConfiguration
	{
		public BitIdOnlyCompressionConfiguration(String structureName, String[] fieldNames, int hasBlocks, int maxBlocks)
		{
			super(
				structureName, fieldNames, 0, 0,
				DirectInvertedDocidOnlyOuptutStream.class,
				BasicIterablePostingDocidOnly.class,
				BitPostingIndex.class, 
				BitPostingIndexInputStream.class,
				BitIn.USUAL_EXTENSION
			);
		}
	}
	
	@Deprecated
	public static CompressionConfiguration getCompressionConfiguration(String structureName, String[] fieldNames, boolean blocks)
	{
		String compressionConfiguration = ApplicationSetup.getProperty("indexing."+structureName+".compression.configuration", BitCompressionConfiguration.class.getName());
		CompressionConfiguration rtr = null;
		
		try{
			rtr = Class.forName(compressionConfiguration)
					.asSubclass(CompressionConfiguration.class)
					.getConstructor(String.class, String[].class,  Integer.TYPE, Integer.TYPE).newInstance(structureName, fieldNames, blocks ? 1 : 0, ApplicationSetup.MAX_BLOCKS);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		return rtr;
	}
	
	/** 
	@since 4.0
	*/
	public static CompressionConfiguration getCompressionConfiguration(String structureName, String[] fieldNames, int hasBlocks, int maxBlocks)
	{
		String compressionConfiguration = ApplicationSetup.getProperty("indexing."+structureName+".compression.configuration", BitCompressionConfiguration.class.getName());
		CompressionConfiguration rtr = null;
		
		try{
			rtr = Class.forName(compressionConfiguration)
					.asSubclass(CompressionConfiguration.class)
					.getConstructor(String.class, String[].class, Integer.TYPE, Integer.TYPE).newInstance(structureName, fieldNames, hasBlocks, maxBlocks);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		return rtr;
	}
	
}
