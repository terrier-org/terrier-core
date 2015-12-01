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
 * The Original Code is BasicShakespeareEndToEndTest.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.tests;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.IterablePosting;

public class BasicShakespeareEndToEndTest extends ShakespeareEndToEndTest {

	String testQrels = System.getProperty("user.dir") + "/share/tests/shakespeare/test.shakespeare-merchant.all.qrels";
	
	public BasicShakespeareEndToEndTest()
	{
		retrievalTopicSets.add(System.getProperty("user.dir") + "/share/tests/shakespeare/test.shakespeare-merchant.basic.topics");		
	}	
	
	@Test public void testBasicClassical() throws Exception {
		System.err.println(this.getClass().getName() +" : testBasicClassical");
		doTrecTerrierIndexingRunAndEvaluate(
				new String[]{"-i", "-Dindexer.meta.reverse.keys=docno"}, 
				new String[0], new String[0],
				testQrels, 1.0f);
		
	}
	

	@Test public void testBasicClassicalUTFTokeniser() throws Exception {
		System.err.println(this.getClass().getName() +" : testBasicClassicalUTFTokeniser");
		doTrecTerrierIndexingRunAndEvaluate(
				new String[]{"-i", "-Dtokeniser=UTFTokeniser", "-Dindexer.meta.reverse.keys=docno", /* "-Dtrec.collection.class=TRECUTFCollection" */}, 
				new String[0], new String[0],
				testQrels, 1.0f);
	}
	
	@SuppressWarnings("unchecked")
	static class FieldBatchEndToEndTestEventChecks extends BatchEndToEndTestEventHooks
	{
		@Override
		public void checkIndex(BatchEndToEndTest test, Index index)
				throws Exception
		{
			assertEquals(2, index.getIntIndexProperty("index.inverted.fields.count", -1));
			assertEquals(2, index.getIntIndexProperty("index.direct.fields.count", -1));
			assertTrue("Constructor for lexicon-value type is incorrect", index.getIndexProperty("index.lexicon-valuefactory.parameter_values", "").length() >0);
			assertEquals("TITLE,SPEAKER", index.getIndexProperty("index.inverted.fields.names", null));
			assertEquals("TITLE,SPEAKER", index.getIndexProperty("index.direct.fields.names", null));
			System.err.println("Field tokens=" + index.getIntIndexProperty("num.field.0.Tokens", -1) + "," + 
					index.getIntIndexProperty("num.field.1.Tokens", -1));	
			assertEquals(123, index.getIntIndexProperty("num.field.0.Tokens", -1));
			assertEquals(611, index.getIntIndexProperty("num.field.1.Tokens", -1));
			assertEquals(2, index.getCollectionStatistics().getNumberOfFields());
			assertEquals(123, index.getCollectionStatistics().getFieldTokens()[0]);
			assertEquals(611, index.getCollectionStatistics().getFieldTokens()[1]);
			
			
			
			//now check correct type of all structures
			PostingIndexInputStream bpiis;
			IterablePosting ip;
			PostingIndex<Pointer> bpi;
			
			//check stream structures
			bpiis = (PostingIndexInputStream) index.getIndexStructureInputStream("direct");
			ip = bpiis.next();
			assertTrue(ip instanceof FieldPosting);
			bpiis.close();
			
			bpiis = (PostingIndexInputStream) index.getIndexStructureInputStream("inverted");
			ip = bpiis.next();
			assertTrue(ip instanceof FieldPosting);
			bpiis.close();
			
			//check random structures
			bpi = (PostingIndex<Pointer>) index.getInvertedIndex();
			ip = bpi.getPostings(index.getLexicon().getLexiconEntry(0).getValue());
			assertTrue(ip instanceof FieldPosting);
			bpi = (PostingIndex<Pointer>) index.getDirectIndex();
			ip = bpi.getPostings(index.getDocumentIndex().getDocumentEntry(0));
			assertTrue(ip instanceof FieldPosting);
		}
	}
	
	@Test public void testBasicClassicalFields() throws Exception {
		//return;
		System.err.println(this.getClass().getName() +" : testBasicClassicalFields");
		testHooks.add(new FieldBatchEndToEndTestEventChecks());
		doTrecTerrierIndexingRunAndEvaluate(
				new String[]{"-i", "-DFieldTags.process=TITLE,SPEAKER", "-Dindexer.meta.reverse.keys=docno"}, 
				new String[]{System.getProperty("user.dir") + "/share/tests/shakespeare/test.shakespeare-merchant.field.topics"}, new String[0],
				testQrels, 1.0f);
	}

	@Override
	protected void addDirectStructure(IndexOnDisk index) throws Exception {}

	
}
