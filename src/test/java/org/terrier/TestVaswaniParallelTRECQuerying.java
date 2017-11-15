package org.terrier;

import java.io.PrintWriter;
import java.util.Properties;

import org.junit.Test;
import org.terrier.applications.batchquerying.ParallelTRECQuerying;
import org.terrier.applications.batchquerying.TRECQuerying;
import org.terrier.structures.IndexOnDisk;
import org.terrier.tests.BatchEndToEndTest;
import org.terrier.utility.ApplicationSetup;

public class TestVaswaniParallelTRECQuerying extends BatchEndToEndTest {

	public TestVaswaniParallelTRECQuerying()
	{
		retrievalTopicSets.add(System.getProperty("user.dir") + "/share/vaswani_npl/query-text.trec");		
	}
	
	
	@Test public void testBasicClassical() throws Exception {
		
		doTrecTerrierIndexing(new String[]{"-i"});
		doRetrieval(retrievalTopicSets.toArray(new String[0]), new String[0]);
		doEvaluation(93, System.getProperty("user.dir") + "/share/vaswani_npl/qrels", 0.2948f);

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
		p.setProperty("ignore.low.idf.terms","false");
	}



	@Override
	protected void makeCollectionSpec(PrintWriter p) throws Exception {
		p.println(System.getProperty("user.dir") + "/share/vaswani_npl/corpus/doc-text.trec");
		p.close();
	}

	@Override
	protected int countNumberOfTopics(String filename) throws Exception {
		return -1;
	}

	@Override
	protected void addDirectStructure(IndexOnDisk index) throws Exception {}
	
}
