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
 * The Original Code is HadoopUtility.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.utility.io;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobConfigurable;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.TaskAttemptID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

/** Utility class for the setting up and configuring of Terrier MapReduce jobs.
 * General scheme for a Hadoop Job
 * <code>
 * JobFactory jf = HadoopUtility.getJobFactory("TerrierJob");
 * JobConf jc = jf.newJob();
 * HadoopUtility.makeTerrierJob(jc);
 * &47;&47; populate jc
 * &47;&47; if an index is needed in the MR job:
 * HadoopUtility.toHConfiguration(index, jc);
 * Running rj = JobClient.runJob(jc);
 * HadoopUtility.finishTerrierJob(jc);
 * </code>
 * During a MR job, the configure method should call HadoopUtility.loadTerrierJob(jc);
 * To obtain an index, Index index = HadoopUtility.fromHConfiguration(jc);
 * @author Craig Macdonald
 * @since 2.2. 
 */
@SuppressWarnings("deprecation")
public class HadoopUtility {
	
	protected static final Logger logger = LoggerFactory.getLogger(HadoopUtility.class);

	/**
	 * A base class for a MapReduce job. It prepare Terrier IO access to the HDFS and
	 * performs configuration of the Map and Reduce classes.
	 * @author Richard McCreadie
	 *
	 */
	static abstract class MRJobBase implements JobConfigurable, Closeable
	{
		protected JobConf jc;
		/** {@inheritDoc} */
        public void configure(JobConf _jc) {
            this.jc = _jc;

            //1. configure application
            try{
                HadoopUtility.loadTerrierJob(_jc);
            } catch (Exception e) {
                throw new Error("Cannot load ApplicationSetup", e);
            }
			//2. configurure this class
            try{
                if (isMap(_jc))
                {
                    configureMap();
                } else {
                    configureReduce();
                }
            } catch (Exception e) {
                throw new Error("Cannot configure indexer", e);
            }
        }
		protected abstract void configureMap() throws IOException;
        protected abstract void configureReduce() throws IOException;

		/** Called at end of map or reduce task. Calls internally closeMap() or closeReduce() */
        public void close() throws IOException {
            if (isMap(jc))
            {
                closeMap();
            } else {
                closeReduce();
            }
        }

        protected abstract void closeMap() throws IOException;
        protected abstract void closeReduce() throws IOException;
		
	}

	/**
	 * Abstract class that provides default configure and close methods for a Reducer.
	 * @author Richard McCreadie
	 *
	 * @param <K1> key 1
	 * @param <V1> value 1
	 * @param <K2> key 2
	 * @param <V2> value 2
	 */
	public static abstract class ReduceBase<K1,V1,K2,V2> extends MRJobBase implements Reducer<K1,V1,K2,V2>
	{
		protected void configureMap() throws IOException {}	
		protected void closeMap() throws IOException {}
	}

	/**
	 * Abstract class that provides default configure and close methods for a Mapper.
	 * @author Richard McCreadie
	 *
	 * @param <K1> key 1
	 * @param <V1> value 1
	 * @param <K2> key 2
	 * @param <V2> value 2
	 */
	public static abstract class MapBase<K1,V1,K2,V2> extends MRJobBase implements Mapper<K1,V1,K2,V2>
	{
        protected void configureReduce() throws IOException {}
        protected void closeReduce() throws IOException {}
    }
	
	/** Handy base class for MapReduce jobs. */
	public static abstract class MapReduceBase<K1,V1,K2,V2,K3,V3> extends MRJobBase implements Mapper<K1,V1,K2,V2>, Reducer<K2,V2,K3,V3>
	{
	}
	
	/** Utility method to detect if a task is a Map task or not */
	public static final boolean isMap(JobConf jc) {
		return TaskAttemptID.forName(jc.get("mapred.task.id")).isMap();
	}
	
	/** Utility method to set MapOutputCompression if possible.
	 * In general, I find that MapOutputCompression fails for
	 * local job trackers, so this code checks the job tracker
	 * location first.
	 * @param conf JobConf of job.
	 * @return true if MapOutputCompression was set.
	 */
	public static boolean setMapOutputCompression(JobConf conf)
	{
		if (! conf.get("mapred.job.tracker").equals("local"))
		{
			conf.setMapOutputCompressorClass(GzipCodec.class);
			conf.setCompressMapOutput(true);
			return true;
		}
		return false;
	}
	
