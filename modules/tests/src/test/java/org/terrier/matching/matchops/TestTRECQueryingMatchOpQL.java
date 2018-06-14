package org.terrier.matching.matchops;

import static org.junit.Assert.*;

import org.junit.Test;
import org.terrier.applications.batchquerying.SingleLineTRECQuery;
import org.terrier.applications.batchquerying.TRECQuerying;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.structures.Index;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestTRECQueryingMatchOpQL extends ApplicationSetupBasedTest {

	@Test public void testTopics() throws Exception
	{
		ApplicationSetup.setProperty("FieldTags.process", "TITLE");
		ApplicationSetup.setProperty("termpipelines", "");
		Index index = IndexTestUtils.makeIndexFieldsBlocks(new String[]{"doc1"}, new String[]{"the fox jumped <TITLE>over</TITLE>"});
		assertEquals(1, index.getCollectionStatistics().getNumberOfFields());
		ApplicationSetup.setProperty("trec.topics.parser", SingleLineTRECQuery.class.getName());
		ApplicationSetup.setProperty("SingleLineTRECQuery.tokenise", "false");
		ApplicationSetup.setProperty("trec.topics.matchopql", "true");
		String f = super.writeTemporaryFile("x.topics", new String[]{
				"1 fox",
				"2 #1(fox jumped)",
				"3 over.TITLE"
		});
		ApplicationSetup.setProperty("trec.topics", f);
		TRECQuerying tq = new TRECQuerying(index.getIndexRef());
		tq.processQueries();
	}
	
}
