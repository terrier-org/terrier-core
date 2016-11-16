package org.terrier.matching.indriql;

import java.io.IOException;
import java.util.List;

import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.postings.ANDIterablePosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.ArrayUtils;


public class ANDQueryTerm extends MultiQueryTerm {

	private static final long serialVersionUID = 1L;

	public ANDQueryTerm(String[] ts) {
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

	@Override
	public String toString() {
		return "#band(" + ArrayUtils.join(terms, ' ') + ")";
	}
	
	public ANDQueryTerm clone()
	{
		throw new UnsupportedOperationException(); //TODO
	}

}