	/** Utility method to set JobOutputCompression if possible.
	 * In general, I find that JobOutputCompression fails for
	 * local job trackers, so this code checks the job tracker
	 * location first.
	 * @param conf JobConf of job.
	 * @return true if JobOutputCompression was set.
	 */
	public static boolean setJobOutputCompression(JobConf conf)
	{
		if (! conf.get("mapred.job.tracker").equals("local"))
		{
			FileOutputFormat.setCompressOutput(conf, true);
			FileOutputFormat.setOutputCompressorClass(conf, GzipCodec.class);
			return true;
		}
		return false;
	}

	/** Saves the current ApplicationSetup to the specified JobConf.
	 * After the JobConf job has run, use finishTerrierJob() to delete any
	 * leftover files */
	public static void makeTerrierJob(JobConf jobConf) throws IOException
	{
		if (jobConf.get("mapred.job.tracker").equals("local"))
			return;
		try{
			saveApplicationSetupToJob(jobConf, true);
			saveClassPathToJob(jobConf);
 		} catch (Exception e) {
			throw new WrappedIOException("Cannot HadoopUtility.makeTerrierJob", e);
		}
	}
	
	/** When the current ApplicationSetup has been saved to the JobConf, by makeTerrierJob(),
	 * use this method during the MR job to properly initialise Terrier.
	 */
	public static void loadTerrierJob(JobConf jobConf) throws IOException
	{
		if (jobConf.get("mapred.job.tracker").equals("local"))
			return;
		try{
			HadoopPlugin.setGlobalConfiguration(jobConf);
			loadApplicationSetup(jobConf);
		} catch (Exception e) {
			 throw new WrappedIOException("Cannot HadoopUtility.loadTerrierJob", e);
		}
	}
	
	/** Call this after the MapReduce job specified by jobConf has completed,
	 * to clean up any leftover files */
	public static void finishTerrierJob(JobConf jobConf) throws IOException
	{
		if (jobConf.get("mapred.job.tracker").equals("local"))
			return;
		deleteJobApplicationSetup(jobConf);
		removeClassPathFromJob(jobConf);
	}
	
	protected static void removeClassPathFromJob(JobConf jobConf) throws IOException
	{
		final String[] jars = findJarFiles(new String[]{
				System.getenv().get("CLASSPATH"),
				System.getProperty("java.class.path")
			});
		
		/**
		 * Remove from classpath hadoop libraries which are already present in a node classpath
		 */
		
		
		ArrayList<String> jarList = new ArrayList<String>(Arrays.asList(jars));
		
		List<String> hadoopJarList = new ArrayList<String>();
		
		// find all hadoop jar files. We use the structure of the lib folder to determine these
		String separator = ApplicationSetup.FILE_SEPARATOR;
		for (String candidateHadoopJar : jarList) {
			if (candidateHadoopJar.contains("lib"+separator+"hadoop"+separator)) {
				//System.err.println("Removing "+candidateHadoopJar+" from classpath");
				hadoopJarList.add(candidateHadoopJar);
			}
		}
		
		jarList.removeAll(hadoopJarList);
		
		final FileSystem defFS = FileSystem.get(jobConf);
		for (String jarFile : jarList)
		{
			Path srcJarFilePath = new Path("file:///"+jarFile);
			String filename = srcJarFilePath.getName();
			//for a given job, makeTemporaryFile will return the same temporary id
			Path tmpJarFilePath = makeTemporaryFile(jobConf, filename);
			defFS.delete(tmpJarFilePath, false);
		}
	}

	protected static void saveClassPathToJob(JobConf jobConf) throws IOException
	{
		logger.info("Copying classpath to job");
		if (jobConf.getBoolean("terrier.classpath.copied", false))
		{
			return;
		}
		jobConf.setBoolean("terrier.classpath.copied", true);
		final String[] jars = findJarFiles(new String[]{
				System.getenv().get("CLASSPATH"),
				System.getProperty("java.class.path")
			});
		final FileSystem defFS = FileSystem.get(jobConf);
		for (String jarFile : jars)
		{
			//logger.debug("Adding " + jarFile + " to job class path");
			Path srcJarFilePath = new Path("file:///"+jarFile);
			String filename = srcJarFilePath.getName();
			Path tmpJarFilePath = makeTemporaryFile(jobConf, filename);			
			defFS.copyFromLocalFile(srcJarFilePath, tmpJarFilePath);
			DistributedCache.addFileToClassPath(tmpJarFilePath, jobConf);
		}
		 DistributedCache.createSymlink(jobConf);
	}

