package org.terrier.structures.indexing;

import static org.junit.Assert.*;
import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.structures.Index;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.structures.indexing.classical.BlockIndexer;
import org.terrier.structures.indexing.singlepass.BasicSinglePassIndexer;
import org.terrier.structures.indexing.singlepass.BlockSinglePassIndexer;
import org.terrier.tests.ApplicationSetupBasedTest;

public class TestIndexingFatalErrors extends ApplicationSetupBasedTest {

	
	@Test
	public void testIndexTrailingEmptyDocumentBasic() throws Exception
	{
		_testIndexTrailingEmptyDocument(BasicIndexer.class);
	}
	
	@Test
	public void testIndexTrailingEmptyDocumentBlock() throws Exception
	{
		_testIndexTrailingEmptyDocument(BlockIndexer.class);
	}
	
	
	@Test
	public void testIndexTrailingEmptyDocumentBasicSP() throws Exception
	{
		_testIndexTrailingEmptyDocument(BasicSinglePassIndexer.class);
	}
	
	@Test
	public void testIndexTrailingEmptyDocumentBlockSP() throws Exception
	{
		_testIndexTrailingEmptyDocument(BasicSinglePassIndexer.class);
	}
	
	void _testIndexTrailingEmptyDocument(Class<? extends Indexer> clz) throws Exception
	{
		Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1", "doc2", "doc3"}, 
				new String[]{"test document", "another test document", "" /* empty doc */}, 
				clz);
		assertEquals(3, index.getCollectionStatistics().getNumberOfDocuments());
	}
	
	void _testIndexDocnoTooLong(Class<? extends Indexer> clz) throws Exception
	{
		IndexTestUtils.makeIndex(
				new String[]{"ConsumerCourt_DCDRC_17555130220114129133OP-05-576"}, 
				new String[]{"test document"}, clz);
	}
	
	@Test(expected=RuntimeException.class)
	public void testIndexDocnoTooLongBasic() throws Exception
	{
		_testIndexDocnoTooLong(BasicIndexer.class);
	}
	
	@Test(expected=RuntimeException.class)
	public void testIndexDocnoTooLongBlocks() throws Exception
	{
		_testIndexDocnoTooLong(BlockIndexer.class);
	}
	
	@Test(expected=RuntimeException.class)
	public void testIndexDocnoTooLongBasicSP() throws Exception
	{
		_testIndexDocnoTooLong(BasicSinglePassIndexer.class);
	}
	
	@Test(expected=RuntimeException.class)
	public void testIndexDocnoTooLongBlockSP() throws Exception
	{
		_testIndexDocnoTooLong(BlockSinglePassIndexer.class);
	}
	
	
}
