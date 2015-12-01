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
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original contributor)
  *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.utility;
import junit.framework.TestCase;

import org.junit.Test;
/**
 * Tests the functionality of the org.terrier.sorting.HeapSort class
 * Creation date: (05/08/2003 09:55:33)
 * @author Vassilis Plachouras
 */
public class TestHeapSort extends TestCase
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
		// Insert code to start the application here.
		double[] test = new double[] { 3, 5, 2, 1, 6, 7, 9, 8, 0, 4 };
		int[] testIds = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		short[] testShorts = new short[test.length];
		
		HeapSort.ascendingHeapSort(test, testIds, testShorts, test.length);
		assertTrue(isAscending(test));
		HeapSort.descendingHeapSort(test, testIds, testShorts, test.length);
		assertTrue(isDescending(test));
	}
}
