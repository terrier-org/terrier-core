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
 * The Original Code is TestQueryParser.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.querying.parser;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.querying.parser.Query.QueryTermsParameter;

import com.google.common.collect.Sets;

public class TestQueryParser {

	static final boolean checkParseTree = false;
	
	public TestQueryParser()
	{
	}
	
	@Ignore @Test public void testEmptyQuery() throws Exception
	{
		Query q = QueryParser.parseQuery("");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(0, terms.size());
	}
	

	@Test public void testControls() throws Exception
	{
		Query q = QueryParser.parseQuery("a end:10 qemodel:KL");
		Map<String,String> controlKV = new HashMap<>();
		q.obtainControls(Sets.newHashSet("end", "qemodel", "start"), controlKV);
		assertTrue(controlKV.containsKey("end"));
		assertTrue(controlKV.containsKey("qemodel"));
		assertFalse(controlKV.containsKey("start"));
		assertEquals("KL",controlKV.get("qemodel"));
	}
	
	@Test public void testSingleTermQuery() throws Exception
	{
		Query q = QueryParser.parseQuery("a");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(1, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		if (checkParseTree) assertEquals("SingleTermQuery(a)", q.parseTree());
		assertEquals("a", q.toString());
	}
	
	@Test public void testSingleTermQueryWeighted() throws Exception
	{
		Query q = QueryParser.parseQuery("a^2");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(1, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		if (checkParseTree) assertEquals("SingleTermQuery(a)", q.parseTree());
		assertEquals("a^2.0", q.toString());
	}
	
	@Test public void testMultipleWithWeights() throws Exception
	{
		final String term1 = "a";
		final String term2 = "b";
		QueryParser.parseQuery(term1+"^0.5 " + term2 + "^1.0");
	}
	
	
	@Test public void testSingleTermQueryWeightedFloat() throws Exception
	{
		Query q = QueryParser.parseQuery("a^1.5");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(1, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		if (checkParseTree) assertEquals("SingleTermQuery(a)", q.parseTree());
		assertEquals("a^1.5", q.toString());
	}
	
//	@Test public void testSingleTermSegmentQuery() throws Exception
//	{
//		Query q = QueryParser.parseQuery("[a]");
//		List<Query> terms = new ArrayList<Query>();
//		q.getTerms(terms);
//		assertEquals(1, terms.size());
//		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
//		if (checkParseTree) assertEquals("SegmentQuery(SingleTermQuery(a))", q.parseTree());
//		assertEquals("[a]", q.toString());
//	}
	
	
	@Test public void testTwoTermQuery() throws Exception
	{
		Query q = QueryParser.parseQuery("a b");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(2, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		if (checkParseTree) assertEquals("MultiTermQuery(SingleTermQuery(a),SingleTermQuery(b))", q.parseTree());
		assertEquals("a b", q.toString());
	}
	
	@Test public void testTwoTermDisjunctiveQuery() throws Exception
	{
		Query q = QueryParser.parseQuery("{a b}");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(2, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		if (checkParseTree) assertEquals("DisjunctiveQuery(SingleTermQuery(a),SingleTermQuery(b))", q.parseTree());
		assertEquals("{a b}", q.toString());
	}
	
	@Test public void testTwoTermDisjunctiveQueryWithWeight() throws Exception
	{
		Query q = QueryParser.parseQuery("{a b}^1.5");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(2, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		if (checkParseTree) assertEquals("DisjunctiveQuery(SingleTermQuery(a),SingleTermQuery(b))", q.parseTree());
		assertEquals("{a b}^1.5", q.toString());
	}
	
//	@Test public void testTwoTermQuerySegment() throws Exception
//	{
//		Query q = QueryParser.parseQuery("[a b]");
//		List<Query> terms = new ArrayList<Query>();
//		q.getTerms(terms);
//		assertEquals(2, terms.size());
//		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
//		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
//		if (checkParseTree) assertEquals("SegmentQuery(SingleTermQuery(a),SingleTermQuery(b))", q.parseTree());
//		assertEquals("[a b]", q.toString());
//	}
	
//	@Test public void testThreeTermQuerySegmentLast() throws Exception
//	{
//		Query q = QueryParser.parseQuery("a [b c]");
//		List<Query> terms = new ArrayList<Query>();
//		q.getTerms(terms);
//		assertEquals(3, terms.size());
//		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
//		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
//		assertEquals("c", ((SingleTermQuery)terms.get(2)).getTerm());
//		if (checkParseTree) assertEquals("MultiTermQuery(SingleTermQuery(a),SegmentQuery(SingleTermQuery(b),SingleTermQuery(c)))", q.parseTree());
//		assertEquals("a [b c]", q.toString());
//	}
	
//	@Test public void testThreeTermQuerySegment2_3() throws Exception
//	{
//		Query q = QueryParser.parseQuery("[a] [b c]");
//		List<Query> terms = new ArrayList<Query>();
//		q.getTerms(terms);
//		assertEquals(3, terms.size());
//		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
//		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
//		assertEquals("c", ((SingleTermQuery)terms.get(2)).getTerm());
//		if (checkParseTree) assertEquals("MultiTermQuery(SegmentQuery(SingleTermQuery(a)),SegmentQuery(SingleTermQuery(b),SingleTermQuery(c)))", q.parseTree());
//		assertEquals("[a] [b c]", q.toString());
//	}
//	
//	
//	@Test public void testThreeTermDisjunctiveSegmentFirst() throws Exception
//	{
//		Query q = QueryParser.parseQuery("{a b} c");
//		List<Query> terms = new ArrayList<Query>();
//		q.getTerms(terms);
//		assertEquals(3, terms.size());
//		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
//		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
//		if (checkParseTree) assertEquals("MultiTermQuery(SegmentQuery(SingleTermQuery(a),SingleTermQuery(b)),SingleTermQuery(c))", q.parseTree());
//		assertEquals("{a b} c", q.toString());
//	}
	
	@Test public void testThreeTermQueryDisjunctiveLast() throws Exception
	{
		Query q = QueryParser.parseQuery("a {b c}");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(3, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		assertEquals("c", ((SingleTermQuery)terms.get(2)).getTerm());
		if (checkParseTree) assertEquals("MultiTermQuery(SingleTermQuery(a),DisjunctiveQuery(SingleTermQuery(b),SingleTermQuery(c)))", q.parseTree());
		assertEquals("a {b c}", q.toString());
	}
	
//	@Test public void testThreeTermQuerySegmentFirst() throws Exception
//	{
//		Query q = QueryParser.parseQuery("[a b] c");
//		List<Query> terms = new ArrayList<Query>();
//		q.getTerms(terms);
//		assertEquals(3, terms.size());
//		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
//		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
//		assertEquals("c", ((SingleTermQuery)terms.get(2)).getTerm());
//		//if (checkParseTree) assertEquals("MultiTermQuery(SegmentQuery(SingleTermQuery(a),SingleTermQuery(b)),SingleTermQuery(c))", q.parseTree());
//		assertEquals("[a b] c", q.toString());
//	}
	
//	@Test public void testFourTermQuerySegmentWithDisjunctive() throws Exception
//	{
//		Query q = QueryParser.parseQuery("[{a1 a2} b] c");
//		List<Query> terms = new ArrayList<Query>();
//		q.getTerms(terms);
//		assertEquals(4, terms.size());
//		assertEquals("a1", ((SingleTermQuery)terms.get(0)).getTerm());
//		assertEquals("a2", ((SingleTermQuery)terms.get(1)).getTerm());
//		assertEquals("b", ((SingleTermQuery)terms.get(2)).getTerm());
//		assertEquals("c", ((SingleTermQuery)terms.get(3)).getTerm());
//		//if (checkParseTree) assertEquals("MultiTermQuery(SegmentQuery(SingleTermQuery(a),SingleTermQuery(b)),SingleTermQuery(c))", q.parseTree());
//		assertEquals("[{a1 a2} b] c", q.toString());
//	}
	
	@Test public void testTwoTermWeight() throws Exception
	{
		Query q = QueryParser.parseQuery("a b^2");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(2, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		assertEquals("a b^2.0", q.toString());
	}
	
	
	@Test public void testTwoTermWeightExplicitMultiTerm() throws Exception
	{
		Query q = QueryParser.parseQuery("(a b)^2");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(2, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		assertEquals("(a b)^2.0", q.toString());
		
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		q.obtainQueryTerms(QueryTermsParameter.of(mqt, true));
		assertEquals(2, mqt.getTerms().length);
		assertEquals("a", mqt.getTerms()[0]);
		assertEquals("b", mqt.getTerms()[1]);
		assertEquals(2d, mqt.getTermWeights()[0], 0);
		assertEquals(2d, mqt.getTermWeights()[1], 0);
		
	}
	
	
	
	
	@Test public void testTwoTermQueryRequirement() throws Exception
	{
		Query q = QueryParser.parseQuery("a -b");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(2, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		assertEquals("a -b", q.toString());
	}
	
	@Test public void testTwoTermQueryRevOrder() throws Exception
	{
		Query q = QueryParser.parseQuery("b a");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(2, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(1)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b a", q.toString());
	}
	
	@Test public void testTwoTermQueryPhrase() throws Exception
	{
		Query q = QueryParser.parseQuery("\"a b\"");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(2, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		List<Query> phrases = new ArrayList<Query>();		
		q.obtainAllOf(PhraseQuery.class, phrases);
		assertEquals(1, phrases.size());
		assertEquals("\"a b\"", q.toString());
	}
	
	@Test public void testTwoTermQueryField() throws Exception
	{
		Query q = QueryParser.parseQuery("a FIELD:b");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(2, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		assertEquals("a FIELD:b", q.toString());
	}
	
	@Test public void testTwoTermQueryFieldGroup() throws Exception
	{
		Query q = QueryParser.parseQuery("FIELD:(a b)");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(2, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		assertEquals("FIELD:(a b)", q.toString());
	}
	
	@Test public void testTwoTermQueryFieldGroupWithWeights() throws Exception
	{
		Query q = QueryParser.parseQuery("FIELD:(a b)^2");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(2, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		assertEquals("FIELD:(a b)^2.0", q.toString());
		

		MatchingQueryTerms mqt = new MatchingQueryTerms();
		q.obtainQueryTerms(new QueryTermsParameter(mqt, null, null, null));
		assertEquals(2, mqt.getTerms().length);
		assertEquals("a.FIELD", mqt.getTerms()[0]);
		assertEquals("b.FIELD", mqt.getTerms()[1]);
		assertEquals(2d, mqt.getTermWeights()[0], 0);
		assertEquals(2d, mqt.getTermWeights()[1], 0);
	}
	
	@Test public void testTwoTermQueryFieldPhrase() throws Exception
	{
		Query q = QueryParser.parseQuery("FIELD:\"a b\"");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(2, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		List<Query> phrases = new ArrayList<Query>();		
		q.obtainAllOf(PhraseQuery.class, phrases);
		assertEquals(1, phrases.size());
		assertEquals("\"a b\"", phrases.get(0).toString());
		assertEquals("FIELD:\"a b\"", q.toString());
	}
	
	@Test public void testThreeTermQueryPhrase() throws Exception
	{
		Query q = QueryParser.parseQuery("\"a b\" c");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(3, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		assertEquals("c", ((SingleTermQuery)terms.get(2)).getTerm());
		List<Query> phrases = new ArrayList<Query>();		
		q.obtainAllOf(PhraseQuery.class, phrases);
		assertEquals(1, phrases.size());
		assertEquals("\"a b\"", phrases.get(0).toString());
		assertEquals("\"a b\" c", q.toString());
	}
	
	@Test public void testThreeTermQueryRequirementPhrase() throws Exception
	{
		Query q = QueryParser.parseQuery("-\"a b\" c");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals(3, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		assertEquals("c", ((SingleTermQuery)terms.get(2)).getTerm());
		List<Query> phrases = new ArrayList<Query>();		
		q.obtainAllOf(PhraseQuery.class, phrases);
		assertEquals(1, phrases.size());
		assertEquals("\"a b\"", phrases.get(0).toString());
		assertEquals("-\"a b\" c", q.toString());
	}
	
	@Test public void testFourTermQuery2Phrase() throws Exception
	{
		Query q = QueryParser.parseQuery("\"a b\" \"c d\"");
		List<Query> terms = new ArrayList<Query>();
		q.getTerms(terms);
		assertEquals("\"a b\" \"c d\"", q.toString());
		assertEquals(4, terms.size());
		assertEquals("a", ((SingleTermQuery)terms.get(0)).getTerm());
		assertEquals("b", ((SingleTermQuery)terms.get(1)).getTerm());
		assertEquals("c", ((SingleTermQuery)terms.get(2)).getTerm());
		assertEquals("d", ((SingleTermQuery)terms.get(3)).getTerm());
		List<Query> phrases = new ArrayList<Query>();		
		q.obtainAllOf(PhraseQuery.class, phrases);
		assertEquals(2, phrases.size());
		assertEquals("\"a b\"", phrases.get(0).toString());
		assertEquals("\"c d\"", phrases.get(1).toString());
		
	}
	
}
