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
 * The Original Code is AdhocEvaluation.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.evaluation;
import gnu.trove.TDoubleFunction;
import gnu.trove.TIntDoubleHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.terrier.utility.Rounding;
/**
 * Performs the evaluation for TREC's tasks, except the named page task.
 * The evaluation measures include the mean average precision and other measures
 * such as precision at 10, precision at 10%, and so on....
 * @author Gianni Amati, Ben He
  */
public class AdhocEvaluation extends Evaluation {
	protected static final Logger logger = LoggerFactory.getLogger(AdhocEvaluation.class);
	
	protected static final int[] PRECISION_RANKS = new int[]{1,2,3,4,5,10,15,20,30,50,100,200,500,1000};
	protected static final int[] PRECISION_PERCENTAGES = new int[]{0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
	
	/** The maximum number of documents retrieved for a query. */
	protected int maxNumberRetrieved;
	/** The number of effective queries. An effective query is a
	*	query that has corresponding relevant documents in the qrels
	*	file.
	*/
	protected int numberOfEffQuery;
	/** The total number of documents retrieved in the task. */
	protected int totalNumberOfRetrieved;
	/** The total number of relevant documents in the qrels file
	* 	for the queries processed in the task. 
	*/
	protected int totalNumberOfRelevant;
	/** The total number of relevant documents retrieved in the task. */
	protected int totalNumberOfRelevantRetrieved;
	/** Precision at rank number of documents */
	protected TIntDoubleHashMap precisionAtRank = new TIntDoubleHashMap();
	
	protected TIntDoubleHashMap precisionAtRecall = new TIntDoubleHashMap();
	/** 
	 * Create adhoc evaluation
	 */
	public AdhocEvaluation() {
		super();
	}
	/** 
	 * Create adhoc evaluation
	 * @param qrelsFile
	 */
	public AdhocEvaluation(String qrelsFile) {
		super(qrelsFile);
	}
	/** 
	 * Create adhoc evaluation
	 * @param qrelsFiles
	 */
	public AdhocEvaluation(String[] qrelsFiles) {
		super(qrelsFiles);
	}

	/** Average Precision. */
	protected double meanAveragePrecision;
	/** Relevant Precision. */
	protected double meanRelevantPrecision;
	/** The average precision of each query. */
	protected double[] averagePrecisionOfEachQuery;
	/** The query number of each query. */
	protected String[] queryNo;
	
	/** Initialise variables. */
	public void initialise() {
		this.maxNumberRetrieved = 
				Integer.parseInt(ApplicationSetup.getProperty("max.number.retrieved", 
						"1000"));
		this.precisionAtRank.clear();
		this.precisionAtRecall.clear();
		this.numberOfEffQuery = 0;
		this.totalNumberOfRetrieved = 0;
		this.totalNumberOfRelevant = 0;
		this.meanAveragePrecision = 0;
		this.meanRelevantPrecision = 0;
		
	}
	/**
	 * Evaluates the given result file.
	 * @param resultFilename String the filename of 
	 *        the result file to evaluate.
	 */
	public void evaluate(String resultFilename) {
		this.initialise();
		logger.info("Evaluating result file: "+resultFilename);
		
		//int retrievedQueryCounter = 0;
		//int releventQueryCounter = 0; 
		int effQueryCounter = 0;
		
		int[] numberOfRelevantRetrieved = null;
		int[] numberOfRelevant = null;
		int[] numberOfRetrieved = null;
		Vector<Record[]> listOfRetrieved = new Vector<Record[]>();
		Vector<Record[]> listOfRelevantRetrieved = new Vector<Record[]>();
		Vector<Integer> vecNumberOfRelevant = new Vector<Integer>();
		Vector<Integer> vecNumberOfRetrieved = new Vector<Integer>();
		Vector<Integer> vecNumberOfRelevantRetrieved = new Vector<Integer>();
		Vector<String> vecQueryNo = new Vector<String>();
		
		
		/** Read records from the result file */
		try {
			
			final BufferedReader br = Files.openFileReader(resultFilename);
			String str = null;
			String previous = ""; // the previous query number
			int numberOfRetrievedCounter = 0;
			int numberOfRelevantRetrievedCounter = 0;
			Vector<Record> relevantRetrieved = new Vector<Record>();
			Vector<Record> retrieved = new Vector<Record>();
			while ((str=br.readLine()) != null) {
				StringTokenizer stk = new StringTokenizer(str);
				String queryid = stk.nextToken();
				
				//remove non-numeric letters in the queryNo
				StringBuilder queryNoTmp = new StringBuilder();
				boolean firstNumericChar = false;
				for (int i = queryid.length()-1; i >=0; i--){
					if (queryid.charAt(i) >= '0' && queryid.charAt(i) <= '9'){
						queryNoTmp.append(queryid.charAt(i));
						firstNumericChar = true;
					}
					else if (firstNumericChar)
						break;
				}
				queryid = ""+ Integer.parseInt(queryNoTmp.reverse().toString()); 
				if (!qrels.queryExistInQrels(queryid))
					continue;
				
				stk.nextToken();
				String docID = stk.nextToken();
				
				int rank = Integer.parseInt(stk.nextToken());
				if (!previous.equals(queryid)) {
					if (effQueryCounter != 0) {
						vecNumberOfRetrieved.addElement(Integer.valueOf(numberOfRetrievedCounter));
						vecNumberOfRelevantRetrieved.addElement(Integer.valueOf(numberOfRelevantRetrievedCounter));
						listOfRetrieved.addElement((Record[])retrieved.toArray(new Record[retrieved.size()]));
						listOfRelevantRetrieved.addElement((Record[])relevantRetrieved.toArray(new Record[relevantRetrieved.size()]));
						numberOfRetrievedCounter = 0;
						numberOfRelevantRetrievedCounter = 0;
						retrieved = new Vector<Record>();
						relevantRetrieved = new Vector<Record>();
					}
					effQueryCounter++;
					vecQueryNo.addElement(queryid);
					vecNumberOfRelevant.addElement(Integer.valueOf(qrels.getNumberOfRelevant(queryid)));
				}
				previous = queryid;
				numberOfRetrievedCounter++;
				totalNumberOfRetrieved++;
				retrieved.addElement(new Record(queryid, docID, rank));
				if (qrels.isRelevant(queryid, docID)){
					relevantRetrieved.addElement(new Record(queryid, docID, rank));
					numberOfRelevantRetrievedCounter++;
				}
			}
			listOfRelevantRetrieved.addElement(relevantRetrieved.toArray(new Record[relevantRetrieved.size()]));
			listOfRetrieved.addElement(retrieved.toArray(new Record[retrieved.size()]));
			vecNumberOfRetrieved.addElement(Integer.valueOf(numberOfRetrievedCounter));
			vecNumberOfRelevantRetrieved.addElement(Integer.valueOf(numberOfRelevantRetrievedCounter));
			br.close();
			this.queryNo = vecQueryNo.toArray(new String[vecQueryNo.size()]);
			numberOfRelevantRetrieved = new int[effQueryCounter];
			numberOfRelevant = new int[effQueryCounter];
			numberOfRetrieved = new int[effQueryCounter];
			this.totalNumberOfRelevant = 0;
			this.totalNumberOfRelevantRetrieved = 0;
			this.totalNumberOfRetrieved = 0;
			for (int i = 0; i < effQueryCounter; i++){
				numberOfRelevantRetrieved[i] = 
					((Integer)vecNumberOfRelevantRetrieved.get(i)).intValue();
				numberOfRelevant[i] = ((Integer)vecNumberOfRelevant.get(i)).intValue();
				numberOfRetrieved[i] = ((Integer)vecNumberOfRetrieved.get(i)).intValue();
				this.totalNumberOfRetrieved += numberOfRetrieved[i];
				this.totalNumberOfRelevant += numberOfRelevant[i];
				this.totalNumberOfRelevantRetrieved += numberOfRelevantRetrieved[i];
			}
		} catch (IOException e) {
			logger.error("Exception while evaluating", e);
		}
		
		this.averagePrecisionOfEachQuery = new double[effQueryCounter];
		
		TIntDoubleHashMap[] precisionAtRankByQuery = new TIntDoubleHashMap[effQueryCounter];
		TIntDoubleHashMap[] precisionAtRecallByQuery = new TIntDoubleHashMap[effQueryCounter];
		for (int i = 0; i < effQueryCounter; i++) {
			precisionAtRankByQuery[i] = new TIntDoubleHashMap();
			precisionAtRecallByQuery[i] = new TIntDoubleHashMap();
		}
		
		double[] ExactPrecision = new double[effQueryCounter];
		double[] RPrecision = new double[effQueryCounter];
		Arrays.fill(ExactPrecision, 0.0d);
		Arrays.fill(RPrecision, 0.0d);
		

		meanAveragePrecision = 0d;
		meanRelevantPrecision = 0d;
		numberOfEffQuery = effQueryCounter;
		for (int i = 0; i < effQueryCounter; i++) {
			Record[] relevantRetrieved = (Record[])listOfRelevantRetrieved.get(i);
			for (int j = 0; j < relevantRetrieved.length; j++)
			{
				if (relevantRetrieved[j].rank < numberOfRelevant[i])
				{
					RPrecision[i] += 1d;
				}
				for(int precisionRank : PRECISION_RANKS)
				{
					if (relevantRetrieved[j].rank < precisionRank)
						precisionAtRankByQuery[i].adjustOrPutValue(precisionRank, 1.0d, 1.0d);
				}
			
				ExactPrecision[i] += (double)(j+1)
					/ (1d + relevantRetrieved[j].rank);
				relevantRetrieved[j].precision =
					(double)(j+1)
						/ (1d + relevantRetrieved[j].rank);
				relevantRetrieved[j].recall =
					(double)(j+1) / numberOfRelevant[i];
			}
			for (int j = 0; j < relevantRetrieved.length; j++) {
				for (int precisionPercentage : PRECISION_PERCENTAGES)
				{
					final double fraction = ((double) precisionPercentage)/100.0d;
					if (relevantRetrieved[j].recall >= fraction 
							&& relevantRetrieved[j].precision >= precisionAtRecallByQuery[i].get(precisionPercentage))
					{
						precisionAtRecallByQuery[i].put(precisionPercentage, relevantRetrieved[j].precision);
					}
				}

			}
			//Modified by G.AMATI 7th May 2002
			if (numberOfRelevant[i] > 0)
				ExactPrecision[i] /= ((double) numberOfRelevant[i]);
			else
				numberOfEffQuery--;
			if (numberOfRelevant[i] > 0)
				RPrecision[i] /= ((double) numberOfRelevant[i]);
			meanAveragePrecision += ExactPrecision[i];
			this.averagePrecisionOfEachQuery[i] = ExactPrecision[i];
			meanRelevantPrecision += RPrecision[i];
			
			for(int precisionRank : PRECISION_RANKS)
			{
				precisionAtRank.adjustOrPutValue(precisionRank, 
						precisionAtRankByQuery[i].get(precisionRank) / (double)precisionRank, 
						precisionAtRankByQuery[i].get(precisionRank) / (double)precisionRank);
			}
		}
		for (int i = 0; i < effQueryCounter; i++) {
			for(int precisionRecall : PRECISION_PERCENTAGES)
				precisionAtRecall.adjustOrPutValue(precisionRecall, 
						precisionAtRecallByQuery[i].get(precisionRecall), 
						precisionAtRecallByQuery[i].get(precisionRecall));
		}
		
		final double numberOfEffQueryD = (double)numberOfEffQuery;
		TDoubleFunction meanTransformer = new TDoubleFunction(){
			public double execute(double value)
			{
				return value / numberOfEffQueryD;
			}
		};
		precisionAtRecall.transformValues(meanTransformer);
		precisionAtRank.transformValues(meanTransformer);
		meanAveragePrecision /= (double) numberOfEffQuery;
		meanRelevantPrecision /= (double) numberOfEffQuery;
	}
	/**
	 * Output the evaluation result of each query to the specific file.
	 * @param resultEvalFilename String the name of the file in which to 
	 *        save the evaluation results.
	 */
	public void writeEvaluationResultOfEachQuery(String resultEvalFilename) {
		try {
			final PrintWriter out = new PrintWriter(Files.writeFileWriter(resultEvalFilename));
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < this.queryNo.length; i++)
				sb.append(
					queryNo[i]
						+ " "
						+ Rounding.toString(
							this.averagePrecisionOfEachQuery[i],
							4)
						+ ApplicationSetup.EOL);
			out.print(sb.toString());
			out.close();
		} catch (IOException fnfe) {
			logger.error("Couldn't create evaluation file "+ resultEvalFilename , fnfe);
		}
	}
	/**
	 * Output the evaluation result to the specific file.
	 * @param out java.io.PrintWriter the stream to which the results are printed.
	 */
	public void writeEvaluationResult(PrintWriter out) {
		out.println("____________________________________");
		out.println("Number of queries  = " + numberOfEffQuery);
		out.println("Retrieved          = " + totalNumberOfRetrieved);
		out.println("Relevant           = " + totalNumberOfRelevant);
		out.println("Relevant retrieved = " + totalNumberOfRelevantRetrieved);
		out.println("____________________________________");
		out.println(
			"Average Precision: " + Rounding.toString(meanAveragePrecision, 4));
		out.println(
			"R Precision      : " + Rounding.toString(meanRelevantPrecision, 4));
		out.println("____________________________________");
		for(int precisionRank : PRECISION_RANKS)
		{
			out.printf("Precision at   %d : %s\n", precisionRank, Rounding.toString(precisionAtRank.get(precisionRank), 4));
		}
		out.println("____________________________________");
		for(int precisionPercent : PRECISION_PERCENTAGES)
		{
			out.printf("Precision at   %d%%: %s\n", precisionPercent, Rounding.toString(precisionAtRecall.get(precisionPercent), 4));
		}
		out.println("____________________________________");
		out.println(
			"Average Precision: " + Rounding.toString(meanAveragePrecision, 4));
		System.out.println("Average Precision: " + Rounding.toString(meanAveragePrecision, 4));
	}
}
