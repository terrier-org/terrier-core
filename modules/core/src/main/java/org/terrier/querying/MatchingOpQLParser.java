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
 * The Original Code is MatchingOpQLParser.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.querying;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;
import org.terrier.matching.matchops.ParseException;
import org.terrier.matching.matchops.TokenMgrError;
import org.terrier.querying.parser.QueryParserException;

@ProcessPhaseRequisites(ManagerRequisite.RAWQUERY)
public class MatchingOpQLParser implements Process {

	protected static final Logger logger = LoggerFactory.getLogger(MatchingOpQLParser.class);
	
	@Override
	public void process(Manager manager, Request q) {
		try{
			List<MatchingTerm> terms = new org.terrier.matching.matchops.MatchOpQLParser(q.getOriginalQuery()).parseAll();
			MatchingQueryTerms mqt = new MatchingQueryTerms(terms);
			q.setMatchingQueryTerms(mqt);
			mqt.setQueryId(q.getQueryID());
		} catch (ParseException | TokenMgrError e) {
			throw new QueryParserException("Could not parse query", e);
		}
	}

}
