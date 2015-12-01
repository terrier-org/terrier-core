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
 * The Original Code is FileSystem.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald (craigm{at}dcs.gla.ac.uk)
 */
package org.terrier.utility.io;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

/** This is the Terrier File Abstraction Layer interface depicting the operations available for a file system.
 * @since 2.1
 * @author Craig Macdonald
 */
public interface FileSystem 
{
	/** returns a name for the filesystem */
	String name();
	/** capabilities of the filesystem */
	byte capabilities();
	/** URI schemes supported by this class */
	String[] schemes();
	/** returns true if the path exists */
	boolean exists(String filename) throws IOException;
	/** returns true if filename can be read */
	boolean canRead(String filename) throws IOException;
	/** returns true if filename can be written to */
	boolean canWrite(String filename) throws IOException;
	/** open a file of given filename for readinng */
	InputStream openFileStream(String filename) throws IOException;
	/** open a file for random input */
	RandomDataInput openFileRandom(String filename) throws IOException;
	/** open a file of given filename for writing */
	OutputStream writeFileStream(String filename) throws IOException;
	/** open a file of given filename for random writing */
	RandomDataOutput writeFileRandom(String filename) throws IOException;
	/** delete the named file */
	boolean delete(String filename) throws IOException;
	/** delete the file after the JVM exits */
	boolean deleteOnExit(String pathname) throws IOException;
	/** mkdir the specified path */
	boolean mkdir(String filename) throws IOException;
	/** returns the length of the specified file */
	long length(String filename) throws IOException;	
	/** returns true if path is a directory */
	boolean isDirectory(String path) throws IOException;
	/** rename a file/dir to another name, on the same file system */
	boolean rename(String source, String destination) throws IOException;
	/** whats the parent path to this path - eg directory containing a file */
	String getParent(String path) throws IOException;
	/** list contents of a directory etc */
	String[] list(String path) throws IOException;
}
