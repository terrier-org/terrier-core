package org.terrier;

import java.io.PrintWriter;
import java.util.Properties;

import org.junit.Test;
import org.terrier.applications.batchquerying.ParallelTRECQuerying;
import org.terrier.applications.batchquerying.TRECQuerying;
import org.terrier.structures.IndexOnDisk;
import org.terrier.tests.BatchEndToEndTest;
import org.terrier.utility.ApplicationSetup;

public class TestShakParallelTRECQuerying extends BatchEndToEndTest {

	public TestShakParallelTRECQuerying()
	{
		retrievalTopicSets.add(System.getProperty("user.dir") + "/../../share/tests/shakespeare/test.shakespeare-merchant.basic.topics");		
	}
	
	
	@Test public void testBasic() throws Exception {
		
		doTrecTerrierIndexing(new String[]{"-i"});
		doRetrieval(retrievalTopicSets.toArray(new String[0]), new String[0]);
		doEvaluation(7, System.getProperty("user.dir") + "/../../share/tests/shakespeare/test.shakespeare-merchant.all.qrels", 1.0f);
		System.err.println("done");
	}
	
	
	@Override
	protected int doRetrieval(String[] topicSet, String[] trecTerrierArgs)
			throws Exception {
		ApplicationSetup.setProperty("trec.topics", topicSet[0]);
		TRECQuerying tq = new ParallelTRECQuerying();
		tq.processQueries();
		return 7;
	}
	
	@Override
	protected void addGlobalTerrierProperties(Properties p) throws Exception {
		super.addGlobalTerrierProperties(p);
		p.setProperty("trec.topics.parser","SingleLineTRECQuery");
		p.setProperty("ignore.low.idf.terms","false");
	}



	@Override
	protected void makeCollectionSpec(PrintWriter p) throws Exception {
		p.println(System.getProperty("user.dir") + "/../../share/tests/shakespeare/shakespeare-merchant.trec.1");
		p.println(System.getProperty("user.dir") + "/../../share/tests/shakespeare/shakespeare-merchant.trec.2");
		p.close();
	}

	@Override
	protected int countNumberOfTopics(String filename) throws Exception {
		return -1;
	}

	@Override
	protected void addDirectStructure(IndexOnDisk index) throws Exception {}
	
}
