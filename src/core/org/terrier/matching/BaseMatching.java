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
 * The Original Code is BaseMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Nicola Tonellotto
 */
package org.terrier.matching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.dsms.DocumentScoreModifier;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.collections.MapEntry;
import org.terrier.utility.ApplicationSetup;


/**
 * Performs the matching of documents with a query, by first assigning scores to 
 * documents for each query term and modifying these scores with the appropriate modifiers.
 * Then, a series of document score modifiers are applied if necessary.
 * <p>
 * <b>Properties</b>
 * <ul>
 * <li><tt>matching.retrieved_set_size</tt> - The maximum number of documents in the final 
 * retrieved set. The default value is 1000, however, setting the property to 0 will return
 * all matched documents.</li>
 * <li><tt>ignore.low.idf.terms</tt> - A property that enables to ignore the terms with a 
 * low IDF.</li>
 * <li><tt>match.empty.query</tt> - whether an empty query should return all documents. 
 * Defaults to false.</li>
 * </ul>
 * @since 3.0
 * @author Vassilis Plachouras, Craig Macdonald, Nicola Tonellotto
 */
public abstract class BaseMatching implements Matching
{
	protected long totalTime = 0;
     /** the logger for this class */
	protected static final Logger logger = LoggerFactory.getLogger(Matching.class);

	/** the default namespace for the document score modifiers that are specified in the properties
	 * file. */
	protected static String dsmNamespace = "org.terrier.matching.dsms.";
	
	/** The maximum number of documents in the final retrieved set. It corresponds to the 
	 * property <tt>matching.retrieved_set_size</tt>. The default value is 1000, however, setting
	 * the property to 0 will return all matched documents. */
	protected static int RETRIEVED_SET_SIZE;
		
	/** A property that enables to ignore the terms with a low IDF. Corresponds to
	 * property <tt>ignore.low.idf.terms</tt>. Defaults to true. This can cause
	 * some query terms to be omitted in small corpora. */
	protected static boolean IGNORE_LOW_IDF_TERMS;
	
	/** A property that when it is true, it allows matching all documents to an empty 
	 * query. In this case the ordering of documents is random. More specifically, 
	 * it is the ordering of documents in the document index. */
	protected static boolean MATCH_EMPTY_QUERY;
	
	/** The number of retrieved documents for a query.*/
	protected int numberOfRetrievedDocuments;
		
	/** The index used for retrieval. */ 
	protected Index index;
	
	/** The lexicon used. */
	protected Lexicon<String> lexicon;
	
	/** The inverted file.*/
	protected PostingIndex<Pointer> invertedIndex;
	
	/** The collection statistics */
	protected CollectionStatistics collectionStatistics;
	
	/** The result set.*/
	protected ResultSet resultSet;
	
	/** Contains the document score modifiers to be applied for a query. */
	protected List<DocumentScoreModifier> documentModifiers;

	protected WeightingModel[][] wm = null;
	protected List<Map.Entry<String,LexiconEntry>> queryTermsToMatchList = null;
	
	protected BaseMatching() 
	{
	}
	/**
	 * Update the start time
	 * @param t
	 */
	public void updateStartTime(long t)
	{
		this.totalTime -= t;
	}

	/**
	 * Update the end time.
	 * @param t
	 */
	public void updateEndTime(long t)
	{
		this.totalTime += t;
	}
	
	/**
	 * get the total time
	 * @return long
	 */
	public long getTotalTime()
	{
		return this.totalTime;
	}

