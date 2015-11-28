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
 * The Original is in 'ANDIterablePosting.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.structures.postings;

import java.io.IOException;
import java.util.Arrays;

import org.terrier.structures.Pointer;

/**
 * An instance of IterablePostings that works with passed arrays of ids and frequencies.
 * The document must contain all of the terms in the query to be matched (AND)
 * @author Richard McCreadie
 *
 */
public class ANDIterablePosting extends IterablePostingImpl {

	protected int currentId;
	protected IterablePosting[] ips;
	protected final int termCount;
	protected int frequency = 0;

	public ANDIterablePosting(IterablePosting[] _ips, Pointer[] _p) throws IOException {
		termCount = _ips.length;
		class PL implements Comparable<PL>
		{
			IterablePosting ip;
			Pointer p;
			@Override
			public int compareTo(PL other) 
			{	
				return other.p.getNumberOfEntries() - this.p.getNumberOfEntries();
			}
		}			
		assert _ips.length == _p.length;
		
		final PL[] lists = new PL[termCount];
		for(int i=0;i<termCount;i++)
		{
			lists[i] = new PL();
			assert _ips[i] != null;
			lists[i].ip = _ips[i];
			assert _p[i] != null;
			lists[i].p = _p[i];
		}
		
		Arrays.sort(lists);			
		
		ips = new IterablePosting[termCount];
		int i=0;
		for(PL list : lists)
		{
			ips[i] = list.ip;
			if (i != 0)
				ips[i].next();
			i++;
		}
	}

	@Override
	public int getId() {
		return currentId;
	}

	@Override
	public int getFrequency() {
		return frequency;
	}

	@Override
	public int getDocumentLength() {
		return ips[0].getDocumentLength();
	}

	@Override
	public void setId(int id) {
		currentId = id;
	}

	@Override
	public int next() throws IOException {
		
		ITERATION: do
		{
			int targetID = ips[0].next();
			if (targetID == EOL)
				return EOL;
			for(int i=1;i<ips.length;i++)
			{
				int foundID = ips[i].getId();
				if (foundID < targetID)
					foundID = ips[i].next(targetID);
				if (foundID > targetID)
					continue ITERATION;
				assert foundID == targetID;
			}
			
			if (calculateFrequency())
			{
				currentId = targetID;
				return targetID;
			}
			
		}while(true);
		
	}
	
	/** returns true if the document matches */
	protected boolean calculateFrequency()
	{
		frequency = 1;
		return true;
	}

	@Override
	public boolean endOfPostings() {
		return ips[0].endOfPostings();
	}

	@Override
	public void close() throws IOException {}

	@Override
	public WritablePosting asWritablePosting() {
		return new BasicPostingImpl(currentId, frequency);
	}

}