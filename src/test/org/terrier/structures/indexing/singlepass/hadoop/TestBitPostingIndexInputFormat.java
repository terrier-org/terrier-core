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
 * The Original Code is TestBitPostingIndexInputFormat.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.indexing.singlepass.hadoop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.Counters.Counter;
import org.junit.Test;
import org.terrier.structures.BasicDocumentIndexEntry;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.bit.DirectInvertedDocidOnlyOuptutStream;
import org.terrier.structures.indexing.DocumentIndexBuilder;
import org.terrier.structures.postings.ArrayOfIdsIterablePosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.bit.BasicIterablePostingDocidOnly;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.FieldScore;
import org.terrier.utility.StaTools;
import org.terrier.utility.Wrapper.IntObjectWrapper;
import org.terrier.utility.io.HadoopUtility;
@SuppressWarnings("deprecation")
public class TestBitPostingIndexInputFormat extends ApplicationSetupBasedTest {

	static boolean validPlatform()
    {
        String osname = System.getProperty("os.name");
        if (osname.contains("Windows"))
            return false;
        return true;
    }

	protected static final Reporter NULL_REPORTER = new Reporter(){
		@Override
		public Counter getCounter(Enum<?> arg0) {
			return null;
		}
		@Override
		public Counter getCounter(String arg0, String arg1) {
			return null;
		}
		@Override
		public InputSplit getInputSplit() throws UnsupportedOperationException {
			return null;
		}
		@Override
		public void incrCounter(Enum<?> arg0, long arg1) {			
		}
		@Override
		public void incrCounter(String arg0, String arg1, long arg2) {
		}
		@Override
		public void setStatus(String arg0) {
		}
		@Override
		public void progress() {
		}		
	};
	
	protected Index writeIndexStructure(int[][] postings) throws Exception
	{
		String path = ApplicationSetup.TERRIER_INDEX_PATH;
		String prefix = ApplicationSetup.TERRIER_INDEX_PREFIX;

		IndexOnDisk index = Index.createNewIndex(path, prefix);
		DirectInvertedDocidOnlyOuptutStream dios = new DirectInvertedDocidOnlyOuptutStream(path + '/'+ prefix + ".direct.bf");
		//FSArrayFile<BitIndexPointer> 
		DocumentIndexBuilder dib = new DocumentIndexBuilder(index, "document");
		BitIndexPointer p;
		for(int[] list : postings)
		{
			final int doclen = StaTools.sum(list);
			p = dios.writePostings(new ArrayOfIdsIterablePosting(list));
			DocumentIndexEntry die = new BasicDocumentIndexEntry(doclen, p);
			dib.addEntryToBuffer(die);
		}
		dios.close();
		dib.finishedCollections();
		index.addIndexStructure(
				"direct", 
				"org.terrier.structures.bit.BitPostingIndex", 
				"org.terrier.structures.IndexOnDisk,java.lang.String,java.lang.Class", 
				"index,structureName,"+ BasicIterablePostingDocidOnly.class.getName());
		index.addIndexStructureInputStream(
				"direct",
				"org.terrier.structures.bit.BitPostingIndexInputStream", 
				"org.terrier.structures.IndexOnDisk,java.lang.String,java.util.Iterator,java.lang.Class",
				"index,structureName,document-inputstream,"+ BasicIterablePostingDocidOnly.class.getName());
		index.setIndexProperty("index.direct.fields.count", ""+FieldScore.FIELDS_COUNT );
		index.setIndexProperty("index.direct.fields.names", ArrayUtils.join(FieldScore.FIELD_NAMES, ","));
		index.addIndexStructure("document-factory", BasicDocumentIndexEntry.Factory.class.getName(), "", "");
		index.flush();
		DocumentIndex di = index.getDocumentIndex();
		assertNotNull(di);
		assertEquals(postings.length, di.getNumberOfDocuments());
		return index;
	}
	
