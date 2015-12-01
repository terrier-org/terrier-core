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
 * The Original Code is PostFilter.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
  *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.querying;
import org.terrier.matching.ResultSet;
/** PostFilters are designed to complement PostProcesses. While PostProcesses
  * operate on the entire resultset at once, with PostFilters, each PostFilter
  * is called for each result in the resultset. According to the return of <tt>filter()</tt>
  * the result can then be included, discarded, or (un)boosted in the resultset. Possible
  * return values for <tt>filter</tt> are FILTER_OK, FILTER_REMOVE, FILTER_ADJUSTED
  * Which PostFilters are run, and when is controlled by two properties, as mentioned below.<br/>
  * <B>Properties</B>
  * <ul>
  * <li><tt>querying.postfilters.controls</tt> : A comma separated list of control to PostFilter
  * class mappings. Mappings are separated by ":". eg <tt>querying.postfilters.controls=scope:Scope</tt></li>
  * <li><tt>querying.postfilters.order</tt> : The order postfilters should be run in</li></ul>
  * '''NB:''' Initialisation and running of post filters is carried out by the Manager.
  * @author Craig Macdonald
    */
public interface PostFilter
{
	/** This result should be included in the resultset */
	byte FILTER_OK = 0;
	/** This result should not be included in the resultset */
	byte FILTER_REMOVE = 1;
	/** The score for this result has been adjusted, please ensure the
	  * resultset is re-sorted */
	byte FILTER_ADJUSTED = 2;
	/** Called before the processing of a resultset using this PostFilter is applied.
	  * Can be used to save information for the duration of the query.
	  * @param m The manager controlling this query
	  * @param srq The search request being processed
	  * @param rs the resultset that is being iterated through
	  */
	void new_query(Manager m, SearchRequest srq, ResultSet rs);
	/**
	  * Called for each result in the resultset, used to filter out unwanted results.
	  * @param m The manager controlling this query
	  * @param srq The search request being processed
	  * @param DocAtNumber which array index (rank) in the resultset have we reached
	  * @param docid The docid of the currently being procesed result.
	  */
	byte filter(Manager m, SearchRequest srq, ResultSet results, int DocAtNumber, int docid);
}
