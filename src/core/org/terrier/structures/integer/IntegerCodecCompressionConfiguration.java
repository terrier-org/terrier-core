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
 * The Original Code is IntegerCodecCompressionConfiguration.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk> (original contributor)
 */

package org.terrier.structures.integer;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.compression.integer.ByteIn;
import org.terrier.compression.integer.codec.GammaCodec;
import org.terrier.compression.integer.codec.IntegerCodec;
import org.terrier.structures.AbstractPostingOutputStream;
import org.terrier.structures.Index;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.integer.BasicIntegerCodingIterablePosting;
import org.terrier.structures.postings.integer.BlockFieldIntegerCodingIterablePosting;
import org.terrier.structures.postings.integer.BlockIntegerCodingIterablePosting;
import org.terrier.structures.postings.integer.FieldIntegerCodingIterablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;

/** 
 * <b>Properties:</b>
 * <ul>
 * <li><tt>compression.<STRUCTURENAME>.integer.<TYPE>.codec</tt> - the compression codec to use 
 * for the structure STRUCTURENAME for posting payload type TYPE - see {@link IntegerCodec}.</li>
 * </ul>
 * For instance, to full compress everything in the inverted index using Frame of Reference (FOR), 
 * as recommended by Catena et al, ECIR 2014, you would set the following properties:
 * <pre>
 * index.structureName.compression.integer.ids.codec=LemireFORVBCodec
 * index.structureName.compression.integer.tfs.codec=LemireFORVBCodec
 * index.structureName.compression.integer.fields.codec=LemireFORVBCodec
 * index.structureName.compression.integer.blocks.codec=LemireFORVBCodec
 * indexing.compression.configuration=IntegerCodecCompressionConfiguration
 * index.structureName.compression.integer.chunk.size=1024
 * </pre>
 * @author Craig Macdonald
 * @since 4.0
 */
