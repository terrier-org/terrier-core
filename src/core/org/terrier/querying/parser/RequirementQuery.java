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
 * The Original Code is RequirementQuery.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.querying.parser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.terrier.matching.MatchingQueryTerms;
/**
 * Models a query where the query terms have been qualified
 * with a requirement operator, either plus, or minus.
 * @author Vassilis Plachouras &amp; Craig Macdonald
 */
public class RequirementQuery extends Query {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 
	 * The query requirement. The default value is true. */
	private boolean MustHave = true;

	/** An empty default constructor. */
	public RequirementQuery(){}
	/** 
	 * {@inheritDoc} 
	 */
	public Object clone()
	{
		RequirementQuery rq = (RequirementQuery)super.clone();
		rq.MustHave = MustHave;
		return (Object)rq;
	}

	/** 
	 * Sets whether the query is required or not.
	 * @param needed boolean indicates whether the query is required or not.
	 */
	public void setRequired(boolean needed) {
		MustHave = needed;
	}

	/** Returns True if the subquery is REQUIRED to exist, or
	 * false if it REQUIRED to NOT exit.
	 * @return See above.
	 */
	public boolean getRequired()
	{
		return MustHave;
	}

	/**
	 * Returns a string representation of the query.
	 * @return String a string representation of the query.
	 */
	public String toString() {
		if (child instanceof PhraseQuery)
		{
			return (MustHave ? "+" : "-") + child.toString();
		}
		else if (child instanceof MultiTermQuery)
		{
			return (MustHave ? "+" : "-") + "(" +child.toString()+ ")";
		}
		return (MustHave ? "+" : "-") + child.toString();
	}

	/**
	 * Stores the terms of the query in the given structure, which 
	 * is used for matching documents to the query. 
	 * @param terms MatchingQueryTerms the structure that holds the query
	 *        terms for matching to documents. 
	 */
	public void obtainQueryTerms(MatchingQueryTerms terms) {
		child.obtainQueryTerms(terms, MustHave);
	}
	/** 
	 * This object cannot contain any controls, 
	 * so this method will always return false.
	 * @return false 
	 */
	public boolean obtainControls(Set<String> allowed, Map<String, String> controls)
	{
		return false;
	}

	/** 
	 * Returns all the query terms, in subqueries that
	 * are instances of a given class.
	 * @param c Class a class of queries.
	 * @param alist ArrayList the list of query terms.
	 * @param req boolean indicates whether the subqueries 
	 *        are required or not.
	 */
	@Override
	public void getTermsOf(Class<? extends Query> c, List<Query> alist, boolean req) {		
		if (PhraseQuery.class.isInstance(child) && !MustHave)
			return;

		int required = 0;
		if (this.toString().startsWith("+")) required=1;
		if (this.toString().startsWith("-")) required=-1;

		//System.err.println("Requirement for "+this.toString()+" = "+required);
		List<Query> termsOfThisType = new ArrayList<Query>();
		if (c.isInstance(this)) this.getTerms(termsOfThisType);
		child.getTermsOf(c, termsOfThisType, req==MustHave);
		try {
			for (Query q : termsOfThisType) {
				((SingleTermQuery)q).required=required;
			}
		} catch (Exception e) {
			System.err.println("RequirementQuery: Failed to set query requirement, this probably failed since getTerms() returned a query type other than SingleTermQuery");
			e.printStackTrace();
		}
		alist.addAll(termsOfThisType);
	}

	@Override
	public String parseTree() {
		return this.getClass().getSimpleName() + "(" + child.parseTree() + ")";
	}
}
