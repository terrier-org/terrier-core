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
 * The Original Code is ExpansionTerms.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.querying;

import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntObjectHashMap;

import java.io.IOException;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.models.queryexpansion.QueryExpansionModel;
import org.terrier.querying.parser.SingleTermQuery;
import org.terrier.structures.EntryStatistics;
/** Base class for weighting term occurrences in feedback documents. 
 * @since 3.0
 * @author Craig Macdonald
 */
public abstract class ExpansionTerms
{
	protected static final Logger logger = LoggerFactory.getLogger(ExpansionTerms.class);
	
	protected static final Comparator<ExpansionTerm> EXPANSIONTERM_DESC_SCORE_SORTER = new Comparator<ExpansionTerm>()
	{
		public int compare(ExpansionTerm t1, ExpansionTerm t2)
		{
			final double res = t1.getWeightExpansion() - t2.getWeightExpansion();
			return res>0.0d ? -1 : (res< 0.0d ? 1 : 0);
		}
	};

	protected ExpansionTerms(){}	

	/** The original query terms. Used only for Conservative Query Expansion,
	 * where no terms are added to the query, only the existing ones are 
	 * reweighted. */
	protected TIntObjectHashMap<String> originalTermids = new TIntObjectHashMap<String>();
	/**
	 * The frequency of each query term in the original query. Currently used by RM3 only.
	 */
	protected TIntDoubleHashMap originalTermFreqs = new TIntDoubleHashMap();
	protected QueryExpansionModel model;

	/**
	 * Set the original query terms.
	 * @param query The original query.
	 */
	public void setOriginalQueryTerms(MatchingQueryTerms query){
		String[] terms = query.getTerms();
		this.originalTermids.clear();
		for (int i=0; i<terms.length; i++)
  		{
			EntryStatistics te = query.getStatistics(terms[i]);
			if (te != null) {
				this.originalTermids.put(te.getTermId(), terms[i]);
				this.originalTermFreqs.adjustOrPutValue(te.getTermId(), query.getTermWeight(terms[i]), query.getTermWeight(terms[i]));
			}
		}
	}
	
	/** Add a single document to be considered for expanding the query 
	 * @throws IOException */
	public abstract void insertDocument(FeedbackDocument doc) throws IOException;
	/** Get the most informative terms for the expanded set.
	 * @param numberOfExpandedTerms - number of terms to get.
	 * @return weighted query terms
	 */
	public abstract SingleTermQuery[] getExpandedTerms(int numberOfExpandedTerms); 

	/** Set query expansion model
	 * 
	 * @param m
	 */
	public void setModel(QueryExpansionModel m) {
		model = m;
	}

	/** Returns the number of terms being considered */
	public abstract int getNumberOfUniqueTerms();
	
	/** 
	 * This class implements a data structure 
	 * for a term in the top-retrieved documents. 
	 */
	public static class ExpansionTerm {
		
		/** The term ID. */
		protected int termID;
		/** The weight for query expansion. */
		protected double weightExpansion;
		/** The number of occurrences of the given term in the X top ranked documents. */
		protected double withinDocumentFrequency;
		
		/** The document frequency of the term in the X top ranked documents. */
		protected int documentFrequency;
		
		/** 
		 * The constructor of ExpansionTerm. Once the term is found in a top-
		 * retrieved documents, we create a record for this term.
		 * @param _termID int the ID of the term
		 * @param _withinDocumentFrequency double the frequency of the term in 
		 *		a top-retrieved document
		 */
		public ExpansionTerm(int _termID, double _withinDocumentFrequency){
			this.termID = _termID;
			this.withinDocumentFrequency = _withinDocumentFrequency;
			this.documentFrequency = 1;
			this.weightExpansion = 0;
		}
			
		/**
		 * Returns the ID of the term. 
		 * @return int the term ID.
		 */
		public int getTermID(){
			return this.termID;
		}
		/** 
		 * If the term is found in another top-retrieved document, we increase
		 * the frequency and the document frequency of the term.
		 * @param _withinDocumentFrequency double the frequency of the term
		 *		in the corresponding top-retrieved document.
		 */
		public void insertRecord(double _withinDocumentFrequency){
			this.withinDocumentFrequency += _withinDocumentFrequency;
			this.documentFrequency++;
		}
		/** 
		 * Sets the expansion weight of the term.
		 * @param _weightExpansion double the expansion weight of the term.
		 */
		public void setWeightExpansion(double _weightExpansion){
			this.weightExpansion = _weightExpansion;
		}
		
		/** 
		 * The method returns the document frequency of term in the top-retrieved
		 * documents.
		 * @return int The document frequency of term in the top-retrieved
		 *		 documents.
		 */
		public int getDocumentFrequency(){
			return this.documentFrequency;
		}
		/** 
		 * The method returns the expansion weight of the term.
		 * @return double The expansion weight of the term.
		 */
		public double getWeightExpansion(){
			return this.weightExpansion;
		}
		
		/** 
		 * The method returns the frequency of the term in the X top-retrieved
		 * documents.
		 * @return double The expansion weight of the term.
		 */
		public double getWithinDocumentFrequency(){
			return this.withinDocumentFrequency;
		}
	}

	
	

}
