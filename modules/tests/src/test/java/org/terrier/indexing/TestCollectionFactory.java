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
 * The Original Code is TestCollectionFactory.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.indexing;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestCollectionFactory {

	@Test public void testSplitList()
	{
		assertEquals(1, CollectionFactory.splitList(makeList(1), 1).size());
		assertEquals(1, CollectionFactory.splitList(makeList(2), 1).size());
		assertEquals(1, CollectionFactory.splitList(makeList(1), 2).size());
		assertEquals(2, CollectionFactory.splitList(makeList(2), 2).size());
		
		assertEquals(2, CollectionFactory.splitList(makeList(10), 2).size());
		assertEquals(3, CollectionFactory.splitList(makeList(10), 3).size());
		
	}
	
	List<Object> makeList(int size) {
		List<Object> l = new ArrayList<Object>(size);
		for(int i=0;i<size;i++)
			l.add(new Object());
		return l;
	}
	
}
