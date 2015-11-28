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
 * The Original Code is FieldQuery.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.querying.parser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.tsms.TermInFieldModifier;
/**
 * Models a query qualified with a field.
 * @author Vassilis Plachouras, Craig Macdonald
  */
public class FieldQuery extends Query {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The optional field.*/
	String field = null;
	
	/**
	 * An empty default constructor
	 */
	public FieldQuery(){}
	
	/**
	 * Constructs a field query from the given query.
	 * @param q the query that is qualified with the field operator.
	 */
	public FieldQuery(Query q) 	{
		child = q;
	}
	/**
	 * Constructs a field query from the given query and 
	 * the given field.
	 * @param q Query the query that is qualified with the field operator.
	 * @param f String the field in which the given query should appear in.
	 */
	public FieldQuery(Query q, String f)
	{
		child = q;
		field = f.toUpperCase();
	}

	/** Deep Clone this object */
	public Object clone()
	{
		FieldQuery fq = (FieldQuery)super.clone();
		fq.field = field;//string is immutable or null
		return (Object)fq;
	}

	/**
	 * Gets the field that the query term should appear.
	 * @return String the field that the query term should
	 *         appear.
	 */
	public String getField() {
		return field;
	}
	/** 
	 * Sets the value of the field.
	 * @param f String the value of the field.
	 */
	public void setField(String f) {
		field = f.toUpperCase();
	}
	
	/**
	 * Returns a string representation of the query.
	 * @return String the string of the query.
	 */
	public String toString() {
		return field + ":" + child.toString();
	}
	
	/**
	 * Prepares the query for matching by transforming
	 * the query objects to a set of query terms.
	 * @param terms MatchingQueryTerms the object which holds the 
	 *        query terms and their modifiers.
	 */
	public void obtainQueryTerms(MatchingQueryTerms terms) {
		//System.out.println("FieldQuery: obtainQueryTerms");
		obtainQueryTerms(terms, true);
	}
	
	/**
	 * Prepares the query for matching by transforming
	 * the query objects to a set of query terms.
	 * @param terms MatchingQueryTerms the object which holds the 
	 *        query terms and their modifiers.
	 * @param required boolean indicates whether the field query
	 *        is required or not.     
	 */
	public void obtainQueryTerms(MatchingQueryTerms terms, boolean required) {
		//System.out.println("FieldQuery: obtainQueryTerms with " + required);
		child.obtainQueryTerms(terms);
		ArrayList<Query> alist = new ArrayList<Query>();
		child.getTerms(alist);
		SingleTermQuery[] queryTerms = (SingleTermQuery[])alist.toArray(tmpSTQ);
		for (int i=0; i<queryTerms.length; i++)
			terms.setTermProperty(queryTerms[i].getTerm(), new TermInFieldModifier(field, required));
	}
	/** Checks to see if field name is a valid control name, as specified by
	  * allowed, and if so adds it to the controls table and returns true to 
	  * specify that this Query object is now dead. 
	  * @param allowed A hashset of lowercase control names that may be set by user queries.
	  * @param controls The hashtable to add the found controls to 
	  * @return true if this node should now be removed, false otherwise */
	public boolean obtainControls(Set<String> allowed, Map<String, String> controls)
	{
		if (child instanceof SingleTermQuery && allowed.contains(field.toLowerCase()))
		{
			controls.put(field.toLowerCase(), ((SingleTermQuery)child).getTerm());
			return true;
		}
		return false;
	}
	
	/** 
	 * Returns all the query terms, in subqueries that
	 * are instances of a given class
	 * @param c Class a class of queries.
	 * @param alist ArrayList the list of query terms.
	 * @param req boolean indicates whether the subqueries 
	 *        are required or not.
	 *
	 */
	@Override
	public void getTermsOf(Class<? extends Query> c, List<Query> alist, boolean req) {		
		//System.out.println("FQ: for " + c.getName() + " req: " + req);
		if (c.isInstance(this) && req)
			child.getTerms(alist);
		
		child.getTermsOf(c, alist, req);
	}

	@Override
	public String parseTree() {
		return this.getClass().getSimpleName() + "(" + child.parseTree() + ")";
	}
	
}
