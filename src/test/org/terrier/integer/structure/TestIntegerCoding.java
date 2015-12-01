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
 * The Original Code is TestIntegerCoding.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */

package org.terrier.integer.structure;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.terrier.compression.bit.BitIn;
import org.terrier.compression.bit.BitInputStream;
import org.terrier.compression.integer.ByteFileBuffered;
import org.terrier.compression.integer.ByteIn;
import org.terrier.compression.integer.codec.GammaCodec;
import org.terrier.compression.integer.codec.IntegerCodec;
import org.terrier.compression.integer.codec.UnaryCodec;
import org.terrier.compression.integer.codec.VIntCodec;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.bit.PostingTestUtils;
import org.terrier.structures.integer.IntegerCodingPostingOutputStream;
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
import org.terrier.structures.postings.integer.IntegerCodingIterablePosting;

@SuppressWarnings({"rawtypes", "unchecked",  "deprecation"})
public class TestIntegerCoding {
	
	private int chunksize = 3;

	@Test
	public void testBasic() throws Exception {
		
		List<List<Posting>> postings = new ArrayList<List<Posting>>(); 
		List<Posting> l = new ArrayList<Posting>();
		postings.add(l);
		l.add(new BasicPostingImpl(2,1));
		l.add(new BasicPostingImpl(10,1));
		l.add(new BasicPostingImpl(100,1));
		l = new ArrayList<Posting>();
		postings.add(l);
		l.add(new BasicPostingImpl(0,1));
		l.add(new BasicPostingImpl(2,1));
		l.add(new BasicPostingImpl(5,1));
		l.add(new BasicPostingImpl(7,3));
		l = new ArrayList<Posting>();
		postings.add(l);
		l.add(new BasicPostingImpl(15000,1));		
		
		List<Iterator<Posting>> iterators = new ArrayList<Iterator<Posting>>();
		for (List<Posting> x : postings) iterators.add(x.iterator());
		Iterator[] iterarray = Arrays.copyOf(iterators.toArray(), iterators.toArray().length, Iterator[].class);
				
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();		
		String filename = PostingTestUtils.writePostingsToFile(iterarray, pointerList);
		BitIn bitIn = new BitInputStream(filename);
		
		IntegerCodec idsCodec, tfsCodec;
		idsCodec = new GammaCodec(); 
		tfsCodec = new UnaryCodec();
		
		
		File tmpFile = File.createTempFile("tmp", BitIn.USUAL_EXTENSION);
		IntegerCodingPostingOutputStream icpw = 
				new IntegerCodingPostingOutputStream(tmpFile.toString(), chunksize, 0, 0, 0, idsCodec, tfsCodec, null, null);
		for (BitIndexPointer p : pointerList) {
			
			IterablePosting ip1 = new BasicIterablePosting(bitIn, p.getNumberOfEntries(), null);
			BitIndexPointer bip = icpw.writePostings(ip1);
			System.err.println(bip.toString());
		}
		icpw.close();
		
		ByteFileBuffered bit = new ByteFileBuffered(tmpFile.toString());
		ByteIn input = bit.readReset(0);
		Iterator<List<Posting>> iterator = postings.iterator();
		for (BitIndexPointer p : pointerList) {
						
			IntegerCodingIterablePosting icip = 
					new IntegerCodingIterablePosting(input, p.getNumberOfEntries(), null, chunksize, 0, 0, idsCodec, tfsCodec, null, null);
			PostingTestUtils.comparePostings(iterator.next(), icip);
		}
		bit.close();
	}
	
	
	@Test
	public void testBlock() throws Exception {
		
		List<List<Posting>> postings = new ArrayList<List<Posting>>(); 
		List<Posting> l = new ArrayList<Posting>();
		postings.add(l);
		int[] a = {0};
		l.add(new BlockPostingImpl(2,1, a));
		int[] b = {1};
		l.add(new BlockPostingImpl(10,1, b));
		int[] c = {5};
		l.add(new BlockPostingImpl(100,1, c));
		l = new ArrayList<Posting>();
		postings.add(l);
		int[] a1 = {0};
		l.add(new BlockPostingImpl(1,1, a1));
		int[] b1 = {2};
		l.add(new BlockPostingImpl(2,1, b1));
		int[] c1 = {4};
		l.add(new BlockPostingImpl(5,1, c1));
		int[] d1 = {1, 2, 3};
		l.add(new BlockPostingImpl(7,3, d1));
		l = new ArrayList<Posting>();
		postings.add(l);
		l.add(new BlockPostingImpl(15000,1, a1));		
		
		
		
		List<Iterator<Posting>> iterators = new ArrayList<Iterator<Posting>>();
		for (List<Posting> x : postings) iterators.add(x.iterator());
		Iterator[] iterarray = Arrays.copyOf(iterators.toArray(), iterators.toArray().length, Iterator[].class);
		
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writeBlockPostingsToFile(iterarray, pointerList);
		BitInputStream bitIn = new BitInputStream(filename);
		
		IntegerCodec idsCodec, tfsCodec, blocksCodec;
		idsCodec = new GammaCodec(); 
		tfsCodec = new UnaryCodec();
		blocksCodec = new GammaCodec();
		
		
		File tmpFile = File.createTempFile("tmp", BitIn.USUAL_EXTENSION);
		IntegerCodingPostingOutputStream icpw = 
				new IntegerCodingPostingOutputStream(tmpFile.toString(), chunksize, 0, 1, 10000, idsCodec, tfsCodec, null, blocksCodec);
		for (BitIndexPointer p : pointerList) {
			
			IterablePosting ip1 = new BlockIterablePosting(bitIn, p.getNumberOfEntries(), null);
			BitIndexPointer bip = icpw.writePostings(ip1);
			System.err.println(bip.toString());
		}
		icpw.close();
		
		ByteFileBuffered bb = new ByteFileBuffered(tmpFile.toString());
		ByteIn input = bb.readReset(0);
		Iterator<List<Posting>> iterator = postings.iterator();
		for (BitIndexPointer p : pointerList) {
						
			IntegerCodingIterablePosting icip = 
					new IntegerCodingIterablePosting(input, p.getNumberOfEntries(), null, chunksize, 0, 1, idsCodec, tfsCodec, null, blocksCodec);
			PostingTestUtils.compareBlockPostings(iterator.next(), icip);
		}		
		bb.close();
	}	
	
