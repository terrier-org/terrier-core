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
 * The Original Code is DisjunctiveQuery.java.
 *
 * The Original Code is Copyright (C) 2010-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.querying.parser;

import java.util.List;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.matchops.Operator;
import org.terrier.matching.matchops.SingleTermOp;
import org.terrier.matching.matchops.SynonymOp;

import com.google.common.collect.Lists;

/**
 * Models a disjunctive choice in single term queries in a query.
 * @author Craig Macdonald
 */
public class DisjunctiveQuery extends MultiTermQuery {
	
	private static final long serialVersionUID = 1L;
	
	int activeIndex = -1;
	
	/** The weight of this "term". */
	double weight = 1.0d;
	
	/**
	 * Sets the weight of the disjunctive "term".
	 * @param w double the weight of the disjunctive "term".
	 */
	public void setWeight(double w) {
		weight = w;
	}
	/**
	 * Returns the weight of the disjunctive "term".
	 * @return double the weight of the disjunctive "term".
	 */
	public double getWeight() {
		return weight;
	}
	/**  Constructor 
	 * 
	 * @param alternatives
	 */
	public DisjunctiveQuery(String[] alternatives) {
		this();
		for(String alt : alternatives)
		{
			v.add(new SingleTermQuery(alt));
		}
	}
	/** 
	 * default constructor
	 */
	public DisjunctiveQuery() {
		super();
		prefix = "{";
		suffix = "}";
	}
	/** reset active index
	 * 
	 */
	public void resetActive()
	{
		activeIndex = -1;
	}
	/** set active index
	 * 
	 * @param _activeIndex
	 */
	public void setActive(int _activeIndex)
	{
		activeIndex = _activeIndex;
	}
	
	public void obtainQueryTerms(MatchingQueryTerms terms, String field, Boolean required, Double weight)
	{
		obtainQueryTerms(new QueryTermsParameter(terms, true, field, required, weight));
	}
	public void obtainQueryTerms(QueryTermsParameter parameters)
	{
		List<Operator> singleTerms = Lists.newArrayList();
		for(Query child : v)
		{
			SingleTermQuery term = (SingleTermQuery)child;
			final String t = parameters.lowercase() ? term.getTerm().toLowerCase() : term.getTerm();
			singleTerms.add(new SingleTermOp(t, parameters.getField()));
		}
		QTPBuilder qtp = QTPBuilder.of(new SynonymOp(singleTerms.toArray(new Operator[singleTerms.size()])));
		if (parameters.getWeight() != null)
		{
			qtp.setWeight(parameters.getWeight() * this.weight);
		}
		else
		{
			qtp.setWeight(this.weight);
		}
		if (parameters.isRequired() != null && parameters.isRequired())
		{
			qtp.setRequired(true);
		}
		else if (parameters.isRequired() != null && !parameters.isRequired())
		{
			qtp.setRequired(false);
			qtp.setWeight(Double.NEGATIVE_INFINITY);
		}
		//qtp.setField(field);
		parameters.getTerms().add(qtp.build());
	}

	@Override
	public void obtainAllOf(Class<? extends Query> c, List<Query> a) {
		if (c.isInstance(this))
			a.add(this);
		int i=0;
		for(Query child : v)
		{
			if (activeIndex != -1 && i == activeIndex)
			{
				child.obtainAllOf(c, a);
			}
			i++;
		}
	}
//	@Override
//	public void obtainQueryTerms(MatchingQueryTerms terms) {
//		final StringBuilder s = new StringBuilder();
//		int i = 0;
//		for(Query child : v)
//		{
//			if ((activeIndex == -1) || (activeIndex != -1 && i == activeIndex))
//			{
//				SingleTermQuery term = (SingleTermQuery)child;
//				s.append(term.getTerm());
//				s.append('|');
//			}
//			i++;
//		}
//		if (s.length() == 0)
//			return;
//		s.setLength(s.length() -1);
//		terms.addTermPropertyWeight(s.toString(), weight);
//	}
	
	@Override
	public String toString() {
		String rtr = super.toString();
		if (weight != 1.0d) 
			rtr += "^" + weight;
		return rtr;
	}

}
