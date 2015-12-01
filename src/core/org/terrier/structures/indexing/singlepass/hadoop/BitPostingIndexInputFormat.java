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
 * The Original Code is BitPostingIndexInputFormat.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.indexing.singlepass.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.bit.BitPostingIndexInputStream;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.Wrapper.IntObjectWrapper;
import org.terrier.utility.io.HadoopPlugin;
import org.terrier.utility.io.HadoopUtility;

/** An InputFormat, i.e. MapReduce input reader, for a BitPostingIndex. Splits the main posting
 * file into generic InputSplits, according to the block size of the underlying file - i.e. the 
 * number of entries, or indeed postings, can be variable. 
 * The following JobConf properties are used:
 * <ul>
 * <li><tt>mapred.index.path</tt> and <tt>mapred.index.prefix</tt> - where to find the index.</li>
 * <li><tt>mapred.bitpostingindex.structure</tt> - which structure are we splitting?</li>
 * <li><tt>mapred.bitpostingindex.lookup.structure</tt> - which structure's inputstream is the Iterator of BitIndexPointers?</li>
 * </ul>
 */
@SuppressWarnings("deprecation")
public class BitPostingIndexInputFormat extends FileInputFormat<IntWritable, IntObjectWrapper<IterablePosting>> {

	final static Logger logger = LoggerFactory.getLogger(BitPostingIndexInputFormat.class);
	final static String BITPOSTING_STRUCTURE_KEY = "mapred.bitpostingindex.structure";
	final static String BITPOSTING_LOOKUP_STRUCTURE_KEY = "mapred.bitpostingindex.lookup.structure";
	
	final static boolean REPLACE_DOCUMENT_INDEX = true;
	
	static class NullDocumentIndex implements DocumentIndex
	{
		int docs;
		public NullDocumentIndex(int numDocs)
		{
			this.docs = numDocs;
		}
		
		@Override
		public DocumentIndexEntry getDocumentEntry(int docid)
				throws IOException {
			return null;
		}

		@Override
		public int getDocumentLength(int docid) throws IOException {
			return 0;
		}

		@Override
		public int getNumberOfDocuments() {
			return docs;
		}
		
	}
	
	static class BitPostingIndexInputSplit extends FileSplit
	{
		/** start entry of split */
		int startingEntryIndex;
		/** number of entries in split */
		int entryCount;
		
		/** Constructor for a split of a BitPosting structures, 
		 * where the start and number of entries are specified */
		public BitPostingIndexInputSplit(
				Path file, 
				long start, long length,
				String[] hosts, int _startingEntryIndex, int _entryCount) {
			super(file, start, length, hosts);			
			startingEntryIndex = _startingEntryIndex;
			entryCount = _entryCount;
			logger.debug("new BitPostingIndexInputSplit: start at " + startingEntryIndex + " entries "+ _entryCount );
		}
		
		/** default constructor, for serialization */
		public BitPostingIndexInputSplit()
		{
			super(null, (long)0, (long)0, new String[0]);
		}
		
		/** Start entry of the split */
		public int getStartingEntryIndex()
		{
			return startingEntryIndex;
		}
	
		/** Number of entries in split */
		public int getEntryCount()
		{
			return entryCount;
		}

		@Override
		public String toString() {
			return super.toString() +", " + entryCount + " entries starting at "+ startingEntryIndex;
		}

		@Override
		public void readFields(DataInput in) throws IOException {
			super.readFields(in);
			startingEntryIndex = WritableUtils.readVInt(in);
			entryCount = WritableUtils.readVInt(in);
		}

		@Override
		public void write(DataOutput out) throws IOException {
			super.write(out);
			WritableUtils.writeVInt(out, startingEntryIndex);
			WritableUtils.writeVInt(out, entryCount);
		}		
	}
	
	static class BitPostingIndexRecordReader implements RecordReader<IntWritable, IntObjectWrapper<IterablePosting>>
	{
		/** id of first entry */
		int startingEntryIndex;
		/** id of entry we're currently at */
		int entryIndex = 0;
		/** number of entries in our split */
		int entryCount = 0;
		/** actual posting stream */
		BitPostingIndexInputStream postingStream;
		
		BitPostingIndexRecordReader(BitPostingIndexInputStream _postingStream, int _entryIndex, int _entryCount)
		{
			this.postingStream = _postingStream;
			this.startingEntryIndex = this.entryIndex = _entryIndex;
			this.entryCount = _entryCount;
			logger.info("new BitPostingIndexRecordReader: start at index " + entryIndex + " process "+ _entryCount + " entries" );
		}		
		
		public void close() throws IOException {
			this.postingStream.close();
			logger.info("BitPostingIndexRecordReader: closing: started at "+startingEntryIndex +" now, at " + entryIndex );
		}