	@Test
	public void testField() throws Exception {
		
		List<List<Posting>> postings = new ArrayList<List<Posting>>(); 
		List<Posting> l = new ArrayList<Posting>();
		postings.add(l);
		int[] a = {0, 1};
		l.add(new FieldPostingImpl(2,1, a));
		int[] b = {1, 0};
		l.add(new FieldPostingImpl(10,1, b));
		int[] c = {0, 1};
		l.add(new FieldPostingImpl(100,1, c));
		l = new ArrayList<Posting>();
		postings.add(l);
		int[] a1 = {0, 1};
		l.add(new FieldPostingImpl(1,1, a1));
		int[] b1 = {1, 0};
		l.add(new FieldPostingImpl(2,1, b1));
		int[] c1 = {1, 0};
		l.add(new FieldPostingImpl(5,1, c1));
		int[] d1 = {1, 2};
		l.add(new FieldPostingImpl(7,3, d1));
		l = new ArrayList<Posting>();
		postings.add(l);
		l.add(new FieldPostingImpl(15000,1, a1));		
		
		
		List<Iterator<Posting>> iterators = new ArrayList<Iterator<Posting>>();
		for (List<Posting> x : postings) iterators.add(x.iterator());
		Iterator[] iterarray = Arrays.copyOf(iterators.toArray(), iterators.toArray().length, Iterator[].class);
				
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writeFieldPostingsToFile(iterarray, pointerList);
		BitInputStream bitIn = new BitInputStream(filename);
		
		IntegerCodec idsCodec, tfsCodec, fieldsCodec;
		idsCodec = new GammaCodec(); 
		tfsCodec = new UnaryCodec();
		fieldsCodec = new UnaryCodec(); 
		
		
		File tmpFile = File.createTempFile("tmp", BitIn.USUAL_EXTENSION);
		IntegerCodingPostingOutputStream icpw = 
				new IntegerCodingPostingOutputStream(tmpFile.toString(), chunksize, 2, 0, 0, idsCodec, tfsCodec, fieldsCodec, null);
		for (BitIndexPointer p : pointerList) {
			
			IterablePosting ip1 = new FieldIterablePosting(bitIn, p.getNumberOfEntries(), null, 2);
			BitIndexPointer bip = icpw.writePostings(ip1);
			System.err.println(bip.toString());
		}
		icpw.close();
		
		ByteFileBuffered bb = new ByteFileBuffered(tmpFile.toString());
		ByteIn input = bb.readReset(0);
		Iterator<List<Posting>> iterator = postings.iterator();
		for (BitIndexPointer p : pointerList) {
						
			IntegerCodingIterablePosting icip = 
					new IntegerCodingIterablePosting(input, p.getNumberOfEntries(), null, chunksize, 2, 0, idsCodec, tfsCodec, fieldsCodec, null);
			PostingTestUtils.compareFieldPostings(iterator.next(), icip);
		}		
		bb.close();
	}	
	
