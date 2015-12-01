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
 * The Original Code is SortAscendingVectors.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> 
 */
package org.terrier.sorting;
/** 
 * This class sorts one or more arrays, based on sorting a key array, ie where 
 * the corresponding entries are related. The result is that the first array 
 * is sorted in ascending order, and the second is transformed in a way that the 
 * corresponding entries are in the correct places.
 * This class replaces SortAscending*Vectors.
 * @author Gianni Amati, Craig Macdonald, Vassilis Plachouras
  */
public class SortAscendingVectors {
	/**
	 * Quick sort algorithm. Sort u by sorting a.
	 * @param a Key vector
	 * @param u 2nd vector
	 * @param lo0 Upper range of sort
	 * @param hi0 Lower range of sort
	 */
    private static void quickSort2(int a[], int u[], int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;
		int dummy;
        double mid;
        if (hi0 > lo0) {
            mid = a[(lo0+hi0)>>>1];
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
                quickSort2(a, u, lo0, hi);
            if (lo < hi0)
                quickSort2(a, u, lo, hi0);
        }
    }
	/** Quicksort all vectors in u based on sorting a
     * @param a Key vector
     * @param u Array of vectors to sort based on a
     * @param lo0 Upper range of sort
     * @param hi0 Lower range of sort
     */
    private static void quickSortN(int a[], int u[][], int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;
        int dummy; int i;
        int uSize = u.length;
        double mid;
        if (hi0 > lo0) {
            mid = a[(lo0 + hi0) >>> 1];
            while (lo <= hi) {
                while ((lo < hi0) && (a[lo] < mid))
                    ++lo;
                while ((hi > lo0) && (a[hi] > mid))
                    --hi;
                if (lo <= hi) {
                    //swapping two elements, in key and other vectors
                    //swap(a, u[i], lo, hi);
                    dummy = a[lo];
                    a[lo] = a[hi];
                    a[hi] = dummy;
					//now sort for each array
                    for(i = 0; i< uSize; i++)
                    {
                        dummy = u[i][lo];
                        u[i][lo] = u[i][hi];
                        u[i][hi] = dummy;
                    }
                    //end of swapping
                    ++lo;
                    --hi;
                }
            }
            if (lo0 < hi)
                quickSortN(a, u, lo0, hi);
            if (lo < hi0)
                quickSortN(a, u, lo, hi0);
        }
    }
    /**
     * Sort the vectors contained in u with respect to the
     * ascending order of the vector a.
     * @param a the first vector to sort (the key vector)
     * @param u the vector of vectors to sort with respect to a
     */
    public static void sort(int[] a, int[][] u) {
        if (u.length == 1)
            quickSort2(a, u[0], 0, a.length - 1);
        else
            quickSortN(a, u, 0, a.length - 1);
    }
    
    /**
     * Sorts the two vectors with respect to the
     * ascending order of the first one.
     * @param a the first vector to sort.
     * @param u the second vector to sort.
     */
    public static void sort2(int[] a, int u[]) {
        quickSort2(a, u, 0, a.length - 1);
    }
}
