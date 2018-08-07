package org.terrier.querying;

import java.io.IOException;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.structures.Index;

public interface MQTRewritingProcess extends Process {

	public boolean expandQuery(MatchingQueryTerms mqt, Request rq) throws IOException;
	default public void configureIndex(Index index){}
	
}
