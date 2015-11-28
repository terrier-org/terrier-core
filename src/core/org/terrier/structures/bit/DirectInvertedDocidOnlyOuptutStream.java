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
 * The Original Code is DirectInvertedDocidOnlyOuptutStream.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.bit;

import java.io.IOException;

import org.terrier.compression.bit.BitOut;
import org.terrier.structures.indexing.CompressionFactory.BitIdOnlyCompressionConfiguration;
import org.terrier.structures.postings.Posting;
/** A BitPosting writing class that doesn't write any frequency information 
 * @since 3.0
 * @author Craig Macdonald
 * @see BitIdOnlyCompressionConfiguration
 */
public class DirectInvertedDocidOnlyOuptutStream extends
		DirectInvertedOutputStream {

	/**
	 * Constructs an instance of the class with
	 * @param filename
	 * @throws IOException
	 */
	public DirectInvertedDocidOnlyOuptutStream(String filename)
			throws IOException {
		super(filename);
	}

	/**
	 * Constructs an instance of the class with
	 * @param out
	 */
	public DirectInvertedDocidOnlyOuptutStream(BitOut out) {
		super(out);
	}

	@Override
	protected void writePostingNotDocid(Posting p) throws IOException {}
	
	

}
