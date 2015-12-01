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
 * The Original Code is LookAheadReader.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.utility;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;
/**
 * Implements a Reader, that encapsulates another stream, but only upto the
 * point that a pre-defined end marker in the stream is identified. The Reader
 * will then become endOfFile, and refuse to return any more characters from the
 * stream. Suppose that we create an instance of a LookAheadReader with the 
 * end marker END. For the following input:
 * <tt>a b c d END e f g...</tt>
 * the LookAheadReader will stop after reading the string END. Note that the 
 * end marker will be missing from the parent stream.
 * 
 * @author Craig Macdonald, Vassilis Plachouras
  * @see org.terrier.utility.LookAheadStream
 */
public class LookAheadReader extends Reader {
	/** the parent stream that this object is looking ahead in */
	private final Reader ParentStream;
	/** the end marker that it is prescanning the stream for */
	private final char[] EndMarker;
	/** How long is the end marker */
	private final int MarkerLen;
	/** How many characters are in the read ahead buffer */
	private int BufLen = 0;
	/** index of the first entry in the buffer */
	private int BufIndex = 0;
	/** The read ahead buffer */
	private final char[] Buffer;
	/** have we reached the end of the file */
	private boolean EOF = false;
	/**
	 * Creates an instance of a LookAheadReader that will read from the 
	 * given stream until the end marker is found.
	 * @param parent Reader the stream used for reading the input/
	 * @param endMarker String the marker which signifies the end of the stream. 
	 */
	//
	public LookAheadReader(Reader parent, String endMarker) {
		this.ParentStream = parent;
		this.EndMarker = endMarker.toCharArray();
		MarkerLen = EndMarker.length;
		Buffer = new char[endMarker.length()];
		BufLen = 0;
	}
	/**
	 * Read a character from the parent stream, first checking that 
	 * it doesnt form part of the end marker.
	 * @return int the code of the read character, or -1 if the end of
	 *		 the stream has been reached.
	 * @throws IOException if there is any error while reading from the stream.
	 */
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
			char cc = (char)c;
			if (cc == EndMarker[BufLen]) {
				Buffer[BufLen++] = cc; 
				if (BufLen == MarkerLen) {
					EOF = true;
					return -1;
				}
			} else {
				Buffer[BufLen++] = cc;
				BufIndex = 0;
				//keepReading = false;
				break;
			}
		}
		BufLen--;
		return Buffer[BufIndex++];
	}
	/** 
	 * Read characters into an array. This method will read 100 characters or the array length, 
	 * and until the end of the stream is reached.
	 * <B>NB:</B> Uses read() internally.
	 * @param cbuf cbuf - Destination buffer
	 * @return The number of characters read, or -1 if the end of the stream has been reached.
	 * @throws IOException If an I/O error occurs
	 */
	public int read(char[] cbuf) throws IOException {
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
				cbuf[i] = (char)c;
		}
		return i;	
	}
	/**
	 * Read characters into a portion of an array. It will try to read the specified number of characters into the
	 * buffer. <B>NB:</B>Implemented in terms of read().
	 * @param cbuf Destination buffer
	 * @param offset Offset at which to start storing characters
	 * @param len Maximum number of characters to read
	 * @return The number of characters read, or -1 if the end of the stream has been reached
	 * @throws IOException If an I/O error occurs
	 */
	public int read(char[] cbuf, int offset, int len) throws IOException {
		//System.out.print("offset="+offset+ " len="+len);
		if (EOF)
			return -1;
		int i=0;
		for(;i<len;i++)
		{
			int c = this.read();
			if (c == -1)
			{
				//System.out.println(" rtr="+i);
				return i;//not i-1, as i (iterator) is 1 behind no of chars read
			}
			else
				cbuf[offset++] = (char)c;	
		}
		//System.out.println(" rtr="+i);
		return i;
	}
	/**
	 * Reset the stream. Attempts to reset it in some way appropriate to the particular stream, for example by 
	 * positioning it to its starting point. Not all character-input streams support the reset() operation. <b>Use
	 * at your own risk.</b>
	 * @throws IOException thrown if ParentStream.reset();
	 */
	public void reset() throws IOException {
		BufLen = BufIndex = 0;
		ParentStream.reset();
	}
	/** 
	 * Skips n characters from the stream. If the end of 
	 * the stream has been reached before reading n characters,
	 * then it returns.
	 * <B>NB:</B> This method uses read() internally.
	 * @param n long the number of characters to skip.
	 * @return long the number of characters skipped.
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
	 * Indicates whether there are more characters
	 * available to read from the stream.
	 * @return boolean true if there are more characters 
	 *		 available for reading, otherwise it returns
	 *		 false.
	 * @throws IOException if there is any error while 
	 *		 reading from the stream.
	 */
	public boolean ready() throws IOException {
		return (! EOF) && ParentStream.ready();
	}
	/**
	 * Closes the current stream, by setting the end of file
	 * flag equal to true.
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
	public void mark(int x) throws IOException {}
	/** 
	 * A testing method for 
	 * @param args the command line arguments, which should contain 
	 * the name of a file with documents delimited by 
	 * the second command line argument. 
	 */
	public static void main(String[] args) {
		try {
			String filename = args[0];
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(new FileInputStream(filename))));
			LookAheadReader lar = new LookAheadReader(br, args[1]);
			int c;
			while ((c = lar.read()) != -1)
				System.err.print((char) c);
			lar.close();
			lar = new LookAheadReader(br, args[1]);
			while ((c = lar.read()) != -1)
				System.err.print((char) c);
			lar.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
