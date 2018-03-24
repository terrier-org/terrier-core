package org.terrier.querying;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.querying.parser.QueryParser;
import org.terrier.querying.parser.QueryParserException;

@ProcessPhaseRequisites(ManagerRequisite.RAWQUERY)
public class TerrierQLParser implements Process {

	protected static final Logger logger = LoggerFactory.getLogger(TerrierQLParser.class);
	
	@Override
	public void process(Manager manager, Request q) {
		try{
			QueryParser.parseQuery(q.getOriginalQuery(), q);	
		} catch (QueryParserException qpe) {
			logger.error("Error while parsing the query.",qpe);
		}
	}

}
