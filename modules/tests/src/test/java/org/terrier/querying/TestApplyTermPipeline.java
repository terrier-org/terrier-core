package org.terrier.querying;

import static org.junit.Assert.*;

import org.junit.Test;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.matchops.SingleTermOp;
import org.terrier.querying.parser.Query.QTPBuilder;
import org.terrier.tests.ApplicationSetupBasedTest;

public class TestApplyTermPipeline extends ApplicationSetupBasedTest {

	@Test public void testSingleTermStem()
	{
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.add(QTPBuilder.of(new SingleTermOp("driving")).build());
		Request rq = new Request();
		rq.setMatchingQueryTerms(mqt);
		new ApplyTermPipeline().process(null, rq);
		assertEquals(1, mqt.size());
		assertEquals("drive", mqt.get(0).getKey().toString());
	}
	
	@Test public void testStopword()
	{
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.add(QTPBuilder.of(new SingleTermOp("i")).build());
		Request rq = new Request();
		rq.setMatchingQueryTerms(mqt);
		new ApplyTermPipeline().process(null, rq);
		assertEquals(0, mqt.size());
	}
	
	@Test public void testMultipleStemsEquals()
	{
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.add(QTPBuilder.of(new SingleTermOp("driving")).build());
		mqt.add(QTPBuilder.of(new SingleTermOp("drive")).build());
		assertEquals(2, mqt.size());
		Request rq = new Request();
		rq.setMatchingQueryTerms(mqt);
		new ApplyTermPipeline().process(null, rq);
		assertEquals(1, mqt.size());
		assertEquals("drive", mqt.get(0).getKey().toString());
		assertEquals(2d, mqt.get(0).getValue().weight, 0.0d);
	}
	
	@Test public void testMultipleStemsNotEqual()
	{
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.add(QTPBuilder.of(new SingleTermOp("driving")).build());
		mqt.add(QTPBuilder.of(new SingleTermOp("drive")).setRequired(true).build());
		assertEquals(2, mqt.size());
		Request rq = new Request();
		rq.setMatchingQueryTerms(mqt);
		new ApplyTermPipeline().process(null, rq);
		assertEquals(2, mqt.size());
		assertEquals("drive", mqt.get(0).getKey().toString());
		assertEquals("drive", mqt.get(1).getKey().toString());
		assertEquals(1d, mqt.get(0).getValue().weight, 0.0d);
		assertEquals(1d, mqt.get(1).getValue().weight, 0.0d);
		assertNotNull(mqt.get(1).getValue().getRequired());
		assertTrue(mqt.get(1).getValue().getRequired());
	}
	
}
