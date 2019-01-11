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
 * The Original Code is TestPostingStructures.java
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.bit;

import java.io.DataInput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.terrier.compression.bit.BitInputStream;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.BlockFieldPostingImpl;
import org.terrier.structures.postings.BlockPostingImpl;
import org.terrier.structures.postings.FieldPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.structures.postings.bit.BasicIterablePosting;
import org.terrier.structures.postings.bit.BlockFieldIterablePosting;
import org.terrier.structures.postings.bit.BlockIterablePosting;
import org.terrier.structures.postings.bit.FieldIterablePosting;
import org.terrier.tests.ApplicationSetupBasedTest;

/** Tests that the Posting structures behave as expected */
public class TestPostingStructures extends ApplicationSetupBasedTest {
	
	@SuppressWarnings("unchecked")
	@Test public void testSingleEntrySinglePosting() throws Exception
	{
		List<Posting> postings = new ArrayList<Posting>();
		postings.add(new BasicPostingImpl(1,1));
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		DataInput file = PostingTestUtils.writePostingsToData(new Iterator[]{postings.iterator()}, pointerList);
		BitInputStream bitIn = new BitInputStream(file);
		IterablePosting ip = new BasicIterablePosting(bitIn, postings.size(), null);
		PostingTestUtils.comparePostings(postings, ip);
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testSingleEntrySeveralPostings() throws Exception
	{
		List<Posting> postings = new ArrayList<Posting>();
		postings.add(new BasicPostingImpl(1,1));
		postings.add(new BasicPostingImpl(2,1));
		postings.add(new BasicPostingImpl(10,1));
		postings.add(new BasicPostingImpl(100,1));
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		DataInput file = PostingTestUtils.writePostingsToData(new Iterator[]{postings.iterator()}, pointerList);
		BitInputStream bitIn = new BitInputStream(file);
		IterablePosting ip = new BasicIterablePosting(bitIn, postings.size(), null);
		PostingTestUtils.comparePostings(postings, ip);
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testSingleEntrySeveralPostingsBlocks() throws Exception
	{
		List<Posting> postings = new ArrayList<Posting>();
		postings.add(new BlockPostingImpl(1,1, new int[]{5}));
		postings.add(new BlockPostingImpl(2,1, new int[]{5}));
		postings.add(new BlockPostingImpl(10,1, new int[]{5}));
		postings.add(new BlockPostingImpl(100,1, new int[]{5}));
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		DataInput file = PostingTestUtils.writeBlockPostingsToData(new Iterator[]{postings.iterator()}, pointerList);
		BitInputStream bitIn = new BitInputStream(file);
		IterablePosting ip = new BlockIterablePosting(bitIn, postings.size(), null);
		PostingTestUtils.comparePostings(postings, ip);
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testSingleEntrySeveralPostingsFields() throws Exception
	{
		List<Posting> postings = new ArrayList<Posting>();
		postings.add(new FieldPostingImpl(1,1, new int[]{5}));
		postings.add(new FieldPostingImpl(2,1, new int[]{5}));
		postings.add(new FieldPostingImpl(10,1, new int[]{5}));
		postings.add(new FieldPostingImpl(100,1, new int[]{5}));
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		DataInput file = PostingTestUtils.writeFieldPostingsToData(new Iterator[]{postings.iterator()}, pointerList);
		BitInputStream bitIn = new BitInputStream(file);
		IterablePosting ip = new FieldIterablePosting(bitIn, postings.size(), null, 1);
		PostingTestUtils.comparePostings(postings, ip);
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testSingleEntrySeveralPostingsBlocksFields() throws Exception
	{
		List<Posting> postings = new ArrayList<Posting>();
		postings.add(new BlockFieldPostingImpl(1,1, new int[]{5}, new int[]{5}));
		postings.add(new BlockFieldPostingImpl(2,1, new int[]{5}, new int[]{5}));
		postings.add(new BlockFieldPostingImpl(10,1, new int[]{5}, new int[]{5}));
		postings.add(new BlockFieldPostingImpl(100,1, new int[]{5}, new int[]{5}));
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		DataInput file = PostingTestUtils.writeBlockFieldPostingsToData(new Iterator[]{postings.iterator()}, pointerList);
		BitInputStream bitIn = new BitInputStream(file);
		IterablePosting ip = new BlockFieldIterablePosting(bitIn, postings.size(), null, 1);
		PostingTestUtils.comparePostings(postings, ip);
	}
	
	
	
}
