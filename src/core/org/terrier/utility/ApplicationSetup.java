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
 * The Original Code is ApplicationSetup.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 *   Ben He <ben{a.}dcs.gla.ac.uk>
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.utility;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * <p>This class retrieves and provides access
 * to all the constants and parameters for
 * the system. When it is statically initialised,
 * it loads the properties file specified by the system property
 * <tt>terrier.setup</tt>. If this is not specified, then the default value is 
 * the value of the <tt>terrier.home</tt> system property, appended by <tt>etc/terrier.properties</tt>.
 * <BR/>
 * eg <tt>java -Dterrier.home=$TERRIER_HOME -Dterrier.setup=$TERRIER_HOME/etc/terrier.properties TrecTerrier </tt>
 * </p><p>
 * <b>System Properties used:</b>
 * <table><tr><td>
 * <tt>terrier.setup</tt></td><td>Specifies where the terrier.properties file can be found.
 * </td></tr>
 * <tr><td><tt>terrier.home</tt></td><td>Specified where Terrier has been installed, if the terrier.properties
 * file cannot be found, or the terrier.properties file does not specify the <tt>terrier.home</tt> in it.
 * <br><b>NB:</b>In the future, this may further default to $TERRIER_HOME from the environment.
 * </td><tr><td><tt>file.separator</tt></td><td>What separates directory names in this platform. Set automatically by Java</td></tr>
 * <tr><td><tt>line.separator</tt></td><td>What separates lines in a file on this platform. Set automatically by Java</td>
 * </table>
 * </p><p>
 * In essence, for Terrier to function properly, you need to specify one of the following on the command line:
 * <ul><li><tt>terrier.setup</tt> pointing to a terrier.properties file containing a <tt>terrier.home</tt> value.
 * </li>OR<li><tt>terrier.home</tt>, and Terrier will use a properties file at etc/terrier.properties, if it finds one.</li></ul>
 * </p>
 * <p>Any property defined in the properties file can be overridden as follows:</p>
 * <ul>
 * <li>If the system property <tt>terrier.usecontext</tt> is equal to <tt>true</tt>, then a Context
 * object is used to override the properties defined in the file.</li>
 * <li>If the system property <tt>terrier.usecontext</tt> is equal to <tt>false</tt>, then 
 * system properties are used to override the properties defined in the file.</li>
 * </ul>
 * @author Gianni Amati, Vassilis Plachouras, Ben He, Craig Macdonald
  */
public class ApplicationSetup {
	
	/** Interface for plugins. Plugins are loaded at initialisation time,
	 * specified by the property <tt>terrier.plugins</tt>
	 * @since 2.2
	 */
	public interface TerrierApplicationPlugin
	{
		/** Called by ApplicationSetup to initialise the plugin */
		void initialise() throws Exception;
	}
	
	/** Current Terrier version */
	public static final String TERRIER_VERSION = org.terrier.Version.VERSION;
	static Logger logger = null;
	
	/** Default log4j config Terrier loads if no TERRIER_ETC/terrier-log.xml file exists 
	* @since 1.1.0
	*/
	public static final String DEFAULT_LOG4J_CONFIG = 
		  "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
		+ "<!DOCTYPE log4j:configuration SYSTEM \"log4j.dtd\">" 
		+ "<log4j:configuration xmlns:log4j=\"http://jakarta.apache.org/log4j/\">"
		+ " <appender name=\"console\" class=\"org.apache.log4j.ConsoleAppender\">"
		+ "  <param name=\"Target\" value=\"System.err\"/>"
		+ "  <layout class=\"org.apache.log4j.SimpleLayout\"/>"
		+ " </appender>"
		+ " <root>"
		+ "  <priority value=\"info\" />"
		+ "  <appender-ref ref=\"console\" />"
		+ " </root>"
		+ "</log4j:configuration>";
	

	/** 
	 * The properties object in which the 
	 * properties from the file are read.
	 */
	protected static final Properties appProperties = new Properties();
	protected static final Properties UsedAppProperties = new Properties();;
	//Operating system dependent constants
	
	/**
	 * The file separator used by the operating system. Defaults to
	 * the system property <tt>file.separator</tt>.
	 */
	public static String FILE_SEPARATOR = System.getProperty("file.separator");
	
	/**
	 * The new line character used by the operating system. Defaults to
	 * the system property <tt>line.separator</tt>.
	 */
	public static String EOL = System.getProperty("line.separator");
	//Application specific constants. Should be specified in the properties file.
	
