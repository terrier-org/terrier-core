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
 * The Original Code is TestDistance.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.utility;

import static org.junit.Assert.*;
import org.junit.Test;

/** Test that Distance works as expected.
 * @since 3.0
 * @author Craig Macdonald
 */
public class TestDistance {
	
	@Test public void testNoTimes_window2()
	{
		int[] x = new int[]{8,10,14,15};
		int[] y = new int[]{1,4,6,12,17};
		assertEquals(0, Distance.noTimes(new int[][]{x,y}, 2, 20));
		x = new int[]{8};
		y = new int[]{9};
		//0:0-1 1:1-2 2:2-3 3:3-4 4:4-5 5:5-6 6:6-7 7:7-8 8:8-9 9:9-10
		//10:10-11 11:11-12 12:12-13 13:13-14 14:14-15 15:15-16 16:16-17 17:18-18 18:18-19
		//only 8-9 is a match
		assertEquals(1, Distance.noTimes(new int[][]{x,y}, 2, 20));	
		x = new int[]{8,10,14,15};
		//8-9 and 9-10 are matches
		assertEquals(2, Distance.noTimes(new int[][]{x,y}, 2, 20));	
		y = new int[]{7};
		//8-7 is a match
		assertEquals(1, Distance.noTimes(new int[][]{x,y}, 2, 20));	
		y = new int[]{7,9};
		//(7,8), (8,9), (9,10) are matches
		assertEquals(3, Distance.noTimes(new int[][]{x,y}, 2, 20));
	}
	
	@Test public void testNoTimes_2terms_window2()
	{
		for(int i=0;i<1000000;i++)
		{
			int[] x = new int[]{8,10,14,15};
			int[] y = new int[]{1,4,6,12,17};
			assertEquals(0, Distance.noTimes(x,y, 2, 20));
			x = new int[]{8};
			y = new int[]{9};
			//0:0-1 1:1-2 2:2-3 3:3-4 4:4-5 5:5-6 6:6-7 7:7-8 8:8-9 9:9-10
			//10:10-11 11:11-12 12:12-13 13:13-14 14:14-15 15:15-16 16:16-17 17:18-18 18:18-19
			//only 8-9 is a match
			assertEquals(1, Distance.noTimes(x,y, 2, 20));	
			x = new int[]{8,10,14,15};
			//8-9 and 9-10 are matches
			assertEquals(2, Distance.noTimes(x,y, 2, 20));	
			y = new int[]{7};
			//8-7 is a match
			assertEquals(1, Distance.noTimes(x,y, 2, 20));	
			y = new int[]{7,9};
			//(7,8), (8,9), (9,10) are matches
			assertEquals(3, Distance.noTimes(x,y, 2, 20));
		}
	}
	
