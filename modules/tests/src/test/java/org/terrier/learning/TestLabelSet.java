package org.terrier.learning;

import static org.junit.Assert.*;

import org.junit.Test;
import org.terrier.matching.QueryResultSet;
import org.terrier.matching.ResultSet;
import org.terrier.querying.Request;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestLabelSet extends ApplicationSetupBasedTest {

	@Test public void testFRS() throws Exception
	{
		String qrelsfile = writeTemporaryFile("test.qrels", new String[]{"1 Q0 doc15 4"});		
		
		FeaturedResultSet frs = new FeaturedQueryResultSet(2);
		frs.getDocids()[0] = 10;
		frs.getDocids()[1] = 15;
		frs.getScores()[0] = 10d;
		frs.getScores()[1] = 8d;
		frs.putFeatureScores("QI:PR", new double[]{9d, 7d});
		frs.addMetaItems("docno", new String[]{"doc10", "doc15"});
		ApplicationSetup.setProperty("learning.labels.file", qrelsfile);
		Request rq = new Request();
		rq.setQueryID("1");
		rq.setResultSet(frs);
		LabelDecorator ld = new LabelDecorator();
		ld.process(null, rq);
		
		assertEquals("0", frs.getDefaultLabel());
		assertArrayEquals(new String[]{"0", "4"}, frs.getLabels());
	}
	
	@Test public void testNotFRS() throws Exception
	{
		String qrelsfile = writeTemporaryFile("test.qrels", new String[]{"1 Q0 doc15 4"});		
		
		ResultSet rs = new QueryResultSet(2);
		rs.getDocids()[0] = 10;
		rs.getDocids()[1] = 15;
		rs.getScores()[0] = 10d;
		rs.getScores()[1] = 8d;
		rs.addMetaItems("docno", new String[]{"doc10", "doc15"});
		assertNotNull(rs.getMetaItems("docno"));
		assertTrue(rs.hasMetaItems("docno"));
		ApplicationSetup.setProperty("learning.labels.file", qrelsfile);
		Request rq = new Request();
		rq.setQueryID("1");
		rq.setResultSet(rs);
		LabelDecorator ld = new LabelDecorator();
		ld.process(null, rq);
		
		assertNotNull(rq.getResultSet());
		assertTrue(rq.getResultSet() instanceof FeaturedResultSet);
		FeaturedResultSet frs = (FeaturedResultSet) rq.getResultSet();
		assertTrue(frs.hasMetaItems("docno"));
		
		assertEquals("0", frs.getDefaultLabel());
		assertArrayEquals(new String[]{"0", "4"}, frs.getLabels());
	}
	
}
