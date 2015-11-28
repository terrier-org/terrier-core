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
 * The Original Code is ArrayUtils.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Ben He ben{a}dcs.gla.ac.uk (original author) 
 *  Craig Macdonald craigm{a}dcs.gla.ac.uk 
 */
package org.terrier.utility;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;

import java.util.Arrays;
import java.util.regex.Pattern;

/** Handy methods for resizing arrays, and other handy array methods
 * This is a fresh implementation of the capacity methods, without the
 * use of any prelicensed code.
 * 
 * @author Ben He, Rodrygo Santos
*/
public class ArrayUtils {
	/* TODO: use as an integer to reduce FP operations */
	/** the Golden ration (&phi;). */ 
	protected static final double GOLDEN_RATIO = 1.618;
	
	
	public static int[][] invert(final int[][] input)
	{
		int L1 = input.length;
		int L2 = input[0].length;
		int[][] rtr = new int[L2][L1];
		for(int i=0;i<L1;i++)
			for(int j=0;j<L2;j++)
				rtr[j][i] = input[i][j];
		return rtr;
	}
	
	/** Grow an array to ensure it is the desired length. 
 	  * @param array input array
 	  * @param length ensure array is this length 
 	  * @return new array with desired length */
	public static byte[] ensureCapacity(byte[] array, int length){
		if (array.length < length){
			byte[] buffer = new byte[length];
			System.arraycopy(array, 0, buffer, 0, array.length);
			array = buffer;
		}
		return array;
	}
	
	/** Grow an array to ensure it is the desired length. Only copy the first preserve
	 * elements from the input array 
	 * @param array input array
	 * @param length new desired length
	 * @param preserve amount of old array to copy to new array in case of reallocation 
	 * @return new array with desired length */
	public static byte[] ensureCapacity(byte[] array, int length, int preserve){
		if (array.length < length){
			byte[] buffer = new byte[length];
			System.arraycopy(array, 0, buffer, 0, preserve);
			array = buffer;
		}
		return array;
	}
	
	/** Grow an array to ensure it is <i>at least</i> the desired length. The golden ratio
	 * is involved in the new length
	 * @param array input array
	 * @param length minimuim length of new array
	 * @return new array appropriately sized
	 */
	public static byte[] grow(byte[] array, int length){
		final int oldlength = array.length; 
		if (oldlength < length){
			int newsize = Math.max(length, (int)(((double)oldlength)*GOLDEN_RATIO));
			byte[] buffer = new byte[newsize];
			System.arraycopy(array, 0, buffer, 0, oldlength);			
			array = buffer;
		}
		return array;
	}
	
	
	public static int[] growOrCreate(int[] arr, int len) {
		
		return (arr == null) ? new int[len] : grow(arr, len);
	}

	public static byte[] growOrCreate(byte[] arr, int len) {
		
		return (arr == null) ? new byte[len] : grow(arr, len);
	}
	
	/** Reverse the order of an array of doubles */
	public static void reverse(double[] a)
	{
		double tmp;
		for(int i=0;i<a.length /2;i++)
		{
			tmp = a[i];
			a[i] = a[a.length -i -1];
			a[a.length -i -1] = tmp;
		}
	}
	
	/** Reverse the order of an array of ints */
	public static void reverse(int[] a)
	{
		int tmp;
		for(int i=0;i<a.length /2;i++)
		{
			tmp = a[i];
			a[i] = a[a.length -i -1];
			a[a.length -i -1] = tmp;
		}
	}
	
	/** Grow an array to ensure it is <i>at least</i> the desired length. The golden ratio
	 * is involved in the new length. Only copy the first preserve
	 * elements from the input array.
	 * @param array input array
	 * @param length minimuim length of new array
	 * @return new array appropriately sized
	 */
	public static byte[] grow(byte[] array, int length, int preserve){
		if (array.length < length){
			int newsize = Math.max(length, (int)((double)array.length*GOLDEN_RATIO));
			byte[] buffer = new byte[newsize];
			System.arraycopy(array, 0, buffer, 0, preserve);
			array = buffer;
		}
		return array;
	}


