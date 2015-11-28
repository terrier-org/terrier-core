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
 * The Original Code is NamedPageEvaluation.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Ben He <ben{a.}dcs.gla.ac.uk>
 *    
 */
package org.terrier.evaluation;
import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.terrier.utility.Rounding;
/**
 * Performs the evaluation for TREC's named/home page finding task.
 * The evaluation measure is the average reciprocal rank of those relevant
 * documents that are among the top 50 documents.
 * @author Vassilis Plachouras
  */
public class NamedPageEvaluation extends Evaluation {
	/** The average reciprocal rank.*/
	protected double arr;
	/** total number of queries.*/
	protected int numberOfQueries;
	/** the rank of the correct answer for each query evaluated.*/
	protected TIntIntHashMap rankRelevantDocument;
	/** The number of queries where the answer is found in the top 50 docs.*/
	protected int inTop50;
	/** The number of queries for which the answer is found in the top 20 docs.*/
	protected int inTop20;
	/** The number of queries where the answer is found in the top 10 docs.*/
	protected int inTop10;
	/** The number of queries where the answer is found in the top 5 docs.*/
	protected int inTop5;
	/** The number of queries where the answer is found in the top first rank.*/
	protected int inTop1;
	/** The number of queries where the answer was not found in the top 50 documents.*/
	protected int notInTop50;
	/** The query numbers' vector.*/
	protected TIntHashSet queryNumbers;
	/** The reciprocal rank of each query. */
	protected TIntDoubleHashMap recipRank;
	/**
	 * Evaluates the given result file.
	 * @param resultFilename String the filename 
	 *        of the result file to evaluate.
	 */
	public void evaluate(String resultFilename) {
		logger.info("Result file: "+resultFilename);
		queryNumbers = new TIntHashSet();
		recipRank = new TIntDoubleHashMap();
		//initialise the arr
		arr = 0.0d;
		inTop50 = 0;
		inTop20 = 0;
		inTop10 = 0;
		inTop1 = 0;
		notInTop50 = 0;
		rankRelevantDocument = new TIntIntHashMap();
		//int queryCounter = -1;
		int previousQueryId = -1;
		try {
			final BufferedReader br = Files.openFileReader(resultFilename);
			int firstSpaceIndex;
			int secondSpaceIndex;
			int thirdSpaceIndex;
			int fourthSpaceIndex;
			int queryId;
			String docno;
			String rankString;
			int rank;
			String line = null;
			boolean foundRelevantForQuery = false;
			while ((line = br.readLine()) != null) {
				firstSpaceIndex = line.indexOf(' ');
				String queryIdString = line.substring(0, firstSpaceIndex);
				secondSpaceIndex = line.indexOf(' ', firstSpaceIndex + 1);
				thirdSpaceIndex = line.indexOf(' ', secondSpaceIndex + 1);
				docno = line.substring(secondSpaceIndex + 1, thirdSpaceIndex);
				fourthSpaceIndex = line.indexOf(' ', thirdSpaceIndex + 1);
				rankString =
					line.substring(thirdSpaceIndex + 1, fourthSpaceIndex);
				rank = (new Integer(rankString)).intValue() + 1;
				//remove non-numeric letters in the queryNo
				StringBuilder queryNoTmp = new StringBuilder();
				boolean firstNumericChar = false;
				for (int i = queryIdString.length()-1; i >=0; i--){
					if (queryIdString.charAt(i) >= '0' && queryIdString.charAt(i) <= '9'){
						queryNoTmp.append(queryIdString.charAt(i));
						firstNumericChar = true;
					}
					else if (firstNumericChar)
						break;
				}
				
				queryId = Integer.parseInt(queryNoTmp.reverse().toString());
				if (!qrels.queryExistInQrels(queryIdString))
					continue;
				
				if (previousQueryId != queryId) {
					//queryCounter++;
					queryNumbers.add(queryId);
					foundRelevantForQuery = false;
					rankRelevantDocument.put(queryId, 0);
				}
				previousQueryId = queryId;
				if (!foundRelevantForQuery && qrels.isRelevant(queryIdString, docno)) {
					if (rankRelevantDocument.get(queryId)<=0) {
						rankRelevantDocument.put(queryId, rank);
						foundRelevantForQuery = true;
					}
				}
			}
			numberOfQueries = queryNumbers.size();
			int[] queryids = queryNumbers.toArray();
			for (int i = 0; i < rankRelevantDocument.size(); i++) {
				rank = rankRelevantDocument.get(queryids[i]); 
				if (rank > 0
					&& rank <= 50)
					inTop50++;
				if (rank > 0
					&& rank <= 20)
					inTop20++;
				if (rank > 0
					&& rank <= 10)
					inTop10++;
				if (rank > 0
					&& rank <= 5)
					inTop5++;
				if (rank > 0
					&& rank == 1)
					inTop1++;
				if (rank > 0
					&& rank > 50)
					notInTop50++;
				if (rank == 0)
					notInTop50++;
				if (rank > 0
					&& rank <= 50){
					arr += 1.0D / (1.0D * rank);
					recipRank.put(queryids[i], 1.0D / (1.0D * rank));
				}
			}
			br.close();
		} catch (IOException ioe) {
			logger.error(
				"An error occured while reading the relevance assessments. Stack trace follows.",ioe);
		}
	}
	/**
	 * Output the evaluation result of each query to the specific file.
	 * @param resultEvalFilename String the name of the file in which to 
	 *        save the evaluation results.
	 */
	public void writeEvaluationResultOfEachQuery(String resultEvalFilename) {
		int[] queryids = queryNumbers.toArray();
		Arrays.sort(queryids);
		try {
			final PrintWriter out = new PrintWriter(Files.writeFileWriter(resultEvalFilename));
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < this.queryNumbers.size(); i++)
			{
				sb.append(
					queryids[i]
						+ " "
						+ Rounding.toString(
							recipRank.get(queryids[i]),
							4)
						+ ApplicationSetup.EOL);
			}
			out.print(sb.toString());
			out.close();
		} catch (IOException fnfe) {
			logger.error("Couldn't write evaluation file "+resultEvalFilename, fnfe);
		}
	}
	/**
	 * Output the evaluation result to the specific file.
	 * @param out PrintWriter the name of the stream to output the result.
	 */
	public void writeEvaluationResult(PrintWriter out) {
		int[] queryids = queryNumbers.toArray();
		Arrays.sort(queryids);
		for (int i = 0; i < rankRelevantDocument.size(); i++) {
			int rank = rankRelevantDocument.get(queryids[i]); 
			if (rank == 0 || rank > 50)
				out.println(
					"for query "
						+ queryids[i]
						+ " no relevant document was found.");
			else
				out.println(
					"for query "
						+ queryids[i]
						+ " 1st Relevant document at rank "
						+ rank);
		}
		out.println(
			"Average Reciprocal Rank: " + (arr / (1.0d * numberOfQueries)));
		out.println(
			"Percentage of answers found in the first rank: "
				+ (inTop1 / (1.0D * numberOfQueries)));
		out.println(
			"Percentage of answers found among the top 5 documents: "
				+ (inTop5 / (1.0D * numberOfQueries)));
		out.println(
			"Percentage of answers found among the top 10 documents: "
				+ (inTop10 / (1.0D * numberOfQueries)));
		out.println(
			"Percentage of answers found among the top 20 documents: "
				+ (inTop20 / (1.0D * numberOfQueries)));
		out.println(
			"Percentage of answers found among the top 50 documents: "
				+ (inTop50 / (1.0D * numberOfQueries)));
		out.println(
			"Percentage of documents not found in top 50 documents: "
				+ (notInTop50 / (1.0D * numberOfQueries)));
		logger.info(
				"Average Reciprocal Rank: " + (arr / (1.0d * numberOfQueries)));
	}
}
