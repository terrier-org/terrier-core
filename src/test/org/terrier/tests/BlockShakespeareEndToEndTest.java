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
 * The Original Code is BlockShakespeareEndToEndTest.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.tests;

import static org.junit.Assert.assertTrue;

import org.terrier.structures.Index;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.IterablePosting;

public class BlockShakespeareEndToEndTest extends BasicShakespeareEndToEndTest {
	public static final String PHRASE_TOPICS = "share/tests/shakespeare/test.shakespeare-merchant.phrase.topics";	
	
	@SuppressWarnings("unchecked")
	static class BlockBatchEndToEndTestEventChecks extends BatchEndToEndTestEventHooks
	{
		@Override
		public void checkIndex(BatchEndToEndTest test, Index index)
				throws Exception
		{			
			//no check correct type of all structures
			PostingIndexInputStream bpiis;
			IterablePosting ip;
			PostingIndex<Pointer> bpi;
			
			//check stream structures
			bpiis = (PostingIndexInputStream) index.getIndexStructureInputStream("direct");
			ip = bpiis.next();
			assertTrue(ip instanceof BlockPosting);
			bpiis.close();
			
			bpiis = (PostingIndexInputStream) index.getIndexStructureInputStream("inverted");
			ip = bpiis.next();
			assertTrue(ip instanceof BlockPosting);
			bpiis.close();
			
			//check random structures
			bpi = (PostingIndex<Pointer>) index.getInvertedIndex();
			ip = bpi.getPostings(index.getLexicon().getLexiconEntry(0).getValue());
			assertTrue(ip instanceof BlockPosting);
			bpi = (PostingIndex<Pointer>) index.getDirectIndex();
			ip = bpi.getPostings(index.getDocumentIndex().getDocumentEntry(0));
			assertTrue(ip instanceof BlockPosting);
		}
	}
	
	public BlockShakespeareEndToEndTest() {
		super();
		super.indexingOptions.add("-Dblock.indexing=true");
		super.retrievalTopicSets.add(PHRASE_TOPICS);
		super.testHooks.add(new BlockBatchEndToEndTestEventChecks());
	}
}