	@Test
	public void testFieldAndBlock() throws Exception {
		
		List<List<Posting>> postings = new ArrayList<List<Posting>>(); 
		List<Posting> l = new ArrayList<Posting>();
		postings.add(l);
		int[] pos = {1};
		int[] field = {1, 0};
		l.add(new BlockFieldPostingImpl(2,1, pos, field));
		int[] pos1 = {1};
		int[] field1 = {1, 0};
		l.add(new BlockFieldPostingImpl(10,1, pos1, field1));
		int[] pos2 = {5};
		int[] field2 = {0, 1};
		l.add(new BlockFieldPostingImpl(100,1, pos2, field2));
		l = new ArrayList<Posting>();
		postings.add(l);
		int[] pos3 = {4};
		int[] field3 = {0, 1};
		l.add(new BlockFieldPostingImpl(1,1, pos3, field3));
		int[] pos4 = {80};
		int[] field4 = {0, 1};
		l.add(new BlockFieldPostingImpl(2,1, pos4, field4));
		int[] pos5 = {1};
		int[] field5 = {1, 0};
		l.add(new BlockFieldPostingImpl(5,1, pos5, field5));
		int[] pos6 = {1, 50, 15001};
		int[] field6 = {1, 2};
		l.add(new BlockFieldPostingImpl(7,3, pos6, field6));
		l = new ArrayList<Posting>();
		postings.add(l);
		int[] pos7 = {9999};
		int[] field7 = {0, 1};
		l.add(new BlockFieldPostingImpl(15000,1, pos7, field7));		
		
		
		List<Iterator<Posting>> iterators = new ArrayList<Iterator<Posting>>();
		for (List<Posting> x : postings) iterators.add(x.iterator());
		Iterator[] iterarray = Arrays.copyOf(iterators.toArray(), iterators.toArray().length, Iterator[].class);
				
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writeBlockFieldPostingsToFile(iterarray, pointerList);
		BitInputStream bitIn = new BitInputStream(filename);
		
		IntegerCodec idsCodec, tfsCodec, fieldsCodec, blocksCodec;
		idsCodec = new GammaCodec(); 
		tfsCodec = new UnaryCodec();
		fieldsCodec = new UnaryCodec(); 
		blocksCodec = new GammaCodec();
		
		
		File tmpFile = File.createTempFile("tmp", BitIn.USUAL_EXTENSION);
		IntegerCodingPostingOutputStream icpw = 
				new IntegerCodingPostingOutputStream(tmpFile.toString(), chunksize, 2, 1, 10000, idsCodec, tfsCodec, fieldsCodec, blocksCodec);
		for (BitIndexPointer p : pointerList) {
			
			IterablePosting ip1 = new BlockFieldIterablePosting(bitIn, p.getNumberOfEntries(), null, 2);
			BitIndexPointer bip = icpw.writePostings(ip1);
			System.err.println(bip.toString());
		}
		icpw.close();
		
		ByteFileBuffered bfb = new ByteFileBuffered(tmpFile.toString());
		ByteIn input = bfb.readReset(0);
		Iterator<List<Posting>> iterator = postings.iterator();
		for (BitIndexPointer p : pointerList) {
						
			IntegerCodingIterablePosting icip = 
					new IntegerCodingIterablePosting(input, p.getNumberOfEntries(), null, chunksize, 2, 1, idsCodec, tfsCodec, fieldsCodec, blocksCodec);
			PostingTestUtils.compareBlockFieldPostings(iterator.next(), icip);
		}
		bfb.close();
		bitIn.close();
	}
	
