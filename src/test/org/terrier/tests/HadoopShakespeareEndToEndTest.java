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
 * The Original Code is HadoopShakespeareEndToEndTest.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.tests;

import gnu.trove.TIntHashSet;

import static org.junit.Assert.*;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;

import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.indexing.singlepass.hadoop.BitPostingIndexInputFormat;
import org.terrier.structures.indexing.singlepass.hadoop.Inv2DirectMultiReduce;
import org.terrier.structures.merging.StructureMerger;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.tests.BatchEndToEndTest.BatchEndToEndTestEventHooks;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Wrapper.IntObjectWrapper;
import org.terrier.utility.io.HadoopPlugin;
import org.terrier.utility.io.HadoopUtility;
@SuppressWarnings("deprecation")
public class HadoopShakespeareEndToEndTest 
{
	static class CheckDirectHSplits extends BatchEndToEndTestEventHooks
	{
		@Override
		public boolean validPlatform() {
			if (System.getProperty("os.name").toLowerCase().contains("windows"))
				return false;
			return super.validPlatform();
		}


		@Override
		public void checkIndex(BatchEndToEndTest test, Index index) throws Exception {
			//check thet using the hadoop oriented splitting of the direct index works as expected
			checkDirectIndexHSplits(index, 
					index.getCollectionStatistics().getNumberOfUniqueTerms(), 
					index.getCollectionStatistics().getNumberOfUniqueTerms(), 
					ShakespeareEndToEndTest.DOCUMENT_LENGTHS,
					ShakespeareEndToEndTest.DOCUMENT_UNIQUE_TERMS);
			//TODO: can we do a similar test for the inverted index?
		}
		
		
		void checkDirectIndexHSplits(Index index, int maxTermId, int numberOfTerms, int documentLengths[], int[] documentPointers)
			throws Exception
		{
			BitPostingIndexInputFormat informat = new BitPostingIndexInputFormat();
			JobConf jc = HadoopPlugin.getJobFactory("testSplits").newJob();
			HadoopUtility.toHConfiguration(index, jc);
			BitPostingIndexInputFormat.setStructures(jc, "direct", "document");
			InputSplit[] splits = informat.getSplits(jc, 2);
			
			TIntHashSet termIds = new TIntHashSet();
			
			long tokens = 0;
			long pointers = 0;
			int docid = 0;
			
			for(InputSplit split : splits)
			{
				RecordReader<IntWritable, IntObjectWrapper<IterablePosting>> rr = informat.getRecordReader(split, jc, null);
				IntWritable key = rr.createKey();
				IntObjectWrapper<IterablePosting> value = rr.createValue();
				while(rr.next(key, value))
				{
					docid = key.get();
					int doclen = 0;	int docpointers = 0;
					IterablePosting ip = value.getObject();
					assertEquals("Number of pointers for docid " + docid + " is incorrect", documentPointers[docid], value.getInt());
					while(ip.next() != IterablePosting.EOL)
					{
						//System.err.println("termid" +ip.getId() + " f=" + ip.getFrequency());
						termIds.add(ip.getId());
						tokens += ip.getFrequency();
						doclen += ip.getFrequency();
						pointers++; docpointers++;
						if (numberOfTerms > 0)
							assertTrue("Got too big a termid ("+ip.getId()+") from direct index input stream, numTerms=" + numberOfTerms, ip.getId() < maxTermId);
					}
					if (documentPointers.length > 0)
						assertEquals("Number of pointers for docid " + docid + " is incorrect", documentPointers[docid], docpointers);
					assertEquals("Document length for docid "+docid+" is incorrect", documentLengths[docid], doclen);
				}
				rr.close();
			}
			CollectionStatistics cs = index.getCollectionStatistics();
			assertEquals("Number of documents is incorrect", cs.getNumberOfDocuments(), docid + 1);
			assertEquals("Number of pointers is incorrect", cs.getNumberOfPointers(), pointers);
			assertEquals("Number of tokens is incorrect", cs.getNumberOfTokens(), tokens);
			if (numberOfTerms > 0)
			{
				assertEquals("Not all termIds found in direct index", termIds.size(), numberOfTerms);
			}
		}
	}
	
