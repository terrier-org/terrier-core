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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;
import org.terrier.matching.matchops.MatchOpQLParser;
import org.terrier.matching.matchops.ANDQueryOp;
import org.terrier.matching.matchops.PhraseOp;
import org.terrier.matching.matchops.Operator;
import org.terrier.matching.matchops.SingleTermOp;
import org.terrier.matching.matchops.SynonymOp;
import org.terrier.matching.matchops.UnorderedWindowOp;

public class TestMatchOpQLParser {
	
	@Test public void testMultiTermsCombine() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("#combine(a b)").parseAll();
		assertNotNull(rtr);
		assertEquals(2, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleTermOp);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertTrue(rtr.get(1).getKey() instanceof SingleTermOp);
		assertEquals("b", rtr.get(1).getKey().toString());
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
		assertEquals("prox1", rtr.get(2).getValue().getTag());
		
		assertTrue(rtr.get(3).getKey() instanceof UnorderedWindowOp);
		assertEquals("#uw8(a b)", rtr.get(3).getKey().toString());
		assertEquals(0.05, rtr.get(3).getValue().getWeight(), 0d);
		assertEquals("prox8", rtr.get(3).getValue().getTag());
		
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
	
	@Test public void testMultiTermsBothTag() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("#tag(org a b)").parseAll();
		assertNotNull(rtr);
		assertEquals(2, rtr.size());
		
		assertTrue(rtr.get(0).getKey() instanceof SingleTermOp);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertEquals("org", rtr.get(0).getValue().tag);
		
		assertTrue(rtr.get(1).getKey() instanceof SingleTermOp);
		assertEquals("b", rtr.get(1).getKey().toString());
		assertEquals("org", rtr.get(1).getValue().tag);
	}
	
	@Test public void testMultiTermsTwoTags() throws Exception {		
		List<MatchingTerm> rtr = new MatchOpQLParser("#tag(org a b) #tag(per c d)").parseAll();
		assertNotNull(rtr);
		assertEquals(4, rtr.size());
		
		assertTrue(rtr.get(0).getKey() instanceof SingleTermOp);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertEquals("org", rtr.get(0).getValue().tag);
		
		assertTrue(rtr.get(1).getKey() instanceof SingleTermOp);
		assertEquals("b", rtr.get(1).getKey().toString());
		assertEquals("org", rtr.get(1).getValue().tag);
		
		assertTrue(rtr.get(2).getKey() instanceof SingleTermOp);
		assertEquals("c", rtr.get(2).getKey().toString());
		assertEquals("per", rtr.get(2).getValue().tag);
		
		assertTrue(rtr.get(3).getKey() instanceof SingleTermOp);
		assertEquals("d", rtr.get(3).getKey().toString());
		assertEquals("per", rtr.get(3).getValue().tag);
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
