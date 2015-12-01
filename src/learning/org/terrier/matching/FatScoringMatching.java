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
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.matching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class FatScoringMatching implements Matching {

	/** the default namespace for the document score modifiers that are specified in the properties
	 * file. */
	protected static String dsmNamespace = "org.terrier.matching.dsms.";
	
	static final Logger logger = LoggerFactory.getLogger(FatScoringMatching.class);
	
	/** check for weighting models giving NaN scores */
	static final boolean DEBUG = true;
	
	Matching parent;
	/** Contains the document score modifiers to be applied for a query. */
	protected List<DocumentScoreModifier> documentModifiers = new ArrayList<DocumentScoreModifier>();
	
	Index index;
	WeightingModel wm;
	public boolean sort = true;
	
	public FatScoringMatching(Index _index, Matching _parent, WeightingModel _wm)
	{
		this.wm = _wm;
		this.parent = _parent;
		this.index =_index;
		String c = ApplicationSetup.getProperty("fat.scoring.matching.model.c", null);
		if (c != null)
			this.wm.setParameter(Double.parseDouble(c));
		String defaultDSMS =  ApplicationSetup.getProperty("fat.scoring.matching.dsms", ApplicationSetup.getProperty("matching.dsms",""));
		
		try {
			for(String modifierName : defaultDSMS.split("\\s*,\\s*")) {
				if (modifierName.length() == 0)
                    continue;
				if (modifierName.indexOf('.') == -1)
					modifierName = dsmNamespace + modifierName;
				documentModifiers.add((DocumentScoreModifier)Class.forName(modifierName).newInstance());
			}
		} catch(Exception e) {
			logger.error("Exception while initialising default modifiers. Please check the name of the modifiers in the configuration file.", e);
		}
	}
	
	public FatScoringMatching(Index _index, Matching _parent)
	{
		this(
				_index, 
				_parent, 
				ApplicationSetup.getProperty("fat.scoring.matching.model", ApplicationSetup.getProperty("trec.model", "BM25")).equals("FromMQT")
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
	
	public ResultSet doMatch(String queryNumber, MatchingQueryTerms queryTerms, ResultSet inputRS) throws IOException
	{
		final int[] docids = inputRS.getDocids()/*.clone()*/; //cloning is unnecessary, as sort is usually disabled when called from FatScoringMatching
		final short[] occurs = inputRS.getOccurrences()/*.clone()*/;
		//final double[] scores = inputRS.getScores();\
		//UNABLE to produce this bug using a junit
		final double[] scores = new double[inputRS.getResultSize()];
		
		final FatResultSet fInputRS = (FatResultSet)inputRS;
		
		final int numDocs = docids.length;
		final int numTerms = fInputRS.getQueryTerms().length;
		final WritablePosting[][] postings = fInputRS.getPostings();
		final EntryStatistics[] entryStats = fInputRS.getEntryStatistics();
		final CollectionStatistics collStats = fInputRS.getCollectionStatistics();
		final double[] keyFreqs = fInputRS.getKeyFrequencies();
		
		WeightingModel[] wms = new WeightingModel[numTerms];
		//initialise the weighting models
		String c = ApplicationSetup.getProperty("fat.scoring.matching.model.c", null);
		if (c != null)
			this.wm.setParameter(Double.parseDouble(c));
		for(int ti=0;ti<numTerms;ti++)
		{
			if (wm != null)
				wms[ti] = (WeightingModel) wm.clone();
			else
				wms[ti] = (WeightingModel) queryTerms.defaultWeightingModel.clone();
			wms[ti].setEntryStatistics(entryStats[ti]);
			wms[ti].setCollectionStatistics(collStats);
			wms[ti].setKeyFrequency(keyFreqs[ti]);
			wms[ti].prepare();
			if (DEBUG)
				System.err.println("Term: " + fInputRS.getQueryTerms()[ti] + " qtw="+keyFreqs[ti] + " es="+entryStats[ti]);
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
				if (postings[di][ti] != null)
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
		logger.info("Rescoring found " + gt0 + " docs with +ve score using " + wms[0].getInfo());
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
