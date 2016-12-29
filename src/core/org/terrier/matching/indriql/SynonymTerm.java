package org.terrier.matching.indriql;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.ORIterablePosting;
import org.terrier.utility.ArrayUtils;

public class SynonymTerm extends MultiQueryTerm {

	public static final String STRING_PREFIX = "#syn";
	private static final long serialVersionUID = 1L;
	
	
	protected static final Logger logger = LoggerFactory.getLogger(SynonymTerm.class);
	
	public SynonymTerm(QueryTerm[] ts) {
		super(ts);
	}

	
	public SynonymTerm(String[] ts) {
		super(ts);
	}

	@Override
	public String toString() {
		return STRING_PREFIX + "("+ArrayUtils.join(terms, ' ')+")";
	}

	/** Adjust the statistics for the #syn operator:
	 * 1. The total number of occurrences F is the sum of the constituent frequencies (handled by super).
	 * 2. The in-document maxTF is the maximum of the constituent maxTFs */
	@Override
	protected EntryStatistics mergeStatistics(EntryStatistics[] entryStats) {
		EntryStatistics rtr = super.mergeStatistics(entryStats);
		int minTF = Integer.MIN_VALUE;
		for(EntryStatistics e : entryStats)
		{
			if (e.getMaxFrequencyInDocuments() > minTF)
				minTF = e.getMaxFrequencyInDocuments();
		}
		rtr.setMaxFrequencyInDocuments(minTF);
		return rtr;
	}


	protected IterablePosting createFinalPostingIterator(
			List<IterablePosting> postings,
			List<LexiconEntry> pointers) throws IOException {
		return ORIterablePosting.mergePostings(postings.toArray(new IterablePosting[postings.size()]));
	}

	

}
