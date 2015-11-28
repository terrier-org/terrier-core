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
 * The Original Code is MatchingQueryTerms.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.terrier.matching.dsms.DocumentScoreModifier;
import org.terrier.matching.models.WeightingModel;
import org.terrier.querying.Request;
import org.terrier.querying.parser.Query;
import org.terrier.structures.EntryStatistics;
/**
 * Models a query used for matching documents. It is created
 * by creating an instance of this class, and then passing it as
 * an argument to the method obtainQueryTerms of a Query. It contains
 * the query terms, their weights, optionally the corresponding term 
 * identifiers and the assocciated term score modifiers. It also stores
 * the document score modifiers for the query.  
 * @author Vassilis Plachouras, Craig Macdonald.
  */
public class MatchingQueryTerms implements Serializable,Cloneable
{
	private static final long serialVersionUID = -9134975387300425203L;
	/** The weight and the modifiers associated with a query term.*/
	protected static class QueryTermProperties implements Serializable, Cloneable
	{
		private static final long serialVersionUID = 6327392687128896557L;
		
		/** the index at which this term was inserted */
		int index;
		
		/** The weight of a query term. This is usually how many times the term occurred
		  * in the query, but sometime may be altered if a weight has been specified on the
		  * query term: eg QueryExpansion will do this, as will manually specifying a weight
		  * on the unparsed query (example <tt>term1 term2^3</tt>). */
		double weight;
		
		/** Info about the query term.*/
		EntryStatistics stats;
		
		/** The term score modifiers associated with a particular query term.*/
		ArrayList<WeightingModel> termModels = new ArrayList<WeightingModel>();
		
		public QueryTermProperties(int _index) {
			this.index = _index;
			//weight = 1.0d;
		}

		/** 
		 * An constructor for setting the term code 
		 * of a query term.
		 * @param _stats the statistics of the query term
		 */
		public QueryTermProperties(int _index, EntryStatistics _stats) {
			this(_index);
			stats = _stats;
		}
		
		/** 
		 * A constructor for setting the weight of a term.
		 * @param w double the weight of a query term. 
		 */
		public QueryTermProperties(int _index, double w) {
			this(_index);
			weight = w;
		}
		/**
		 * A constructor for setting a term score modifier for a term.
		 * @param model WeightingModel modifier associated with a query term.
		 */
		public QueryTermProperties(int _index, WeightingModel model) {
			this(_index);
			termModels.add(model);
		}		
		
		/**
		 * A constructor for setting the weight and a 
		 * term score modifier for a term.
		 * @param w double the weight of a query term. 
		 * @param model WeightingModel modifier associated with a query term.
		 */
		public QueryTermProperties(int _index, double w, WeightingModel model) {
			this(_index);
			weight = w;
			termModels.add(model);
		}
		
		/** 
		 * A constructor for setting the weight of a term
		 * and its term code.
		 * @param w double the weight of a query term. 
		 * @param _stats statistics of the query term
		 */
		public QueryTermProperties(int _index, double w, EntryStatistics _stats) {
			this(_index);
			weight = w;
			stats = _stats;
		}
		
		/**
		 * A constructor for setting a term score modifier for a term 
		 * and its term code.
		 * @param model WeightingModel modifier associated with a query term.
		 * @param _stats statistics of the query term
		 */
		public QueryTermProperties(int _index, WeightingModel model, EntryStatistics _stats) {
			this(_index);
			termModels.add(model);
			stats = _stats;
		}
		
		/**
		 * A constructor for setting a weight, a term score modifier 
		 * and the term code for a query term.
		 * @param w double the weight of a query term.
		 * @param model WeightingModel modifier associated with a query term.
		 * @param _stats statistics of the query term
		 */
		public QueryTermProperties(int _index, double w, WeightingModel model, EntryStatistics _stats) {
			this(_index);
			weight = w;
			termModels.add(model);
			stats = _stats;
		}

