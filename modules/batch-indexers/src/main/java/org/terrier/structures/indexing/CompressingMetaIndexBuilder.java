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
 * The Original Code is CompressingMetaIndexBuilder.java
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.indexing;

import gnu.trove.TObjectIntHashMap;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.Deflater;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.collections.FSOrderedMapFile;
import org.terrier.structures.collections.FSOrderedMapFile.MapFileWriter;
import org.terrier.structures.collections.FSOrderedMapFile.MultiFSOMapWriter;
import org.terrier.structures.seralization.FixedSizeIntWritableFactory;
import org.terrier.structures.seralization.FixedSizeTextFactory;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.Files;
import org.terrier.utility.MemoryChecker;
import org.terrier.utility.RuntimeMemoryChecker;
/** Creates a metaindex structure that compresses all values using Deflator. 
 * <b>Properties:</b>
 * <ul>
 * <li><tt>metaindex.compressed.max.data.in-mem.mb</tt> - maximum size that a meta index .zdata file will be kept in memory. Defaults to 400(mb). </li>
 * <li><tt>metaindex.compressed.max.index.in-mem.mb</tt> - maximum size that a meta index .zdata file will be kept in memory. Defaults to 100(mb).</li>
 * <li><tt>metaindex.compressed.reverse.allow.duplicates</tt> - set this property to true to suppress errors when a reverse meta value is not unique. Default false.</li>
 * <li><tt>metaindex.compressed.crop.long</tt> - set this property to suppress errors with overlong Document metadata, while will instead be cropped.</li>
 * </ul>
 * @since 3.0
 * @author Craig Macdonald &amp; Vassilis Plachouras 
 */
public class CompressingMetaIndexBuilder extends MetaIndexBuilder implements Flushable {
	protected final Logger logger = LoggerFactory.getLogger(CompressingMetaIndexBuilder.class);
	protected final int MAX_MB_IN_MEM_RETRIEVAL = 
			Integer.parseInt(ApplicationSetup.getProperty("metaindex.compressed.max.data.in-mem.mb", "400"));
	protected final int MAX_INDEX_MB_IN_MEM_RETRIEVAL = 
			Integer.parseInt(ApplicationSetup.getProperty("metaindex.compressed.max.index.in-mem.mb", "100"));
	protected final boolean REVERSE_ALLOW_DUPS = 
			Boolean.parseBoolean(ApplicationSetup.getProperty("metaindex.compressed.reverse.allow.duplicates", "false"));
	protected final boolean CROP_LONG = 
			Boolean.parseBoolean(ApplicationSetup.getProperty("metaindex.compressed.crop.long", "false"));
	
	protected final int REVERSE_KEY_LOOKUP_WRITING_BUFFER_SIZE = 20000;
	protected final int DOCS_PER_CHECK = ApplicationSetup.DOCS_CHECK_SINGLEPASS;
	protected final int ZIP_COMPRESSION_LEVEL = 5;//TODO (auto)configure? 
		
	protected final TObjectIntHashMap<String> key2Index;
	protected DataOutputStream dataOutput = null;
	protected final String[] keyNames;
	protected final int keyCount;
	protected Deflater zip = new Deflater();
	protected ByteArrayOutputStream baos = new ByteArrayOutputStream();
	protected DataOutputStream indexOutput = null;
	protected byte[] compressedBuffer = new byte[1024];
	protected IndexOnDisk index;
	protected int[] valueLensChars;
	protected int[] valueLensBytes;
	
	protected byte[] spaces;
	protected int entryLengthBytes = 0;
	protected long currentOffset = 0;
	protected long currentIndexOffset = 0;
	protected int entryCount = 0;

	protected int[] forwardKeys;
	protected String[] forwardKeyNames;
	
	protected MapFileWriter[] forwardWriters;
	protected boolean[] forwardKeyValuesSorted;
	protected String[] lastValues;
	protected MemoryChecker memCheck = new RuntimeMemoryChecker();
	protected FixedSizeWriteableFactory<Text>[] keyFactories;
	protected String structureName;
	
