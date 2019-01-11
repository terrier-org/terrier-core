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
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.postings.bit;

import java.io.IOException;


/**
 * The most basic implementation of an iterable posting, written for postings with
 * docid and TF only. Docids are assumed d-gapped and encoded with gamma, while TF are 
 * encoded with unary.
 * 
 * @author Nicola Tonellotto
 */
import org.terrier.compression.bit.BitIn;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.WritablePosting;

@SuppressWarnings("serial")
public class BasicIterablePosting extends BasicPostingImpl implements IterablePosting
{
	protected int numEntries;
	
	protected BitIn bitFileReader;
	protected DocumentIndex doi;
	
	/**
	 * Empty constructor used ONLY for reflection
	 */
	public BasicIterablePosting()
	{
		this.doi = null;
	}
	
	/**
	 * Constructor
	 * 
	 * @param _bitFileReader			The bit file where we read the postings from
	 * @param _numEntries				Total number of postings to read before returning EOL
	 * @param _doi						The document index to get the doc length of the current docid
	 * @throws IOException
	 */
	public BasicIterablePosting(BitIn _bitFileReader, int _numEntries, DocumentIndex _doi) throws IOException 
	{
		bitFileReader = _bitFileReader;
		doi = _doi;
		numEntries = _numEntries;
	}

	@Override
	public boolean endOfPostings()
	{
		return (numEntries <= 0);
	}
	
	@Override
	public int getDocumentLength()
	{
		try {
			return doi.getDocumentLength(id);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			throw new RuntimeException("Problem looking for doclength for document "+ id + " -- docid out of bounds, possible (concurrent?) decompression error");
		} catch (Exception e) {
			throw new RuntimeException("Unknown problem looking for doclength for document "+ id, e);
		}
	}
	
	@Override
	public void close() throws IOException 
	{
		//// does not close the underlying file, just the read buffer
		bitFileReader.close();
	}
	
	@Override
	public WritablePosting asWritablePosting() 
	{
		BasicPostingImpl bp = new BasicPostingImpl(id, tf);
		return bp;
	}	
	
	@Override
	public String toString()
	{
		return "ID(" + id + ") TF(" + tf + ")";
	}

	@Override
	public int next() throws IOException 
	{
	    if (numEntries == 0) {
			id = END_OF_LIST;
		} else {
			id += bitFileReader.readGamma();
			this.tf = bitFileReader.readUnary();
			numEntries--;
		}
		return id;
	}
	
	@Override
	public int next(int target) throws IOException
	{
	    while (id < target)
	        if (numEntries > 0)
	            next();
	        else
	            id = END_OF_LIST;
	    return id;
	}
}