		public IntWritable createKey() {
			return new IntWritable();
		}

		public IntObjectWrapper<IterablePosting> createValue() {
			return new IntObjectWrapper<IterablePosting>();
		}

		public long getPos() throws IOException {
			return postingStream.getPos().getOffset();
		}

		public float getProgress() throws IOException {
			/* TODO: could we calculate progress in terms of bytes of the target structure, as this
			 * would be more accurate than entries */
			//progress can be greater than 1, because of trailing empty entries
			final float progress = (float)(entryIndex - startingEntryIndex)/(float)entryCount;
			return progress > 1.0f ? 1.0f : progress;
		}

		public boolean next(IntWritable docid, IntObjectWrapper<IterablePosting> wrapperPostingList)
				throws IOException 
		{
			//check if entryCount entries have been read
			//count can be greater than entry count due to entry skipping
			if ((entryIndex - startingEntryIndex) >= entryCount )
				return false;
			if (! postingStream.hasNext())
				return false;
			IterablePosting rtr = postingStream.next();
			
			//System.err.println("skipped=" + postingStream.getEntriesSkipped());
			entryIndex += postingStream.getEntriesSkipped();
			
			if (rtr == null)
			{
				entryIndex++;
				//this entry should be trailing
				logger.warn("No posting list for trailing entry " + entryIndex);
				return next(docid, wrapperPostingList); //TODO recursion is BAD
			}
			docid.set(entryIndex++);
			wrapperPostingList.setObject(rtr);
			wrapperPostingList.setInt(postingStream.getNumberOfCurrentPostings());
			return true;
		}
	}

	/** Get a record reader for the specified split */
	public RecordReader<IntWritable, IntObjectWrapper<IterablePosting>> getRecordReader(
				final InputSplit _split, final JobConf job, final Reporter reporter) 
			throws IOException 
	{
		HadoopUtility.loadTerrierJob(job);
		final BitPostingIndexInputSplit split = (BitPostingIndexInputSplit)_split;
		Index.setIndexLoadingProfileAsRetrieval(false);
		final IndexOnDisk index = HadoopUtility.fromHConfiguration(job);
		if (index == null)
			throw new IOException("Index not found in JobConf:" + Index.getLastIndexLoadError());
		if (REPLACE_DOCUMENT_INDEX)
			IndexUtil.forceStructure(index, "document", new NullDocumentIndex(index.getCollectionStatistics().getNumberOfDocuments()));
		final String bitPostingStructureName = job.get(BITPOSTING_STRUCTURE_KEY);
		
		final BitPostingIndexInputStream postingStream = (BitPostingIndexInputStream)index.getIndexStructureInputStream(bitPostingStructureName);
		postingStream.skip(split.getStartingEntryIndex());
		logger.info("BitPostingIndexRecordReader for structure "+ bitPostingStructureName + " start entry "+ split.getStartingEntryIndex() + " split size " + split.getEntryCount());
		return new BitPostingIndexRecordReader(postingStream, split.getStartingEntryIndex(), split.getEntryCount());
	}
	
	/** Returns the block size of the specified file. Only recommended to overload for testing */
	protected long getBlockSize(Path path, FileStatus fss)
	{
		return fss.getBlockSize();
	}
	/** 
	 * {@inheritDoc} 
	 */
	@SuppressWarnings("unchecked")
	/** Make the splits of the index structure. Bit structures split across multiple files are supported */
	public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
		HadoopUtility.loadTerrierJob(job);
		
		final String lookupStructureName = job.get(BITPOSTING_LOOKUP_STRUCTURE_KEY);
		final String bitPostingStructureName = job.get(BITPOSTING_STRUCTURE_KEY);
		Index.setIndexLoadingProfileAsRetrieval(false);
		final IndexOnDisk index = HadoopUtility.fromHConfiguration(job);		
		
		final byte fileCount = Byte.parseByte(index.getIndexProperty("index." + bitPostingStructureName + ".data-files", "1"));
		final Path bitPostingStructureFiles[] = new Path[fileCount];
		final FileStatus[] fss = new FileStatus[fileCount];
		final long[] bitPostingStructureFSBlockSizes = new long[fileCount];
		
		logger.info("Calculating splits of structure " + bitPostingStructureName);
		FileSystem fs = FileSystem.get(job);
		for(byte i=0;i<fileCount;i++)
		{
			bitPostingStructureFiles[i] = new Path(BitPostingIndexInputStream.getFilename(index, bitPostingStructureName, fileCount, i));
			fss[i] = fs.getFileStatus(bitPostingStructureFiles[i]);
			bitPostingStructureFSBlockSizes[i] = getBlockSize(bitPostingStructureFiles[i], fss[i]);
			logger.info("File " + i + " approx splits=" + ((double)fss[i].getLen() /(double)bitPostingStructureFSBlockSizes[i]));
		}
		
