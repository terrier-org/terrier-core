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
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.terrier.matching.MatchingQueryTerms.MatchingTerm;
import org.terrier.matching.dsms.DocumentScoreModifier;
import org.terrier.matching.matchops.Operator;
import org.terrier.matching.matchops.SingleTermOp;
import org.terrier.matching.models.WeightingModel;
import org.terrier.querying.Request;
import org.terrier.querying.parser.Query;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.collections.MapEntry;

import com.google.common.collect.Lists;
/**
 * Models a query used for matching documents. It is created
 * by creating an instance of this class, and then passing it as
 * an argument to the method obtainQueryTerms of a Query. It contains
 * the query terms, their weights, optionally the corresponding term 
 * identifiers and the associated term score modifiers. It also stores
 * the document score modifiers for the query.  
 * @author Vassilis Plachouras, Craig Macdonald.
  */
public class MatchingQueryTerms 
extends ArrayList<MatchingTerm> 
implements Serializable,Cloneable
{
	
	public static class MatchingTerm 
	extends MapEntry<Operator, MatchingQueryTerms.QueryTermProperties>
	implements Map.Entry<Operator, MatchingQueryTerms.QueryTermProperties>{

		public MatchingTerm(Operator _key, QueryTermProperties _value) {
			super(_key, _value);
		}
	}

	private static final long serialVersionUID = -9l;
	/** The weight and the modifiers associated with a query term.*/
	public static class QueryTermProperties implements Serializable, Cloneable
	{
		private static final long serialVersionUID = 6327392687128896557L;
		
		/** the index at which this term was inserted */
		int index;
		
		//tri-valued logic: if null, then no requirement; if true then required, 
		//if false, then not required
		public Boolean required = null;

		/** The weight of a query term. This is usually how many times the term occurred
		  * in the query, but sometime may be altered if a weight has been specified on the
		  * query term: eg QueryExpansion will do this, as will manually specifying a weight
		  * on the unparsed query (example <tt>term1 term2^3</tt>). */
		public double weight;
		
		/** Info about the query term.*/
		public EntryStatistics stats;
		
		/** The term score modifiers associated with a particular query term.*/
		public List<WeightingModel> termModels = new ArrayList<WeightingModel>();
		
		public Set<String> tags = new HashSet<>();
		
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

		public double getWeight() {
			return weight;
		}

		public Set<String> getTags() {
			return tags;
		}
		
		public Boolean getRequired() {
			return required;
		}

		public void setWeight(double weight) {
			this.weight = weight;
		}

		public void setTag(String tag) {
			this.tags.add(tag);
		}
		
		public void setRequired(boolean b) {
			required = b;
		}

		@Override
		public QueryTermProperties clone()
		{
			QueryTermProperties newO = new QueryTermProperties(index, weight, stats);
			for (WeightingModel model : termModels)
				newO.termModels.add((WeightingModel)(model.clone()));
			newO.tags = new HashSet<>(tags);
			newO.required = required;
			return newO;
		}

		@Override
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
		
		@Override
		public String toString()
		{
			return "{ req=" + this.required + ", w=" + this.weight + ", stats=" + this.stats + ", models=" + this.termModels.toString() + " tags="+tags+"}";
		}

		
		
	}
	
	static class StringQueryTermPropertiesByIndexComparator implements Comparator<Map.Entry<Operator,QueryTermProperties>>, Serializable
	{
		private static final long serialVersionUID = 1L;
		public int compare(Entry<Operator, QueryTermProperties> o1, Entry<Operator, QueryTermProperties> o2)
		{
			return o1.getValue().index - o2.getValue().index;
		}		
	}
	static final Comparator<Map.Entry<Operator,QueryTermProperties>> BY_INDEX_COMPARATOR = new StringQueryTermPropertiesByIndexComparator();
	
	
	/** The query ID, if provided */
	protected String queryId = null;
	
	protected Request rq = null;
	
	protected Set<String> defaultTags = new HashSet<>(Arrays.asList(BaseMatching.BASE_MATCHING_TAG));
		
	/** default weighting model for all terms */
	protected WeightingModel defaultWeightingModel;
	
	/** 
	 * The document score modifiers associated with the query terms.
	 * It should contain the phrase score modifiers for example.
	 */
	protected ArrayList<DocumentScoreModifier> docScoreModifiers = new ArrayList<DocumentScoreModifier>();
	
	/** The original query as it came from the parser, in case any TSMs or DSMs
	 * wish to refer to it
	 */
	protected Query query; 

	public MatchingQueryTerms(Collection<MatchingTerm> coll)
	{
		super(coll);
	}
	
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
	 * This method normalises the term weights by dividing each term weight
	 * by the maximum of the terms.
	 */
	public void normaliseTermWeights(){
		// obtain the maximum term weight
		double maxWeight = 0d;
		
		for(Map.Entry<?, QueryTermProperties> e : this)
		{
			maxWeight = Math.max(maxWeight, e.getValue().weight);
		}
		// normalise
		for(Map.Entry<?, QueryTermProperties> e : this)
		{
			e.getValue().weight /= maxWeight;
		}

	}
	
	/**
	 * This method resets query term statistics to allow for a single instance
	 * of MatchingQueryTerms to be reused for matching against different indices.
	 */
	public void resetTermProperties() {
		for(Map.Entry<?, QueryTermProperties> e : this) {
			e.setValue(null);
		}
	}
	
	
	public void setTermProperty(Operator term, EntryStatistics e) {
		QueryTermProperties properties = (QueryTermProperties)this.get(term);
		if (properties == null) {
			this.add( new MatchingTerm(term, properties = new QueryTermProperties(0, e)));
			properties.tags.addAll(this.defaultTags);
		} else {
			properties.stats = e;
		}
	}
	
	/**
	 * Sets a term's statistics for the given query term.
	 * @param term String the term for which to override the statitics
	 * @param e EntryStatistics the term score modifier to apply for the given term.
	 */
	public void setTermProperty(String term, EntryStatistics e) {
		setTermProperty(new SingleTermOp(term), e);
	}
	
	/**
	 * Sets a term score modifier for the given query term.
	 * @param term String the term for which to add a term score modifier.
	 * @param tsm TermScoreModifier the term score modifier to apply for the given term.
	 */
	public void setTermProperty(Operator term, WeightingModel tsm) {
		QueryTermProperties properties = (QueryTermProperties)this.get(term);
		if (properties == null) {
		//	this.put(term, new QueryTermProperties(termAdditionIndex++, tsm));
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
	public void setTermProperty(Operator term, double weight, WeightingModel tsm) {
		QueryTermProperties properties = (QueryTermProperties)this.get(term);
		if (properties == null) {
			
			this.add(new MatchingTerm(term, properties = new QueryTermProperties(0 /*termAdditionIndex++*/, weight, tsm)));
			properties.tags.addAll(this.defaultTags);
		} else {
			//properties.weight = weight;
			//TODO adjust the weights?
			properties.termModels.add(tsm);
		}
	}
	
	protected QueryTermProperties combine(QueryTermProperties existingProps, QueryTermProperties newProps) {
		
		if (existingProps.required != newProps.required)
			return null;
		if (! existingProps.termModels.equals(newProps.termModels))
			return null;
		
		existingProps.weight += newProps.weight;
		return existingProps;
	}
	
	@Override
	public boolean add(MatchingTerm e) {
		QueryTermProperties existing = this.get(e.getKey());
		if (existing != null && combine(existing, e.getValue()) != null)
		{
			//do nothing, we have combined
			return true;
		}
		return super.add(e);
		
	}

	/**
	 * Returns the associated weight of the given query term.
	 * @param term String the query term for which the weight is returned.
	 * @return double the weight of the given query term. If the term is not part
	 *         of the query, then it returns 0.
	 */
	public double getTermWeight(Operator term) {
		final QueryTermProperties tp = this.get(term);
		if (tp!=null)
			return tp.weight;
		return 0.0d;
	}
	
	public double getTermWeight(String term) {
		Map.Entry<Operator,QueryTermProperties> ee = this.get(term);
		if (ee != null)
			return ee.getValue().weight;
		return 0d;
	}
	
	/**
	 * Returns the associated weights of the given query terms.
	 * @return double The weights of the given terms in a double array.
	 */
	public double[] getTermWeights(){
		double[] tws = new double[this.size()];
		int i=0;
		for(Map.Entry<Operator, QueryTermProperties> e : this)
			tws[i++] = e.getValue().weight;
		return tws;
	}
	
	/**
	 * Returns the assocciated code of the given query term.
	 * @param term String the query term for which the weight is returned.
	 * @return EntryStatistics the statistics of the term, or null if the
	 * term does not appear in the query.
	 */
	public EntryStatistics getStatistics(Operator term) {
		final QueryTermProperties tp = this.get(term);
		if (tp == null)
			return null;
		return tp.stats;
	}
	
	public EntryStatistics getStatistics(String term) {
		final Map.Entry<Operator, QueryTermProperties> ee = this.get(term);
		if (ee == null)
			return null;
		return ee.getValue().stats;
	}
	
	public QueryTermProperties get(Operator term) {
		//TODO: this is slow
		for( Map.Entry<Operator, QueryTermProperties> e : this)
		{
			if (e.getKey().equals(term))
				return e.getValue();
		}
		return null;
	}
	
	public Map.Entry<Operator, QueryTermProperties> get(String singleTerm) {
		//TODO: this is slow
		for( Map.Entry<Operator, QueryTermProperties> e : this)
		{
			if (e.getKey().toString().equals(singleTerm))
				return e;
			//if (e.getKey() instanceof SingleQueryTerm)
			//	if (((SingleQueryTerm)e.getKey()).toString().equals(singleTerm) )
			//		return e;
		}
		return null;
	}
	
	
	public void setTermProperty(String term, double weight) {
		Map.Entry<Operator, QueryTermProperties> e = get(term);
		if (e == null)
		{
			QueryTermProperties ev = new QueryTermProperties(0, weight);
			ev.tags.addAll(this.defaultTags);
			this.add(new MatchingTerm(new SingleTermOp(term), ev));
		}
		else
			e.getValue().weight = weight;
	}
	
	
	
	/** Returns the query terms, as they were added to this object. 
	 * @return Query terms in order that they were added to the query. Empty array if object has no query terms added.
	 */
	public Operator[] getMatchingTerms() {
		
		List<Operator> l = Lists.newArrayList();
		for( Map.Entry<Operator, QueryTermProperties> e : this)
		{
			l.add(e.getKey());
		}
		return l.toArray(new Operator[l.size()]);
	}
	
	/** Returns the query terms, as they were added to this object. 
	 * @return Query terms in order that they were added to the query. Empty array if object has no query terms added.
	 */
	public String[] getTerms() {
		
		List<String> l = Lists.newArrayList();
		for( Map.Entry<Operator, QueryTermProperties> e : this)
		{
			l.add(e.getKey().toString());
		}
		return l.toArray(new String[l.size()]);
	}
	
//	public Set<String> getMatchingTags()
//	{
//		return matchOnTags;
//	}
	
	//Set<String> matchOnTags = new HashSet<String>();
	public List<MatchingTerm> getMatchingTerms(String tag)
	{
//		if (matchOnTags.size() == 0)
//			return this;
		return this.stream()
				.filter(kv -> kv.getValue().getTags().contains(tag) )
				.collect(Collectors.toList());
	}
	
	/** 
	* Adds a term to the query with a given weight. If the term already exists,
	* the existing weight is overwritten.
	* @param term String the term to add.
	* @param weight double the weight of the added term.
	*/
	public void setTermProperty(Operator term, double weight) {
		QueryTermProperties properties = this.get(term);
		if (properties == null) {
		//	termProperties.put(term, new QueryTermProperties(termAdditionIndex++, weight));
		} else {
			properties.weight = weight;
		}
	}
	
	/**
	 * Returns the number of unique terms in the query.
	 * @return int the number of unique terms in the query.
	 */
	@Deprecated
	public int length() {
		return this.size();
	}

	/** Performs a deep clone of this object, and all objects it contains. This allows a MQT to be copied,
	  * and changed without affecting the original object. */
	public MatchingQueryTerms clone()
	{
		MatchingQueryTerms newMQT = new MatchingQueryTerms(this.queryId);
		
		//copy queryID, Strings are immutable
		//clone query term properties
		for (Map.Entry<Operator, QueryTermProperties> e : this)
		{
			newMQT.add(new MatchingTerm(e.getKey().clone(), e.getValue().clone()));
		}
		for (DocumentScoreModifier dsm : docScoreModifiers)
		{
			newMQT.docScoreModifiers.add( (DocumentScoreModifier)(dsm.clone()));
		}
		//clone query
		if (this.query != null)
			newMQT.query = (Query)this.query.clone();
		if (this.defaultWeightingModel != null)
			newMQT.defaultWeightingModel = this.defaultWeightingModel.clone();
		//newMQT.matchOnTags = new HashSet<>(this.matchOnTags);
		newMQT.queryId = this.queryId;
		return newMQT;
	}
	
	/** Remove a term from the list of terms to be matched
	 * @since 3.6 
	 */
	public void removeTerm(Operator term)
	{
		this.remove(term);
	}
	
	/* 
	 * The following attributes are used for creating arrays of the correct type.
	 */
	private static final DocumentScoreModifier[] tmpDSM = new DocumentScoreModifier[0];
	//private static final String[] tmpString = new String[0];
	//private static final WeightingModel[] tmpModels = new WeightingModel[0];
	

//	/** Returns the weighting models to be used for a given term. This will always include the default
//	 * weighting model */
//	public WeightingModel[] getTermWeightingModels(QueryTerm term) {
//		QueryTermProperties qtp = this.get(term);
//		if (qtp == null)
//			return tmpModels;
//		if (qtp.termModels.size() != 0)
//		{
//			final ArrayList<WeightingModel> n = new ArrayList<WeightingModel>(qtp.termModels);
//			n.add(0, defaultWeightingModel.clone());
//			return n.toArray(tmpModels);
//		}
//		return new WeightingModel[]{defaultWeightingModel.clone()};
//	}

	/** Set the default weighting model to be used for terms that do NOT have an explicit WeightingModel set. */
	public void setDefaultTermWeightingModel(WeightingModel weightingModel) {
		defaultWeightingModel = weightingModel;
		for(Map.Entry<Operator, QueryTermProperties> e : this)
		{
			if (e.getValue().termModels.size() == 0)
				e.getValue().termModels.add(weightingModel.clone());
		}
	}

	public void addTermPropertyWeight(String term, double d) {
		double w = getTermWeight(term);
		setTermProperty(term, d+w);
	}
	
	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		for(Map.Entry<Operator, QueryTermProperties> e : this)
		{
			s.append(e.getKey().toString());
			s.append(' ');
			s.append(e.getValue().toString());
			s.append(' ');
		}
		return s.toString();		
	}

}
