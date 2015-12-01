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
 * The Original Code is ArrayOfBasicIterablePosting.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.postings;

/** An instance of IterablePostings that works with passed arrays of ids and frequencies
 * @author Craig Macdonald */
public class ArrayOfBasicIterablePosting extends ArrayOfIdsIterablePosting {
	/** frequencies of all of the stored postings */
	protected int[] frequencies;
	protected int[] doclens = null;
	
	/** Make a new posting list with these ids and frequencies */
	public ArrayOfBasicIterablePosting(int[] _ids, int[] _freqs) 
	{
		super(_ids);
		this.frequencies = _freqs;
	}
	
	public ArrayOfBasicIterablePosting(int[] _ids, int[] _freqs, int[] _lens) 
	{
		super(_ids);
		this.frequencies = _freqs;
		this.doclens = _lens;
	}

	/** {@inheritDoc} */
	@Override
	public int getFrequency() {
		return this.frequencies[indice];
	}

	@Override
	public int getDocumentLength() {
		if (doclens != null)
			return doclens[indice];
		return 0;
	}
	
	

}
