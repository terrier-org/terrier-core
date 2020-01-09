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
 * The Original Code is MultiDirect.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime.multi;

import java.io.IOException;

import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;


/**
 * Multi Direct Index structure. This class is based upon the
 * MultiInverted structure.
 * @author Richard McCreadie
 * @since 4.0
 *
 */
public class MultiDirect implements PostingIndex<Pointer> {

	private PostingIndex<Pointer>[] postings;
	private MultiLexicon lex;
	
	boolean blocks;
	boolean fields;

	/**
	 * Constructor.
	 */
	public MultiDirect(PostingIndex<Pointer>[] postings,  MultiLexicon lex,  boolean blocks, boolean fields) {
		this.postings = postings;
		this.blocks = blocks;
		this.fields = fields;
		this.lex = lex;
	}

	/** {@inheritDoc} */
	public IterablePosting getPostings(Pointer _multiPointer)
			throws IOException {
		MultiDocumentEntry die = (MultiDocumentEntry) _multiPointer;
		int shard = die.getDocumentIndexShardIndex();
		return MultiDirectIterablePosting.of(
				postings[shard].getPostings(die.innerDocumentIndexEntry), lex, shard, blocks, fields);
	}

	/** Not implemented. */
	public void close() throws IOException {
	}
}