	static public class BasicHadoopShakespeareEndToEndTest extends BasicShakespeareEndToEndTest
	{		
		public BasicHadoopShakespeareEndToEndTest()
		{
			super.indexingOptions.add("-H");
			super.indexingOptions.add("-Dterrier.hadoop.indexing.reducers=1");
			super.testHooks.add(new CheckDirectHSplits());
		}
		
		@Override
		protected void addDirectStructure(IndexOnDisk index) throws Exception {
			Inv2DirectMultiReduce.invertStructure(index, HadoopPlugin.getJobFactory("inv2direct"), 1);
		}
	}
	
	static public class BasicHadoopShakespeareEndToEndTestMultiReduce extends BasicShakespeareEndToEndTest
	{
		public BasicHadoopShakespeareEndToEndTestMultiReduce()
		{
			indexingOptions.add("-H");
			indexingOptions.add("-Dterrier.hadoop.indexing.reducers=2");
			super.testHooks.add(new CheckDirectHSplits());
		}
		
		@Override
		protected void addDirectStructure(IndexOnDisk index) throws Exception {
			Inv2DirectMultiReduce.invertStructure(index, HadoopPlugin.getJobFactory("inv2direct"), 1);
		}

		@Override
		protected void finishIndexing() throws Exception {
			IndexOnDisk i1 = Index.createIndex(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX + "-0");
			IndexOnDisk i2 = Index.createIndex(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX + "-0");
			IndexOnDisk dest = Index.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
			new StructureMerger(i1, i2, dest).mergeStructures();
		}		
	}
	
	/** Forces indexing with very little memory - i.e. may flushes to be merged */
	static public class BasicHadoopShakespeareEndToEndTestLowMem extends BasicHadoopShakespeareEndToEndTest
	{
		public BasicHadoopShakespeareEndToEndTestLowMem()
		{
			indexingOptions.add("-Dindexing.singlepass.max.postings.memory=200");
			super.indexingOptions.add("-Dterrier.hadoop.indexing.reducers=1");
			indexingOptions.add("-Ddocs.check=5");
			super.testHooks.add(new CheckDirectHSplits());
		}
		
		@Override
		protected void addDirectStructure(IndexOnDisk index) throws Exception {
			Inv2DirectMultiReduce.invertStructure(index, HadoopPlugin.getJobFactory("inv2direct"), 1);
		}
	}
	
	
	static public class BlockHadoopShakespeareEndToEndTest extends BlockShakespeareEndToEndTest
	{
		public BlockHadoopShakespeareEndToEndTest()
		{
			indexingOptions.add("-H");
			super.indexingOptions.add("-Dterrier.hadoop.indexing.reducers=1");
			super.testHooks.add(new CheckDirectHSplits());
		}
		
		@Override
		protected void addDirectStructure(IndexOnDisk index) throws Exception {
			Inv2DirectMultiReduce.invertStructure(index, HadoopPlugin.getJobFactory("inv2direct"), 1);
		}
	}
	
	/** Forces block indexing with very little memory - i.e. may flushes to be merged */
	static public class BlockHadoopShakespeareEndToEndTestLowMem extends BlockHadoopShakespeareEndToEndTest
	{
		public BlockHadoopShakespeareEndToEndTestLowMem()
		{
			indexingOptions.add("-Dindexing.singlepass.max.postings.memory=200");
			super.indexingOptions.add("-Dterrier.hadoop.indexing.reducers=1");
			indexingOptions.add("-Ddocs.check=5");
			super.testHooks.add(new CheckDirectHSplits());
		}
		
		@Override
		protected void addDirectStructure(IndexOnDisk index) throws Exception {
			Inv2DirectMultiReduce.invertStructure(index, HadoopPlugin.getJobFactory("inv2direct"), 1);
		}
	}
}
