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
 * The Original Code is MultiThreadedShakespeareEndToEndTest.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.tests;

import org.terrier.tests.SinglePassShakespeareEndToEndTest.BasicSinglePassShakespeareEndToEndTest;
import org.terrier.tests.SinglePassShakespeareEndToEndTest.BlockSinglePassShakespeareEndToEndTest;


public class MultiThreadedShakespeareEndToEndTest {

	public static class BasicThreads extends BasicShakespeareEndToEndTest {
		public BasicThreads() {
			super();
			indexingOptions.add("-P");
		}
	}
	
	public static class BlockThreads extends BlockShakespeareEndToEndTest {
		public BlockThreads() {
			super();
			indexingOptions.add("-P");
		}
	}

	public static class BasicSinglePassThreads extends BasicSinglePassShakespeareEndToEndTest {
		public BasicSinglePassThreads() {
			super();
			indexingOptions.add("-P");
		}
	}
	
	public static class BlockSinglePassThreads extends BlockSinglePassShakespeareEndToEndTest {
		public BlockSinglePassThreads() {
			super();
			indexingOptions.add("-P");
		}
	}
	
}
