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
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.indexing;

import gnu.trove.TObjectIntHashMap;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.Deflater;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Partitioner;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.lib.NullOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.structures.Index;
import org.terrier.structures.CompressingMetaIndex.CompressingMetaIndexInputFormat;
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
import org.terrier.utility.Wrapper;
import org.terrier.utility.io.HadoopPlugin;
import org.terrier.utility.io.HadoopUtility;
/** Creates a metaindex structure that compresses all values using Deflator. 
 * <b>Properties:</b>
 * <ul>
 * <li><tt>metaindex.compressed.max.data.in-mem.mb</tt> - maximum size that a meta index .zdata file will be kept in memory. Defaults to 400(mb). </li>
 * <li><tt>metaindex.compressed.max.index.in-mem.mb</tt> - maximum size that a meta index .zdata file will be kept in memory. Defaults to 100(mb).</li>
 * <li><tt>metaindex.compressed.reverse.allow.duplicates</tt> - set this property to true to suppress errors when a reverse meta value is not unique. Default false.</li>
 * <li><tt>metaindex.compressed.crop.long</tt> - set this property to suppress errors with overlong Document metadata, while will instead be cropped.</li>
 * @since 3.0
 * @author Craig Macdonald &amp; Vassilis Plachouras 
 */
@SuppressWarnings("deprecation")
public class CompressingMetaIndexBuilder extends MetaIndexBuilder implements Flushable {
	protected static final Logger logger = LoggerFactory.getLogger(CompressingMetaIndexBuilder.class);
	protected static final int MAX_MB_IN_MEM_RETRIEVAL = 
			Integer.parseInt(ApplicationSetup.getProperty("metaindex.compressed.max.data.in-mem.mb", "400"));
	protected static final int MAX_INDEX_MB_IN_MEM_RETRIEVAL = 
			Integer.parseInt(ApplicationSetup.getProperty("metaindex.compressed.max.index.in-mem.mb", "100"));
	protected static final boolean REVERSE_ALLOW_DUPS = 
			Boolean.parseBoolean(ApplicationSetup.getProperty("metaindex.compressed.reverse.allow.duplicates", "false"));
	protected static final boolean CROP_LONG = 
			Boolean.parseBoolean(ApplicationSetup.getProperty("metaindex.compressed.crop.long", "false"));
	
	protected static final int REVERSE_KEY_LOOKUP_WRITING_BUFFER_SIZE = 20000;
	protected static final int DOCS_PER_CHECK = ApplicationSetup.DOCS_CHECK_SINGLEPASS;
	protected static final int ZIP_COMPRESSION_LEVEL = 5;//TODO (auto)configure? 
		
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
				if (CROP_LONG)
					value = value.substring(0,valueLensChars[i]-1);
				else
					throw new IllegalArgumentException("Data ("+value+") of string length "+value.length()+" for key "
						+keyNames[i]+" exceeds max string length of " + valueLensChars[i] +"(byte length of " + valueLensBytes[i] + 
						"). Crop in the Document, increase indexer.meta.forward.keylens, or set metaindex.compressed.crop.long");
				
			final byte[] b = Text.encode(value).array();
			final int numberOfBytesToWrite = b.length;
			if (numberOfBytesToWrite > valueLensBytes[i])
				throw new IllegalArgumentException("Data ("+value+") of byte length "+numberOfBytesToWrite+" for key "
						+keyNames[i]+" exceeds max byte length of " + valueLensBytes[i] +"(string length of " 
						+ valueLensChars[i] + "). Crop in the Document, or increase indexer.meta.forward.keylens");
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

	public static void main(String[] args) throws Exception
	{
		if (args.length == 0)
		{
			System.err.println("Usage: -Dterrier.index.path=hdfs://path/to/index " + CompressingMetaIndexBuilder.class.getName() + " docno");
			return;
		}
		Index.setIndexLoadingProfileAsRetrieval(false);
		IndexOnDisk index = Index.createIndex();
		reverseAsMapReduceJob(index, "meta", args);
	}
	
