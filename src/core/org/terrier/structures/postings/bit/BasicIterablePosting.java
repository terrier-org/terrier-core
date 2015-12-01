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
 * The Original Code is BasicIterablePosting.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */

package org.terrier.structures.postings.bit;

import java.io.IOException;


import org.terrier.compression.bit.BitIn;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.WritablePosting;

/** Basic inverted and direct index format: [gamma(first docid +1) unary (frequency)], [gamma(delta docid) unary(frequency)]
 * @since 3.0 
 */
@SuppressWarnings("serial")
public class BasicIterablePosting extends BasicPostingImpl implements IterablePosting
{
	protected int numEntries;
	protected BitIn bitFileReader;
	protected DocumentIndex doi;
	
	/** Create a new posting iterator */
	protected BasicIterablePosting(){}
	
	/** Create a new posting iterator
	 * @param _bitFileReader BitIn to read the postings from
	 * @param _numEntries number of postings in the list
	 * @param _doi document index to use to satisfy getDocumentLength()
	 * @throws IOException thrown in an IO exception occurs
	 */
	public BasicIterablePosting(BitIn _bitFileReader, int _numEntries, DocumentIndex _doi) throws IOException {
		bitFileReader = _bitFileReader;
		numEntries = _numEntries;
		doi = _doi;
	}

	/** {@inheritDoc} */
	public int next() throws IOException {
		if (numEntries-- <= 0)
			return EOL;
		id = bitFileReader.readGamma() + id;
		tf = bitFileReader.readUnary();
		return id;
	}
	
	/** {@inheritDoc}
	 * This implementation of next(int) which uses next() */
	public int next(int target) throws IOException {
		do
		{
			if (this.next() == EOL)
				return EOL;
		} while(this.getId() < target);
		return this.getId();
	}
	
	/** {@inheritDoc} */
	public boolean endOfPostings() {
		return numEntries <= 0;
	}

	/** {@inheritDoc} */
	public int getDocumentLength()
	{
		try{
			return doi.getDocumentLength(id);
		} catch (Exception e) {
			//TODO log?
			System.err.println("Problem looking for doclength for document "+ id);
			e.printStackTrace();
			return -1;
		}
	}

	/** {@inheritDoc} */
	public void close() throws IOException {
		//does not close the underlying file, just the read buffer
		bitFileReader.close();
	}
	
	/** {@inheritDoc} */
	public WritablePosting asWritablePosting() {
		BasicPostingImpl bp = new BasicPostingImpl(id, tf);
		return bp;
	}

	
}