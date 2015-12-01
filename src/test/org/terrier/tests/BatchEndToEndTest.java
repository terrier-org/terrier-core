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
 * The Original Code is BatchEndToEndTest.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.terrier.applications.TrecTerrier;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.Files;
import org.terrier.utility.io.CheckClosedStreams;

public abstract class BatchEndToEndTest extends ApplicationSetupBasedTest {

	public static abstract class BatchEndToEndTestEventHooks
	{
		public boolean validPlatform()
		{
			return true;
		}
		
		public void checkIndex(BatchEndToEndTest test, Index index) throws Exception
		{}
		
		public void finishedIndexing(BatchEndToEndTest test) throws Exception
		{}
		
		public String[] processRetrievalOptions(String[] retrievalOptions)
		{
			return retrievalOptions;
		}
	}
	
	protected List<String> indexingOptions = new ArrayList<String>();
	protected List<String> retrievalTopicSets = new ArrayList<String>();
	protected List<BatchEndToEndTestEventHooks> testHooks = new ArrayList<BatchEndToEndTestEventHooks>();

	public static String[] joinSets(String[] specified, List<String> classDefaults) {
		List<String> theseOptions = new ArrayList<String>();
		for(String speccedArg : specified)
			theseOptions.add(speccedArg);
		theseOptions.addAll(classDefaults);
		return theseOptions.toArray(new String[0]);
	}

	protected abstract int countNumberOfTopics(String filename) throws Exception;

	public BatchEndToEndTest() {
		super();
	}

	protected void finishIndexing() throws Exception
	{
		for(BatchEndToEndTestEventHooks hook : testHooks)
		{
			hook.finishedIndexing(this);
		}
	}
	
	protected void doIndexing(String... trec_terrier_args) throws Exception
	{
		String path = ApplicationSetup.TERRIER_INDEX_PATH;
		String prefix = ApplicationSetup.TERRIER_INDEX_PREFIX;
		TrecTerrier.main(joinSets(trec_terrier_args, indexingOptions));
		
		//check that application setup hasnt changed unexpectedly
		assertEquals(path, ApplicationSetup.TERRIER_INDEX_PATH);
		assertEquals(prefix, ApplicationSetup.TERRIER_INDEX_PREFIX);
		
		//check that indexing actually created an index
		assertTrue("Index does not exist at ["+ApplicationSetup.TERRIER_INDEX_PATH+","+ApplicationSetup.TERRIER_INDEX_PREFIX+"]", Index.existsIndex(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX));
		IndexOnDisk i = Index.createIndex();
		assertNotNull(Index.getLastIndexLoadError(), i);
		assertEquals(ApplicationSetup.TERRIER_VERSION,i.getIndexProperty("index.terrier.version", ""));
		assertTrue("Index does not have an inverted structure", i.hasIndexStructure("inverted"));
		assertTrue("Index does not have an lexicon structure", i.hasIndexStructure("lexicon"));
		assertTrue("Index does not have an document structure", i.hasIndexStructure("document"));
		assertTrue("Index does not have an meta structure", i.hasIndexStructure("meta"));
		addDirectStructure(i);
		i.close();
		finishIndexing();
	}

	protected abstract void addDirectStructure(IndexOnDisk index) throws Exception;
	
