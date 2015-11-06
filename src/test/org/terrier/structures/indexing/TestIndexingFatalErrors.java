package org.terrier.structures.indexing;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.structures.indexing.classical.BlockIndexer;
import org.terrier.structures.indexing.singlepass.BasicSinglePassIndexer;
import org.terrier.structures.indexing.singlepass.BlockSinglePassIndexer;
import org.terrier.tests.ApplicationSetupBasedTest;

public class TestIndexingFatalErrors extends ApplicationSetupBasedTest {

	
	public void _testIndexDocnoTooLong(Class<? extends Indexer> clz) throws Exception
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
