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
 * The Original Code is CompressingMetaIndex.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import gnu.trove.TObjectIntHashMap;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.sorting.HeapSortInt;
import org.terrier.structures.collections.FSOrderedMapFile;
import org.terrier.structures.collections.OrderedMap;
import org.terrier.structures.seralization.FixedSizeIntWritableFactory;
import org.terrier.structures.seralization.FixedSizeTextFactory;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.Files;
import org.terrier.utility.TerrierTimer;
import org.terrier.utility.Wrapper;
import org.terrier.utility.io.HadoopUtility;
import org.terrier.utility.io.RandomDataInput;
import org.terrier.utility.io.RandomDataInputMemory;
import org.terrier.utility.io.WrappedIOException;

/** A {@link MetaIndex} implementation that compresses contents. 
 * Values have maximum lengths, but overall value blobs are 
 * compressed using java.util.zip.Inflater.
 * @author Craig Macdonald &amp; Vassilis Plachouras
 * @since 3.0
 */
@SuppressWarnings("deprecation")
public class CompressingMetaIndex implements MetaIndex {
	
	private final static Pattern SPLIT_SPACE = Pattern.compile("\\s+");
	
	/** logger to be used in this class */
	private static Logger logger = LoggerFactory.getLogger(CompressingMetaIndex.class);
	/** 
	 * A Hadoop input format for a compressing meta index (allows the reading of a meta index
	 * as input to a MapReduce job.
	 */
	public static class CompressingMetaIndexInputFormat implements InputFormat<IntWritable, Wrapper<String[]>>
	{
		static String STRUCTURE_NAME_JC_KEY = "MetaIndexInputStreamRecordReader.structureName";
		/** 
		 * Set structure
		 * @param jc
		 * @param metaStructureName
		 */
		public static void setStructure(JobConf jc, String metaStructureName)
		{
			jc.set(STRUCTURE_NAME_JC_KEY, metaStructureName);
		}
		
		static class MetaIndexSplit extends FileSplit
		{
			int startId;
			int endId;
			
			public MetaIndexSplit(){
				super(null, (long)0, (long)0, new String[0]);
			}
			
			public MetaIndexSplit(Path file, long start, long length, String[] hosts, int _startId, int _endId) {
				super(file, start, length, hosts);
				startId = _startId;
				endId = _endId;
			}			
			
			public void readFields(DataInput in) throws IOException {
				super.readFields(in);
				startId = in.readInt();
				endId = in.readInt();
			}

			public void write(DataOutput out) throws IOException {
				super.write(out);
				out.writeInt(startId);
				out.writeInt(endId);
			}
			
			public String toString()
			{
				StringBuilder rtr = new StringBuilder();
				rtr.append("MetaIndexSplit: BlockSize=").append(this.getLength());
				rtr.append(" startAt=").append(+this.getStart());
				try{
					rtr.append(" hosts=");
					rtr.append(ArrayUtils.join(this.getLocations(), ","));
				}
				catch (IOException ioe ) {
					logger.warn("Problem getting locations", ioe);
				}
				rtr.append(" ids=["+startId+","+endId +"]");
				return rtr.toString();
			}
		}		
		
		static class MetaIndexInputStreamRecordReader implements RecordReader<IntWritable, Wrapper<String[]>>
		{
			final InputStream in;
			final int startID;
			final int endID;
			
			public MetaIndexInputStreamRecordReader(IndexOnDisk index, String structureName, int startingDocID, int endingID)
				throws IOException
			{
				in = new InputStream(index, structureName, startingDocID, endingID);
				startID = startingDocID;
				endID = endingID;
			}
			
			public void close() throws IOException {
				in.close();
			}

			public IntWritable createKey() {
				return new IntWritable();
			}

			public Wrapper<String[]> createValue() {
				return new Wrapper<String[]>();
			}

			public long getPos() throws IOException {
				return 0;
			}

			public float getProgress() throws IOException {
				return (float)(in.getIndex() - startID)/(float)(endID - startID);
			}

			public boolean next(IntWritable docid, Wrapper<String[]> values)
					throws IOException
			{
				if (! in.hasNext())
					return false;
				//these methods MUST have this order
				values.setObject(in.next());
				docid.set(in.getIndex());
				return true;
			}
			
		}
		
		
		/** 
		 * {@inheritDoc} 
		 */
		public RecordReader<IntWritable, Wrapper<String[]>> getRecordReader(
				InputSplit _split, JobConf jc, Reporter reporter)
				throws IOException
		{
			HadoopUtility.loadTerrierJob(jc);
			
			//load the index
			Index.setIndexLoadingProfileAsRetrieval(false);
			IndexOnDisk index = HadoopUtility.fromHConfiguration(jc);
			if (index == null)
				throw new IOException("Index could not be loaded from JobConf: " + Index.getLastIndexLoadError() );
			
			//determine the structure to work on
			String structureName = jc.get(STRUCTURE_NAME_JC_KEY);
			if (structureName == null)
				throw new IOException("JobConf property "+STRUCTURE_NAME_JC_KEY+" not specified");
			
			//get the split
			MetaIndexSplit s = (MetaIndexSplit)_split;
			return new MetaIndexInputStreamRecordReader(index, structureName, s.startId, s.endId);			
		}
		
