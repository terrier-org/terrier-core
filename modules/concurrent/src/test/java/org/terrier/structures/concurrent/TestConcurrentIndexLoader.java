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
 * The Original Code is TestConcurrentIndexLoader.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures.concurrent;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.querying.IndexRef;
import org.terrier.structures.ConcurrentIndexLoader;
import org.terrier.structures.Index;
import org.terrier.structures.IndexFactory;
import org.terrier.tests.ApplicationSetupBasedTest;

public class TestConcurrentIndexLoader extends ApplicationSetupBasedTest {

	@Test public void testNewIndex() throws Exception
	{
		Index index = IndexTestUtils.makeIndex(new String[]{"doc1", "doc2"}, new String[]{"the quick fox", "and all that stuff"});
		IndexRef ref = index.getIndexRef();
		assertTrue(IndexFactory.isLoaded(ref));
		System.out.println(ref.toString());
		IndexRef concRef = ConcurrentIndexLoader.makeConcurrent(ref);
		System.out.println(concRef.toString());
		Index concurrent = IndexFactory.of(concRef);
		assertNotNull(concurrent);
		assertTrue(concurrent.getLexicon() instanceof ConcurrentLexicon);
	}
	
	@Test public void testDirectIndex() throws Exception
	{
		Index index = IndexTestUtils.makeIndex(new String[]{"doc1", "doc2"}, new String[]{"the quick fox", "and all that stuff"});
		IndexRef ref = index.getIndexRef();
		assertTrue(IndexFactory.isLoaded(ref));
		System.out.println(ref.toString());
		IndexRef concRef = ConcurrentIndexLoader.makeConcurrent(IndexRef.of(ref.toString()));
		Index concurrent = IndexFactory.of(concRef);
		assertNotNull(concurrent);
		assertTrue(concurrent.getLexicon() instanceof ConcurrentLexicon);
	}
	
}
