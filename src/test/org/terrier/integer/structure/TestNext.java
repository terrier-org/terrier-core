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
 * The Original Code is TestNext.java.
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
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.bit.PostingTestUtils;
import org.terrier.structures.integer.IntegerCodingPostingOutputStream;
import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.structures.postings.bit.BasicIterablePosting;
import org.terrier.structures.postings.integer.IntegerCodingIterablePosting;
@SuppressWarnings({"rawtypes", "unchecked",  "deprecation"})
public class TestNext {

	protected int numPostings = 1000;
	protected int chunksize = 256;

	protected List<List<Posting>> postings;
	private IntegerCodec ids;
	private IntegerCodec tfs;

	public TestNext() {

		ids = new GammaCodec();
		tfs = new UnaryCodec();

		postings = new ArrayList<List<Posting>>();
		List<Posting> l = new ArrayList<Posting>();
		postings.add(l);
		
		for (int i = 0; i < numPostings; i++) {

			BasicPostingImpl pi = new BasicPostingImpl(i*2, i+1);

			l.add(pi);
		}

	}

	@Test
	public void test() throws Exception {

		List<Iterator<Posting>> iterators = new ArrayList<Iterator<Posting>>();
		for (List<Posting> x : postings)
			iterators.add(x.iterator());
		Iterator[] iterarray = Arrays.copyOf(iterators.toArray(),
				iterators.toArray().length, Iterator[].class);

		List<BitIndexPointer> pointerList = new ArrayList<BitIndexPointer>();
		String filename = PostingTestUtils.writePostingsToFile(iterarray,
				pointerList);
		BitIn bitIn = new BitInputStream(filename);

		File tmpFile = File.createTempFile("tmp", BitIn.USUAL_EXTENSION);
		IntegerCodingPostingOutputStream icpw = new IntegerCodingPostingOutputStream(
				tmpFile.toString(), chunksize, 0, 0, 0, ids, tfs, null, null);
		for (BitIndexPointer p : pointerList) {

			IterablePosting ip1 = new BasicIterablePosting(bitIn, p.getNumberOfEntries(), null);
			/* BitIndexPointer bip =*/  icpw.writePostings(ip1);
			//System.err.println(bip.toString());
		}
		icpw.close();
		ByteFileBuffered bfb = new ByteFileBuffered(tmpFile.toString());
		ByteIn input = bfb.readReset(0);
		//Iterator<List<Posting>> iterator = postings.iterator();
		for (BitIndexPointer p : pointerList) {

			IterablePosting icip = new IntegerCodingIterablePosting(
					input, p.getNumberOfEntries(), null, chunksize, 0, 0, ids,
					tfs, null, null);
			
			icip.next(8);
			assertEquals(8, icip.getId());
			icip.next(9);
			assertEquals(10, icip.getId());
			icip.next(600);
			assertEquals(600, icip.getId());
			icip.close();
		}
		bfb.close();
		bitIn.close();
	}
}
