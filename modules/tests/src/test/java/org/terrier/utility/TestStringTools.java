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
 * The Original is in 'TestStringTools.java'
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.utility;

import static org.junit.Assert.assertEquals;
import static org.terrier.utility.StringTools.toLowerCase;
import static org.terrier.utility.StringTools.toUpperCase;

import org.junit.Test;

public class TestStringTools {

	@Test
	public void testEncodingNorm() {
		assertEquals("x-MacRoman", StringTools.normaliseEncoding("x-mac-roman"));
	}

	protected void assertMatch(String t) {
		assertEquals(t + " does not match", t.toLowerCase(), toLowerCase(t));
		assertEquals(t + " does not match", t.toUpperCase(), toUpperCase(t));
	}

//	@Test
//	public void timeLower() {
//
//		final SecureRandom random = new SecureRandom();
//		final int count = 100000;
//
//		long start;
//		start = System.currentTimeMillis();
//		String s = new String("");
//		boolean rtr = false;
//		for (int i = 0; i < count; i++) {
//			s = new BigInteger(130, random).toString(32).toUpperCase()
//					.toLowerCase();
//			// System.out.println(s);
//			rtr = !s.equals("");
//			// rtr = s.length() > 0;
//		}
//		System.err.println("String.toLowerCase "
//				+ (System.currentTimeMillis() - start) + " " + rtr);
//
//		start = System.currentTimeMillis();
//		for (int i = 0; i < count; i++) {
//			s = toLowerCase(new BigInteger(130, random).toString(32)
//					.toUpperCase());
//			rtr = !s.equals("");
//		}
//		System.err.println("Fast toLowerCase "
//				+ (System.currentTimeMillis() - start) + " " + rtr);
//
//		start = System.currentTimeMillis();
//		for (int i = 0; i < count; i++) {
//			s = new BigInteger(130, random).toString(32).toUpperCase();
//			rtr = !s.equals("");
//		}
//		System.err.println("None toLowerCase "
//				+ (System.currentTimeMillis() - start) + " " + rtr);
//	}

	@Test
	public void testLowerUpperCase() {
		assertMatch("");
		assertMatch("aa");
		assertMatch("a111aa");
		assertMatch("a11*-11");
		assertMatch("a11-11*");
		assertMatch("aa");
		assertMatch("a111Aa");
		assertMatch("a11*-11");
		assertMatch("A11-11*");
		assertMatch("A11-11*");
		assertMatch("ABCDEFGHIJKLMNOPQRSTUVWXYZ");

		// E acute and e acute are not altered.
		assertEquals("\u00E9", toUpperCase("\u00E9"));
		assertEquals("\u00C9", toLowerCase("\u00C9"));

	}

}
