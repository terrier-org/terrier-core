package org.terrier.matching;
import org.terrier.structures.Index;
import java.io.IOException;
import org.terrier.learning.FeaturedQueryResultSet;

/** Implements the DocVectors style of computing additional features, however uses Direct2WritablePostings to 
 * make a FatResultsSet (from a DirectIndex) for rescoring documents for computing additional features.
 * Extends FatFeaturedScoringMatching. This class does NOT support features expressed using "tagged" query terms.
 */
public class DVFeaturedScoringMatching2 extends FatFeaturedScoringMatching {

	public DVFeaturedScoringMatching2(Index _index, Matching _parent, String[] _featureNames) throws Exception
	{
		super(_index, _parent, _featureNames);
	}
	
	public DVFeaturedScoringMatching2(Index _index, Matching _parent) throws Exception
	{
		super(_index, _parent);
	}

	@Override
	public ResultSet match(String queryNumber, MatchingQueryTerms queryTerms)
			throws IOException 
	{
		final ResultSet res = parent.match(queryNumber, queryTerms);
		if (res == null)
		{
			logger.warn("I got NO ResultSet from parent " + parent.getInfo() );
			return new FeaturedQueryResultSet(0);
		}
		return doMatch(queryNumber, queryTerms, res, true);
	}

	@Override
	public ResultSet doMatch(String queryNumber,
			MatchingQueryTerms queryTerms, ResultSet res, boolean keepInputScores) throws IOException 
	{
		final FatResultSet fatRs = Direct2WritablePostings.decorateFromDirect(res, this.index, queryTerms);
		return super.doMatch(queryNumber, queryTerms, fatRs, keepInputScores);
	}

}
