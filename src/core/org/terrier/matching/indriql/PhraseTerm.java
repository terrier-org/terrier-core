package org.terrier.matching.indriql;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.SimpleNgramEntryStatistics;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.PhraseIterablePosting;
import org.terrier.utility.ArrayUtils;

public class PhraseTerm extends MultiQueryTerm {

	public static final String STRING_PREFIX = "#1";
	
	private static final long serialVersionUID = 1L;

	public PhraseTerm(QueryTerm[] ts) {
		super(ts);
	}
	
	public PhraseTerm(String[] ts) {
		super(ts);
	}

	@Override
	public String toString() {
		return STRING_PREFIX + "(" + ArrayUtils.join(terms, ' ') + ")";
	}

	@Override
	Pair<EntryStatistics, IterablePosting> getPostingIterator(Index index)
			throws IOException {
		Pair<EntryStatistics, IterablePosting> p = super.getPostingIterator(index);
		SimpleNgramEntryStatistics nes = new SimpleNgramEntryStatistics(p.getKey());
		nes.setWindowSize(1);//OR should this be terms.length
		return Pair.of((EntryStatistics) nes, p.getValue());
	}

	@Override
	protected IterablePosting createFinalPostingIterator(
			List<IterablePosting> postings, List<LexiconEntry> pointers)
			throws IOException {
		return new PhraseIterablePosting(
				postings.toArray(new IterablePosting[postings.size()]), 
				pointers.toArray(new LexiconEntry[pointers.size()]), false);
	}

}
