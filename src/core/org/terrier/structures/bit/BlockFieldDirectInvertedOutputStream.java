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
 * The Original Code is BlockFieldDirectInvertedOutputStream.java
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
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.structures.postings.bit.BlockFieldIterablePosting;
/** 
 * Writes out a blocks and fields direct index to an output stream
 */
public class BlockFieldDirectInvertedOutputStream extends
		FieldDirectInvertedOutputStream {

	/**
	 * Constructs an instance of the class with
	 * @param out
	 */
	public BlockFieldDirectInvertedOutputStream(BitOut out) {
		super(out);
	}

	/**
	 * Constructs an instance of the class with
	 * @param filename
	 * @throws IOException
	 */
	public BlockFieldDirectInvertedOutputStream(String filename)
			throws IOException {
		super(filename);
	}
	
	@Override
	public Class<? extends IterablePosting> getPostingIteratorClass()
	{
		return BlockFieldIterablePosting.class;
	}
	
	@Override
	protected void writePostingNotDocid(Posting _p) throws IOException
	{
		super.writePostingNotDocid(_p);
		final BlockPosting p = (BlockPosting)_p;
		final int positions[] = p.getPositions();
		final int l = positions.length;
		//System.err.println("posting has " + l + "blocks");
		output.writeUnary(l+1);
		if (l == 0)
			return;
		output.writeGamma(positions[0]+1);
		for(int i=1;i<l;i++)
		{
			output.writeGamma(positions[i] - positions[i-1]);
		}
	}

}
