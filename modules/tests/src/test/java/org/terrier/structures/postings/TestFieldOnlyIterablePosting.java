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
 * The Original Code is TestFieldOnlyIterablePosting.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures.postings;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestFieldOnlyIterablePosting {

	@Test public void doTest() throws Exception
	{
		ArrayOfFieldIterablePosting fips = getFips();
		IterablePosting ip1 = new FieldOnlyIterablePosting(fips, 0);
		assertEquals(1, ip1.next());
		assertEquals(1, ip1.getFrequency());
		assertEquals(2, ip1.getDocumentLength());
		
		assertEquals(2, ip1.next());
		assertEquals(8, ip1.getFrequency());
		assertEquals(3, ip1.getDocumentLength());
		assertEquals(IterablePosting.EOL, ip1.next());
		assertEquals(IterablePosting.EOL, ip1.getId());
		ip1.close();
	}

	protected ArrayOfFieldIterablePosting getFips() {
		return new ArrayOfFieldIterablePosting(
				new int[]{0,1,2},
				new int[]{0,0,0}, //doesnt matter,
				new int[]{1,2,3},
				new int[][]{ new int[]{0,1},  new int[]{1,9},  new int[]{8,8}},//tff
				new int[][]{ new int[]{1,1}, new int[]{2,2}, new int[]{3,3}});//lf
	}
	
}
