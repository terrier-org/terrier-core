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
 * The Original Code is BlockMaxBlocksShakespeareEndToEndTest.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
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
