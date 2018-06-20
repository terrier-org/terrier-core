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
