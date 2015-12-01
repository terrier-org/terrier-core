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
 * The Original Code is BooleanFallback.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching.dsms;

import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.ResultSet;
import org.terrier.querying.parser.Query;
import org.terrier.querying.parser.RequirementQuery;
import org.terrier.querying.parser.SingleTermQuery;
import org.terrier.structures.Index;

/**
 * This class provides a boolean fallback document score modifier for 
 * matching. In other words, if there any of the retrieved documents
 * contain all undecorated query terms (ie query terms without any operators),
 * then we remove from the result set documents that do not contain all
 * undecorated query terms. Otherwise, we do nothing.
 * 
 * @author Vassilis Plachouras &amp; Craig Macdonald
  */
public class BooleanFallback implements DocumentScoreModifier {
	protected static final Logger logger = LoggerFactory.getLogger(BooleanFallback.class);

	/** Builds a hashset containing all terms that are required NOT
	 * to be in the query
	 * @param q The original query as was used to generate MatchingQueryTerms
	 * @return See above
	 */
	protected HashSet<String> getMinusTerms(Query q)
	{
		ArrayList<Query> requirements = new ArrayList<Query>();
		q.obtainAllOf(RequirementQuery.class, requirements);
		ArrayList<Query> terms = new ArrayList<Query>();
		for(Query g : requirements)
		{
			RequirementQuery r = (RequirementQuery)g;
			if (! r.getRequired())
				r.obtainAllOf(SingleTermQuery.class, terms);
		}
		HashSet<String> rtr = new HashSet<String>(terms.size());
		for(Query g : terms)
		{
			SingleTermQuery queryTerm = (SingleTermQuery)g;
			rtr.add(queryTerm.getTerm());
			if(logger.isDebugEnabled()){
				logger.debug("-"+queryTerm.getTerm());
			}
		}
		return rtr;
	}
	
	/**
	 * Applies boolean fallback to the given result set.
	 * @param index The data structures used for retrieval.
	 * @param queryTerms the terms of the query.
	 * @param resultSet the set of retrieved documents for the query.
	 * @return true if any scores have been altered
	 */
	public boolean modifyScores(Index index, MatchingQueryTerms queryTerms, ResultSet resultSet) {
		
		
		/* generate the query mask */
		short queryMask = (short)0;
		//get all the query terms
		String[] queryTermStrings = queryTerms.getTerms();
		if (queryTermStrings.length < 2)
			return false;

		//get all the "requirement false" (ie -ve) terms
		HashSet<String> reqs = getMinusTerms(queryTerms.getQuery());
		
		for (int i=0;i<queryTermStrings.length;i++) //for each (all terms)
		{
			//mask 1 IFF terms does NOT occur in -ve terms
			if (!reqs.contains(queryTermStrings[i]))
				queryMask = (short) (queryMask | (1 << i)); 
		}
			
		//creating local references to faraway arrays
		short[] occurrences = resultSet.getOccurrences();
		double[] scores = resultSet.getScores();
		
		/* see if any documents match the query mask */
		boolean applyFilter = false;
		final int numOfDocs = resultSet.getResultSize();
		for (int i=0; i<numOfDocs; i++) {
			if (scores[i]>0.0d && ((occurrences[i] & queryMask) == queryMask)) {
				applyFilter = true;
				break;
			}
		}
		
		/* if any documents do match the query mask, remove any documents
		that do not the query mask */
		int numOfModifiedDocs = 0;
		if (applyFilter) {
			for (int i=0; i<numOfDocs; i++) {
				if (scores[i] > 0.0d && ((occurrences[i] & queryMask) != queryMask)) {
					numOfModifiedDocs++;
					scores[i] = Double.NEGATIVE_INFINITY;
				}
			}
			resultSet.setResultSize(numOfDocs -numOfModifiedDocs);
			return true;
		}
		return false;		
	}

	/** 
	 * Returns the name of the modifier, which is BooleanFallback.
	 * @return the name of the modifier.
	 */
	public String getName() {
		return "BooleanFallback";
	}
	/** {@inheritDoc}*/ @Override 
	/* Copies this object. It has not state. */
	public Object clone() {
		return (Object)this;
	}

}
