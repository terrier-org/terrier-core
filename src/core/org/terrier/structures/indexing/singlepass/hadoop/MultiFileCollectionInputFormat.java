/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.uk
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
 * The Original Code is MultiFileCollectionInputFormat.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richardm{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> 
 */
package org.terrier.structures.indexing.singlepass.hadoop;

import gnu.trove.TObjectLongProcedure;
import gnu.trove.TObjectLongHashMap;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MultiFileInputFormat;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.lib.CombineFileSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.indexing.Document;

/**
 * Input Format Class for Hadoop Indexing. Splits the input collection into
 * sets of files where each Map task gets about the same number of files.
 * Files are assumed to be un-splittable and are not split. Splits are of
 * adjacent files - i.e. split 0 always has the first file, and the last
 * split always has the last file. Any given split will have adjacent files.
 * @author Richard McCreadie and Craig Macdonald
 * @since 2.2
 */
@SuppressWarnings("deprecation")
public class MultiFileCollectionInputFormat extends MultiFileInputFormat<Text, SplitAwareWrapper<Document>>
{

	/** logger for this class */
	protected static final Logger logger = LoggerFactory.getLogger(MultiFileCollectionInputFormat.class);
	
	@SuppressWarnings("unchecked")
	@Override
	/**
	 * Instantiates a FileCollectionRecordReader using the specified spit (which is
	 * assumed to be a CombineFileSplit.
	 * @param genericSplit contains files to be processed, assumed to be a CombineFileSplit
	 * @param job JobConf of this job
	 * @param reported To report progress
	 */
	public RecordReader<Text, SplitAwareWrapper<Document>> getRecordReader(
			InputSplit genericSplit, 
			JobConf job,
            Reporter reporter) 
		throws IOException 
	{
		reporter.setStatus(genericSplit.toString());
	    return new FileCollectionRecordReader(job, (PositionAwareSplit<CombineFileSplit>) genericSplit);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	/**
	 * Splits the input collection into
	 * sets of files where each Map task 
	 * gets about the same number of files
	 */
	public InputSplit[] getSplits(JobConf job, int numSplits) 
		throws IOException 
	{

		Path[] paths = FileInputFormat.getInputPaths(job);
		// HADOOP-1818: Manage splits only if there are paths
		if (paths.length == 0) 
		{
            return new InputSplit[0];
        }

		

		if (numSplits>paths.length) 
		{
			numSplits = paths.length;
		} 
		else if (numSplits<1) 
		{
			numSplits = 1;
		}
		logger.info("Allocating "+paths.length+ " files across "+numSplits +" map tasks");
		List<PositionAwareSplit<CombineFileSplit>> splits = new ArrayList<PositionAwareSplit<CombineFileSplit>>(numSplits);
		final int numPaths = paths.length;
		long[] lengths = new long[numPaths];
		TObjectLongHashMap<String>[] locations = (TObjectLongHashMap<String>[])Array.newInstance(TObjectLongHashMap.class, numPaths);
		final FileSystem fs = FileSystem.get(job);
		for(int i=0; i<paths.length; i++) 
		{
			final FileStatus fss = fs.getFileStatus(paths[i]);
			lengths[i] = fss.getLen();
			final TObjectLongHashMap<String> location2size = locations[i] = new TObjectLongHashMap<String>();
			final long normalblocksize = fss.getBlockSize();
			for(long offset = 0; offset < lengths[i]; offset += normalblocksize)
			{
				final long blocksize = Math.min(offset + normalblocksize, lengths[i]);
				final BlockLocation[] blockLocations = fs.getFileBlockLocations(fss, offset, blocksize);
				for(BlockLocation bl : blockLocations)
				{
					for (String host : bl.getHosts())
					{
						location2size.adjustOrPutValue(host, blocksize, blocksize);
					}
				}
			}
		}
		
		//we need to over-estimate using ceil, to ensure that the last split is not /too/ big
		final int numberOfFilesPerSplit = (int)Math.ceil((double)paths.length / (double)numSplits);
		
		int pathsUsed = 0;
		int splitnum = 0;
		CombineFileSplit mfs;
		// for each split except the last one (which may be smaller than numberOfFilesPerSplit)
		while(pathsUsed < numPaths)
		{
			/* caclulate split size for this task - usually numberOfFilesPerSplit, but
			 * less than this for the last split */
			final int splitSizeForThisSplit = numberOfFilesPerSplit + pathsUsed > numPaths
				? numPaths - pathsUsed
				: numberOfFilesPerSplit;
			//arrays of information for split
			Path[] splitPaths = new Path[splitSizeForThisSplit];
			long[] splitLengths = new long[splitSizeForThisSplit];
			long[] splitStarts = new long[splitSizeForThisSplit];
			final TObjectLongHashMap<String> allLocationsForSplit = new TObjectLongHashMap<String>();
			String[] splitLocations = null; //final recommended locations for this split.
			for(int i=0;i<splitSizeForThisSplit;i++)
			{
				locations[pathsUsed+i].forEachEntry(new  TObjectLongProcedure<String>() {
					public boolean execute(String a, long b)
					{
						allLocationsForSplit.adjustOrPutValue(a, b, b); return true;
					}
				});
				if ( allLocationsForSplit.size() <=3 )
				{
					splitLocations = allLocationsForSplit.keys(new String[allLocationsForSplit.size()]);
				}
				else
				{
					String[] hosts = allLocationsForSplit.keys(new String[allLocationsForSplit.size()]);
					 Arrays.sort(hosts, new Comparator<String>() {
                        public int  compare(String o1, String o2) {
                            long diffamount = allLocationsForSplit.get(o1) - allLocationsForSplit.get(o2);
                            if (diffamount > 0)
                            {
                                return -1;
                            }
                            else if (diffamount < 0)
                            {
                                return 1;
                            }
                            return 0;
                        }
                    });
                    splitLocations = new String[3];
                    System.arraycopy(hosts, 0, splitLocations, 0, 3);
				}
			}
			
			
			//copy information for this split
			System.arraycopy(lengths, pathsUsed, splitLengths, 0, splitSizeForThisSplit);
			System.arraycopy(paths, pathsUsed, splitPaths, 0, splitSizeForThisSplit);
			//count the number of paths consumed
			pathsUsed += splitSizeForThisSplit;
			
			//make the actual split object
			//logger.info("New split of size " + splitSizeForThisSplit);
			mfs = new CombineFileSplit(job, splitPaths, splitStarts, splitLengths, splitLocations);
			splits.add(new PositionAwareSplit<CombineFileSplit>(mfs, splitnum));
			splitnum++;
		}

		if (!(pathsUsed==paths.length)) {
			throw new IOException("Number of used paths does not equal total available paths!");
		}
		return splits.toArray(new PositionAwareSplit[splits.size()]);    
	}

}