	/** Join some strings together.
	  * @param in Strings to join
	  * @param join Character or String to join by */
    public static String join (String[] in, String join) {
    	return join(in, join, 0, in.length);
    }
    /**
     * Join some strings together.
     * @param in
     * @param join
     * @param l
     * @param r
     * @return String
     */
    public static String join (String[] in, String join, int l, int r) {
        final StringBuilder s = new StringBuilder();
        if (in.length == 0) {
        	return "";
        }
        else if (in.length > r) {
        	throw new ArrayIndexOutOfBoundsException();
        }
        for (int i = l; i < r; i++) {
            s.append(in[i]);
            s.append(join);
        }
        s.setLength(s.length() - join.length());
        return s.toString();
    }
    
    /** Join some strings together.
	  * @param in Strings to join
	  * @param join Character or String to join by */
   public static String join (String[] in, char join)
   {
       final StringBuilder s = new StringBuilder();
       if (in.length == 0)
       	return "";
       for(String i : in)
       {
           s.append(i);
           s.append(join);
       }
       s.setLength(s.length() - 1);
       return s.toString();
   }

   /**
    * Join some strings together.
    * @param in
    * @param join
    * @return String
    */
	public static String join(int[] in, String join) {
		final StringBuilder s = new StringBuilder();
		if (in.length == 0)
        	return "";
        for(int i : in)
        {
            s.append(""+i);
            s.append(join);
        }
        s.setLength(s.length() - join.length());
        return s.toString();
	}
	/**
	 * Join some strings together.
	 * @param in
	 * @param join
	 * @return String
	 */
	public static String join(boolean[] in, String join) {
		final StringBuilder s = new StringBuilder();
		if (in.length == 0)
        	return "";
        for(boolean i : in)
        {
            s.append(""+i);
            s.append(join);
        }
        s.setLength(s.length() - join.length());
        return s.toString();
	}

	/**
	 * grow array
	 * @param array
	 * @param length
	 * @return int[]
	 */
	public static int[] grow(int[] array, int length) {
		final int oldlength = array.length; 
		if (oldlength < length){
			int newsize = Math.max(length, (int)(((double)oldlength)*GOLDEN_RATIO));
			int[] buffer = new int[newsize];
			System.arraycopy(array, 0, buffer, 0, oldlength);			
			array = buffer;
		}
		return array;
	}

	/**
	 * grow array
	 * @param array
	 * @param length
	 * @return double[]
	 */
	public static double[] grow(double[] array, int length) {
		final int oldlength = array.length; 
		if (oldlength < length){
			int newsize = Math.max(length, (int)(((double)oldlength)*GOLDEN_RATIO));
			double[] buffer = new double[newsize];
			System.arraycopy(array, 0, buffer, 0, oldlength);			
			array = buffer;
		}
		return array;
	}

	/**
	 * grow array
	 * @param array
	 * @param length
	 * @return short[]
	 */
	public static short[] grow(short[] array, int length) {
		final int oldlength = array.length; 
		if (oldlength < length){
			int newsize = Math.max(length, (int)(((double)oldlength)*GOLDEN_RATIO));
			short[] buffer = new short[newsize];
			System.arraycopy(array, 0, buffer, 0, oldlength);			
			array = buffer;
		}
		return array;
	}

	/**
	 * parse comma delimited string
	 * @param src
	 * @return String[]
	 */
	public static String[] parseCommaDelimitedString(String src)
	{
		if (src == null)
			return new String[0];
		src = src.trim();
		if (src.length() == 0)
			return new String[0];
		String[] parts = src.split("\\s*,\\s*");
		return parts;
	}
	/**
	 * parse delimited string
	 * @param src
	 * @param delim
	 * @return String[]
	 */
	public static String[] parseDelimitedString(String src, String delim)
	{
		if (src == null)
			return new String[0];
		src = src.trim();
		if (src.length() == 0)
			return new String[0];
		String[] parts = src.split("\\s*"+Pattern.quote(delim)+"\\s*");
		return parts;
	}
	
