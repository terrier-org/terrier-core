package org.terrier.matching.indriql;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestIndriQLParser {

	@Test public void testSimpleTerms() throws Exception {		
		QueryTerm rtr = new IndriQLParser("a").parse();
		assertNotNull(rtr);
		assertTrue(rtr instanceof SingleQueryTerm);
		assertEquals("a", rtr.toString());		
	}
	
	@Test public void testSimpleField() throws Exception {		
		QueryTerm rtr = new IndriQLParser("a.field").parse();
		assertNotNull(rtr);
		assertTrue(rtr instanceof SingleQueryTerm);
		assertEquals("a.field", rtr.toString());
		assertEquals("a",((SingleQueryTerm)rtr).getTerm());
		assertEquals("field",((SingleQueryTerm)rtr).getField());
		
	}
	
	@Test public void testBand() throws Exception {		
		QueryTerm rtr = new IndriQLParser("#band(a b)").parse();
		assertNotNull(rtr);
		assertTrue(rtr instanceof ANDQueryTerm);
		assertEquals("#band(a b)", rtr.toString());
	}
	
	@Test public void testSynTerms() throws Exception {		
		QueryTerm rtr = new IndriQLParser("#syn(a b)").parse();
		assertNotNull(rtr);
		assertTrue(rtr instanceof SynonymTerm);
		assertEquals("#syn(a b)", rtr.toString());		
	}
	
//	@Test public void testSynTermsComma() throws Exception {		
//		QueryTerm rtr = new IndriQLParser("#syn(a,b)").parse();
//		assertNotNull(rtr);
//		assertTrue(rtr instanceof SynonymTerm);
//		assertEquals("#syn(a b)", rtr.toString());		
//	}

	
	@Test public void testUwTerms() throws Exception {		
		QueryTerm rtr = new IndriQLParser("#uw1(a b)").parse();
		assertNotNull(rtr);
		assertTrue(rtr instanceof UnorderedWindowTerm);
		assertEquals(1, ((UnorderedWindowTerm)rtr).distance);
		assertEquals("#uw1(a b)", rtr.toString());		
	}
	
	@Test public void testPhraseTerms_implicit() throws Exception {		
		QueryTerm rtr = new IndriQLParser("#1(a b)").parse();
		assertNotNull(rtr);
		assertTrue(rtr instanceof PhraseTerm);
		assertEquals("#1(a b)", rtr.toString());		
	}
	
	@Test public void testPhraseTerms() throws Exception {		
		QueryTerm rtr = new IndriQLParser("#ow1(a b)").parse();
		assertNotNull(rtr);
		assertTrue(rtr instanceof PhraseTerm);
		assertEquals("#1(a b)", rtr.toString());		
	}
	
	@Test public void testRecursionSyn() throws Exception {		
		QueryTerm rtr = new IndriQLParser("#1(a #syn(b c))").parse();
		assertNotNull(rtr);
		assertTrue(rtr instanceof PhraseTerm);
		assertEquals("#1(a #syn(b c))", rtr.toString());		
	}
	
	@Test public void testRecursionFields() throws Exception {		
		QueryTerm rtr = new IndriQLParser("#1(a.field b.field)").parse();
		assertNotNull(rtr);
		assertTrue(rtr instanceof PhraseTerm);
		assertEquals("#1(a.field b.field)", rtr.toString());		
	}
	
}
