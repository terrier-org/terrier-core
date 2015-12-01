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
 * The Original Code is MultiSort.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Rodrygo Santos <rodrygo{a.}dcs.gla.ac.uk>
 */
package org.terrier.sorting;
/**
 * The current implementation is a simple extension of the existing HeapSort to
 * allow an int array to be used as the key array while sorting.
 * 
 * Ideally, this class should replace HeapSort to provide sorting of multiple
 * numeric arrays (not necessarily of a specific numeric type), and to allow
 * multiple keys to be used for sorting. Also, it might be interesting to
 * an alternative sorting algorithm, such as quick sort.
 * 
 * @author Rodrygo Santos
 */
public class MultiSort {
	/**
	 * Builds a maximum heap.
	 * @param A int[] the array which will be transformed into a heap.
	 * @param B int[] the array which will be transformed into a heap,
	 *		based on the values of the first argument.
	 * @param C short[] an additional array to sort by A.
	 */
	private static int buildMaxHeap(double[] A, int[] B, short[] C) {
		final int heapSize = A.length;
		for (int i = heapSize/2; i > 0; i--)
			maxHeapify(A, B, C, i, heapSize);
		return heapSize;
	}
	
	private static int buildMaxHeap(int[] A, double[] B, short[] C) {
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
	 * @param C short[] an additional array to sort by A.
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
	 * Sorts the given arrays in ascending order, using heap-sort.
	 * @param A double[] the first array to be sorted.
	 * @param B int[] the second array to be sorted, according to the
	 *		values of the first array.
	 */
	public static final void ascendingHeapSort(Comparable<?>[] A, int[] B) {
		int heapSize = buildMaxHeap(A, B);

		//temporary variables for swaps
		Comparable<?> tmpDouble;
		int tmpInt;
		
		for (int i = A.length; i > 0; i--) {
			//swap elements in i-1 with 0
			tmpDouble = A[i - 1];
			A[i - 1] = A[0];
			A[0] = tmpDouble;

			tmpInt = B[i - 1];
			B[i - 1] = B[0];
			B[0] = tmpInt;

			heapSize--;
			maxHeapify(A, B, 1, heapSize);
		}
	}
	
	
	
	
	
	private static int buildMaxHeap(Comparable<?>[] A, int[] B) {
		final int heapSize = A.length;
		for (int i = heapSize/2; i > 0; i--)
			maxHeapify(A, B, i, heapSize);
		return heapSize;
	}

	@SuppressWarnings("unchecked")
	private static void maxHeapify(@SuppressWarnings("rawtypes") Comparable[] A, int[] B, int i,
			int heapSize) {
		final int l = 2 * i;
		final int r = 2 * i + 1;

		int largest = 
			(l <= heapSize && A[l - 1].compareTo(A[i - 1]) > 0)
				? l
				: i;
		//if (l <= heapSize && A[l - 1] > A[i - 1])
		//	largest = l;
		//else
		//	largest = i;
		if (r <= heapSize &&  A[r - 1].compareTo(A[largest - 1]) > 0)
			largest = r;

		//temporary variables for swaps
		Comparable<?> tmpDouble;
		int tmpInt;
		

		if (largest != i) {
			tmpDouble = A[largest - 1];
			A[largest - 1] = A[i - 1];
			A[i - 1] = tmpDouble;

			tmpInt = B[largest - 1];
			B[largest - 1] = B[i - 1];
			B[i - 1] = tmpInt;

			maxHeapify(A, B, largest, heapSize);
		}
	}

	/**
	 * Sorts the given arrays in descending order, using heap-sort.
	 * @param A double[] the first array to be sorted.
	 * @param B int[] the second array to be sorted, according to the
	 *		values of the first array.
	 * @param C short[] an additional array to sort by A.
	 */
	public static final void descendingHeapSort(double[] A, int[] B, short[] C) {
		MultiSort.ascendingHeapSort(A, B, C);
		reverse(A, B, C, A.length);
	}
	/**
	 * Sorts the top <tt>topElements</tt> of the given array in
	 * ascending order, using heap sort.
	 * @param A double[] the first array to be sorted.
	 * @param B int[] the second array to be sorted, according to the
	 *		values of the first array.
	 * @param C short[] an additional array to sort by A.
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
	
	/** ascendingHeapSort
	 */
	public static final void ascendingHeapSort(int[] A, double[] B, short[] C, int topElements) {
		int heapSize = buildMaxHeap(A, B, C);
		int end = A.length - topElements;

		//temporary variables for swaps
		double tmpDouble;
		int tmpInt;
		short tmpShort;


		for (int i = A.length; i > end; i--) {
			//swap elements in i-1 with 0
			tmpInt = A[i - 1];
			A[i - 1] = A[0];
			A[0] = tmpInt;

			tmpDouble = B[i - 1];
			B[i - 1] = B[0];
			B[0] = tmpDouble;

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
	 * @param C short[] an additional array to sort by A.
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
	 * @param C short[] an additional array to sort by A.
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
	
	private static void maxHeapify(final int[] A, final double[] B, final short[] C, final int i, final int heapSize) {
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
			tmpInt = A[largest - 1];
			A[largest - 1] = A[i - 1];
			A[i - 1] = tmpInt;
			tmpDouble = B[largest - 1];
			B[largest - 1] = B[i - 1];
			B[i - 1] = tmpDouble;
			tmpShort = C[largest -1];
			C[largest -1] = C[i - 1];
			C[i - 1] = tmpShort;
			maxHeapify(A, B, C, largest, heapSize);
		}
	}
}