	/**
	 * Constructs an instance of the BaseMatching
	 * @param _index
	 */
	@SuppressWarnings("unchecked")
	public BaseMatching(Index _index) 
	{
		documentModifiers = new ArrayList<DocumentScoreModifier>();
		
		this.index = _index;
		this.lexicon = _index.getLexicon();	
		this.invertedIndex = (PostingIndex<Pointer>) _index.getInvertedIndex();
		this.collectionStatistics = _index.getCollectionStatistics();
				
		String defaultDSMS = ApplicationSetup.getProperty("matching.dsms","");
		
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
	
	protected void initialisePostings(MatchingQueryTerms queryTerms)
	{
		
		// We purge the query terms not present in the lexicon and retrieve the information from the lexicon
		String[] queryTermStrings = queryTerms.getTerms();
		queryTermsToMatchList = new ArrayList<Map.Entry<String,LexiconEntry>>(queryTermStrings.length);
		for (String queryTerm: queryTermStrings) {
			LexiconEntry t = lexicon.getLexiconEntry(queryTerm);
			if (t != null) {
				//check if the term IDF is very low.
				if (IGNORE_LOW_IDF_TERMS && collectionStatistics.getNumberOfDocuments() < t.getFrequency()) {
					logger.warn("query term " + queryTerm + " has low idf - ignored from scoring.");
					continue;
				}
				// check if the term has weighting models
				WeightingModel[] termWeightingModels = queryTerms.getTermWeightingModels(queryTerm);
				if (termWeightingModels.length == 0) {
					logger.warn("No weighting models for term " + queryTerm +", skipping scoring");
					continue;
				}
				queryTermsToMatchList.add(new MapEntry<String, LexiconEntry>(queryTerm, t));
			}
			else
				logger.debug("Term Not Found: " + queryTerm);			
		}

		//logger.warn("queryTermsToMatchList = " + queryTermsToMatchList.size());
		int queryLength = queryTermsToMatchList.size();
		
		wm = new WeightingModel[queryLength][];
		for (int i = 0; i < queryLength; i++) 
		{
			Map.Entry<String, LexiconEntry> termEntry    = queryTermsToMatchList.get(i);
			String 							queryTerm    = termEntry.getKey();
			LexiconEntry 					lexiconEntry = termEntry.getValue();
			//get the entry statistics - perhaps this came from "far away"
			EntryStatistics entryStats = queryTerms.getStatistics(queryTerm);
			//if none were provided with the query we seek the entry statistics query term in the lexicon
			if (entryStats == null)
			{
				entryStats = lexiconEntry;
				//save them as they may be useful for query expansion. HOWEVER ONLY IF we didnt
				//get the statistics from MQT in the first place
				queryTerms.setTermProperty(queryTerm, lexiconEntry);
			}

			// Initialise the weighting models for this term
			int numWM = queryTerms.getTermWeightingModels(queryTerm).length;
			wm[i] = new WeightingModel[numWM];
			for (int j = 0; j < numWM; j++) {
				wm[i][j] = (WeightingModel) queryTerms.getTermWeightingModels(queryTerm)[j].clone();
				wm[i][j].setCollectionStatistics(collectionStatistics);
				wm[i][j].setEntryStatistics(entryStats);
				wm[i][j].setRequest(queryTerms.getRequest());
				wm[i][j].setKeyFrequency(queryTerms.getTermWeight(queryTerm));
				IndexUtil.configure(index, wm[i][j]);
				wm[i][j].prepare();
			}
		}
	}
	
	protected void initialise(MatchingQueryTerms queryTerms) 
	{
		//System.gc();
		
		updateStartTime(System.currentTimeMillis());
		
		RETRIEVED_SET_SIZE   = Integer.parseInt(ApplicationSetup.getProperty("matching.retrieved_set_size", "1000"));
		IGNORE_LOW_IDF_TERMS = Boolean.parseBoolean(ApplicationSetup.getProperty("ignore.low.idf.terms","true"));
		MATCH_EMPTY_QUERY    = Boolean.parseBoolean(ApplicationSetup.getProperty("match.empty.query","false"));
		
		this.numberOfRetrievedDocuments = 0;

		initialisePostings(queryTerms);
	}
	
	protected void finalise(MatchingQueryTerms queryTerms)
	{
		// resultSet.initialise();
		
		logger.debug("Number of docs with +ve score: "+numberOfRetrievedDocuments);
		//sort in descending score order the top RETRIEVED_SET_SIZE documents
		//long sortingStart = System.currentTimeMillis();
		//we need to sort at most RETRIEVED_SET_SIZE, or if we have retrieved
		//less documents than RETRIEVED_SET_SIZE then we need to find the top 
		//numberOfRetrievedDocuments.
		int set_size = Math.min(RETRIEVED_SET_SIZE, numberOfRetrievedDocuments);
		if (set_size == 0) 
			set_size = numberOfRetrievedDocuments;
		
		//sets the effective size of the result set.
		resultSet.setExactResultSize(numberOfRetrievedDocuments);
		
		//sets the actual size of the result set.
		resultSet.setResultSize(set_size);
		
		resultSet.sort(set_size);
		//long sortingEnd = System.currentTimeMillis();
		
		/*we apply the query dependent document score modifiers first and then 
		we apply the application dependent ones. This is to ensure that the 
		BooleanFallback modifier is applied last. If there are more than 
		one application dependent dsms, then it's up to the application, ie YOU!
		to ensure that the BooleanFallback is applied last.*/
		
		/* dsms each require resorts of the result list. This is expensive, so should
		   be avoided if possible. Sorting is only done if the dsm actually altered any scores */

		/*query dependent modification of scores
		of documents for a query, defined by this query*/
		
		// Load in the document score modifiers
		int numOfDocModifiers = documentModifiers.size();
		int NumberOfQueryDSMs = 0;
		DocumentScoreModifier[] dsms = queryTerms.getDocumentScoreModifiers();
		if (dsms != null)
			NumberOfQueryDSMs = dsms.length;

		for (int t = NumberOfQueryDSMs-1; t >= 0; t--) {
			if (dsms[t].modifyScores(index, queryTerms, resultSet))
				resultSet.sort(resultSet.getResultSize());
		}
		
		/*application dependent modification of scores
		of documents for a query, based on a static set by the client code
		sorting the result set after applying each DSM*/
		for (int t = 0; t < numOfDocModifiers; t++) {
			if (documentModifiers.get(t).modifyScores(index, queryTerms, resultSet))
				resultSet.sort(resultSet.getResultSize());
		}
		logger.debug("number of retrieved documents: " + resultSet.getResultSize());
		
		updateEndTime(System.currentTimeMillis());
	}
	
	/** {@inheritDoc} */
	public void setCollectionStatistics(CollectionStatistics cs)
	{
		collectionStatistics = cs;
	}
	/** {@inheritDoc} */
	public abstract String getInfo();
	/** {@inheritDoc} */
	public abstract ResultSet match(String queryNumber, MatchingQueryTerms queryTerms) throws IOException;	
}
