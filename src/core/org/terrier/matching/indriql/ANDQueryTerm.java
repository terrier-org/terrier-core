package org.terrier.matching.indriql;

import java.io.IOException;
import java.util.List;

import org.terrier.structures.EntryStatistics;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.postings.ANDIterablePosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.ArrayUtils;


public class ANDQueryTerm extends MultiQueryTerm {
	
	public static final String STRING_PREFIX = "#band";

	private static final long serialVersionUID = 1L;

	public ANDQueryTerm(String[] ts) {
		super(ts);
	}
	
	public ANDQueryTerm(QueryTerm[] ts) {
		super(ts);
	}

	@Override
	protected IterablePosting createFinalPostingIterator(
			List<IterablePosting> postings, List<LexiconEntry> pointers)
			throws IOException {
		return new ANDIterablePosting(
				postings.toArray(new IterablePosting[0]), 
				pointers.toArray(new Pointer[0]));		
	}
	
	
	/** Adjust the statistics for this operator:
	 * 1. The number of occurrences is the minimum of the constituent frequencies (handled by super).
	 * 2. The in-document maxTF is the minimum of the constituent maxTFs */
	@Override
	protected EntryStatistics mergeStatistics(EntryStatistics[] entryStats) {
		int minTF = Integer.MAX_VALUE;
		int minNt = Integer.MAX_VALUE;
		for(EntryStatistics e : entryStats)
		{
			if (e.getMaxFrequencyInDocuments() < minTF)
				minTF = e.getMaxFrequencyInDocuments();
			if (e.getDocumentFrequency() < minNt)
				minNt = e.getDocumentFrequency();
		}
		
		//TODO update minNt
		EntryStatistics rtr = super.mergeStatistics(entryStats);		
		rtr.setMaxFrequencyInDocuments(minTF);
		return rtr;
	}

	@Override
	public String toString() {
		return STRING_PREFIX + "(" + ArrayUtils.join(terms, ' ') + ")";
	}
	
	public ANDQueryTerm clone()
	{
		throw new UnsupportedOperationException(); //TODO
	}

}
