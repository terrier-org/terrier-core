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
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.dsms.DocumentScoreModifier;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
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
public abstract class BaseReentrantMatching implements Matching
{
	public static final String BASE_MATCHING_TAG = "firstmatchscore";
	public static final String NONMATCHING_TAG = "firstkeep";
	
    /** the logger for this class */
	protected static final Logger logger = LoggerFactory.getLogger(Matching.class);

	/** the default namespace for the document score modifiers that are specified in the properties
	 * file. */
	protected static String dsmNamespace = "org.terrier.matching.dsms.";
	
	/** A property that enables to ignore the terms with a low IDF. Corresponds to
	 * property <tt>ignore.low.idf.terms</tt>. Defaults to true. This can cause
	 * some query terms to be omitted in small corpora. */
	protected static boolean IGNORE_LOW_IDF_TERMS;
	
	/** A property that when it is true, it allows matching all documents to an empty 
	 * query. In this case the ordering of documents is random. More specifically, 
	 * it is the ordering of documents in the document index. */
	protected static boolean MATCH_EMPTY_QUERY;

	protected static class MatchingState {

		/** input query to matching */
		public MatchingQueryTerms queryTerms;

		/** The result set.*/
		public ResultSet resultSet;

		/** The number of documents that are requested to be retrieved for this query.*/
		public int numberOfRequestedDocuments = 0;

		/** The number of actually retrieved documents for this query.*/
		public int numberOfRetrievedDocuments = 0;

		public long totalTime = 0;
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

	}
	
	/** The index used for retrieval. */ 
	protected Index index;
	
	/** The lexicon used. */
	protected Lexicon<String> lexicon;
	
	/** The inverted file.*/
	protected PostingIndex<Pointer> invertedIndex;
	
	/** The collection statistics */
	protected CollectionStatistics collectionStatistics;
		
	/** Contains the document score modifiers to be applied for a query. */
	protected List<DocumentScoreModifier> documentModifiers;

	protected BaseReentrantMatching()  {}
	

	/**
	 * Constructs an instance of the BaseMatching
	 * @param _index
	 */
	@SuppressWarnings("unchecked")
	public BaseReentrantMatching(Index _index) 
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
				documentModifiers.add(ApplicationSetup.getClass(modifierName).asSubclass(DocumentScoreModifier.class).newInstance());
			}
		} catch(Exception e) {
			logger.error("Exception while initialising default modifiers. Please check the name of the modifiers in the configuration file.", e);
		}
	}

	protected MatchingState initialiseState()
	{
		return new MatchingState();
	}
	
	protected void initialisePostings(MatchingState state)
	{}
	
	protected MatchingState initialise(MatchingQueryTerms queryTerms) 
	{
		MatchingState state = initialiseState();
		state.updateStartTime(System.currentTimeMillis());
		state.queryTerms = queryTerms;
		
		state.numberOfRequestedDocuments = Integer.parseInt(ApplicationSetup.getProperty("matching.retrieved_set_size", "1000"));
		if (queryTerms.getMatchingRequestSize() > -1) {
			state.numberOfRequestedDocuments = queryTerms.getMatchingRequestSize();
		}
		IGNORE_LOW_IDF_TERMS = Boolean.parseBoolean(ApplicationSetup.getProperty("ignore.low.idf.terms","false"));
		MATCH_EMPTY_QUERY    = Boolean.parseBoolean(ApplicationSetup.getProperty("match.empty.query","false"));
		return state;
	}
	
	protected void finalise(MatchingState state)
	{
		MatchingQueryTerms queryTerms = state.queryTerms;
		
		logger.debug("Number of docs with +ve score: "+state.numberOfRetrievedDocuments);
		//sort in descending score order the top RETRIEVED_SET_SIZE documents
		//long sortingStart = System.currentTimeMillis();
		//we need to sort at most RETRIEVED_SET_SIZE, or if we have retrieved
		//less documents than RETRIEVED_SET_SIZE then we need to find the top 
		//numberOfRetrievedDocuments.
		int set_size = Math.min(RETRIEVED_SET_SIZE, state.numberOfRetrievedDocuments);
		if (set_size == 0) 
			set_size = state.numberOfRetrievedDocuments;
		
		//sets the effective size of the result set.
		state.resultSet.setExactResultSize(state.numberOfRetrievedDocuments);
		
		//sets the actual size of the result set.
		state.resultSet.setResultSize(set_size);
		
		state.resultSet.sort(set_size);
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
			if (dsms[t].modifyScores(index, queryTerms, state.resultSet))
			state.resultSet.sort(state.resultSet.getResultSize());
		}
		
		/*application dependent modification of scores
		of documents for a query, based on a static set by the client code
		sorting the result set after applying each DSM*/
		for (int t = 0; t < numOfDocModifiers; t++) {
			if (documentModifiers.get(t).modifyScores(index, queryTerms, state.resultSet))
			state.resultSet.sort(state.resultSet.getResultSize());
		}
		logger.debug("query "+ queryTerms.getQueryId() +" number of retrieved documents: " + state.resultSet.getResultSize());
		state.updateEndTime(System.currentTimeMillis());
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
