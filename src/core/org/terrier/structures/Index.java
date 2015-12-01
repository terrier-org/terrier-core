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
 * The Original Code is Index.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures;

import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
/** 
 * This class encapsulates all the Indexes at retrieval time. 
 * It is loaded by giving a path and prefix. This looks for an 
 * index properties file at path/prefix.properties. Essentially, the
 * properties file then specifies which index structures the index
 * supports. The index then loads these so they can be used in retrieval.
 * <p>
 * Arbitrary properties can be defined in the index properties files, and
 * in particular, properties are used to record index statistics and 
 * the contructor type and values of various index objects.
 * <p>
 * The Index will apply methods on specially marked interfaces. Currently,
 * the only interface supported is <a href="IndexConfigurable.html">IndexConfigurable</a>. 
 * Moreover, structures implementing java.io.Closeable will have
 * their close method called when the Index is closed.
 * <p>
 * 
 * @author Craig Macdonald &amp; Vassilis Plachouras
 */
public abstract class Index implements Closeable, Flushable {
	/**
	 * This collection statistics parses the associated index properties for
	 * each call. It doesnt support fields.
	 */
	protected class UpdatingCollectionStatistics extends CollectionStatistics {
		private static final long serialVersionUID = 1L;

		public UpdatingCollectionStatistics(Index index) {
			super(0, 0, 0, 0, new long[0]);
		}

		@Override
		public double getAverageDocumentLength() {
			final int numDocs = getNumberOfDocuments();
			if (numDocs == 0)
				return 0.0d;
			return (double)getNumberOfTokens() / (double) numDocs;
		}

		@Override
		public int getNumberOfDocuments() {
			return Integer.parseInt(properties.getProperty("num.Documents","0"));
		}

		@Override
		public long getNumberOfPointers() {
			return Long.parseLong(properties.getProperty("num.Pointers", "0"));
		}

		@Override
		public long getNumberOfTokens() {
			return Long.parseLong(properties.getProperty("num.Pointers", "0"));
		}

		@Override
		public int getNumberOfUniqueTerms() {
			return Integer.parseInt(properties.getProperty("num.Terms", "0"));
		}
		
	}

	protected final static int MINIMUM_INDEX_TERRIER_VERSION = 4;
	protected final static String PROPERTIES_SUFFIX = ".properties";

	protected static boolean RETRIEVAL_LOADING_PROFILE = Boolean
			.parseBoolean(ApplicationSetup.getProperty(
					"terrier.index.retrievalLoadingProfile.default", "true"));

	/** The logger used */
	protected static final Logger logger = LoggerFactory.getLogger(Index.class);

	protected static String lastLoadError = null;

	/** empty class array */
	protected static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

