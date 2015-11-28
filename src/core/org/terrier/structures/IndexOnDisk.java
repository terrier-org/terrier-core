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
 * The Original Code is IndexOnDisk.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk> (original contributor)
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.structures;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.terrier.utility.restructure.Terrier4;
/** 
 * The replacement for what was Index in earlier Terrier versions.
 * Represents the most common type of index, i.e. one which is stored
 * on disk.
 * @author Stuart Mackie, Craig Macdonald, Richard McCreadie
 * @since 4.0
 */
public class IndexOnDisk extends Index {

	/** path component of this index's location */
	protected String path;
	/** prefix component of this index's location */
	protected String prefix;
	/** Cache of all opened index structures, but not input streams */
	protected final HashMap<String, Object> structureCache = new HashMap<String, Object>(
			10);

	/** Set to true if loading an index succeeds */
	protected boolean loadSuccess = true;
	protected String loadError = null;

	/**
	 * A default constructor that creates an instance of the index.
	 */
	protected IndexOnDisk() {
		this(ApplicationSetup.TERRIER_INDEX_PATH,
				ApplicationSetup.TERRIER_INDEX_PREFIX, false);
	}

	/**
	 * Constructs a new Index object. Don't call this method, call the
	 * createIndex(String) factory method to construct an Index object.
	 * 
	 * @param _path
	 *            String the path in which the data structures will be created.
	 * @param _prefix
	 *            String the prefix of the files to be created.
	 * @param isNew
	 *            where a new Index should be created if there is no index at
	 *            the specified location
	 */
	protected IndexOnDisk(String _path, String _prefix, boolean isNew) {
		super(0l, 0l, 0l);
		if (!(new File(_path)).isAbsolute())
			_path = ApplicationSetup.makeAbsolute(_path,
					ApplicationSetup.TERRIER_VAR);

		this.path = _path;
		this.prefix = _prefix;

		if (isNew && (! Files.exists(this.path) ))
		{
			String message = "Cannot create new index: path " + this.path + " does not exist, or cannot be written to";
			logger.error(message);
			throw new IllegalArgumentException(message);
		}
			
		
		boolean indexExists = loadProperties();

		if (isNew && !indexExists) {
			logger.debug("Creating new index : " + this.toString());
			setIndexProperty("index.terrier.version",
					ApplicationSetup.TERRIER_VERSION);
			setIndexProperty("index.created", "" + System.currentTimeMillis());
			setIndexProperty("num.Documents", "0");
			setIndexProperty("num.Terms", "0");
			setIndexProperty("num.Tokens", "0");
			setIndexProperty("num.Pointers", "0");
			loadUpdatingStatistics();
			dirtyProperties = true;
			loadSuccess = true;
		} else if (indexExists) {
			logger.debug("Loading existing index : " + this.toString());
			// note the order - some structures will require collection
			// statistics, so load this first.
			loadStatistics();
			loadIndices();
		}
	}

	public IndexOnDisk(long l, long m, long n) {
		super(0l, 0l, 0l);
	}

	@Override
	public void close() throws IOException {
		// invoke the close methods on all currently open index structures
		for (Object o : structureCache.values()) {
			try {
				IndexUtil.close(o);
			} catch (IOException ioe) {/* ignore */
			}
		}
		structureCache.clear();
		flushProperties();
	}

	@Override
	public void flush() throws IOException {
		flushProperties();
	}

	/** Write any dirty properties down to disk */
	protected void flushProperties() throws IOException {
		if (dirtyProperties) {
			final String propertiesFilename = path
					+ ApplicationSetup.FILE_SEPARATOR + prefix
					+ PROPERTIES_SUFFIX;
			if ((Files.exists(propertiesFilename) && !Files
					.canWrite(propertiesFilename))
					|| (!Files.exists(propertiesFilename) && !Files
							.canWrite(path))) {
				logger.warn("Could not write to index properties at "
						+ propertiesFilename
						+ " because you do not have write permission on the index - some changes may be lost");
				return;
			}

			final OutputStream outputStream = Files
					.writeFileStream(propertiesFilename);
			properties.store(outputStream, this.toString());
			outputStream.close();
			dirtyProperties = false;

		}
	}

	@Override
	public CollectionStatistics getCollectionStatistics() {
		return (CollectionStatistics) getIndexStructure("collectionstatistics");
	}

	@SuppressWarnings("unchecked")
	@Override
	public PostingIndex<Pointer> getDirectIndex() {
		return (PostingIndex<Pointer>) getIndexStructure("direct");
	}

	@Override
	public DocumentIndex getDocumentIndex() {
		return (DocumentIndex) getIndexStructure("document");
	}

	/**
	 * Obtains the named index structure, using an already loaded one if
	 * possible.
	 * 
	 * @param structureName
	 *            name of the required structure
	 * @return desired object or null if not found
	 */
	public Object getIndexStructure(String structureName) {
		Object rtr = structureCache.get(structureName);
		if (rtr != null)
			return rtr;
		rtr = loadIndexStructure(structureName);
		if (rtr != null)
			structureCache.put(structureName, rtr);
		return rtr;
	}

