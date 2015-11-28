/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.uk
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
 * The Original Code is DirectInvertedOutputStream.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.bit;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.compression.bit.BitOut;
import org.terrier.compression.bit.BitOutputStream;
import org.terrier.structures.AbstractPostingOutputStream;
import org.terrier.structures.BitFilePosition;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.FilePosition;
import org.terrier.structures.SimpleBitIndexPointer;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.structures.postings.bit.BasicIterablePosting;

/** Writes a block direct or block inverted index, when passed appropriate posting lists.
  * @author Craig Macdonald
  * @since 2.0
  */
public class DirectInvertedOutputStream extends AbstractPostingOutputStream implements Closeable {
	/** what to write to */
	protected BitOut output;
	/** The logger used */
	protected static final Logger logger = LoggerFactory.getLogger(DirectInvertedOutputStream.class);
 
	/** Creates a new output stream, writing a BitOutputStream to the specified file. The number of binary bits
	  * for fields must also be specified.
	  * @param filename Location of the file to write to
	  */
	public DirectInvertedOutputStream(String filename) throws IOException
	{
		this.output = new BitOutputStream(filename);
	}
	/** Creates a new output stream, writing to the specified BitOut implementation.  The number of binary bits
	  * for fields must also be specified.
	  * @param out BitOut implementation to write the file to 
	  */
	public DirectInvertedOutputStream(BitOut out)
	{
		this.output = out;
	}
	
	/** Returns the IterablePosting class to use for reading structure written by this class */
	@Override
	public Class<? extends IterablePosting> getPostingIteratorClass()
	{
		return BasicIterablePosting.class;
	}
	
	/** Write out the specified postings. The delta for the first id must be specified.
	  * @param postings The postings to write out
	  * @param firstId the (delta) value of the first docid to write out.
	  */
	public BitIndexPointer writePostings(int[][] postings, int firstId) throws IOException
	{
		 return writeNoFieldPostings(postings, 0, postings[0].length, firstId);
	}
	
	/** Write out the specified postings.
	 * @param iterator an Iterator of Posting objects
	 */
	@Override
	public BitIndexPointer writePostings(Iterator<Posting> iterator) throws IOException
	{
		return writePostings(iterator, -1);
	}
	
	/** Write out the specified postings, but allowing the delta for the first document to be adjusted
	 * @param iterator an Iterator of Posting objects
	 * @param previousId id of the previous posting in this stream
	 */
	@Override
	public BitIndexPointer writePostings(Iterator<Posting> iterator, int previousId) throws IOException
	{
		BitIndexPointer pointer = new SimpleBitIndexPointer();
		pointer.setOffset(output.getByteOffset(), output.getBitOffset());
		int numberOfEntries = 0;
		
		Posting posting = null;
		while(iterator.hasNext())
		{
			posting = iterator.next();
			output.writeGamma(posting.getId() - previousId);
			previousId = posting.getId();
			writePostingNotDocid(posting);
			numberOfEntries++;
		}
		pointer.setNumberOfEntries(numberOfEntries);
		return pointer;
	}
	
	/** Write out the specified postings, but allowing the delta for the first document to be adjusted
	 * @param postings IterablePosting postings accessed through an IterablePosting object
	 * @param previousId id of the previous posting in this stream
	 */
	@Override
	public BitIndexPointer writePostings(IterablePosting postings, int previousId) throws IOException
	{
		BitIndexPointer pointer = new SimpleBitIndexPointer();
		pointer.setOffset(output.getByteOffset(), output.getBitOffset());
		int numberOfEntries = 0;
		
		while(postings.next() != IterablePosting.EOL)
		{
			output.writeGamma(postings.getId() - previousId);
			//System.err.println("Writing id" + postings.getId());
			previousId = postings.getId();
			writePostingNotDocid(postings);
			numberOfEntries++;
		}
		pointer.setNumberOfEntries(numberOfEntries);
		return pointer;
	}
	
	/** Write out the specified postings.
	 * @param postings IterablePosting postings accessed through an IterablePosting object
	 */
	@Override
	public BitIndexPointer writePostings(IterablePosting postings) throws IOException
	{
		return writePostings(postings, -1);
	}
	
	/** Hook method for writing out the remainder of the posting */
	protected void writePostingNotDocid(Posting p) throws IOException
	{
		output.writeUnary(p.getFrequency());
	}
	
	/** Write out a range of the specified postings. The delta for the first id must be specified.
	  * @param postings The postings to write out
	  * @param startOffset The location of the first posting to write out.
	  * @param Length The number of postings to be written out.
	  * @param firstId the (delta) value of the first docid to write out.
	  */
	@Override
	public BitIndexPointer writePostings(int[][] postings, int startOffset, int Length, int firstId) throws IOException
	{
		return writeNoFieldPostings(postings, startOffset, Length, firstId);
	}
	
	
	/**
	 * Writes the given postings to the bit file. This method assumes that
	 * field information is not provided.
	 * @param postings the postings list to write.
	 * @param firstId the first identifier to write. This can be 
	 *        an id plus one, or the gap of the current id and the previous one.
	 * @param offset The location of the first posting to write out.
	 * @param length The number of postings to be written out.
	 * @throws IOException if an error occurs during writing to a file.
	 */
	protected BitIndexPointer writeNoFieldPostings(final int[][] postings, int offset, final int length, final int firstId) 
			throws IOException {

		BitIndexPointer pointer = new SimpleBitIndexPointer();
		pointer.setOffset(output.getByteOffset(), output.getBitOffset());
		
		//local variables in order to reduce the number
		//of times we need to access a two-dimensional array
		final int[] postings0 = postings[0];
		final int[] postings1 = postings[1];
		
		//write the first entry
		output.writeGamma(firstId);
		output.writeUnary(postings1[offset]);
	
		offset++;
		for (; offset < length; offset++) {
			output.writeGamma(postings0[offset] - postings0[offset - 1]);
			output.writeUnary(postings1[offset]);
		}
		
		return pointer;
	}
	
	/** close this object. suppresses any exception */
	@Override
	public void close()
	{
		try{ 
			output.close();
		} catch (IOException ioe) {
			logger.error("Problem closing DirectInvOutputStream", ioe);
		}
	}
	
	/** What is current offset? */
	@Override
	public BitFilePosition getOffset()
	{
		return new FilePosition(output.getByteOffset(), output.getBitOffset());
	}
	
	/** Return the current offset in bytes in the written file
	 * @deprecated */
	public long getByteOffset()
	{
		return output.getByteOffset();
	}
	
	/** Return the current offset in bits in the written file 
	 * @deprecated */
	public byte getBitOffset()
	{
		return output.getBitOffset();
	}
	
	/** Return the underlying BitOut implementation being used by the class */
	public BitOut getBitOut()
	{
		return output;
	}
}
