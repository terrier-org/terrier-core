package org.terrier.matching.matchops;

import java.io.IOException;
import java.util.List;

import org.terrier.structures.EntryStatistics;
import org.terrier.structures.SimpleNgramEntryStatistics;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.PhraseIterablePosting;
import org.terrier.utility.ArrayUtils;
/** This combines multiple operators into a single op, where they occur adjacently. 
 * It is logically equivalent to Indri's #1() operator.
 * @since 5.0
 */
public class PhraseOp extends ANDQueryOp {

	public static final String STRING_PREFIX = "#1";
	
	private static final long serialVersionUID = 1L;

	public PhraseOp(Operator[] ts) {
		super(ts);
	}
	
	public PhraseOp(String[] ts) {
		super(ts);
	}

	@Override
	public String toString() {
		return STRING_PREFIX + "(" + ArrayUtils.join(terms, ' ') + ")";
	}
	
	@Override
	protected EntryStatistics mergeStatistics(EntryStatistics[] entryStats) {
		EntryStatistics parent = super.mergeStatistics(entryStats);
		SimpleNgramEntryStatistics nes = new SimpleNgramEntryStatistics(parent);
		nes.setWindowSize(1); //OR should this be terms.length
		return nes;
	}

	@Override
	protected IterablePosting createFinalPostingIterator(
			List<IterablePosting> postings, List<EntryStatistics> pointers)
			throws IOException {
		return new PhraseIterablePosting(
				postings.toArray(new IterablePosting[postings.size()]), 
				pointers.toArray(new EntryStatistics[pointers.size()]), false);
	}

}
