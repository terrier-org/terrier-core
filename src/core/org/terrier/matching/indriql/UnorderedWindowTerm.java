package org.terrier.matching.indriql;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.SimpleNgramEntryStatistics;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.ProximityIterablePosting;
import org.terrier.utility.ArrayUtils;

public class UnorderedWindowTerm extends MultiQueryTerm {

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
	
	@Override
	Pair<EntryStatistics, IterablePosting> getPostingIterator(Index index)
			throws IOException {
		Pair<EntryStatistics, IterablePosting> p = super.getPostingIterator(index);
		SimpleNgramEntryStatistics nes = new SimpleNgramEntryStatistics(p.getKey());
		nes.setWindowSize(distance);
		return Pair.of((EntryStatistics) nes, p.getValue());
	}

	@Override
	public String toString() {
		return STRING_PREFIX +distance+"("+ArrayUtils.join(terms, ' ')+")";
	}

	@Override
	protected IterablePosting createFinalPostingIterator(
			List<IterablePosting> postings, List<LexiconEntry> pointers)
			throws IOException {
		return new ProximityIterablePosting(
				postings.toArray(new IterablePosting[postings.size()]),
				pointers.toArray(new LexiconEntry[pointers.size()]), 
				distance);
	}

}
