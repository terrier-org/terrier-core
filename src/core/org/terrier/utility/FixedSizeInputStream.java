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
 * The Original Code is FixedSizeInputStream.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.utility;

import java.io.IOException;
import java.io.FilterInputStream;
import java.io.InputStream;
/** An inputstream which only reads a fixed length of a parent input stream.
 * No buffering is done.
 * @author Craig Macdonald
 */
public class FixedSizeInputStream extends FilterInputStream
{
	/** maximum bytes to read */
	protected final long maxsize;
	/** number of bytes read so far */
	protected long size;

	/** prevent a close() from doing anyhing, like closing the underlying stream */
	protected boolean suppressClose = false;

	/** create a new FixedSizeInputStream, using in as the underlying
	  * InputStream, and maxsize as the maximum number of bytes to read. 
	  * @param in underlying InputStream to read bytes from.
	  * @param _maxsize maximum number of bytes to read.
	  */
	public FixedSizeInputStream(InputStream in, long _maxsize)
	{
		super(in);
		assert _maxsize >= 0 : "_maxsize must have a non-negative length : " + _maxsize;
		this.maxsize = _maxsize;
	}

	/** Read a single byte from the underlying Reader.
	  * @return The byte read, or -1 if the end of the underlying stream has been reached
	  * or the maximum allowed number of bytes has been read from it.
	  * @throws IOException If an I/O error occurs.
	  */
	public int read() throws IOException
	{
		if (size == maxsize)
			return -1;
		final int by = in.read();
		if (by != -1)
			size++;
		//System.err.println("1. size="+size);
		return by;
	}
	
	/** Read bytes into a portion of an array. 
	  * @param cbuf Destination buffer
	  * @param off Offset at which to start storing bytes
	  * @param len  Maximum number of bytes to read 
	  * @return The number of bytes read, or -1 if the end of the stream has been reached.
	  * @throws IOException If an I/O error occurs in the underlying reader.
	  */
	public int read(byte[] cbuf, int off, int len) throws IOException
	{
		if (size == maxsize)
            return -1;
		if (size + len < maxsize)
		{
			int rtr = in.read(cbuf, off, len);
			size += rtr;
			//System.err.println("2. size="+size +" rtr="+rtr);
			return rtr;
		}
		assert ! (off < 0);
		assert ! (len < 0);
		assert ! ((int)(maxsize - size) < 0) : "Read request would be negative: maxsize - size = " + maxsize + " - " + size + " = " + (int)(maxsize - size);
		int rtr = in.read(cbuf, off, (int)(maxsize - size));
		size += rtr;
		//System.err.println("3. size="+size +" rtr="+rtr);
		return rtr;	
	}

    /**
     * Skips n bytes from the stream. If the end of
     * the stream has been reached before reading n bytes
     * then it returns.
     * <B>NB:</B> This method uses read() internally.
     * @param n long the number of characters to skip.
     * @return long the number of characters skipped.
     * @throws IOException if there is any error while
     *       reading from the stream.
     */
    public long skip(long n) throws IOException {
        /* TODO a more efficient implementation could be made */
        long i = 0;
        for (; i < n && size < maxsize; i++) {
            this.read();
        }
        return i;
    }

	/* simple remaining implementation - marks and reset not supported */
	/** 
	 * {@inheritDoc} 
	 */
	public boolean markSupported() { return false; }
	/** 
	 * {@inheritDoc} 
	 */
	public void mark(int readAheadLimit) { return; }
	/** 
	 * {@inheritDoc} 
	 */
	public void reset() throws IOException { return; }
	/** 
	 * Sets it so that close() will never be called
	 */
	public void suppressClose() { suppressClose = true; }
	/** 
	 * {@inheritDoc} 
	 */
	public void close() throws IOException
	{ 
		if (suppressClose)
			return;
		System.err.println("FixedInputStream forcing parent close"); 
		super.close();
	}
}