	/**
	 * The directory under which the application is installed.
	 * It corresponds to the property <tt>terrier.home</tt> and it
	 * should be set in the properties file, or as a property on the
	 * command line.
	 */
	public static String TERRIER_HOME; 
	
	/**
	 * The directory under which the configuration files 
	 * of Terrier are stored. The corresponding property is 
	 * <tt>terrier.etc</tt> and it should be set
	 * in the properties file. If a relative path is given, 
	 * TERRIER_HOME will be prefixed. 
	 */
	public static String TERRIER_ETC;
	/**
	 * The name of the directory where installation independent
	 * read-only data is stored. Files like stopword lists, and
	 * example and testing data are examples. The corresponding
	 * property is <tt>terrier.share</tt> and its default value is
	 * <tt>share</tt>. If a relative path is given, then TERRIER_HOME
	 * will be prefixed. */
	public static String TERRIER_SHARE;
	/**
	 * The name of the directory where the data structures
	 * and the output of Terrier are stored. The corresponding 
	 * property is <tt>terrier.var</tt> and its default value is 
	 * <tt>var</tt>. If a relative path is given, 
	 * TERRIER_HOME will be prefixed.
	 */
	public static String TERRIER_VAR;
	
	/**
	 * The name of the directory where the inverted
	 * file and other data structures are stored.
	 * The default value is InvFileCollection but it
	 * can be overridden with the property <tt>terrier.index.path</tt>.
	 * If a relative path is given, TERRIER_VAR will be prefixed.
	 */
	public static String TERRIER_INDEX_PATH;
	
	/**
	 * The name of the file that contains the
	 * list of resources to be processed during indexing.
	 * The contents of this file are collection implementation
	 * dependent. For example, for a TREC collection, this file
	 * must contain the list of files to index.
	 * The corresponding property is <tt>collection.spec</tt>
	 * and by default its value is <tt>collection.spec</tt>.
	 * If a relative path is given, TERRIER_ETC will be prefixed.
	 */
	public static String COLLECTION_SPEC;
	
	
	
	//TREC SPECIFIC setup
	/**
	 * The name of the directory where the results
	 * are stored. The corresponding property is 
	 * <tt>trec.results</tt> and the default value is 
	 * <tt>results</tt>. If a relative path is given, 
	 * TERRIER_VAR will be prefixed.
	 */
	public static String TREC_RESULTS;

	
	/** 
	 * The suffix of the files, where the results are stored.
	 * It corresponds to the property <tt>trec.results.suffix</tt>
	 * and the default value is <tt>.res</tt>. 
	 */
	public static String TREC_RESULTS_SUFFIX;

	//end of TREC specific section
		
	

	/** The maximum size of a term. It corresponds to the the property 
	  * <tt>max.term.length</tt>, and the default value is 20.
	  * @since 1.1.0 */
	public static int MAX_TERM_LENGTH;
	
	/** 
	 * Ignore or not empty documents. That is, if it is true, then a document 
	 * that does not contain any terms will have a corresponding entry in the 
	 * .docid file and the total number of documents in the statistics will be
	 * the total number of documents in the collection, even if some of them 
	 * are empty. It corresponds to the property <tt>ignore.empty.documents</tt>
	 * and the default value is false.
	 */
	public static boolean IGNORE_EMPTY_DOCUMENTS;
	/** 
	 * The prefix of the data structures' filenames. 
	 * It corresponds to the property <tt>terrier.index.prefix</tt>
	 * and the default value is <tt>data</tt>.
	 */
	public static String TERRIER_INDEX_PREFIX;
	
		//query expansion properties
	/** 
	 * The number of terms added to the original query. 
	 * The corresponding property is <tt>expansion.terms</tt>
	 * and the default value is <tt>10</tt>.
	 */
	public static int EXPANSION_TERMS;
		
	/**
	 * The number of top ranked documents considered for 
	 * expanding the query. The corresponding property is 
	 * <tt>expansion.documents</tt> and the default value is <tt>3</tt>.
	 */
	public static int EXPANSION_DOCUMENTS;
	

	//block related properties
	/** 
	 * The size of a block of terms in a document.
	 * The corresponding property is <tt>blocks.size</tt>
	 * and the default value is 1.
	 */
	public static int BLOCK_SIZE;
	
