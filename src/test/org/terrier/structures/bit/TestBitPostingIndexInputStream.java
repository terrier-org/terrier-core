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
 * The Original Code is TestBitPostingIndexInputStream.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.bit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.bit.BitPostingIndexInputStream;
import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.structures.postings.bit.BasicIterablePosting;
import org.terrier.structures.postings.bit.BasicIterablePostingDocidOnly;
import org.terrier.tests.ApplicationSetupBasedTest;

public class TestBitPostingIndexInputStream extends ApplicationSetupBasedTest {

	
	@SuppressWarnings("unchecked")
	@Test public void testSingleEntrySinglePostingSingleFile() throws Exception
	{
		List<Posting> postings = new ArrayList<Posting>();
		postings.add(new BasicPostingImpl(1,1));
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writePostingsToFile(new Iterator[]{postings.iterator()}, pointerList);
		BitPostingIndexInputStream structure = new BitPostingIndexInputStream(filename, (byte)1, pointerList.iterator(), BasicIterablePosting.class, 0);
		assertTrue(structure.hasNext());
		PostingTestUtils.assertEqualsBitFilePosition(pointerList.get(0), structure.getPos());
		PostingTestUtils.comparePostings(postings, structure.next());
		assertFalse(structure.hasNext());
		structure.close();
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testSingleEntryMultiplePostingSingleFile() throws Exception
	{
		List<Posting> postings = new ArrayList<Posting>();
		postings.add(new BasicPostingImpl(1,1));
		postings.add(new BasicPostingImpl(1000,1000));
		postings.add(new BasicPostingImpl(1001,1000));
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writePostingsToFile(new Iterator[]{postings.iterator()}, pointerList);
		BitPostingIndexInputStream structure = new BitPostingIndexInputStream(filename, (byte)1, pointerList.iterator(), BasicIterablePosting.class, 0);
		assertTrue(structure.hasNext());
		PostingTestUtils.comparePostings(postings, structure.next());
		assertFalse(structure.hasNext());
		structure.close();
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testMultipleEntryMultiplePostingSingleFile() throws Exception
	{
		List<Posting> postings1 = new ArrayList<Posting>();
		postings1.add(new BasicPostingImpl(1,1));
		postings1.add(new BasicPostingImpl(1000,1000));
		postings1.add(new BasicPostingImpl(1001,1000));
		
		List<Posting> postings2 = new ArrayList<Posting>();
		postings2.add(new BasicPostingImpl(2,20));
		postings2.add(new BasicPostingImpl(2000,1000));
		postings2.add(new BasicPostingImpl(2001,1000));
		
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writePostingsToFile(new Iterator[]{postings1.iterator(), postings2.iterator()}, pointerList);
		BitPostingIndexInputStream structure = new BitPostingIndexInputStream(filename, (byte)1, pointerList.iterator(), BasicIterablePosting.class, 0);
		assertTrue(structure.hasNext());
		PostingTestUtils.assertEqualsBitFilePosition(pointerList.get(0), structure.getPos());
		PostingTestUtils.comparePostings(postings1, structure.next());
		assertTrue(structure.hasNext());
		PostingTestUtils.assertEqualsBitFilePosition(pointerList.get(1), structure.getPos());
		PostingTestUtils.comparePostings(postings2, structure.next());
		assertFalse(structure.hasNext());
		structure.close();
		
		structure = new BitPostingIndexInputStream(filename, (byte)1, PostingTestUtils.makeSkippable(pointerList.iterator()), BasicIterablePosting.class, 0);
		structure.skip(1);
		assertTrue(structure.hasNext());
		IterablePosting ip = structure.next();
		PostingTestUtils.assertEqualsBitFilePosition(pointerList.get(1), structure.getPos());
		PostingTestUtils.comparePostings(postings2, ip);
		assertFalse(structure.hasNext());
		structure.close();
	}

	@Test public void testSpecific1MultipleEntryMultiplePostingSingleFile() throws Exception
	{
		doSpecific1MultipleEntryMultiplePostingSingleFile(false);
	}
	
	@Test public void testSpecific1MultipleEntryMultiplePostingSingleFileDocidsOnly() throws Exception
	{
		doSpecific1MultipleEntryMultiplePostingSingleFile(true);
	}
	
	
	@SuppressWarnings("unchecked")
	void doSpecific1MultipleEntryMultiplePostingSingleFile (boolean docids) throws Exception
	{
		List<Posting> postings0 = new ArrayList<Posting>();
		postings0.add(new BasicPostingImpl(100,1));
		postings0.add(new BasicPostingImpl(200,1000));
		postings0.add(new BasicPostingImpl(300,1000));
		postings0.add(new BasicPostingImpl(400,1000));
		
		List<Posting> postings1 = new ArrayList<Posting>();
		postings1.add(new BasicPostingImpl(0,1));
		postings1.add(new BasicPostingImpl(1,1000));
		postings1.add(new BasicPostingImpl(2,1000));
		postings1.add(new BasicPostingImpl(4,1000));
		postings1.add(new BasicPostingImpl(8,1000));
		
		List<Posting> postings2 = new ArrayList<Posting>();
		postings2.add(new BasicPostingImpl(0,20));
		postings2.add(new BasicPostingImpl(8,1000));
		postings2.add(new BasicPostingImpl(10,1000));
		
		IterablePosting ip;
		final List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		final String filename = docids
			? PostingTestUtils.writePostingsToFileDocidOnly(new Iterator[]{postings0.iterator(), postings1.iterator(), postings2.iterator()}, pointerList)
			: PostingTestUtils.writePostingsToFile(new Iterator[]{postings0.iterator(), postings1.iterator(), postings2.iterator()}, pointerList);
		final Class<? extends IterablePosting> postingIteratorClass = docids ? BasicIterablePostingDocidOnly.class : BasicIterablePosting.class;
		BitPostingIndexInputStream structure = new BitPostingIndexInputStream(filename, (byte)1, 
				pointerList.iterator(), 
				postingIteratorClass, 
				0);
		
		
		//the following checks correctness via various ways of skipping entries		
		
		//no skip
		assertTrue(structure.hasNext());
		ip = structure.next();
		PostingTestUtils.assertEqualsBitFilePosition(pointerList.get(0), structure.getPos());
		//System.err.println("0 " + structure.getPos());
		PostingTestUtils.comparePostings(postings0, ip, docids);
		assertTrue(structure.hasNext());
		ip = structure.next();
		//System.err.println("1 " + structure.getPos());
		PostingTestUtils.assertEqualsBitFilePosition(pointerList.get(1), structure.getPos());
		PostingTestUtils.comparePostings(postings1, ip, docids);
		assertTrue(structure.hasNext());
		ip = structure.next();
		//System.err.println("2 " + structure.getPos());
		PostingTestUtils.assertEqualsBitFilePosition(pointerList.get(2), structure.getPos());
		PostingTestUtils.comparePostings(postings2, ip, docids);
		assertFalse(structure.hasNext());
		structure.close();
		
		//skip 1
		structure = new BitPostingIndexInputStream(filename, (byte)1, PostingTestUtils.makeSkippable(pointerList.iterator()), postingIteratorClass, 0);
		structure.skip(1);
		assertTrue(structure.hasNext());
		ip = structure.next();
		//System.err.println("1 " + structure.getPos());
		PostingTestUtils.assertEqualsBitFilePosition(pointerList.get(1), structure.getPos());
		PostingTestUtils.comparePostings(postings1, ip, docids);
		assertTrue(structure.hasNext());
		ip = structure.next();
		//System.err.println("2 " + structure.getPos());
		PostingTestUtils.assertEqualsBitFilePosition(pointerList.get(2), structure.getPos());
		PostingTestUtils.comparePostings(postings2, ip, docids);
		assertFalse(structure.hasNext());
		structure.close();
		
		//skip 2
		structure = new BitPostingIndexInputStream(filename, (byte)1, PostingTestUtils.makeSkippable(pointerList.iterator()), postingIteratorClass, 0);
		structure.skip(2);
		assertTrue(structure.hasNext());
		ip = structure.next();
		PostingTestUtils.assertEqualsBitFilePosition(pointerList.get(2), structure.getPos());
		PostingTestUtils.comparePostings(postings2, ip, docids);
		assertFalse(structure.hasNext());
		structure.close();
		
		//skip middle
		structure = new BitPostingIndexInputStream(filename, (byte)1, PostingTestUtils.makeSkippable(pointerList.iterator()), postingIteratorClass, 0);
		assertTrue(structure.hasNext());
		ip = structure.next();
		PostingTestUtils.assertEqualsBitFilePosition(pointerList.get(0), structure.getPos());
		PostingTestUtils.comparePostings(postings0, ip, docids);
		structure.skip(1);
		assertTrue(structure.hasNext());
		ip = structure.next();
		PostingTestUtils.assertEqualsBitFilePosition(pointerList.get(2), structure.getPos());
		PostingTestUtils.comparePostings(postings2, ip, docids);
		assertFalse(structure.hasNext());
		structure.close();		
	}
	
	
}