		//this smells of a hack, because we dont have a strategy for naming various index structures streams
		final Iterator<? extends BitIndexPointer> offsetIterator = 
			index.hasIndexStructureInputStream(lookupStructureName+ "-entry")
				? (Iterator<? extends BitIndexPointer>)index.getIndexStructureInputStream(lookupStructureName+ "-entry")
				: (Iterator<? extends BitIndexPointer>)index.getIndexStructureInputStream(lookupStructureName);

		if (offsetIterator == null)
			throw new IOException("No such stream structure called " + lookupStructureName+ "-entry or "+lookupStructureName+" found in index");
		final List<InputSplit> splitList = new ArrayList<InputSplit>();
		
		int currentId = 0;
		
		//size of the current split of each file
		final long[] blockSize = new long[fileCount];
		//location of the last split for each file
		final long[] bitPostingStructureSplitEndOffsets = new long[fileCount];
		
		//how many entries will be in this split, for each file
		final int[] entriesInBlock = new int[fileCount];
		//what is the starting id of the next entry split, for each file
		final int[] firstEntryOfNextSplit = new int[fileCount];
		
		//number of splits per file, for logging only
		final int[] splitsPerFile = new int[fileCount];
		
		Arrays.fill(firstEntryOfNextSplit, Integer.MAX_VALUE);

		BitIndexPointer currentPointer = null;
		//iterate through the lookup iterator
		//split the target bit posting index structure into chunks of size bitPostingStructureFSBlockSize
		while(offsetIterator.hasNext())
		{			
			//ok, where is the next pointer to
			currentPointer = offsetIterator.next();
			final byte fileId = currentPointer.getFileNumber();
			
			//what is the first entry of the next split of this file?
			firstEntryOfNextSplit[fileId] = Math.min(currentId, firstEntryOfNextSplit[fileId]);
			//this split will have one more entry
			entriesInBlock[fileId]++;
			
			//what is our current offset?
			long offset = currentPointer.getOffset();
			//System.err.println("Offset" + offset);
			//if we made the split here, how big would it be?
			blockSize[fileId] = offset - bitPostingStructureSplitEndOffsets[fileId];
			//is this block is large enough
			if (blockSize[fileId] > bitPostingStructureFSBlockSizes[fileId])
			{
				//yes, its big enough
				//block will be from bitPostingStructureSplitEndOffsets[fileId] to offset, which is blockSize[fileId]
				BlockLocation[] blkLocations = fs.getFileBlockLocations(
					fss[fileId], 
					bitPostingStructureSplitEndOffsets[fileId], 
					blockSize[fileId]);
				splitList.add(
					new BitPostingIndexInputSplit(
						bitPostingStructureFiles[fileId],  //path
						bitPostingStructureSplitEndOffsets[fileId],  //start
						blockSize[fileId],  //length
						blkLocations[0].getHosts(), //hosts
						firstEntryOfNextSplit[fileId], //first entry in this split
						entriesInBlock[fileId]) //number of entries in this split
					);
				logger.info("File "+ fileId + " split " +(splitList.size()-1)
					+ " "+ splitList.get(splitList.size() -1).toString());
				//record another split for this file (for logging only)
				splitsPerFile[fileId]++;
				//update recording of last offset for this file
				bitPostingStructureSplitEndOffsets[fileId] = offset;
				//reset size of split for this file
				blockSize[fileId] = 0; 
				//reset counter of entries in split of this file
				entriesInBlock[fileId] = 0;
				//reset the first offset of this split
				firstEntryOfNextSplit[fileId] = Integer.MAX_VALUE;
			}
			
			//ids always increment
			currentId++;
		}
		IndexUtil.close(offsetIterator);
		//find any files which have trailing blocks
		for(byte fileId=0;fileId<fileCount;fileId++)
		{
			if (entriesInBlock[fileId] == 0)
				continue;
			assert(firstEntryOfNextSplit[fileId] != Integer.MAX_VALUE);
			
			//block will be from bitPostingStructureSplitEndOffsets[fileId], with length blockSize[fileId]
			BlockLocation[] blkLocations = fs.getFileBlockLocations(fss[fileId], bitPostingStructureSplitEndOffsets[fileId], blockSize[fileId]);
			splitList.add(
					new BitPostingIndexInputSplit(
						bitPostingStructureFiles[fileId], //path of file for split
						bitPostingStructureSplitEndOffsets[fileId], //start offset of this split
						blockSize[fileId], //size of this split
						blkLocations[0].getHosts(), //hosts for this split
						firstEntryOfNextSplit[fileId], //first entry id for this split
						entriesInBlock[fileId]) //number of entries in this split
					);
			logger.info("File "+ fileId + " trailing split "+ (splitList.size() -1) 
				+ " " + splitList.get(splitList.size() -1).toString());

			//record another split for this file (for logging only)
			splitsPerFile[fileId]++;
		}