	/**
	 * The maximum number of blocks in a document.
	 * The corresponding property is <tt>blocks.max</tt>
	 * and the default value is 100000.
	 */
	public static int MAX_BLOCKS;
	
	/** 
	 * Specifies whether block information will 
	 * be used for indexing. The corresponding property is
	 * <tt>block.indexing</tt> and the default value is false.
	 * The value of this property cannot be modified after
	 * the index of a collection has been built.
	 */
	public static boolean BLOCK_INDEXING = false;
	
	/** 
	 * Specifies whether fields will be used for querying. 
	 * The corresponding property is <tt>field.querying</tt> and 
	 * the default value is false.
	 */
	public static boolean FIELD_QUERYING = false;
	
	//new
	/**
	 * Memory threshold in the single pass inversion method. If a memory check is below this value, the postings
	 * in memory are written to disk. Default is 50 000 000 (50MB) and 100 000 000 (100MB) for 32bit and 64bit 
	 * JVMs respectively, and can be configured using the property
	 * <tt>memory.reserved</tt>.
	 */
	public static int MEMORY_THRESHOLD_SINGLEPASS;
	
	/**
	 * Number of documents between each memory check in the single pass inversion method. The default value is 20,
	 * and this can be configured using the property <tt>docs.check</tt>.
	 */
	public static int DOCS_CHECK_SINGLEPASS;
	
	
	/** Checks whether a context is used instead of the properties file */
	private static boolean useContext = false;

	/** The configuration file used by log4j */
	public static String LOG4J_CONFIG = null;
	
	/**	
	 * The context that replaces the properties file if the 
	 * property <tt>terrier.usecontext</tt> is equal to <tt>true</tt>.
	 */
	private static Context envCtx = null;
	
	static {
		bootstrapInitialisation();
	}
	/** forces ApplicatinSetup initilisation */
	public static void bootstrapInitialisation()
	{
		useContext = Boolean.parseBoolean(System.getProperty("terrier.usecontext", "false"));

		String propertiesFile = null;
		String terrier_home = null;
		String terrier_etc = null;
		try {
			if (useContext)
			{
				 //
				Context initCtx = null;
				try{
					initCtx = (Context)( new InitialContext());
					envCtx = (Context) initCtx.lookup("java:comp/env");
					
				}catch(NamingException ne) {
					throw new RuntimeException("NamingException loading an InitialContext or EnvironmentContext",ne);
				}
				try{
					terrier_home = (String)envCtx.lookup("terrier.home");
				}catch(NamingException ne) {
					throw new RuntimeException("NamingException finding terrier variables from envCtx",ne);
				}
				try{
					terrier_etc = (String)envCtx.lookup("terrier.etc");
				} catch(NamingException ne) {
					throw new RuntimeException("NamingException finding terrier variables from envCtx",ne);
				}
				try{
					propertiesFile = (String)envCtx.lookup("terrier.setup");
				}catch(NamingException ne) {
					throw new RuntimeException("NamingException finding terrier variables from envCtx",ne);
				}
				if (propertiesFile == null)
					if (terrier_etc == null)
						terrier_etc = terrier_home +FILE_SEPARATOR+"etc";
					propertiesFile = terrier_etc+FILE_SEPARATOR+"terrier.properties";				
			}
			else
			{
				terrier_home = System.getProperty("terrier.home", "");
				terrier_etc = System.getProperty("terrier.etc", terrier_home +FILE_SEPARATOR+"etc");
				propertiesFile = System.getProperty("terrier.setup", terrier_etc + FILE_SEPARATOR+"terrier.properties");
			}
	
			//if system property terrier.setup is not specified, then it is 
			//assumed that the properties file is at ./etc/terrier.properties
	
			//System.err.println("Properties file is "+propertiesFile);	
			clearAllProperties();
			TERRIER_HOME = getProperty("terrier.home", terrier_home);
			FileInputStream in = new FileInputStream(propertiesFile);
			configure(new BufferedInputStream(in));
			in.close();
		} catch (java.io.FileNotFoundException fnfe) {
			System.out.println("WARNING: The file terrier.properties was not found at location "+propertiesFile);
			System.out.println(" Assuming the value of terrier.home from the corresponding system property.");
		} catch (java.io.IOException ioe) {
			System.err.println(
				"Input/Output Exception during initialization of ");
			System.err.println("org.terrier.utility.ApplicationSetup: "+ioe);
			System.err.println("Stack trace follows.");
			ioe.printStackTrace();
		}
		/* 
		 * The property terrier.home does not have a default value, so it has
		 * to be specified by the user in the terrier.properties file. If there
		 * is no terrier.properties specified, then we try to read a value from 
		 * the system property terrier.home. Ideally, the value of terrier.home
		 * would be $ENV{TERRIER_HOME} but java geniuses, in their infinite wisdom
		 * have deprecated System.getEnv() in Java 1.4. 
		 */
		TERRIER_HOME = getProperty("terrier.home", terrier_home);
		if (TERRIER_HOME.equals("")) {
			System.err.println("Please ensure that the property terrier.home");
			System.err.println("is specified in the file terrier.properties,");
			System.err.println("or as a system property in the command line.");
		}
		loadCommonProperties();
	}
	
