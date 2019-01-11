package org.terrier.applications;

import static org.junit.Assert.*;

import java.io.PrintWriter;

import org.junit.Test;
import org.terrier.structures.Index;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

public class TestBatchIndexingBlocks extends ApplicationSetupBasedTest {

	@Test public void test() throws Exception
	{
		PrintWriter p = new PrintWriter(Files.writeFileWriter(ApplicationSetup.COLLECTION_SPEC));
		p.println(System.getProperty("user.dir") + "/../../share/tests/shakespeare/shakespeare-merchant.trec.1");
		p.println(System.getProperty("user.dir") + "/../../share/tests/shakespeare/shakespeare-merchant.trec.2");
		p.close();
		
		CLITool.main(new String[]{"batchindexing", "-b"});
		Index index = Index.createIndex();
		assertNotNull(index);
		assertEquals(22, index.getCollectionStatistics().getNumberOfDocuments());
		assertTrue( index.getDirectIndex().getPostings(index.getDocumentIndex().getDocumentEntry(0)) instanceof BlockPosting );
	}
	
	@Test public void testParallel() throws Exception
	{
		PrintWriter p = new PrintWriter(Files.writeFileWriter(ApplicationSetup.COLLECTION_SPEC));
		p.println(System.getProperty("user.dir") + "/../../share/tests/shakespeare/shakespeare-merchant.trec.1");
		p.println(System.getProperty("user.dir") + "/../../share/tests/shakespeare/shakespeare-merchant.trec.2");
		p.close();
		
		CLITool.main(new String[]{"batchindexing", "-b", "-p"});
		Index index = Index.createIndex();
		assertNotNull(index);
		assertEquals(22, index.getCollectionStatistics().getNumberOfDocuments());
		assertTrue( index.getDirectIndex().getPostings(index.getDocumentIndex().getDocumentEntry(0)) instanceof BlockPosting );
	}
	
}
