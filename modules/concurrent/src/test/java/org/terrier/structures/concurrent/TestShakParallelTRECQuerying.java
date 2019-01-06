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
 * The Original Code is TestShakParallelTRECQuerying.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures.concurrent;

import java.io.PrintWriter;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;
import org.terrier.applications.batchquerying.ParallelTRECQuerying;
import org.terrier.applications.batchquerying.TRECQuerying;
import org.terrier.structures.IndexOnDisk;
import org.terrier.tests.BatchEndToEndTest;
import org.terrier.utility.ApplicationSetup;
@Ignore //this test fails on travis-ci
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
		tq.intialise();
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
