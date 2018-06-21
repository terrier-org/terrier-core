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
 * The Original Code is TrecEvalEvaluation.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.evaluation;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.applications.CLITool;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.Files;
import org.terrier.utility.Rounding;

import com.google.common.collect.Sets;

import uk.ac.gla.terrier.jtreceval.trec_eval;

public class TrecEvalEvaluation implements Evaluation {

	public static class Command extends CLITool
	{

		@Override
		public String commandname() {
			return "trec_eval";
		}
		
		
		@Override
		public Set<String> commandaliases() {
			return Sets.newHashSet("treceval");
		}

		@Override
		public String help() {
			new trec_eval().run(new String[]{"-h"});
			return "";
		}

		@Override
		public String helpsummary() {
			return "runs the NIST standard trec_eval tool";
		}

		@Override
		public int run(String[] args) throws Exception {
			new trec_eval().run(args);
			return 0;
		}
		
	}
	
	protected static final Logger logger = LoggerFactory.getLogger(TrecEvalEvaluation.class);

	String qrels;
	protected String[][] output;
	String resFile;

	public TrecEvalEvaluation(String[] qrels) {
		this.qrels = qrels[0];
		if (qrels.length != 1)
			throw new IllegalArgumentException("Only one qrels file can be specified");
		if (! isPlatformSupported())
			throw new UnsupportedOperationException("Your platform is not currently supported by jtreceval");
	}

	public TrecEvalEvaluation(String qrels) {
		this.qrels = qrels;
	}

	@Override
	public void evaluate(String resultFilename) {
		logger.info("Evaluating result file: "+resultFilename);
		String[] args = new String[]{qrels, resFile = resultFilename};
		output = new trec_eval().runAndGetOutput(args);

	}

	@Override
	public void writeEvaluationResult() {
		writeEvaluationResult(new PrintWriter(new OutputStreamWriter(System.out)));
	}

	@Override
	public void writeEvaluationResult(PrintWriter out) {
		//String summary = null;
		for(String[] line : output)
		{
			if (line.length >= 3 && line[0].equals("map") && line[1].equals("all"))
			{
				System.out.println("Average Precision: " + Rounding.toString(Double.parseDouble(line[2]), 4));
			}
			out.println(ArrayUtils.join(line, '\t'));
		}

	}

	@Override
	public void writeEvaluationResultOfEachQuery(String evaluationResultFilename) {
		String[] args = new String[]{"-q", qrels, resFile};
		output = new trec_eval().runAndGetOutput(args);
		writeEvaluationResult(evaluationResultFilename);
	}

	@Override
	public void writeEvaluationResult(String resultEvalFilename) {
		try{
			PrintWriter pw = new PrintWriter(Files.writeFileWriter(resultEvalFilename));
			writeEvaluationResult(pw);
			pw.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public static boolean isPlatformSupported() {
		try {
		if (! trec_eval.isPlatformSupported())
			return false;
		} catch (UnsupportedOperationException uoe) {
			return false;
		}
		return true;
	}


}
