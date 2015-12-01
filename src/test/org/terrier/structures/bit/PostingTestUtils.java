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
 * The Original Code is PostingTestUtils.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.bit;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.terrier.compression.bit.BitIn;
import org.terrier.structures.BitFilePosition;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.Skipable;
import org.terrier.structures.bit.BlockDirectInvertedOutputStream;
import org.terrier.structures.bit.BlockFieldDirectInvertedOutputStream;
import org.terrier.structures.bit.DirectInvertedDocidOnlyOuptutStream;
import org.terrier.structures.bit.DirectInvertedOutputStream;
import org.terrier.structures.bit.FieldDirectInvertedOutputStream;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;

public class PostingTestUtils {

	public static void assertEqualsBitFilePosition(BitIndexPointer expected, BitFilePosition actual)
	{
		assertEquals(expected.getOffset(), actual.getOffset());
		assertEquals(expected.getOffsetBits(), actual.getOffsetBits());
	}
	
	public static void comparePostings(List<Posting> inputPostings, IterablePosting outputPostings, boolean docidsOnly) throws Exception
	{
		if (docidsOnly)
			comparePostingsDocids(inputPostings, outputPostings);
		else
			comparePostings(inputPostings, outputPostings);
	}
	
	public static void comparePostings(List<Posting> inputPostings, IterablePosting outputPostings) throws Exception
	{
		for(Posting p : inputPostings)
		{
			assertEquals(p.getId(), outputPostings.next());
			assertEquals(p.getId(), outputPostings.getId());
			assertEquals(p.getFrequency(), outputPostings.getFrequency());
		}
		assertFalse(outputPostings.next() != IterablePosting.EOL);
	}
	
	public static void comparePostingsDocids(List<Posting> inputPostings, IterablePosting outputPostings) throws Exception
	{
		for(Posting p : inputPostings)
		{
			assertEquals(p.getId(), outputPostings.next());
			assertEquals(p.getId(), outputPostings.getId());
			System.err.println(outputPostings.getId());
			if (outputPostings.getId() == 2 )
			{
				System.err.println("at 2");
			}
		}
		assertFalse(outputPostings.next() != IterablePosting.EOL);
	}
	
	public static void compareBlockPostings(List<Posting> inputPostings, IterablePosting outputPostings) throws Exception
    {
        for(Posting p : inputPostings)
        {
            assertEquals(p.getId(), outputPostings.next());
            assertEquals(p.getId(), outputPostings.getId());
            assertEquals(p.getFrequency(), outputPostings.getFrequency());
            assertArrayEquals(((BlockPosting) p).getPositions(), ((BlockPosting) p).getPositions());
        }
        assertFalse(outputPostings.next() != IterablePosting.EOL);
    }
     
    public static void compareFieldPostings(List<Posting> inputPostings, IterablePosting outputPostings) throws Exception
    {
        for(Posting p : inputPostings)
        {
            assertEquals(p.getId(), outputPostings.next());
            assertEquals(p.getId(), outputPostings.getId());
            assertEquals(p.getFrequency(), outputPostings.getFrequency());
            assertArrayEquals(((FieldPosting) p).getFieldFrequencies(), ((FieldPosting) p).getFieldFrequencies());
        }
        assertFalse(outputPostings.next() != IterablePosting.EOL);
    }
     
