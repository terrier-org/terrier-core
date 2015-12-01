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
 * The Original Code is FatCandidateResult.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Eric Sutherland
 */

/**
 * author: Eric Sutherland
 * 
 * based on original Terrier code by Craig MacDonald etc.
 */

package org.terrier.matching.daat;

import org.terrier.structures.postings.WritablePosting;
/** A version of {@link CandidateResult} suitable for use within the Fat framework
 * by {@link FatCandidateResultSet}.
 * 
 * @author Eric Sutherland, Craig Macdonald
 * @since 4.0
 */
public class FatCandidateResult extends CandidateResult {

	protected WritablePosting[] postings;
	
	public FatCandidateResult(int id, int postingCount) {
		super(id);
		postings = new WritablePosting[postingCount];
	}
	
	public void setPosting(int term, WritablePosting p) {
		postings[term] = p;
	}

	public WritablePosting[] getPostings() {
		return postings;
	}
	
}
