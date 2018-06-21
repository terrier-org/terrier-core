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

import java.util.Random;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.Test;
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

public class TestClientAndServer extends ApplicationSetupBasedTest {


	@Test public void itWorksBlocksFeatures() throws Exception {
		Index index = IndexTestUtils.makeIndexBlocks(new String[]{"doc1"}, new String[]{"token1 token2 token3"});		
		int port = new Random().nextInt(65536-1024)+1024;
		String uri = "http://127.0.0.1:"+port+"/";		
		HttpServer server = makeServer(index, uri);
		index.close();
		
		ApplicationSetup.setProperty("fat.featured.scoring.matching.features", "WMODEL:BM25;WMODEL:PL2;DSM:"+DFRDependenceScoreModifier.class.getSimpleName());
		
		Manager restManager = ManagerFactory.from(IndexRef.of(uri));
		assertNotNull(restManager);
		
		SearchRequest srq = restManager.newSearchRequestFromQuery("\"token1 token2\"");
		srq.setControl(SearchRequest.CONTROL_MATCHING, FatFeaturedScoringMatching.class.getName() + "," + FatFull.class.getName() );
		restManager.runSearchRequest(srq);
		assertEquals(1, srq.getResults().size());
		assertEquals("doc1", srq.getResults().get(0).getMetadata("docno"));		
		server.shutdown();
	}
	
	@Test public void itWorks() throws Exception {
		Index index = IndexTestUtils.makeIndex(new String[]{"doc1"}, new String[]{"token1 token2 token3"});		
		int port = new Random().nextInt(65536-1024)+1024;
		String uri = "http://127.0.0.1:"+port+"/";		
		HttpServer server = makeServer(index, uri);
		index.close();
		
		Manager restManager = ManagerFactory.from(IndexRef.of(uri));
		assertNotNull(restManager);
		
		SearchRequest srq = restManager.newSearchRequestFromQuery("token1");
		restManager.runSearchRequest(srq);
		assertEquals(1, srq.getResults().size());
		assertEquals("doc1", srq.getResults().get(0).getMetadata("docno"));		
		server.shutdown();		
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