	/** Loads the ApplicationSetup variables, e.g. ApplicationSetup.TERRIER_HOME */
	public static void loadCommonProperties()
	{
		TERRIER_ETC = makeAbsolute( getProperty("terrier.etc","etc"), TERRIER_HOME);
		TERRIER_VAR = makeAbsolute( getProperty("terrier.var","var"), TERRIER_HOME);
		TERRIER_SHARE = makeAbsolute( getProperty("terrier.share", "share"), TERRIER_HOME);
		TERRIER_INDEX_PATH = makeAbsolute(getProperty("terrier.index.path", "index"), TERRIER_VAR); 
		TERRIER_INDEX_PREFIX = getProperty("terrier.index.prefix", "data");
				
		//TREC specific
		
		TREC_RESULTS = makeAbsolute(getProperty("trec.results", "results"), TERRIER_VAR);		
		TREC_RESULTS_SUFFIX = getProperty("trec.results.suffix", ".res");
			
		//The following properties specify the filenames and suffixes
		COLLECTION_SPEC = makeAbsolute(getProperty("collection.spec", "collection.spec"), TERRIER_ETC);
	
				
		//if a document is empty, that is it does not contain any terms, 
		//we have the option to add it to the index, or not. By default, 
		//empty documents are added to the index.
		IGNORE_EMPTY_DOCUMENTS = Boolean.parseBoolean(getProperty("ignore.empty.documents", "false"));
		
		//During the indexing process, we process and create temporary structures
		//for bundle.size files.
		//BUNDLE_SIZE = Integer.parseInt(getProperty("bundle.size", "2000"));
		
		//the maximum size of a term (string)
		MAX_TERM_LENGTH = Integer.parseInt(getProperty("max.term.length", "20"));

		//the maximum number of bytes used to store a document number.
		//DOCNO_BYTE_LENGTH = Integer.parseInt(getProperty("docno.byte.length", "20"));	

		

		//query expansion properties
		EXPANSION_TERMS = Integer.parseInt(getProperty("expansion.terms", "10"));
		EXPANSION_DOCUMENTS = Integer.parseInt(getProperty("expansion.documents", "3"));
		//html tags and proximity related properties		
		BLOCK_INDEXING = Boolean.parseBoolean(getProperty("block.indexing", "false"));
		BLOCK_SIZE = Integer.parseInt(getProperty("blocks.size", "1"));
		MAX_BLOCKS = Integer.parseInt(getProperty("blocks.max", "100000"));
		FIELD_QUERYING = Boolean.parseBoolean(getProperty("field.querying", "false"));
	
		//double the amount of memory if using 64bit JVM.	
		MEMORY_THRESHOLD_SINGLEPASS = UnitUtils.parseInt(getProperty("memory.reserved", 
			System.getProperty("sun.arch.data.model", "32").equals("64") ? "100Mi" : "50Mi")); 
		DOCS_CHECK_SINGLEPASS = Integer.parseInt(getProperty("docs.check", "20"));
		
		
		//setup the logger for this class
		logger = LoggerFactory.getLogger(ApplicationSetup.class);
		//setup any plugins
		setupPlugins();
	}
	
	/** Loads the common Terrier properties from the specified InputStream */
	public static void configure(InputStream propertiesStream) throws IOException
	{
		appProperties.load(propertiesStream);
		loadCommonProperties();
	}
	
