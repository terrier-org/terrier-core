/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is TestAdhocEvaluation.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.junit.Test;

/** Ensures that evaluation classes behave as expected */
public class TestAdhocEvaluation extends TestCase {

	protected String makeRun(String qids[], String[][] docnos) throws Exception
	{
		final File tmpFile = File.createTempFile("/tmp", "tmp.res");
		final PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(tmpFile)));
		int queryIndex = 0;
		for(String[] docnosByQ : docnos)
		{
			final String qid = qids[queryIndex];
			int rank = 0;
			double score = 1000;
			for(String document : docnosByQ)
			{
				out.printf("%s Q0 %s %d %g tmp\n", qid, document, rank, score);
				rank++;
				score -= 5;
			}
			queryIndex++;
		}
		out.close();
		System.out.println("Writing results file to " + tmpFile.getAbsolutePath());
		return tmpFile.getAbsolutePath();
	}
	
	protected String makeQrels(String qids[], String[][] rel_docnos) throws Exception
	{
		final File tmpFile = File.createTempFile("/tmp", "tmp.qrels");
		final PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(tmpFile)));
		int queryIndex = 0;
		for(String[] docnosByQ : rel_docnos)
		{
			final String qid = qids[queryIndex];
			for(String document : docnosByQ)
			{
				out.printf("%s 0 %s 1\n", qid, document);
			}
			queryIndex++;
		}
		out.close();
		return tmpFile.getAbsolutePath();
	}
	
	protected String[] doEvaluation(Evaluation e, String resFilename) throws Exception
	{
		assertNotNull(e);
		e.evaluate(resFilename);
		StringWriter w = new StringWriter();
		PrintWriter p = new PrintWriter(w);
		e.writeEvaluationResult(p);
		e.writeEvaluationResult();
		p.close();
		return  w.toString().split("\n");
	}
	
	@Test public void testSingleQueryNoRetrieved() throws Exception {
		String resFilename = makeRun(new String[0], new String[0][0]);
		String qrelFilename = makeQrels(new String[]{"qid0"}, new String[][]{new String[]{"doc1"}});
		Evaluation e = new AdhocEvaluation(qrelFilename);
		String[] evaluationOutput = doEvaluation(e, resFilename);
		checkMeasure(evaluationOutput, "Number of queries", 0);
		checkMeasure(evaluationOutput, "Retrieved", 0);
		checkMeasure(evaluationOutput, "Relevant", 0);
		checkMeasure(evaluationOutput, "Relevant retrieved", 0);
		checkMeasure(evaluationOutput, "Average Precision", 0);
		checkMeasure(evaluationOutput, "R Precision", 0);
		checkMeasure(evaluationOutput, "Precision at   1 ", 0);
		
	}
	
	@Test public void testMultipleQuerySingleRelevant() throws Exception {
		String resFilename = makeRun(new String[]{"qid0", "qid1"}, new String[][]{
				new String[]{"doc1", "doc2", "doc3"},
				new String[]{"doc5", "doc10", "doc15"}});
		String qrelFilename = makeQrels(new String[]{"qid0", "qid1"}, new String[][]{
				new String[]{"doc1"},
				new String[]{"doc5"}});
		Evaluation e = new AdhocEvaluation(qrelFilename);
		String[] evaluationOutput = doEvaluation(e, resFilename);
		checkMeasure(evaluationOutput, "Number of queries", 2);
		checkMeasure(evaluationOutput, "Retrieved", 6);
		checkMeasure(evaluationOutput, "Relevant", 2);
		checkMeasure(evaluationOutput, "Relevant retrieved", 2);
		checkMeasure(evaluationOutput, "Average Precision", 1);
		checkMeasure(evaluationOutput, "R Precision", 1);
		checkMeasure(evaluationOutput, "Precision at   1 ", 1);
		checkMeasure(evaluationOutput, "Precision at   2 ", 0.5d);
	}
	
	@Test public void testMultipleQueryFirstTwoRelevant() throws Exception {
		String resFilename = makeRun(new String[]{"qid0", "qid1"}, new String[][]{
				new String[]{"doc1", "doc2", "doc3"},
				new String[]{"doc5", "doc10", "doc15"}});
		String qrelFilename = makeQrels(new String[]{"qid0", "qid1"}, new String[][]{
				new String[]{"doc1", "doc2"},
				new String[]{"doc5", "doc10"}});
		Evaluation e = new AdhocEvaluation(qrelFilename);
		String[] evaluationOutput = doEvaluation(e, resFilename);
		checkMeasure(evaluationOutput, "Number of queries", 2);
		checkMeasure(evaluationOutput, "Retrieved", 6);
		checkMeasure(evaluationOutput, "Relevant", 4);
		checkMeasure(evaluationOutput, "Relevant retrieved", 4);
		checkMeasure(evaluationOutput, "Average Precision", 1);
		checkMeasure(evaluationOutput, "R Precision", 1);
		checkMeasure(evaluationOutput, "Precision at   1 ", 1);
		checkMeasure(evaluationOutput, "Precision at   2 ", 1);
	}
	
	
	@Test public void testMultipleQueryTwoRelevant() throws Exception {
		String resFilename = makeRun(new String[]{"qid0", "qid1"}, new String[][]{
				new String[]{"doc1", "doc2", "doc3"},
				new String[]{"doc5", "doc10", "doc15"}});
		String qrelFilename = makeQrels(new String[]{"qid0", "qid1"}, new String[][]{
				new String[]{"doc1", "doc3"},
				new String[]{"doc5", "doc15"}});
		Evaluation e = new AdhocEvaluation(qrelFilename);
		String[] evaluationOutput = doEvaluation(e, resFilename);
		checkMeasure(evaluationOutput, "Number of queries", 2);
		checkMeasure(evaluationOutput, "Retrieved", 6);
		checkMeasure(evaluationOutput, "Relevant", 4);
		checkMeasure(evaluationOutput, "Relevant retrieved", 4);
		checkMeasure(evaluationOutput, "Average Precision", 0.8333d);
		checkMeasure(evaluationOutput, "R Precision", 0.5d);
		checkMeasure(evaluationOutput, "Precision at   1 ", 1);
		checkMeasure(evaluationOutput, "Precision at   2 ", 0.5d);
		checkMeasure(evaluationOutput, "Precision at   3 ", 0.6667d);
	}
	
	@Test public void testMultipleQueryFirstThreeRelevant() throws Exception {
		String resFilename = makeRun(new String[]{"qid0", "qid1"}, new String[][]{
				new String[]{"doc1", "doc2", "doc3", "doc4"},
				new String[]{"doc5", "doc10", "doc15", "doc20"}});
		String qrelFilename = makeQrels(new String[]{"qid0", "qid1"}, new String[][]{
				new String[]{"doc1", "doc2", "doc3"},
				new String[]{"doc5", "doc10", "doc15"}});
		Evaluation e = new AdhocEvaluation(qrelFilename);
		String[] evaluationOutput = doEvaluation(e, resFilename);
		checkMeasure(evaluationOutput, "Number of queries", 2);
		checkMeasure(evaluationOutput, "Retrieved", 8);
		checkMeasure(evaluationOutput, "Relevant", 6);
		checkMeasure(evaluationOutput, "Relevant retrieved", 6);
		checkMeasure(evaluationOutput, "Average Precision", 1);
		checkMeasure(evaluationOutput, "R Precision", 1);
		checkMeasure(evaluationOutput, "Precision at   1 ", 1);
		checkMeasure(evaluationOutput, "Precision at   2 ", 1);
		checkMeasure(evaluationOutput, "Precision at   3 ", 1);
		checkMeasure(evaluationOutput, "Precision at   4 ", 0.75d);
		checkMeasure(evaluationOutput, "Precision at   5 ", 0.6d);
	}
	
	@Test public void testSingleQuerySingleRelevant() throws Exception {
		String resFilename = makeRun(new String[]{"qid0"}, new String[][]{new String[]{"doc1"}});
		String qrelFilename = makeQrels(new String[]{"qid0"}, new String[][]{new String[]{"doc1"}});
		Evaluation e = new AdhocEvaluation(qrelFilename);
		String[] evaluationOutput = doEvaluation(e, resFilename);
		checkMeasure(evaluationOutput, "Number of queries", 1);
		checkMeasure(evaluationOutput, "Retrieved", 1);
		checkMeasure(evaluationOutput, "Relevant", 1);
		checkMeasure(evaluationOutput, "Relevant retrieved", 1);
		checkMeasure(evaluationOutput, "Average Precision", 1);
		checkMeasure(evaluationOutput, "R Precision", 1);
		checkMeasure(evaluationOutput, "Precision at   1 ", 1);
		
		resFilename = makeRun(new String[]{"qid0"}, new String[][]{new String[]{"doc1", "doc2"}});
		evaluationOutput = doEvaluation(e, resFilename);
		checkMeasure(evaluationOutput, "Number of queries", 1);
		checkMeasure(evaluationOutput, "Retrieved", 2);
		checkMeasure(evaluationOutput, "Relevant", 1);
		checkMeasure(evaluationOutput, "Relevant retrieved", 1);
		checkMeasure(evaluationOutput, "Average Precision", 1);
		checkMeasure(evaluationOutput, "R Precision", 1);
		checkMeasure(evaluationOutput, "Precision at   1 ", 1);
		
		resFilename = makeRun(new String[]{"qid0"}, new String[][]{new String[]{"doc2", "doc1"}});
		evaluationOutput = doEvaluation(e, resFilename);
		checkMeasure(evaluationOutput, "Number of queries", 1);
		checkMeasure(evaluationOutput, "Retrieved", 2);
		checkMeasure(evaluationOutput, "Relevant", 1);
		checkMeasure(evaluationOutput, "Relevant retrieved", 1);
		checkMeasure(evaluationOutput, "Average Precision", 0.5);
		checkMeasure(evaluationOutput, "R Precision", 0);
		checkMeasure(evaluationOutput, "Precision at   1 ", 0);
		checkMeasure(evaluationOutput, "Precision at   2 ", 0.5);
		
		resFilename = makeRun(new String[]{"qid0"}, new String[][]{new String[]{"doc3", "doc2", "doc1"}});
		evaluationOutput = doEvaluation(e, resFilename);
		checkMeasure(evaluationOutput, "Number of queries", 1);
		checkMeasure(evaluationOutput, "Retrieved", 3);
		checkMeasure(evaluationOutput, "Relevant", 1);
		checkMeasure(evaluationOutput, "Relevant retrieved", 1);
		checkMeasure(evaluationOutput, "Average Precision", 0.3333d);
		checkMeasure(evaluationOutput, "R Precision", 0);
		checkMeasure(evaluationOutput, "Precision at   1 ", 0);
		checkMeasure(evaluationOutput, "Precision at   2 ", 0);
		checkMeasure(evaluationOutput, "Precision at   3 ", 0.3333d);
	}
	
	
	protected static void checkMeasure(String[] output, String measureName, double targetValue)
	{
		for(String line : output)
		{
			//System.out.println(line);
			if (line.startsWith(measureName))
			{
				String svalue = line.split("\\s*[:=]\\s*")[1];
				double value = Double.parseDouble(svalue);
				assertEquals("Unexpected value for measure " + measureName, targetValue, value);
				return;
			}
		}
		assertTrue("Measure "+ measureName + " not found", false);
	}
}