	@Override
	/** Return the input stream associated with the specified structure of this index
	 * @param structureName  The name of the structure of which you want the inputstream. Eg "lexicon"
	 * @return Required structure, or null if not found */
	public Object getIndexStructureInputStream(String structureName) {
		// no caching on inputstreams
		return loadIndexStructure(structureName + "-inputstream");
	}

	@Override
	public PostingIndex<?> getInvertedIndex() {
		return (PostingIndex<?>) getIndexStructure("inverted");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Lexicon<String> getLexicon() {
		return (Lexicon<String>) getIndexStructure("lexicon");
	}

	@Override
	public MetaIndex getMetaIndex() {
		return (MetaIndex) getIndexStructure("meta");
	}

	/** Returns the path of this index */
	public String getPath() {
		return path;
	}

	/** Returns the prefix of this index */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Load a new instance of the named index structure.
	 * 
	 * @param structureName
	 *            name of the required structure
	 * @return desired object or null if not found
	 */
	protected Object loadIndexStructure(String structureName) {
		logger.debug("Attempting to load structure " + structureName);
		try {
			// figure out the correct class
			String structureClassName = properties.getProperty("index."
					+ structureName + ".class");
			if (structureClassName == null) {
				logger.error("This index (" + this.toString()
						+ ") doesnt have an index structure called "
						+ structureName + ": property index." + structureName
						+ ".class not found");
				logger.error("Valid structures are: "
						+ Arrays.deepToString(IndexUtil.getStructures(this)));
				return null;// TODO exceptions?
			}
			if (structureClassName.startsWith("uk.ac.gla.terrier"))
				structureClassName = structureClassName.replaceAll(
						"uk.ac.gla.terrier", "org.terrier");
			// obtain the class definition for the index structure
			Class<?> indexStructureClass = null;
			try {
				indexStructureClass = Class.forName(structureClassName, false,
						this.getClass().getClassLoader());
			} catch (ClassNotFoundException cnfe) {
				logger.error("ClassNotFoundException: This index ("
						+ this.toString()
						+ ") references an unknown index structure class: "
						+ structureName + " looking for " + structureClassName);
				cnfe.printStackTrace();
				return null;// TODO exceptions?
			}

			// build up the constructor parameter type array
			final ArrayList<Class<?>> paramTypes = new ArrayList<Class<?>>(5);

			final String typeList = properties.getProperty(
					"index." + structureName + ".parameter_types",
					"java.lang.String,java.lang.String").trim();
			Object rtr = null;
			// for objects with constructor arguments
			if (typeList.length() > 0) {
				final String[] types = typeList.split("\\s*,\\s*");
				for (String t : types) {
					if (t.startsWith("uk.ac.gla.terrier"))
						t = t.replaceAll("uk.ac.gla.terrier", "org.terrier");
					paramTypes.add(Class.forName(t));
				}
				Class<?>[] param_types = paramTypes.toArray(EMPTY_CLASS_ARRAY);

				// build up the constructor parameter value array
				String[] params = properties.getProperty(
						"index." + structureName + ".parameter_values",
						"path,prefix").split("\\s*,\\s*");
				Object[] objs = new Object[paramTypes.size()];
				int i = 0;
				for (String p : params) {
					// System.err.println("looking for parameter value called "+
					// p + " with type '" + param_types[i]+ "'");
					if (p.equals("path"))
						objs[i] = path;
					else if (p.equals("prefix"))
						objs[i] = prefix;
					else if (p.equals("index"))
						objs[i] = this;
					else if (p.equals("structureName")) {
						final String tmp = structureName;
						objs[i] = tmp.replaceAll("-inputstream$", "");
					} else if (param_types[i].equals(java.lang.Class.class)) {
						// System.err.println("loading class called "+p);
						if (p.startsWith("uk.ac.gla.terrier"))
							p = p.replaceAll("uk.ac.gla.terrier", "org.terrier");
						objs[i] = Class.forName(p);
					} else if (p.endsWith("-inputstream"))// no caching for
															// input streams
						objs[i] = loadIndexStructure(p);
					else if (p.matches("^\\$\\{.+\\}$")) {
						String propertyName = p.substring(2, p.length() - 1);
						objs[i] = properties.getProperty(propertyName,
								ApplicationSetup
										.getProperty(propertyName, null));
						if (objs[i] == null)
							throw new IllegalArgumentException("Property "
									+ propertyName + " not found");
					} 
					else if (p.matches("^\".+\"$")) {
						String literal = p.substring(1, p.length() - 1);
						if (param_types[i].equals(String.class))
							objs[i] = literal;
						else if (param_types[i].equals(Integer.class))
							objs[i] = Integer.valueOf(Integer.parseInt(literal));
						else
							throw new IllegalArgumentException("Type "
									+ param_types[i] + " is not supported for literal parameter values");
					} else
						objs[i] = getIndexStructure(p);
					i++;
				}

				// get the index structure using the appropriate constructor
				// with correct parameters
				rtr = indexStructureClass.getConstructor(param_types)
						.newInstance(objs);
			} else { // no constructor arguments
				rtr = indexStructureClass.newInstance();
			}

			// Special case hacks
			// 1. set the Index properties if desired
			if (rtr instanceof IndexConfigurable) {
				((IndexConfigurable) rtr).setIndex(this);
			}
			// we're done
			return rtr;

		} catch (Throwable t) {
			logger.error("Couldn't load an index structure called "
					+ structureName, t);
			return null;
		}
	}

	/**
	 * load all index structures. Is disabled if index property
	 * <tt>index.preloadIndices.disabled</tt> is set to true. It is false by
	 * default, which means that all non-inputstream indices are loaded on
	 * initialisation of the index. When the property is true, indices are
	 * loaded as required.
	 */
	protected void loadIndices() {
		final boolean methodDisabled = Boolean.parseBoolean(properties
				.getProperty("index.preloadIndices.disabled", "false"));
		if (methodDisabled || !RETRIEVAL_LOADING_PROFILE)
			return;

		boolean OK = true;
		// look for all index structures
		for (Object oKey : properties.keySet()) {
			final String sKey = (String) oKey;
			if (sKey.matches("^index\\..+\\.class$")
					&& !(sKey.matches("^index\\..+-inputstream.class$"))) // don't
																			// pre-load
																			// input
																			// streams
			{
				final String structureName = sKey.split("\\.")[1];
				Object o = getIndexStructure(structureName);
				if (o == null) {
					loadError = "Could not load an index structure called "
							+ structureName;
					OK = false;
				}
			}
		}
		if (!OK)
			this.loadSuccess = false;
	}

	/**
	 * loads in the properties file, falling back to the Terrier 1.xx log file
	 * if no properties exist.
	 */
	protected boolean loadProperties() {
		try {
			String propertiesFilename = path + ApplicationSetup.FILE_SEPARATOR
					+ prefix + ".properties";
			if (!allExists(propertiesFilename)) {
				loadSuccess = false;
				loadError = "Index not found: " + propertiesFilename
						+ " not found.";
				return false;
			} else {
				InputStream propertyStream = Files
						.openFileStream(propertiesFilename);
				properties.load(propertyStream);
				propertyStream.close();
			}

		} catch (IOException ioe) {
			loadSuccess = false;
			logger.error("Problem loading index properties", ioe);
			loadError = "Problem loading index properties: " + ioe;
			return false;
		}
		if (properties.getProperty("index.terrier.version", null) == null) {
			loadSuccess = false;
			logger.error("index.terrier.version not set in index, invalid index?");
			loadError = "index.terrier.version not set in index";
			return false;
		}
		final String versionString = properties.getProperty(
				"index.terrier.version", null);
		final String[] versionStringParts = versionString.split("\\.", 2);
		final int MAJOR_VERSION = Integer.parseInt(versionStringParts[0]);
		if (MAJOR_VERSION < MINIMUM_INDEX_TERRIER_VERSION) {
			if (MAJOR_VERSION == 3) {
				Terrier4 upgrade = new Terrier4();
				upgrade.updateIndexProperties(properties);
				logger.warn(this.toString() + " is a Terrier " + versionString 
					+ " index - temporarily upgrading. Use " + Terrier4.class.getName() + " to make changes permanent");
			} else {
				loadSuccess = false;
				logger.error(loadError = "This index is too old. Need at least version "
						+ MINIMUM_INDEX_TERRIER_VERSION + " index");
				return false;
			}
		}
		return true;
	}

	/**
	 * for an immutable index, use a normal collection statistics, never changes
	 */
	protected void loadStatistics() {
		// calculate fields
		int fieldCount = 0;
		if (this.hasIndexStructure("inverted")) {
			fieldCount = Integer.parseInt(properties.getProperty(
					"index.inverted.fields.count", "0"));
		} else if (this.hasIndexStructure("direct")) {
			fieldCount = Integer.parseInt(properties.getProperty(
					"index.direct.fields.count", "0"));
		}
		final long[] tokensF = new long[fieldCount];
		for (int fi = 0; fi < fieldCount; fi++) {
			tokensF[fi] = Long.parseLong(properties.getProperty("num.field."
					+ fi + ".Tokens", "0"));
		}
		// create collection statistics
		structureCache.put(
				"collectionstatistics",
				new CollectionStatistics(Integer.parseInt(properties
						.getProperty("num.Documents", "0")), Integer
						.parseInt(properties.getProperty("num.Terms", "0")),
						Long.parseLong(properties
								.getProperty("num.Tokens", "0")), Long
								.parseLong(properties.getProperty(
										"num.Pointers", "0")), tokensF));
	}

	/**
	 * for an index that is not yet built, use an UpdatingCollectionStatistics,
	 * which is slower but can support updates of the index statistics
	 */
	protected void loadUpdatingStatistics() {
		structureCache.put("collectionstatistics",
				new UpdatingCollectionStatistics(this));
	}

	@Override
	public String toString() {
		return "Index(" + path + "," + prefix + ")";
	}

}