public class IntegerCodecCompressionConfiguration extends
		CompressionConfiguration {

	Logger log = LoggerFactory.getLogger(IntegerCodecCompressionConfiguration.class);
	
	String compressionPrefix;
	String idsPrefix;
	String tfsPrefix;
	String fieldsPrefix;
	String blocksPrefix;
	
	int chunkSize;
	
	IntegerCodec idsCodec;
	IntegerCodec tfsCodec; 
	IntegerCodec fieldsCodec; 
	IntegerCodec blocksCodec;
	
	Properties props;
	
	public static IntegerCodec loadCodec(String className) throws Exception
	{
		String[] params = null;
		if (className.matches(".+\\(.+\\)$"))
		{
			String[] tmp = className.split("\\(");
			className = tmp[0];
			params = ArrayUtils.parseCommaDelimitedString(tmp[1].replaceAll("\\)$", ""));			
		}
		if (! className.contains("."))
		{
			className = GammaCodec.class.getPackage().toString().replaceFirst("package ", "") +'.' + className;
		}
		
		Class<? extends IntegerCodec> c = Class.forName(className).asSubclass(IntegerCodec.class);
		IntegerCodec factory = null;
		if (params != null)
		{
			factory = c.getConstructor(String[].class).newInstance((Object) params);
		}
		else
		{
			factory = c.newInstance();
		}
		return factory;		
	}
	
	public IntegerCodec loadCodecForType(String type) throws Exception
	{
		final String className = ApplicationSetup.getProperty("index."+super.structureName+".compression.integer."+type+".codec", GammaCodec.class.getName() );
		assert className != null;
		
		this.props.setProperty(compressionPrefix + "." + type + ".codec", className);
		final IntegerCodec rtr = loadCodec(className);
		log.info("Index structure " + super.structureName+ " payload type "+type + " will be compressed by " + rtr.getClass().getSimpleName());		
		return rtr;
	}
	
	
	public IntegerCodecCompressionConfiguration(String structureName, String[] fieldNames, int hasBlocks, int maxBlocks)
	{
		super(structureName, fieldNames, hasBlocks, maxBlocks);
		
		compressionPrefix = "index." + structureName + ".compression.integer";
		idsPrefix = compressionPrefix + ".ids";
		tfsPrefix = compressionPrefix + ".tfs";
		fieldsPrefix = compressionPrefix + ".fields";
		blocksPrefix = compressionPrefix + ".blocks";
		props = new Properties();
		
		try{
			idsCodec = loadCodecForType("ids");
			tfsCodec = loadCodecForType("tfs");
			//if (fieldCount > 0)
				fieldsCodec = loadCodecForType("fields");
			//if (blocks)
				blocksCodec = loadCodecForType("blocks");
			chunkSize = Integer.parseInt(ApplicationSetup.getProperty("index."+structureName+".compression.integer.chunk.size", "1024"));
						
			writeProperties(props);
			
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	@Override
	public AbstractPostingOutputStream getPostingOutputStream(String filename) {
		AbstractPostingOutputStream rtr;
		try{
			rtr = new IntegerCodingPostingOutputStream(
				filename,
				chunkSize, fieldCount, hasBlocks, maxBlocks, 
						idsCodec, 
						tfsCodec, 
						fieldsCodec, 
						blocksCodec
			);
		}catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		return rtr;
	}
	
	protected void writeProperties(Properties p)
	{
		String compressionPrefix = "index." + structureName + ".compression.integer";
		p.setProperty(compressionPrefix+".chunk-size", String.valueOf(chunkSize));
		p.setProperty("index."+structureName+".blocks", String.valueOf(hasBlocks));	
		p.setProperty("index."+structureName+".blocks.max", String.valueOf(maxBlocks));	
	}

	@Override
	public Class<? extends IterablePosting> getPostingIteratorClass() {
		return hasBlocks > 0 
				? fieldCount > 0 
						? BlockFieldIntegerCodingIterablePosting.class 
						: BlockIntegerCodingIterablePosting.class 
				: fieldCount > 0 
						? FieldIntegerCodingIterablePosting.class 
						: BasicIntegerCodingIterablePosting.class;
	}

	@Override
	public Class<? extends PostingIndex<?>> getStructureClass() {
		return IntegerCodingPostingIndex.class;
	}

	@Override
	public Class<? extends Iterator<IterablePosting>> getStructureInputStreamClass() {
		return IntegerCodingPostingIndexInputStream.class;
	}

	@Override
	public String getStructureFileExtension() {
		return ByteIn.USUAL_EXTENSION;
	}
	
	@Override
	public void writeIndexProperties(Index index, String pointerSourceStreamStructureName)
	{
		index.addIndexStructure(
				this.structureName, 
				this.getStructureClass().getName(), 
				"org.terrier.structures.IndexOnDisk,java.lang.String", 
				"index,structureName"// +
				//","+ this.getPostingIteratorClass().getName() 
				);
		index.addIndexStructureInputStream(
				this.structureName,
				this.getStructureInputStreamClass().getName(), 
				"org.terrier.structures.IndexOnDisk,java.lang.String,java.util.Iterator",
				"index,structureName,"+pointerSourceStreamStructureName);
		index.setIndexProperty("index."+this.structureName+".fields.count", String.valueOf(this.fieldCount));
		index.setIndexProperty("index."+this.structureName+".fields.names", ArrayUtils.join(this.fieldNames, ","));
		index.setIndexProperty("index."+this.structureName+".blocks", String.valueOf(this.hasBlocks));
		index.setIndexProperty("index."+this.structureName+".blocks.max", String.valueOf(this.maxBlocks));
		if (this.hasBlocks > 0)
		{
			log.warn("Compression of blocks - this format is subject to change, and may evolve in future Terrier versions");
		}
		
		for(Entry<Object,Object> e : props.entrySet())
		{
			String k = (String)e.getKey();
			String v = (String)e.getValue();
			index.setIndexProperty(k, v);			
		}
//		
//		for(Entry<Object,Object> e : index.getProperties().entrySet())
//		{
//			String k = (String)e.getKey();
//			String v = (String)e.getValue();
//			System.out.println(k+ "=" + v);
//		}
		
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" +idsCodec.toString() + "," + tfsCodec.toString() + ","  
				+ fieldsCodec.toString() + "," + blocksCodec.toString() + "]";
	}

}
