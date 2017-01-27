package org.terrier.evaluation;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.utility.ArrayUtils;
import org.terrier.utility.Files;
import org.terrier.utility.Rounding;

import uk.ac.gla.terrier.jtreceval.trec_eval;

public class TrecEvalEvaluation implements Evaluation {

	protected static final Logger logger = LoggerFactory.getLogger(TrecEvalEvaluation.class);

	String qrels;
	String[][] output;
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
