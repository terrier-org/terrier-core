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
 * The Original Code is TestHeapSort.java
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original contributor)
  *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.utility;
import junit.framework.TestCase;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import com.google.common.primitives.Doubles;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
/**
 * Tests the functionality of the org.terrier.utility.StableSort class
 * Creation date: (05/08/2003 09:55:33)
 * @author Vassilis Plachouras
 */
public class TestStableSort extends TestCase
{
	
	static boolean isAscending(final double[] a)
	{
		double last = a[0];
		for(double v : a)
		{
			if (v < last)
				return false;
		}
		return true;
	}
	
	static boolean isDescending(final double[] a)
	{
		double last = a[0];
		for(double v : a)
		{
			if (v > last)
				return false;
		}
		return true;
	}
	
	@Test public void testSortAllItems()
	{
		double[] test = new double[] { 3, 5, 2, 1, 6, 7, 9, 8, 0, 4 };
		int[] testIds = new int[]    { 6, 4, 7, 8, 3, 2, 0, 1, 9, 5 };
		short[] testShorts = new short[test.length];
		
		StableSort.sortDescending(test, Arrays.asList(
			new List<?>[] { 
				Ints.asList(testIds), 
				Shorts.asList(testShorts)
			}));
		//System.out.println(Arrays.toString(test));
		assertTrue(isDescending(test));
		assertEquals(testIds[0], 0);
	}

	@Test public void testSort2ItemsStable()
	{
		double[] test = new double[] { 9,9 };
		int[] testIds = new int[]    { 0,1 };
		short[] testShorts = new short[test.length];
		
		StableSort.sortDescending(test, Arrays.asList(
			new List<?>[] { 
				Ints.asList(testIds), 
				Shorts.asList(testShorts)
			}));
		assertTrue(isDescending(test));
		assertEquals(testIds[0], 0);
		assertEquals(testIds[1], 1);
	}

	@Test public void testSort3ItemsStable()
	{
		double[] test = new double[] { 8, 9, 9 };
		int[] testIds = new int[]    { 2, 0, 1 };
		short[] testShorts = new short[test.length];
		
		StableSort.sortDescending(test, Arrays.asList(
			new List<?>[] { 
				Ints.asList(testIds), 
				Shorts.asList(testShorts)
			}));
		assertTrue(isDescending(test));
		assertEquals(testIds[0], 0);
		assertEquals(testIds[1], 1);
		assertEquals(testIds[2], 2);
	}
}
