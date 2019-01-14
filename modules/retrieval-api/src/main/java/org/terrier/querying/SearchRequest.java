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
 * The Original Code is SearchRequest.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
  *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
  *   Dyaa Albakour <dyaa{a.}dcs.gla.ac.uk>
 */
package org.terrier.querying;
import java.io.Serializable;
import java.util.Map;
/** SearchRequest is the one of two main classes of which are made available to client code by
  * the Terrier framework at retrieval time. Each search query, whether entered by a user or
  * in batch mode retrieval creates a search request. Each search request is then passed to 4
  * methods of the Manager that is handling each search request: runPreProcessing, runMatching,
  * runPostProcess and runPostFilters
  * Example usage:
  * <pre>
  * IndexRef indexref = IndexRef.of("/path/to/my/index/data.properties");
  * IManager manager = ManagerFactor.from(indexref);
  * SearchRequest srq = manager.newSearchRequest("my query");
  * //run the query
  * manager.runSearchRequest(srq);
  * </pre>
  * <P><B>NB:</B>Controls (name, value String tuples) are used to control the retrieval process. You may 
  * want to set controls in your application code. However, default controls can be set using 
  * the <tt>querying.default.controls</tt> property in the terrier.properties file.
  * <p><b>Context Objects</b> (name, value object tuples) are used to pass arbitrary information to classes
  * within Terrier.
  */
public interface SearchRequest extends Serializable
{
	public static final String CONTROL_WMODEL = "wmodel";
	public static final String CONTROL_MATCHING = "matching";
	
	
	/** Set the matching model and weighting model that the Manager should use for this query.
	  * The Matching model  should be a subclass of org.terrier.matching.Matching, and
	  * the weighting model should implement org.terrier.matching.Model.<br>
	  * Example: <tt>request.addMatchingModel("Matching", "PL2")</tt>
	  * @param MatchingModelName the String class name that should be used
	  * @param WeightingModelName the String class name that should be used
	  * @deprecated
	  */
	void addMatchingModel(String MatchingModelName, String WeightingModelName);

	/** Set a unique identifier for this query request.
	  * @param qid the unique string identifier
	  */
	void setQueryID(String qid);
	/** Set a control named to have value Value. This is essentially a
	  * hashtable wrappers. Controls are used to set properties of the
	  * retrieval process.
	  * @param Name the name of the control to set the value of.
	  * @param Value the value that the control should take. */
	void setControl(String Name, String Value);
	/** Returns the query id as set by setQueryID(String).
	  * @return the query Id as a string. */
	String getQueryID();
	
	Map<String,String> getControls();
	
	default boolean hasControl(String Name)
	{
		return getControl(Name).length() > 0;
	}
	
	/** Returns the value of the control.empty string if not set.
	  * @return the value. */
	String getControl(String Name);
	
	/** Returns the value of the control, or Default if not set.
	  * @return the value. */
	default String getControl(String Name, String Default)
	{
		return hasControl(Name) ? getControl(Name) : Default;
	}
	
	/** Set if the query input had no terms.
	  * @return true if the query has no terms. Used by Manager.runMatching() to short circuit the
	  * matching process - if this is set, then a resultset with 0 documents is created
	  * automatically.
	  * - return true if the query has no terms */
	boolean isEmpty();

	/**
	 * sets the original query, before any preprocessing
	 */
	void setOriginalQuery(String q);
	
	/**
	 * gets the original query, before any preprocessing
	 */
	String getOriginalQuery();
	
	/**
	 * Sets the number of documents returned for a search request, after
	 * applying post filtering
	 * @param n
	 */
	void setNumberOfDocumentsAfterFiltering(int n);
	
	/**
	 * gets the number of documents returned for a search request, after
	 * applying post filtering
	 * @return the number of documents returned for a search request. integer.
	 */
	int getNumberOfDocumentsAfterFiltering();
	/**
	 * Returns the time the process start.
	 * @return time long
	 */
	long getStartedProcessingTime();
	
	/**
	 * Sets the started processing time.
	 * @param time
	 */
	void setStartedProcessingTime(long time);
	
	/** Get the results */
	public ScoredDocList getResults();
	
	/**
	 *  Set a value of a context object.
	 * @param key the key of the context object
	 * @param value the value of the context object
	 * @since 3.6
	 */
	public void setContextObject(String key, Object value);
	
	/**
	 * Returns the value of a context object.
	 * @param key the key of the context object to get
	 * @return the value of the context object for the given key
	 * @since 3.6
	 */
	public Object getContextObject(String key);

	
	
}
/* the following methods are implmented in the SearchRequest - ie Request.java
   so are only visible from inside the querying package:
        public String getWeightingModel()
        public String getMatchingModel()
        public void setControl(String Name, String Value)
        public void setResultSet(ResultSet results)
        public ResultSet getResultSet()
        public String getControl(String Name)
        public Map<String,String> getControlHashtable()
        public boolean isEmpty()
        public void setEmpty(boolean in)
        public void setMatchingQueryTerms(MatchingQueryTerms mqts)
        public MatchingQueryTerms getMatchingQueryTerms()
*/