	@Test
	public void testFieldAndBlockNotPos() throws Exception {
		
		List<List<Posting>> postings = new ArrayList<List<Posting>>(); 
		List<Posting> l = new ArrayList<Posting>();
		postings.add(l);
		int[] pos = {1};
		int[] field = {1, 0};
		l.add(new BlockFieldPostingImpl(2,1, pos, field));
		int[] pos1 = {1};
		int[] field1 = {1, 0};
		l.add(new BlockFieldPostingImpl(10,1, pos1, field1));
		int[] pos2 = {5};
		int[] field2 = {0, 1};
		l.add(new BlockFieldPostingImpl(100,1, pos2, field2));
		l = new ArrayList<Posting>();
		postings.add(l);
		int[] pos3 = {4};
		int[] field3 = {0, 1};
		l.add(new BlockFieldPostingImpl(1,1, pos3, field3));
		int[] pos4 = {80};
		int[] field4 = {0, 1};
		l.add(new BlockFieldPostingImpl(2,1, pos4, field4));
		int[] pos5 = {1};
		int[] field5 = {1, 0};
		l.add(new BlockFieldPostingImpl(5,1, pos5, field5));
		int[] pos6 = {40};
		int[] field6 = {1, 2};
		l.add(new BlockFieldPostingImpl(7,3, pos6, field6));
		l = new ArrayList<Posting>();
		postings.add(l);
		int[] pos7 = {9999};
		int[] field7 = {0, 1};
		l.add(new BlockFieldPostingImpl(15000,1, pos7, field7));		
		
		
		List<Iterator<Posting>> iterators = new ArrayList<Iterator<Posting>>();
		for (List<Posting> x : postings) iterators.add(x.iterator());
		Iterator[] iterarray = Arrays.copyOf(iterators.toArray(), iterators.toArray().length, Iterator[].class);
				
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writeBlockFieldPostingsToFile(iterarray, pointerList);
		BitInputStream bitIn = new BitInputStream(filename);
		
		IntegerCodec idsCodec, tfsCodec, fieldsCodec, blocksCodec;
		idsCodec = new GammaCodec(); 
		tfsCodec = new UnaryCodec();
		fieldsCodec = new UnaryCodec(); 
		blocksCodec = new GammaCodec();
		
		
		File tmpFile = File.createTempFile("tmp", BitIn.USUAL_EXTENSION);
		IntegerCodingPostingOutputStream icpw = 
				new IntegerCodingPostingOutputStream(tmpFile.toString(), chunksize, 2, 2, 10000, idsCodec, tfsCodec, fieldsCodec, blocksCodec);
		for (BitIndexPointer p : pointerList) {
			
			IterablePosting ip1 = new BlockFieldIterablePosting(bitIn, p.getNumberOfEntries(), null, 2);
			BitIndexPointer bip = icpw.writePostings(ip1);
			System.err.println(bip.toString());
		}
		icpw.close();
		
		ByteFileBuffered bfb = new ByteFileBuffered(tmpFile.toString());
		ByteIn input = bfb.readReset(0);
		Iterator<List<Posting>> iterator = postings.iterator();
		for (BitIndexPointer p : pointerList) {
						
			IntegerCodingIterablePosting icip = 
					new IntegerCodingIterablePosting(input, p.getNumberOfEntries(), null, chunksize, 2, 2, idsCodec, tfsCodec, fieldsCodec, blocksCodec);
			PostingTestUtils.compareBlockFieldPostings(iterator.next(), icip);
		}
		bfb.close();
		bitIn.close();
	}	
	