	/**
	 * constructor
	 * @param _index
	 * @param _keyNames
	 * @param _valueLens
	 * @param _forwardKeys
	 */
	public CompressingMetaIndexBuilder(IndexOnDisk _index, String[] _keyNames, int[] _valueLens, String[] _forwardKeys)
	{
		this(_index, "meta", _keyNames, _valueLens, _forwardKeys);
	}
	/**
	 * constructor
	 * @param _index
	 * @param _structureName
	 * @param _keyNames
	 * @param _valueLens
	 * @param _forwardKeys
	 */
	@SuppressWarnings("unchecked")
	public CompressingMetaIndexBuilder(IndexOnDisk _index, String _structureName, String[] _keyNames, int[] _valueLens, String[] _forwardKeys)
	{
		this.index = _index;
		this.structureName = _structureName;
		this.keyNames = _keyNames;
		this.valueLensChars = _valueLens;
		if (this.keyNames.length != this.valueLensChars.length)
			throw new IllegalArgumentException("CompressingMetaIndexBuilder configuration incorrect: number of keys and number of value lengths are unequal: "+ Arrays.toString(keyNames) + " vs " + Arrays.toString(_valueLens));
		this.key2Index = new TObjectIntHashMap<String>(keyNames.length);
		this.keyCount = keyNames.length;
		for(int i=0;i<keyCount;i++)
			this.key2Index.put(keyNames[i], i);
		logger.debug("Initialising CompressingMetaIndexBuilder");
		try{
			this.dataOutput = new DataOutputStream(Files.writeFileStream(_index.getPath() + "/" + _index.getPrefix() + "."+structureName+".zdata"));
			this.indexOutput = new DataOutputStream(Files.writeFileStream(_index.getPath() + "/" + _index.getPrefix() + "."+structureName+".idx"));
		} catch (IOException ioe) {
			throw new IllegalArgumentException(ioe);
		}
		
		this.zip.setLevel(ZIP_COMPRESSION_LEVEL);
		
		if (_forwardKeys.length == 1 && _forwardKeys[0].length() == 0)
			_forwardKeys = new String[0];
		
		this.forwardKeyNames = _forwardKeys;
		this.forwardKeys = new int[_forwardKeys.length];int i=0;
		for(String fwdKey : _forwardKeys)
		{
			if (! key2Index.contains(fwdKey))
				throw new IllegalArgumentException("Reverse key " + fwdKey + " must also be a forward meta index key. Add it to indexer.meta.forward.keys");
			forwardKeys[i++] = key2Index.get(fwdKey);
		}
		
		this.forwardWriters = new MultiFSOMapWriter[forwardKeys.length];
		this.keyFactories = new FixedSizeWriteableFactory[forwardKeys.length];
		this.forwardKeyValuesSorted = new boolean[forwardKeys.length];
		this.lastValues = new String[forwardKeys.length];
		
		for(i=0;i<forwardKeys.length;i++)
		{
			forwardWriters[i] = new MultiFSOMapWriter(
					_index.getPath() + "/" + _index.getPrefix() + "."+structureName+"-"+i+FSOrderedMapFile.USUAL_EXTENSION, 
				REVERSE_KEY_LOOKUP_WRITING_BUFFER_SIZE, 
				keyFactories[i] = new FixedSizeTextFactory(valueLensChars[forwardKeys[i]]), 
				new FixedSizeIntWritableFactory(), REVERSE_ALLOW_DUPS
				);
			forwardKeyValuesSorted[i] = true;
		}
		
		this.valueLensBytes = new int[keyNames.length];
		assert (this.valueLensBytes.length > 0);
		for(i=0;i<keyNames.length;i++)
		{
			this.valueLensBytes[i] = FixedSizeTextFactory.getMaximumTextLength(this.valueLensChars[i]);
			this.entryLengthBytes += this.valueLensBytes[i];
		}
		this.spaces = new byte[entryLengthBytes];//for padding
	}
	
	/** {@inheritDoc} */
	@Override
	public void writeDocumentEntry(Map<String, String> data) throws IOException {
		String[] values = new String[keyCount];
		int i=0;
		for(String keyName : keyNames)
		{
			values[i++] = data.get(keyName);
		}
		writeDocumentEntry(values);
	}
	
