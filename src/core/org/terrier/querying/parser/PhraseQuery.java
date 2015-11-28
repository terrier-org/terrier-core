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
 * The Original Code is PhraseQuery.java.
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
import org.terrier.matching.dsms.PhraseScoreModifier;
/**
 * Models a phrase query, which can have a proximity requirement.
 * @author Vassilis Plachouras, Craig Macdonald
  */
public class PhraseQuery extends MultiTermQuery {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Indicates the distance, in number of blocks, 
	 * that the phrase terms can appear in a document.
	 * A value of zero corresponds to phrase searching
	 * and it is the default value, while any higher
	 * value enables proximity searching.
	 */
	protected int proximityDistance = 1;
	
	/**
	 * A default constructor that calls the constructor
	 * of the super class.
	 */
	public PhraseQuery() {
		super();
	}
	
	/**
	 * Constructs a phrase query and sets the 
	 * proximity distance
	 * @param proxDistance int the distance, in number 
	 *        of blocks, that the phrase terms can appear
	 *        in a document.
	 */
	public PhraseQuery(int proxDistance) {
		super();
		proximityDistance = proxDistance;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public Object clone()
	{
		PhraseQuery pq = (PhraseQuery)super.clone();
		pq.setProximityDistance(proximityDistance);
		return (Object)pq;
	}
	
	/**
	 * Sets the allowed distance, in blocks, between
	 * the phrase terms.
	 * @param prox the allowed distance between the phrase terms.
	 */
	public void setProximityDistance(int prox) {
		proximityDistance = prox;
	}
	
	/**
	 * Returns a string representation of the phrase query.
	 * @return String the string representation of the phrase query.
	 */
	public String toString() {
		final StringBuilder output = new StringBuilder();
		//phrase 
		output.append("\"");
		output.append(super.toString());
		output.append("\"");
		//with proximity constraint if not default	
		if (proximityDistance > 1) 
		{
			output.append("~");
			output.append(proximityDistance);
		}
		return output.toString();
	}
	/**
	 * Stores the query terms of the phrase query in the 
	 * given matching query terms structure. The query terms
	 * are required to appear in the matched documents.
	 * @param terms MatchingQueryTerms the structure that stores
	 *        the query terms for matching.
	 */
	public void obtainQueryTerms(MatchingQueryTerms terms) {
		for(Query child : v)
		{
			child.obtainQueryTerms(terms, true);
		}
		ArrayList<Query> alist = new ArrayList<Query>();
		this.getTerms(alist);
		terms.addDocumentScoreModifier(new PhraseScoreModifier(alist, proximityDistance));
	}
	
	/**
	 * Stores the query terms of the phrase query in the 
	 * given matching query terms structure. Whether the 
	 * phrase is required to appear in the retrieved documents, 
	 * depends on the value of the parameter required.
	 * @param terms MatchingQueryTerms the structure that stores
	 *        the query terms for matching.
	 * @param required boolean indicates whether the phrase is
	 *        required or not.
	 */
	public void obtainQueryTerms(MatchingQueryTerms terms, boolean required) {
		if (required)
			for(Query child : v)
			{	
				child.obtainQueryTerms(terms, required);
			}
		ArrayList<Query> alist = new ArrayList<Query>();
		this.getTerms(alist);
		terms.addDocumentScoreModifier(new PhraseScoreModifier(alist, required, proximityDistance));
	}
	/** This object cannot contain any controls, so this method will always return false.
      * @return false */
	public boolean obtainControls(Set<String> allowed, Map<String, String> controls)
	{
		return false;
	}
	
	/** 
	 * Returns all the query terms, in subqueries that
	 * are instances of a given class
	 * @param c Class a class of queries.
	 * @param alist ArrayList the list of query terms.
	 * @param req boolean indicates whether the subqueries 
	 *        are required or not.
	 */
	@Override
	public void getTermsOf(Class<? extends Query> c, List<Query> alist, boolean req) {		
		if (req) 
			getTerms(alist);
	}
}
