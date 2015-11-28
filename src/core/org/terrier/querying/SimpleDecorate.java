/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is SimpleDecorate.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.querying;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.ResultSet;
import org.terrier.structures.MetaIndex;

/** A simple decorator, which applies all metadata in the MetaIndex to each retrieved, displayed document. */
public class SimpleDecorate implements PostFilter {

	static Logger logger = LoggerFactory.getLogger(SimpleDecorate.class);
	protected static final Pattern controlNonVisibleCharacters = Pattern.compile("[\\p{Cntrl}\uFFFD\uFFFF]|[^\\p{Graph}\\p{Space}]");
	
	MetaIndex meta = null;
	String[] decorateKeys = null;
	
	Matcher controlNonVisibleCharactersMatcher = controlNonVisibleCharacters.matcher("");
	/** 
	 * Adds all the metadata for the specified document occurring at the specified
	 * rank to the ResultSet
	 * {@inheritDoc} 
	 */
	public final byte filter(
			Manager m, SearchRequest srq, ResultSet results,
			int rank, int docid) 
	{
		try{
			final String[] values = meta.getItems(decorateKeys, docid);
			for(int j=0;j<decorateKeys.length;j++)
			{
				controlNonVisibleCharactersMatcher.reset(values[j]);
				results.addMetaItem(decorateKeys[j], rank, controlNonVisibleCharactersMatcher.replaceAll(""));
			}
			return PostFilter.FILTER_OK;
		} catch (Exception e) {
			return PostFilter.FILTER_REMOVE;
		}
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void new_query(Manager m, SearchRequest srq, ResultSet rs) 
	{
		meta = ((Request)srq).getIndex().getMetaIndex();
		decorateKeys = meta.getKeys();
	}

}
