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
 * The Original Code is GrowingMapQueryResultCache.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 *   Ben He <ben{a.}dcs.gla.ac.uk>
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.structures.cache;

import java.util.HashMap;
import java.util.Map;

import org.terrier.matching.ResultSet;
import org.terrier.querying.SearchRequest;

/** an astract results cache that puts stuff into an ever-growing Map */
public abstract class GrowingMapQueryResultCache<K> implements QueryResultCache 
{
	Map<K, ResultSet> cache = new HashMap<K, ResultSet>();		
	public void reset()
	{
		cache.clear();
	}
	protected abstract K hashQuery(SearchRequest q);
	
	public void add(SearchRequest q)
	{
		cache.put(hashQuery(q), q.getResultSet());
	}		
	
	public ResultSet checkCache(SearchRequest q)
	{
		return cache.get(hashQuery(q));
	}		
}
