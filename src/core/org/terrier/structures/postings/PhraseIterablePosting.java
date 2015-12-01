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
 * The Original is in 'PhraseIterablePosting.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.structures.postings;

import gnu.trove.TIntArrayList;

import java.io.IOException;
import java.util.Arrays;

import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;

/** Implementation of an IterablePosting for block indices that returns
 * only documents that match a multi-term phase.
 * @since 3.5
 * @author Craig Macdonald
 */
public class PhraseIterablePosting extends ANDIterablePosting implements BlockPosting 
{
	BlockPosting[] bs;
	int smallestIndex = -1;
	final TIntArrayList positions;
	
	public PhraseIterablePosting(IterablePosting[] _ips, Pointer[] _p, boolean recordPositions) throws IOException
	{
		super(_ips, _p);
		bs = new BlockPosting[termCount];
		int i=0;
		for(IterablePosting ip : ips)
		{
			bs[i] = (BlockPosting)ip;
			i++;
		}
		if (recordPositions)
		{
			positions = new TIntArrayList();
		}
		else
		{
			positions = null;
		}
	}
	
	
	@Override
	public WritablePosting asWritablePosting() {
		return new BlockPostingImpl(currentId, frequency, positions.toNativeArray());
	}
	
	@Override
	protected boolean calculateFrequency()
	{
		
		int smallestIndex = -1;
		int posSmall = Integer.MAX_VALUE;
		final int[][] pos = new int[bs.length][]; 
		for(int i=0;i<termCount;i++)
		{
			pos[i] = bs[i].getPositions();
			if (pos[i].length < posSmall)
			{
				posSmall = pos[i].length;
				smallestIndex = i;
			}
		}
		assert posSmall != Integer.MAX_VALUE;
		
		frequency = 0;
		
		
		int[] seed = pos[smallestIndex];			
		int[] position = new int[termCount];
		boolean[] found = new boolean[termCount];
		boolean[] end = new boolean[termCount];
		for(int posInSeed=0;posInSeed<seed.length;posInSeed++)
		{
			Arrays.fill(found, false);
			int targetPosition = seed[posInSeed];
			found[smallestIndex] = true;
			for(int i=0;i<termCount;i++)
			{
				if (i == smallestIndex)
					continue;
				final int thisTarget = /*i < smallestIndex*/ 
					/*?*/ targetPosition - (smallestIndex - i); 
					//: targetPosition + (i - smallestIndex);
				while(position[i] < pos[i].length && pos[i][position[i]] < thisTarget)
				{
					position[i]++;
				}
				if (position[i] == pos[i].length)
				{
					end[i] = true;
					break;
				}
				if (pos[i][position[i]] == thisTarget)
				{
					found[i] = true;
				}
			}
			if (allTrue(found))
			{
				frequency++;
				if (positions != null)
					positions.add(pos[0][position[0]]);
			}
			else if (anyTrue(end))
			{
				break;
			}
		}
		//System.err.println("frequency = "+ frequency);
		return frequency > 0;
	}
	
	static boolean allTrue(final boolean[] in) {
		for (boolean b : in)
		{
			if (! b)
				return false;
		}
		return true;
	}
	
	static boolean anyTrue(final boolean[] in) {
		for (boolean b : in)
		{
			if (b)
				return true;
		}
		return false;
	}
	
	@Override
	public int[] getPositions() {
		return positions != null ? positions.toNativeArray() : new int[0];
	}
	
	public static IterablePosting createPhrasePostingList(Pointer[] ps, PostingIndex<Pointer> invIndex, boolean savePositions)
			throws IOException
	{
		int phraseLength = ps.length;
		IterablePosting[] ips = new IterablePosting[phraseLength];
		for(int i=0;i<phraseLength;i++)
		{
			ips[i] = invIndex.getPostings(ps[i]);
		}
		return new PhraseIterablePosting(ips, ps, savePositions);
	}
	
	public static IterablePosting createPhrasePostingList(String[] terms, Lexicon<String> lex, PostingIndex<Pointer> invIndex, boolean savePositions)
		throws IOException
	{
		int phraseLength = terms.length;
		IterablePosting[] ips = new IterablePosting[phraseLength];
		Pointer[] ps = new Pointer[phraseLength];
		for(int i=0;i<phraseLength;i++)
		{
			LexiconEntry le = lex.getLexiconEntry(terms[i]);
			ips[i] = invIndex.getPostings(ps[i] = (Pointer) le);
		}
		return new PhraseIterablePosting(ips, ps, savePositions);
	}
}