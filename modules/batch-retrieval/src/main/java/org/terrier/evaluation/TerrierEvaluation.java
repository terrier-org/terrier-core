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
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Ben He <ben{a.}dcs.gla.ac.uk>
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> 
 */
package org.terrier.evaluation;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import org.terrier.utility.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * An abstract class for evaluating the retrieval results.
 * @author Gianni Amati, Ben He, Vassilis Plachouras
  */
public abstract class TerrierEvaluation implements Evaluation {
	protected static final Logger logger = LoggerFactory.getLogger(TerrierEvaluation.class);
	/**
	 * A structure of a record of retrieved document.
	 */
	static public class Record {
		/** The topic number. */
		String queryNo;
		
		/** The rank of the document. */
		int rank;
		/** The document identifier. */
		String docNo;
		/** The precision at this document. */
		double precision;
		/** The recall at this document. */
		double recall;
		/**
		 * create a record of retrieved document
		 * @param _queryNo
		 * @param _docNo
		 * @param _rank
		 */
		public Record(String _queryNo, String _docNo, int _rank) {
			this.queryNo = _queryNo;
			this.rank = _rank;
			this.docNo = _docNo;
		}
		/**
		 * get rank number
		 * @return int
		 */
		public int getRank() {
			return rank;
		}
		/**
		 * set rank position
		 * @param _rank
		 */
		public void setRank(int _rank) {
			this.rank = _rank;
		}
		/**
		 * get document number
		 * @return String
		 */
		public String getDocNo() {
			return docNo;
		}
		/**
		 * set query number
		 * @param _queryNo
		 */
		public void setQueryNo(String _queryNo) {
			this.queryNo = _queryNo;
		}
		/**
		 * get query number
		 * @return query number
		 */
		public String getQueryNo()
		{
			return queryNo;
		}
	}
	
	
	protected TerrierEvaluation()
	{
		qrels = new TRECQrelsInMemory();
	}
	
	protected TerrierEvaluation(String qrelsFile)
	{
		qrels = new TRECQrelsInMemory(qrelsFile);
	}
	
	protected TerrierEvaluation(String[] qrelsFiles)
	{
		qrels = new TRECQrelsInMemory(qrelsFiles);
	}
	
	/**
	 * A structure of all the records in the qrels files.
	 */
	public TRECQrelsInMemory qrels;
	
	/* (non-Javadoc)
	 * @see org.terrier.evaluation.GenericEvaluation#evaluate(java.lang.String)
	 */
	@Override
	abstract public void evaluate(String resultFilename);
	
	/* (non-Javadoc)
	 * @see org.terrier.evaluation.GenericEvaluation#writeEvaluationResult()
	 */
	@Override
	public void writeEvaluationResult() {
		writeEvaluationResult(new PrintWriter(new OutputStreamWriter(System.out)));
	}
	/* (non-Javadoc)
	 * @see org.terrier.evaluation.GenericEvaluation#writeEvaluationResult(java.io.PrintWriter)
	 */
	@Override
	abstract public void writeEvaluationResult(PrintWriter out);
	
	/* (non-Javadoc)
	 * @see org.terrier.evaluation.GenericEvaluation#writeEvaluationResultOfEachQuery(java.lang.String)
	 */
	@Override
	abstract public void writeEvaluationResultOfEachQuery(String evaluationResultFilename);

	
	/* (non-Javadoc)
	 * @see org.terrier.evaluation.GenericEvaluation#writeEvaluationResult(java.lang.String)
	 */
	@Override
	public void writeEvaluationResult(String resultEvalFilename) {
		try {
			final PrintWriter out = new PrintWriter(Files.writeFileWriter(resultEvalFilename));
			writeEvaluationResult(out);
			out.close();
		} catch (IOException fnfe) {
			logger.error(
				"File not found exception occurred when trying to write to file" +resultEvalFilename,
					fnfe);
		}
	}
}
