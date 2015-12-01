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
 * The Original Code is AdhocFullQueryEvaluation.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 */
package org.terrier.evaluation;
import gnu.trove.TDoubleFunction;
import gnu.trove.TIntDoubleHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.utility.Files;
/**
 * Performs a query by query evaluation of a results file. Like trec_eval -q.
 */
public class AdhocFullQueryEvaluation extends AdhocEvaluation{
	protected static final Logger logger = LoggerFactory.getLogger(AdhocFullQueryEvaluation.class);
	
	
	/** 
	 * Create full query adhoc evaluation 
	 */
	public AdhocFullQueryEvaluation() {
		super();
	}

	/**
	 * Create full query adhoc evaluation 
	 * @param qrelsFile
	 */
	public AdhocFullQueryEvaluation(String qrelsFile) {
		super(qrelsFile);
	}
	/** 
	 * Create full query adhoc evaluation 
	 * @param qrelsFiles
	 */
	public AdhocFullQueryEvaluation(String[] qrelsFiles) {
		super(qrelsFiles);
	}



	/**
	 * Evaluates the given result file for the given qrels file.
	 * @param resultFilename java.lang.String the filename of the result file to evaluate.
	 * @param qrelsFilename java.lang.String the filename of the qrels file corresponding
	 *	to the result file.
	 */
	public void evaluate(String qrelsFilename, String resultFilename) {
		this.initialise();
		logger.info("Evaluating result file: "+resultFilename);
		logger.info("Using qrel file: "+qrelsFilename);
		TRECQrelsInMemory qrels = new TRECQrelsInMemory(qrelsFilename);
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
		precisionAtRank.clear();
		precisionAtRecall.clear();
		
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
				for (int i = 0; i < queryid.length(); i++)
					if (queryid.charAt(i) >= '0' && queryid.charAt(i) <= '9')
						queryNoTmp.append(queryid.charAt(i));
				queryid = queryNoTmp.toString();
				if (!qrels.queryExistInQrels(queryid))
					continue;
				
				stk.nextToken();
				String docID = stk.nextToken();
				
				int rank = Integer.parseInt(stk.nextToken());
				if (!previous.equals(queryid)) {
					if (effQueryCounter != 0) {
						vecNumberOfRetrieved.addElement(Integer.valueOf(numberOfRetrievedCounter));
						vecNumberOfRelevantRetrieved.addElement(Integer.valueOf(numberOfRelevantRetrievedCounter));
						listOfRetrieved.addElement(retrieved.toArray(new Record[retrieved.size()]));
						listOfRelevantRetrieved.addElement(relevantRetrieved.toArray(new Record[relevantRetrieved.size()]));
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
			listOfRelevantRetrieved.addElement((Record[])relevantRetrieved.toArray(new Record[relevantRetrieved.size()]));
			listOfRetrieved.addElement((Record[])retrieved.toArray(new Record[retrieved.size()]));
			vecNumberOfRetrieved.addElement(Integer.valueOf(numberOfRetrievedCounter));
			vecNumberOfRelevantRetrieved.addElement(Integer.valueOf(numberOfRelevantRetrievedCounter));
			br.close();
			this.queryNo = (String[])vecQueryNo.toArray(new String[vecQueryNo.size()]);
			numberOfRelevantRetrieved = new int[effQueryCounter];
			numberOfRelevant = new int[effQueryCounter];
			numberOfRetrieved = new int[effQueryCounter];
			for (int i = 0; i < effQueryCounter; i++){
				numberOfRelevantRetrieved[i] = 
					((Integer)vecNumberOfRelevantRetrieved.get(i)).intValue();
				numberOfRelevant[i] = ((Integer)vecNumberOfRelevant.get(i)).intValue();
				numberOfRetrieved[i] = ((Integer)vecNumberOfRetrieved.get(i)).intValue();
			}
		} catch (IOException e) {
			logger.error("Exception while evaluating", e);
		}
//		System.out.println("effQueryCounter: " + effQueryCounter);
//		for (int i = 0; i < effQueryCounter; i++){
//			Record[] records = (Record[])listOfRelevantRetrieved.get(i);
//			if (numberOfRelevantRetrieved[i]!=records.length)
//				System.out.println("error: numberOfRelevantRetrieved[i]!=records.length for query " +
//						this.queryNo[i]);
//			for (int j = 0; j < records.length-1; j++)
//				if (records[j].rank >= records[j+1].rank)
//					System.out.println("error: records[j].rank >= records[j+1].rank for query " +
//							queryNo[j]);
//			System.out.println(queryNo[i] + 
//					", numberOfRelevant: " + numberOfRelevant[i] +
//					", numberOfRetrieved: " + numberOfRetrieved[i] +
//					", numberOfRelevantRetrieved: " + numberOfRelevantRetrieved[i]);
//		}
		
		this.averagePrecisionOfEachQuery = new double[effQueryCounter];
		TIntDoubleHashMap[] precisionAtRankByQuery = new TIntDoubleHashMap[effQueryCounter];
		TIntDoubleHashMap[] precisionAtRecallByQuery = new TIntDoubleHashMap[effQueryCounter];
		for (int i = 0; i < effQueryCounter; i++) {
			precisionAtRankByQuery[i] = new TIntDoubleHashMap();
			precisionAtRecallByQuery[i] = new TIntDoubleHashMap();
		}
		
//		int[] PrecisionAt1 = new int[effQueryCounter];
//		//Modified by G.AMATI 7th May 2002
//		int[] PrecisionAt2 = new int[effQueryCounter];
//		int[] PrecisionAt3 = new int[effQueryCounter];
//		int[] PrecisionAt4 = new int[effQueryCounter];
//		//END of modification
//		int[] PrecisionAt5 = new int[effQueryCounter];
//		int[] PrecisionAt10 = new int[effQueryCounter];
//		int[] PrecisionAt15 = new int[effQueryCounter];
//		int[] PrecisionAt20 = new int[effQueryCounter];
//		int[] PrecisionAt30 = new int[effQueryCounter];
//		int[] PrecisionAt50 = new int[effQueryCounter];
//		int[] PrecisionAt100 = new int[effQueryCounter];
//		int[] PrecisionAt200 = new int[effQueryCounter];
//		int[] PrecisionAt500 = new int[effQueryCounter];
//		int[] PrecisionAt1000 = new int[effQueryCounter];
		double[] ExactPrecision = new double[effQueryCounter];
		double[] RPrecision = new double[effQueryCounter];
//		double[] PrecisionAt0_0 = new double[effQueryCounter];
//		double[] PrecisionAt0_1 = new double[effQueryCounter];
//		double[] PrecisionAt0_2 = new double[effQueryCounter];
//		double[] PrecisionAt0_3 = new double[effQueryCounter];
//		double[] PrecisionAt0_4 = new double[effQueryCounter];
//		double[] PrecisionAt0_5 = new double[effQueryCounter];
//		double[] PrecisionAt0_6 = new double[effQueryCounter];
//		double[] PrecisionAt0_7 = new double[effQueryCounter];
//		double[] PrecisionAt0_8 = new double[effQueryCounter];
//		double[] PrecisionAt0_9 = new double[effQueryCounter];
//		double[] PrecisionAt1_0 = new double[effQueryCounter];
		//computing the precision-recall measures
//		for (int i = 0; i < effQueryCounter; i++) {
//			PrecisionAt0_0[i] = 0d;
//			PrecisionAt0_1[i] = 0d;
//			PrecisionAt0_2[i] = 0d;
//			PrecisionAt0_3[i] = 0d;
//			PrecisionAt0_4[i] = 0d;
//			PrecisionAt0_5[i] = 0d;
//			PrecisionAt0_6[i] = 0d;
//			PrecisionAt0_7[i] = 0d;
//			PrecisionAt0_8[i] = 0d;
//			PrecisionAt0_9[i] = 0d;
//			PrecisionAt1_0[i] = 0d;
//			PrecisionAt1[i] = 0;
//			//Modified by G.AMATI 7th May 2002
//			PrecisionAt2[i] = 0;
//			PrecisionAt3[i] = 0;
//			PrecisionAt4[i] = 0;
//			//END of modification
//			PrecisionAt5[i] = 0;
//			PrecisionAt10[i] = 0;
//			PrecisionAt15[i] = 0;
//			PrecisionAt20[i] = 0;
//			PrecisionAt30[i] = 0;
//			PrecisionAt50[i] = 0;
//			PrecisionAt100[i] = 0;
//			PrecisionAt200[i] = 0;
//			PrecisionAt500[i] = 0;
//			PrecisionAt1000[i] = 0;
//			ExactPrecision[i] = 0d;
//			RPrecision[i] = 0d;
//		}
//		precAt1 = 0d;
//		//Modified by G.AMATI 7th May 2002
//		precAt2 = 0d;
//		precAt3 = 0d;
//		precAt4 = 0d;
//		//END of modification
//		precAt5 = 0d;
//		PrecAt10 = 0d;
//		PrecAt15 = 0d;
//		PrecAt20 = 0d;
//		PrecAt30 = 0d;
//		PrecAt50 = 0d;
//		PrecAt100 = 0d;
//		PrecAt200 = 0d;
//		PrecAt500 = 0d;
//		PrecAt1000 = 0d;
//		PrecAt0Percent = 0d;
//		PrecAt10Percent = 0d;
//		PrecAt20Percent = 0d;
//		PrecAt30Percent = 0d;
//		PrecAt40Percent = 0d;
//		PrecAt50Percent = 0d;
//		PrecAt60Percent = 0d;
//		PrecAt70Percent = 0d;
//		PrecAt80Percent = 0d;
//		PrecAt90Percent = 0d;
//		PrecAt100Percent = 0d;
		meanAveragePrecision = 0d;
		meanRelevantPrecision = 0d;

	
		numberOfEffQuery = effQueryCounter;
		for (int i = 0; i < effQueryCounter; i++) {
			Record[] relevantRetrieved = (Record[])listOfRelevantRetrieved.get(i);
			for (int j = 0; j < relevantRetrieved.length; j++) {
				if (relevantRetrieved[j].rank < numberOfRelevant[i]) {
					RPrecision[i] += 1d;
				}
				for(int precisionRank : PRECISION_RANKS)
				{
					if (relevantRetrieved[j].rank <= precisionRank)
						precisionAtRankByQuery[i].adjustOrPutValue(precisionRank, 1.0d, 1.0d);
				}
//				if (relevantRetrieved[j].rank < 1) {
//					PrecisionAt1[i]++;
//					PrecisionAt2[i]++;
//					PrecisionAt3[i]++;
//					PrecisionAt4[i]++;
//					PrecisionAt5[i]++;
//					PrecisionAt10[i]++;
//					PrecisionAt15[i]++;
//					PrecisionAt20[i]++;
//					PrecisionAt30[i]++;
//					PrecisionAt50[i]++;
//					PrecisionAt100[i]++;
//					PrecisionAt200[i]++;
//					PrecisionAt500[i]++;
//					PrecisionAt1000[i]++;
//					/**			Modified by G.AMATI 7th May 2002 */
//				} else if (relevantRetrieved[j].rank < 2) {
//					PrecisionAt2[i]++;
//					PrecisionAt3[i]++;
//					PrecisionAt4[i]++;
//					PrecisionAt5[i]++;
//					PrecisionAt10[i]++;
//					PrecisionAt15[i]++;
//					PrecisionAt20[i]++;
//					PrecisionAt30[i]++;
//					PrecisionAt50[i]++;
//					PrecisionAt100[i]++;
//					PrecisionAt200[i]++;
//					PrecisionAt500[i]++;
//					PrecisionAt1000[i]++;
//				} else if (relevantRetrieved[j].rank < 3) {
//					PrecisionAt3[i]++;
//					PrecisionAt4[i]++;
//					PrecisionAt5[i]++;
//					PrecisionAt10[i]++;
//					PrecisionAt15[i]++;
//					PrecisionAt20[i]++;
//					PrecisionAt30[i]++;
//					PrecisionAt50[i]++;
//					PrecisionAt100[i]++;
//					PrecisionAt200[i]++;
//					PrecisionAt500[i]++;
//					PrecisionAt1000[i]++;
//				} else if (relevantRetrieved[j].rank < 4) {
//					PrecisionAt4[i]++;
//					PrecisionAt5[i]++;
//					PrecisionAt10[i]++;
//					PrecisionAt15[i]++;
//					PrecisionAt20[i]++;
//					PrecisionAt30[i]++;
//					PrecisionAt50[i]++;
//					PrecisionAt100[i]++;
//					PrecisionAt200[i]++;
//					PrecisionAt500[i]++;
//					PrecisionAt1000[i]++;
//					//END of modification
//				} else if (relevantRetrieved[j].rank < 5) {
//					PrecisionAt5[i]++;
//					PrecisionAt10[i]++;
//					PrecisionAt15[i]++;
//					PrecisionAt20[i]++;
//					PrecisionAt30[i]++;
//					PrecisionAt50[i]++;
//					PrecisionAt100[i]++;
//					PrecisionAt200[i]++;
//					PrecisionAt500[i]++;
//					PrecisionAt1000[i]++;
//				} else if (relevantRetrieved[j].rank < 10) {
//					PrecisionAt10[i]++;
//					PrecisionAt15[i]++;
//					PrecisionAt20[i]++;
//					PrecisionAt30[i]++;
//					PrecisionAt50[i]++;
//					PrecisionAt100[i]++;
//					PrecisionAt200[i]++;
//					PrecisionAt500[i]++;
//					PrecisionAt1000[i]++;
//				} else if (relevantRetrieved[j].rank < 15) {
//					PrecisionAt15[i]++;
//					PrecisionAt20[i]++;
//					PrecisionAt30[i]++;
//					PrecisionAt50[i]++;
//					PrecisionAt100[i]++;
//					PrecisionAt200[i]++;
//					PrecisionAt500[i]++;
//					PrecisionAt1000[i]++;
//				} else if (relevantRetrieved[j].rank < 20) {
//					PrecisionAt20[i]++;
//					PrecisionAt30[i]++;
//					PrecisionAt50[i]++;
//					PrecisionAt100[i]++;
//					PrecisionAt200[i]++;
//					PrecisionAt500[i]++;
//					PrecisionAt1000[i]++;
//				} else if (relevantRetrieved[j].rank < 30) {
//					PrecisionAt30[i]++;
//					PrecisionAt50[i]++;
//					PrecisionAt100[i]++;
//					PrecisionAt200[i]++;
//					PrecisionAt500[i]++;
//					PrecisionAt1000[i]++;
//				} else if (relevantRetrieved[j].rank < 50) {
//					PrecisionAt50[i]++;
//					PrecisionAt100[i]++;
//					PrecisionAt200[i]++;
//					PrecisionAt500[i]++;
//					PrecisionAt1000[i]++;
//				} else if (relevantRetrieved[j].rank < 100) {
//					PrecisionAt100[i]++;
//					PrecisionAt200[i]++;
//					PrecisionAt500[i]++;
//					PrecisionAt1000[i]++;
//				} else if (relevantRetrieved[j].rank < 200) {
//					PrecisionAt200[i]++;
//					PrecisionAt500[i]++;
//					PrecisionAt1000[i]++;
//				} else if (relevantRetrieved[j].rank < 500) {
//					PrecisionAt500[i]++;
//					PrecisionAt1000[i]++;
//				} else
//					PrecisionAt1000[i]++;
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
						precisionAtRecallByQuery[i].adjustOrPutValue(precisionPercentage, relevantRetrieved[j].precision, relevantRetrieved[j].precision);
					}
				}
//				if (relevantRetrieved[j].recall
//					>= 0d && relevantRetrieved[j].precision
//					>= PrecisionAt0_0[i])
//					PrecisionAt0_0[i] = relevantRetrieved[j].precision;
//				if (relevantRetrieved[j].recall
//					>= 0.1d && relevantRetrieved[j].precision
//					>= PrecisionAt0_1[i])
//					PrecisionAt0_1[i] = relevantRetrieved[j].precision;
//				if (relevantRetrieved[j].recall
//					>= 0.2d && relevantRetrieved[j].precision
//					>= PrecisionAt0_2[i])
//					PrecisionAt0_2[i] = relevantRetrieved[j].precision;
//				if (relevantRetrieved[j].recall
//					>= 0.3d && relevantRetrieved[j].precision
//					>= PrecisionAt0_3[i])
//					PrecisionAt0_3[i] = relevantRetrieved[j].precision;
//				if (relevantRetrieved[j].recall
//					>= 0.4d && relevantRetrieved[j].precision
//					>= PrecisionAt0_4[i])
//					PrecisionAt0_4[i] = relevantRetrieved[j].precision;
//				if (relevantRetrieved[j].recall
//					>= 0.5d && relevantRetrieved[j].precision
//					>= PrecisionAt0_5[i])
//					PrecisionAt0_5[i] = relevantRetrieved[j].precision;
//				if (relevantRetrieved[j].recall
//					>= 0.6d && relevantRetrieved[j].precision
//					>= PrecisionAt0_6[i])
//					PrecisionAt0_6[i] = relevantRetrieved[j].precision;
//				if (relevantRetrieved[j].recall
//					>= 0.7d && relevantRetrieved[j].precision
//					>= PrecisionAt0_7[i])
//					PrecisionAt0_7[i] = relevantRetrieved[j].precision;
//				if (relevantRetrieved[j].recall
//					>= 0.8d && relevantRetrieved[j].precision
//					>= PrecisionAt0_8[i])
//					PrecisionAt0_8[i] = relevantRetrieved[j].precision;
//				if (relevantRetrieved[j].recall
//					>= 0.9d && relevantRetrieved[j].precision
//					>= PrecisionAt0_9[i])
//					PrecisionAt0_9[i] = relevantRetrieved[j].precision;
//				if (relevantRetrieved[j].recall
//					>= 1.0d && relevantRetrieved[j].precision
//					>= PrecisionAt1_0[i])
//					PrecisionAt1_0[i] = relevantRetrieved[j].precision;
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
//			precAt1 += (double) PrecisionAt1[i];
//			precAt2 += ((double) PrecisionAt2[i]) / ((double) 2);
//			precAt3 += ((double) PrecisionAt3[i]) / ((double) 3);
//			precAt4 += ((double) PrecisionAt4[i]) / ((double) 4);
//			//END of modification
//			precAt5 += ((double) PrecisionAt5[i]) / ((double) 5);
//			PrecAt10 += ((double) PrecisionAt10[i]) / ((double) 10);
//			PrecAt15 += ((double) PrecisionAt15[i]) / ((double) 15);
//			PrecAt20 += ((double) PrecisionAt20[i]) / ((double) 20);
//			PrecAt30 += ((double) PrecisionAt30[i]) / ((double) 30);
//			PrecAt50 += ((double) PrecisionAt50[i]) / ((double) 50);
//			PrecAt100 += ((double) PrecisionAt100[i]) / ((double) 100);
//			PrecAt200 += ((double) PrecisionAt200[i]) / ((double) 200);
//			PrecAt500 += ((double) PrecisionAt500[i]) / ((double) 500);
//			PrecAt1000 += ((double) PrecisionAt1000[i]) / ((double) 1000);
		}
//		for (int i = 0; i < effQueryCounter; i++) {
//			PrecAt0Percent += (double) PrecisionAt0_0[i];
//			PrecAt10Percent += (double) PrecisionAt0_1[i];
//			PrecAt20Percent += (double) PrecisionAt0_2[i];
//			PrecAt30Percent += (double) PrecisionAt0_3[i];
//			PrecAt40Percent += (double) PrecisionAt0_4[i];
//			PrecAt50Percent += (double) PrecisionAt0_5[i];
//			PrecAt60Percent += (double) PrecisionAt0_6[i];
//			PrecAt70Percent += (double) PrecisionAt0_7[i];
//			PrecAt80Percent += (double) PrecisionAt0_8[i];
//			PrecAt90Percent += (double) PrecisionAt0_9[i];
//			PrecAt100Percent += (double) PrecisionAt1_0[i];
//		}
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
		
//		PrecAt0Percent /= (double) numberOfEffQuery;
//		PrecAt10Percent /= (double) numberOfEffQuery;
//		PrecAt20Percent /= (double) numberOfEffQuery;
//		PrecAt30Percent /= (double) numberOfEffQuery;
//		PrecAt40Percent /= (double) numberOfEffQuery;
//		PrecAt50Percent /= (double) numberOfEffQuery;
//		PrecAt60Percent /= (double) numberOfEffQuery;
//		PrecAt70Percent /= (double) numberOfEffQuery;
//		PrecAt80Percent /= (double) numberOfEffQuery;
//		PrecAt90Percent /= (double) numberOfEffQuery;
//		PrecAt100Percent /= (double) numberOfEffQuery;
//		precAt1 /= (double) numberOfEffQuery;
//		//Modified by G.AMATI 7th May 2002
//		precAt2 /= (double) numberOfEffQuery;
//		precAt3 /= (double) numberOfEffQuery;
//		precAt4 /= (double) numberOfEffQuery;
//		//END of modification
//		precAt5 /= (double) numberOfEffQuery;
//		PrecAt10 /= (double) numberOfEffQuery;
//		PrecAt15 /= (double) numberOfEffQuery;
//		PrecAt20 /= (double) numberOfEffQuery;
//		PrecAt30 /= (double) numberOfEffQuery;
//		PrecAt50 /= (double) numberOfEffQuery;
//		PrecAt100 /= (double) numberOfEffQuery;
//		PrecAt200 /= (double) numberOfEffQuery;
//		PrecAt500 /= (double) numberOfEffQuery;
//		PrecAt1000 /= (double) numberOfEffQuery;
		meanAveragePrecision /= (double) numberOfEffQuery;
		meanRelevantPrecision /= (double) numberOfEffQuery;
	}
}
