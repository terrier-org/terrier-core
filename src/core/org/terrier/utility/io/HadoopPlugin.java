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
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.utility.io;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.mapred.JobConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.terrier.utility.KillHandler;
import org.terrier.utility.Files.FSCapability;
import org.terrier.utility.KillHandler.Killable;

/** This class provides the main glue between Terrier and Hadoop. It has several main roles:<ol>
  * <li>Configure Terrier such that the Hadoop file systems can be accessed by Terrier.</li>
  * <li>Provide a means to access the Hadoop map-reduce cluster, using <a href="http://hadoop.apache.org/core/docs/current/hod.html">Hadoop on Demand (HOD)</a> if necessary.</li>
  * </ol>
  * <p><h3>Configuring Terrier to access HDFS</h3>
  * Terrier can access a Hadoop Distributed File System (HDFS), allowing collections and indices to be placed there.
  * To do so, ensure that your Hadoop <tt>conf/</tt> is on your CLASSPATH, and that the Hadoop plugin is loaded by Terrier,
  * by setting <tt>terrier.plugins=org.terrier.utility.io.HadoopPlugin</tt> in your <tt>terrier.properties</tt> file.
  * </p>
  * <p><h3>Configuring Terrier to access an existing Hadoop MapReduce cluster</h3>
  * Terrier can access an existing MapReduce cluster, as long as the <tt>conf/</tt> folder for Hadoop is on your CLASSPATH.
  * If you do not already have an existing Hadoop cluster, Terrier can be configured to use HOD, to build a temporary
  * Hadoop cluster from a PBS (Torque) cluster. To configure HOD itself, the reader is referred to the
  * <a href="http://hadoop.apache.org/core/docs/current/hod.html">HOD documentation</a>. To use HOD from Terrier,
  * set the following properties:
  * <ul>
  * <li><tt>plugin.hadoop.hod</tt> - path to the hod binary, normally $HADOOP_HOME/contrib/hod/bin. If unset, then HOD is presusmed
  * to be unconfigured.</li>
  * <li><tt>plugin.hadoop.hod.nodes</tt> - the number of nodes/CPUs that you want to request from the PBS Torque cluster. Defaults to 6.</li>
  * <li><tt>plugin.hadoop.hod.params</tt> - any additional options you want to set on the HOD command line. See the 
  * <a href="http://hadoop.apache.org/core/docs/current/hod_user_guide.html#Command+Line">HOD User guide</a> for examples.</li>
  * </ul>
  * </p><p><h3>Using Hadoop MapReduce from Terier</h3>
  * You should use the JobFactory provided by this class when creating a MapReduce job from Terrier. The JobFactory
  * creates a HOD session should one be required, and also configures jobs such that the Terrier environment can
  * be recreated on the execution nodes.
  * <pre>
  * HadoopPlugin.JobFactory jf = HadoopPlugin.getJobFactory("HOD-TerrierIndexing");
  * if (jf == null)
  *	 throw new Exception("Could not get JobFactory from HadoopPlugin");
  * JobConf conf = jf.newJob();
  * ....
  * jf.close(); //closing the JobFactory will ensure that the HOD session ends
  * </pre>
  * When using your own code in Terrier MapReduce jobs, ensure that you configure the Terrier application before
  * anything else:
  * <pre>
  * public void configure(JobConf jc)
  * {
  *	 try{
  *		 HadoopUtility.loadTerrierJob(jc);
  *	 } catch (Exception e) {
  *		 throw new Error("Cannot load ApplicationSetup", e);
  *	 }
  * }
  * </pre>
  * </p>
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
	

	/** a Job Factory is responsible for creating Terrier MapReduce jobs.
	  * This should be used when requesting a Terrier MapReduce job, as it
	  * adequately initialises the job, such that Terrier can run correctly.
	  */	
	public static abstract class JobFactory {
		/** Make a new job */
		public abstract JobConf newJob() throws Exception;
		
		/** Add additional informatino to a MapReduce job about the Terrier configuration */
		protected static void makeTerrierJob(JobConf jc) throws IOException {
			HadoopUtility.makeTerrierJob(jc);
		}
		
		/** Finish with this job factory. If the JobFactory was created using HOD, then
		  * the HOD job will also be ended */
		public abstract void close();
	}
	
	/** JobFactory that doesn't resort to HadoopOnDemand, and directly wraps a Configuration object */
	static class DirectJobFactory extends JobFactory {
		protected Configuration c;
		DirectJobFactory() { c = null; }
		DirectJobFactory(Configuration _c) { c = _c; }
		public JobConf newJob() throws Exception {
			JobConf rtr = c != null ? new JobConf(c) : new JobConf();
			makeTerrierJob(rtr);
			return rtr;
		}
		public void close() { }
	}
	
	private static final Random random = new Random();
	
	/** JobFactory that uses HadoopOnDemand */
	static class HODJobFactory extends JobFactory implements Killable {
		protected String hodConfLocation = null;
		protected String hodBinaryLocation = null;
		protected boolean connected = false;
		HODJobFactory(String _hodJobName, String _hodBinaryLocation, String[] hodParams, int HodNumNodes) throws Exception
		{
			hodBinaryLocation = _hodBinaryLocation;
			KillHandler.addKillhandler(this);
			doHod(_hodJobName, hodParams, HodNumNodes);
		}
		
		protected void doHod(String jobName, String[] hodParams, int NumNodes) throws Exception
		{
			if (jobName == null || jobName.length() == 0)
				jobName = "terrierHOD";	
			logger.info("Processing HOD for "+jobName+" at "+hodBinaryLocation + " request for " + NumNodes + " nodes");

			File hodDir = null;
			while (hodDir == null)
			{
				hodDir = new File(System.getProperty("java.io.tmpdir", "/tmp") + "/hod" + random.nextInt());
				if (hodDir.exists())
					hodDir = null;
			}
			if (! hodDir.mkdir())
			{
				throw new IOException("Could not create new HOD tmp dir at "+hodDir);
			}

			//build the HOD command
			String[] command = new String[8 + hodParams.length];
			command[0] = hodBinaryLocation;
			command[1] = "allocate";
			command[2] = "-d"; 
			command[3] = hodDir.getAbsolutePath();
			command[4] = "-n";
			command[5] = ""+NumNodes;
			command[6] = "-N";
			command[7] = jobName;
			int offset = 8;
			for(String param : hodParams)
				command[offset++] = param;

			//execute the command
			ProcessBuilder pb = new ProcessBuilder();
			pb.command(command);
			Process p = pb.start();
			
			//log all output from HOD
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String line = null;
				while ((line = br.readLine()) != null)
					logger.info(line);
				br.close();
			} catch(IOException ioe) {
				logger.warn("Problem reading error stream of HOD", ioe);
			}
			p.waitFor();
			
			//check for successfull HOD
			File hodConf = new File(hodDir, "hadoop-site.xml");
			if (! Files.exists((hodConf.toString())))
				throw new IOException("HOD did not produce a hadoop-site.xml");
			final int exitValue = p.exitValue();
			if (exitValue != 0)
			{
				throw new Exception("HOD allocation did not succeed (exit value was "+exitValue+")");
			}
			hodConfLocation = hodDir.getAbsolutePath();
			connected = true;
		}
		
		protected void disconnectHod() throws Exception
		{
			logger.info("Processing HOD disconnect");
			ProcessBuilder pb = new ProcessBuilder();
			pb.command(new String[]{hodBinaryLocation, "deallocate", "-d", hodConfLocation});
			Process p = pb.start();
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String line = null;
				while ((line = br.readLine()) != null)
					logger.info(line);
				br.close();
			} catch(IOException ioe) {
				logger.warn("Problem reading error stream of HOD", ioe);
			}
			p.waitFor();
			final int exitValue = p.exitValue();
			if (exitValue != 0)
			{
				logger.warn("HOD deallocate might not have succeeded (exit value was "+exitValue+")");
			}
			connected = false;
		}
		
		public JobConf newJob() throws Exception {
			JobConf rtr = new JobConf(hodConfLocation+"/hadoop-site.xml");
				makeTerrierJob(rtr);
			return rtr;
		}

		protected void finalize() {
			close();
		}
	
		public void close()	
		{
			if (connected)
				try{
					disconnectHod();
				} catch (Exception e) {
					logger.warn("Encoutered exception while closing HOD. A PBS job may need to be deleted.", e);
				} finally {
					KillHandler.removeKillhandler(this);
				}
		}

		public void kill() {
			close();
		}
	}
	
	/** Get a JobFactory with the specified session name. This method attempts three processes, in order:
	  * <ol>
	  * <li>If the current/default Hadoop configuration has a real Hadoop cluster Job Tracker configured, then
	  * that will be used. This requires that the <tt>mapred.job.tracker</tt> property in the haddop-site.xml
	  * be configured.</li>
	  * <li>Next, it will attempt to use HOD to build a Hadoop MapReduce cluster. This requies the Terrier property
	  * relating to HOD be configured to point to the location of the HOD binary - <tt>plugin.hadoop.hod</tt></li>
	  * <li>As a last resort, Terrier will use the local job tracker that Hadoop provides on the localhost. This is
	  * useful for unit testing, however it does not support multiple reducers.</li> 
	  * </ol>
	  */
	public static JobFactory getJobFactory(String sessionName) { 
		return getJobFactory(sessionName, false);
	}
	
	/** implements the obtaining of job factories */
	protected static JobFactory getJobFactory(String sessionName, boolean persistent) {
		if (persistent)//TODO
			throw new Error("Persistent JobFactory not yet supported, sorry");
		Configuration globalConf = getGlobalConfiguration();
		
		try {
			//JobConf jc_sampleConf = new JobConf();
			//see if the current hadoop configuration has a real job tracker configured
			String jt = globalConf.get("mapred.job.tracker");
			if (jt == null)
			{
				jt = new JobConf().get("mapred.job.tracker");
			}
			if (jt != null && ! jt.equals("local"))
			{
				if (logger.isDebugEnabled()) logger.debug("Default configuration has job tracker set to " + globalConf.get("mapred.job.tracker"));	
				return new DirectJobFactory(/*globalConf*/);
			}
			// if not, try HOD	
			String hod = ApplicationSetup.getProperty("plugin.hadoop.hod", null);
			String[] hodParams = ApplicationSetup.getProperty("plugin.hadoop.hod.params", "").split(" ");
			if (hod != null && hod.length() > 0)
			{
				int HodNodes = Integer.parseInt(ApplicationSetup.getProperty("plugin.hadoop.hod.nodes", ""+6));
				return new HODJobFactory(sessionName, hod, hodParams, HodNodes);
			}
			//as a last resort, use the local Hadoop job tracker
			logger.warn("No remote job tracker or HOD configuration found, using local job tracker");
			return new DirectJobFactory(globalConf);
		} catch (Exception e) {
			logger.warn("Exception occurred while creating JobFactory", e);
			return null;
		}
	}
	
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
