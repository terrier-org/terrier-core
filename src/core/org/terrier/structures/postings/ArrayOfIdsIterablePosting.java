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
 * The Original Code is ArrayOfIdsIterablePosting.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.postings;

import java.io.IOException;

import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.IterablePostingImpl;
import org.terrier.structures.postings.WritablePosting;

/** An instance of IterablePostings that works with a passed array of ids 
 * @author Craig Macdonald */
public class ArrayOfIdsIterablePosting extends IterablePostingImpl {

	/** ids of each of the entries in this posting list */
	protected int[] ids;
	/** where we are in the current posting list */
	protected int indice = -1;
	
	/** Make a new IterablePosting using these ids */
	public ArrayOfIdsIterablePosting(int[] _ids)
	{
		ids = _ids;
	}
	
	/** {@inheritDoc} */
	public int next() throws IOException {
		if (indice == ids.length -1)
			return EOL;
		return ids[++indice];
	}
	
	/** {@inheritDoc} */
	public boolean endOfPostings() {
		return (indice == ids.length -1);
	}

	/** {@inheritDoc} */
	public WritablePosting asWritablePosting() {
		return new BasicPostingImpl(ids[indice], 0);
	}
	
	/** {@inheritDoc} */
	public int getId()
	{
		return ids[indice];
	}

	/** {@inheritDoc} Returns 0. */
	public int getDocumentLength() {return 0;}
	/** 
	 * {@inheritDoc} 
	 */
	public int getFrequency() {return 0;}
	/** 
	 * {@inheritDoc} 
	 */
	public void setId(int id) {}
	/** 
	 * {@inheritDoc} 
	 */
	public void close() throws IOException {}

}
