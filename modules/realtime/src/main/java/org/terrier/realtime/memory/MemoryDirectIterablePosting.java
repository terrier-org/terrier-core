package org.terrier.realtime.memory;

import java.io.IOException;

import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.IterablePostingImpl;
import org.terrier.structures.postings.WritablePosting;

import gnu.trove.TIntArrayList;

public class MemoryDirectIterablePosting extends IterablePostingImpl {

	/*
	 * Postings data structures.
	 */
	protected int index = -1;
	protected TIntArrayList pl_termids = new TIntArrayList();
	private TIntArrayList pl_freq = new TIntArrayList();

	/**
	 * Constructor.
	 */
	public MemoryDirectIterablePosting(TIntArrayList pl_termids,
			TIntArrayList pl_freq) {
		this.pl_termids = pl_termids;
		this.pl_freq = pl_freq;
	}

	/** {@inheritDoc} */
	public int getFrequency() {
		return pl_freq.get(index);
	}


	/** {@inheritDoc} */
	public int getId() {
		if (pl_termids.size()==0) {
			// special case: the posting list is empty, but some retrieval code (i.e. DAAT retrieval) assumes 
			//               that each posting list must have at least one document in it. So we add a new document
			//               with no terms in it
			pl_termids.add(0);
			pl_freq.add(0);
		}
		return pl_termids.get(index);
	}

	/** {@inheritDoc} */
	public int next() throws IOException {
		if ((pl_termids == null) || (++index >= pl_termids.size()))
			return EOL;
		else {
			//System.err.println("DI: " +getId());
			return getId();
		}
	}

	/** {@inheritDoc} */
	public boolean endOfPostings() {
		if ((pl_termids == null) || (index >= pl_termids.size()-1) || pl_termids.size()==0)
			return true;
		else
			return false;
	}

	/** {@inheritDoc} */
	public void close() throws IOException {
		index = -1;
		pl_termids = null;
		pl_freq = null;
	}

	/** Not implemented. */
	public void setId(int id) {
	}

	/** {@inheritDoc} */
	public WritablePosting asWritablePosting() {
		BasicPostingImpl bp = new BasicPostingImpl();
		bp.setId(getId());
		bp.setTf(getFrequency());
		return bp;
	}

	@Override
	public int getDocumentLength() {
		return 0;
	}

}
