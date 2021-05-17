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
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
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
import org.terrier.utility.ArrayUtils;

/** A simple decorator, which applies all metadata in the MetaIndex to each retrieved, displayed document. */
@ProcessPhaseRequisites({ManagerRequisite.MQT, ManagerRequisite.RESULTSET})
public class SimpleDecorateProcess implements Process {

	static Logger logger = LoggerFactory.getLogger(SimpleDecorate.class);
	protected static final Pattern controlNonVisibleCharacters = Pattern.compile("[\\p{Cntrl}\uFFFD\uFFFF]|[^\\p{Graph}\\p{Space}]");

	public void process(Manager manager, Request q) {

		//we dont repeat decorating that has already happenned
		if (q.getControl("decorated").equals("true"))
			return;


		Matcher controlNonVisibleCharactersMatcher = controlNonVisibleCharacters.matcher("");
		try{
			final MetaIndex metaindex = q.getIndex().getMetaIndex();
			
			final String[] decorateKeys = q.getControl("decorate").equals("on") 
				? metaindex.getKeys()
				: ArrayUtils.parseCommaDelimitedString(q.getControl("decorate"));
			logger.debug("Decorating for " + java.util.Arrays.toString(decorateKeys));
			if (decorateKeys.length == 0){
				logger.warn("SimpleDecorate called, but no meta keys detected - either metaindex is empty, or decorate control is empty");
				return;
			}
			ResultSet res = q.getResultSet();
			final int num_docs = res.getResultSize();
			final String[][] meta = metaindex.getItems(decorateKeys, res.getDocids());

			for(int j=0;j<decorateKeys.length;j++)
			{
				String[] finalmeta = new String[num_docs];
				for (int i=0;i<num_docs;i++)
				{
					String value = meta[i][j];
					controlNonVisibleCharactersMatcher.reset(value);
					finalmeta[i] = controlNonVisibleCharactersMatcher.replaceAll("");
				}
				res.addMetaItems(decorateKeys[j], finalmeta);
			}
		}
		catch (Exception e) {
			logger.error("Problem performing decoration", e);
		}
	}

}
