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
 * The Original Code is TestMatchOpQLParser.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.matching.matchops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;

public class TestMatchOpQLParser {
	
	@Test public void testFuzzy() throws Exception {
		List<MatchingTerm> rtr;
		rtr = new MatchOpQLParser("#fuzzy(ab)").parseAll();
		assertNotNull(rtr);
		assertEquals(1, rtr.size());
		
		rtr = new MatchOpQLParser("#fuzzy:fuzziness=AUTO(ab)").parseAll();
		assertNotNull(rtr);
		assertEquals(1, rtr.size());
		
		rtr = new MatchOpQLParser("#fuzzy:fuzziness=AUTO.2.5(ab)").parseAll();
		assertNotNull(rtr);
		assertEquals(1, rtr.size());
		
		rtr = new MatchOpQLParser("#fuzzy:fuzziness=3:max_expansions=49:prefix_length=3(ab)").parseAll();
		assertNotNull(rtr);
		assertEquals(1, rtr.size());

	}
	
	@Test public void testMultiTermsCombine() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("#combine(a b)").parseAll();
		assertNotNull(rtr);
		assertEquals(2, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleTermOp);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertTrue(rtr.get(1).getKey() instanceof SingleTermOp);
		assertEquals("b", rtr.get(1).getKey().toString());
	}
	
	@Test public void testMultiTermsCombineWmodel() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("#combine:wmodel=PL2(a b)").parseAll();
		assertNotNull(rtr);
		assertEquals(2, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleTermOp);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertEquals(1, rtr.get(0).getValue().termModels.size());
		assertTrue(rtr.get(0).getValue().termModels.get(0).getClass().getSimpleName().equals("PL2"));
		
		assertTrue(rtr.get(1).getKey() instanceof SingleTermOp);
		assertEquals("b", rtr.get(1).getKey().toString());
		assertEquals(1, rtr.get(1).getValue().termModels.size());
		assertTrue(rtr.get(1).getValue().termModels.get(0).getClass().getSimpleName().equals("PL2"));

	}
	
	@Test public void testMultiTermsCombineWeights() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("#combine:0=0.85:1=0.1(a b)").parseAll();
		assertNotNull(rtr);
		assertEquals(2, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleTermOp);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertEquals(0.85, rtr.get(0).getValue().getWeight(), 0d);
		assertTrue(rtr.get(1).getKey() instanceof SingleTermOp);
		assertEquals("b", rtr.get(1).getKey().toString());
		assertEquals(0.1, rtr.get(1).getValue().getWeight(), 0d);
	}
	
	@Test public void testMultiTermsCombineOther() throws Exception {
		List<MatchingTerm> rtr = new MatchOpQLParser("#combine:0=0.85:1=0.1:tag=bla( #combine:req=true(a) #combine:req=false(b) c)").parseAll();
		assertNotNull(rtr);
		assertEquals(3, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleTermOp);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertEquals(0.85, rtr.get(0).getValue().getWeight(), 0d);
		assertTrue(rtr.get(0).getValue().getTags().contains("bla"));
		assertEquals(Boolean.TRUE, rtr.get(0).getValue().getRequired());
		
		assertTrue(rtr.get(1).getKey() instanceof SingleTermOp);
		assertEquals("b", rtr.get(1).getKey().toString());
		assertEquals(0.1, rtr.get(1).getValue().getWeight(), 0d);
		assertTrue(rtr.get(1).getValue().getTags().contains("bla"));
		assertEquals(Boolean.FALSE, rtr.get(1).getValue().getRequired());
		
		assertTrue(rtr.get(2).getKey() instanceof SingleTermOp);
		assertEquals("c", rtr.get(2).getKey().toString());
		assertEquals(1, rtr.get(2).getValue().getWeight(), 0d);
		assertTrue(rtr.get(2).getValue().getTags().contains("bla"));
		assertNull(rtr.get(2).getValue().getRequired());
	}
	
	@Test public void testMultiTermsCombineWeightsSD() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("#combine:0=0.85:1=0.1:2=0.05( #combine(a b) #1(a b) #uw8(a b))").parseAll();
		assertNotNull(rtr);
		assertEquals(4, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleTermOp);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertEquals(0.85, rtr.get(0).getValue().getWeight(), 0d);
		assertTrue(rtr.get(1).getKey() instanceof SingleTermOp);
		assertEquals("b", rtr.get(1).getKey().toString());
		assertEquals(0.85, rtr.get(1).getValue().getWeight(), 0d);
		
		assertTrue(rtr.get(2).getKey() instanceof PhraseOp);
		assertEquals("#1(a b)", rtr.get(2).getKey().toString());
		assertEquals(0.1, rtr.get(2).getValue().getWeight(), 0d);
		
		assertTrue(rtr.get(3).getKey() instanceof UnorderedWindowOp);
		assertEquals("#uw8(a b)", rtr.get(3).getKey().toString());
		assertEquals(0.05, rtr.get(3).getValue().getWeight(), 0d);
	}
	
	@Test public void testMultiTermsCombineWeightsSDTagged() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("#combine:0=0.85:1=0.1:2=0.05( #combine(a b) #tag(prox1 #1(a b)) #tag(prox8 #uw8(a b)))").parseAll();
		assertNotNull(rtr);
		assertEquals(4, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleTermOp);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertEquals(0.85, rtr.get(0).getValue().getWeight(), 0d);
		assertTrue(rtr.get(1).getKey() instanceof SingleTermOp);
		assertEquals("b", rtr.get(1).getKey().toString());
		assertEquals(0.85, rtr.get(1).getValue().getWeight(), 0d);
		
		assertTrue(rtr.get(2).getKey() instanceof PhraseOp);
		assertEquals("#1(a b)", rtr.get(2).getKey().toString());
		assertEquals(0.1, rtr.get(2).getValue().getWeight(), 0d);
		assertTrue(rtr.get(2).getValue().getTags().contains("prox1"));
		
		assertTrue(rtr.get(3).getKey() instanceof UnorderedWindowOp);
		assertEquals("#uw8(a b)", rtr.get(3).getKey().toString());
		assertEquals(0.05, rtr.get(3).getValue().getWeight(), 0d);
		assertTrue(rtr.get(3).getValue().getTags().contains("prox8"));
		
	}
	
	@Test public void testMultiTerms() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("a b").parseAll();
		assertNotNull(rtr);
		assertEquals(2, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleTermOp);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertTrue(rtr.get(1).getKey() instanceof SingleTermOp);
		assertEquals("b", rtr.get(1).getKey().toString());
	}
	
	@Test public void testPrefix() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("#prefix(a)").parseAll();
		assertNotNull(rtr);
		assertEquals(1, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof PrefixTermOp);
	}
	
	@Test public void testBase64() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("#base64(YQ==)").parseAll();
		assertNotNull(rtr);
		assertEquals(1, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleTermOp);
		assertEquals("a", rtr.get(0).getKey().toString());
	}
	
	@Test public void testBase64Field() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("#base64(YQ==).field").parseAll();
		assertNotNull(rtr);
		assertEquals(1, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleTermOp);
		assertEquals("a.field", rtr.get(0).getKey().toString());
	}
	
	@Test public void testBase64Nested() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("#uw2( b #base64(YQ==))").parseAll();
		assertNotNull(rtr);
		assertEquals(1, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof UnorderedWindowOp);
		Operator[] within = ((UnorderedWindowOp) rtr.get(0).getKey()).getConstituents();
		
		assertTrue(within[0] instanceof SingleTermOp);
		assertEquals("b", within[0].toString());
		assertTrue(within[1] instanceof SingleTermOp);
		assertEquals("a", within[1].toString());
	}
	
	@Test public void testMultiTermsBothTag() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("#tag(org a b)").parseAll();
		assertNotNull(rtr);
		assertEquals(2, rtr.size());
		
		assertTrue(rtr.get(0).getKey() instanceof SingleTermOp);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertTrue(rtr.get(1).getValue().getTags().contains("org"));
		
		assertTrue(rtr.get(1).getKey() instanceof SingleTermOp);
		assertEquals("b", rtr.get(1).getKey().toString());
		assertTrue(rtr.get(1).getValue().getTags().contains("org"));
	}
	
	@Test public void testMultiTermsTwoTags() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("#tag(org a b) #tag(per c d)").parseAll();
		assertNotNull(rtr);
		assertEquals(4, rtr.size());
		
		assertTrue(rtr.get(0).getKey() instanceof SingleTermOp);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertTrue(rtr.get(0).getValue().getTags().contains("org"));
		
		assertTrue(rtr.get(1).getKey() instanceof SingleTermOp);
		assertEquals("b", rtr.get(1).getKey().toString());
		assertTrue(rtr.get(1).getValue().getTags().contains("org"));
		
		assertTrue(rtr.get(2).getKey() instanceof SingleTermOp);
		assertEquals("c", rtr.get(2).getKey().toString());
		assertTrue(rtr.get(2).getValue().getTags().contains("per"));
		
		assertTrue(rtr.get(3).getKey() instanceof SingleTermOp);
		assertEquals("d", rtr.get(3).getKey().toString());
		assertTrue(rtr.get(3).getValue().getTags().contains("per"));
	}

	@Test public void testOneSimpleTerms() throws Exception {		
		Operator rtr = new MatchOpQLParser("a").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof SingleTermOp);
		assertEquals("a", rtr.toString());		
	}
	
	@Test public void testOneSimpleField() throws Exception {		
		Operator rtr = new MatchOpQLParser("a.field").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof SingleTermOp);
		assertEquals("a.field", rtr.toString());
		assertEquals("a",((SingleTermOp)rtr).getTerm());
		assertEquals("field",((SingleTermOp)rtr).getField());
		
	}
	
	@Test public void testOneBand() throws Exception {		
		Operator rtr = new MatchOpQLParser("#band(a b)").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof ANDQueryOp);
		assertEquals("#band(a b)", rtr.toString());
	}
	
	@Test public void testOneSynTerms() throws Exception {		
		Operator rtr = new MatchOpQLParser("#syn(a b)").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof SynonymOp);
		assertEquals("#syn(a b)", rtr.toString());		
	}

	
	@Test public void testOneUwTerms() throws Exception {		
		Operator rtr = new MatchOpQLParser("#uw1(a b)").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof UnorderedWindowOp);
		assertEquals(1, ((UnorderedWindowOp)rtr).distance);
		assertEquals("#uw1(a b)", rtr.toString());		
	}
	
	@Test public void testOnePhraseTerms_implicit() throws Exception {		
		Operator rtr = new MatchOpQLParser("#1(a b)").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof PhraseOp);
		assertEquals("#1(a b)", rtr.toString());		
	}
	
	@Test public void testOnePhraseTerms() throws Exception {		
		Operator rtr = new MatchOpQLParser("#ow1(a b)").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof PhraseOp);
		assertEquals("#1(a b)", rtr.toString());		
	}
	
	@Test public void testOneRecursionSyn() throws Exception {		
		Operator rtr = new MatchOpQLParser("#1(a #syn(b c))").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof PhraseOp);
		assertEquals("#1(a #syn(b c))", rtr.toString());		
	}
	
	@Test public void testOneRecursionFields() throws Exception {		
		Operator rtr = new MatchOpQLParser("#1(a.field b.field)").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof PhraseOp);
		assertEquals("#1(a.field b.field)", rtr.toString());		
	}
	
}
