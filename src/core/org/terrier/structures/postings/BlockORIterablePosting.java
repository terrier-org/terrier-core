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
 * The Original Code is BlockORIterablePosting.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.postings;

import gnu.trove.TIntHashSet;

import java.io.IOException;
import java.util.Arrays;


/** ORIterablePosting implementation that implements BlockPosting
 * @since 3.5
 * @author Craig Macdonald
 */
public class BlockORIterablePosting extends ORIterablePosting implements
		BlockPosting {

	TIntHashSet positions = new TIntHashSet();
	/** 
	 * Construct an intance of the BlockORIterablePosting.
	 * @param ips
	 * @throws IOException
	 */
	public BlockORIterablePosting(IterablePosting[] ips) throws IOException {
		super(ips);		
	}

	@Override
	public int[] getPositions() {
		int[] rtr = positions.toArray();
		Arrays.sort(rtr);
		return rtr;
	}

	@Override
	protected void addPosting(Posting p) {
		super.addPosting(p);
		positions.addAll(((BlockPosting)p).getPositions());
	}

	@Override
	protected void firstPosting(Posting p) {
		super.firstPosting(p);
		if (positions.size() > 30)
		{
			positions.clear();
			positions.compact();
		}
		else
		{
			positions.clear();
		}
		positions.addAll(((BlockPosting)p).getPositions());
	}

	@Override
	public WritablePosting asWritablePosting() {
		return new BlockPostingImpl(this.getId(), this.getFrequency(), this.getPositions());
	}

	
	

}
