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
 * The Original Code is SortAscendingPairedVectors.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Gianni Amati (original author)
 *  Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */  
package org.terrier.sorting;
/** 
 * This class sorts a pair of arrays, where the corresponding entries
 * are related. The result is that the first array is sorted in ascending
 * order, and the second is transformed in a way that the 
 * corresponding entries are in the correct places.
 * @author Gianni Amati, Vassilis Plachouras
  */
public class SortAscendingPairedVectors {
	
	/**
	 * Quick sort algorithm.
	 * @param a
	 * @param u
	 * @param lo0
	 * @param hi0
	 */
    private static void quickSort(int a[], int u[], int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;
        double mid;
		int dummy;
        if (hi0 > lo0) {
            mid = a[(lo0 + hi)>>>1];
            while (lo <= hi) {
                while ((lo < hi0) && (a[lo] < mid))
                    ++lo;
                while ((hi > lo0) && (a[hi] > mid))
                    --hi;
                if (lo <= hi) {
                    //swapping two elements
                	//swap(a, u, lo, hi);
                    dummy = a[lo];
                    a[lo] = a[hi];
                    a[hi] = dummy;
                    dummy = u[lo];
                    u[lo] = u[hi];
                    u[hi] = dummy;
                    //end of swapping
                    
                    ++lo;
                    --hi;
                }
            }
            if (lo0 < hi)
                quickSort(a, u, lo0, hi);
            if (lo < hi0)
                quickSort(a, u, lo, hi0);
        }
    }
    
    /**
     * Sorts the two vectors with respect to the
     * ascending order of the first one.
     * @param a the first vector to sort.
     * @param u the second vector to sort.
     */
    public static void sort(int[] a, int u[]) {
	    quickSort(a, u, 0, a.length - 1);
    }
}