	/** returns true if all named files exist */
	public static boolean allExists(String... files) {
		for (int i = 0; i < files.length; i++) {
			if (!Files.exists(files[i])) {
				logger.debug("Files  " + files[i] + " doesn't exist");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Empty Index constructor - this should never be used!
	 * Use Index.createIndex() instead
	 */
	public Index() {
		
	}

	/**
	 * Constructs a new Index object. Don't call this method,
	 * call the createIndex(String, String) factory method to
	 * construct an Index object.
	 */
	public static IndexOnDisk createIndex() {
		return createIndex(ApplicationSetup.TERRIER_INDEX_PATH,
				ApplicationSetup.TERRIER_INDEX_PREFIX);
	}

	/**
	 * Factory method for load an index. This method should be used in order to
	 * load an existing index in the applications.
	 * 
	 * @param path
	 *            String the path in which the data structures will be created.
	 * @param prefix
	 *            String the prefix of the files to be created.
	 */
	public static IndexOnDisk createIndex(String path, String prefix) {
		IndexOnDisk i = new IndexOnDisk(path, prefix, false);
		if (!i.loadSuccess) {
			lastLoadError = i.loadError;
			return null;
		}
		return i;
	}

	/**
	 * Factory method create a new index. This method should be used in order to
	 * load a new index in the applications.
	 * 
	 * @param path
	 *            String the path in which the data structures will be created.
	 * @param prefix
	 *            String the prefix of the files to be created.
	 */
	public static IndexOnDisk createNewIndex(String path, String prefix) {
		IndexOnDisk i = new IndexOnDisk(path, prefix, true);
		if (!i.loadSuccess) {
			lastLoadError = i.loadError;
			return null;
		}
		return i;
	}

	/**
	 * Returns true if it is likely that an index exists at the specified
	 * location
	 * 
	 * @param path
	 * @param prefix
	 * @return true if a .properties or a .log files exists
	 */
	public static boolean existsIndex(String path, String prefix) {
		if (!(new File(path)).isAbsolute())
			path = ApplicationSetup.makeAbsolute(path,
					ApplicationSetup.TERRIER_VAR);
		return allExists(path + ApplicationSetup.FILE_SEPARATOR + prefix
				+ PROPERTIES_SUFFIX)
		/*
		 * || allExists(path + ApplicationSetup.FILE_SEPARATOR + prefix +
		 * LOG_SUFFIX)
		 */;
	}

	/**
	 * Get RETRIEVAL_LOADING_PROFILE
	 * 
	 * @return retrieval loading profile
	 */
	public static boolean getIndexLoadingProfileAsRetrieval() {
		return RETRIEVAL_LOADING_PROFILE;
	}

	/** Returns the last warning given by an index being loaded. */
	public static String getLastIndexLoadError() {
		return lastLoadError;
	}

	/** joins a series of strings together with a delimiter */
	protected static String join(String[] input, String joinString) {
		StringBuilder rtr = new StringBuilder();
		int i = input.length;
		int count = 0;
		for (String s : input) {
			rtr.append(s);
			if (i > 0 && count<(input.length-1))
				rtr.append(joinString);
			count++;
		}
		return rtr.toString();
	}

	/**
	 * main
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Index index = Index.createIndex();
		System.out.println("Index " + index.toString());
	}

	/**
	 * Set RETRIEVAL_LOADING_PROFILE
	 * 
	 * @param yes
	 */
	public static void setIndexLoadingProfileAsRetrieval(boolean yes) {
		RETRIEVAL_LOADING_PROFILE = yes;
	}

	/** properties of this index */
	protected final Properties properties = new Properties();

	/**
	 * Have the properties of this index changed, suggesting a flush() is
	 * necessary when closing
	 */
	protected boolean dirtyProperties = false;

	/** A constructor for child classes that doesnt open the file */
	protected Index(long a, long b, long c) {
	}

	/**
	 * tell the index about a new index structure it provides. Class and
	 * parameter types specified as Class objects instead of Strings.
	 */
	public void addIndexStructure(String structureName, Class<?> className,
			Class<?>[] paramTypes, String[] paramValues) {
		final int l = paramTypes.length;
		String[] SparamTypes = new String[l];
		for (int i = 0; i < l; i++)
			SparamTypes[i] = paramTypes[i].getName();
		addIndexStructure(structureName, className.getName(), SparamTypes,
				paramValues);
	}

	/**
	 * add an index structure to this index. Structure will be called
	 * structureName, and instantiated by a class called className.
	 * Instantiation parameters are "String,String", which are "path,prefix".
	 * 
	 * @param structureName
	 * @param className
	 */
	public void addIndexStructure(String structureName, String className) {
		properties.setProperty("index." + structureName + ".class", className);
		properties.setProperty("index." + structureName + ".parameter_types",
				"java.lang.String,java.lang.String");
		properties.setProperty("index." + structureName + ".parameter_values",
				"path,prefix");
		dirtyProperties = true;
	}

	/**
	 * add an index structure to this index. Structure will be called
	 * structureName, and instantiated by a class called className.
	 * Instantiation type parameters or values are non-default.
	 */
	public void addIndexStructure(String structureName, String className,
			String paramTypes, String paramValues) {
		properties.setProperty("index." + structureName + ".class", className);
		properties.setProperty("index." + structureName + ".parameter_types",
				paramTypes);
		properties.setProperty("index." + structureName + ".parameter_values",
				paramValues);
		dirtyProperties = true;
	}

	/**
	 * add an index structure to this index. Structure will be called
	 * structureName, and instantiated by a class called className.
	 * Instantiation type parameters or values are non-default.
	 */
	public void addIndexStructure(String structureName, String className,
			String[] paramTypes, String[] paramValues) {
		properties.setProperty("index." + structureName + ".class", className);
		properties.setProperty("index." + structureName + ".parameter_types",
				join(paramTypes, ","));
		properties.setProperty("index." + structureName + ".parameter_values",
				join(paramValues, ","));
		dirtyProperties = true;
	}

	/** tell the index about a new input stream index structure it provides. */
	public void addIndexStructureInputStream(String structureName,
			Class<?> className, Class<?>[] paramTypes, String[] paramValues) {
		addIndexStructure(structureName + "-inputstream", className,
				paramTypes, paramValues);
	}

	/** tell the index about a new input stream index structure it provides. */
	public void addIndexStructureInputStream(String structureName,
			String className) {
		addIndexStructure(structureName + "-inputstream", className);
	}

	/** tell the index about a new input stream index structure it provides. */
	public void addIndexStructureInputStream(String structureName,
			String className, String paramTypes, String paramValues) {
		addIndexStructure(structureName + "-inputstream", className,
				paramTypes, paramValues);
	}

	/** tell the index about a new input stream index structure it provides. */
	public void addIndexStructureInputStream(String structureName,
			String className, String[] paramTypes, String[] paramValues) {
		addIndexStructure(structureName + "-inputstream", className,
				paramTypes, paramValues);
	}

	/**
	 * Closes the data structures of the index.
	 */
	public abstract void close() throws IOException;

	/** Write any dirty data structures down to disk */
	public abstract void flush() throws IOException;

	/**
	 * Get the collection statistics
	 */
	public abstract CollectionStatistics getCollectionStatistics();

	/** Return the DirectIndex associated with this index */
	public abstract PostingIndex<?> getDirectIndex();

	/** Return the DocumentIndex associated with this index */
	public abstract DocumentIndex getDocumentIndex();

	/**
	 * get an arbitrary property in the index
	 * 
	 * @param key
	 *            Key of the property to get
	 * @param defaultValue
	 *            value of the property to use if property is not set
	 * @return Value of the property
	 */
	public String getIndexProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	/**
	 * Obtains the named index structure, using an already loaded one if
	 * possible.
	 * 
	 * @param structureName
	 *            name of the required structure
	 * @return desired object or null if not found
	 */
	public abstract Object getIndexStructure(String structureName);

	/**
	 * Return the input stream associated with the specified structure of this
	 * index
	 * 
	 * @param structureName
	 *            The name of the structure of which you want the inputstream.
	 *            Eg "lexicon"
	 * @return Required structure, or null if not found
	 */
	public abstract Object getIndexStructureInputStream(String structureName);

	/** get an arbitrary int property from the index */
	public int getIntIndexProperty(String key, int defaultValue) {
		String rtr = properties.getProperty(key, null);
		if (rtr == null)
			return defaultValue;
		return Integer.parseInt(rtr);
	}

	/** Returns the InvertedIndex to use for this index */
	public abstract PostingIndex<?> getInvertedIndex();

	/** Return the Lexicon associated with this index */
	public abstract Lexicon<String> getLexicon();

	/**
	 * Get the Meta Index structure
	 */
	public abstract MetaIndex getMetaIndex();

	/**
	 * Get the index properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Does this index have an index structure with the specified name?
	 * 
	 * @param structureName
	 *            name of the required structure
	 * @return true if the index has an appropriately named structure
	 */
	public boolean hasIndexStructure(String structureName) {
		return properties.containsKey("index." + structureName + ".class");
	}

	/**
	 * Does this index have an index structure input stream with the specified
	 * name?
	 * 
	 * @param structureName
	 *            name of the required structure
	 * @return true if the index has an appropriately named structure
	 */
	public boolean hasIndexStructureInputStream(String structureName) {
		return properties.containsKey("index." + structureName
				+ "-inputstream.class");
	}

	/**
	 * set an arbitrary property in the index
	 * 
	 * @param key
	 *            Key to of the property to set
	 * @param value
	 *            Value of the property to set
	 */
	public void setIndexProperty(String key, String value) {
		properties.setProperty(key, value);
		dirtyProperties = true;
	}

	/** Returns a String representation of this index */
	public abstract String toString();
	
	/** Returns the first docid in this index **/
	public int getStart() {
		return 0;
	}
	
	/** Returns the last docid in this index **/
	public int getEnd() {
		return this.getCollectionStatistics().getNumberOfDocuments()-1;
	}
}
