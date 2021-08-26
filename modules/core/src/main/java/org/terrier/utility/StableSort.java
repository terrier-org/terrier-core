package org.terrier.utility;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;

public class StableSort {

    /** numDocs is ignored */
    public static void sortDescending(double[] scores, int[] docids, short[] occurrences, int numDocs) {
        sortDescending(scores, docids, occurrences);
    }

    public static void sortDescending(double[] scores, int[] docids, short[] occurrences) {
        StableSort.sortDescending(scores, Arrays.asList(
			new List<?>[] { 
				Ints.asList(docids), 
				Shorts.asList(occurrences)
			}));
    }

    /* some code in this method comes from https://stackoverflow.com/questions/12164795/how-to-sort-multiple-arrays-in-java */
    public static void sortDescending(double[] scores, List<List<?>> lists)
    {
        //Note: There aren't any checks that the arrays
        // are the same length, or even that there are
        // any arrays! So exceptions can be expected...

        // Create an array of indices, initially in order.
        int[] indices = ascendingIntegerArray(scores.length);
        
        // Sort the indices in order of the first array's items.
        // Collections.sort() is stable
        // Arrays.asList this does not create a copy of the array
        Collections.sort(Ints.asList(indices), (Integer i1, Integer i2) -> Double.compare(scores[i2], scores[i1]));

        // Scan the new indices array and swap items to their new locations,
        // while remembering where original items stored.
        // Only swaps can be used since we do not know the type of the lists
        int[] prevSwaps = new int[indices.length];
        for (int i = 0; i < indices.length; i++) {
            int k = indices[i];
            while (i > k)
                k = prevSwaps[k];
            if (i != k) {
                prevSwaps[i] = k;
                //swap for scores arrya
                swap(scores, i, k);
                //swap for other arrays
                for (List<?> list : lists)
                    Collections.swap(list, i, k);
            }
        }
    }

    /** a version thast breaks ties by ascending integers in the secondAsc array */
    public static void sortDescendingTieBreaker(double[] firstDesc, int[] secondAsc, List<List<?>> others)
    {
        //Note: There aren't any checks that the arrays
        // are the same length, or even that there are
        // any arrays! So exceptions can be expected...

        // Create an array of indices, initially in order.
        int[] indices = ascendingIntegerArray(firstDesc.length);
        
        // Sort the indices in order of the first array's items.
        // Collections.sort() is stable
        // Arrays.asList this does not create a copy of the array
        Collections.sort(Ints.asList(indices), new Comparator<Integer>() {
            public int compare(Integer i1, Integer i2) {
                int rtr = Double.compare(firstDesc[i2], firstDesc[i1]);
                if (rtr != 0){
                    return rtr;
                }
                return Integer.compare(secondAsc[i1], secondAsc[i2]);
            }
        });

        // Scan the new indices array and swap items to their new locations,
        // while remembering where original items stored.
        // Only swaps can be used since we do not know the type of the lists
        int[] prevSwaps = new int[indices.length];
        for (int i = 0; i < indices.length; i++) {
            int k = indices[i];
            while (i > k)
                k = prevSwaps[k];
            if (i != k) {
                prevSwaps[i] = k;
                //swap for scores and docis arrays
                swap(firstDesc, i, k);
                swap(secondAsc, i, k);
                //swap for other arrays
                for (List<?> list : others)
                    Collections.swap(list, i, k);
            }
        }
    }


    public static final void swap(double[] array, int i1, int i2)
    {
        double temp = array[i1];
        array[i1] = array[i2];
        array[i2] = temp;
    }

    public static final void swap(int[] array, int i1, int i2)
    {
        int temp = array[i1];
        array[i1] = array[i2];
        array[i2] = temp;
    }

    public static int[] ascendingIntegerArray(int length)
    {
        int[] array = new int[length];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = i;
        }
        return array;
    }

}