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
 * The Original Code is FatScoringMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.matching;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;
import org.terrier.matching.dsms.DocumentScoreModifier;
import org.terrier.matching.models.WeightingModel;
import org.terrier.matching.models.WeightingModelFactory;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.HeapSort;
/** Scores a FatResultSet into a normal ResultSet for a given weighting model
 * @since 4.0
 * @author Craig Macdonald
 */ 
public class FatScoringMatching extends AbstractScoringMatching {
	
	/** check for weighting models giving NaN scores */
	static final boolean DEBUG = true;
	
	
	protected static final boolean SCORE_ONLY_FROM_MQT = Boolean.parseBoolean(ApplicationSetup.getProperty("fat.scoring.only.mqt", "false"));
	
	public FatScoringMatching(Index _index, Matching _parent, WeightingModel _wm, Predicate<Pair<String,Set<String>>> _filter)
	{
		super(_index, _parent, _wm, _filter);
	}
	
	public FatScoringMatching(Index _index, Matching _parent, WeightingModel _wm)
	{
		super(_index, _parent, _wm);
	}
	
	public FatScoringMatching(Index _index, Matching _parent)
	{
		super(_index, _parent, ApplicationSetup.getProperty("fat.scoring.matching.model", ApplicationSetup.getProperty("trec.model", "BM25")).equals("FromMQT")
				? null
				: WeightingModelFactory.newInstance(
						ApplicationSetup.getProperty("fat.scoring.matching.model", 
						ApplicationSetup.getProperty("trec.model", "BM25"))
				)
			);
	}
	
	@Override
	public String getInfo() {
		return "FatScoringMatching";
	}

	protected static boolean containsFieldPostings(WritablePosting[][] postings)
	{
		boolean _fields = false;
		CHECKFIELDS: for(int di=0;di<postings.length;di++)
		{
			
			for(int ti=0;ti<postings[0].length;ti++)
			{
				if (postings[di][ti] != null)
				{
					if (postings[di][ti] instanceof FieldPosting)
						_fields = true;
					break CHECKFIELDS;
				}
			}
		}
		return _fields;
	}
	
