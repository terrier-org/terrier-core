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
 * The Original Code is Distance.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   David Hannah <hannahd{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.utility;

import java.util.Arrays;


/**
 * Class containing useful utility methods for counting the number of occurrences 
 * of two terms within windows, etc.
 * @author David Hannah and Craig Macdonald
 * @since 3.0
 */
public class Distance{
	
	/** Counts number of blocks where two terms occur within a block of windowSize in length, in a document of length documentLengthInTokens
	 * where the blocks for the terms are as given
	 * @param blocksOfTerm1 
	 * @param start1 The start index for the correct blockIds in blocksOfTerm1 
	 * @param end1 The end for the correct blockIds in blocksOfTerm1 
	 * @param blocksOfTerm2
	 * @param start2 The start index for the correct blockIds in blocksOfTerm2
	 * @param end2 The end index for the correct blockIds in blocksOfTerm2
	 * @param windowSize
	 * @param documentLengthInTokens
	 **/
	public static final int noTimes(final int[] blocksOfTerm1, int start1, int end1, final int[] blocksOfTerm2, int start2, int end2, final int windowSize, final int documentLengthInTokens){
		
		if( blocksOfTerm1 == null){
			return 0;
		}
		
		if(blocksOfTerm2 == null){
			return 0;
		}
		
		int numberOfNGrams = documentLengthInTokens< windowSize ? 1 :documentLengthInTokens - windowSize + 1;
		int count = 0;
		final int[] windows_for_term1 = new int[numberOfNGrams];
		final int[] windows_for_term2 = new int[numberOfNGrams];
		windowsForTerms(blocksOfTerm1, start1, end1, windowSize, numberOfNGrams, windows_for_term1);
		windowsForTerms(blocksOfTerm2, start2, end2, windowSize, numberOfNGrams, windows_for_term2);
		
		for(int i=0; i<numberOfNGrams; i++){
			if (windows_for_term1[i] > 0 && windows_for_term2[i] > 0)
				count++;
		}
		return count;
	}
	
	/** Counts number of blocks where two terms occur within a block of windowSize in length, in a document of length documentLengthInTokens
	 * where the blocks for the terms are as given
	 * @param blocksOfTerm1 
	 * @param blocksOfTerm2
	 * @param windowSize
	 * @param documentLengthInTokens
	 **/
	public static final int noTimes(final int[] blocksOfTerm1, final int[] blocksOfTerm2, final int windowSize, final int documentLengthInTokens)
	{		
		if( blocksOfTerm1 == null){
			return 0;
		}
		
		if(blocksOfTerm2 == null){
			return 0;
		}
		if (blocksOfTerm1.length == 0 || blocksOfTerm2.length == 0)
			return 0;
		
		int numberOfNGrams = documentLengthInTokens< windowSize ? 1 :documentLengthInTokens - windowSize + 1;
		int count = 0;
		final int[] windows_for_term1 = new int[numberOfNGrams];
		final int[] windows_for_term2 = new int[numberOfNGrams];
		windowsForTerms(blocksOfTerm1, windowSize, numberOfNGrams, windows_for_term1);
		windowsForTerms(blocksOfTerm2, windowSize, numberOfNGrams, windows_for_term2);
		
		for(int i=0; i<numberOfNGrams; i++){
			if (windows_for_term1[i] > 0 && windows_for_term2[i] > 0)
				count++;
		}
		return count;
	}
	
	/** Counts number of blocks where all given terms occur within a block of windowSize in length, in a document of length documentLengthInTokens
	 * where the blocks for the terms are as given
	 * @param blocksForEachTerm - array of int[] of blocks for each term
	 * @param windowSize
	 * @param documentLengthInTokens
	 **/
	public static final int noTimes(int[][] blocksForEachTerm, final int windowSize, final int documentLengthInTokens)
	{
		final int termCount = blocksForEachTerm.length;
		final int numberOfNGrams = documentLengthInTokens< windowSize ? 1 :documentLengthInTokens - windowSize + 1;
		int count = 0;
		
		final int[][] windows_for_terms = new int[termCount][numberOfNGrams];
		//get occurrence windows for each term
		for(int t=0;t<termCount;t++)
		{
			windowsForTerms(blocksForEachTerm[t], windowSize, numberOfNGrams, windows_for_terms[t]);
		}
		//check each window for having all terms
		for(int i=0; i<numberOfNGrams; i++)
		{
			boolean yes = true;
			for(int t=0;t<termCount;t++)
			{
				if (windows_for_terms[t][i] == 0)
				{
					yes = false;
				}
			}
			if (yes)
				count++;
		}
		return count;
	}
	
	/** Sets the number of occurrences of a term in each window, given the specified window size, the number of n-grams in the document,
	 * and the blocks of the term. To control how much of array is examined, see windowsForTerms(int[], int, int, int, int, int[]).
	 * @param blocksOfTerm - block occurrences for term
	 * @param start - start index to consider in blocksOfTerm
	 * @param end - end index to consider in blocksOfTerm
	 * @param windowSize - size of each window
	 * @param numberOfNGrams - number of windows in document 
	 * @param windows_for_term - array of length numberOfNGrams
	 */
	public static final void windowsForTerms(int[] blocksOfTerm, int start, int end, int windowSize, int numberOfNGrams, int[] windows_for_term)
	{		
		//for each block
		for(int i=start; i<end; i++){
			final int a = blocksOfTerm[i];
			int j;
			if( a - windowSize+1 < 0)
				j = 0;
			else
				j = a-windowSize+1;
			//for each window matching that block
			for(; j<=a && j< numberOfNGrams; j++){
				windows_for_term[j] = 1;
			}
		}
	}
	
	/** Sets the number of occurrences of a term in each window, given the specified window size, the number of n-grams in the document,
	 * and the blocks of the term. To control how much of array is examined, see windowsForTerms(int[], int, int, int, int, int[]).
	 * @param blocksOfTerm - block occurrences for term
	 * @param windowSize - size of each window
	 * @param numberOfNGrams - number of windows in document 
	 * @param windows_for_term - array of length numberOfNGrams
	 */
	public static final void windowsForTerms(int[] blocksOfTerm, int windowSize, int numberOfNGrams, int[] windows_for_term)
	{
		final int l = blocksOfTerm.length;	
		//for each block
		for(int i=0; i<l; i++){
			final int a = blocksOfTerm[i];
			int j;
			if( a - windowSize+1 < 0)
				j = 0;
			else
				j = a-windowSize+1;
			//for each window matching that block
			for(; j<=a && j< numberOfNGrams; j++){
				windows_for_term[j] = 1;
			}
		}
	}
	
	
	//window size 2:
	//0:0-1, 1:1-2
	//window size 3:
	//0:0-2, 1:1-3
	
//	/** Returns the number of windows that have the both terms occurring, in the order specified.
//	 * New version, implemented 10/6/2010 by craigm. */
//	public static final int noTimesSameOrder(final int[] term0Positions, final int[] term1Positions, final int windowSize, final int documentLengthInTokens)
//	{
//		if (term0Positions.length == 0 || term1Positions.length == 0)
//			return 0;
//		final int numberOfNGrams = documentLengthInTokens< windowSize ? 1 :documentLengthInTokens - windowSize + 1;
//		final boolean[] matchingWindows = new boolean[numberOfNGrams];
//		boolean any = false;
//		int pos0 = 0;
//		int pos1 = 0;
//		final int term0Length = term0Positions.length;
//		final int term1Length = term1Positions.length;
//		OUTER: for(int ngramPosition = 0; ngramPosition < numberOfNGrams; ngramPosition++)
//		{
//			//System.err.println("ngramPosition=" + ngramPosition);			
//			while(term0Positions[pos0] < ngramPosition)
//			{
//				pos0++;
//				if (pos0 == term0Length)
//					break OUTER;
//			}
//			
//			while(term1Positions[pos1] < ngramPosition)
//			{
//				pos1++;
//				if (pos1 == term1Length)
//					break OUTER;
//			}
//			//System.err.println("pos0="+pos0 + " posi="+term0Positions[pos0]+" l1="+term0Positions.length +" pos1=" +pos1 + " posi="+term1Positions[pos1]+" l2="+term1Positions.length);
//			//term1Positions[pos0] and term2Positions[pos1] are now no less than ngramPosition
//			while(term0Positions[pos0] <= ngramPosition + windowSize -1 && term1Positions[pos1] <= ngramPosition + windowSize -1)
//			{
//				//System.err.println("positions ("+term0Positions[pos0]+","+term1Positions[pos1]+") are in window " + ngramPosition);
//				//first and second terms are within window
//				if (term0Positions[pos0] < term1Positions[pos1])
//				{
//					//ordering is correct
//					any = true;
//					matchingWindows[ngramPosition]= true;
//					pos0++;
//					if (pos0 == term0Length)
//						break OUTER;
//				}
//				else
//				{
//					pos1++;
//					if (pos1 == term1Length)
//						break OUTER;
//				}
//			}
//		}
//		return any ? countTrue(matchingWindows) : 0;
//	}
	
	public static final int noTimesSameOrder(final int[] term0Positions, final int[] term1Positions, final int windowSize, final int documentLengthInTokens)
	{
		return noTimesSameOrder(term0Positions, 0, term0Positions.length, term1Positions, 0, term1Positions.length, windowSize, documentLengthInTokens);
	}
	
	public static final int noTimesSameOrder(final int[] posTerm1, int start1, int end1, final int[] posTerm2, int start2, int end2, final int windowSize, final int documentLength) 
	{
		assert(posTerm1 != posTerm2);
		if (posTerm1 == null || posTerm2 == null || windowSize < 2)
			return 0;
		
		int count = 0;
		int pos1 = start1, pos2 = start2;
				
		while (pos1 < end1 && posTerm1[pos1] + windowSize <= documentLength) {
			while (pos2 < end2 && posTerm2[pos2] - posTerm1[pos1] < 0)
				pos2++;
					
			if (pos2 == end2)
			break;
			
			assert(posTerm2[pos2] != posTerm1[pos1]);
			if (posTerm2[pos2] - posTerm1[pos1] < windowSize)
				count++;
					
			pos1++;
		}
		return count;
	}
	
	
	/** Returns the number of windows that have the both terms occurring, in the order specified.
	 * New version, implemented 10/6/2010 by craigm. */
	public static final int noTimesNEW(final int[] term0Positions, final int[] term1Positions, final int windowSize, final int documentLengthInTokens)
	{
		final int numberOfNGrams = documentLengthInTokens< windowSize ? 1 :documentLengthInTokens - windowSize + 1;
		final boolean[] matchingWindows = new boolean[numberOfNGrams];
		int pos0 = 0;
		int pos1 = 0;
		final int term0Length = term0Positions.length;
		final int term1Length = term1Positions.length;
		OUTER: for(int ngramPosition = 0; ngramPosition < numberOfNGrams; ngramPosition++)
		{
			//System.err.println("ngramPosition=" + ngramPosition);			
			while(term0Positions[pos0] < ngramPosition)
			{
				pos0++;
				if (pos0 == term0Length)
					break OUTER;
			}
			
			while(term1Positions[pos1] < ngramPosition)
			{
				pos1++;
				if (pos1 == term1Length)
					break OUTER;
			}
			//System.err.println("pos0="+pos0 + " posi="+term0Positions[pos0]+" l1="+term0Positions.length +" pos1=" +pos1 + " posi="+term1Positions[pos1]+" l2="+term1Positions.length);
			//term1Positions[pos0] and term2Positions[pos1] are now no less than ngramPosition
			while(term0Positions[pos0] <= ngramPosition + windowSize -1 && term1Positions[pos1] <= ngramPosition + windowSize -1)
			{
				//System.err.println("positions ("+term0Positions[pos0]+","+term1Positions[pos1]+") are in window " + ngramPosition);
				//first and second terms are within window
				matchingWindows[ngramPosition]= true;
				if (term0Positions[pos0] < term1Positions[pos1])
				{
					pos0++;
					if (pos0 == term0Length)
						break OUTER;
				}
				else
				{
					pos1++;
					if (pos1 == term1Length)
						break OUTER;
				}
			}
		}
		return countTrue(matchingWindows);
	}
	
	
	
//	/** Returns the number of windows that have the both terms occurring, in the order specified.
//	 * New version, implemented 10/6/2010 by craigm. */
//	public static final int noTimesSameOrder(
//			final int[] term0Positions, int pos0, final int length0, 
//			final int[] term1Positions, int pos1, final int length1, 
//			final int windowSize, final int documentLengthInTokens)
//	{
//		final int numberOfNGrams = documentLengthInTokens< windowSize ? 1 :documentLengthInTokens - windowSize + 1;
//		final boolean[] matchingWindows = new boolean[numberOfNGrams];
//		final int term0Length = pos0 + length0;
//		final int term1Length = pos1 + length1;
//		OUTER: for(int ngramPosition = 0; ngramPosition < numberOfNGrams; ngramPosition++)
//		{
//			//System.err.println("ngramPosition=" + ngramPosition);			
//			while(term0Positions[pos0] < ngramPosition)
//			{
//				pos0++;
//				if (pos0 == term0Length)
//					break OUTER;
//			}
//			
//			while(term1Positions[pos1] < ngramPosition)
//			{
//				pos1++;
//				if (pos1 == term1Length)
//					break OUTER;
//			}
//			//System.err.println("pos0="+pos0 + " posi="+term0Positions[pos0]+" l1="+term0Positions.length +" pos1=" +pos1 + " posi="+term1Positions[pos1]+" l2="+term1Positions.length);
//			//term1Positions[pos0] and term2Positions[pos1] are now no less than ngramPosition
//			while(term0Positions[pos0] <= ngramPosition + windowSize -1 && term1Positions[pos1] <= ngramPosition + windowSize -1)
//			{
//				//System.err.println("positions ("+term0Positions[pos0]+","+term1Positions[pos1]+") are in window " + ngramPosition);
//				//first and second terms are within window
//				if (term0Positions[pos0] < term1Positions[pos1])
//				{
//					//ordering is correct
//					matchingWindows[ngramPosition]= true;
//					pos0++;
//					if (pos0 == term0Length)
//						break OUTER;
//				}
//				else
//				{
//					pos1++;
//					if (pos1 == term1Length)
//						break OUTER;
//				}
//			}
//		}
//		return countTrue(matchingWindows);
//	}
	
	protected static final int countTrue(final boolean[] in) {
		int count = 0;
		for (boolean b : in)
		{
			if (b) count++;
		}
		return count;
	}
	
	/** number of blocks where terms occur in an ajdacent manner.
	 * dont use this method, it has no concept of windows
	 */
	@Deprecated
	public static final int noTimesSameOrder(final int[][] blocksOfAllTerms1, final int documentLengthInTokens)
	{
		final int numberOfTerms = blocksOfAllTerms1.length;
		int count = 0;
		final int[] positions = new int[numberOfTerms];	
		OUTER: for (positions[0]=0; positions[0]< blocksOfAllTerms1[0].length; positions[0]++)
		{	
			boolean OK = false;
			INNER: for(int term=1;term<numberOfTerms;term++)
			{
				if (positions[term] >= blocksOfAllTerms1[term].length)
				{
					OK = false;
					break INNER;
				}
				//System.err.printf("Comparing: %d %d\n", blocksOfAllTerms1[term-1][positions[term-1]], blocksOfAllTerms1[0][positions[0]]);
				
				while(blocksOfAllTerms1[term][positions[term]] < blocksOfAllTerms1[0][positions[0]])
				{
					positions[term]++;
					//System.err.println("moving term " + term + " to position " + positions[term] + " of "+ blocksOfAllTerms1[term].length);
					if (positions[term] >= blocksOfAllTerms1[term].length)
					{
						//System.err.printf("break %d >= %d == %b\n", positions[term], blocksOfAllTerms1[term].length, ());
						break OUTER;
					}
				}
				//System.err.printf("Comparing: %d %d\n", blocksOfAllTerms1[term-1][positions[term-1]], blocksOfAllTerms1[term][positions[term]]);
				
				if (blocksOfAllTerms1[term][positions[term]] == blocksOfAllTerms1[term-1][positions[term-1]] +1)
				{
					//System.err.printf("Match: %d %d\n", blocksOfAllTerms1[term-1][positions[term-1]], blocksOfAllTerms1[term][positions[term]]);
					OK = true;
				}
			}
			if (OK)
				count++;
		}
		return count;
	}
	
	
	/** number of blocks where */
	public static final int noTimesSameOrderOLD(final int[] blocksOfTerm1, final int[] blocksofTerm2, final int windowSize, final int documentLengthInTokens){
		
		if( blocksOfTerm1 == null){
			return 0;
		}
		
		if(blocksofTerm2 == null){
			return 0;
		}
		final int end1 = blocksOfTerm1.length;
		final int end2 = blocksofTerm2.length;
		
		final int numberOfNGrams = documentLengthInTokens< windowSize ? 1 :documentLengthInTokens - windowSize + 1;
		final boolean[] matchingWindows = new boolean[numberOfNGrams];
		
		for (int k1=0; k1<end1; k1++) {
			for (int k2=0; k2<end2; k2++) {
				if ( ( (blocksofTerm2[k2]-blocksOfTerm1[k1]<windowSize) && ( (blocksofTerm2[k2]-blocksOfTerm1[k1])>0) ) ) {
					final int len = blocksofTerm2.length;
					for(int i=0; i<len; i++){
						final int a = blocksofTerm2[i];
						int j;
						if( a - windowSize+1 < 0)
							j = 0;
						else
							j = a-windowSize+1;
						System.err.println("j="+j + " a="+a);
						//for each window matching that block
						for(; j<=a && j< numberOfNGrams; j++)
						{
							System.err.println("window " + j + " matches");
							matchingWindows[j] = true;
						}
					}		
				}
			}
		}
		
		int count = 0;
		
		for(int i=0; i<numberOfNGrams; i++){
			if(matchingWindows[i])
				count++;
		}
		return count;
	}
	
	/** Find smallest difference between two elements of two arrays */
	public static final int findSmallest(int[] x, int[] y){
		Arrays.sort(x);
		Arrays.sort(y);
		
		int i = 0;
		int j = 0;
		int smallest = -1;
		
		while(true){
			
			final int dif = Math.abs( ( x[i] - y[j] ) );
			
			if(smallest == -1){
				smallest = dif;
			}else if( dif < smallest ){
				smallest = dif;
			}else if( dif == 0){
				return 0;
			}
			
			if( x[i] < y[j] ){
				i++;
				if( i == x.length )
					return smallest;
			}else if( x[i] > y[j] ){
				j++;
				if( j == y.length )
					return smallest;
			}
		}
	}

}
