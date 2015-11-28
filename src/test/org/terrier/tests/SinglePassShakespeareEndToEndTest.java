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
 * The Original Code is SinglePassShakespeareEndToEndTest.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.tests;

import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.indexing.singlepass.BlockInverted2DirectIndexBuilder;
import org.terrier.structures.indexing.singlepass.Inverted2DirectIndexBuilder;
import org.terrier.tests.BlockMaxBlocksShakespeareEndToEndTest.MaxBlockChecks;
import org.terrier.utility.ApplicationSetup;

public class SinglePassShakespeareEndToEndTest 
{
	static public class BasicSinglePassShakespeareEndToEndTest extends BasicShakespeareEndToEndTest
	{
		public BasicSinglePassShakespeareEndToEndTest()
		{
			indexingOptions.add("-j");
		}

		@Override
		protected void addDirectStructure(IndexOnDisk index) throws Exception {
			new Inverted2DirectIndexBuilder(index).createDirectIndex();
		}
	}
		
	static public class MultiPassBasicSinglePassShakespeareEndToEndTest extends BasicSinglePassShakespeareEndToEndTest
	{
		public MultiPassBasicSinglePassShakespeareEndToEndTest()
		{
			indexingOptions.add("-Dindexing.singlepass.max.documents.flush=12");
		}
		
		@Override
		protected void addDirectStructure(IndexOnDisk index) throws Exception {
			ApplicationSetup.setProperty("inverted2direct.processtokens", "4272");
			super.addDirectStructure(index);
		}
	}
	
	static public class BlockSinglePassShakespeareEndToEndTest extends BlockShakespeareEndToEndTest
	{
		public BlockSinglePassShakespeareEndToEndTest()
		{
			indexingOptions.add("-j");
		}
		
		@Override
		protected void addDirectStructure(IndexOnDisk index) throws Exception {
			new BlockInverted2DirectIndexBuilder(index).createDirectIndex();
		}
		
	}
	
	static public class BlockSinglePassMaxBlocksShakespeareEndToEndTest extends BlockSinglePassShakespeareEndToEndTest
	{
		public BlockSinglePassMaxBlocksShakespeareEndToEndTest()
		{
			super();
			super.indexingOptions.add("-Dblocks.max=" + 2);
			super.testHooks.add(new MaxBlockChecks(2));
		}
		
		@Override
		protected void doEvaluation(int expectedQueryCount, String qrels,
				float expectedMAP) throws Exception {}
		
	}
	
	static public class MultiPassBlockSinglePassShakespeareEndToEndTest extends BlockSinglePassShakespeareEndToEndTest
	{
		public MultiPassBlockSinglePassShakespeareEndToEndTest()
		{
			indexingOptions.add("-Dindexing.singlepass.max.documents.flush=12");
		}

		@Override
		protected void addDirectStructure(IndexOnDisk index) throws Exception {
			ApplicationSetup.setProperty("inverted2direct.processtokens", "4272");
			super.addDirectStructure(index);
		}
	}
}
