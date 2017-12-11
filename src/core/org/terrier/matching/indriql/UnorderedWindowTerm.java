package org.terrier.matching.indriql;

import java.io.IOException;
import java.util.List;

import org.terrier.structures.EntryStatistics;
import org.terrier.structures.SimpleNgramEntryStatistics;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.ProximityIterablePosting;
import org.terrier.utility.ArrayUtils;

public class UnorderedWindowTerm extends ANDQueryTerm {

	public static final String STRING_PREFIX = "#uw";
	private static final long serialVersionUID = 1L;
	
	
	int distance;
	
	public UnorderedWindowTerm(QueryTerm[] ts, int dist) {
		super(ts);
		this.distance = dist;
	}
	
	public UnorderedWindowTerm(String[] ts, int dist) {
		super(ts);
		this.distance = dist;
	}
	
	public int getDistance()
	{
		return this.distance;
	}
	
	
	@Override
	protected EntryStatistics mergeStatistics(EntryStatistics[] entryStats) {
		EntryStatistics parent = super.mergeStatistics(entryStats);
		SimpleNgramEntryStatistics nes = new SimpleNgramEntryStatistics(parent);
		nes.setWindowSize(distance);
		return nes;
	}

	@Override
	public String toString() {
		return STRING_PREFIX +distance+"("+ArrayUtils.join(terms, ' ')+")";
	}

	@Override
	protected IterablePosting createFinalPostingIterator(
			List<IterablePosting> postings, List<EntryStatistics> pointers)
			throws IOException {
		assert postings.size() <= distance;
		return new ProximityIterablePosting(
				postings.toArray(new IterablePosting[postings.size()]),
				pointers.toArray(new EntryStatistics[pointers.size()]), 
				distance);
	}

}
