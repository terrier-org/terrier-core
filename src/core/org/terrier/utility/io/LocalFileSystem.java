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
 * The Original Code is LocalFileSystem.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald (craigm{at}dcs.gla.ac.uk)
 */
package org.terrier.utility.io;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.terrier.utility.Files.FSCapability;

/** This is a Terrier File Abstraction Layer implementation of the local file system. The file system implementation for the 
 * "file:" scheme can handle all file system capabilities (READ,WRITE,RANDOM_READ,RANDOM_WRITE,LS_DIR and STAT).
 * Generally speaking, this is a lightweight wrapper around the following four Java SE classes:
 * java.io.File and java.io.FileInputStream and java.io.FileOutputStream, java.io.RandomAccessFile.
 * <p></p>
 * <p>On Windows, this FileSystem also advertises support for all the single-letter file systems, eg A:, C:, D:</p>
 * @since 2.1
 * @author Craig Macdonald
 */
public class LocalFileSystem implements FileSystem, FSCapability
{
	
	/**
	 * A file that supports random access
	 * @author Richard McCreadie
	 *
	 */
	protected static class LocalRandomAccessFile 
		extends RandomAccessFile 
		implements RandomDataOutput
	{
		public LocalRandomAccessFile(String name, String mode) throws FileNotFoundException
		{
			super(name,mode);
		}
	}

	protected String normalise(String filename)
	{
		return filename.replaceFirst("^file:", "");
	}

	/** Get the name of the file system */
	public String name() {
		return "local";
	}

	/** Get the capabilities of the file system */
	public byte capabilities()
	{
		return READ|WRITE|RANDOM_READ|RANDOM_WRITE|LS_DIR|STAT|DEL_ON_EXIT;
	}
	
	/** Get the schemes */
	public String[] schemes()
	{
		if (System.getProperty("os.name").toLowerCase().indexOf("windows")!= -1)
			return new String[]{"file","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q",
				"r","s","t","u","v","w","x","y","z" };
		return new String[]{"file"};
	}
	
	/** Does the file exist? */
	public boolean exists(String filename)
	{
		return new File(normalise(filename)).exists();
	}

	/** returns true if filename can be read */
	public boolean canRead(String filename)
	{
		return new File(normalise(filename)).canRead();
	}

	/** returns true if filename can be written to */
	public boolean canWrite(String filename)
	{
		return new File(normalise(filename)).canWrite();
	}

	/** Opens an input file*/
	public InputStream openFileStream(String filename) throws IOException
	{
		return new FileInputStream(normalise(filename));
	}

	/** Opens an output file */
	public OutputStream writeFileStream(String filename) throws IOException
	{
		return new FileOutputStream(normalise(filename));
	}

	/** Opens an input random access file */
	public RandomDataInput openFileRandom(String filename) throws IOException
	{
		return new LocalRandomAccessFile(normalise(filename), "r");
	}

	/** Opens a writable random access file */
	public RandomDataOutput writeFileRandom(String filename) throws IOException
	{
		return new LocalRandomAccessFile(normalise(filename), "rw");
	}

	/** Delete a file */
	public boolean delete(String filename)
	{
		return new File(normalise(filename)).delete();
	}

	/** Delete the file on program exit */
	public boolean deleteOnExit(String pathname) throws IOException
	{
		new File(normalise(pathname)).deleteOnExit();
		return true;
	}

	/** Make a directory */
	public boolean mkdir(String filename)
	{
		return new File(normalise(filename)).mkdir();
	}

	/** Does the path point to a directory */
	public boolean isDirectory(String path)
	{
		return new File(normalise(path)).isDirectory();
	}

	/** What is the length of the file? */
	public long length (String filename)
	{
		return new File(normalise(filename)).length();
	}

	/** rename a file/dir to another name, on the same file system */
	public boolean rename(String source, String destination) throws IOException
	{
		return new File(normalise(source)).renameTo(new File(normalise(destination)));
	}

	/** whats the parent path to this path - eg directory containing a file */
    public String getParent(String path) throws IOException
	{
		return new File(normalise(path)).getParent();
	}

	/** list contents of a directory etc */
    public String[] list(String path) throws IOException
	{
		return new File(normalise(path)).list();
	}
}
