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
 * The Original Code is TerrierQLToMatchingQueryTerms.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.querying;

import org.terrier.matching.BaseMatching;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;
import org.terrier.querying.parser.Query;
import org.terrier.querying.parser.Query.QueryTermsParameter;
import org.terrier.utility.ApplicationSetup;

@ProcessPhaseRequisites(ManagerRequisite.TERRIERQL)
public class TerrierQLToMatchingQueryTerms implements Process {

	protected static final boolean lowercase = Boolean.parseBoolean(ApplicationSetup.getProperty("lowercase", "true"));
	
	@Override
	public void process(Manager manager, Request rq) {
		MatchingQueryTerms queryTerms = new MatchingQueryTerms(rq.getQueryID(), rq);
		Query query = rq.getQuery();
		query.obtainQueryTerms(QueryTermsParameter.of(queryTerms, lowercase));
		for(MatchingTerm me : queryTerms)
			me.getValue().getTags().add(BaseMatching.BASE_MATCHING_TAG);
		rq.setMatchingQueryTerms(queryTerms);
	}

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName();
	}

}