		public Object clone()
		{
			QueryTermProperties newO = new QueryTermProperties(index, weight, stats);
			for (WeightingModel model : termModels)
				newO.termModels.add((WeightingModel)(model.clone()));
			return (Object)newO;
		}

		public int hashCode()
		{
			int hashCodeValue = stats.hashCode();
			hashCodeValue += (new Double(weight)).hashCode();
			for (WeightingModel model : termModels)
			{
				hashCodeValue += model.hashCode();
			}
			return hashCodeValue;
		}
		
	}
	
	static class StringQueryTermPropertiesByIndexComparator implements Comparator<Map.Entry<String,QueryTermProperties>>, Serializable
	{
		private static final long serialVersionUID = 1L;
		public int compare(Entry<String, QueryTermProperties> o1, Entry<String, QueryTermProperties> o2)
		{
			return o1.getValue().index - o2.getValue().index;
		}		
	}
	static final Comparator<Map.Entry<String,QueryTermProperties>> BY_INDEX_COMPARATOR = new StringQueryTermPropertiesByIndexComparator();
	
	
	/** The query ID, if provided */
	protected String queryId = null;
	
	protected Request rq = null;
	
	/** A mapping from the string of a query term to its properties.*/
	protected HashMap<String,QueryTermProperties> termProperties = new HashMap<String,QueryTermProperties>();
	
	/** default weighting model for all terms */
	protected WeightingModel defaultWeightingModel;
	
	protected int termAdditionIndex = 0;
	
	/** 
	 * The document score modifiers associated with the query terms.
	 * It should contain the phrase score modifiers for example.
	 */
	protected ArrayList<DocumentScoreModifier> docScoreModifiers = new ArrayList<DocumentScoreModifier>();
	
	/** The original query as it came from the parser, in case any TSMs or DSMs
	 * wish to refer to it
	 */
	protected Query query; 

	/** Generate a MatchingQueryTerms object. Query id will be null. */
	public MatchingQueryTerms()
	{}

	/** Generate a MatchingQueryTerms object, with the specified query id.
	  * @param qid A string representation of the query id */
	public MatchingQueryTerms(String qid)
	{
		queryId = qid;
	}
	/** Generate a MatchingQueryTerms object, with the specified query id, and request
	  * @param qid A string representation of the query id
	  * @param _rq A request for matching */
	public MatchingQueryTerms(String qid, Request _rq)
	{
		queryId = qid;
		this.rq = _rq;
	}
	/** Returns the request.
	  * @return Request*/
	public Request getRequest()
	{
		return this.rq;
	}
	
	/**
	 * Adds a document score modifier for the query.
	 * @param dsm DocumentScoreModifier a document score modifier for 
	 *        the query.
	 */
	public void addDocumentScoreModifier(DocumentScoreModifier dsm) {
		docScoreModifiers.add(dsm);
	}
	
	/**
	 * Returns the document score modifiers used for the query.
	 * @return DocumentScoreModifiers[] an array of the registered
	 *         document score modifiers for the query. If there are 
	 *         no document score modifiers, then it returns null.
	 */
	public DocumentScoreModifier[] getDocumentScoreModifiers() {
		if (docScoreModifiers.size()>0)
			return docScoreModifiers.toArray(tmpDSM);
		return null;
	}
	
	/** Allows the manager to set the query that was used to
	 * query the system.
	 * @param q The Query, duh
	 */
	public void setQuery(Query q)
	{
		query = q;
	}

	/** Returns guess what?
	 * @return the query
	 */
	public Query getQuery()
	{
		return query;
	}

	/** Returns the query id specified when constructing this object.
	  * @return String query id, or null if none was specified. */
	public String getQueryId()
	{
		return queryId;
	}

	/** Sets the query id */
	public void setQueryId(String newId)
	{
		queryId = newId;
	}
	
