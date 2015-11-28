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
 * The Original is in 'TestSimpleDecorate.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.querying;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.matching.ResultSet;
import org.terrier.matching.models.TF_IDF;
import org.terrier.matching.taat.Full;
import org.terrier.structures.Index;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.StringTools;
import org.terrier.utility.StringTools.ESCAPE;

public class TestSimpleDecorate extends ApplicationSetupBasedTest {
	
	protected Index createIndex() throws Exception {
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.reverse.keys", "");
		ApplicationSetup.setProperty("termpipelines", "");
		Index index = IndexTestUtils.makeIndex(
			new String[]{"doc1", "doc2"}, 
			new String[]{"The quick brown fox jumps over the lazy dog", "foxes"});
		return index;
	}
	
	protected Index createIndexAbstract() throws Exception {
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename,abstract");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "20,2048");
		ApplicationSetup.setProperty("indexer.meta.reverse.keys", "");
		ApplicationSetup.setProperty("FileDocument.abstract", "abstract");
		ApplicationSetup.setProperty("FileDocument.abstract.length", "2048");
		ApplicationSetup.setProperty("termpipelines", "");
		Index index = IndexTestUtils.makeIndex(
			new String[]{"doc1", "doc2"}, 
			new String[]{"The quick brown fox jumps over the lazy dog", "foxes"});
		assertEquals(2, index.getMetaIndex().getKeys().length);
		assertEquals("filename", index.getMetaIndex().getKeys()[0]);
		assertEquals("abstract", index.getMetaIndex().getKeys()[1]);
		return index;
	}
		
	@Test public void testOneDocument() throws Exception {
		Index index = createIndex();
		Manager m = new Manager(index);
		SearchRequest srq = m.newSearchRequest("test", "fox");
		srq.addMatchingModel(Full.class.getName(), TF_IDF.class.getName());
		m.runPreProcessing(srq);
		m.runMatching(srq);
		m.runPostProcessing(srq);
		m.runPostFilters(srq);
		ResultSet rs = srq.getResultSet();
		assertEquals(1, rs.getResultSize());
		SimpleDecorate decorate = new SimpleDecorate();
		decorate.new_query(m, srq, rs);
		decorate.filter(m, srq, rs, 0, rs.getDocids()[0]);
		assertEquals(1, rs.getMetaKeys().length);
		assertEquals("doc1", rs.getMetaItems("filename")[0]);
	}
	
	@Test public void testOneDocumentTwoKeys() throws Exception {
		Index index = createIndexAbstract();
		Manager m = new Manager(index);
		SearchRequest srq = m.newSearchRequest("test", "fox");
		srq.addMatchingModel(Full.class.getName(), TF_IDF.class.getName());
		m.runPreProcessing(srq);
		m.runMatching(srq);
		m.runPostProcessing(srq);
		m.runPostFilters(srq);
		ResultSet rs = srq.getResultSet();
		assertEquals(1, rs.getResultSize());
		SimpleDecorate decorate = new SimpleDecorate();
		decorate.new_query(m, srq, rs);
		decorate.filter(m, srq, rs, 0, rs.getDocids()[0]);
		assertEquals(2, rs.getMetaKeys().length);
		assertEquals("doc1", rs.getMetaItems("filename")[0]);
		assertEquals("The quick brown fox jumps over the lazy dog", 
				StringTools.escape(ESCAPE.JAVA, rs.getMetaItems("abstract")[0]));
	}
	
	
	@Test public void testTwoDocuments() throws Exception {
		Index index = createIndex();
		Manager m = new Manager(index);
		SearchRequest srq = m.newSearchRequest("test", "fox foxes");
		srq.addMatchingModel(Full.class.getName(), TF_IDF.class.getName());
		m.runPreProcessing(srq);
		m.runMatching(srq);
		m.runPostProcessing(srq);
		m.runPostFilters(srq);
		ResultSet rs = srq.getResultSet();
		assertEquals(2, rs.getResultSize());
		SimpleDecorate decorate = new SimpleDecorate();
		decorate.new_query(m, srq, rs);
		decorate.filter(m, srq, rs, 0, rs.getDocids()[0]);
		decorate.filter(m, srq, rs, 1, rs.getDocids()[1]);
		assertEquals(1, rs.getMetaKeys().length);
		assertEquals("doc1", rs.getMetaItems("filename")[1]);
		assertEquals("doc2", rs.getMetaItems("filename")[0]);
	}
}
