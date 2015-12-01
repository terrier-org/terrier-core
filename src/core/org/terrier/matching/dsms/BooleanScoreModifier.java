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
 * The Original Code is BooleanScoreModifier.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author) 
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching.dsms;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.ResultSet;
import org.terrier.querying.parser.Query;
import org.terrier.querying.parser.SingleTermQuery;
import org.terrier.structures.Index;
/**
 * If not all the query terms appear in a document, then this
 * modifier zeros the document's score.  
 * @author Vassilis Plachouras and Craig Macdonald
  */
public class BooleanScoreModifier implements DocumentScoreModifier, Serializable {
	private static final long serialVersionUID = 8827289509840106672L;

	/** the logger for this class */
	protected static final Logger logger = LoggerFactory.getLogger(BooleanScoreModifier.class);
	
	/** 
	 * The terms to check. If this is null, then we 
	 * check for the whole query. This property can 
	 * only be set from the constructor.
	 */
	protected ArrayList<Query> terms = null;
	
	/**
	 * An empty default constructor. 
	 */
	public BooleanScoreModifier() {}
	
	/** 
	 * A constructor where we specify which of the 
	 * query terms should exist in the documents.
	 * @param ts ArrayList the query terms that should 
	 *        appear in the retrieved documents after
	 *        applying this modifier.
	 */
	public BooleanScoreModifier(ArrayList<Query> ts) {
		terms = ts;
	}
	
	/** 
	 * Returns the name of the document score modifier.
	 * @return String the name of the modifier.
	 */
	public String getName() {
		return "BooleanScoreModifier";
	}
	
	/**
	 * Zeros the scores of documents in which only some
	 * of the query terms appear.
	 * @param index Index the data structures used for retrieval.
	 * @param query TermTreeNode[] the array of the query terms.
	 * @param resultSet ResultSet the set of retrieved documents.
	 */
	public boolean modifyScores(Index index, MatchingQueryTerms query, ResultSet resultSet) {
		short[] occurrences = resultSet.getOccurrences();
		double[] scores = resultSet.getScores();
		//int[] docids = resultSet.getDocids();
		int size = resultSet.getResultSize();
		int start = 0;
		int end = size;
		int numOfModifiedDocumentScores = 0;
		short queryLengthMask = 0;
		
		//set the bit mask
		if (terms !=null) {
			HashSet<String> set = new HashSet<String>();
			for(Query qt : terms)
				set.add( ((SingleTermQuery)qt).getTerm() );
			
			final String[] queryTerms = query.getTerms();
			for (int i=0; i < queryTerms.length; i++) {
				if (set.contains((String)queryTerms[i]))
					queryLengthMask |= (short)(1 << i);
			}
			
		} else {
			for (int i = 0; i < query.length(); i++) {
				queryLengthMask = (short)((queryLengthMask << 1) + 1);
			}
		}
		//modify the scores
		for (int i = start; i < end; i++) {
			
			if ((occurrences[i] & queryLengthMask) != queryLengthMask) {
				if (scores[i] > Double.NEGATIVE_INFINITY)
					numOfModifiedDocumentScores++;
				scores[i] = Double.NEGATIVE_INFINITY;
			}
		}
		logger.debug("BooleanScoreModifier modified score for "+ numOfModifiedDocumentScores +" documents");
		if (numOfModifiedDocumentScores == 0)
			return false;
		resultSet.setResultSize(size -numOfModifiedDocumentScores);
		resultSet.setExactResultSize(resultSet.getExactResultSize()-numOfModifiedDocumentScores);
		return true;
	}

	/** Clone this DSM. Note that terms is shall copied. */
	public Object clone()
	{
		return new BooleanScoreModifier(new ArrayList<Query>(terms));
	}
	
}
