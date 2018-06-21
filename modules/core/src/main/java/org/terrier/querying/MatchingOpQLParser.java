package org.terrier.querying;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;
import org.terrier.matching.matchops.ParseException;

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
		} catch (ParseException pe) {
			logger.error("Error while parsing the query.",pe);
		}
	}

}