	protected int doRetrieval(String[] topicSet, String[] trecTerrierArgs) throws Exception
	{
		for(BatchEndToEndTestEventHooks hook : testHooks)
		{
			trecTerrierArgs = hook.processRetrievalOptions(trecTerrierArgs);
		}
//		Writer w = Files.writeFileWriter(ApplicationSetup.TREC_TOPICS_LIST);
//		System.err.println("Writing topics files to" + ApplicationSetup.TREC_TOPICS_LIST);
		int queryCount = 0;
		final String[] allTopicFiles = joinSets(topicSet, retrievalTopicSets);
		for(String topicFile : allTopicFiles)
		{	
			queryCount += countNumberOfTopics(topicFile);
//			w.write(topicFile + "\n");
		}
//		w.close();
//		
		String[] newArgs = new String[2+trecTerrierArgs.length];
		newArgs[0] = "-r";
		newArgs[1] = "-Dtrec.topics=" + ArrayUtils.join(allTopicFiles, ',');
		System.arraycopy(trecTerrierArgs, 0, newArgs, 2, trecTerrierArgs.length);
		//System.err.println("TrecTerrier " + ArrayUtils.join(newArgs, " "));
		TrecTerrier.main(newArgs);
		return queryCount;
	}

	
	protected void doEvaluation(int expectedQueryCount, String qrels, float expectedMAP) throws Exception
	{
//		Writer w = Files.writeFileWriter(ApplicationSetup.TREC_QRELS);
//		System.err.println("Writing qrel files files to " + ApplicationSetup.TREC_QRELS);
//		w.write(qrels + "\n");
//		w.close();
		TrecTerrier.main(new String[]{"-e", "-Dtrec.qrels=" + qrels});
		float MAP = -1.0f;
		int queryCount = 0;
		File[] fs = new File(ApplicationSetup.TREC_RESULTS).listFiles();
		assertNotNull(fs);
		for (File f : fs)
		{
			if (f.getName().endsWith(".eval"))
			{
				BufferedReader br = Files.openFileReader(f);
				String line = null;
				while((line = br.readLine()) != null )
				{
					//System.out.println(line);
					if (line.startsWith("Average Precision:"))
					{
						MAP = Float.parseFloat(line.split(":")[1].trim());	
					} 
					else if (line.startsWith("Number of queries  ="))
					{
						queryCount = Integer.parseInt(line.split("\\s+=\\s+")[1].trim());
					}
				}
				br.close();
				break;
			}
		}
		assertEquals("Query count was "+ queryCount + " instead of "+ expectedQueryCount, expectedQueryCount, queryCount);
		assertEquals("MAP was "+MAP + " instead of "+expectedMAP, expectedMAP, MAP, 0.0d);
		
		//System.err.println("Tidying results folder:");
		//System.err.println("ls "+ ApplicationSetup.TREC_RESULTS);
		//System.err.println(Arrays.deepToString(new File(ApplicationSetup.TREC_RESULTS).listFiles()));
		
		//delete all runs and evaluations
		fs = new File(ApplicationSetup.TREC_RESULTS).listFiles();
		assertNotNull(fs);
		for (File f :fs)
		{
			//System.err.println("Checking file for possible deletion: "+f);
			if (f.getName().endsWith(".res") || f.getName().endsWith(".eval"))
			{
				System.err.println("Removing finished file "+f);
				if (! f.delete())
					System.err.println("Could not remove finished file "+f);
			}
		}
	}

	protected void doTrecTerrierIndexingRunAndEvaluate(String[] indexingArgs, String[] topics, String[] retrievalArgs,
			String qrels, float expectedMAP) throws Exception
	{
		CheckClosedStreams.enable();
		for(BatchEndToEndTestEventHooks hook : this.testHooks)
			if (! hook.validPlatform())
			{
				System.err.println("Omitting test due to invalid platform, see " + hook.getClass().getName());
				return;
			}
		doTrecTerrierIndexing(indexingArgs);
		CheckClosedStreams.finished();
		doTrecTerrierRunAndEvaluate(topics, retrievalArgs, qrels, expectedMAP);
		 
	}
	
	protected void doTrecTerrierIndexing(String... indexingArgs) throws Exception
	{
		makeCollectionSpec(new PrintWriter(Files.writeFileWriter(ApplicationSetup.TERRIER_ETC +  "/collection.spec")));
		doIndexing(indexingArgs);
		checkIndex(); 
	}
	
	protected void checkIndex() throws Exception
	{
		Index i = Index.createIndex();
		for(BatchEndToEndTestEventHooks hook : testHooks)
		{
			hook.checkIndex(this, i);
		}
	}

	protected void doTrecTerrierRunAndEvaluate(String[] topics, String[] retrievalArgs,
			String qrels, float expectedMAP) throws Exception
	{
		int queryCount = doRetrieval(topics, retrievalArgs);
		doEvaluation(queryCount, qrels, expectedMAP);
	}
	
	protected abstract void makeCollectionSpec(PrintWriter p) throws Exception;
	
//	@Override
//	protected void tearDown() throws Exception {
//		// TODO Auto-generated method stub
//		super.tearDown();
//		try{
//			IndexUtil.deleteIndex(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
//			final File[] fs = new File(ApplicationSetup.TERRIER_INDEX_PATH).listFiles();
//			if (fs != null)
//				for(File f : fs)
//					f.delete();
//			new File(ApplicationSetup.TERRIER_INDEX_PATH).delete();
//		} catch (IOException ioe) {
//			
//		}
//		ApplicationSetup.clearAllProperties();
//	}

}
