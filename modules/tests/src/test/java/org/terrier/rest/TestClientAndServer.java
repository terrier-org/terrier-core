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
 * The Original Code is TestClientAndServer.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.terrier.applications.batchquerying.SingleLineTRECQuery;
import org.terrier.applications.batchquerying.TRECQuerying;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.matching.FatFeaturedScoringMatching;
import org.terrier.matching.daat.FatFull;
import org.terrier.matching.dsms.DFRDependenceScoreModifier;
import org.terrier.querying.IndexRef;
import org.terrier.querying.Manager;
import org.terrier.querying.ManagerFactory;
import org.terrier.querying.SearchRequest;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

public class TestClientAndServer extends ApplicationSetupBasedTest {

	 @Rule
	 public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Test public void itWorksBlocksFeatures() throws Exception {
		Index index = IndexTestUtils.makeIndexBlocks(new String[]{"doc1"}, new String[]{"token1 token2 token3"});		
//		String path = ((IndexOnDisk)index).getPath();
//		String prefix = ((IndexOnDisk)index).getPrefix();
		int port = new Random().nextInt(65536-1024)+1024;
		System.err.println("itWorksBlocksFeatures: Index is " + index.getIndexRef().toString());
		String uri = "http://127.0.0.1:"+port+"/";		
		HttpServer server = makeServer(index, uri);
		SearchResource.reinit();
		index.close();
		
		ApplicationSetup.setProperty("fat.featured.scoring.matching.features", "WMODEL:BM25;WMODEL:PL2;DSM:"+DFRDependenceScoreModifier.class.getSimpleName());
		
		Manager restManager = ManagerFactory.from(IndexRef.of(uri));
		assertNotNull(restManager);
		
		SearchRequest srq = restManager.newSearchRequestFromQuery("\"token1 token2\"");
		srq.setControl(SearchRequest.CONTROL_MATCHING, FatFeaturedScoringMatching.class.getName() + "," + FatFull.class.getName() );
		restManager.runSearchRequest(srq);
		assertEquals(1, srq.getResults().size());
		assertEquals("doc1", srq.getResults().get(0).getMetadata("docno"));		
		server.shutdown().get();
		//IndexUtil.deleteIndex(path, prefix);
	}
	
	@Test public void itWorks() throws Exception {
		Index index = IndexTestUtils.makeIndex(new String[]{"doc1"}, new String[]{"token1 token2 token3"});
//		String path = ((IndexOnDisk)index).getPath();
//		String prefix = ((IndexOnDisk)index).getPrefix();
		
		int port = new Random().nextInt(65536-1024)+1024;
		String uri = "http://127.0.0.1:"+port+"/";
		System.err.println("itWorks: Index is " + index.getIndexRef().toString());
		HttpServer server = makeServer(index, uri);
		SearchResource.reinit();
		index.close();
		
		Manager restManager = ManagerFactory.from(IndexRef.of(uri));
		assertNotNull(restManager);
		
		SearchRequest srq = restManager.newSearchRequestFromQuery("token1");
		restManager.runSearchRequest(srq);
		assertEquals(1, srq.getResults().size());
		assertEquals("doc1", srq.getResults().get(0).getMetadata("docno"));		
		server.shutdown().get();	
		//IndexUtil.deleteIndex(path, prefix);
	}
	
	@Test public void testTRECQuerying() throws Exception {
		Index index = IndexTestUtils.makeIndex(new String[]{"doc1"}, new String[]{"token1 token2 token3"});
		int port = new Random().nextInt(65536-1024)+1024;
		System.err.println("testTRECQuerying: Index is " + index.getIndexRef().toString());
		String uri = "http://127.0.0.1:"+port+"/";		
		HttpServer server = makeServer(index, uri);
		SearchResource.reinit();
		index.close();
		
		//write topics
		File f = tempFolder.newFile("topics");
		FileWriter fw = new FileWriter(f);
		fw.append("1 token2");
		fw.close();
		String topicsFile = f.toString();
		
//		f = tempFolder.newFile("qrels");
//		fw = new FileWriter(f);
//		fw.append("1 Q0 doc1 1");
//		fw.close();
//		String qrelsFilename = f.toString();
		
		
		TRECQuerying tq = new TRECQuerying(IndexRef.of(uri));
		tq.setTopicsParser(SingleLineTRECQuery.class.getName());
		ApplicationSetup.setProperty("trec.topics", topicsFile);
		tq.intialise();
		String resFilename = tq.processQueries();
		
		String line;
		BufferedReader br = Files.openFileReader(resFilename);
		while((line = br.readLine()) != null)
		{
			System.err.println(line);
			String[] parts = line.split("\\s+");
			assertEquals(6, parts.length);
			assertTrue(parts[5].length() > 0);
		}
		br.close();
		server.shutdown().get();
		//IndexUtil.deleteIndex(path, prefix);
	}

	protected HttpServer makeServer(Index index, String uri) {
		String path = ((IndexOnDisk)index).getPath();
		String prefix = ((IndexOnDisk)index).getPrefix();
		assertTrue(IndexOnDisk.existsIndex(path,prefix));
		ApplicationSetup.TERRIER_INDEX_PATH = path;
		ApplicationSetup.TERRIER_INDEX_PREFIX = prefix;
		HttpServer server = SingleIndexRestServer.startServer(uri);
		return server;
	}
}
