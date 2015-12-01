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
 * The Original Code is HeapSortInt.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.sorting;
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
public class HeapSortInt {

   	/**
	 * Builds a maximum heap.
	 * @param A int[] the array which will be transformed into a heap.
	 * @param B int[] an additional array to sort by A.
	 * @param C int[] an additional array to sort by A.
	 *		based on the values of the first argument.
	 */
	private static int buildMaxHeap(int[] A, int[] B, int[] C) {
		final int heapSize = A.length;
		for (int i = heapSize/2; i > 0; i--)
			maxHeapify(A, B, C, i, heapSize);
		return heapSize;
	}

    private static int buildMaxHeap(int[] A, int[] B, int[] C, int[] D) {
        final int heapSize = A.length;
        for (int i = heapSize/2; i > 0; i--)
            maxHeapify(A, B, C, D, i, heapSize);
        return heapSize;
    }

    private static int buildMaxHeap(int[][] A) {
        final int heapSize = A[0].length;
        for (int i = heapSize/2; i > 0; i--)
            maxHeapify(A, i, heapSize);
        return heapSize;
    }

   	/**
	 * Builds a maximum heap.
	 * @param A int[] the array which will be transformed into a heap.
	 * @param B int[] an additional array to sort by A.
	 *		based on the values of the first argument.
	 */
	private static int buildMaxHeap(int[] A, int[] B) {
		final int heapSize = A.length;
		for (int i = heapSize/2; i > 0; i--)
			maxHeapify(A, B, i, heapSize);
		return heapSize;
	}


	/**
	 * Sorts the given arrays in ascending order, using heap-sort.
	 * @param A int[] the first array to be sorted.
	 * @param B int[] the second array to be sorted, according to the
	 *		values of the first array.
	 * @param C int[] an additional array to sort by A.
	 */
	public static void ascendingHeapSort(int[] A, int[] B, int[] C) {
		int heapSize = buildMaxHeap(A, B, C);

		//temporary variables for swaps
		int tmpDouble;
		int tmpInt;
		int tmpShort;

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
	 * ascendingHeapSort
	 * @param A int[] the first array to be sorted.
	 * @param B int[] the second array to be sorted, according to the
	 *		values of the first array.
	 * @param C int[] an additional array to sort by A.
	 * @param D int[] an additional array to sort by A.
	 */
    public static void ascendingHeapSort(int[] A, int[] B, int[] C, int[] D) {
        int heapSize = buildMaxHeap(A, B, C, D);

        //temporary variables for swaps
        int tmpDouble;
        int tmpInt;
        int tmpShort;

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

			tmpShort = D[i - 1];
            D[i - 1] = D[0];
            D[0] = tmpShort;


            heapSize--;
            maxHeapify(A, B, C, D, 1, heapSize);
        }
    }

