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
 * The Original Code is Evaluation.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.evaluation;

import java.io.PrintWriter;

public interface Evaluation {

	/**
	 * Evaluates the given result file for the given qrels file.
	 * All subclasses must implement this method.
	 * @param resultFilename java.lang.String the filename of the result 
	 *        file to evaluate.
	 */
	abstract public void evaluate(String resultFilename);

	/**
	 * Output the evaluation result to standard output
	 */
	public abstract void writeEvaluationResult();

	/**
	 * The abstract method that evaluates and prints
	 * the results. All the subclasses of Evaluation
	 * must implement this method.
	 * @param out java.io.PrintWriter
	 */
	abstract public void writeEvaluationResult(PrintWriter out);

	/**
	 * Output the evaluation result of each query to the specific file.
	 * @param evaluationResultFilename String the name of the file in which to 
	 *        save the evaluation results.
	 */
	abstract public void writeEvaluationResultOfEachQuery(
			String evaluationResultFilename);

	/**
	 * Output the evaluation result to the specific file.
	 * @param resultEvalFilename java.lang.String the filename of 
	 *        the file to output the result.
	 */
	public abstract void writeEvaluationResult(String resultEvalFilename);

}
