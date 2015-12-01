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
 * The Original Code is TestBitPostingIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.bit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.bit.BitPostingIndex;
import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.Posting;
import org.terrier.structures.postings.bit.BasicIterablePosting;
import org.terrier.tests.ApplicationSetupBasedTest;

public class TestBitPostingIndex extends ApplicationSetupBasedTest {
	
	@SuppressWarnings("unchecked")
	@Test public void testSingleEntrySinglePostingSingleFile() throws Exception
	{
		List<Posting> postings = new ArrayList<Posting>();
		postings.add(new BasicPostingImpl(1,1));
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writePostingsToFile(new Iterator[]{postings.iterator()}, pointerList);
		BitPostingIndex structure = new BitPostingIndex(filename, (byte) 1, BasicIterablePosting.class, "file", 0);
		PostingTestUtils.comparePostings(postings, structure.getPostings(pointerList.get(0)));
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testSingleEntryMultiplePostingsSingleFile() throws Exception
	{
		List<Posting> postings = new ArrayList<Posting>();
		postings.add(new BasicPostingImpl(1,1));
		postings.add(new BasicPostingImpl(2,2));
		postings.add(new BasicPostingImpl(1000,1000));
		postings.add(new BasicPostingImpl((int)5e6,(int)5e6));
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writePostingsToFile(new Iterator[]{postings.iterator()}, pointerList);
		BitPostingIndex structure = new BitPostingIndex(filename, (byte) 1, BasicIterablePosting.class, "file", 0);
		PostingTestUtils.comparePostings(postings, structure.getPostings(pointerList.get(0)));
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testMultipleEntryMultiplePostingSingleFile() throws Exception
	{
		List<Posting> postings1 = new ArrayList<Posting>();
		postings1.add(new BasicPostingImpl(1,1));
		postings1.add(new BasicPostingImpl(2,2));
		postings1.add(new BasicPostingImpl(1000,1000));
		
		List<Posting> postings2 = new ArrayList<Posting>();
		postings2.add(new BasicPostingImpl(1,4));
		postings2.add(new BasicPostingImpl(2,4));
		postings2.add(new BasicPostingImpl(1000,4));
		
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writePostingsToFile(new Iterator[]{postings1.iterator(), postings2.iterator()}, pointerList);
		BitPostingIndex structure = new BitPostingIndex(filename, (byte) 1, BasicIterablePosting.class, "file", 0);
		PostingTestUtils.comparePostings(postings1, structure.getPostings(pointerList.get(0)));
		PostingTestUtils.comparePostings(postings2, structure.getPostings(pointerList.get(1)));
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testMultipleEntrySinglePostingSingleFile() throws Exception
	{
		List<Posting> postings1 = new ArrayList<Posting>();
		postings1.add(new BasicPostingImpl(2,2));
		
		List<Posting> postings2 = new ArrayList<Posting>();
		postings2.add(new BasicPostingImpl(1000,4));
		
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writePostingsToFile(new Iterator[]{postings1.iterator(), postings2.iterator()}, pointerList);
		BitPostingIndex structure = new BitPostingIndex(filename, (byte) 1, BasicIterablePosting.class, "file", 0);
		PostingTestUtils.comparePostings(postings1, structure.getPostings(pointerList.get(0)));
		PostingTestUtils.comparePostings(postings2, structure.getPostings(pointerList.get(1)));
	}

}
