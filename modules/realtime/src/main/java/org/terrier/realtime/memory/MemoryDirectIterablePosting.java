/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is MemoryDirectIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime.memory;

import java.io.IOException;

import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.IterablePostingImpl;
import org.terrier.structures.postings.WritablePosting;

import gnu.trove.TIntArrayList;

/**
 * Provides iteration capabilities over a Memory Direct Posting
 * @author richardm
 *
 */
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
		if (pl_termids == null) return true;
		if ( (index >= pl_termids.size()-1) || pl_termids.size()==0)
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

	/** {@inheritDoc} */
	public void setId(int id) {
		pl_freq.set(index, id);
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
