package org.terrier.matching.matchops;

import org.apache.commons.text.similarity.EditDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.terrier.utility.ArrayUtils;

/** A synonym class that uses leveinsten distance to match terms.
 * Has a number of configuration options from the MatchOpQL parser:
 * <ul>
 * <li>fuzziness - The maximum Levenshtein Edit Distance threshold (integer), or AUTO, or AUTO.lowT.highT. Default is AUTO.3.6. See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/common-options.html#fuzziness">Elastic option reference</a>.</li>
 * <li>prefix_length - The number of initial characters which must match to accept a term - See Elastic's <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-fuzzy-query.html">fuzzy documentation</a></li>
 * <li>max_expansions - The maximum number of terms to accept into the synonym group - See Elastic's <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-fuzzy-query.html">fuzzy documentation</a></li>
 * </ul>
 * @author Craig Macdonald
 * @since 5.0
 */
public class FuzzyTermOp extends PrefixTermOp {

	public static final String STRING_PREFIX = "#fuzzy";
	
	private static final long serialVersionUID = 1L;
	
	final EditDistance<Integer> lev = new LevenshteinDistance();
	final int prefix_length;
	
	public FuzzyTermOp(String searchString)
	{
		this(searchString, null, null, null, null, null);
	}

	public FuzzyTermOp(String searchString, 
			Integer _prefix_length, Integer maxExpansions, Integer maxDist, 
			Integer _lowT, Integer _highT) {
		super(searchString);
		if (_prefix_length != null) {
			prefix_length = _prefix_length;
		} else {
			prefix_length = 0;
		}
		if (prefix_length == 0)
			logger.warn("prefix_length of 0 is expensive to match terms");
		if (maxExpansions != null)
		{
			maxMatch = maxExpansions;
		}
		if (maxDist != null) {
			super.predFunction = (t -> lev.apply(searchString, t) <= maxDist);
		}
		else //maxDist == auto
		{
			final int lowT, highT;
			if (_lowT == null)
				lowT = 3;
			else
				lowT = _lowT;
			if (_highT == null)
				highT = 6;
			else
				highT = _highT;
			
			super.predFunction = (t -> {
				if (t.length() < lowT)
				{
					return t.equals(searchString);
				}
				else if (t.length() < highT) {
					return lev.apply(searchString, t) <= 1;
				}
				else {
					return lev.apply(searchString, t) <= 2;
				}
			});
		}
	}
	
	protected String getStartString(String search)
	{
		if (prefix_length == 0)
			return String.valueOf(Character.MIN_VALUE);
		return search.substring(0, prefix_length);
	}
	
	protected String getEndString(String termLo)
	{
		if (prefix_length == 0)
			return String.valueOf(Character.MAX_VALUE);
		return termLo + Character.MAX_VALUE;
	}
	
	@Override
	public String toString() {
		return STRING_PREFIX + "("+ArrayUtils.join(terms, ' ')+")";
	}
	
	

}
