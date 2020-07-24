package org.terrier.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( { 
	SinglePassShakespeareEndToEndTest.BasicSinglePassShakespeareEndToEndTest.class,
	SinglePassShakespeareEndToEndTest.BlockSinglePassShakespeareEndToEndTest.class,
	SinglePassShakespeareEndToEndTest.MultiPassBasicSinglePassShakespeareEndToEndTest.class,
	SinglePassShakespeareEndToEndTest.MultiPassBlockSinglePassShakespeareEndToEndTest.class,
	SinglePassShakespeareEndToEndTest.BlockSinglePassMaxBlocksShakespeareEndToEndTest.class,
})
public class SinglePassEndToEndTestSuite {}