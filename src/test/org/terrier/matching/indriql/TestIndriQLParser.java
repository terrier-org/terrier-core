package org.terrier.matching.indriql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;

public class TestIndriQLParser {
	
	@Test public void testMultiTermsCombine() throws Exception {		
		List<MatchingTerm> rtr = new IndriQLParser("#combine(a b)").parseAll();
		assertNotNull(rtr);
		assertEquals(2, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleQueryTerm);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertTrue(rtr.get(1).getKey() instanceof SingleQueryTerm);
		assertEquals("b", rtr.get(1).getKey().toString());
	}
	
	@Test public void testMultiTermsCombineWeights() throws Exception {		
		List<MatchingTerm> rtr = new IndriQLParser("#combine:0=0.85:1=0.1(a b)").parseAll();
		assertNotNull(rtr);
		assertEquals(2, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleQueryTerm);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertEquals(0.85, rtr.get(0).getValue().getWeight(), 0d);
		assertTrue(rtr.get(1).getKey() instanceof SingleQueryTerm);
		assertEquals("b", rtr.get(1).getKey().toString());
		assertEquals(0.1, rtr.get(1).getValue().getWeight(), 0d);
	}
	
	@Test public void testMultiTermsCombineWeightsSD() throws Exception {		
		List<MatchingTerm> rtr = new IndriQLParser("#combine:0=0.85:1=0.1:2=0.05( #combine(a b) #1(a b) #uw8(a b))").parseAll();
		assertNotNull(rtr);
		assertEquals(4, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleQueryTerm);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertEquals(0.85, rtr.get(0).getValue().getWeight(), 0d);
		assertTrue(rtr.get(1).getKey() instanceof SingleQueryTerm);
		assertEquals("b", rtr.get(1).getKey().toString());
		assertEquals(0.85, rtr.get(1).getValue().getWeight(), 0d);
		
		assertTrue(rtr.get(2).getKey() instanceof PhraseTerm);
		assertEquals("#1(a b)", rtr.get(2).getKey().toString());
		assertEquals(0.1, rtr.get(2).getValue().getWeight(), 0d);
		
		assertTrue(rtr.get(3).getKey() instanceof UnorderedWindowTerm);
		assertEquals("#uw8(a b)", rtr.get(3).getKey().toString());
		assertEquals(0.05, rtr.get(3).getValue().getWeight(), 0d);
	}
	
	@Test public void testMultiTermsCombineWeightsSDTagged() throws Exception {		
		List<MatchingTerm> rtr = new IndriQLParser("#combine:0=0.85:1=0.1:2=0.05( #combine(a b) #tag(prox1 #1(a b)) #tag(prox8 #uw8(a b)))").parseAll();
		assertNotNull(rtr);
		assertEquals(4, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleQueryTerm);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertEquals(0.85, rtr.get(0).getValue().getWeight(), 0d);
		assertTrue(rtr.get(1).getKey() instanceof SingleQueryTerm);
		assertEquals("b", rtr.get(1).getKey().toString());
		assertEquals(0.85, rtr.get(1).getValue().getWeight(), 0d);
		
		assertTrue(rtr.get(2).getKey() instanceof PhraseTerm);
		assertEquals("#1(a b)", rtr.get(2).getKey().toString());
		assertEquals(0.1, rtr.get(2).getValue().getWeight(), 0d);
		assertEquals("prox1", rtr.get(2).getValue().getTag());
		
		assertTrue(rtr.get(3).getKey() instanceof UnorderedWindowTerm);
		assertEquals("#uw8(a b)", rtr.get(3).getKey().toString());
		assertEquals(0.05, rtr.get(3).getValue().getWeight(), 0d);
		assertEquals("prox8", rtr.get(3).getValue().getTag());
		
	}
	
	@Test public void testMultiTerms() throws Exception {		
		List<MatchingTerm> rtr = new IndriQLParser("a b").parseAll();
		assertNotNull(rtr);
		assertEquals(2, rtr.size());
		assertTrue(rtr.get(0).getKey() instanceof SingleQueryTerm);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertTrue(rtr.get(1).getKey() instanceof SingleQueryTerm);
		assertEquals("b", rtr.get(1).getKey().toString());
	}
	
