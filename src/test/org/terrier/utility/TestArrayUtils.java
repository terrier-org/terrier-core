/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is TestArrayUtils.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.utility;

import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.Test;
/** Test ArrayUtils methods */
public class TestArrayUtils extends TestCase {

	@Test public void testParseCommaDelimitedInt() throws Exception
	{
		assertTrue(Arrays.equals(new int[0], ArrayUtils.parseCommaDelimitedInts("")));
		assertTrue(Arrays.equals(new int[0], ArrayUtils.parseCommaDelimitedInts(" ")));
		assertTrue(Arrays.equals(new int[]{1}, ArrayUtils.parseCommaDelimitedInts("1")));
		assertTrue(Arrays.equals(new int[]{1}, ArrayUtils.parseCommaDelimitedInts(" 1")));
		assertTrue(Arrays.equals(new int[]{1}, ArrayUtils.parseCommaDelimitedInts("1 ")));
		assertTrue(Arrays.equals(new int[]{1}, ArrayUtils.parseCommaDelimitedInts(" 1 ")));
		assertTrue(Arrays.equals(new int[]{1,2}, ArrayUtils.parseCommaDelimitedInts("1,2")));
		assertTrue(Arrays.equals(new int[]{1,2}, ArrayUtils.parseCommaDelimitedInts("1 ,2")));
		assertTrue(Arrays.equals(new int[]{1,2}, ArrayUtils.parseCommaDelimitedInts("1, 2")));
		assertTrue(Arrays.equals(new int[]{1,2}, ArrayUtils.parseCommaDelimitedInts("1 , 2")));
		assertTrue(Arrays.equals(new int[]{12,256}, ArrayUtils.parseCommaDelimitedInts("12,256")));
	}
	
	@Test public void testParseCommaDelimitedStrings() throws Exception
	{
		assertTrue(Arrays.equals(new String[0], ArrayUtils.parseCommaDelimitedString("")));
		assertTrue(Arrays.equals(new String[0], ArrayUtils.parseCommaDelimitedString(" ")));
		assertTrue(Arrays.equals(new String[]{"1"}, ArrayUtils.parseCommaDelimitedString("1")));
		assertTrue(Arrays.equals(new String[]{"1"}, ArrayUtils.parseCommaDelimitedString(" 1")));
		assertTrue(Arrays.equals(new String[]{"1"}, ArrayUtils.parseCommaDelimitedString("1 ")));
		assertTrue(Arrays.equals(new String[]{"1"}, ArrayUtils.parseCommaDelimitedString(" 1 ")));
		assertTrue(Arrays.equals(new String[]{"1","2"}, ArrayUtils.parseCommaDelimitedString("1,2")));
		assertTrue(Arrays.equals(new String[]{"1","2"}, ArrayUtils.parseCommaDelimitedString("1 ,2")));
		assertTrue(Arrays.equals(new String[]{"1","2"}, ArrayUtils.parseCommaDelimitedString("1, 2")));
		assertTrue(Arrays.equals(new String[]{"1","2"}, ArrayUtils.parseCommaDelimitedString("1 , 2")));
		assertTrue(Arrays.equals(new String[]{"12","256"}, ArrayUtils.parseCommaDelimitedString("12,256")));
	}
	
	@Test public void testReverseDouble() throws Exception
	{
		double[] a;
		
		a = new double[]{1};
		ArrayUtils.reverse(a);
		assertEquals(1.0d, a[0], 0.0d);
		
		a = new double[]{1,2};
		ArrayUtils.reverse(a);
		assertEquals(2.0d, a[0], 0.0d);
		assertEquals(1.0d, a[1], 0.0d);
		
		a = new double[]{1,2, 3};
		ArrayUtils.reverse(a);
		assertEquals(3.0d, a[0], 0.0d);
		assertEquals(2.0d, a[1], 0.0d);
		assertEquals(1.0d, a[2], 0.0d);
	}
	
	@Test public void testReverseInt() throws Exception
	{
		int[] a;
		
		a = new int[]{1};
		ArrayUtils.reverse(a);
		assertEquals(1, a[0]);
		
		a = new int[]{1,2};
		ArrayUtils.reverse(a);
		assertEquals(2, a[0]);
		assertEquals(1, a[1]);
		
		a = new int[]{1,2, 3};
		ArrayUtils.reverse(a);
		assertEquals(3, a[0]);
		assertEquals(2, a[1]);
		assertEquals(1, a[2]);
	}
	
	@Test public void testIntersection() {
		int[] arr1 = new int[]{ 0, 1, 2, 3, 4, 5 };
		int[] arr2 = new int[]{ 2, 5, 7, 8 };
		
		int[] inter = ArrayUtils.intersection(arr1, arr2);
		assertEquals(2, inter.length);
		assertEquals(2, inter[0]);
		assertEquals(5, inter[1]);
		
		arr1 = new int[]{};
		inter = ArrayUtils.intersection(arr1, arr2);
		assertEquals(0, inter.length);
	}
	
	@Test public void testUnion() {
		int[] arr1 = new int[]{ 0, 1, 2, 3, 4, 5 };
		int[] arr2 = new int[]{ 2, 5, 7, 8 };
		
		int[] inter = ArrayUtils.union(arr1, arr2);
		assertEquals(8, inter.length);
		assertEquals(0, inter[0]);
		assertEquals(1, inter[1]);
		assertEquals(2, inter[2]);
		assertEquals(3, inter[3]);
		assertEquals(4, inter[4]);
		assertEquals(5, inter[5]);
		assertEquals(7, inter[6]);
		assertEquals(8, inter[7]);
		
		arr1 = new int[]{};
		inter = ArrayUtils.union(arr1, arr2);
		assertEquals(4, inter.length);
		assertEquals(2, inter[0]);
		assertEquals(5, inter[1]);
		assertEquals(7, inter[2]);
		assertEquals(8, inter[3]);
	}
}
