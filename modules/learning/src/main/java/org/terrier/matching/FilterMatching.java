package org.terrier.matching;

import java.io.IOException;

public abstract class FilterMatching implements Matching {
	
	protected Matching parent;
	FilterMatching(Matching _parent)
	{
		this.parent = _parent;
	}
	
	@Override
	public ResultSet match(String queryNumber, MatchingQueryTerms queryTerms)
			throws IOException 
	{
		return doMatch(queryNumber, queryTerms, parent.match(queryNumber, queryTerms), true);
	}

	public ResultSet doMatch(String queryNumber, MatchingQueryTerms queryTerms, ResultSet match) throws IOException {
		return this.doMatch(queryNumber, queryTerms, match, true);
	}

	public abstract ResultSet doMatch(String queryNumber, MatchingQueryTerms queryTerms, ResultSet match, boolean keepInputScores) throws IOException;
}
