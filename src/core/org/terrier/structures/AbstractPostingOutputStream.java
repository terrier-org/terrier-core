/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is AbstractPostingOutputStream.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk> (original contributor)
 */

package org.terrier.structures;

import java.io.IOException;
import java.util.Iterator;

import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;

/**
 * Abstract class that is extended by other posting output stream implementations
 * 
 * @author Craig Macdonald
 * @since 4.0
 *
 */
public abstract class AbstractPostingOutputStream {

	public AbstractPostingOutputStream() {
		super();
	}

	public abstract BitFilePosition getOffset();

	public abstract void close();

	public abstract BitIndexPointer writePostings(int[][] postings, int startOffset,
			int Length, int firstId) throws IOException;

	public abstract BitIndexPointer writePostings(IterablePosting postings) throws IOException;

	public abstract BitIndexPointer writePostings(IterablePosting postings, int previousId)
			throws IOException;

	public abstract BitIndexPointer writePostings(Iterator<Posting> iterator, int previousId)
			throws IOException;

	public abstract BitIndexPointer writePostings(Iterator<Posting> iterator) throws IOException;

	public abstract Class<? extends IterablePosting> getPostingIteratorClass();

}