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
 * The Original is in 'TestPostingListManager.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.matching;

import static org.junit.Assert.*;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.matching.models.TF_IDF;
import org.terrier.structures.Index;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestPostingListManager extends ApplicationSetupBasedTest {

	protected Index createIndex() throws Exception {
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.reverse.keys", "");
		ApplicationSetup.setProperty("termpipelines", "");
		Index index = IndexTestUtils.makeIndex(
			new String[]{"doc1", "doc2"}, 
			new String[]{"The quick brown fox jumps over the lazy dog", "foxes"});
		return index;
	}
	
	
	
	@Test public void testSingleTermNoMatch() throws Exception {
		Index index = createIndex();
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.addTermPropertyWeight("red", 1.2d);
		mqt.setDefaultTermWeightingModel(new TF_IDF());
		PostingListManager p;
		p = new PostingListManager(index, index.getCollectionStatistics(), mqt);
		p.prepare(true);
		assertEquals(0, p.size());
		p.close();
	}
	
	@Test public void testSingleTerm() throws Exception {
		Index index = createIndex();
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.addTermPropertyWeight("foxes", 1.2d);
		mqt.setDefaultTermWeightingModel(new TF_IDF());
		PostingListManager p;
		IterablePosting ip;
		p = new PostingListManager(index, index.getCollectionStatistics(), mqt);
		p.prepare(true);
		assertEquals(1, p.size());
		
		assertEquals(1.2d, p.getKeyFrequency(0), 0.0d);
		assertEquals("foxes", p.getTerm(0));
		
		ip = p.getPosting(0);
		assertEquals(1, ip.getId());
		assertEquals(1, ip.getFrequency());
		assertEquals(IterablePosting.EOL, ip.next());
		p.close();
		p = new PostingListManager(index, index.getCollectionStatistics(), mqt);
		p.prepare(false);
		assertEquals(1, p.size());
		ip = p.getPosting(0);
		assertEquals(1, ip.next());
		assertEquals(1, ip.getId());
		assertEquals(1, ip.getFrequency());
		assertTrue(p.score(0) > 0);
		assertEquals(1, p.getStatistics(0).getFrequency());
		assertEquals(1, p.getStatistics(0).getDocumentFrequency());
		assertEquals(IterablePosting.EOL, ip.next());
		p.close();
	}
	
	@Test public void testMultipleTermBothMatch() throws Exception {
		Index index = createIndex();
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setDefaultTermWeightingModel(new TF_IDF());
		mqt.addTermPropertyWeight("foxes", 1);
		mqt.addTermPropertyWeight("quick", 1);
		PostingListManager p;
		IterablePosting ip;
		
		p = new PostingListManager(index, index.getCollectionStatistics(), mqt);
		p.prepare(false);
		assertEquals(2, p.size());
		ip = p.getPosting(0);
		assertEquals(1, ip.next());
		assertEquals(1, ip.getId());
		assertEquals(1, ip.getFrequency());
		assertEquals(IterablePosting.EOL, ip.next());
		
		ip = p.getPosting(1);
		assertEquals(0, ip.next(), 0);
		assertEquals(0, ip.getId(), 0);
		assertEquals(1, ip.getFrequency(), 1);
		assertEquals(IterablePosting.EOL, ip.next());
		p.close();
	}
	
	@Test public void testMultipleTermOneMatch() throws Exception {
		Index index = createIndex();
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.addTermPropertyWeight("foxes", 1.2d);
		mqt.addTermPropertyWeight("red", 1d);
		mqt.setDefaultTermWeightingModel(new TF_IDF());
		PostingListManager p;
		IterablePosting ip;
		p = new PostingListManager(index, index.getCollectionStatistics(), mqt);
		p.prepare(true);
		assertEquals(1, p.size());
		
		assertEquals(1.2d, p.getKeyFrequency(0), 0.0d);
		assertEquals("foxes", p.getTerm(0));
		
		ip = p.getPosting(0);
		assertEquals(1, ip.getId());
		assertEquals(1, ip.getFrequency());
		assertEquals(IterablePosting.EOL, ip.next());
		p.close();
		p = new PostingListManager(index, index.getCollectionStatistics(), mqt);
		p.prepare(false);
		assertEquals(1, p.size());
		ip = p.getPosting(0);
		assertEquals(1, ip.next());
		assertEquals(1, ip.getId());
		assertEquals(1, ip.getFrequency());
		assertEquals(IterablePosting.EOL, ip.next());
		p.close();
	}
	
	@Test public void testMultipleTermNoMatch() throws Exception {
		Index index = createIndex();
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.addTermPropertyWeight("badger", 1.2d);
		mqt.addTermPropertyWeight("mole", 1.2d);		
		mqt.setDefaultTermWeightingModel(new TF_IDF());
		PostingListManager p;
		p = new PostingListManager(index, index.getCollectionStatistics(), mqt);
		p.prepare(true);
		assertEquals(0, p.size());
		p.close();
	}
	
	
	@Test public void testSynonymBothMatch() throws Exception {
		Index index = createIndex();
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.addTermPropertyWeight("fox|foxes", 1.2d);
		mqt.setDefaultTermWeightingModel(new TF_IDF());
		PostingListManager p;
		IterablePosting ip;
		p = new PostingListManager(index, index.getCollectionStatistics(), mqt);
		p.prepare(true);
		assertEquals(1, p.size());
		
		assertEquals(1.2d, p.getKeyFrequency(0), 0.0d);
		assertEquals("fox|foxes", p.getTerm(0));
		
		ip = p.getPosting(0);
		assertEquals(0, ip.getId());
		assertEquals(1, ip.getFrequency());
		assertEquals(1, ip.next());
		assertEquals(1, ip.getId());
		assertEquals(1, ip.getFrequency());
		assertEquals(IterablePosting.EOL, ip.next());		
		p.close();
		p = new PostingListManager(index, index.getCollectionStatistics(), mqt);
		p.prepare(false);
		assertEquals(1, p.size());
		ip = p.getPosting(0);
		assertEquals(0, ip.next());
		assertEquals(0, ip.getId());
		assertEquals(1, ip.getFrequency());
		assertEquals(1, ip.next());
		assertEquals(1, ip.getId());
		assertEquals(1, ip.getFrequency());
		assertEquals(IterablePosting.EOL, ip.next());
		p.close();
	}
	
	@Test public void testSynonymOneMatch() throws Exception {
		Index index = createIndex();
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.addTermPropertyWeight("red|brown", 1.2d);
		mqt.setDefaultTermWeightingModel(new TF_IDF());
		PostingListManager p;
		IterablePosting ip;
		p = new PostingListManager(index, index.getCollectionStatistics(), mqt);
		p.prepare(true);
		assertEquals(1, p.size());
		
		assertEquals(1.2d, p.getKeyFrequency(0), 0.0d);
		assertEquals("red|brown", p.getTerm(0));
		
		ip = p.getPosting(0);
		assertEquals(0, ip.getId());
		assertEquals(1, ip.getFrequency());
		assertEquals(IterablePosting.EOL, ip.next());
		p.close();
	}
	
	
	@Test public void testSynonymNoMatch() throws Exception {
		Index index = createIndex();
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.addTermPropertyWeight("badger|mole", 1.2d);
		mqt.setDefaultTermWeightingModel(new TF_IDF());
		PostingListManager p;
		p = new PostingListManager(index, index.getCollectionStatistics(), mqt);
		p.prepare(true);
		assertEquals(0, p.size());
		p.close();
	}
	
}