	/**
	 * Sorts the given arrays in ascending order, using heap-sort.
	 * @param A int[] the first array to be sorted.
	 * @param B int[] the second array to be sorted, according to the
	 *		values of the first array.
	 */
	public static void ascendingHeapSort(int[] A, int[] B) {
		int heapSize = buildMaxHeap(A, B);

		//temporary variables for swaps
		int tmpDouble;
		int tmpInt;
		//int tmpShort;

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
	
	/**
	 * Sorts the given arrays in descending order, using heap-sort.
	 * @param A int[] the first array to be sorted.
	 * @param B int[] the second array to be sorted, according to the
	 *		values of the first array.
	 */
	public static void descendingHeapSort(int[] A, int[] B) {
		ascendingHeapSort(A, B);
		reverse(A, B, A.length);
	}
	
	/** ascendingHeapSort
	 * 
	 * @param A an arry to sort ascending
	 */
	public static void ascendingHeapSort(int[][] A) {
		int heapSize = buildMaxHeap(A);

		//temporary variables for swaps
		
		int tmpInt;
		//int tmpShort;

		for (int i = A[0].length; i > 0; i--) {
			//swap elements in i-1 with 0
			for(int j=0;j<A.length;j++)
			{
				tmpInt = A[j][i-1];
				A[j][i-1] = A[j][0];
				A[j][0] = tmpInt;
			}

			heapSize--;
			maxHeapify(A, 1, heapSize);
		}
	}

	/**
	 * Sorts the given arrays in descending order, using heap-sort.
	 * @param A double[] the first array to be sorted.
	 * @param B int[] the second array to be sorted, according to the
	 * @param C int[] an additional array to sort by A.
	 *		values of the first array.
	 */
	public static void descendingHeapSort(int[] A, int[] B, int[] C) {
		HeapSortInt.ascendingHeapSort(A, B, C);
		reverse(A, B, C, A.length);
	}

	/**
	 * Sorts the given arrays in descending order, using heap-sort by the first array.
	 */
	 
    public static void descendingHeapSort(int[] A, int[] B, int[] C, int[] D) {
        HeapSortInt.ascendingHeapSort(A, B, C, D);
        reverse(A, B, C, D, A.length);
    }

	/**
	 * Sorts the top <tt>topElements</tt> of the given array in
	 * ascending order, using heap sort.
	 * @param A double[] the first array to be sorted.
	 * @param B int[] the second array to be sorted, according to the
	 *		values of the first array.
	 * @param C int[] an additional array to sort by A.
	 * @param topElements int the number of elements to be sorted.
	 */
	public static void ascendingHeapSort(int[] A, int[] B, int[] C, int topElements) {
		int heapSize = buildMaxHeap(A, B, C);
		int end = A.length - topElements;

		//temporary variables for swaps
		int tmpDouble;
		int tmpInt;
		int tmpShort;


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
	 * @param B int[] an additional array to sort by A.
	 * @param C int[] an additional array to sort by A.
	 * @param topElements int the number of elements to be reversed.
	 */
	private static void reverse(final int[] A, final int[] B, final int[] C, final int topElements) {
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
		int tmpDouble;
		int tmpInt;
		int tmpShort;

		for (int i=0; i<elems; i++) {
			j = length - i - 1;
			//swap elements in i with those in j
			tmpDouble = A[i]; A[i] = A[j]; A[j] = tmpDouble;
			tmpInt = B[i]; B[i] = B[j]; B[j] = tmpInt;
			tmpShort = C[i]; C[i] = C[j]; C[j] = tmpShort;
		}
	}
	
	/**
	 * Reverses the elements of the two arrays, after they have
	 * been sorted.
	 * @param A double[] the first array.
	 * @param B int[] an additional array to sort by A.
	 * @param C int[] an additional array to sort by A.
	 * @param topElements int the number of elements to be reversed.
	 */
	private static void reverse(final int[] A, final int[] B, final int topElements) {
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
		int tmpDouble;
		int tmpInt;
		//int tmpShort;

		for (int i=0; i<elems; i++) {
			j = length - i - 1;
			//swap elements in i with those in j
			tmpDouble = A[i]; A[i] = A[j]; A[j] = tmpDouble;
			tmpInt = B[i]; B[i] = B[j]; B[j] = tmpInt;
			//tmpShort = C[i]; C[i] = C[j]; C[j] = tmpShort;
		}
	}

    private static void reverse(final int[] A, final int[] B, final int[] C, final int[] D, final int topElements) {
        //reversing the top elements
        final int length = A.length;
        final int elems = //topElements
            topElements > length/2
            ? length/2
            : topElements;
        //if (elems > A.length/2)
        //  elems = A.length/2;


        int j;
        //temporary swap variables
        int tmpDouble;
        int tmpInt;
        int tmpShort;

        for (int i=0; i<elems; i++) {
            j = length - i - 1;
            //swap elements in i with those in j
            tmpDouble = A[i]; A[i] = A[j]; A[j] = tmpDouble;
            tmpInt = B[i]; B[i] = B[j]; B[j] = tmpInt;
            tmpShort = C[i]; C[i] = C[j]; C[j] = tmpShort;
			tmpShort = D[i]; D[i] = D[j]; D[j] = tmpShort;
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
	public static void descendingHeapSort(final int[] A, final int[] B, final int[] C, final int topElements) {
		ascendingHeapSort(A, B, C, topElements);
		reverse(A, B, C, topElements);
	}
	/**
	 * Maintains the heap property.
	 * @param A int[] The array on which we operate.
	 * @param i int a position in the array. This number is
	 * between 1 and A.length inclusive.
	 */
	private static void maxHeapify(final int[] A, final int[] B, final int[] C, final int i, final int heapSize) {
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
		int tmpDouble;
		int tmpInt;
		int tmpShort;


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

    private static void maxHeapify(final int[] A, final int[] B, final int[] C, final int[] D, final int i, final int heapSize) {
        final int l = 2 * i;
        final int r = 2 * i + 1;

        int largest =
            (l <= heapSize && A[l - 1] > A[i - 1])
                ? l
                : i;
        //if (l <= heapSize && A[l - 1] > A[i - 1])
        //  largest = l;
        //else
        //  largest = i;
        if (r <= heapSize && A[r - 1] > A[largest - 1])
            largest = r;

        //temporary variables for swaps
        int tmpDouble;
        int tmpInt;
        int tmpShort;


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

            tmpShort = D[largest -1];
            D[largest -1] = D[i - 1];
            D[i - 1] = tmpShort;

            maxHeapify(A, B, C, D, largest, heapSize);
        }
    }


	/**
	 * Maintains the heap property.
	 * @param A int[] The array on which we operate.
	 * @param B int[] an additional array to sort by A.
	 * @param i int a position in the array. This number is
	 * between 1 and A.length inclusive.
	 */
	private static void maxHeapify(final int[] A, final int[] B, final int i, final int heapSize) {
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
		int tmpDouble;
		int tmpInt;
		//int tmpShort;


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


	private static void maxHeapify(final int[][] A, final int i, final int heapSize) {
		final int l = 2 * i;
		final int r = 2 * i + 1;

		int largest = 
			(l <= heapSize && A[0][l - 1] > A[0][i - 1])
				? l
				: i;
		//if (l <= heapSize && A[l - 1] > A[i - 1])
		//	largest = l;
		//else
		//	largest = i;
		if (r <= heapSize && A[0][r - 1] > A[0][largest - 1])
			largest = r;

		//temporary variables for swaps
		int tmpInt;
		//int tmpShort;


		if (largest != i) {
			for(int j=0;j<A.length;j++)
			{
				tmpInt = A[j][largest - 1];
				A[j][largest - 1] = A[j][i - 1];
				A[j][i - 1] = tmpInt;
			}
			maxHeapify(A, largest, heapSize);
		}
	}



	/** The size of the heap.*/
	//private int heapSize;
	/** The left child.*/
	//private int l;
	/** The right child.*/
	//private int r;
	/** The largest.*/
	//private int largest;
	/** A temporary double.*/
	//private int tmpDouble;
	/** A temporary int.*/
	//private int tmpInt;
	
	/**
	 * Builds a maximum heap.
	 * @param A int[] the array which will be transformed into a heap.
	 * @return heapSize
	 */
	/*private int buildMaxHeap(int[] A, int[] B, int[] C) {
		int heapSize = A.length;
		for (int i = (int) Math.floor(heapSize / 2.0D); i > 0; i--)
			maxHeapify(A, B, C, i, heapSize);
		return heapSize;
	}*/
	
	/**
	 * Sorts the given array using heap-sort in ascending order
	 * @param A int[] the array to be sorted
	 */
	/*public void heapSort(int[] A, int[] B, int[] C) {
		int heapSize = buildMaxHeap(A, B, C);
		int tmpDouble;
		int tmpInt;
		final int Alength = A.length;
		for (int i = Alength; i > 0; i--) {
			tmpDouble = A[i - 1];
			A[i - 1] = A[0];
			A[0] = tmpDouble;
			tmpInt = B[i - 1];
			B[i - 1] = B[0];
			B[0] = tmpInt;
			tmpInt = C[i - 1];
			C[i - 1] = C[0];
			C[0] = tmpInt;
			heapSize--;
			maxHeapify(A, B, C, 1, heapSize);
		}
	}
	/**
	 * Sorts the top <tt>topElements</tt> of the given array in 
	 * ascending order using heap sort.
	 * @param A int[] the array to be sorted
	 * @param topElements int the number of elements to be sorted.
	 */
	/*public void heapSort(int[] A, int[] B, int[] C, int topElements) {
		int heapSize = buildMaxHeap(A, B, C);
		
		final int Alength = A.length;
		final int end = Alength - topElements;
		int tmpDouble;
		int tmpInt;
		for (int i = Alength; i > end; i--) {
			tmpDouble = A[i - 1];
			A[i - 1] = A[0];
			A[0] = tmpDouble;
			tmpInt = B[i - 1];
			B[i - 1] = B[0];
			B[0] = tmpInt;
			
			tmpInt = C[i - 1];
			C[i - 1] = C[0];
			C[0] = tmpInt;
			
			heapSize--;
			maxHeapify(A, B, C, 1, heapSize);
		}
	}
	/**
	 * Maintains the heap property.
	 * @param A int[] The array on which we operate.
	 * @param i int a position in the array. This number is 
	 * between 1 and A.length inclusive.
	 */

	/*private void maxHeapify(int[] A, int[] B, int[] C, int i, int heapSize) {		
		int largest;
		int l = 2 * i;
		int r = 2 * i + 1;
		if (l <= heapSize && A[l - 1] > A[i - 1])
			largest = l;
		else
			largest = i;
		if (r <= heapSize && A[r - 1] > A[largest - 1])
			largest = r;
		if (largest != i) {
			int tmpDouble = A[largest - 1];
			A[largest - 1] = A[i - 1];
			A[i - 1] = tmpDouble;
			int tmpInt = B[largest - 1];
			B[largest - 1] = B[i - 1];
			B[i - 1] = tmpInt;
			
			tmpInt = C[largest - 1];
			C[largest - 1] = C[i - 1];
			C[i - 1] = tmpInt;

			
			maxHeapify(A, B, C, largest, heapSize);
		}
	}*/
}
