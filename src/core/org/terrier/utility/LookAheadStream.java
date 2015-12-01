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
 * The Original Code is LookAheadStream.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.utility;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implements an InputStream, that encapsulates another stream, but only upto the
 * point that a pre-defined end marker in the stream is identified. The Reader
 * will then become endOfFile, and refuse to return any more bytes from the
 * stream. Suppose that we create an instance of a LookAheadStream with the 
 * end marker END. For the following input:
 * <tt>a b c d END e f g...</tt>
 * the LookAheadStream, will stop after reading the string END. Note that the 
 * end marker will be missing from the parent stream.
 * <p>LookAheadStream allows the encoding to be changed between markers. Handy
 * for collections of webpages, which may use different encodings. However, the
 * end marker must be obtainable using the default encoding.
 * 
 * @author Craig Macdonald, Vassilis Plachouras
  * @see org.terrier.utility.LookAheadReader
 */
public class LookAheadStream extends InputStream {
	/** the parent stream that this object is looking ahead in */
	protected final InputStream ParentStream;
	/** the end marker that it is pre-scanning the stream for */
	protected final byte[] EndMarker;
	/** How long is the end marker */
	protected final int MarkerLen;
	/** How many bytes are in the read ahead buffer */
	protected int BufLen = 0;
	/** index of the first entry in the buffer */
	protected int BufIndex = 0;
	/** The read ahead buffer */
	protected final int[] Buffer;
	/** have we reached the end of the file */
	protected boolean EOF = false;
	/**
	 * Creates an instance of a LookAheadStream that will read from the 
	 * given stream until the end marker is found.
	 * <b>NB:</b>. This constructor assumes the default charset.
	 * @param parent InputStream the stream used for reading the input.
	 * @param endMarker String the marker which signifies the end of the stream. 
	 * Not deprecated, but recommended to use LookAheadStream(InputStream parent, String endMarker, String charSet) instead.
	 */
	public LookAheadStream(InputStream parent, String endMarker) {
		this(parent, endMarker.getBytes());
	}
	
	/**
     * Creates an instance of a LookAheadStream that will read from the 
     * given stream until the end marker is found. The end marker is decoded
     * from bytes using the described charSet.
     * @param parent InputStream the stream used for reading the input.
     * @param endMarker String the marker which signifies the end of the stream.
	 * @param charSet String the name of the character set to use.
     */
	public LookAheadStream(InputStream parent, String endMarker, String charSet) throws java.io.UnsupportedEncodingException
	{
		this(parent, endMarker.getBytes(charSet));
	}

	/**
	 * Creates an instance of a LookAheadStream that will read from the 
	 * given stream until the end marker byte pattern is found.
	 * @param parent InputStream the stream used for reading the input.
	 * @param endMarker String the marker which signifies the end of the stream. 
	 */
	public LookAheadStream(InputStream parent, byte[] endMarker) {
		this.ParentStream = parent;
		this.EndMarker = endMarker;
		this.MarkerLen = this.EndMarker.length;
		this.Buffer = new int[this.MarkerLen];//this is fine. Buffer only contains byte values.
		this.BufLen = 0;
	}

