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
 * The Original Code is CountingInputStream.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richarm{a.}dcs.gla.a.uk (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   
 */
package org.terrier.utility.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Sub-Class of Filter Input Stream with the extra method 
 * getPos which returns the position in the Stream 
 * @author Richard McCreadie and Craig Macdonald
  */
public class CountingInputStream extends FilterInputStream{
	/** number of bytes read or skipped */	
	protected long count = 0;
		
	/**
	 * Constructor - Calls Super Class
	 * @param _in InputStream to wrap
	 */
	public CountingInputStream (InputStream _in)
	{
		super(_in);
	}
	
	/**
	 * Constructor - Calls Super Class
	 * @param _in InputStream to wrap
	 * @param offset Offset in bytes for which the counter should start
	 */
	public  CountingInputStream (InputStream _in, long offset)
	{
		super(_in);
		count = offset;	
	}
	
	/**
	 * Reads the Next Byte from the Stream
	 */
	public int read() throws IOException
	{
		final int rtr = super.read();
		if (rtr != -1)
			count++;
		return rtr;
	}
	
	/**
	 * Reads the next Byte Array with Offset and Length from the Stream
	 */
	public int read(byte[] b, int off, int len)throws IOException
	{
		final int rtr = super.read(b, off, len);
		count += (long)rtr;
		return rtr;
	}
	
	/**
	 * Calls Skip(long n)
	 */
	public long skip(long n) throws IOException {
		final long rtr = super.skip(n);
		count += rtr;
		return rtr;
	}
	
	/**
	 * Returns the Position in the Stream
	 * @return Position in bytes since the start of the stream
	 */
	public long getPos()
	{
		return count;
	}
		
}
