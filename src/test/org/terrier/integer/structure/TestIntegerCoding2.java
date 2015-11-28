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
 * The Original Code is TestIntegerCoding2.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */

package org.terrier.integer.structure;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.terrier.compression.bit.BitIn;
import org.terrier.compression.bit.BitInputStream;
import org.terrier.compression.integer.ByteFileBuffered;
import org.terrier.compression.integer.ByteIn;
import org.terrier.compression.integer.codec.GammaCodec;
import org.terrier.compression.integer.codec.IntegerCodec;
import org.terrier.compression.integer.codec.LemireCodec;
import org.terrier.compression.integer.codec.UnaryCodec;
import org.terrier.compression.integer.codec.VIntCodec;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.bit.PostingTestUtils;
import org.terrier.structures.integer.IntegerCodingPostingOutputStream;
import org.terrier.structures.postings.BlockFieldPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.structures.postings.bit.BlockFieldIterablePosting;
import org.terrier.structures.postings.integer.IntegerCodingIterablePosting;

@RunWith(Parameterized.class)
@SuppressWarnings({"rawtypes", "unchecked",  "deprecation"})
public class TestIntegerCoding2 {

	protected int numPostings = 1000;
	protected int chunksize = 256;
	protected int numFields = 4;

	protected List<List<Posting>> postings;
	private IntegerCodec ids;
	private IntegerCodec tfs;
	private IntegerCodec blks;
	private IntegerCodec ffs;

	@Parameters
	public static Collection<String[]> data() {

		String[][] codecs = { {"gamma"}, {"unary"}, {"vint"}, {"s16"}, {"fastpfor_vint"},
				{"newpfd_vint"}, {"optpfd_vint"}, {"kamikaze_vint"}, {"fastpfor_s16"},
				{"newpfd_s16"}, {"optpfd_s16"}, {"kamikaze_s16"} };

		return Arrays.asList(codecs);
	}

	public TestIntegerCoding2(String codec) throws Exception {
		
		Properties prop = null;
		IntegerCodec[] ic = new IntegerCodec[4];

		for (int n = 0; n < 4; n++) {

			if ("gamma".equals(codec)){				
				ic[n] = new GammaCodec();
			}
			if ("unary".equals(codec)){	
				ic[n] = new UnaryCodec();
			}
			if ("vint".equals(codec)){	
				ic[n] = new VIntCodec();
			}
			if ("s16".equals(codec)){	
				prop = new Properties();
				//prop.setProperty(".integercodec-class",
				//		"me.lemire.integercompression.Simple16");
				ic[n] = new LemireCodec(new String[]{"Simple16"});
			}
			if ("fastpfor_vint".equals(codec)){
				prop = new Properties();
				//prop.setProperty(".integercodec-class",
				//		"me.lemire.integercompression.FastPFOR");
				ic[n] = new LemireCodec(new String[]{"Composition", "FastPFOR", "VariableByte"});
			}
			if ("newpfd_vint".equals(codec)){
			//case "newpfd_vint":
				prop = new Properties();
				//prop.setProperty(".integercodec-class",
				//		"me.lemire.integercompression.FastPFOR");
				ic[n] = new LemireCodec(new String[]{"Composition", "NewPFD", "VariableByte"});
			}
			if ("optpfd_vint".equals(codec)){
			//case "optpfd_vint":
				prop = new Properties();
				//prop.setProperty(".integercodec-class",
				//		"me.lemire.integercompression.FastPFOR");
				ic[n] = new LemireCodec(new String[]{"Composition", "OptPFD", "VariableByte"});
			}
			if ("kamikaze_vint".equals(codec)){
			//case "kamikaze_vint":
				prop = new Properties();
				prop.setProperty(".integercodec-class",
						"me.lemire.integercompression.FastPFOR");
				ic[n] = new LemireCodec(new String[]{"Composition", "Kamikaze", "VariableByte"});
			}
			if ("fastpfor_s16".equals(codec)){
			//case "fastpfor_s16":
				prop = new Properties();
				prop.setProperty(".integercodec-class",
						"me.lemire.integercompression.FastPFOR");
				ic[n] = new LemireCodec(new String[]{"Composition", "FastPFOR", "Simple16"});
			}
			if ("newpfd_s16".equals(codec)){
			//case "newpfd_s16":
				prop = new Properties();
				//prop.setProperty(".integercodec-class",
				//		"me.lemire.integercompression.FastPFOR");
				ic[n] = new LemireCodec(new String[]{"Composition", "NewPFD", "Simple16"});
			}
			if ("optpfd_s16".equals(codec)){
			//case "optpfd_s16":
				prop = new Properties();
				//prop.setProperty(".integercodec-class",
				//		"me.lemire.integercompression.FastPFOR");
				ic[n] = new LemireCodec(new String[]{"Composition", "OptPFD", "Simple16"});
			}
			if ("kamikaze_s16".equals(codec)){
			//case "kamikaze_s16":
				prop = new Properties();
				//prop.setProperty(".integercodec-class",
				//		"me.lemire.integercompression.FastPFOR");
				ic[n] = new LemireCodec(new String[]{"Composition", "Kamikaze", "Simple16"});
			}
		}
		this.ids = ic[0];
		this.tfs = ic[1];
		this.ffs = ic[2];
		this.blks = ic[3];

		Random r = new Random(System.nanoTime());
		int id = 0;

		postings = new ArrayList<List<Posting>>();
		List<Posting> l = new ArrayList<Posting>();
		postings.add(l);
		for (int i = 0; i < numPostings; i++) {

			int[] fields = new int[numFields];
			for (int j = 0; j < numFields; j++)
				fields[j] = r.nextInt(256);
			fields[2]++; //just to be sure ;)
			int tf = 0;
			for (int j = 0; j < numFields; j++)
				tf += fields[j];
			int[] blocks = new int[tf];
			int acc = 0;
			for (int j = 0; j < tf; j++)
				blocks[j] = acc += r.nextInt(255) + 1;

			BlockFieldPostingImpl bfpi = new BlockFieldPostingImpl(
					id += r.nextInt(255)+1, tf, blocks, fields);

			l.add(bfpi);
		}

	}