		private static String[] getHosts(FileStatus fs, FileSystem f, long start, long len) throws IOException
		{
			BlockLocation[] bs = f.getFileBlockLocations(fs, start, len);
			Set<String> hosts = new HashSet<String>();
			for(BlockLocation b : bs)
			{
				for(String host : b.getHosts())
				{
					hosts.add(host);
				}
			}
			return hosts.toArray(new String[0]);
		}
		/** 
		 * {@inheritDoc} 
		 */
		public InputSplit[] getSplits(JobConf jc, int advisedNumberOfSplits)
				throws IOException
		{
			HadoopUtility.loadTerrierJob(jc);
			List<InputSplit> splits = new ArrayList<InputSplit>(advisedNumberOfSplits);
			IndexOnDisk index = HadoopUtility.fromHConfiguration(jc);
			String structureName = jc.get(STRUCTURE_NAME_JC_KEY);
			final String dataFilename = index.getPath() + ApplicationSetup.FILE_SEPARATOR + index.getPrefix() + "." + structureName + ".zdata";
			final String indxFilename = index.getPath() + ApplicationSetup.FILE_SEPARATOR + index.getPrefix() + "." + structureName + ".idx";
			final DataInputStream idx = new DataInputStream(Files.openFileStream(indxFilename));
			FileSystem fSys = FileSystem.get(jc);
			FileStatus fs = fSys.getFileStatus(new Path(dataFilename));
			
			final int entryCount = index.getIntIndexProperty("index."+structureName+".entries", 0);
			long dataFileBlockSize = fs.getBlockSize();
			if (forcedDataFileBlockSize != -1) dataFileBlockSize = forcedDataFileBlockSize;
			logger.debug("Block size for "+ dataFilename + " is " + dataFileBlockSize);
			//logger.debug("FSStatus("+dataFilename+")="+ fs.toString());
			int startingId = 0;
			int currentId = 0;
			long startingBlockLocation = 0;
			long blockSizeSoFar = 0;
			long lastRead = idx.readLong();
			while(++currentId < entryCount)
			{
				lastRead = idx.readLong();
				blockSizeSoFar = lastRead - startingBlockLocation;
				//logger.debug("Offset for docid "+ currentId + " is " + lastRead + " blockSizeSoFar="+blockSizeSoFar + " blockStartsAt="+startingBlockLocation);
				if (blockSizeSoFar > dataFileBlockSize)
				{
					final String[] hosts = getHosts(fs, fSys, startingBlockLocation, blockSizeSoFar);
					MetaIndexSplit s = new MetaIndexSplit(new Path(dataFilename), startingBlockLocation, blockSizeSoFar, hosts, startingId, currentId);
					splits.add(s);
					logger.debug("Got split: "+ s.toString());
					
					blockSizeSoFar = 0;
					startingBlockLocation = lastRead + 1;
					startingId = currentId +1;
				}
			}
			if (startingId < currentId)
			{
				blockSizeSoFar = lastRead - startingBlockLocation;
				final String[] hosts = getHosts(fs, fSys, startingBlockLocation, blockSizeSoFar);
				MetaIndexSplit s = new MetaIndexSplit(new Path(dataFilename), startingBlockLocation, blockSizeSoFar, hosts, startingId, currentId-1);
				logger.debug("Got last split: "+ s);
				splits.add(s);
			}
			idx.close();
			logger.debug("Got "+ splits.size() + " splits when splitting meta index");
			return splits.toArray(new InputSplit[0]);
		}
		
		long forcedDataFileBlockSize = -1;
		
		/** Permit the blocksize to be overridden, useful for testing different code paths */
		public void overrideDataFileBlockSize(long blocksize)
		{
			forcedDataFileBlockSize = blocksize;
		}
		/** 
		 * Validates the structure based on the job configuration
		 */
		public void validateInput(JobConf jc) throws IOException {
			if (jc.get(STRUCTURE_NAME_JC_KEY, null) == null)
				throw new WrappedIOException(new IllegalArgumentException("Key " + STRUCTURE_NAME_JC_KEY +" not specified"));
		}
		
	}
	
	/** thread-local cache of Inflaters to be re-used for decompression */
	protected static final ThreadLocal<Inflater> inflaterCache = new ThreadLocal<Inflater>() 
	{
		protected final synchronized Inflater initialValue() {
			return new Inflater();
		}
	};
	
	static interface ByteAccessor extends java.io.Closeable
	{
		byte[] read(long offset, int bytes) throws IOException;
	}
	
	static class RandomDataInputAccessor implements ByteAccessor
	{
		final RandomDataInput dataSource;
		public RandomDataInputAccessor(RandomDataInput rdi)
		{
			this.dataSource = rdi;
		}
		
		public final byte[] read(long offset, int bytes) throws IOException
		{
			byte[] out = new byte[bytes];
			dataSource.seek(offset);
			dataSource.readFully(out);
			return out;
		}
		
		public final void close() throws IOException
		{
			dataSource.close();
		}
	}
	
	static class ChannelByteAccessor implements ByteAccessor
	{
		final RandomAccessFile dataSource;
		final FileChannel dataSourceChannel; 
		
		public ChannelByteAccessor(RandomAccessFile ds)
		{
			dataSource = ds;
			dataSourceChannel = dataSource.getChannel();
		}
		
		public final byte[] read(long offset, int bytes) throws IOException
		{
			byte[] out = new byte[bytes];
			dataSourceChannel.read(MappedByteBuffer.wrap(out), offset);
			return out;
		}
		
		public final void close() throws IOException
		{
			dataSourceChannel.close();
			dataSource.close();
		}
	}
	
	static final class LoggingDocid2OffsetLookup implements Docid2OffsetLookup
	{
		final Docid2OffsetLookup parent;
		public LoggingDocid2OffsetLookup(Docid2OffsetLookup _parent)
		{
			this.parent = _parent;
		}
		
		public int getLength(int docid) throws IOException {
			final int length = this.parent.getLength(docid);
			//logger.debug("Lookup of length of meta record for doc "+ docid + " gave length "+ length);
			return length;
		}
		
		public long getOffset(int docid) throws IOException {
			final long offset = this.parent.getOffset(docid);
			//logger.debug("Lookup of offset of meta record for doc "+ docid + " gave offset "+ offset);
			return offset;
		}
		
