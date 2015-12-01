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
 * The Original is in 'TestDecorate.java'
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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.matching.ResultSet;
import org.terrier.matching.models.TF_IDF;
import org.terrier.matching.taat.Full;
import org.terrier.querying.summarisation.TestDefaultSummariser;
import org.terrier.structures.Index;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestDecorate extends ApplicationSetupBasedTest {

	
	protected Index createIndex() throws Exception {
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename,abstract");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "20,2048");
		ApplicationSetup.setProperty("indexer.meta.reverse.keys", "");
		ApplicationSetup.setProperty("termpipelines", "");
		ApplicationSetup.setProperty("FileDocument.abstract", "abstract");
		ApplicationSetup.setProperty("FileDocument.abstract.length", "2048");
		ApplicationSetup.setProperty("decorate.escape", "url");
		Index index = IndexTestUtils.makeIndex(
			new String[]{"doc1", "doc2"}, 
			new String[]{"The quick brown fox jumps over the lazy dog", TestDefaultSummariser.doc1});
		assertEquals(2, index.getMetaIndex().getKeys().length);
		assertEquals("filename", index.getMetaIndex().getKeys()[0]);
		assertEquals("abstract", index.getMetaIndex().getKeys()[1]);
		return index;
	}
	
	SearchRequest performQuery(Manager m, String query)
	{
		SearchRequest srq = m.newSearchRequest("test", query);
		srq.addMatchingModel(Full.class.getName(), TF_IDF.class.getName());
		m.runPreProcessing(srq);
		m.runMatching(srq);
		m.runPostProcessing(srq);
		m.runPostFilters(srq);
		return srq;
	}
	
	@Test public void testOneDocumentBasic() throws Exception {
		Index index = createIndex();
		Manager m = new Manager(index);
		ResultSet rs; Decorate decorate; SearchRequest srq;
		//postfilter
		srq = performQuery(m, "fox");
		
		rs = srq.getResultSet();
		assertEquals(1, rs.getResultSize());
		decorate = new Decorate();
		decorate.new_query(m, srq, rs);
		decorate.filter(m, srq, rs, 0, rs.getDocids()[0]);
		assertEquals(2, rs.getMetaKeys().length);
		assertEquals("doc1", rs.getMetaItems("filename")[0]);
		assertEquals("The quick brown fox jumps over the lazy dog", rs.getMetaItems("abstract")[0]);
		
	}
	
	@Test public void testOneDocumentBasicEarly() throws Exception {
		Index index = createIndex();
		Manager m = new Manager(index);
		ResultSet rs; Decorate decorate; SearchRequest srq;

		//postprocess
		srq = performQuery(m, "fox");
		srq.setControl("earlyDecorate", "filename;abstract");
		rs = srq.getResultSet();
		assertEquals(1, rs.getResultSize());
		decorate = new Decorate();
		decorate.process(m, srq);
		decorate.new_query(m, srq, rs);
		decorate.filter(m, srq, rs, 0, rs.getDocids()[0]);
		
		assertEquals(2, rs.getMetaKeys().length);
		assertEquals("doc1", rs.getMetaItems("filename")[0]);
		assertEquals("The quick brown fox jumps over the lazy dog", rs.getMetaItems("abstract")[0]);
	}
	
	@Test public void testOneDocumentEscape() throws Exception {
		Index index = createIndex();
		Manager m = new Manager(index);
		ResultSet rs; Decorate decorate; SearchRequest srq;
		
		srq = performQuery(m, "fox");
		rs = srq.getResultSet();
		assertEquals(1, rs.getResultSize());
		
		srq.setControl("escape", "abstract");
		decorate = new Decorate();
		decorate.new_query(m, srq, rs);
		decorate.filter(m, srq, rs, 0, rs.getDocids()[0]);
		assertEquals(2, rs.getMetaKeys().length);
		assertEquals("doc1", rs.getMetaItems("filename")[0]);
		assertEquals("The+quick+brown+fox+jumps+over+the+lazy+dog", rs.getMetaItems("abstract")[0]);
	}
	
	@Test public void testOneDocumentEmph() throws Exception {
		Index index = createIndex();
		Manager m = new Manager(index);
		ResultSet rs; Decorate decorate; SearchRequest srq;
		
		//postfilter
		srq = performQuery(m, "fox");
		rs = srq.getResultSet();
		assertEquals(1, rs.getResultSize());
		srq.setControl("emphasis", "abstract");
		decorate = new Decorate();
		decorate.new_query(m, srq, rs);
		decorate.filter(m, srq, rs, 0, rs.getDocids()[0]);
		assertEquals(3, rs.getMetaKeys().length);
		assertEquals("doc1", rs.getMetaItems("filename")[0]);
		assertEquals("The quick brown fox jumps over the lazy dog", rs.getMetaItems("abstract")[0]);
		assertEquals("The quick brown <b>fox</b> jumps over the lazy dog", rs.getMetaItems("abstract_emph")[0]);	
	}
	
	@Test public void testOneDocumentSummary() throws Exception {
		Index index = createIndex();
		Manager m = new Manager(index);
		ResultSet rs; Decorate decorate; SearchRequest srq;
		
		srq = performQuery(m, "lorem ipsum");
		rs = srq.getResultSet();
		assertEquals(1, rs.getResultSize());
		srq.setControl("emphasis", "abstract");
		srq.setControl("summaries", "abstract");
		decorate = new Decorate();
		decorate.new_query(m, srq, rs);
		decorate.filter(m, srq, rs, 0, rs.getDocids()[0]);
		assertEquals(3, rs.getMetaKeys().length);
		assertEquals("doc2", rs.getMetaItems("filename")[0]);
		assertTrue(rs.getMetaItems("abstract")[0].contains("Lorem Ipsum is simply dummy"));
		assertTrue(rs.getMetaItems("abstract_emph")[0].contains("<b>Lorem</b> <b>Ipsum</b> is simply dummy"));
	}
	
	@Test public void testOneDocumentSummaryField() throws Exception {
		Index index = createIndex();
		Manager m = new Manager(index);
		ResultSet rs; Decorate decorate; SearchRequest srq;
		
		srq = performQuery(m, "lorem ipsum");
		rs = srq.getResultSet();
		assertEquals(1, rs.getResultSize());
		srq.setControl("emphasis", "abstract");
		srq.setControl("summaries", "abstract");
		decorate = new Decorate();
		((Request)srq).setOriginalQuery("field:since");
		assertEquals("field:since", srq.getOriginalQuery());
		decorate.new_query(m, srq, rs);
		decorate.filter(m, srq, rs, 0, rs.getDocids()[0]);
		assertEquals(3, rs.getMetaKeys().length);
		assertEquals("doc2", rs.getMetaItems("filename")[0]);
		assertTrue(rs.getMetaItems("abstract")[0].startsWith("Lorem Ipsum has been the industry's standard dummy text ever since"));
		assertTrue(rs.getMetaItems("abstract_emph")[0].startsWith("Lorem Ipsum has been the industry's standard dummy text ever <b>since</b>"));
	}
	
	
}