	/**
	 * Read a byte from the parent stream, first checking that 
	 * it doesn't form part of the end marker.
	 * @return int the code of the read byte, or -1 if the end of
	 *		 the stream has been reached.
	 * @throws IOException if there is any error while reading from the stream.
	 */
	@Override
	public int read() throws IOException {
		if (EOF)
			return -1;
		if (BufLen > 0) {
			BufLen--;
			return Buffer[BufIndex++];
		}
		int c = -1;
		boolean keepReading = true;
		while (keepReading) {
			if ((c = ParentStream.read()) == -1)
			{
				EOF = true;
				return -1;
			}
			if (c == EndMarker[BufLen]) {
				Buffer[BufLen++] = c; 
				if (BufLen == MarkerLen) {
					EOF = true;
					return -1;
				}
			} else {
				Buffer[BufLen++] = c;
				BufIndex = 0;
				break;
			}
		}
		BufLen--;
		return Buffer[BufIndex++];
	}
	/** 
	 * Read bytes into an array. This method will read 100 bytes or the array length, 
	 * and until the end of the stream is reached.
	 * <B>NB:</B> Uses read() internally.
	 * @param cbuf cbuf - Destination buffer
	 * @return The number of bytes read, or -1 if the end of the stream has been reached.
	 * @throws IOException If an I/O error occurs
	 */
	@Override
	public int read(byte[] cbuf) throws IOException {
		if (EOF)
			return -1;
		int ReadSize = 100;
		if (ReadSize > cbuf.length)
			ReadSize = cbuf.length;
		int i=0;
		for(;i<ReadSize;i++)
		{
			int c = this.read();
			if (c == -1)
				return i-1;
			else
				cbuf[i] = (byte)c;//safe, as this.read() returns -1 to 255, and c!=-1
		}
		return i;	
	}
	/**
	 * Read bytes into a portion of an array. It will try to read the specified number of bytes into the
	 * buffer. <B>NB:</B>Implemented in terms of read().
	 * @param cbuf Destination buffer
	 * @param offset Offset at which to start storing bytes
	 * @param len Maximum number of bytes to read
	 * @return The number of bytes read, or -1 if the end of the stream has been reached
	 * @throws IOException If an I/O error occurs
	 */
	@Override
	public int read(byte[] cbuf, int offset, int len) throws IOException {
		if (EOF)
			return -1;
		int i=0;
		int c = this.read();
		if (c == -1)
			return -1;
		cbuf[offset++] = (byte)c;//safe, as this.read() returns -1 to 255, and c!=-1
		i++;
		for(;i<len;i++)
		{
			c = this.read();
			if (c == -1)
			{
				return i;//not i-1, as i (iterator) is 1 behind no of bytes read
			}
			else
				cbuf[offset++] = (byte)c;//safe, as this.read() returns -1 to 255, and c!=-1
		}
		return i;
	}
	/**
	 * Reset the stream. Attempts to reset it in some way appropriate 
	 * to the particular stream, for example by positioning it to its
	 * starting point. Not all input streams support the
	 * reset() operation. <b>Use at your own risk.</b>
	 * @throws IOException thrown if ParentStream.reset();
	 */
	public void reset() throws IOException {
		BufLen = BufIndex = 0;
		ParentStream.reset();
	}
	/** 
	 * Skips n bytes from the stream. If the end of 
	 * the stream has been reached before reading n bytes,
	 * then it returns.
	 * <B>NB:</B> This method uses read() internally.
	 * @param n long the number of bytes to skip.
	 * @return long the number of bytes skipped.
	 * @throws IOException if there is any error while 
	 *		 reading from the stream.
	 */
	public long skip(long n) throws IOException {
		/* TODO a more efficient implementation could be made */
		long i = 0;
		for (; i < n && this.ready(); i++) {
			this.read();
		}
		return i;
	}
	/** 
	 * Indicates whether there are more bytes
	 * available to read from the stream.
	 * @return boolean true if there are more bytes
	 *		 available for reading, otherwise it returns
	 *		 false.
	 * @throws IOException if there is any error while 
	 *		 reading from the stream.
	 */
	public boolean ready() throws IOException {
		return (! EOF) && ParentStream.available()>0;
	}
	/**
	 * Closes the current stream, by setting the end of file
	 * flag equal to true. Does NOT close the wrapped stream.
	 */
	public void close() throws IOException {
		EOF = true;
	}
	/** 
	 * Support for marking is not implemented.
	 * @return boolean false.
	 */
	public boolean markSupported() {
		return false;
	}
	
	/**
	 * This method is not implemented.
	 */
	public void mark(int x) {}
}