	/** {@inheritDoc} */
	@Override
	public void writeDocumentEntry(String[] data) throws IOException
	{
		int i=0;
		for(String value : data)
		{
			if (value == null)
				value = "";
			else if (value.length() > valueLensChars[i])
				if (CROP_LONG) {
					value = value.substring(0,valueLensChars[i]-1);
				}else
					throw new IllegalArgumentException("CROP_LONG="+CROP_LONG+": Data ("+value+") of string length "+value.length()+" for key "
						+keyNames[i]+" exceeds max string length of " + valueLensChars[i] +"(byte length of " + valueLensBytes[i] + 
						"). Crop in the Document, increase indexer.meta.forward.keylens, or set metaindex.compressed.crop.long");
				
			byte[] b = Text.encode(value).array();
			int numberOfBytesToWrite = b.length;
			while (numberOfBytesToWrite > valueLensBytes[i]) {
				if (CROP_LONG) {
					// we have reached an exception case, see http://terrier.org/issues/browse/TR-518
					// incrementally shorten the value until it can be encoded
						
					// guess overfill
					double oversizeRatio = (1.0*valueLensBytes[i])/numberOfBytesToWrite;
					int newTargetLength = (int)(value.length()*oversizeRatio);
					value = value.substring(0,newTargetLength-1);
					b = Text.encode(value).array();
					numberOfBytesToWrite = b.length;
					
					//logger.info("Extra cropping was applied, reducing text to length "+value.length()+" characters to fit in the target byte length "+numberOfBytesToWrite+"/"+valueLensBytes[i]);
					
				} else {
					throw new IllegalArgumentException("CROP_LONG="+CROP_LONG+": Data ('"+value+"') with "+value.length()+" characters and byte length "+numberOfBytesToWrite+" for key "
							+keyNames[i]+" exceeds max byte length of " + valueLensBytes[i] +"(string length of " 
							+ valueLensChars[i] + "). Crop in the Document, increase indexer.meta.forward.keylens, or set metaindex.compressed.crop.long");
				}
				
			}
			baos.write(b);
			if (numberOfBytesToWrite < valueLensBytes[i]) 
				baos.write(spaces, 0, valueLensBytes[i]-numberOfBytesToWrite);
			i++;
		}
		zip.reset();
		zip.setInput(baos.toByteArray());
		zip.finish();
		baos.reset();
		indexOutput.writeLong(currentOffset);
		currentIndexOffset += 8;
		int compressedEntrySize = 0;
		while(! zip.finished())
		{
			final int numOfCompressedBytes = zip.deflate(compressedBuffer);
			dataOutput.write(compressedBuffer, 0, numOfCompressedBytes);
			compressedEntrySize += numOfCompressedBytes;
		}
		currentOffset += compressedEntrySize;
		for(i=0;i<forwardKeys.length;i++)
		{
			Text key = keyFactories[i].newInstance();
			key.set(data[forwardKeys[i]]);
			IntWritable value = new IntWritable();
			value.set(entryCount);
			forwardWriters[i].write(key, value);
			if (lastValues[i] != null && data[forwardKeys[i]].compareTo(lastValues[i]) < 1)
				forwardKeyValuesSorted[i] = false;
			lastValues[i] = data[forwardKeys[i]];
		}
		entryCount++;
		
		//check for low memory, and flush if necessary
		if (entryCount % DOCS_PER_CHECK == 0 && memCheck.checkMemory())
		{
			flush();
			memCheck.reset();
		}
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void flush() throws IOException {
		//logger.info("CompressingMetaIndexBuilder flush");
		for(MapFileWriter w : forwardWriters)
			((Flushable)w).flush();
			
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void close() throws IOException
	{
		dataOutput.close();
		indexOutput.close();
		index.addIndexStructure(structureName, "org.terrier.structures.CompressingMetaIndex", "org.terrier.structures.IndexOnDisk,java.lang.String", "index,structureName");
		index.addIndexStructureInputStream(structureName, "org.terrier.structures.CompressingMetaIndex$InputStream", "org.terrier.structures.IndexOnDisk,java.lang.String", "index,structureName");
		index.setIndexProperty("index."+structureName+".entries", ""+entryCount);
		index.setIndexProperty("index."+structureName+".compression-level", ""+ZIP_COMPRESSION_LEVEL);
		index.setIndexProperty("index."+structureName+".key-names", ArrayUtils.join(keyNames, ","));
		index.setIndexProperty("index."+structureName+".value-lengths", ArrayUtils.join(valueLensChars, ","));
		index.setIndexProperty("index."+structureName+".entry-length", ""+entryLengthBytes);
		index.setIndexProperty("index."+structureName+".data-source",
			currentOffset > MAX_MB_IN_MEM_RETRIEVAL * (long)1024 * (long)1024 
			? "file"
			: "fileinmem");
		index.setIndexProperty("index."+structureName+".index-source", currentIndexOffset > MAX_INDEX_MB_IN_MEM_RETRIEVAL* (long)1024 * (long)1024 
			? "file"
			: "fileinmem");
		//TODO emit warnings
		index.flush();
		
		for(int i=0;i<forwardKeys.length;i++)
		{
			if (forwardKeyValuesSorted[i])
			{
				logger.info("Key "+ forwardKeyNames[i] + " values are sorted in meta index, consider binary searching zdata file");
				forwardWriters[i].close();
			}
			else
			{
				forwardWriters[i].close();
			}
		}		
		index.setIndexProperty("index."+structureName+".reverse-key-names", ArrayUtils.join(forwardKeyNames, ","));
		index.flush();
		
	}

	

}
