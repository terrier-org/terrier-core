package org.terrier.querying;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.querying.parser.Query;

@ProcessPhaseRequisites(ManagerRequisite.TERRIERQL)
public class TerrierQLToMatchingQueryTerms implements Process {

	@Override
	public void process(Manager manager, SearchRequest q) {
		Request rq = (Request) q;
		MatchingQueryTerms queryTerms = new MatchingQueryTerms(rq.getQueryID(), rq);
		Query query = rq.getQuery();
		query.obtainQueryTerms(queryTerms, null, null, null);
		rq.setMatchingQueryTerms(queryTerms);
	}

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName();
	}

}
