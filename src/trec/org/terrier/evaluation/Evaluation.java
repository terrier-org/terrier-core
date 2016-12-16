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