	@Test public void testNoTimesNEW_2terms_window2()
	{
		for(int i=0;i<1000000;i++)
		{
			int[] x = new int[]{8,10,14,15};
			int[] y = new int[]{1,4,6,12,17};
			assertEquals(0, Distance.noTimesNEW(x,y, 2, 20));
			x = new int[]{8};
			y = new int[]{9};
			//0:0-1 1:1-2 2:2-3 3:3-4 4:4-5 5:5-6 6:6-7 7:7-8 8:8-9 9:9-10
			//10:10-11 11:11-12 12:12-13 13:13-14 14:14-15 15:15-16 16:16-17 17:18-18 18:18-19
			//only 8-9 is a match
			assertEquals(1, Distance.noTimesNEW(x,y, 2, 20));	
			x = new int[]{8,10,14,15};
			//8-9 and 9-10 are matches
			assertEquals(2, Distance.noTimesNEW(x,y, 2, 20));	
			y = new int[]{7};
			//8-7 is a match
			assertEquals(1, Distance.noTimesNEW(x,y, 2, 20));	
			y = new int[]{7,9};
			//(7,8), (8,9), (9,10) are matches
			assertEquals(3, Distance.noTimesNEW(x,y, 2, 20));
		}
	}
	
		
	@Test public void testNoTimesSameOrder_window3()
	{
		int[] x = new int[]{8,10,14,15};
		int[] y = new int[]{1,4,6,12,17};
		assertEquals(0, Distance.noTimesSameOrder(x,y, 2, 20));
		x = new int[]{8};
		y = new int[]{9};
		//0:0-2 1:1-3 2:2-4 3:3-5 4:4-6 5:5-7 6:6-8 7:7-9 8:8-10 9:9-11
		//10:10-12 11:11-13 12:12-14 13:13-15 14:14-16 15:15-17 16:16-18 17:18-19 18:18-20
		//only window 8-10 is a match
		assertEquals(1, Distance.noTimesSameOrder(x,y, 3, 20));	
		//(9,8) is not a match
		assertEquals(0, Distance.noTimesSameOrder(y,x, 3, 20));	
		
		x = new int[]{8,10,14,15};
		//8-9 and 9-10 are matches
		assertEquals(1, Distance.noTimesSameOrder(x,y, 3, 20));	
		//8-7 is a NOT match, but 9-10 is a match
		assertEquals(1, Distance.noTimesSameOrder(x,y, 3, 20));	
	}
	
	
	@Test public void testNoTimesSameOrder_window2()
	{
		int[] x = new int[]{8,10,14,15};
		int[] y = new int[]{1,4,6,12,17};
		assertEquals(0, Distance.noTimesSameOrder(x,y, 2, 20));
		x = new int[]{8};
		y = new int[]{9};
		//0:0-1 1:1-2 2:2-3 3:3-4 4:4-5 5:5-6 6:6-7 7:7-8 8:8-9 9:9-10
		//10:10-11 11:11-12 12:12-13 13:13-14 14:14-15 15:15-16 16:16-17 17:18-18 18:18-19
		//only 8-9 is a match
		assertEquals(1, Distance.noTimesSameOrder(x,y, 2, 20));	
		//9-8 is not a match
		assertEquals(0, Distance.noTimesSameOrder(y,x, 2, 20));	
		
		x = new int[]{8,10,14,15};
		y = new int[]{9};
		//8-9 is a match, 10-9 is not a match
		assertEquals(1, Distance.noTimesSameOrder(x,y, 2, 20));	
		//8-7 is a NOT match, but 9-10 is a match
		assertEquals(1, Distance.noTimesSameOrder(y,x, 2, 20));	
	}
	
	
//	@Test public void testNoTimesSameOrder_2terms()
//	{
//		int[] x = new int[]{8,10,14,15};
//		int[] y = new int[]{1,4,6,12,17};
//		assertEquals(0, Distance.noTimesSameOrder(new int[][]{x,y}, 20));
//		
//		y = new int[]{1,4,9,10,12,17};
//		assertEquals(1, Distance.noTimesSameOrder(new int[][]{x,y}, 20));
//		
//		x = new int[]{0};
//		y = new int[]{1,4,9,10,12,17};
//		assertEquals(1, Distance.noTimesSameOrder(new int[][]{x,y}, 20));
//		
//		x = new int[]{0};
//		y = new int[]{1};
//		assertEquals(1, Distance.noTimesSameOrder(new int[][]{x,y}, 20));
//		
//		x = new int[]{0,5};
//		y = new int[]{1,6};
//		assertEquals(2, Distance.noTimesSameOrder(new int[][]{x,y}, 20));
//		
//		x = new int[]{10,15};
//		y = new int[]{1,6};
//		assertEquals(0, Distance.noTimesSameOrder(new int[][]{x,y}, 20));		
//		
//	}
	
	@Test public void testOverflow()
	{
		int[] x = new int[]{10,15};
		int[] y = new int[]{1,6};
		//assertEquals(0, Distance.noTimesSameOrder(x, 0, x.length -1, y, 0, y.length -1, 2, 872));
		assertEquals(0, Distance.noTimesSameOrder(x, y, 2, 872));		
	}
	
	@Test public void testFindSmallest()
	{
		int[] x = new int[]{8,14,10,15};
		int[] y = new int[]{4,6,10,12,17,1};
		assertEquals(0, Distance.findSmallest(x, y));
	}
}