		public void close() throws IOException {
			parent.close();
		}		
	}
	
	static interface Docid2OffsetLookup extends java.io.Closeable
    {	
        long getOffset(int docid) throws IOException;
        int getLength(int docid) throws IOException;
    }
	
	static class ArrayDocid2OffsetLookup implements Docid2OffsetLookup
    {
        protected final long[] docid2offsets;
        protected final long fileLength;
        protected final int docidCount;

        public ArrayDocid2OffsetLookup(long[] _docid2offsets, long _fileLength)
        {
            docid2offsets = _docid2offsets;
            fileLength = _fileLength;
            docidCount = docid2offsets.length;
        }

        public final long getOffset(final int docid)
        {
            return docid2offsets[docid];
        }
        
        public final int getLength(final int docid)
        {
            return  (docid+1)==docidCount 
            	? (int)(fileLength-docid2offsets[docid])
            	: (int)(docid2offsets[docid+1] - docid2offsets[docid]);
        }
        
        public void close()
        {}
    }
	
	static class OnDiskDocid2OffsetLookup implements Docid2OffsetLookup
    {
		private static final int SIZE_OF_LONG = Long.SIZE / 8;
		final ByteAccessor b;
        int lastDocid = -1;
        long lastOffset = -1;
        int lastLength = -1;
        
        protected final long fileLength;
        protected final int docidCount;

        public OnDiskDocid2OffsetLookup(ByteAccessor _b, int _docCount, long _fileLength)
        {
            b=_b;
            docidCount = _docCount;
            fileLength = _fileLength;
        }

        public final long getOffset(final int docid) throws IOException
        {
        	readOffset(docid);
        	//logger.info("Offset for docid "+ docid + " is " + lastOffset);
            return lastOffset;
        }

        public final int getLength(final int docid) throws IOException
        {
        	readOffset(docid);
        	//logger.info("length for docid "+ docid + " is " + lastLength);
            return lastLength;
        }
        
        protected final void readOffset(int docid) throws IOException
        {
        	if (docid == lastDocid)
        		return;
        	if (docid +1 == docidCount )
        	{
        		final byte[] readBuffer = b.read((long)docid * SIZE_OF_LONG, SIZE_OF_LONG);
            	lastOffset = (((long)readBuffer[0] << 56) +
                         ((long)(readBuffer[1] & 255) << 48) +
                         ((long)(readBuffer[2] & 255) << 40) +
                         ((long)(readBuffer[3] & 255) << 32) +
                         ((long)(readBuffer[4] & 255) << 24) +
                         ((readBuffer[5] & 255) << 16) +
                         ((readBuffer[6] & 255) <<  8) +
                         ((readBuffer[7] & 255) <<  0));
            	lastLength = (int)(fileLength - lastOffset);
        	}
        	else
        	{
        		final byte[] readBuffer = b.read((long)docid * SIZE_OF_LONG, SIZE_OF_LONG*2);
            	lastOffset = (((long)readBuffer[0] << 56) +
                         ((long)(readBuffer[1] & 255) << 48) +
                         ((long)(readBuffer[2] & 255) << 40) +
                         ((long)(readBuffer[3] & 255) << 32) +
                         ((long)(readBuffer[4] & 255) << 24) +
                         ((readBuffer[5] & 255) << 16) +
                         ((readBuffer[6] & 255) <<  8) +
                         ((readBuffer[7] & 255) <<  0));
            	final long tmpLong = (((long)readBuffer[8+0] << 56) +
                        ((long)(readBuffer[8+1] & 255) << 48) +
                        ((long)(readBuffer[8+2] & 255) << 40) +
                        ((long)(readBuffer[8+3] & 255) << 32) +
                        ((long)(readBuffer[8+4] & 255) << 24) +
                        ((readBuffer[8+5] & 255) << 16) +
                        ((readBuffer[8+6] & 255) <<  8) +
                        ((readBuffer[8+7] & 255) <<  0));
            	lastLength = (int)(tmpLong - lastOffset);
        	}
        	        	
        	lastDocid = docid;
        }

        public void close() throws IOException
        {
        	b.close();
        }
    }
	
	static class BinarySearchForwardIndex implements OrderedMap<Text, IntWritable>
	{
		int numberOfEntries = 0;
		MetaIndex meta;
		int itemIndex = 0;
		
		public BinarySearchForwardIndex(MetaIndex _meta, int _numberOfEntries, int _itemIndex)
		{
			meta = _meta;
			numberOfEntries = _numberOfEntries;
			itemIndex = _itemIndex;
		}

		public IntWritable get(Object _key) {
			
			int[] bounds = new int[]{0, numberOfEntries};
	    	int low = bounds[0];
			int high = bounds[1];
			
			int i;
			int compareEntry;
			
			String key = ((Text)_key).toString();
			//Text testKey = new Text();
			IntWritable value = new IntWritable();	
			
			try{
			
				while (low <= high) {
				    i = (low + high) >>> 1;
                    String[] parts = meta.getAllItems(i);
	                
	                if ((compareEntry = parts[itemIndex].compareTo(key))< 0)
	                	low = i + 1;
	                else if (compareEntry > 0)
	                	high = i - 1;
	                else 
	                {
	                    //return the data
	                	value.set(i);
	                	return value;
	                }
	            }
	        
	            if (high == numberOfEntries)
	                return null;
	            
	                   	if (high == 0) {
            		i = 0; 
	            } else {
	                i = high;
	            }
            	String[] parts = meta.getAllItems(i);
	            
	        
	            if (key.compareTo(parts[itemIndex]) == 0) {
	            	value.set(i);
	                return value;
	            }
			} catch (IOException ioe) {
			  logger.error("IOException reading FSOrderedMapFile", ioe);
			}
			return null;
		}
		