	@Test public void testMultiTermsBothTag() throws Exception {		
		List<MatchingTerm> rtr = new IndriQLParser("#tag(org a b)").parseAll();
		assertNotNull(rtr);
		assertEquals(2, rtr.size());
		
		assertTrue(rtr.get(0).getKey() instanceof SingleQueryTerm);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertEquals("org", rtr.get(0).getValue().tag);
		
		assertTrue(rtr.get(1).getKey() instanceof SingleQueryTerm);
		assertEquals("b", rtr.get(1).getKey().toString());
		assertEquals("org", rtr.get(1).getValue().tag);
	}
	
	@Test public void testMultiTermsTwoTags() throws Exception {		
		List<MatchingTerm> rtr = new IndriQLParser("#tag(org a b) #tag(per c d)").parseAll();
		assertNotNull(rtr);
		assertEquals(4, rtr.size());
		
		assertTrue(rtr.get(0).getKey() instanceof SingleQueryTerm);
		assertEquals("a", rtr.get(0).getKey().toString());
		assertEquals("org", rtr.get(0).getValue().tag);
		
		assertTrue(rtr.get(1).getKey() instanceof SingleQueryTerm);
		assertEquals("b", rtr.get(1).getKey().toString());
		assertEquals("org", rtr.get(1).getValue().tag);
		
		assertTrue(rtr.get(2).getKey() instanceof SingleQueryTerm);
		assertEquals("c", rtr.get(2).getKey().toString());
		assertEquals("per", rtr.get(2).getValue().tag);
		
		assertTrue(rtr.get(3).getKey() instanceof SingleQueryTerm);
		assertEquals("d", rtr.get(3).getKey().toString());
		assertEquals("per", rtr.get(3).getValue().tag);
	}

	@Test public void testOneSimpleTerms() throws Exception {		
		QueryTerm rtr = new IndriQLParser("a").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof SingleQueryTerm);
		assertEquals("a", rtr.toString());		
	}
	
	@Test public void testOneSimpleField() throws Exception {		
		QueryTerm rtr = new IndriQLParser("a.field").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof SingleQueryTerm);
		assertEquals("a.field", rtr.toString());
		assertEquals("a",((SingleQueryTerm)rtr).getTerm());
		assertEquals("field",((SingleQueryTerm)rtr).getField());
		
	}
	
	@Test public void testOneBand() throws Exception {		
		QueryTerm rtr = new IndriQLParser("#band(a b)").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof ANDQueryTerm);
		assertEquals("#band(a b)", rtr.toString());
	}
	
	@Test public void testOneSynTerms() throws Exception {		
		QueryTerm rtr = new IndriQLParser("#syn(a b)").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof SynonymTerm);
		assertEquals("#syn(a b)", rtr.toString());		
	}

	
	@Test public void testOneUwTerms() throws Exception {		
		QueryTerm rtr = new IndriQLParser("#uw1(a b)").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof UnorderedWindowTerm);
		assertEquals(1, ((UnorderedWindowTerm)rtr).distance);
		assertEquals("#uw1(a b)", rtr.toString());		
	}
	
	@Test public void testOnePhraseTerms_implicit() throws Exception {		
		QueryTerm rtr = new IndriQLParser("#1(a b)").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof PhraseTerm);
		assertEquals("#1(a b)", rtr.toString());		
	}
	
	@Test public void testOnePhraseTerms() throws Exception {		
		QueryTerm rtr = new IndriQLParser("#ow1(a b)").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof PhraseTerm);
		assertEquals("#1(a b)", rtr.toString());		
	}
	
	@Test public void testOneRecursionSyn() throws Exception {		
		QueryTerm rtr = new IndriQLParser("#1(a #syn(b c))").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof PhraseTerm);
		assertEquals("#1(a #syn(b c))", rtr.toString());		
	}
	
	@Test public void testOneRecursionFields() throws Exception {		
		QueryTerm rtr = new IndriQLParser("#1(a.field b.field)").parse().getKey();
		assertNotNull(rtr);
		assertTrue(rtr instanceof PhraseTerm);
		assertEquals("#1(a.field b.field)", rtr.toString());		
	}
	
}
