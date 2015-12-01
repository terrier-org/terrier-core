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
 * The Original Code is MultiIterablePosting.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.multi;

import java.io.IOException;

import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.IterablePostingImpl;
import org.terrier.structures.postings.WritablePosting;

/**
 * A posting list implementation used within a MultiIndex. It iterates over the posting
 * lists from multiple index shards. 
 * 
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class MultiIterablePosting extends IterablePostingImpl implements
		IterablePosting {

	private IterablePosting[] children;
	private int[] offsets;
	private int currentChild = 0;

	/**
	 * Constructor.
	 */
	public MultiIterablePosting(IterablePosting[] constituentIPs, int[] offsets) {
		this.children = constituentIPs;
		this.offsets = offsets;
		currentChild = 0;
	}

	/** {@inheritDoc} */
	public int next() throws IOException {
		if (children[currentChild] != null) {
			int id = children[currentChild].next();
			if (id != IterablePosting.EOL)
				return id + offsets[currentChild];
		}
		currentChild++;
		if (currentChild == children.length)
			return IterablePosting.EOL;
		return next();
	}

	/** {@inheritDoc} */
	public boolean endOfPostings() {
		return currentChild >= children.length;
	}

	/** {@inheritDoc} */
	public int getId() {
		return children[currentChild].getId() + offsets[currentChild];
	}

	/** {@inheritDoc} */
	public int getFrequency() {
		return children[currentChild].getFrequency();
	}

	/** {@inheritDoc} */
	public int getDocumentLength() {
		return children[currentChild].getDocumentLength();
	}

	/** Not implemented. */
	public void setId(int id) {
	}

	/** Not implemented. */
	public WritablePosting asWritablePosting() {
		return null;
	}

	/** Not implemented. */
	public void close() throws IOException {
	}

}
