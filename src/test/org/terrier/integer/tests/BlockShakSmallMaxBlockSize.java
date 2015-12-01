package org.terrier.integer.tests;

import org.terrier.tests.BlockMaxBlocksShakespeareEndToEndTest.MaxBlockChecks;

public class BlockShakSmallMaxBlockSize extends BlockShak {

	public BlockShakSmallMaxBlockSize()
	{
		super();
		super.indexingOptions.add("-Dblocks.max=" + 2);
		super.testHooks.add(new MaxBlockChecks(2));
	}
	
	@Override
	protected void doEvaluation(int expectedQueryCount, String qrels,
			float expectedMAP) throws Exception {}
	
}
