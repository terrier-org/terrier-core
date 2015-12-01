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
 * The Original Code is IncrementalSelectiveMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime.matching;

import java.util.List;

import org.terrier.structures.Index;

/**
 * This class is used by the incremental indexer to do selective matching on a 
 * MultiIndex structure. The basic idea is that you specify a policy to determine
 * which of the index shards actually get used for each specific query. This is
 * useful because each shard can be seen as a subset of the documents indexed for
 * a specific time, hence this class enables one to specify that I want to retrieve
 * from only the last h hours of documents for example. What selective matching
 * actually means will be dependent upon the flush policy that is selected, i.e.
 * is the boundary for a shard based on time, number of documents, etc. 
 * 
 * @author Richard McCreadie
 * @since 4.0
 */
public class IncrementalSelectiveMatching {
	
	/**
	 * Empty Constructor
	 */
	public IncrementalSelectiveMatching() {}
	
	/**
	 * Get an new IncrementalSelectiveMatching policy object of the
	 * specified type.
	 * @param policy a string describing the policy to be applied. "mostrecent" results in {@link IncrementalSelectiveMostRecent}
	 * @return {@link IncrementalSelectiveMostRecent} unless policy is "mostrecent"
	 */
	public static IncrementalSelectiveMatching get(String policy) {
		if (policy.equalsIgnoreCase("mostrecent")) return new IncrementalSelectiveMostRecent();
		return new IncrementalSelectiveMatching();
	}
	
	/**
	 * Return all indices (default)
	 * @param indices the list of indices to be filtered. 
	 * @return all indices
	 */
	public List<Index> getSelectedIndices(List<Index> indices) {
		return indices;
	}

}
