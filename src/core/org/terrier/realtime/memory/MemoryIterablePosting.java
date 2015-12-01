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
 * The Original Code is MemoryIterablePosting.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory;

import gnu.trove.TIntArrayList;

import java.io.IOException;

import org.terrier.structures.DocumentIndex;
import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.IterablePostingImpl;
import org.terrier.structures.postings.WritablePosting;

/**
 * A postings list implementation held fully in memory.
 * 
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class MemoryIterablePosting extends IterablePostingImpl {

	/*
	 * Postings data structures.
	 */
	protected int index = -1;
	protected DocumentIndex doi;
	protected TIntArrayList pl_doc = new TIntArrayList();
	private TIntArrayList pl_freq = new TIntArrayList();

	/**
	 * Constructor.
	 */
	public MemoryIterablePosting(DocumentIndex doi, TIntArrayList pl_doc,
			TIntArrayList pl_freq) {
		this.doi = doi;
		this.pl_doc = pl_doc;
		this.pl_freq = pl_freq;
	}

	/** {@inheritDoc} */
	public int getFrequency() {
		return pl_freq.get(index);
	}

	/** {@inheritDoc} */
	public int getDocumentLength() {
		try {
			return doi.getDocumentLength(pl_doc.get(index));
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/** {@inheritDoc} */
	public int getId() {
		if (pl_doc.size()==0) {
			// special case: the posting list is empty, but some retrieval code (i.e. DAAT retrieval) assumes 
			//               that each posting list must have at least one document in it. So we add a new document
			//               with no terms in it
			pl_doc.add(0);
			pl_freq.add(0);
		}
		return pl_doc.get(index);
	}

	/** {@inheritDoc} */
	public int next() throws IOException {
		if ((pl_doc == null) || (++index >= pl_doc.size()))
			return EOL;
		else
			return getId();
	}

	/** {@inheritDoc} */
	public boolean endOfPostings() {
		if ((pl_doc == null) || (index >= pl_doc.size()) || pl_doc.size()==0)
			return true;
		else
			return false;
	}

	/** {@inheritDoc} */
	public void close() throws IOException {
		index = -1;
		doi = null;
		pl_doc = null;
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

}