	/** 
	 * Adds a term to the query.
	 * @param term String the term to add.
	 */
	public void setTermProperty(String term) {
		QueryTermProperties properties = termProperties.get(term);
		if (properties == null) {
			termProperties.put(term, new QueryTermProperties(termAdditionIndex++, 1));
		}
	}
	
	/** 
	 * Adds a term to the query with a given weight. If the term already exists,
	 * the existing weight is overwritten.
	 * @param term String the term to add.
	 * @param weight double the weight of the added term.
	 */
	public void setTermProperty(String term, double weight) {
		QueryTermProperties properties = termProperties.get(term);
		if (properties == null) {
			termProperties.put(term, new QueryTermProperties(termAdditionIndex++, weight));
		} else {
			properties.weight = weight;
		}
	}
	
	/**
	 * Adds the given weight for an already existing term in the query.
	 * If the term does not exist, it is added to the arraylist, with weight w.
	 * If the term does exist, the weight is added to its existing weight.
	 * @param term String the term for which we add the weight.
	 * @param w double the added weight.
	 */
	public void addTermPropertyWeight(String term, double w) {
		QueryTermProperties properties = termProperties.get(term);
		if (properties == null) {
			termProperties.put(term, new QueryTermProperties(termAdditionIndex++, w));
		} else {
			properties.weight += w;
		}
	}
	
	/**
	 * Sets the term statistics for the given query term.
	 * @param term String the term for which the term identifier is set.
	 * @param stats TermStatistics the statistics of the term.
	 */
	public void setTermProperty(String term, EntryStatistics stats) {
		QueryTermProperties properties = termProperties.get(term);
		if (properties == null) {
			termProperties.put(term, new QueryTermProperties(termAdditionIndex++, stats));
		} else {
			properties.stats = stats;
		}
	}
	
	/**
	 * This method normalises the term weights by dividing each term weight
	 * by the maximum of the terms.
	 */
	public void normaliseTermWeights(){
		// obtain the maximum term weight
		double maxWeight = 0d;
		QueryTermProperties[] properties = termProperties.values().toArray(
					new QueryTermProperties[termProperties.size()]);
		for (int i = 0; i < properties.length; i++)
			maxWeight = Math.max(maxWeight, properties[i].weight);
		// normalise
		for (int i = 0; i < termProperties.size(); i++)
			properties[i].weight /= maxWeight;
	}
	
	/**
	 * This method resets query term statistics to allow for a single instance
	 * of MatchingQueryTerms to be reused for matching against different indices.
	 */
	public void resetTermProperties() {
		for (QueryTermProperties prop : termProperties.values()) {
			prop.stats = null;
		}
	}
	
	/**
	 * Sets a term score modifier for the given query term.
	 * @param term String the term for which to add a term score modifier.
	 * @param tsm TermScoreModifier the term score modifier to apply for the given term.
	 */
	public void setTermProperty(String term, WeightingModel tsm) {
		QueryTermProperties properties = (QueryTermProperties)termProperties.get(term);
		if (properties == null) {
			termProperties.put(term, new QueryTermProperties(termAdditionIndex++, tsm));
		} else {
			properties.termModels.add(tsm);
		}
	}
	
	/**
	 * Sets the weight and a term score modifier for the given query term.
	 * @param term String the term for which we set the properties.
	 * @param weight int the weight of the query term.
	 * @param tsm TermScoreModifier the term score modifier applied for the query term.
	 */
	public void setTermProperty(String term, double weight, WeightingModel tsm) {
		QueryTermProperties properties = (QueryTermProperties)termProperties.get(term);
		if (properties == null) {
			termProperties.put(term, new QueryTermProperties(termAdditionIndex++, weight, tsm));
		} else {
			properties.weight = weight;
			properties.termModels.add(tsm);
		}
	}
	
