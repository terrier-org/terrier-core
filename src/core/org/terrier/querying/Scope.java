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
 * The Original Code is Scope.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
  *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.querying;
import org.terrier.matching.ResultSet;
import java.util.HashSet;
/**
 * Checks that the prefix of the document number (upto the first "-") is included in
 * this list of scopes as given by the <tt>scope</tt> control.
 * This is a PostFilter, so new_query() is called for each new query, and filter()
 * is called for each result in the resultset.
 * @author Craig Macdonald
  */
public class Scope implements PostFilter
{
	/** The list of documber number prefixes that each document must have one of
	  * to be allowed into the final resultset */
	private HashSet<String> AllowedScopes;
	/** If any scopes have been set - basically a safecheck */
	private boolean useScopes = false;
	/** 
	 * Creates a HashSet of scopes that can are allowed to 
	 * be in the document number prefix. 
	 * @param m Manager the manager used for processing the request.
	 * @param srq SearchRequest the search request to process.
	 * @param rs ResultSet the result set for the search request.
	 */
	public void new_query(Manager m, SearchRequest srq, ResultSet rs)
	{
		AllowedScopes = new HashSet<String>();
		String scope = srq.getControl("scope");
		if (scope.equals(""))
			scope = srq.getControl("scopes");
		if (scope != null &&  scope.length() > 0)
		{
			String[] scopes = scope.split(",");
			for(int scopeno = 0; scopeno < scopes.length; scopeno++)
			{
 				AllowedScopes.add(scopes[scopeno].toLowerCase());
 			}
 			useScopes = true;
 		}
	}
	/**
	  * Called for each result in the resultset, used to filter out unwanted results,
	  * based on the presence of some strings in the document number. The document number
	  * upto the first "-" is checked for presence in the scopes HashSet.
	  * @param m The manager controlling this query
	  * @param srq The search request being processed
	  * @param rs the resultset that is being iterated through
	  * @param rank the array index in the resultset have we reached
	  * @param docid The document number of the currently being procesed result.
	  */
	public byte filter(Manager m, SearchRequest srq, ResultSet rs, int rank, int docid)
	{
		if (! useScopes)
			return FILTER_OK;
		//get docno for DocAtNo
		try{
			String docno = m.getIndex().getMetaIndex().getItem("docno", docid);
			String[] sScope = docno.split("-");
			if (! AllowedScopes.contains(sScope[0].toLowerCase()))
			{
				return FILTER_REMOVE;
			}
			rs.addMetaItem("docid", rank, docno); //can we know if this is needed?
			return FILTER_OK;
		}catch (Exception e) {
			return FILTER_OK;
		}
	}
}
