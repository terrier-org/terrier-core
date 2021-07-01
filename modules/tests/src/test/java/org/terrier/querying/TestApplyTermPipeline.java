package org.terrier.querying;

import static org.junit.Assert.*;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.matchops.SingleTermOp;
import org.terrier.querying.parser.Query.QTPBuilder;
import org.terrier.structures.Index;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

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

	public void testMixedStemming() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		Index indexNoStem = IndexTestUtils.makeIndex(new String[]{"doc1"}, new String[]{"chemicals"});
		ApplicationSetup.setProperty("termpipelines", "PorterStemmer");
		Index indexStem = IndexTestUtils.makeIndex(new String[]{"doc1"}, new String[]{"chemicals"});

		Manager mNS = new LocalManager(indexNoStem);
		Manager mS = new LocalManager(indexStem);

		SearchRequest srq;
		srq = mNS.newSearchRequest("testQuery", "chemical");
		mNS.runSearchRequest(srq);
		assertEquals(0, srq.getResults().size());

		srq = mNS.newSearchRequest("testQuery", "chemical");
		mS.runSearchRequest(srq);
		assertEquals(1, srq.getResults().size());	
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
