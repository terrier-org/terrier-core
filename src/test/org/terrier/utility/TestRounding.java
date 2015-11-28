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
 * The Original Code is TestRounding.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.utility;

import org.junit.Test;

import junit.framework.TestCase;

/** Test rounding works as expected */
public class TestRounding extends TestCase {
	static final int maxDP=9;
	
	@Test public void testIntegers()
	{
		assertEquals("-100", Rounding.toString(-100,0));
		assertEquals("0", Rounding.toString(0,0));
		assertEquals("1", Rounding.toString(1,0));
		assertEquals("100", Rounding.toString(100,0));
		assertEquals("100", Rounding.toString(100.4d,0));
		assertEquals("100", Rounding.toString(100.49999999d,0));
		assertEquals("101", Rounding.toString(100.5d,0));
	}
	
	@Test public void testIntegersVariableRangeDP()
	{
		int number = 0;
		for(int i=0;i<=maxDP;i++)
		{
			testIntegerWithDP(number, i);
		}
	}
	
	protected void testIntegerWithDP(int number, int places)
	{
		StringBuilder s = new StringBuilder();
		s.append(""+number);
		if (places > 0)
		{
			s.append('.');
			for(int j=1;j<=places;j++)
			{
				s.append("0");
			}
		}
		assertEquals("testIntegerWithDP failed for "+places+" decimal places", s.toString(), Rounding.toString(number, places));
	}
	
	@Test public void testNonIntegers1DP()
	{
		assertEquals("0.1", Rounding.toString(0.1d,1));
		assertEquals("0.1", Rounding.toString(0.11d,1));
		assertEquals("0.1", Rounding.toString(0.111111111111111d,1));
		assertEquals("100.0", Rounding.toString(100,1));
		assertEquals("100.4", Rounding.toString(100.4d,1));
		assertEquals("100.4", Rounding.toString(100.44d,1));
		assertEquals("100.5", Rounding.toString(100.45d,1));
		assertEquals("100.5", Rounding.toString(100.49999999d,1));
	}
	
	@Test public void testNonIntegers4DP()
	{
		assertEquals("0.1000", Rounding.toString(0.1d,4));
		assertEquals("0.1100", Rounding.toString(0.11d,4));
		assertEquals("0.1111", Rounding.toString(0.111111111111111d,4));
		assertEquals("0.1111", Rounding.toString(0.111149d,4));
		assertEquals("0.1112", Rounding.toString(0.11115d,4));
	}
	
	@Test public void testNonIntegers5DP()
	{
		assertEquals("0.00000", Rounding.toString(0,5));
		assertEquals("0.10000", Rounding.toString(0.1d,5));
		assertEquals("0.11000", Rounding.toString(0.11d,5));
		assertEquals("0.11111", Rounding.toString(0.111111111111111d,5));
		assertEquals("0.11115", Rounding.toString(0.111149d,5));
		assertEquals("0.11115", Rounding.toString(0.11115d,5));
	}
	
	@Test public void testNonIntegers6DP()
	{
		assertEquals("0.000000", Rounding.toString(0,6));
		assertEquals("0.100000", Rounding.toString(0.1d,6));
		assertEquals("0.110000", Rounding.toString(0.11d,6));
		assertEquals("0.111111", Rounding.toString(0.111111111111111d,6));
		assertEquals("0.111149", Rounding.toString(0.111149d,6));
		assertEquals("0.111150", Rounding.toString(0.11115d,6));
	}
	
	@Test public void testNonIntegers7DP()
	{
		assertEquals("0.0000000", Rounding.toString(0,7));
		assertEquals("0.1000000", Rounding.toString(0.1d,7));
		assertEquals("0.1100000", Rounding.toString(0.11d,7));
		assertEquals("0.1111111", Rounding.toString(0.111111111111111d,7));
		assertEquals("0.1111490", Rounding.toString(0.111149d,7));
		assertEquals("0.1111500", Rounding.toString(0.11115d,7));
	}
	
	@Test public void testNonIntegers8DP()
	{
		assertEquals("0.00000000", Rounding.toString(0,8));
		assertEquals("0.10000000", Rounding.toString(0.1d,8));
		assertEquals("0.11000000", Rounding.toString(0.11d,8));
		assertEquals("0.11111111", Rounding.toString(0.111111111111111d,8));
		assertEquals("0.11114900", Rounding.toString(0.111149d,8));
		assertEquals("0.11115000", Rounding.toString(0.11115d,8));
	}
	
	@Test public void testNonIntegers9DP()
	{
		assertEquals("0.000000000", Rounding.toString(0,9));
		assertEquals("0.100000000", Rounding.toString(0.1d,9));
		assertEquals("0.110000000", Rounding.toString(0.11d,9));
		assertEquals("0.111111111", Rounding.toString(0.111111111111111d,9));
		assertEquals("0.111149000", Rounding.toString(0.111149d,9));
		assertEquals("0.111150000", Rounding.toString(0.11115d,9));
	}
	
	
//	@Test public void testNonIntegers10DP()
//	{
//		assertEquals("0.1000000000", Rounding.toString(0.1d,10));
//		assertEquals("0.1100000000", Rounding.toString(0.11d,10));
//		assertEquals("0.1111111111", Rounding.toString(0.111111111111111d,10));
//		assertEquals("0.1111490000", Rounding.toString(0.111149d,10));
//		assertEquals("0.1111500000", Rounding.toString(0.11115d,10));
//	}
}
