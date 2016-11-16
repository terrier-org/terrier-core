package org.terrier.querying;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;

@ProcessPhaseRequisites(ManagerRequisite.MQT)
public class NoNegativeReq implements Process {

	@Override
	public void process(Manager manager, SearchRequest q) {
		Request rq = (Request)q;
		MatchingQueryTerms mqt = rq.getMatchingQueryTerms();
		for(MatchingTerm qt  : mqt)
		{
			if (qt.getValue().required != null && qt.getValue().required == false)
				qt.getValue().required = null;
		}
	}

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName();
	}

}
