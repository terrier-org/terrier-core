
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
 * The Original Code is HTTPFileSystem.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald (craigm{at}dcs.gla.ac.uk)
 */
package org.terrier.utility.io;
import org.terrier.utility.Files.FSCapability;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Implements a read-only HTTP file system for Terrier. Files can be read directly from a URL
 * @author Craig Macdonald
 * @since 2.1
 */
public class HTTPFileSystem implements FileSystem
{
	/** Create a new HTTPFileSystem */
	public HTTPFileSystem()
	{}

	/** return the name of the file system */
	public String name() 
	{
		return "HTTP";
	}

	/** capabilities of the filesystem */
	public byte capabilities()
	{
		return FSCapability.READ;
	}

	/** URI schemes supported by this class: http, ftp, https */
	public String[] schemes()
	{
		return new String[]{"http","ftp","https"};
	}

	/** returns true if the path exists */
	public boolean exists(String filename) throws IOException
	{
		throw new IOException("exists support for HTTP not yet implemented");
	}

	/** open a file of given filename for reading */
	public InputStream openFileStream(String filename) throws IOException
	{
		URL url;
		try{
			// Create an URL instance
			url = new URL(filename);
		} catch (java.net.MalformedURLException mue) {
			throw new IOException("Invalid URL "+ filename);
		}
		// Get an input stream for reading
		return url.openStream();
	}

	/** open a file for random input */
	public RandomDataInput openFileRandom(String filename) throws IOException
	{
		throw new IOException("Cannot random read by HTTP");
	}

	/** open a file of given filename for writing */
	public OutputStream writeFileStream(String filename) throws IOException
	{
		throw new IOException("Cannot write by HTTP");
	}

	/** open a file of given filename for random writing */
	public RandomDataOutput writeFileRandom(String filename) throws IOException
	{
		throw new IOException ("Cannot random write by HTTP");
	}

	/** delete the named file */
	public boolean delete(String filename) throws IOException
	{
		throw new IOException("Cannot delete file by HTTP");
	}

	/** delete the file when the JVM exits */
	public boolean deleteOnExit(String pathname) throws IOException
	{
		throw new IOException("Cannot delete anything, now or later, by HTTP");
	}

	/** mkdir the specified path */
	public boolean mkdir(String filename) throws IOException
	{
		throw new IOException("cannot mkdir by HTTP");
	}

	/** returns true if filename can be read */
	public boolean canRead(String filename)
	{
		try{
			// Create an URL instance
			new URL(filename);
		} catch (java.net.MalformedURLException mue) {
			return false; 
		}
		return true;
	}

	/** returns true if filename can be written to */
	public boolean canWrite(String filename)
	{
		return false;
	}
	/** returns the length of the specified file */
	public long length(String filename) throws IOException
	{
		return -1;
	}
	/** returns true if path is a directory */
	public boolean isDirectory(String path) throws IOException
	{
		return false;
	}
	/** rename a file/dir to another name, on the same file system */
	public boolean rename(String source, String destination) throws IOException
	{
		throw new IOException("Rename not supported by HTTP");
	}
	/** whats the parent path to this path - eg directory containing a file */
	public String getParent(String path) throws IOException
	{
		return path.replaceAll("/[^/]+","");
	}
	/** list contents of a directory etc */
	public String[] list(String path) throws IOException
	{
		return new String[0];
	}


}
