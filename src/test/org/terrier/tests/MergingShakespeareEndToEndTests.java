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
 * The Original Code is MergingShakespeareEndToEndTests.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.tests;

import org.terrier.tests.SinglePassShakespeareEndToEndTest.BasicSinglePassShakespeareEndToEndTest;
import org.terrier.tests.SinglePassShakespeareEndToEndTest.BlockSinglePassShakespeareEndToEndTest;

public class MergingShakespeareEndToEndTests
{
	public static class BasicMerging extends BasicShakespeareEndToEndTest {
		public BasicMerging() {
			super();
			indexingOptions.add("-Dindexing.max.docs.per.builder=12");
		}
	}
	
	public static class BlockMerging extends BlockShakespeareEndToEndTest {
		public BlockMerging() {
			super();
			indexingOptions.add("-Dindexing.max.docs.per.builder=12");
		}
	}

	public static class BasicSinglePassMerging extends BasicSinglePassShakespeareEndToEndTest {
		public BasicSinglePassMerging() {
			super();
			indexingOptions.add("-Dindexing.max.docs.per.builder=12");
		}
	}
	
	public static class BlockSinglePassMerging extends BlockSinglePassShakespeareEndToEndTest {
		public BlockSinglePassMerging() {
			super();
			indexingOptions.add("-Dindexing.max.docs.per.builder=12");
		}
	}
	
}
