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