	/**
	 * parse delimited string
	 * @param src
	 * @param delims
	 * @return String[]
	 */
	public static String[] parseDelimitedString(String src, String[] delims)
	{
		if (src == null)
			return new String[0];
		src = src.trim();
		if (src.length() == 0)
			return new String[0];
		String[] parts = src.split("\\s*["+ArrayUtils.join(delims, "") +"]\\s*");
		return parts;
	}
	
	/**
	 * parse comma delimited int
	 * @param src
	 * @return int[]
	 */
	public static int[] parseCommaDelimitedInts(String src)
	{
		final String[] parts = parseCommaDelimitedString(src);
		if (parts.length == 0)
			return new int[0];
		int[] rtr = new int[parts.length];
		for(int i=0;i<parts.length;i++)
			rtr[i] = Integer.parseInt(parts[i]);
		return rtr;
	}
	/**
	 * parse comma delimited int
	 * @param src
	 * @param sep
	 * @return int[]
	 */
	public static int[] parseDelimitedInts(String src, String sep)
	{
		final String[] parts = parseDelimitedString(src, sep);
		if (parts.length == 0)
			return new int[0];
		int[] rtr = new int[parts.length];
		for(int i=0;i<parts.length;i++)
			rtr[i] = Integer.parseInt(parts[i]);
		return rtr;
	}

	/**
	 * join string
	 * @param in
	 * @param join
	 * @return String
	 */
	public static String join(byte[] in, String join) {
		final StringBuilder s = new StringBuilder();
		if (in.length == 0)
        	return "";
        for(byte i : in)
        {
            s.append(""+i);
            s.append(join);
        }
        s.setLength(s.length() - join.length());
        return s.toString();
	}
	/**
	 * join string
	 * @param in
	 * @param join
	 * @return String
	 */
	public static String join(double[] in, String join) {
		final StringBuilder s = new StringBuilder();
		if (in.length == 0)
        	return "";
        for(double i : in)
        {
            s.append(""+i);
            s.append(join);
        }
        s.setLength(s.length() - join.length());
        return s.toString();
	}
	/**
	 * cast double[] to int[]
	 * @param arr
	 * @return int[]
	 */
	public static int[] cast(double[] arr) {
		int[] carr = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			carr[i] = (int) arr[i];
		}		
		return carr;
	}
	/**
	 * cast int[] to double[]
	 * @param arr
	 * @return double[]
	 */
	public static double[] cast(int[] arr) {
		double[] carr = new double[arr.length];
		for (int i = 0; i < arr.length; i++) {
			carr[i] = (double) arr[i];
		}		
		return carr;
	}

	/**
	 * cast float[] to double[]
	 * @param arr
	 * @return double[]
	 */
	public static double[] cast(float[] arr) {
		double[] carr = new double[arr.length];
		for (int i = 0; i < arr.length; i++) {
			carr[i] = (double) arr[i];
		}		
		return carr;
	}
	/**
	 * return intersection between two int[]
	 * @param arr1
	 * @param arr2
	 * @return intersection
	 */
	public static int[] intersection(int[] arr1, int[] arr2) {
		TIntHashSet set = new TIntHashSet();
		set.addAll(arr1);
		
		Arrays.sort(arr2);
		TIntArrayList list = new TIntArrayList();
		for (int i : arr2) {
			if (set.contains(i)) {
				list.add(i);
			}
		}
		return list.toNativeArray();
	}

	/**
	 * union 2 int[]
	 * @param arr1
	 * @param arr2
	 * @return int[]
	 */
	public static int[] union(int[] arr1, int[] arr2) {
		TIntHashSet set = new TIntHashSet();
		set.addAll(arr1);
		set.addAll(arr2);

		int[] arr = set.toArray();
		Arrays.sort(arr);
		
		return arr;
	}
	
}
