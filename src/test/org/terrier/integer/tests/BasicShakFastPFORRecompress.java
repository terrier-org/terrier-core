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
 * The Original Code is BasicShakFastPFORRecompress.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */

package org.terrier.integer.tests;

import static org.junit.Assert.assertTrue;

import org.terrier.compression.integer.codec.LemireFastPFORVBCodec;
import org.terrier.structures.Index;
import org.terrier.structures.integer.IntegerCodecCompressionConfiguration;
import org.terrier.structures.integer.IntegerCodingPostingIndex;
import org.terrier.tests.BatchEndToEndTest;
import org.terrier.utility.ApplicationSetup;

public class BasicShakFastPFORRecompress extends BasicShakNullRecompress {

	static class PForDoRecompress extends DoRecompress
	{

		@Override
		public void finishedIndexing(BatchEndToEndTest test) throws Exception {
			/*
			 * compression.tmp-inverted.integer.ids.codec=LemireFastPFORVBCodec
				compression.tmp-inverted.integer.tfs.codec=LemireFastPFORVBCodec
				compression.tmp-inverted.integer.fields.codec=LemireFastPFORVBCodec
				compression.tmp-inverted.integer.blocks.codec=LemireFastPFORVBCodec
				indexing.tmp-inverted.compression.configuration=IntegerCodecCompressionConfiguration
				compression.integer.chunk.size=1024
			 */
			ApplicationSetup.setProperty("compression.tmp-inverted.integer.ids.codec", LemireFastPFORVBCodec.class.getName());
			ApplicationSetup.setProperty("compression.tmp-inverted.integer.tfs.codec", LemireFastPFORVBCodec.class.getName());
			ApplicationSetup.setProperty("compression.tmp-inverted.integer.fields.codec", LemireFastPFORVBCodec.class.getName());
			ApplicationSetup.setProperty("compression.tmp-inverted.integer.blocks.codec", LemireFastPFORVBCodec.class.getName());
			ApplicationSetup.setProperty("indexing.tmp-inverted.compression.configuration", IntegerCodecCompressionConfiguration.class.getName());
			ApplicationSetup.setProperty("compression.integer.chunk.size", "1024");
			super.finishedIndexing(test);

		}

		@Override
		public void checkIndex(BatchEndToEndTest test, Index index)
				throws Exception {
			assertTrue(index.getIndexProperty("index.inverted.class", null).contains(IntegerCodingPostingIndex.class.getSimpleName()));
		}
		
		
		
	}
	
	public BasicShakFastPFORRecompress()
	{
		super.testHooks.add(new PForDoRecompress());
	}
	
}
