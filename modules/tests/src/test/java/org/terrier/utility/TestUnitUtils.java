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
 * The Original Code is TestUnitUtils.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.utility;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestUnitUtils {

	@Test public void testInt()
	{
		assertEquals(1000, UnitUtils.parseInt("1000"));
		assertEquals(1000, UnitUtils.parseInt("1K"));
		assertEquals(1024, UnitUtils.parseInt("1Ki"));
	}
	
	@Test public void testDouble()
	{
		assertEquals(1000, UnitUtils.parseDouble("1000"), 0.0d);
		assertEquals(1000, UnitUtils.parseDouble("1K"), 0.0d);
		assertEquals(1000, UnitUtils.parseDouble("1K"), 0.0d);
	}
	
	@Test public void testLong()
	{
		assertEquals(1000, UnitUtils.parseLong("1000"));
		assertEquals(1000, UnitUtils.parseLong("1K"));
		assertEquals(1024, UnitUtils.parseLong("1Ki"));
	}
	
	@Test public void testFloat()
	{
		assertEquals(1000, UnitUtils.parseFloat("1000"), 0.0f);
		assertEquals(1000, UnitUtils.parseFloat("1K"), 0.0f);
		assertEquals(1024, UnitUtils.parseFloat("1Ki"), 0.0f);
	}
	
	
}