		public java.util.Map.Entry<Text, IntWritable> get(int index) {
			throw new UnsupportedOperationException("");
		}
		
		public boolean containsKey(Object key) {
			return get(key) != null;
		}
		
		public int size() {
			return numberOfEntries;
		}

		public void clear() {
			throw new UnsupportedOperationException("");
		}		

		public boolean containsValue(Object value) {
			throw new UnsupportedOperationException("");
		}

		public Set<java.util.Map.Entry<Text, IntWritable>> entrySet() {
			throw new UnsupportedOperationException("");
		}		

		public boolean isEmpty() {
			return false;
		}

		public Set<Text> keySet() {
			throw new UnsupportedOperationException("");
		}

		public Integer put(String key, IntWritable value) {
			throw new UnsupportedOperationException("");
		}

		public void putAll(Map<? extends Text, ? extends IntWritable> t) {
			throw new UnsupportedOperationException("");
		}

		public IntWritable remove(Object key) {
			throw new UnsupportedOperationException("");
		}

		public Collection<IntWritable> values() {
			throw new UnsupportedOperationException("");
		}

		public IntWritable put(Text key, IntWritable value) {
			throw new UnsupportedOperationException("");
		}	
	}
	/** An iterator for reading a MetaIndex as a stream */
	public static class InputStream implements Iterator<String[]>, java.io.Closeable
	{
		final DataInputStream zdata;
		final DataInputStream idx;
		final protected int compressionLevel;
		final protected int recordLength;
		
		protected Inflater inflater;
		
		protected int keyCount;
		protected int[] keyByteOffset;
		protected int[] valueByteLengths;
		//private int[] valueCharLengths;
		
		final int numberOfRecords;
		final int lastId;
		int index=0;
		
		//String[] metaValues;
		
		protected long lastOffset;
		protected long fileLength;
		/**
		 * Constructs an instance of the class with
		 * @param _index
		 * @param _structureName
		 * @param _startingId
		 * @param _endId
		 * @throws IOException
		 */
		public InputStream(IndexOnDisk _index, String _structureName, int _startingId, int _endId) throws IOException
		{
			final String dataFilename = _index.getPath() + ApplicationSetup.FILE_SEPARATOR + _index.getPrefix() + "." + _structureName + ".zdata";
			final String indxFilename = _index.getPath() + ApplicationSetup.FILE_SEPARATOR + _index.getPrefix() + "." + _structureName + ".idx";
			zdata = new DataInputStream(Files.openFileStream(dataFilename));
			idx = new DataInputStream(Files.openFileStream(indxFilename));
			fileLength = Files.length(dataFilename);
			
			//1. int - how much zlib was used
			compressionLevel = _index.getIntIndexProperty("index."+_structureName+".compression-level", 5);
			//2. int - how big each record was before compression
			//recordLength = _index.getIntIndexProperty("index."+_structureName+".entry-length", 0);
			//TR-167: recordLength is counted as characters instead of bytes in Terrier 3.0, and hence is inaccurate.
			//obtain from value character lengths instead
			
			//3. key names
			//keyNames = index.getIndexProperty("index."+_structureName+".key-names", "").split("\\s*,\\s*");
			//4. lengths of each key
			String[] _tmpValueLengths = _index.getIndexProperty("index."+_structureName+".value-lengths", "").split("\\s*,\\s*");
			int i=0;
			valueByteLengths = new int[_tmpValueLengths.length];
			int _recordLength = 0;
			if (_index.getIndexProperty("index."+_structureName+".value-lengths", "").length()>0) {
			for(String lens : _tmpValueLengths)
			{
				valueByteLengths[i] = FixedSizeTextFactory.getMaximumTextLength(Integer.parseInt(lens));
				_recordLength += valueByteLengths[i];
				i++;
			}
			}
			recordLength = _recordLength;
			keyCount = valueByteLengths.length;
			
			//5. offsets in file
			lastId = _endId;
			numberOfRecords = _index.getIntIndexProperty("index."+_structureName+".entries", 0);
						
			inflater = inflaterCache.get();
			index = _startingId -1;
			long targetSkipped = (long)_startingId  * (long)8;
			long actualSkipped = 0;
			//skip to appropriate place in index file
			while(actualSkipped < targetSkipped)
			{
				actualSkipped += idx.skip(targetSkipped - actualSkipped);
			}
			lastOffset = idx.readLong();
			//now skip forward in data file also
			if (lastOffset > 0)
			{
				long actualSkippedData = 0;
				while(actualSkippedData < lastOffset)
				{
					actualSkippedData += zdata.skip(lastOffset - actualSkippedData);
				}
			}
			keyByteOffset = new int[keyCount];
			int cumulativeOffset = 0;
			for(i=0;i<keyCount;i++)
			{
				//key2length.put(keyNames[i], keyLengths[i]);
				//key2offset.put(keyNames[i], cumulativeOffset);
				keyByteOffset[i] = cumulativeOffset;
				cumulativeOffset += valueByteLengths[i];
			}
		}
		/**
		 * Constructs an instance of the class with
		 * @param _index
		 * @param structureName
		 * @throws IOException
		 */
		public InputStream(IndexOnDisk _index, String structureName) throws IOException
		{
			this(_index, structureName, 0, -1 + _index.getIntIndexProperty("index."+structureName+".entries", 0));
		}
		/** 
		 * {@inheritDoc} 
		 */
		public boolean hasNext() {
			//logger.info("Checking that docid "+ index + " not greater than "+ lastId);
			return index < lastId;			
		}
		/** Return the position that we are at (entry number) */
		public int getIndex()
		{
			return index;
		}
		/** 
		 * {@inheritDoc} 
		 */
		public String[] next() {
			index++;
			long endOffset = -1;
			long startOffset = -1;
			try
			{	
				//logger.info("Checking for index "+ (index+1) + " < last possible id " + numberOfRecords);
				endOffset = index < (numberOfRecords-1)
					? idx.readLong() -1
					: fileLength-1;
				startOffset = lastOffset;
				final int dataLength = (int)(endOffset - lastOffset + 1);
				//logger.info("Reading zdata file docid="+index+" start=" + lastOffset + " end="+endOffset + " length="+dataLength);
				byte[] b = new byte[dataLength];
				zdata.readFully(b);
				lastOffset = endOffset +1;
				inflater.reset();
				inflater.setInput(b);
				byte[] bOut = new byte[recordLength];
				inflater.inflate(bOut);
				String[] sOut = new String[keyCount];
		        for(int i=0;i<keyCount;i++)
		        {
		            sOut[i] = Text.decode(
		                bOut,
		                keyByteOffset[i],
		                valueByteLengths[i]).trim();
		        }
		        //logger.info("Got entry " + Arrays.deepToString(sOut));
		        return sOut;
			} catch (Exception ioe) {
				logger.error("Problem reading MetaIndex as a stream. index="+ index + " start="+startOffset+" endOffset="+endOffset, ioe);
				return null;
			}
		}
		/** 
		 * {@inheritDoc} 
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
		/** 
		 * {@inheritDoc} 
		 */
		public void close() throws IOException
		{
			zdata.close();
			idx.close();
		}
		
	}
	
