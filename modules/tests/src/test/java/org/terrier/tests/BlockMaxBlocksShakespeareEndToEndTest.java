package org.terrier.tests;

import static org.junit.Assert.assertTrue;

import org.terrier.structures.Index;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.IterablePosting;

public class BlockMaxBlocksShakespeareEndToEndTest extends
		BlockShakespeareEndToEndTest {

	public BlockMaxBlocksShakespeareEndToEndTest()
	{
		super();
		super.indexingOptions.add("-Dblocks.max=" + 2);
		super.testHooks.add(new MaxBlockChecks(2));
	}
	
	
	
	@Override
	protected void doEvaluation(int expectedQueryCount, String qrels,
			float expectedMAP) throws Exception {}



	public static class MaxBlockChecks extends BatchEndToEndTestEventHooks
	{
		int maxBlocks;
		public MaxBlockChecks(int max) {
			maxBlocks = max;
		}
		
		@Override
		public void checkIndex(BatchEndToEndTest test, Index index)
				throws Exception
		{			
			testInverted(index);
			if (index.hasIndexStructure("direct"))
				testDirect(index);
		}
		
		protected void testDirect(Index index) throws Exception {
			
		}
		
		protected void testInverted(Index index) throws Exception {
			PostingIndexInputStream piis = (PostingIndexInputStream) index.getIndexStructureInputStream("inverted");
			IterablePosting ip = null;
			BlockPosting bp = null;
			while(piis.hasNext())
			{
				ip = piis.getNextPostings();
				if (ip == null)
					continue;
				bp = (BlockPosting) ip;
				while(ip.next() != IterablePosting.EOL)
				{
					int tf = bp.getFrequency();
					int[] blocks = bp.getPositions();
					assertTrue("blocks.length="+blocks.length  + " tf="+tf, 
							blocks.length <= tf);
					assertTrue("blocks.length="+blocks.length + " tf="+tf + ", blocks longer than max "+ maxBlocks, 
								blocks.length <= maxBlocks);				
				}
			}
			piis.close();
		}
	}
}
