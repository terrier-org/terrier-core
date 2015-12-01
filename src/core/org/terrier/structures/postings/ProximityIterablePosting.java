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
 * The Original is in 'ProximityIterablePosting.java'
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

import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.ANDIterablePosting;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.IterablePosting;

/** Implementation of an IterablePosting for block indices that returns
 * only documents that match phases within a fixed window size.
 * @since 3.6
 * @author Matteo Catena and Richard McCreadie
 */
public class ProximityIterablePosting extends ANDIterablePosting {

	protected int window;

	public ProximityIterablePosting(IterablePosting[] _ips, Pointer[] _p, int window)
			throws IOException {
		super(_ips, _p);
		this.window = window;
	}

	@Override
	protected boolean calculateFrequency() {

		//System.err.println("calculateFrequency");
		
		int smallestIndex = -1;
		int posSmall = Integer.MAX_VALUE;
		final int[][] pos = new int[ips.length][]; 
		for(int i=0;i<termCount;i++)
		{
			pos[i] = ((BlockPosting)ips[i]).getPositions();
			if (pos[i].length < posSmall)
			{
				posSmall = pos[i].length;
				smallestIndex = i;
			}
		}
		assert posSmall != Integer.MAX_VALUE;

		frequency = 0;


		int[] seed = pos[smallestIndex];		

		for(int posInSeed=0;posInSeed<seed.length;posInSeed++)
		{
			
			int targetPosition = seed[posInSeed];

			int[][] otherTermPos = new int[termCount-1][];

			int termIndex = 0;
			for(int i=0;i<termCount;i++)
			{
				if (i == smallestIndex)
					continue;

				//System.err.println(smallestIndex+" "+i+" ");
				
				otherTermPos[termIndex] = pos[i];

				termIndex++;

			}

			boolean isInWindow = isInWindow(otherTermPos, targetPosition);
			//System.err.println(posInSeed+" "+" "+isInWindow+" "+termCount);
			
			if (isInWindow)
			{
				frequency++;
				//System.err.println("Match");
			} else {
				//System.err.println("No Match");
			}
		}



		//System.err.println("frequency = "+ frequency);
		return frequency > 0;		
	}	



	private boolean isInWindow(int[][] otherTermPos, int i) {
		int diameter = window;
		
		// fix window length if it is too short to match anything
		if (diameter<otherTermPos.length+1) diameter=otherTermPos.length+1;

		// this is potentially a good match

		// slide the window to make the final determination
		int windowstart = i-diameter+1;
		int windowend = i+1;

		for (int j=0; j<diameter; j++) {
			boolean[] matched = new boolean[otherTermPos.length];
			int[] positions = new int[otherTermPos.length];
			Arrays.fill(matched, false);
			Arrays.fill(positions, i);
			//System.err.println("Reset");
			for (int termIndex = 0; termIndex<otherTermPos.length; termIndex++) {
				int[] pos = otherTermPos[termIndex];
				for (int k = 0; k < pos.length; k++) {
					//System.err.println("S: "+j+" "+termIndex+" "+k+" ["+pos[k]+" "+windowstart+" "+windowend+"]");
					if (pos[k]>=windowstart && pos[k]<windowend) {
						boolean duplicate = false;
						for (int p : positions) {
							if (p==pos[k] || pos[k]==i) {
								duplicate=true;
								break;
							}
						}
						if (duplicate) continue;
						positions[termIndex]=pos[k];
						matched[termIndex]=true;
						//System.err.println("   m");
						break;
					}
				}
			}
			boolean allMatch = true;
			for (boolean term : matched) {
				if (!term) {
					allMatch=false;
					break;
				}
			}

			if (allMatch) return true;

			windowstart=windowstart+1;
			windowend=windowend+1;
		}


		return false;
	}

	public static IterablePosting createProximityPostingList(Pointer[] ps,
			PostingIndex<Pointer> invIndex, int blockDistance) throws IOException {

		IterablePosting[] _ips = new IterablePosting[ps.length];
		for (int i = 0; i < ps.length; i++) _ips[i] = invIndex.getPostings(ps[i]);
		return new ProximityIterablePosting(_ips, ps, blockDistance);
	}

	public static IterablePosting createProximityPostingList(String[] terms, Lexicon<String> lex, PostingIndex<Pointer> invIndex, int blockDistance)
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
		return createProximityPostingList(ps, invIndex, blockDistance);
	}

}