	protected Docid2OffsetLookup offsetLookup;
	
	//protected long[] docid2offsets;
	protected int compressionLevel;
	protected int recordLength;
	//protected long fileLength;
	
	//protected int EntryLength;

	protected String[] keyNames;
	protected TObjectIntHashMap<String> key2byteoffset;
	protected TObjectIntHashMap<String> key2bytelength;
	
	protected TObjectIntHashMap<String> key2forwardOffset;
	
	protected int keyCount;
	protected int[] valueByteOffsets;
	protected int[] valueByteLengths;
	
	protected final String path;
	protected final String prefix;
	
	protected final ByteAccessor dataSource;
	protected Map<Text,IntWritable>[] forwardMetaMaps;
	protected FixedSizeWriteableFactory<Text>[] keyFactories;
	
	/**
	 * Construct an instance of the class with
	 * @param index
	 * @param structureName
	 * @throws IOException
	 */
	public CompressingMetaIndex(IndexOnDisk index, String structureName)
		throws IOException
	{
		this.path = index.getPath(); this.prefix = index.getPrefix();
		loadIndex(index, structureName);
		final String dataFilename = 
			path + ApplicationSetup.FILE_SEPARATOR + prefix + "."+structureName+".zdata";
		long dataFileLength = Files.length(dataFilename);

		String fileSource = index.getIndexProperty("index."+structureName + ".data-source", "fileinmem");
		ByteAccessor _dataSource = null;
		if (fileSource.equals("fileinmem"))
		{
			logger.info("Structure "+ structureName + " loading data file into memory");
			try{
				logger.debug("Caching metadata file "+ dataFilename + " to memory");
				final DataInputStream di = new DataInputStream(Files.openFileStream(dataFilename));
				_dataSource = new RandomDataInputAccessor(new RandomDataInputMemory(di, dataFileLength));
				di.close();
			} catch (OutOfMemoryError oome) {
				logger.warn("OutOfMemoryError: Structure "+ structureName + " reading data file directly from disk");
				//logger.debug("Metadata will be read directly from disk");
				RandomDataInput rfi = Files.openFileRandom(dataFilename);
				_dataSource = (rfi instanceof RandomAccessFile)
					? new ChannelByteAccessor((RandomAccessFile)rfi)
					: new RandomDataInputAccessor(rfi);
			}
			dataSource = _dataSource;
		}
		else if (fileSource.equals("file"))
		{
			logger.warn("Structure "+ structureName + " reading data file directly from disk (SLOW) - try index."
					+structureName+".data-source=fileinmem in the index properties file");
			//logger.debug("Metadata will be read directly from disk");
			RandomDataInput rfi = Files.openFileRandom(dataFilename);
			dataSource = (rfi instanceof RandomAccessFile)
				? new ChannelByteAccessor((RandomAccessFile)rfi)
				: new RandomDataInputAccessor(rfi);
		}
		else
		{
			throw new IOException(
				"Bad property value for index."+structureName + ".source="+fileSource); 
		}
	}
	/** 
	 * {@inheritDoc} 
	 */
	public String[] getKeys()
	{
		return this.keyNames;
	}
	
	/** Closes the underlying structures.*/
	public void close() throws IOException {
		dataSource.close();
		offsetLookup.close();
		for (Map<Text,IntWritable> m : forwardMetaMaps)
		{
			IndexUtil.close(m);
		}
	}
	
	/** {@inheritDoc} */
	public int getDocument(String key, String value) throws IOException {
		final int forwardId = key2forwardOffset.get(key) -1;
		if (forwardId == -1)
			throw new NoSuchElementException("No reverse lookup for key " + key + " is supported");
		final Text wKey = keyFactories[forwardId].newInstance();
		wKey.set(value);
		assert forwardMetaMaps[forwardId].size() > 0;
		final IntWritable rtr = forwardMetaMaps[forwardId].get(wKey);		
		if (rtr == null)
			return -1;
		return rtr.get();
	}
	
