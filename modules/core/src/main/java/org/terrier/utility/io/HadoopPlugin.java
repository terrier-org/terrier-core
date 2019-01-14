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
 * The Original Code is HadoopPlugin.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.utility.io;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.terrier.utility.Files.FSCapability;

/** This class provides the main glue between Terrier and Hadoop. It has several main roles:<ol>
  * <li>Configure Terrier such that the Hadoop file systems can be accessed by Terrier.</li>
  * <li>Provide a means to access the Hadoop map-reduce cluster, using <a href="http://hadoop.apache.org/core/docs/current/hod.html">Hadoop on Demand (HOD)</a> if necessary.</li>
  * </ol>
  * <h3>Configuring Terrier to access HDFS</h3>
  * <p>Terrier can access a Hadoop Distributed File System (HDFS), allowing collections and indices to be placed there.
  * To do so, ensure that your Hadoop <tt>conf/</tt> is on your CLASSPATH, and that the Hadoop plugin is loaded by Terrier,
  * by setting <tt>terrier.plugins=org.terrier.utility.io.HadoopPlugin</tt> in your <tt>terrier.properties</tt> file.
  * </p>
  * 
  * @since 2.2
  * @author Craig Macdonald
  */
@SuppressWarnings("deprecation")
public class HadoopPlugin implements ApplicationSetup.TerrierApplicationPlugin
{
	/** instance of this class - it is a singleton */
	protected static HadoopPlugin singletonHadoopPlugin;
	/** main configuration object to use for Hadoop access */
	protected static Configuration singletonConfiguration;

	/** The logger used */
	protected static final Logger logger = LoggerFactory.getLogger(HadoopPlugin.class);
	
	/** Update the global Hadoop configuration in use by the plugin */
	public static void setGlobalConfiguration(Configuration _config) {
		singletonConfiguration = _config;
	}

	/** Obtain the global Hadoop configuration in use by the plugin */
	public static Configuration getGlobalConfiguration()
	{
		if (singletonConfiguration == null)
		{
			singletonConfiguration = new Configuration();
		}
		return singletonConfiguration;
	}

	static HadoopPlugin getHadoopPlugin()
	{
		return singletonHadoopPlugin;
	}
	
	/** configuration used by this plugin */
	protected Configuration config = null;
	/** distributed file system used by this plugin */
	protected org.apache.hadoop.fs.FileSystem hadoopFS = null; 
	
