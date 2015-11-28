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
 * The Original Code is PostingUtil.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.postings;

import gnu.trove.TIntArrayList;

import java.io.IOException;
/** Handy methods for Posting classes, such as obtaining all ids in a
 * posting list, or selecting the minimum id in an array of posting lists. 
 * @author Craig Macdonald
 * @since 3.0
 */
public class PostingUtil {

	
	
	
	/** Get an array of all the ids in a given IterablePosting stream */
	public static int[] getIds(final IterablePosting ip) throws IOException
	{
		final TIntArrayList ids = new TIntArrayList();
		while(ip.next() != IterablePosting.EOL)
			ids.add(ip.getId());
		return ids.toNativeArray();
	}
	
	/** Get an array of all the ids in a given IterablePosting stream, 
	 * where the length of the stream is known */
	public static int[] getIds(final IterablePosting ip, final int numPointers) throws IOException
	{
		final int[] ids = new int[numPointers];
		for(int i=0;ip.next() != IterablePosting.EOL;i++)
			ids[i] = ip.getId();
		return ids;
	}
	
	/** Get an array of all the ids in a given IterablePosting stream, 
	 * where the length of the stream is known */
	public static int[][] getAllPostings(final IterablePosting ip, final int numPointers) throws IOException
	{
		final int[][] rtr = new int[2][numPointers];
		for(int i=0;ip.next() != IterablePosting.EOL;i++)
		{
			rtr[0][i] = ip.getId();
			rtr[1][i] = ip.getFrequency();
		}
		return rtr;
	}
	
	/** Get an array of all the ids in a given IterablePosting stream, 
	 * where the length of the stream is known */
	public static int[][] getAllPostingsWithFields(final IterablePosting ip, final int numPointers, final int fieldCount) throws IOException
	{
		FieldPosting fp = (FieldPosting)ip;
		final int[][] rtr = new int[2+fieldCount][numPointers];

		for(int i=0;ip.next() != IterablePosting.EOL;i++)
		{
			rtr[0][i] = ip.getId();
			rtr[1][i] = ip.getFrequency();
			int[] tff = fp.getFieldFrequencies();
			for(int j=0;j<fieldCount;j++)
			{
				rtr[j+2][i] = tff[j];
			}
		}
		
		return rtr;
	}
	
	
	/** Get an array of all the ids in a given IterablePosting stream, 
	 * where the length of the stream is known */
	public static int[][] getAllPostings(final IterablePosting ip) throws IOException
	{
		if (ip == null)
			return null;
		final TIntArrayList ids = new TIntArrayList();
		final TIntArrayList tfs = new TIntArrayList();
		
		while(ip.next() != IterablePosting.EOL)
		{
			ids.add(ip.getId());
			tfs.add(ip.getFrequency());
		}
		return new int[][]{ids.toNativeArray(), tfs.toNativeArray()};
	}
	
	/** Get an array of all the ids in a given IterablePosting stream, 
	 * where the length of the stream is known */
//	public static int[][] getAllPostingsFieldBlocks(final IterablePosting ip, int fieldCount) throws IOException
//	{
//		final TIntArrayList ids = new TIntArrayList();
//		final TIntArrayList tfs = new TIntArrayList();
//		final TIntArrayList positions = new TIntArrayList();
//		final TIntArrayList blocksfreq = new TIntArrayList();
//		final TIntArrayList[] tff = new TIntArrayList[fieldCount];
//		for(int i=0;i<fieldCount;i++)
//			tff[i] = new TIntArrayList();
//		
//		BlockPosting bp = (BlockPosting)ip;
//		FieldPosting fp = null; boolean fields = false;
//		if (ip instanceof FieldPosting) 
//		{
//			fp = (FieldPosting)ip;
//			fields = true;
//		}
//		
//		for(int i=0;ip.next() != IterablePosting.EOL;i++)
//		{
//			ids.add(ip.getId());
//			tfs.add(ip.getFrequency());
//			
//			int[] pos = bp.getPositions();
//			blocksfreq.add(pos.length);
//			positions.add(pos);
//			if (fields)
//			{
//				int[] tffs = fp.getFieldFrequencies();
//				for(int fi=0;fi<fieldCount;fi++)
//					tff[fi].add(tffs[fi]);
//			}
//		}
//		return new int[][]{
//				ids.toNativeArray(), 
//				tfs.toNativeArray()
//				};
//	}
	
	
	/** Returns the minimum docid of the current postings in the array of IterablePostings
	 * @return minimum docid, or -1 if all postings have ended. */
	public static int selectMinimumDocId(final IterablePosting postingListArray[])
	{
		int docid = Integer.MAX_VALUE;
		for (IterablePosting postingList: postingListArray)
			if (!postingList.endOfPostings() && docid > postingList.getId()) 
				docid = postingList.getId();
		if (docid == Integer.MAX_VALUE)
			docid = -1;
		return docid;
	}
	
}