	/**
	 * Returns the assocciated weight of the given query term.
	 * @param term String the query term for which the weight is returned.
	 * @return double the weight of the given query term. If the term is not part
	 *         of the query, then it returns 0.
	 */
	public double getTermWeight(String term) {
		final QueryTermProperties tp = termProperties.get(term);
		if (tp!=null)
			return tp.weight;
		return 0.0d;
	}
	/**
	 * Returns the associated weights of the given query terms.
	 * @return double The weights of the given terms in a double array.
	 */
	public double[] getTermWeights(){
		double[] tws = new double[this.length()];
		for (int i = 0; i < tws.length; i++)
			tws[i] = this.getTermWeight(this.getTerms()[i]);
		return tws;
	}
	
	/**
	 * Returns the assocciated code of the given query term.
	 * @param term String the query term for which the weight is returned.
	 * @return EntryStatistics the statistics of the term, or null if the
	 * term does not appear in the query.
	 */
	public EntryStatistics getStatistics(String term) {
		final QueryTermProperties tp = termProperties.get(term);
		if (tp == null)
			return null;
		return tp.stats;
	}
	
	
	/** Returns the query terms, as they were added to this object. 
	 * @return Query terms in order that they were added to the query. Empty array if object has no query terms added.
	 */
	@SuppressWarnings("unchecked")
	public String[] getTerms() {
		final Map.Entry<String, QueryTermProperties>[] entries = 
			termProperties.entrySet().toArray(new Map.Entry[termProperties.size()]);
		if (entries.length == 0)
			return tmpString;
		Arrays.sort(entries, BY_INDEX_COMPARATOR);
		final int l = entries.length;
		final String[] terms = new String[l];
		for(int i=0;i<l;i++)
			terms[i] = entries[i].getKey();
		return terms;
	}
	
	/**
	 * Returns the number of unique terms in the query.
	 * @return int the number of unique terms in the query.
	 */
	public int length() {
		return termProperties.size();
	}

	/** Performs a deep clone of this object, and all objects it contains. This allows a MQT to be copied,
	  * and changed without affecting the original object. */
	public Object clone()
	{
		MatchingQueryTerms newMQT = new MatchingQueryTerms(this.queryId);
		//copy queryID, Strings are immutable
		//clone query term properties
		for (String term : termProperties.keySet().toArray(tmpString))
		{
			newMQT.termProperties.put(term, (QueryTermProperties)(this.termProperties.get(term).clone()));
		}
		for (DocumentScoreModifier dsm : docScoreModifiers)
		{
			newMQT.docScoreModifiers.add( (DocumentScoreModifier)(dsm.clone()));
		}
		//clone query
		if (this.query != null)
			newMQT.query = (Query)this.query.clone();
		return newMQT;
	}
	
	/** Remove a term from the list of terms to be matched
	 * @since 3.6 
	 */
	public void removeTerm(String term)
	{
		termProperties.remove(term);
	}
	
	/* 
	 * The following attributes are used for creating arrays of the correct type.
	 */
	private static final DocumentScoreModifier[] tmpDSM = new DocumentScoreModifier[0];
	private static final String[] tmpString = new String[0];
	private static final WeightingModel[] tmpModels = new WeightingModel[0];
	

	/** Returns the weighting models to be used for a given term. This will always include the default
	 * weighting model */
	public WeightingModel[] getTermWeightingModels(String term) {
		QueryTermProperties qtp = termProperties.get(term);
		if (qtp == null)
			return tmpModels;
		if (qtp.termModels.size() != 0)
		{
			final ArrayList<WeightingModel> n = new ArrayList<WeightingModel>(qtp.termModels);
			n.add(0, defaultWeightingModel.clone());
			return n.toArray(tmpModels);
		}
		return new WeightingModel[]{defaultWeightingModel.clone()};
	}

	/** Set the default weighting model to be used for all terms */
	public void setDefaultTermWeightingModel(WeightingModel weightingModel) {
		defaultWeightingModel = weightingModel;
	}
	
}
