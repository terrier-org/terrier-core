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
 * The Original Code is TestInverted2DirectIndexBuilder.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures.indexing.singlepass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

/** This tests some methods internal to Inverted2DirectIndexBuilder. Most testing of
 * Inverted2DirectIndexBuilder is carried out within the ShakespeareEndtoEndTestSuite.
 * @author craigm
 * @since 4.1
 */
public class TestInverted2DirectIndexBuilder extends ApplicationSetupBasedTest {
	
	@Test public void testQuickly() throws Exception
	{
		Index index = IndexTestUtils.makeIndexSinglePass(
				new String[]{"doc1", "doc2"}, 
				new String[]{"Quick fast brown fox", "jumped huge black lazy dog"});//no stopwords
		assertFalse(index.hasIndexStructure("direct"));
		assertFalse(index.hasIndexStructure("direct-inputstream"));
		assertTrue(index instanceof IndexOnDisk);
		new Inverted2DirectIndexBuilder((IndexOnDisk) index).createDirectIndex();
		assertTrue(index.hasIndexStructure("direct"));
		assertTrue(index.hasIndexStructure("direct-inputstream"));
	}
	
	@Test public void testQuicklyMultiPass() throws Exception
	{
		Index index = IndexTestUtils.makeIndexSinglePass(
				new String[]{"doc1", "doc2"}, 
				new String[]{"Quick fast brown fox", "jumped huge black lazy dog"});//no stopwords
		assertFalse(index.hasIndexStructure("direct"));
		assertFalse(index.hasIndexStructure("direct-inputstream"));
		assertTrue(index instanceof IndexOnDisk);
		ApplicationSetup.setProperty("inverted2direct.processtokens", "4");
		new Inverted2DirectIndexBuilder((IndexOnDisk) index).createDirectIndex();
		assertTrue(index.hasIndexStructure("direct"));
		assertTrue(index.hasIndexStructure("direct-inputstream"));
	}

	@SuppressWarnings("unchecked")
	@Test public void testScanForTokens() throws Exception
	{
		Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1", "doc2"}, 
				new String[]{"Quick fast brown fox", "jumped huge black lazy dog"});//no stopwords
		
		long[] testConstraint = new long[]{0, 1, 4, 5, 9, 10};
		int[] numDocs = new int[]{1,1,1,2,2,2};
		for(int i=0;i<testConstraint.length;i++)
		{
			Iterator<DocumentIndexEntry> dois = (Iterator<DocumentIndexEntry>) index.getIndexStructureInputStream("document");
			assertEquals("Looking for numdocs to match " + testConstraint[i] + " tokens", 
					numDocs[i], Inverted2DirectIndexBuilder.scanDocumentIndexForTokens(testConstraint[i], dois));
			IndexUtil.close(dois);
		}
		
	}
	
}