	@Test
	public void testNextWithTarget() throws Exception {
		
		List<List<Posting>> postings = new ArrayList<List<Posting>>(); 
		List<Posting> l = new ArrayList<Posting>();
		postings.add(l);
		int[] pos = {1};
		int[] field = {1, 0};
		l.add(new BlockFieldPostingImpl(2,1, pos, field));
		int[] pos1 = {1};
		int[] field1 = {1, 0};
		l.add(new BlockFieldPostingImpl(10,1, pos1, field1));
		int[] pos2 = {5};
		int[] field2 = {0, 1};
		l.add(new BlockFieldPostingImpl(100,1, pos2, field2));
		
		l = new ArrayList<Posting>();
		postings.add(l);
		int[] pos3 = {4};
		int[] field3 = {0, 1};
		l.add(new BlockFieldPostingImpl(1,1, pos3, field3));
		int[] pos4 = {80};
		int[] field4 = {0, 1};
		l.add(new BlockFieldPostingImpl(2,1, pos4, field4));
		int[] pos5 = {1};
		int[] field5 = {1, 0};
		l.add(new BlockFieldPostingImpl(5,1, pos5, field5));
		int[] pos6 = {1, 50, 15001};
		int[] field6 = {1, 2};
		l.add(new BlockFieldPostingImpl(7,3, pos6, field6));
		
		l = new ArrayList<Posting>();
		postings.add(l);
		int[] pos7 = {9999};
		int[] field7 = {0, 1};
		l.add(new BlockFieldPostingImpl(15000,1, pos7, field7));		
		
		
		List<Iterator<Posting>> iterators = new ArrayList<Iterator<Posting>>();
		for (List<Posting> x : postings) iterators.add(x.iterator());
		Iterator[] iterarray = Arrays.copyOf(iterators.toArray(), iterators.toArray().length, Iterator[].class);
				
		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writeBlockFieldPostingsToFile(iterarray, pointerList);
		BitInputStream bitIn = new BitInputStream(filename);
		
		IntegerCodec idsCodec, tfsCodec, fieldsCodec, blocksCodec;
		idsCodec = new VIntCodec(); 
		tfsCodec = new VIntCodec();
		fieldsCodec = new VIntCodec(); 
		blocksCodec = new VIntCodec();
		
		
		File tmpFile = File.createTempFile("tmp", BitIn.USUAL_EXTENSION);
		IntegerCodingPostingOutputStream icpw = 
				new IntegerCodingPostingOutputStream(tmpFile.toString(), chunksize, 2, 1, 10000, idsCodec, tfsCodec, fieldsCodec, blocksCodec);
		for (BitIndexPointer p : pointerList) {
			
			IterablePosting ip1 = new BlockFieldIterablePosting(bitIn, p.getNumberOfEntries(), null, 2);
			BitIndexPointer bip = icpw.writePostings(ip1);
			System.err.println(bip.toString());
		}
		icpw.close();
		
		ByteFileBuffered bb = new ByteFileBuffered(tmpFile.toString());
		ByteIn input = bb.readReset(0);
		Iterator<BitIndexPointer> iterator = pointerList.iterator();		
		
		IterablePosting icip = 
				new IntegerCodingIterablePosting(input, iterator.next().getNumberOfEntries(), null, chunksize, 2, 1, idsCodec, tfsCodec, fieldsCodec, blocksCodec);
		icip.next(8);
		assertEquals(10, icip.getId());
		icip.next(11);
		assertEquals(100, icip.getId());
		icip.close();
		
		icip = new IntegerCodingIterablePosting(input, iterator.next().getNumberOfEntries(), null, chunksize, 2, 1, idsCodec, tfsCodec, fieldsCodec, blocksCodec);
		icip.next(4);
		assertEquals(5, icip.getId());
		bitIn.close();
		bb.close();
		icip.close();
	}	
}