	protected static String[] findJarFiles(String [] classPathLines)
	{
		Set<String> jars = new HashSet<String>();
		for (String locationsLine : classPathLines)
		{
			if (locationsLine == null)
				continue;
			for (String CPentry : locationsLine.split(":"))
			{
				if (CPentry.endsWith(".jar"))
					jars.add(new File(CPentry).getAbsoluteFile().toString());
			}
		}
		return jars.toArray(new String[0]);
	}

	protected static final String HADOOP_TMP_PATH = ApplicationSetup.getProperty("terrier.hadoop.io.tmpdir", "/tmp");
	protected static final String[] checkSystemProperties = {"file", "java", "line", "os", "path", "sun", "user"};
	protected static final Random random = new Random();

	protected static Path makeTemporaryFile(JobConf jobConf, String filename) throws IOException
	{
		final int randomKey = jobConf.getInt("terrier.tempfile.id", random.nextInt());
		jobConf.setInt("terrier.tempfile.id", randomKey);
		FileSystem defFS = FileSystem.get(jobConf);
        final Path tempFile = new Path(HADOOP_TMP_PATH + "/"+(randomKey)+"-"+filename);
        defFS.deleteOnExit(tempFile);
		return tempFile;
	}
	
	protected static void deleteJobApplicationSetup(JobConf jobConf) throws IOException
	{
		FileSystem remoteFS = FileSystem.get(jobConf);
		String copiedTerrierShare = jobConf.get("terrier.share.copied", null);
		if (copiedTerrierShare != null)
		{
			logger.debug("Removing temporary terrier.share at " + copiedTerrierShare);
			Files.delete(copiedTerrierShare);
		}
		for(String filename : new String[]{"terrier.properties", "system.properties"})
		{	
			Path p = findCacheFileByFragment(jobConf, filename);
			remoteFS.delete(p, false);
		}
	}
	
	protected static void saveApplicationSetupToJob(JobConf jobConf, boolean getFreshProperties) throws Exception
	{
		// Do we load a fresh properties File?
		//TODO fix, if necessary
		//if (getFreshProperties)
		//	loadApplicationSetup(new Path(ApplicationSetup.TERRIER_HOME));
		
		FileSystem remoteFS = FileSystem.get(jobConf);
		URI remoteFSURI = remoteFS.getUri();
		//make a copy of the current application setup properties, these may be amended
		//as some files are more globally accessible
		final Properties propertiesDuringJob = new Properties();
		Properties appProperties = ApplicationSetup.getProperties();
		for (Object _key: appProperties.keySet())
		{
			String key = (String)_key;
			propertiesDuringJob.put(key, appProperties.get(key));
		}

		//the share folder is needed during indexing, save this on DFS
		if (Files.getFileSystemName(ApplicationSetup.TERRIER_SHARE).equals("local"))
		{
			Path tempTRShare = makeTemporaryFile(jobConf, "terrier.share");
			propertiesDuringJob.setProperty("terrier.share", remoteFSURI.resolve(tempTRShare.toUri()).toString());
			if (Files.exists(ApplicationSetup.TERRIER_SHARE))
			{
				jobConf.set("terrier.share.copied", remoteFSURI.resolve(tempTRShare.toUri()).toString());
				logger.info("Copying terrier share/ directory ("+ApplicationSetup.TERRIER_SHARE+") to shared storage area ("+remoteFSURI.resolve(tempTRShare.toUri()).toString()+")");
				FileUtil.copy(
						FileSystem.getLocal(jobConf), new Path(ApplicationSetup.TERRIER_SHARE),
						remoteFS, tempTRShare,
						false, false, jobConf);
			}
			else
			{
				logger.warn("No terrier.share folder found at "+ApplicationSetup.TERRIER_SHARE+", copying skipped");
			}
		}

		//copy the terrier.properties content over
		Path tempTRProperties = makeTemporaryFile(jobConf, "terrier.properties");
		logger.debug("Writing terrier properties out to DFS "+tempTRProperties.toString());
		OutputStream out = remoteFS.create(tempTRProperties);
		remoteFS.deleteOnExit(tempTRProperties);
		propertiesDuringJob.store(out, "Automatically generated by HadoopUtility.saveApplicationSetupToJob()");
		out.close();
		out = null;
		DistributedCache.addCacheFile(tempTRProperties.toUri().resolve(new URI("#terrier.properties")), jobConf);
		DistributedCache.createSymlink(jobConf);
	
		//copy the non-JVM system properties over as well
		Path tempSysProperties = makeTemporaryFile(jobConf, "system.properties");	
		DataOutputStream dos = FileSystem.get(jobConf).create(tempSysProperties);
		logger.debug("Writing system properties out to DFS "+tempSysProperties.toString());
		for (Object _propertyKey : System.getProperties().keySet())
		{
			String propertyKey = (String)_propertyKey;
			if (! startsWithAny(propertyKey, checkSystemProperties))
			{
				dos.writeUTF(propertyKey);
				dos.writeUTF(System.getProperty(propertyKey));
			}
		}
		dos.writeUTF("FIN");
		dos.close();
		dos = null;
		DistributedCache.addCacheFile(tempSysProperties.toUri().resolve(new URI("#system.properties")), jobConf);
	}

