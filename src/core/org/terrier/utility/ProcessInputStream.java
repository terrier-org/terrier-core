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
 * The Original Code is ProcessInputStream.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.utility;

import java.io.InputStream;
import java.io.IOException;

/** Reads a stream from a process
 * @author Craig Macdonald
 */
public class ProcessInputStream extends InputStream
{
	protected final InputStream in;
	protected final Process p;
	/**
	 * constructor
	 * @param command
	 * @param file
	 * @throws IOException
	 */
	public ProcessInputStream(String command, String file) throws IOException
	{
		p = Runtime.getRuntime().exec(command + " " + file);
		in = p.getInputStream();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void close() throws IOException
	{
		in.close();
		try{
			p.waitFor(); //wait for completion
		} catch (InterruptedException ie) {
		} finally {
			p.destroy();
		}
		//int exitCode = p.exitValue();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public int read() throws IOException
	{
		return in.read();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public int read(byte[] b, int off, int len) throws IOException
	{
		return in.read(b,off,len);
	}
	/** 
	 * {@inheritDoc} 
	 */
	public int read(byte[] b) throws IOException
	{
		return in.read(b);
	}
	/** 
	 * {@inheritDoc} 
	 */
	public int available() throws IOException
	{
		return in.available();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void mark(int readlimit) {}
	/** 
	 * {@inheritDoc} 
	 */
	public void reset() throws IOException {}
	/** 
	 * {@inheritDoc} 
	 */
	public boolean markSupported() {return false; }
	/** 
	 * {@inheritDoc} 
	 */
	public long skip(long n) throws IOException
	{
		return in.skip(n);
	}
}
