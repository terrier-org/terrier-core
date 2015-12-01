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
 * The Original Code is SortAscendingQuadrupleVectors.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Gianni Amati (original author)
 *  Douglas Johnson
 *  Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */  
package org.terrier.sorting;
/**
 * This class sorts four arrays, where the corresponding entries
 * are related. The result is that the first array is sorted in ascending
 * order, and the rest are transformed in a way that the 
 * corresponding entries are in the correct places
 * @author Douglas Johnson
  */
public class SortAscendingQuadrupleVectors {
	
	/**
	 * The quick sort algorithm
	 * @param a 
	 * @param u 
	 * @param e 
	 * @param o 
	 * @param lo0
	 * @param hi0 
	 */
	private static void quickSort(int a[], int u[], int e[], int o[], int lo0, int hi0) {
		int lo = lo0;
		int hi = hi0;
		int dummy;
		double mid, mid2;
		if (hi0 > lo0) {
			mid = a[(lo0 +hi0)>>>1 /*(lo0 + hi0) / 2*/];
			mid2 = u[(lo0 +hi0)>>>1 /*(lo0 + hi0) / 2*/];
			while (lo <= hi) {
				while ((lo < hi0)
					&& (a[lo] == mid)
					&& (u[lo] < mid2)
					|| (lo < hi0)
					&& (a[lo] < mid))
					++lo;
				while ((hi > lo0)
					&& (a[hi] == mid)
					&& (u[hi] > mid2)
					|| (hi > lo0)
					&& (a[hi] > mid))
					--hi;
				if (lo <= hi) {
					//start swapping 
					//swap(a, u, e, o, lo, hi);
					dummy = a[lo];
					a[lo] = a[hi];
					a[hi] = dummy;
					
					dummy = u[lo];
					u[lo] = u[hi];
					u[hi] = dummy;
					
					dummy = e[lo];
					e[lo] = e[hi];
					e[hi] = dummy;
					
					dummy = o[lo];
					o[lo] = o[hi];
					o[hi] = dummy;
					//end swapping
					
					++lo;
					--hi;
				}
			}
			if (lo0 < hi)
				quickSort(a, u, e, o, lo0, hi);
			if (lo < hi0)
				quickSort(a, u, e, o, lo, hi0);
		}
	}
    /**
	* Sorts the four vectors with respect to the
    * ascending order of the first one.
    * @param a the first vector to sort.
    * @param u the second vector to sort.
    * @param e the third vector to sort.
    * @param o the fourth vector to sort.
	*/
	public static void sort(int[] a, int u[], int e[], int o[]) {
		quickSort(a,u,e,o, 0, a.length - 1);
	}
}