	/**
	 * reverseAsMapReduceJob
	 * @param index
	 * @param structureName
	 * @param keys
	 * @throws Exception
	 */
	public static void reverseAsMapReduceJob(IndexOnDisk index, String structureName, String[] keys) throws Exception
	{
		final HadoopPlugin.JobFactory jf = HadoopPlugin.getJobFactory("TerrierIndexingMeta");
		if (jf == null)
			throw new Exception("Could not get JobFactory from HadoopPlugin");
		reverseAsMapReduceJob(index, structureName, keys, jf);
		jf.close();
	}
	/**
	 * reverseAsMapReduceJob
	 * @param index
	 * @param structureName
	 * @param keys
	 * @param jf
	 * @throws Exception
	 */
	//@SuppressWarnings("deprecation")
	public static void reverseAsMapReduceJob(IndexOnDisk index, String structureName, String[] keys, HadoopPlugin.JobFactory jf) throws Exception
	{		
		long time =System.currentTimeMillis();
		final JobConf conf = jf.newJob();
		conf.setJobName("Reverse MetaIndex");
		conf.setMapOutputKeyClass(KeyValueTuple.class);
		conf.setMapOutputValueClass(IntWritable.class);
		conf.setMapperClass(MapperReducer.class);
		conf.setReducerClass(MapperReducer.class);
		conf.setNumReduceTasks(keys.length);
		conf.setPartitionerClass(KeyedPartitioner.class);
		conf.setInputFormat(CompressingMetaIndexInputFormat.class);
		conf.setReduceSpeculativeExecution(false);
		conf.set("MetaIndexInputStreamRecordReader.structureName", structureName);
		conf.setInt("CompressingMetaIndexBuilder.reverse.keyCount", keys.length);
		conf.set("CompressingMetaIndexBuilder.reverse.keys", ArrayUtils.join(keys, ","));
		conf.set("CompressingMetaIndexBuilder.forward.valueLengths", index.getIndexProperty("index."+structureName+".value-lengths", ""));
		conf.set("CompressingMetaIndexBuilder.forward.keys", index.getIndexProperty("index."+structureName+".key-names", ""));
		FileOutputFormat.setOutputPath(conf, new Path(index.getPath()));
		HadoopUtility.toHConfiguration(index, conf);
		
		conf.setOutputFormat(NullOutputFormat.class);
		try{
			RunningJob rj = JobClient.runJob(conf);
			rj.getID();
			HadoopUtility.finishTerrierJob(conf);
		} catch (Exception e) { 
			throw new Exception("Problem running job to reverse metadata", e);
		}
		//only update the index from the controlling process, so that we dont have locking/concurrency issues
		index.setIndexProperty("index."+structureName+".reverse-key-names", ArrayUtils.join(keys, ","));
		index.flush();
		logger.info("Time Taken = "+((System.currentTimeMillis()-time)/1000)+" seconds");		
	}
	/**
	 * class KeyedPartitioner
	 *
	 */
	//@SuppressWarnings("deprecation")
	public static class KeyedPartitioner implements Partitioner<KeyValueTuple, IntWritable>
	{
		protected int keyCount;
		protected TObjectIntHashMap<String> key2reverseOffset = null;
		/** 
		 * {@inheritDoc} 
		 */
		public int getPartition(KeyValueTuple kv, IntWritable docid, int numReducers) {
			if (numReducers == 1)
				return 0;
			final String key = kv.getKeyName();
			final int keyIndex = key2reverseOffset.get(key);
			return keyIndex % numReducers;
		}
		/** 
		 * {@inheritDoc} 
		 */
		public void configure(JobConf jc) {
			keyCount = jc.getInt("CompressingMetaIndexBuilder.reverse.keyCount", 0);
			key2reverseOffset = new TObjectIntHashMap<String>();
			String[] keys = jc.get("CompressingMetaIndexBuilder.reverse.keys", "").split("\\s*,\\s*");
			int i=0;
			for(String k : keys)
			{
				key2reverseOffset.put(k, i++);
			}
		}
		
	}
	
	static class KeyValueTuple implements WritableComparable<KeyValueTuple>
	{
		String k;
		String v;
		
		public KeyValueTuple(String key, String value) {
			k = key;
			v = value;
		}
		
