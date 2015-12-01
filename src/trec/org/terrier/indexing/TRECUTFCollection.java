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
 * The Original Code is TRECUTFCollection.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */

package org.terrier.indexing;

import java.io.InputStream;

/** 
  * @since 1.1.0
    * @author Craig Macdonald
  * @see org.terrier.indexing.TRECCollection
  */
@Deprecated
public class TRECUTFCollection extends TRECCollection
{
	
	/** Instantiate a new TRECUTFCollection. Calls parent default constructor of TRECCollection */
	public TRECUTFCollection()
	{
		super();
		throw new UnsupportedOperationException("TRECUTFCollection is deprecated. Use TRECCollection and the tokeniser property instead");
	}

	/** Instantiate a new TRECUTFCollection. Calls parent with inputstream constructor of TRECCollection. */	
	public TRECUTFCollection(InputStream input) {
		super(input);
		throw new UnsupportedOperationException("TRECUTFCollection is deprecated. Use TRECCollection and the tokeniser property instead");
	}
	
	/** Instantiate a new TRECUTFCollection. Calls parent 4 String constructor of TRECCollection */
	public TRECUTFCollection(String CollectionSpecFilename, 
		String TagSet, 
		String BlacklistSpecFilename,
		String docPointersFilename)
	{
		super(CollectionSpecFilename, TagSet, BlacklistSpecFilename, docPointersFilename);
		throw new UnsupportedOperationException("TRECUTFCollection is deprecated. Use TRECCollection and the tokeniser property instead");
	}

}