	/** 
	 * Returns the value for the specified property, given 
	 * a default value, in case the property was not defined
	 * during the initialization of the system.
	 * 
	 * The property values are read from the properties file. If the value 
	 * of the property <tt>terrier.usecontext</tt> is true, then the properties
	 * file is overridden by the context. If the value of the property 
	 * <tt>terrier.usecontext</tt> is false, then the properties file is overridden 
	 * @param propertyKey The property to be returned
	 * @param defaultValue The default value used, in case it is not defined
	 * @return the value for the given property.
	 */
	public static String getProperty(String propertyKey, String defaultValue) {
		String propertyValue = appProperties.getProperty(propertyKey, defaultValue);
		if (useContext) {//context is used
			try{
				propertyValue = (String)envCtx.lookup(propertyKey);
				if (propertyValue != null)
	                UsedAppProperties.setProperty(propertyKey, propertyValue);
			}catch(NamingException ne) {
				//in case of an exception, ie the property is not defined 
				//in the context, use the value from the properties file,
				//or the default value.
			}
			
		} else { 
			propertyValue = System.getProperty(propertyKey, propertyValue);
			
			if (propertyValue != null)
				UsedAppProperties.setProperty(propertyKey, propertyValue);
			
			//in case there is no system property, the returned property value
			//is the one read from the properties file, or the default value.
		}
		return propertyValue;
	}

	/** Returns a properties object detailing all the properties fetched during the lifetime of this class.
	  * It is of note that this is NOT the underlying appProperties table, as to update that would mean that
	  * properties fetched using their defaults, could not have different defaults in different places. */
	public static Properties getUsedProperties()
	{
		return UsedAppProperties;
	}
	
	/** Returns the underlying properties object for ApplicationSetup */
	public static Properties getProperties()
	{
		return appProperties;
	}
	
	/**
	 * Sets a value for the specified property. The properties
	 * set with this method are not saved in the properties file.
	 * @param propertyKey the name of the property to set.
	 * @param value the value of the property to set.
	 */
	public static void setProperty(String propertyKey, String value) {
		appProperties.setProperty(propertyKey, value);
	}

	/** set a property value only if it has not already been set 
	 * @param propertyKey the name of the property to set.
	 * @param defaultValue the value of the property to set.
	 */
	public static void setDefaultProperty(String propertyKey, String defaultValue) {
		if (getProperty(propertyKey,null) != null)
			setProperty(propertyKey, defaultValue);
	}
	
	/** list of loaded plugins */
	protected static List<TerrierApplicationPlugin> loadedPlugins = null;
	/** Calls the initialise method of any plugins named in terrier.plugins */
	protected static void setupPlugins()
	{
		loadedPlugins  = new LinkedList<TerrierApplicationPlugin>();
		final String[] pluginNames = getProperty("terrier.plugins", "").split("\\s*,\\s*");
		for (String pluginName : pluginNames)
		{
			if (pluginName.length() == 0)
				continue;
			if (pluginName.startsWith("uk.ac.gla.terrier.utility"))
				pluginName = pluginName.replaceFirst("uk.ac.gla.terrier.utility", "org.terrier.utility");
			try{
				TerrierApplicationPlugin plugin = Class.forName(pluginName).asSubclass(TerrierApplicationPlugin.class).newInstance();
				plugin.initialise();
				loadedPlugins.add(plugin);
			} catch (Exception e) {
				logger.warn("Problem loading plugin named "+ pluginName, e);
			}
		}
	}

	/** Return a loaded plugin by name. Returns null if a plugin
	  * of that name has not been loaded */
	public TerrierApplicationPlugin getPlugin(String name)
	{
		for (TerrierApplicationPlugin p : loadedPlugins)
			if (p.getClass().getName().equals(name))
				return p;
		return null;
	}
	
	/**
	 * Checks whether the given filename is absolute and if not, it 
	 * adds on the default path to make it absolute.
	 * If a URI scheme is present, the filename is assumed to be absolute
	 * @param filename String the filename to make absolute
	 * @param DefaultPath String the prefix to add
	 * @return the absolute filename
	 */
	public static String makeAbsolute(String filename, String DefaultPath)
	{
		if(filename == null)
			return null;
		if(filename.length() == 0)
			return filename;
		if (filename.matches("^\\w+:.*"))
			return filename;
		if ( new File(filename).isAbsolute() )
			return filename;
		if (! DefaultPath.endsWith(FILE_SEPARATOR))
		{
			DefaultPath = DefaultPath + FILE_SEPARATOR;
		}
		return DefaultPath+filename;
	}
	
	/** Clears ApplicationSetup of all properties */
	public static void clearAllProperties()
	{
		TermCodes.reset();
		appProperties.clear();
		UsedAppProperties.clear();
	}
}