	@Override
	public ResultSet doMatch(String queryNumber, MatchingQueryTerms queryTerms, ResultSet inputRS) throws IOException
	{
		final int[] docids = inputRS.getDocids()/*.clone()*/; //cloning is unnecessary, as sort is usually disabled when called from FatScoringMatching
		final short[] occurs = inputRS.getOccurrences()/*.clone()*/;
		//final double[] scores = inputRS.getScores();\
		//UNABLE to produce this bug using a junit
		final double[] scores = new double[inputRS.getResultSize()];
		
		final FatResultSet fInputRS = (FatResultSet)inputRS;
		
		final int numDocs = docids.length;

		System.err.println("mqt has " +  queryTerms.size() + " terms while fatresultset has " + fInputRS.getQueryTerms().length);
		System.err.println(filterTerm == null ? "Using all of " : "Filtering from " + (SCORE_ONLY_FROM_MQT ? "former" : "latter") + " for scoring");
		//we rely on the MQT to define the query terms to score
		//in doing so, we assume that the query termis in mqt 
		//are a subset of those in the FatResultSet
		assert (!SCORE_ONLY_FROM_MQT) || queryTerms.size() <= fInputRS.getQueryTerms().length;
		assert (!SCORE_ONLY_FROM_MQT) || queryTerms.get(0).toString().equals(fInputRS.getQueryTerms()[0]);
		final int numTerms = SCORE_ONLY_FROM_MQT
			? queryTerms.size()
			: fInputRS.getQueryTerms().length;

		final WritablePosting[][] postings = fInputRS.getPostings();
		final EntryStatistics[] entryStats = fInputRS.getEntryStatistics();
		final CollectionStatistics collStats = fInputRS.getCollectionStatistics();
		final double[] keyFreqs = fInputRS.getKeyFrequencies();
		final boolean[] okToScore = new boolean[numTerms];
		
		WeightingModel[] wms = new WeightingModel[numTerms];
		//initialise the weighting models
		String c = ApplicationSetup.getProperty("fat.scoring.matching.model.c", null);
		if (c != null)
			this.wm.setParameter(Double.parseDouble(c));
		for(int ti=0;ti<numTerms;ti++)
		{
			//check if this term has been suppressed by the filter
			okToScore[ti] = true;
			if (filterTerm != null)
				okToScore[ti] = filterTerm.test(Pair.of(fInputRS.getQueryTerms()[ti],fInputRS.getTags()[ti]));
			if (! okToScore[ti])
			{
				System.err.println("Term: "+fInputRS.getQueryTerms()[ti]+" not scored for wm " + wm.getInfo());
				continue;
			}
			if (DEBUG)
				System.err.println("Term: " + fInputRS.getQueryTerms()[ti] + " qtw="+keyFreqs[ti] + " es="+entryStats[ti] + " scored for wm " + wm.getInfo());
			
			if (wm != null)
				wms[ti] = (WeightingModel) wm.clone();
			else
				wms[ti] = (WeightingModel) queryTerms.defaultWeightingModel.clone();
			wms[ti].setEntryStatistics(entryStats[ti]);
			wms[ti].setCollectionStatistics(collStats);
			wms[ti].setKeyFrequency(keyFreqs[ti]);
			wms[ti].prepare();
			
		}
		//rescore the documents
		int gt0 = 0;
		
		for(int di=0;di<numDocs;di++)
		{
			double score = 0.0d;
			if (postings[di] == null)
				continue;
			for(int ti=0;ti<numTerms;ti++)
			{
				if (postings[di][ti] != null && okToScore[ti]) //check if scoring
				{
					assert postings[di][ti].getId() == docids[di] : "At position "+di+" in resultset, Posting id " + docids[di] + " for term "+ti+" was expected, found id " +postings[di][ti].getId()+ " with contents " + postings[di][ti].toString() ;
					assert postings[di][ti].getFrequency() > 0;
					final WritablePosting p = postings[di][ti];
					final double s = wms[ti].score(p);
					//System.err.println(docids[di] + " score=" + s);
					if (DEBUG && (Double.isNaN(s) || Double.isInfinite(s)))
					{
						System.err.println(wms[ti].getInfo() + " was "+s+": posting=(" +  p.toString() + ") for term " + ti + " ks=" + keyFreqs[ti] + " es="+ entryStats[ti] + " l=" + p.getDocumentLength());
						if (p instanceof FieldPosting)
							System.err.println("lf="+ Arrays.toString(((FieldPosting)p).getFieldLengths()));
					}
					score += s;
				}
			}
			scores[di] = score;
			if (score > 0.0d)
				gt0++;
		}
		logger.info("Rescoring found " + gt0 + " docs with +ve score using " + wm.getInfo());
		//make a new resultset
		ResultSet outputRS = new QueryResultSet(docids, scores, occurs);
		if (fInputRS.hasMetaItems("docno"))
			outputRS.addMetaItems("docno", fInputRS.getMetaItems("docno"));
		if (sort)
			HeapSort.descendingHeapSort(outputRS.getScores(), outputRS.getDocids(), outputRS.getOccurrences(), numDocs);
		int numOfDocModifiers = documentModifiers.size();
		int NumberOfQueryDSMs = 0;
		DocumentScoreModifier[] dsms = queryTerms.getDocumentScoreModifiers();
		if (dsms != null)
			NumberOfQueryDSMs = dsms.length;

		for (int t = NumberOfQueryDSMs-1; t >= 0; t--) {
			if (dsms[t].modifyScores(index, queryTerms, outputRS) && sort)
				outputRS.sort();
		}
		
		/*application dependent modification of scores
		of documents for a query, based on a static set by the client code
		sorting the result set after applying each DSM*/
		for (int t = 0; t < numOfDocModifiers; t++) {
			if (documentModifiers.get(t).modifyScores(index, queryTerms, outputRS) && sort)
				outputRS.sort();
		}
		return outputRS;
	}
	
	@Override
	public ResultSet match(String queryNumber, MatchingQueryTerms queryTerms)
		throws IOException 
	{
		return doMatch(queryNumber, queryTerms, parent.match(queryNumber, queryTerms));
	}

	@Override
	public void setCollectionStatistics(CollectionStatistics cs) {
		throw new UnsupportedOperationException();
	}

}
