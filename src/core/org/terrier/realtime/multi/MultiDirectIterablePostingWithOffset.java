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
 * The Original Code is MultiDirectIterablePostingWithOffset.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime.multi;

import java.io.IOException;

import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.WritablePosting;

/**
 * This acts as a wrapper class that surrounds an IterablePosting object. It is designed
 * for use with the MultiDirect structure, to allow fast termid lookups. The motivation is
 * that when iterating over a direct index, the termids that come out are index shard local.
 * To fix this, we add on the number of terms in each preceding lexicon to turn local termids
 * into global ones. The MultiLexicon is then responsible for converting the global termid into
 * its local termid and index shard components, which can be used to do the termid lookup. 
 * @author Richard McCreadie
 * @since 4.0
 *
 */
public class MultiDirectIterablePostingWithOffset implements IterablePosting {

	IterablePosting posting;
	int idoffset;
	
	public MultiDirectIterablePostingWithOffset(IterablePosting posting, int idoffset) {
		this.posting = posting;
		this.idoffset = idoffset;
	}

	@Override
	public int getId() {
		//System.err.println("PostingWrapper: Local ID="+posting.getId());
		return posting.getId()+idoffset;

	}

	@Override
	public int getFrequency() {
		return posting.getFrequency();
	}

	@Override
	public int getDocumentLength() {
		return posting.getDocumentLength();
	}

	@Override
	public void setId(int id) {
		posting.setId(id);
		
	}

	@Override
	public WritablePosting asWritablePosting() {
		return posting.asWritablePosting();
	}

	@Override
	public void close() throws IOException {
		posting.close();
	}

	@Override
	public int next() throws IOException {
		return posting.next();
	}

	@Override
	public int next(int targetId) throws IOException {
		return posting.next(targetId);
	}

	@Override
	public boolean endOfPostings() {
		return posting.endOfPostings();
	}
	
}