	@Ignore @Test
	public void test() throws Exception {

		List<Iterator<Posting>> iterators = new ArrayList<Iterator<Posting>>();
		for (List<Posting> x : postings)
			iterators.add(x.iterator());
		Iterator[] iterarray = Arrays.copyOf(iterators.toArray(),
				iterators.toArray().length, Iterator[].class);

		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writeBlockFieldPostingsToFile(iterarray,
				pointerList);
		BitIn bitIn = new BitInputStream(filename);

		File tmpFile = File.createTempFile("tmp", BitIn.USUAL_EXTENSION);
		IntegerCodingPostingOutputStream icpw = new IntegerCodingPostingOutputStream(
				tmpFile.toString(), chunksize, 4, 1, 10000, ids, tfs, ffs, blks);
		for (BitIndexPointer p : pointerList) {

			IterablePosting ip1 = new BlockFieldIterablePosting(bitIn, p.getNumberOfEntries(), null, 4);
			/* BitIndexPointer bip = */ icpw.writePostings(ip1);
			//System.err.println(bip.toString());
		}
		icpw.close();

		ByteFileBuffered bfb = new ByteFileBuffered(tmpFile.toString());
		ByteIn input = bfb.readReset(0);
		Iterator<List<Posting>> iterator = postings.iterator();
		for (BitIndexPointer p : pointerList) {

			IntegerCodingIterablePosting icip = new IntegerCodingIterablePosting(
					input, p.getNumberOfEntries(), null, chunksize, 4, 1, ids,
					tfs, ffs, blks);
			PostingTestUtils.compareBlockFieldPostings(iterator.next(), icip);
		}
		bfb.close();
	}
	

	@Ignore @Test
	public void test2() throws Exception {

		List<Iterator<Posting>> iterators = new ArrayList<Iterator<Posting>>();
		for (List<Posting> x : postings)
			iterators.add(x.iterator());
		Iterator[] iterarray = Arrays.copyOf(iterators.toArray(),
				iterators.toArray().length, Iterator[].class);

		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writeBlockFieldPostingsToFile(iterarray,
				pointerList);
		BitIn bitIn = new BitInputStream(filename);

		File tmpFile = File.createTempFile("tmp", BitIn.USUAL_EXTENSION);
		IntegerCodingPostingOutputStream icpw = new IntegerCodingPostingOutputStream(
				tmpFile.toString(), chunksize, 4, 2, 10000, ids, tfs, ffs, blks);
		for (BitIndexPointer p : pointerList) {

			IterablePosting ip1 = new BlockFieldIterablePosting(bitIn, p.getNumberOfEntries(), null, 4);
			@SuppressWarnings("unused")
			BitIndexPointer bip = icpw.writePostings(ip1);
			//System.err.println(bip.toString());
		}
		icpw.close();

		ByteFileBuffered bfb = new ByteFileBuffered(tmpFile.toString());
		ByteIn input = bfb.readReset(0);
		Iterator<List<Posting>> iterator = postings.iterator();
		for (BitIndexPointer p : pointerList) {

			IntegerCodingIterablePosting icip = new IntegerCodingIterablePosting(
					input, p.getNumberOfEntries(), null, chunksize, 4, 2, ids,
					tfs, ffs, blks);
			PostingTestUtils.compareBlockFieldPostings(iterator.next(), icip);
		}
		bfb.close();
	}	

}
