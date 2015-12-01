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
 * The Original Code is PostingIndexInputStream.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import org.terrier.structures.postings.IterablePosting;
/** 
 * Interface for reading postings from an input stream
 */
public interface PostingIndexInputStream extends Closeable, Iterator<IterablePosting> 
{
	/** Return the next IterablePosting object, or null if none defined.
	 * Use this method instead of hasNext() and next().
	 * @return IterablePosting postings for next object
	 * @throws IOException if an I/O problem occurs.
	 */
	IterablePosting getNextPostings() throws IOException;

	/** Returns the number of postings in the current IterablePosting object */
	int getNumberOfCurrentPostings();
	
	/** Returns the pointer associated with the current postings being accessed */
	Pointer getCurrentPointer();
	
	/**
	 * Returns the number of entries that were skipped during
	 * a call to the next().
	 * @return int the number of entries skipped.
	 */
	int getEntriesSkipped();

	/** Renders the entire structure to stdout */
	void print();
}