	/** {@inheritDoc}.
	 * In this implementation, _docids are sorted to improve disk cache hits. 
	 *  _docids is however unchanged.
	 */
	public String[] getItems(String Key, int[] _docids) throws IOException {
		final int numDocs = _docids.length;
		final int[] docids = new int[numDocs];
		System.arraycopy(_docids, 0, docids, 0, numDocs);
		final String values[] = new String[numDocs];
		//optimisation: order by docid, to improve disk cache hit rate
		final int[] order = new int[numDocs];
		for(int i=0;i<numDocs;i++)
			order[i] = i;
		HeapSortInt.ascendingHeapSort(docids, order);
		
		for(int i=0;i<numDocs;i++)
		{
			values[order[i]] = getItem(Key, docids[i]);
		}
		return values;
	}

	/** {@inheritDoc} 
	 *  In this implementation, _docids are sorted to improve disk cache hits. 
	 *  _docids is however unchanged. */
	public String[][] getItems(String Keys[], final int[] _docids) throws IOException {
		final int numDocs = _docids.length;
		final int[] docids = new int[numDocs];
		System.arraycopy(_docids, 0, docids, 0, numDocs);
		final String[][] saOut = new String[numDocs][];
		
		//optimisation: order by docid, to improve disk cache hit rate
		final int[] order = new int[numDocs];
		for(int i=0;i<numDocs;i++)
			order[i] = i;
		HeapSortInt.ascendingHeapSort(docids, order);
		
		for(int i=0;i<numDocs;i++)
		{
			saOut[order[i]] = getItems(Keys, docids[i]);
		}
		return saOut;
	}

	/** {@inheritDoc} */	
	public String getItem(String Key, int docid)
        throws IOException
    {
		Inflater unzip = inflaterCache.get();
		unzip.reset();
		unzip.setInput(dataSource.read(
			offsetLookup.getOffset(docid), offsetLookup.getLength(docid)
			));
		
		byte[] bOut = new byte[recordLength];
		try {
			unzip.inflate(bOut);
		} catch(DataFormatException dfe) {
			logger.error("Failed to inflate compressed meta data", dfe);
		}
		return Text.decode(bOut, key2byteoffset.get(Key), key2bytelength.get(Key)).trim();
    }
	
	/** {@inheritDoc} */
	public String[] getItems(String[] Keys, int docid) throws IOException {
		Inflater unzip = inflaterCache.get();
		unzip.reset();
		unzip.setInput(dataSource.read(
				offsetLookup.getOffset(docid), offsetLookup.getLength(docid)
				));
		byte[] bOut = new byte[recordLength];
		try {
			unzip.inflate(bOut);
		} catch(DataFormatException dfe) {
			logger.error("Failed to inflate compressed meta data", dfe);
		}
        final int kCount = Keys.length;
        String[] sOut = new String[kCount];
        for(int i=0;i<kCount;i++)
        {
            sOut[i] = Text.decode(
                bOut,
                key2byteoffset.get(Keys[i]),
                key2bytelength.get(Keys[i])).trim();
        }
        return sOut;
    }
	
	/** {@inheritDoc} */
	public String[] getAllItems(int docid) throws IOException {
		Inflater unzip = inflaterCache.get();
		unzip.reset();
		unzip.setInput(dataSource.read(
				offsetLookup.getOffset(docid), offsetLookup.getLength(docid)
				));
		//unzip.setInput(
		//		dataSource.read(docid2offsets[docid],
		//				(docid+1)==docid2offsets.length ? (int)(fileLength-docid2offsets[docid])
		//				                                : (int)(docid2offsets[docid+1] - docid2offsets[docid])));
		byte[] bOut = new byte[recordLength];
		try {
			unzip.inflate(bOut);
		} catch(DataFormatException dfe) {
			logger.error("Failed to inflate compressed meta data", dfe);
		}
        final int kCount = this.keyCount;
        String[] sOut = new String[kCount];
 
        
        for(int i=0;i<kCount;i++)
        {
            sOut[i] = Text.decode(
                bOut,
                valueByteOffsets[i],
                valueByteLengths[i]).trim();
        }
        return sOut;
	}