    public static void compareBlockFieldPostings(List<Posting> inputPostings, IterablePosting outputPostings) throws Exception
    {
        for(Posting p : inputPostings)
        {
            assertEquals(p.getId(), outputPostings.next());
            assertEquals(p.getId(), outputPostings.getId());
            assertEquals(p.getFrequency(), outputPostings.getFrequency());
            assertArrayEquals(((FieldPosting) p).getFieldFrequencies(), ((FieldPosting) p).getFieldFrequencies());
            assertArrayEquals(((BlockPosting) p).getPositions(), ((BlockPosting) p).getPositions());            
        }
        assertFalse(outputPostings.next() != IterablePosting.EOL);
    }
	
	
	  public static String writeBlockPostingsToFile(Iterator<Posting>[] iterators, List<BitIndexPointer> pointerList) throws Exception
	    {
	        File tmpFile = File.createTempFile("tmp", BitIn.USUAL_EXTENSION);
	        DirectInvertedOutputStream dios = new BlockDirectInvertedOutputStream(tmpFile.toString());
	        for(Iterator<Posting> iterator : iterators)
	        {
	            BitIndexPointer p = dios.writePostings(iterator);
	            pointerList.add(p);
	        }
	        dios.close();
	        assertEquals(iterators.length, pointerList.size());
	        return tmpFile.toString();
	    }
	     
	    public static String writeFieldPostingsToFile(Iterator<Posting>[] iterators, List<BitIndexPointer> pointerList) throws Exception
	    {
	        File tmpFile = File.createTempFile("tmp", BitIn.USUAL_EXTENSION);
	        DirectInvertedOutputStream dios = new FieldDirectInvertedOutputStream(tmpFile.toString());
	        for(Iterator<Posting> iterator : iterators)
	        {
	            BitIndexPointer p = dios.writePostings(iterator);
	            pointerList.add(p);
	        }
	        dios.close();
	        assertEquals(iterators.length, pointerList.size());
	        return tmpFile.toString();
	    }
	     
	    public static String writeBlockFieldPostingsToFile(Iterator<Posting>[] iterators, List<BitIndexPointer> pointerList) throws Exception
	    {
	        File tmpFile = File.createTempFile("tmp", BitIn.USUAL_EXTENSION);
	        DirectInvertedOutputStream dios = new BlockFieldDirectInvertedOutputStream(tmpFile.toString());
	        for(Iterator<Posting> iterator : iterators)
	        {
	            BitIndexPointer p = dios.writePostings(iterator);
	            pointerList.add(p);
	        }
	        dios.close();
	        assertEquals(iterators.length, pointerList.size());
	        return tmpFile.toString();
	    }   
	
	
	public static String writePostingsToFile(Iterator<Posting>[] iterators, List<BitIndexPointer> pointerList) throws Exception
	{
		File tmpFile = File.createTempFile("tmp", BitIn.USUAL_EXTENSION);
		DirectInvertedOutputStream dios = new DirectInvertedOutputStream(tmpFile.toString());
		for(Iterator<Posting> iterator : iterators)
	 	{
	 		BitIndexPointer p = dios.writePostings(iterator);
	 		pointerList.add(p);
	 	}
		dios.close();
		assertEquals(iterators.length, pointerList.size());
		return tmpFile.toString();
	}
	
	public static String writePostingsToFileDocidOnly(Iterator<Posting>[] iterators, List<BitIndexPointer> pointerList) throws Exception
	{
		File tmpFile = File.createTempFile("tmp", BitIn.USUAL_EXTENSION);
		DirectInvertedOutputStream dios = new DirectInvertedDocidOnlyOuptutStream(tmpFile.toString());
		for(Iterator<Posting> iterator : iterators)
	 	{
	 		BitIndexPointer p = dios.writePostings(iterator);
	 		pointerList.add(p);
	 	}
		dios.close();
		assertEquals(iterators.length, pointerList.size());
		return tmpFile.toString();
	}
	
	public static Iterator<BitIndexPointer> makeSkippable(final Iterator<BitIndexPointer> in)
	{
		class SkippablePointerIterator implements Iterator<BitIndexPointer>,Skipable
		{
			@Override
			public boolean hasNext() {
				return in.hasNext();
			}

			@Override
			public BitIndexPointer next() {
				return in.next();
			}

			@Override
			public void remove() {
				in.remove();
			}

			@Override
			public void skip(int numEntries) throws IOException {
				while(numEntries > 0 && in.hasNext())
				{
					in.next();
					numEntries--;
				}
			}			
		}		
		return new SkippablePointerIterator();
	}
}
