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
 * The Original is in 'TestFieldORIterablePosting.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.structures.postings;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestFieldORIterablePosting {

	protected IterablePosting joinPostings(IterablePosting[] ips)
			throws Exception
	{
		return new FieldORIterablePosting(ips);
	}

	
	@Test public void testFieldsManySomeOverlap() throws Exception
	{
		IterablePosting[] ips = new IterablePosting[]{
				new ArrayOfFieldIterablePosting(new int[]{0,1}, new int[]{1,1}, new int[]{4,5}, 
					new int[][]{new int[]{0,1}, new int[]{1,0}}, 
					new int[][]{new int[]{3,1}, new int[]{2,3}}),
				new ArrayOfFieldIterablePosting(new int[]{1,2}, new int[]{2,2}, new int[]{5,6},
					new int[][]{new int[]{2,0}, new int[]{1,1}}, 
					new int[][]{new int[]{2,3}, new int[]{3,3}}),
		};
		IterablePosting joined = joinPostings(ips);
		FieldPosting fp = (FieldPosting)joined;
		assertEquals(0, joined.next());
		assertEquals(0, joined.getId());
		assertEquals(1, joined.getFrequency());
		assertEquals(4, joined.getDocumentLength());
		assertEquals(0, fp.getFieldFrequencies()[0]);
		assertEquals(1, fp.getFieldFrequencies()[1]);
		assertEquals(3, fp.getFieldLengths()[0]);
		assertEquals(1, fp.getFieldLengths()[1]);
		assertEquals(1, joined.next());
		assertEquals(1, joined.getId());
		assertEquals(3, joined.getFrequency());
		assertEquals(5, joined.getDocumentLength());
		assertEquals(3, fp.getFieldFrequencies()[0]);
		assertEquals(0, fp.getFieldFrequencies()[1]);
		assertEquals(2, fp.getFieldLengths()[0]);
		assertEquals(3, fp.getFieldLengths()[1]);
		assertEquals(2, joined.next());
		assertEquals(2, joined.getId());
		assertEquals(2, joined.getFrequency());
		assertEquals(6, joined.getDocumentLength());
		assertEquals(1, fp.getFieldFrequencies()[0]);
		assertEquals(1, fp.getFieldFrequencies()[1]);
		assertEquals(3, fp.getFieldLengths()[0]);
		assertEquals(3, fp.getFieldLengths()[1]);
		assertEquals(IterablePosting.EOL, joined.next());			
	}
	
}