	@SuppressWarnings("unchecked")
	protected void loadIndex(IndexOnDisk index, String structureName) throws IOException {
	     
		//1. int - how much zlib was used
		compressionLevel = index.getIntIndexProperty("index."+structureName+".compression-level", 5);
		//2. int - how big each record was before compression
		//recordLength = index.getIntIndexProperty("index."+structureName+".entry-length", 0);
		//TR-167: recordLength is counted as characters instead of bytes in Terrier 3.0, and hence is inaccurate.
		//obtain from value character lengths instead
		
		//3. key names
		keyNames = index.getIndexProperty("index."+structureName+".key-names", "").split("\\s*,\\s*");
		//4. lengths of each key
		String[] _tmpValueLengths = index.getIndexProperty("index."+structureName+".value-lengths", "").split("\\s*,\\s*");
		int i=0;
		valueByteLengths = new int[_tmpValueLengths.length];
		int[] valueCharLengths = new int[_tmpValueLengths.length];
		recordLength = 0;
		if (index.getIndexProperty("index."+structureName+".value-lengths", "").length()>0) {
		for(String lens : _tmpValueLengths)
		{
			valueCharLengths[i] = Integer.parseInt(lens);
			valueByteLengths[i] = FixedSizeTextFactory.getMaximumTextLength(valueCharLengths[i]);
			recordLength += valueByteLengths[i];
			i++;
		}
		} else {
			valueByteLengths = new int[0];
			valueCharLengths = new int[0];
		}
		//5. (long[]) length (numDocs+1) - offsets in file
		final int length = index.getIntIndexProperty("index."+structureName+".entries", 0);
		
		String indexFilename = path+ApplicationSetup.FILE_SEPARATOR+prefix+"."+structureName+".idx";
		String dataFilename = path+ApplicationSetup.FILE_SEPARATOR+prefix+"."+structureName+".zdata";
		String indexSource = index.getIndexProperty("index."+structureName + ".index-source", "fileinmem");
		long indexFileLength = Files.length(indexFilename);
		long dataFileLength = Files.length(dataFilename);
		
		if (indexSource.equals("fileinmem"))
		{
			logger.info("Structure "+ structureName + " reading lookup file into memory");
			if (indexFileLength < Integer.MAX_VALUE)
			{	
				try{
					DataInputStream dis = new DataInputStream(Files.openFileStream(indexFilename));
					final long[] docid2offsets = new long[length];
					for(i=0;i<length;i++)
						docid2offsets[i] = dis.readLong();
					logger.debug("docid2offsets.length: " + docid2offsets.length + " ZIP_COMPRESSION_LEVEL: " + compressionLevel + " recordLength: " + recordLength);
					offsetLookup = new ArrayDocid2OffsetLookup(docid2offsets, dataFileLength);
					//finished with index file
					dis.close();
				} catch (OutOfMemoryError oome) {
					logger.warn("OutOfMemoryError: Structure "+ structureName + " reading lookup file directly from disk");
					//logger.debug("Metadata lookup will be read directly from disk: "+ length +" entries, size "+ dataFileLength + " bytes");
					RandomDataInput rfi = Files.openFileRandom(indexFilename);
					offsetLookup = new OnDiskDocid2OffsetLookup(
						rfi instanceof RandomAccessFile
							? new ChannelByteAccessor((RandomAccessFile)rfi)
							: new RandomDataInputAccessor(rfi),
						length, dataFileLength
						);
				}
			}
			else
			{
				try{
					DataInputStream dis = new DataInputStream(Files.openFileStream(indexFilename));
					offsetLookup = new OnDiskDocid2OffsetLookup(new RandomDataInputAccessor(new RandomDataInputMemory(dis, indexFileLength)),length, dataFileLength);
					dis.close();
				}
				catch (OutOfMemoryError oome) {
					logger.warn("OutOfMemoryError: Structure "+ structureName + " reading lookup file directly from disk");
					//logger.debug("Metadata lookup will be read directly from disk: "+ length +" entries, size "+ dataFileLength + " bytes");
					RandomDataInput rfi = Files.openFileRandom(indexFilename);
					offsetLookup = new OnDiskDocid2OffsetLookup(
						rfi instanceof RandomAccessFile
							? new ChannelByteAccessor((RandomAccessFile)rfi)
							: new RandomDataInputAccessor(rfi),
						length, dataFileLength
						);
				}
			}	
		} else {
			logger.warn("Structure "+ structureName + " reading lookup file directly from disk (SLOW) - try index."+
					structureName+".index-source=fileinmem in the index properties file");
			//logger.debug("Metadata lookup will be read directly from disk: "+ length +" entries, size "+ dataFileLength + " bytes");
			RandomDataInput rfi = Files.openFileRandom(indexFilename);
			offsetLookup = new OnDiskDocid2OffsetLookup(
				rfi instanceof RandomAccessFile
					? new ChannelByteAccessor((RandomAccessFile)rfi)
					: new RandomDataInputAccessor(rfi),
				length, dataFileLength
				);
		}
		//debug log lookups using a wrapper class
		if (logger.isDebugEnabled())
			offsetLookup = new LoggingDocid2OffsetLookup(offsetLookup);
    	
    
		

		//now build the keyname and lengths into 2 maps:
		// keyname -> length & keyname -> offsets
		keyCount = keyNames.length;
		key2bytelength = new TObjectIntHashMap<String>(keyCount);
		TObjectIntHashMap<String> key2stringlength = new TObjectIntHashMap<String>(keyCount);
		key2byteoffset = new TObjectIntHashMap<String>(keyCount);
		valueByteOffsets = new int[keyCount];
		int cumulativeOffset = 0;
		for(i=0;i<keyCount;i++)
		{
			key2stringlength.put(keyNames[i], valueCharLengths[i]);
			key2bytelength.put(keyNames[i], valueByteLengths[i]);
			key2byteoffset.put(keyNames[i], cumulativeOffset);
			valueByteOffsets[i] = cumulativeOffset;
			cumulativeOffset += valueByteLengths[i];
		}
		
		key2forwardOffset = new TObjectIntHashMap<String>(2);
		final String[] forwardKeys = index.getIndexProperty("index."+structureName+".reverse-key-names", "").split("\\s*,\\s*");
		forwardMetaMaps = (Map<Text,IntWritable>[])new Map[forwardKeys.length];
		keyFactories = (FixedSizeWriteableFactory<Text>[])new FixedSizeWriteableFactory[forwardKeys.length];
		i=0; 
		final FixedSizeIntWritableFactory valueFactory = new FixedSizeIntWritableFactory();
		for(String keyName : forwardKeys)
		{
			if (keyName.trim().equals(""))
				continue;
			key2forwardOffset.put(keyName, 1+i);
			logger.debug("Forward key "+ keyName +", length="+ key2bytelength.get(keyName));
			keyFactories[i] = new FixedSizeTextFactory(key2stringlength.get(keyName));
			String filename = path+ApplicationSetup.FILE_SEPARATOR+prefix+"."+structureName+"-"+i+FSOrderedMapFile.USUAL_EXTENSION;
			String loadFormat = index.getIndexProperty("index."+structureName+".reverse."+keyName+".in-mem", "false");
			if (loadFormat.equals("hashmap"))
			{
				logger.info("Structure "+ structureName + " reading reverse map for key "+ keyName + " into memory as hashmap");
				forwardMetaMaps[i] = new FSOrderedMapFile.MapFileInMemory<Text, IntWritable>(
						filename,
						keyFactories[i], 
						valueFactory);
			}
			else if (loadFormat.equals("mapfileinmem"))
			{
				
				final long revDataFileLength = Files.length(filename);
				//if (revDataFileLength > Integer.MAX_VALUE)
				//{
				//	loadFormat = "false";
				//	logger.info("Structure "+ structureName + " reading reverse map for key "+ keyName + " - too big for memory as bytearray");
				//}
				//else
				//{	
					logger.info("Structure "+ structureName + " reading reverse map for key "+ keyName + " into memory as bytearray");
					DataInputStream dis = new DataInputStream(Files.openFileStream(filename));
					//final byte[] bytes = new byte[(int)revDataFileLength];
					//dis.readFully(bytes);
					//dis.close();				
					forwardMetaMaps[i] = new FSOrderedMapFile<Text, IntWritable>(
							new RandomDataInputMemory(dis, revDataFileLength),
							filename,
							keyFactories[i], 
							valueFactory);
				//}
			}
			
			if (loadFormat.equals("false"))
			{	
				logger.info("Structure "+ structureName + " reading reverse map for key "+ keyName + " directly from disk");
				forwardMetaMaps[i] = new FSOrderedMapFile<Text, IntWritable>(
						filename, 
						false,
						keyFactories[i], 
						valueFactory);
			}
			i++;
		}
	}
	/** 
	 * main
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception
	{
		if (args.length == 0)
		{
			System.err.println("Usage: " + CompressingMetaIndex.class.getName() + " {print|printrange min max|get docid|docno} ");
			return;
		}
		
		//load structures that we actually need
		Index.setIndexLoadingProfileAsRetrieval(false);
		IndexOnDisk index = Index.createIndex();
		if (args[0].equals("print"))
		{
			IndexUtil.printMetaIndex(index, "meta");
		}
		else if (args[0].equals("printrange"))
		{
			Iterator<String[]> inputStream = new InputStream(index, "meta", Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			while(inputStream.hasNext())
			{
				System.out.println(Arrays.toString(inputStream.next()));
			}
			IndexUtil.close(inputStream);
		}
		else if (args[0].equals("get"))
		{
			MetaIndex m = index.getMetaIndex();	
			int docid = Integer.parseInt(args[1]);
			String[] values = m.getAllItems(docid);
			String[] keys = m.getKeys();
			for(int i=0;i<keys.length;i++)
			{
				System.out.println(keys[i] + "=" + values[i]);
			}
		}
		else if (args[0].equals("lookup"))
		{
			MetaIndex m = index.getMetaIndex();			
			int docid = m.getDocument(args[1], args[2]);
			System.out.println(args[1] + " " + args[2] + " -> " + docid);
		}
		else if (args[0].equals("rundocid2docno"))
		{
			final BufferedReader br = args.length > 1 
					? Files.openFileReader(args[1])
					: new BufferedReader(new InputStreamReader(System.in));
			final PrintWriter out = args.length > 2
				? new PrintWriter(Files.writeFileWriter(args[2]))
				: new PrintWriter(System.out);
			
			MetaIndex m = index.getMetaIndex();		
			String line = null;
			while((line = br.readLine()) != null)
			{
				final String[] parts = SPLIT_SPACE.split(line);
				parts[2] = m.getItem("docno", Integer.parseInt(parts[2]));
				out.println(ArrayUtils.join(parts, ' '));
			}
			br.close();
			out.close();
		}
		else if (args[0].equals("rundocno2docid"))
		{
			final BufferedReader br = args.length > 1 
					? Files.openFileReader(args[1])
					: new BufferedReader(new InputStreamReader(System.in));
			final PrintWriter out = args.length > 2
				? new PrintWriter(Files.writeFileWriter(args[2]))
				: new PrintWriter(System.out);
			
			MetaIndex m = index.getMetaIndex();		
			String line = null;
			while((line = br.readLine()) != null)
			{
				final String[] parts = SPLIT_SPACE.split(line);
				parts[2] = String.valueOf(m.getDocument("docno", parts[2]));
				out.println(ArrayUtils.join(parts, ' '));
			}
			br.close();
			out.close();
		}
		else if (args[0].equals("rundocno2docid_seq"))
		{
			final BufferedReader br = args.length > 1 
					? Files.openFileReader(args[1])
					: new BufferedReader(new InputStreamReader(System.in));
			final PrintWriter out = args.length > 2
				? new PrintWriter(Files.writeFileWriter(args[2]))
				: new PrintWriter(System.out);
			
			List<String[]> lines = new ArrayList<String[]>();
			TObjectIntHashMap<String> docnos = new TObjectIntHashMap<String>();
			String line = null;
			while((line = br.readLine()) != null)
			{
				final String[] parts = SPLIT_SPACE.split(line);
				lines.add(parts);
				docnos.put(parts[2], -1);
			}
			@SuppressWarnings("unchecked")
			Iterator<String[]> metaIn = (Iterator<String[]>) index.getIndexStructureInputStream("meta");
			int docid = 0;
			TerrierTimer tt = new TerrierTimer("Reading metaindex", index.getCollectionStatistics().getNumberOfDocuments());
			tt.start();
			try{
				while(metaIn.hasNext())
				{
					String docno = metaIn.next()[0];
					if (docnos.containsKey(docno))
					{
						docnos.put(docno, docid);
					}
					docid++;
					tt.increment();
				}
			}
			finally {
				IndexUtil.close(metaIn);
				tt.finished();
			}
			for(String[] parts : lines)
			{
				parts[2] = String.valueOf(docnos.get(parts[2]));
				out.println(ArrayUtils.join(parts, ' '));
			}
			br.close();
			out.close();
		}
		else
		{
			MetaIndex m = index.getMetaIndex();			
			int docid = m.getDocument("docno", args[0]);
			System.out.println(args[0] + " -> " + docid);
			String value = m.getItem("docno", docid);
			System.out.println(docid + " -> " + value);
			System.out.println("Equals check: " + value.equals(args[0]));
		}
	}
	
}