	protected BitPostingIndexInputFormat makeInputFormat(JobConf jc, Index index, final long blockSize) throws Exception
	{
		BitPostingIndexInputFormat bpiif;
		if (blockSize == 0)
		{
			bpiif = new BitPostingIndexInputFormat();
		}
		else
		{
			bpiif = new BitPostingIndexInputFormat(){
				@Override
				protected long getBlockSize(Path path, FileStatus fss) {
					System.err.println("Forcing blocksize of file " + path + " (size="+fss.getLen() +" actualBlocksize="+fss.getBlockSize() +") to " + blockSize + " bytes");
					return blockSize;
				}				
			};
		}
		BitPostingIndexInputFormat.setStructures(jc, "direct", "document");
		HadoopUtility.toHConfiguration(index, jc);
		return bpiif;
	}
	
	@Test public void SingleFileSingleSplit() throws Exception
	{	
		if (! validPlatform()) return;
		final int[][] postings = new int[][]{new int[]{0,1,2,4,8}, new int[]{0,8,10}};
		Index index = writeIndexStructure(postings);
		
		JobConf jc = new JobConf();
		BitPostingIndexInputFormat bpiif = makeInputFormat(jc, index, 0);
		
		InputSplit[] splits = bpiif.getSplits(jc, 1);
		assertEquals(1, splits.length);		
		RecordReader<IntWritable, IntObjectWrapper<IterablePosting>> rr = bpiif.getRecordReader(splits[0], jc, NULL_REPORTER);
		IntWritable docid = rr.createKey();
		IntObjectWrapper<IterablePosting> iterWrapper = rr.createValue();
		for(int i=0;i<postings.length;i++)
		{
			assertTrue(rr.next(docid, iterWrapper));
			assertEquals(postings[i].length, iterWrapper.getInt());
			IterablePosting iter = iterWrapper.getObject();
			for(int j=0;j<postings[i].length;j++)
			{				
				assertEquals(postings[i][j], iter.next());
				assertEquals(postings[i][j], iter.getId());
			}
			assertEquals(IterablePosting.EOL, iter.next());
		}
		assertFalse(rr.next(docid, iterWrapper));		
	}
//	
	@Test public void SingleFileMultipleSplitsTrailing() throws Exception
	{
		if (! validPlatform()) return;
		final int[][] postings = new int[][]{new int[]{100,200,300,400}, new int[]{0,1,2,4,8}, new int[]{0,8,10}};
		Index index = writeIndexStructure(postings);
		
		JobConf jc = new JobConf();
		BitPostingIndexInputFormat bpiif = makeInputFormat(jc, index, 3);
		
		InputSplit[] splits = bpiif.getSplits(jc, 2);
		assertEquals(2, splits.length);	
		int splitIndex = 0;
		//System.err.println("Split = " + splits[splitIndex]);
		RecordReader<IntWritable, IntObjectWrapper<IterablePosting>> rr = bpiif.getRecordReader(splits[splitIndex], jc, NULL_REPORTER);
		IntWritable docid = rr.createKey();
		IntObjectWrapper<IterablePosting> iterWrapper = rr.createValue();
		
		//System.err.println(((BitPostingIndexInputFormat.BitPostingIndexRecordReader)rr).postingStream.getCurrentPointer());
		
		
		for(int i=0;i<postings.length;i++)
		{
			//System.err.println("i=" + i);
			if (! rr.next(docid, iterWrapper))
			{
				splitIndex++;
				//System.err.println("Split = " + splits[splitIndex]);
				rr = bpiif.getRecordReader(splits[splitIndex], jc, NULL_REPORTER);
				rr.next(docid, iterWrapper);
			}
			assertEquals(postings[i].length, iterWrapper.getInt());
			IterablePosting iter = iterWrapper.getObject();
			assertNotNull(iter);
			for(int j=0;j<postings[i].length;j++)
			{	
				assertEquals(postings[i][j], iter.next());
				//System.err.println("id in posting=" +iter.getId() );
				assertEquals(postings[i][j], iter.getId());
				
			}
			assertEquals(IterablePosting.EOL, iter.next());
		}
		assertFalse(rr.next(docid, iterWrapper));		
	}
	
	
	
//	
//	@Test public void SingleFileMultipleSplitsExact() throws Exception
//	{
//		
//	}
//	
//	@Test public void MultipleFilesOneSplitEach() throws Exception
//	{
//		
//	}
//	
//	@Test public void MultipleFilesMultipleSplitsTrailing() throws Exception
//	{
//		
//	}
//	
//	@Test public void MultipleFilesMultipleSplitsExact() throws Exception
//	{
//		
//	}
	
}