		public KeyValueTuple(){}
		
		public String getKeyName() {
			return k;
		}
		public String getValue() {
			return v;
		}
		public void readFields(DataInput in) throws IOException {
			k = in.readUTF();
			v = in.readUTF();
		}
		public void write(DataOutput out) throws IOException {
			out.writeUTF(k);
			out.writeUTF(v);
		}
		
		public int compareTo(KeyValueTuple o) {
			final int rtr = k.compareTo(o.getKeyName());
			if (rtr != 0)
				return rtr;
			return v.compareTo(o.getValue());
		}

		@Override
		public boolean equals(Object other) {
			if (! (other instanceof KeyValueTuple))
				return false;
			return this.compareTo((KeyValueTuple)other) == 0;
		}

		@Override
		public int hashCode() {
			return k.hashCode() + v.hashCode();
		}
	}

	static class MapperReducer 
		extends HadoopUtility.MapReduceBase<IntWritable, Wrapper<String[]>, KeyValueTuple, IntWritable, Object, Object>
	{		
		String[] reverseKeyNames;
		int[] reverseKeyIndices;
		int reverseKeyCount;
				
		@Override
		protected void configureMap() throws IOException {
			reverseKeyCount = jc.getInt("CompressingMetaIndexBuilder.reverse.keyCount", 0);
			reverseKeyNames = jc.get("CompressingMetaIndexBuilder.reverse.keys", "").split("\\s*,\\s*");
			final TObjectIntHashMap<String> key2forwardOffset = new TObjectIntHashMap<String>(reverseKeyCount);
			final String[] forwardKeyNames = jc.get("CompressingMetaIndexBuilder.forward.keys", "").split("\\s*,\\s*");
			int i=0;
			for(String k : forwardKeyNames)
			{
				key2forwardOffset.put(k, i++);
			}
			reverseKeyIndices = new int[reverseKeyNames.length];
			i = 0;
			for(String k : reverseKeyNames)
			{
				reverseKeyIndices[i] = key2forwardOffset.get(k);
			}
		}
		
		public void map(IntWritable docid, Wrapper<String[]> _metadata,
				OutputCollector<KeyValueTuple, IntWritable> collector, Reporter reporter)
				throws IOException
		{
			String[] metadata = _metadata.getObject();
			reporter.setStatus("Processing metadata for document "+ docid.get());
			for(int i=0;i<reverseKeyCount;i++)
			{
				collector.collect(new KeyValueTuple(reverseKeyNames[i], metadata[i]), docid);
			}
			reporter.progress();
		}
		
		@Override
		protected void closeMap() throws IOException { }

		String currentReducingKey = null;
		MapFileWriter currentReducingOutput;
		IndexOnDisk index;
		Path reduceTaskFileDestinations;
		TObjectIntHashMap<String> key2reverseOffset = null;
		TObjectIntHashMap<String> key2valuelength = null;
		FixedSizeWriteableFactory<Text> keyFactory;
		int duplicateKeyCount = 0;
		int currentKeyTupleCount = 0;
		
		@Override
		protected void configureReduce() throws IOException {
			Index.setIndexLoadingProfileAsRetrieval(false);
			index = HadoopUtility.fromHConfiguration(jc);
			reduceTaskFileDestinations = FileOutputFormat.getWorkOutputPath(jc);
			Files.mkdir(reduceTaskFileDestinations.toString());
			
			String structureName = jc.get("MetaIndexInputStreamRecordReader.structureName", "");
			reverseKeyCount = jc.getInt("CompressingMetaIndexBuilder.reverse.keyCount", 0);
			reverseKeyNames = jc.get("CompressingMetaIndexBuilder.reverse.keys", "").split("\\s*,\\s*");
			key2reverseOffset = new TObjectIntHashMap<String>(reverseKeyCount);
			int i=0;
			for(String k : reverseKeyNames)
			{
				key2reverseOffset.put(k, i++);
			}
			key2valuelength = new TObjectIntHashMap<String>(reverseKeyCount);
			final String[] allKeys = index.getIndexProperty("index."+structureName+".key-names", "").split("\\s*,\\s*");
			final String[] allValueLens = index.getIndexProperty("index."+structureName+".value-lengths", "").split("\\s*,\\s*");
			i=0;
			for(String k : allKeys)
			{
				logger.debug("Key "+ k + " value length="+ allValueLens[i]);
				key2valuelength.put(k, Integer.parseInt(allValueLens[i++]));
			}
		}
		
		/** Reduce function. Input Key: (meta Key name, meta Key value) Value: list of matching docids. */
		public void reduce(KeyValueTuple metaTuple, Iterator<IntWritable> docids,
				OutputCollector<Object, Object> arg2, Reporter reporter)
			throws IOException
		{
			if (currentReducingKey == null || !  metaTuple.getKeyName().equals(currentReducingKey))
			{
				if (currentReducingKey != null)
				{
					logger.info("currentKey was "+ currentReducingKey + " ("+currentKeyTupleCount+" entries) new Key is " + metaTuple.getKeyName()
							+ " : force closed");
					currentReducingOutput.close();
					if (duplicateKeyCount > 0)
					{
						logger.warn("MetaIndex key "+currentReducingKey + " had "+ duplicateKeyCount + " distinct values with duplicated associated document ids");
					}
					currentReducingOutput = null;
				}
				currentKeyTupleCount = 0;
				duplicateKeyCount = 0;
				currentReducingKey = metaTuple.getKeyName();
				currentReducingOutput = openMapFileWriter(currentReducingKey);
				logger.info("Opening new MapFileWriter for key "+ currentReducingKey);
			}
			final IntWritable docid = docids.next();
			final Text key = keyFactory.newInstance();
			key.set(metaTuple.getValue());
			currentReducingOutput.write(key, docid);
			currentKeyTupleCount++;
			int extraCount  = 0;
			while(docids.hasNext())
			{
				docids.next();
				extraCount++;
			}
			reporter.progress();
			if (extraCount > 0)
			{
				//logger.warn("Key "+currentReducingKey + " value "+ metaTuple.getValue() + " had "+ extraCount +" extra documents. First document selected.");
				duplicateKeyCount++;
			}
			reporter.setStatus("Reducing metadata value "+ metaTuple.getValue());
		}
		
		@Override
		protected void closeReduce() throws IOException
		{
			if (currentKeyTupleCount > 0)
			{
				logger.info("Finished reducing for " + currentReducingKey +", with " +currentKeyTupleCount +" entries");
			}
			if (duplicateKeyCount > 0)
			{
				logger.warn("MetaIndex key "+currentReducingKey + " had "+ duplicateKeyCount + " distinct values with duplicated associated document ids");
			}
			if (currentReducingOutput != null)
			currentReducingOutput.close();
		}
		
		/* open a MapFileWriter for the specified key. This will automatically promoted to the index folder when the job is finished */
		protected MapFileWriter openMapFileWriter(String keyName) throws IOException
		{
			final int metaKeyIndex = key2reverseOffset.get(keyName);
			final int valueLength = key2valuelength.get(keyName);
			keyFactory = new FixedSizeTextFactory(valueLength);
			logger.info("Opening MapFileWriter for key "+ keyName + " - index " + metaKeyIndex);
			return FSOrderedMapFile.mapFileWrite(reduceTaskFileDestinations.toString() /*index.getPath()*/ 
						+ "/" + ((IndexOnDisk) index).getPrefix() + "."
						+ jc.get("MetaIndexInputStreamRecordReader.structureName")
						+ "-"+metaKeyIndex+FSOrderedMapFile.USUAL_EXTENSION
					);
			//
			/*return new MultiFSOMapWriter(
					reduceTaskFileDestinations.toString()
						+ "/" + index.getPrefix() + "."
						+ jc.get("MetaIndexInputStreamRecordReader.structureName")
						+ "-"+metaKeyIndex+FSOrderedMapFile.USUAL_EXTENSION, 
					REVERSE_KEY_LOOKUP_WRITING_BUFFER_SIZE, 
					keyFactory = new FixedSizeTextFactory(valueLength), 
					new FixedSizeIntWritableFactory()
				);*/
		}
	}

}
