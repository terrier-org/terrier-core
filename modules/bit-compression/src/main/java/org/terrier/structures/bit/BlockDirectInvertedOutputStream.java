/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.uk
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
 * The Original Code is BlockDirectInvertedOutputStream.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.bit;

import java.io.IOException;
import java.io.OutputStream;

import org.terrier.compression.bit.BitOut;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.SimpleBitIndexPointer;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.structures.postings.bit.BlockIterablePosting;

/** Writes a block direct or block inverted index, when passed appropriate posting lists.
  * @author Craig Macdonald
  * @since 2.0
  */
public class BlockDirectInvertedOutputStream extends DirectInvertedOutputStream {

	/** Creates a new output stream, writing a BitOutputStream to the specified file. The number of binary bits
	  * for fields must also be specified.
	  * @param filename Location of the file to write to
	  */
	public BlockDirectInvertedOutputStream(String filename) throws IOException
	{
		super(filename);
	}
	/** Creates a new output stream, writing to the specified BitOut implementation.  The number of binary bits
	  * for fields must also be specified.
	  * @param out BitOut implementation to write the file to 
	  */
	public BlockDirectInvertedOutputStream(BitOut out)
	{
		super(out);
	}
	
	public BlockDirectInvertedOutputStream(OutputStream os) throws IOException {
		super(os);
	}
	
	@Override
	public Class<? extends IterablePosting> getPostingIteratorClass()
	{
		return BlockIterablePosting.class;
	}
	
	@Override
	protected void writePostingNotDocid(Posting _p) throws IOException
	{
		BlockPosting p = (BlockPosting)_p;
		output.writeUnary(p.getFrequency());
		final int positions[] = p.getPositions();
		final int l = positions.length;
		output.writeUnary(l+1);
		if (l== 0)
			return;
		output.writeGamma(positions[0]+1);
		for(int i=1;i<l;i++)
		{
			output.writeGamma(positions[i] - positions[i-1]);
		}
	}
	

}