		logger.info("Split "+ bitPostingStructureName+ " (of "+currentId+" entries) into " + splitList.size() + " splits");
		if (fileCount > 1)
		{
			logger.info("Multiple files of " + bitPostingStructureName + " were split as follows: " + ArrayUtils.join(splitsPerFile, ","));
		}
		assert(splitList.size() > 0);
		index.close();
		return splitList.toArray(new InputSplit[splitList.size()]);
	}
	
	/** Checks to see if required keys are present */
	public void validateInput(JobConf job) throws IOException {
		for (String k : new String[]{BITPOSTING_LOOKUP_STRUCTURE_KEY, BITPOSTING_STRUCTURE_KEY})
		{
			if (job.get(k, null) == null) 
				throw new IOException("Required key "+ k + " not defined in job");
		}
	}
	
	/** Provides the starting entry id for the specified split */
	public static int getSplit_StartingEntryIndex(InputSplit s)
	{
		return ((BitPostingIndexInputSplit)s).getStartingEntryIndex();
	}
	
	/** Returns the number of entries in specified split */
	public static int getSplit_EntryCount(InputSplit s)
	{
		return ((BitPostingIndexInputSplit)s).getEntryCount();
	}
	
	/** Save in the JobConf, the names of the bit and pointer lookup structures that this inputformat should look for */
	public static void setStructures(JobConf jc, String bitStructureName, String lookupStructureName)
	{
		jc.setInputFormat(BitPostingIndexInputFormat.class);
		jc.set(BITPOSTING_STRUCTURE_KEY, bitStructureName);
		jc.set(BITPOSTING_LOOKUP_STRUCTURE_KEY, lookupStructureName);
	}

	
	
	/** Test method, runs splits for inverted/lexicon with the command line specified index */
	public static void main(String[] args) throws Exception
	{
		Index.setIndexLoadingProfileAsRetrieval(false);
		IndexOnDisk index = Index.createIndex(args[1], args[2]);
		if (args[0].equals("--splits"))
		{
			JobConf job = HadoopPlugin.getJobFactory(BitPostingIndexInputFormat.class.getSimpleName()).newJob();
			HadoopUtility.toHConfiguration(index, job);
			setStructures(job, "inverted", "lexicon");
			index.close();
			new BitPostingIndexInputFormat().getSplits(job, 100);
		}
		else
		{
			JobConf job = HadoopPlugin.getJobFactory(BitPostingIndexInputFormat.class.getSimpleName()).newJob();
			setStructures(job, "linksin", "linksin-lookup");
			HadoopUtility.toHConfiguration(index, job);
			index.close();
			InputSplit s = new BitPostingIndexInputSplit(
					new Path(args[3]), Long.parseLong(args[4]), Long.parseLong(args[5]), 
					new String[0], Integer.parseInt(args[6]), Integer.parseInt(args[7]));
			RecordReader<IntWritable, IntObjectWrapper<IterablePosting>> rr = new BitPostingIndexInputFormat().getRecordReader(s, job, new Reporter(){
				public InputSplit getInputSplit() throws UnsupportedOperationException {return null;}
				@SuppressWarnings({ "rawtypes" })
				public void incrCounter(Enum arg0, long arg1) {}
				public void incrCounter(String arg0, String arg1, long arg2) {}
				@SuppressWarnings({ "rawtypes" })
				public org.apache.hadoop.mapred.Counters.Counter getCounter(Enum arg0) {return null;}
				public org.apache.hadoop.mapred.Counters.Counter getCounter(String arg0, String arg1) {return null;}
				public void setStatus(String arg0) {}
				public void progress() {}}
			);
			IntWritable key = rr.createKey();
			IntObjectWrapper<IterablePosting> value = rr.createValue();
			long pointers = 0;
			int lastId = 0;
			int nonZeroEntryCount = 0;
			float maxProgress = 0;
			while(rr.next(key, value))
			{
				IterablePosting ip = value.getObject();
				lastId = key.get();
				while(ip.next() != IterablePosting.EOL)
				{
					pointers++;
				}
				nonZeroEntryCount++;
				if (rr.getProgress() > maxProgress)
					maxProgress = rr.getProgress();
			}
			rr.close();
			System.out.println("maxProgress="+maxProgress+" Lastid=" + lastId + " nonZeroEntryCount="+nonZeroEntryCount + " postings="+ pointers);
		}
	}

}