	/** Constructs a new plugin */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
			value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
			justification="Its a singleton")
	public HadoopPlugin() {
		singletonHadoopPlugin = this;
	}
	
	/** What is the String prefix of the default file system according to Hadoop */
	public static String getDefaultFileSystemPrefix()
	{
		return org.apache.hadoop.fs.FileSystem.getDefaultUri(singletonConfiguration).toString();
	}
	
	/** What is the URI of the default file system according to Hadoop */
	public static URI getDefaultFileSystemURI()
	{
		return org.apache.hadoop.fs.FileSystem.getDefaultUri(singletonConfiguration);
	}
	
	/** What is the default file system according to Hadoop 
	 * @throws IOException */
	public static org.apache.hadoop.fs.FileSystem getDefaultFileSystem() throws IOException
	{
		return org.apache.hadoop.fs.FileSystem.get(singletonConfiguration);
	}
	

	/** Wrapper around FSDataInputStream which implements RandomDataInput. */
	static class HadoopFSRandomAccessFile implements RandomDataInput
	//static class HadoopFSRandomAccessFile extends RandomAccessFile implements RandomDataInput
	{
		FSDataInputStream in;
		org.apache.hadoop.fs.FileSystem fs;
		String filename;
	
		public HadoopFSRandomAccessFile(org.apache.hadoop.fs.FileSystem _fs, String _filename) throws IOException
		{
			this.fs = _fs;
			this.filename = _filename;
			this.in = _fs.open(new Path(_filename));
		}

		public int read() throws IOException
		{
			return in.read();
		}

		public int read(byte b[], int off, int len) throws IOException
		{
			return in.read(in.getPos(), b, off, len);
		}

		public int readBytes(byte b[], int off, int len) throws IOException
		{
			return in.read(in.getPos(), b, off, len);
		}

		public void seek(long pos) throws IOException
		{
			in.seek(pos);
		}

		public long length() throws IOException
		{
			return fs.getFileStatus(new Path(filename)).getLen();
		}

		public void close() throws IOException
		{
			in.close();
		}
		
		// implementation from RandomAccessFile
		public final double readDouble() throws IOException {
			return in.readDouble();
		}

		public final int readUnsignedShort() throws IOException {
			return in.readUnsignedShort();
		}

		public final short readShort() throws IOException {
			return in.readShort();
		}

		public final int readUnsignedByte() throws IOException {
			return in.readUnsignedByte();
		}

		public final byte readByte() throws IOException {
			return in.readByte();
		}

		public final boolean readBoolean() throws IOException {
			return in.readBoolean();
		}
		
		public final int readInt() throws IOException {
		return in.readInt();
		}

		public final long readLong() throws IOException {
			return in.readLong();
		}
		
		public final float readFloat() throws IOException {
		return in.readFloat();
		}

		public final void readFully(byte b[]) throws IOException {
		in.readFully(b);
		}

		public final void readFully(byte b[], int off, int len) throws IOException {
		in.readFully(b,off,len);
		}

		public int skipBytes(int n) throws IOException {
		return in.skipBytes(n);
		}

		public long getFilePointer() throws IOException
		{
			return in.getPos();
		}
		public final char readChar() throws IOException {
			return in.readChar();
		}

		public final String readUTF() throws IOException {
			return in.readUTF();
		}

		public final String readLine() throws IOException {
			return in.readLine();
		}
	}

	/** Initialises the Plugin, by connecting to the distributed file system */
	public void initialise() throws Exception
	{
		config = getGlobalConfiguration();

		final org.apache.hadoop.fs.FileSystem DFS = hadoopFS = org.apache.hadoop.fs.FileSystem.get(config);

		FileSystem terrierDFS = new FileSystem() 
		{
			public String name()
			{
				return "hdfs";
			}

			/** capabilities of the filesystem */
			public byte capabilities() 
			{
				return FSCapability.READ | FSCapability.WRITE | FSCapability.RANDOM_READ 
					| FSCapability.STAT | FSCapability.DEL_ON_EXIT | FSCapability.LS_DIR;
			}
			public String[] schemes() { return new String[]{"dfs", "hdfs"}; }
	
			/** returns true if the path exists */
			public boolean exists(String filename) throws IOException
			{
				if (logger.isDebugEnabled()) logger.debug("Checking that "+filename+" exists answer="+DFS.exists(new Path(filename)));
				return DFS.exists(new Path(filename));
			}

			/** open a file of given filename for reading */
			public InputStream openFileStream(String filename) throws IOException
			{
				if (logger.isDebugEnabled()) logger.debug("Opening "+filename);
				return DFS.open(new Path(filename));
			}
			/** open a file of given filename for writing */
			public OutputStream writeFileStream(String filename) throws IOException
			{
				if (logger.isDebugEnabled()) logger.debug("Creating "+filename);
				return DFS.create(new Path(filename));
			}

			public boolean mkdir(String filename) throws IOException
			{
				return DFS.mkdirs(new Path(filename));
			}

			public RandomDataOutput writeFileRandom(String filename) throws IOException
			{
				throw new IOException("HDFS does not support random writing");
			}

			public RandomDataInput openFileRandom(String filename) throws IOException
			{
				return new HadoopFSRandomAccessFile(DFS, filename);
			}

			public boolean delete(String filename) throws IOException
			{
				return DFS.delete(new Path(filename), true);
			}

			public boolean deleteOnExit(String filename) throws IOException
			{
				return DFS.deleteOnExit(new Path(filename));
			}

			public String[] list(String path) throws IOException
			{
				final FileStatus[] contents = DFS.listStatus(new Path(path));
				if (contents == null)
					throw new FileNotFoundException("Cannot list path " + path);
				final String[] names = new String[contents.length];
				for(int i=0; i<contents.length; i++)
				{
					names[i] = contents[i].getPath().getName();
				}
				return names;
			}

			public String getParent(String path) throws IOException
			{
				return new Path(path).getParent().getName();
			}

			public boolean rename(String source, String destination) throws IOException
			{
				return DFS.rename(new Path(source), new Path(destination));
			} 

			public boolean isDirectory(String path) throws IOException
			{
				return DFS.getFileStatus(new Path(path)).isDir();
			}

			public long length(String path) throws IOException
			{
				return DFS.getFileStatus(new Path(path)).getLen();
			}

			public boolean canWrite(String path) throws IOException
			{
				return DFS.getFileStatus(new Path(path)).getPermission().getUserAction().implies(FsAction.WRITE);
			}

			public boolean canRead(String path) throws IOException
			{
				return DFS.getFileStatus(new Path(path)).getPermission().getUserAction().implies(FsAction.READ);
			}
		};
		Files.addFileSystemCapability(terrierDFS);
	}

	/** Returns the Hadoop configuration underlying this plugin instance */
	public Configuration getConfiguration()
	{
		return config;
	}
}
