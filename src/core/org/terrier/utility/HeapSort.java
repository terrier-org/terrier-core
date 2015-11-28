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
 * The Original Code is HeapSort.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.utility;
/**
 * An implementation of the heap sort algorithm as described in Cormen et al. 
 * Introduction to Algorithms. This class sorts two arrays, with respect to the
 * values of the elements in the first array. There is also an option to sort
 * only N entries with the highest values. In this case, there are two options:
 * <ul>
 * <li>The top N entries can be sorted in ascending order, so that the 
 * N elements with the highest values are placed in the array's last 
 * N positions (the highest entry of the arrays will be in 
 * array[array.length-1], the second highest in array[array.length-2] and 
 * so on.</li> 
 * <li>The top N entries can be sorted in descending order, so that 
 * the N elements with the highest values are placed in the array's first
 * N positions (the highest entry of the array will be in array[0], the second
 * highest entry will be in array[1] and so on.</li>
 * </ul>
 * 
 * @author Vassilis Plachouras
  */
public class HeapSort {
	/**
	 * Builds a maximum heap.
	 * @param A int[] the array which will be transformed into a heap.
	 * @param B int[] the array which will be transformed into a heap,
	 *		based on the values of the first argument.
	 */
	private static int buildMaxHeap(double[] A, int[] B, short[] C) {
		final int heapSize = A.length;
		for (int i = heapSize/2; i > 0; i--)
			maxHeapify(A, B, C, i, heapSize);
		return heapSize;
	}
	/**
	 * Sorts the given arrays in ascending order, using heap-sort.
	 * @param A double[] the first array to be sorted.
	 * @param B int[] the second array to be sorted, according to the
	 *		values of the first array.
	 */
	public static final void ascendingHeapSort(double[] A, int[] B, short[] C) {
		int heapSize = buildMaxHeap(A, B, C);

		//temporary variables for swaps
		double tmpDouble;
		int tmpInt;
		short tmpShort;

		for (int i = A.length; i > 0; i--) {
			//swap elements in i-1 with 0
			tmpDouble = A[i - 1];
			A[i - 1] = A[0];
			A[0] = tmpDouble;

			tmpInt = B[i - 1];
			B[i - 1] = B[0];
			B[0] = tmpInt;

			tmpShort = C[i - 1];
			C[i - 1] = C[0];
			C[0] = tmpShort;

			heapSize--;
			maxHeapify(A, B, C, 1, heapSize);
		}
	}
	/**
	 * Sorts the given arrays in descending order, using heap-sort.
	 * @param A double[] the first array to be sorted.
	 * @param B int[] the second array to be sorted, according to the
	 *		values of the first array.
	 */
	public static final void descendingHeapSort(double[] A, int[] B, short[] C) {
		HeapSort.ascendingHeapSort(A, B, C);
		reverse(A, B, C, A.length);
	}
	/**
	 * Sorts the top <tt>topElements</tt> of the given array in
	 * ascending order, using heap sort.
	 * @param A double[] the first array to be sorted.
	 * @param B int[] the second array to be sorted, according to the
	 *		values of the first array.
	 * @param topElements int the number of elements to be sorted.
	 */
	public static final void ascendingHeapSort(double[] A, int[] B, short[] C, int topElements) {
		int heapSize = buildMaxHeap(A, B, C);
		int end = A.length - topElements;

		//temporary variables for swaps
		double tmpDouble;
		int tmpInt;
		short tmpShort;


		for (int i = A.length; i > end; i--) {
			//swap elements in i-1 with 0
			tmpDouble = A[i - 1];
			A[i - 1] = A[0];
			A[0] = tmpDouble;

			tmpInt = B[i - 1];
			B[i - 1] = B[0];
			B[0] = tmpInt;

			tmpShort = C[i - 1];
			C[i - 1] = C[0];
			C[0] = tmpShort;

			heapSize--;
			maxHeapify(A, B, C, 1, heapSize);
		}
	}
	/**
	 * Reverses the elements of the two arrays, after they have
	 * been sorted.
	 * @param A double[] the first array.
	 * @param B int[] the second array.
	 * @param topElements int the number of elements to be reversed.
	 */
	private static void reverse(final double[] A, final int[] B, final short[] C, final int topElements) {
		//reversing the top elements
		final int length = A.length;
		final int elems = //topElements
			topElements > length/2 
			? length/2 
			: topElements;
		//if (elems > A.length/2)
		//	elems = A.length/2;


		int j;
		//temporary swap variables
		double tmpDouble;
		int tmpInt;
		short tmpShort;

		for (int i=0; i<elems; i++) {
			j = length - i - 1;
			//swap elements in i with those in j
			tmpDouble = A[i]; A[i] = A[j]; A[j] = tmpDouble;
			tmpInt = B[i]; B[i] = B[j]; B[j] = tmpInt;
			tmpShort = C[i]; C[i] = C[j]; C[j] = tmpShort;
		}
	}
	/**
	 * Sorts the top <tt>topElements</tt> of the given array in
	 * descending order, using heap sort for sorting the values
	 * in ascending order and then reversing the order of a
	 * specified number of elements.
	 * @param A double[] the first array to be sorted.
	 * @param B int[] the second array to be sorted, according to the
	 *		values of the first array.
	 * @param topElements int the number of elements to be sorted.
	 */
	public static final void descendingHeapSort(final double[] A, final int[] B, final short[] C, final int topElements) {
		ascendingHeapSort(A, B, C, topElements);
		reverse(A, B, C, topElements);
	}
	/**
	 * Maintains the heap property.
	 * @param A int[] The array on which we operate.
	 * @param i int a position in the array. This number is
	 * between 1 and A.length inclusive.
	 */
	private static void maxHeapify(final double[] A, final int[] B, final short[] C, final int i, final int heapSize) {
		final int l = 2 * i;
		final int r = 2 * i + 1;

		int largest = 
			(l <= heapSize && A[l - 1] > A[i - 1])
				? l
				: i;
		//if (l <= heapSize && A[l - 1] > A[i - 1])
		//	largest = l;
		//else
		//	largest = i;
		if (r <= heapSize && A[r - 1] > A[largest - 1])
			largest = r;

		//temporary variables for swaps
		double tmpDouble;
		int tmpInt;
		short tmpShort;


		if (largest != i) {
			tmpDouble = A[largest - 1];
			A[largest - 1] = A[i - 1];
			A[i - 1] = tmpDouble;
			tmpInt = B[largest - 1];
			B[largest - 1] = B[i - 1];
			B[i - 1] = tmpInt;
			tmpShort = C[largest -1];
			C[largest -1] = C[i - 1];
			C[i - 1] = tmpShort;
			maxHeapify(A, B, C, largest, heapSize);
		}
	}
}