	protected static Path findCacheFileByFragment(JobConf jc, String name) throws IOException
	{
		URI[] ps = DistributedCache.getCacheFiles(jc);
		URI defaultFS = FileSystem.getDefaultUri(jc);
		if (ps == null)
			return null;
		for (URI _p : ps)
		{
			final URI p = defaultFS.resolve(_p);
			if (p.getFragment().equals(name))
			{
				logger.debug("Found matching path in DistributedCache in search for "+name+" : " +new Path(p.getScheme(), p.getAuthority(), p.getPath()).toString());
				return new Path(p.getScheme(), p.getAuthority(), p.getPath());
			}
		}
		return null;
	}
	
	protected static void loadApplicationSetup(JobConf jobConf) throws IOException
	{
		logger.info("Reloading Application Setup");
		//we dont use Terrier's IO layer here, because it is not yet initialised
		FileSystem sharedFS = FileSystem.get(jobConf);
		Path terrierPropertiesFile =  findCacheFileByFragment(jobConf, "terrier.properties");	
		Path systemPropertiesFile = findCacheFileByFragment(jobConf, "system.properties");
	
		if (systemPropertiesFile != null && sharedFS.exists(systemPropertiesFile))
		{
			DataInputStream dis = sharedFS.open(systemPropertiesFile);
			while(true)
			{
				String key = dis.readUTF();
				if (key.equals("FIN"))
					break;
				String value = dis.readUTF();
				System.setProperty(key, value);
			}
			dis.close();
		}
		else
		{
			logger.warn("No system.properties file found at "+systemPropertiesFile);
		}

		if (terrierPropertiesFile != null && sharedFS.exists(terrierPropertiesFile))
		{
			ApplicationSetup.configure(sharedFS.open(terrierPropertiesFile));
		}
		else
		{
			throw new java.io.FileNotFoundException("No terrier.properties file found at "+terrierPropertiesFile);
		}
	}
	
	/** Get an Index saved to the specifified Hadoop configuration by toHConfiguration() */
	public static IndexOnDisk fromHConfiguration(Configuration c)
	{
		return Index.createIndex(c.get("terrier.index.path"), c.get("terrier.index.prefix"));
	}
	
	/** Puts the specified index onto the given Hadoop configuration */
	public static void toHConfiguration(Index i, Configuration c)
	{
		c.set("terrier.index.path", ((IndexOnDisk) i).getPath());
		c.set("terrier.index.prefix", ((IndexOnDisk) i).getPrefix());
	}
	
	/**
	 * Returns true if source contains any of the Strings held in checks. Case insensitive.
	 * @param source String to check
	 * @param checks Strings to check for
	 * @return true if source starts with one of checks, false otherwise.
	 */
	protected static boolean startsWithAny(String source, String[] checks) {
		for (String s:checks) {
			if (source.toLowerCase().startsWith(s.toLowerCase())) return true;
		}
		return false;
	}
}
