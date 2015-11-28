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
 * The Original Code is ShakespeareEndToEndTestSuite.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( { 
	BasicShakespeareEndToEndTest.class,
	BlockShakespeareEndToEndTest.class,
	
	BlockMaxBlocksShakespeareEndToEndTest.class,
	
	MatchingShakespeareEndToEndTests.TestDAATFullMatchingShakespeareEndToEndTest.class,
	MatchingShakespeareEndToEndTests.TestDAATFullNoPLMMatchingShakespeareEndToEndTest.class,
	MatchingShakespeareEndToEndTests.TestTAATFullNoPLMMatchingShakespeareEndToEndTest.class,	
	
	SinglePassShakespeareEndToEndTest.BasicSinglePassShakespeareEndToEndTest.class,
	SinglePassShakespeareEndToEndTest.BlockSinglePassShakespeareEndToEndTest.class,
	SinglePassShakespeareEndToEndTest.MultiPassBasicSinglePassShakespeareEndToEndTest.class,
	SinglePassShakespeareEndToEndTest.MultiPassBlockSinglePassShakespeareEndToEndTest.class,
	SinglePassShakespeareEndToEndTest.BlockSinglePassMaxBlocksShakespeareEndToEndTest.class,
	
	
	MergingShakespeareEndToEndTests.BasicMerging.class,
	MergingShakespeareEndToEndTests.BlockMerging.class,
	MergingShakespeareEndToEndTests.BasicSinglePassMerging.class,
	MergingShakespeareEndToEndTests.BlockSinglePassMerging.class,
	
	HadoopShakespeareEndToEndTest.BasicHadoopShakespeareEndToEndTest.class,
	HadoopShakespeareEndToEndTest.BlockHadoopShakespeareEndToEndTest.class,
	HadoopShakespeareEndToEndTest.BasicHadoopShakespeareEndToEndTestLowMem.class,
	HadoopShakespeareEndToEndTest.BlockHadoopShakespeareEndToEndTestLowMem.class,
	
	OnDiskShakespeareEndToEndTest.class,
	BlockOnDiskShakespeareEndToEndTest.class
} )
public class ShakespeareEndToEndTestSuite {}
