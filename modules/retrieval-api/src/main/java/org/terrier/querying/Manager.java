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
 * The Original Code is Manager.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.querying;

import java.util.Properties;

public interface Manager {

	/* -------------- factory methods for SearchRequest objects ---------*/
	/** Ask for new SearchRequest object to be made. This is internally a
	 * Request object */
	public abstract SearchRequest newSearchRequest();

	/** Ask for new SearchRequest object to be made. This is internally a
	 * Request object
	 * @param QueryID The request should be identified by QueryID
	 */
	public abstract SearchRequest newSearchRequest(String QueryID);

	/** Ask for new SearchRequest object to be made, instantiated using the 
	 * specified query id, and that the specified query should be parsed.
	 * @since 2.0
	 * @param QueryID The request should be identified by QueryID
	 * @param query The actual user query
	 * @return The fully init'd search request for use in the manager */
	public abstract SearchRequest newSearchRequest(String QueryID, String query);

	/** Ask for new SearchRequest object to be made given a query to be parsed
	 * @since 4.2
	 * @param query The actual user query
	 * @return The fully init'd search request for use in the manager */
	public abstract SearchRequest newSearchRequestFromQuery(String query);

	/** Provide a common interface for changing property values.
	 * @param key Key of property to set
	 * @param value Value of property to set */
	public abstract void setProperty(String key, String value);

	/** Set all these properties. Implemented using setProperty(String,String).
	 * @param p All properties to set */
	public abstract void setProperties(Properties p);

	/**
	 * This runs a given SearchRequest through the four retrieval stages and adds the ResultSet to the
	 * SearchRequest object. 
	 * @param srq - the SearchRequest to be processed
	 */
	public abstract void runSearchRequest(SearchRequest srq);
	
	public abstract IndexRef getIndexRef();

}
