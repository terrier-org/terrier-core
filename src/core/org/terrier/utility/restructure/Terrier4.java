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
 * The Original Code is Terrier4.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   
 */

package org.terrier.utility.restructure;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.Index;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

/**
 * This Class allows the updating of a Terrier 3.x index to the specification of 
 * Terrier 4.x index. The main difference between these versions from a structure
 * perspective was that addition of pluggable compression within the IndexOnDisk.
 * 
 * 
 * @author Richard McCreadie
 * @since 4.0
 */
public class Terrier4 {
	
	Logger log = LoggerFactory.getLogger(Terrier4.class);
	
	protected Properties properties = null;
	protected String path = null;
	protected String prefix = null;
	
	/** Use this method when you wish to specify the properties object to rewrite */
	public Terrier4()
	{
		
	}
	
	/** Use this method to load an index data.properties file and rewrite it */
	public Terrier4(String path, String prefix) {
		this.path = path;
		this.prefix = prefix;
		properties = new Properties();
		boolean ok = loadProperties(path,prefix);
		if (!ok) System.exit(1);
	}
	
	
	public static void main(String[] args) {
		Terrier4 converter = new Terrier4(args[0],args[1]);
		converter.updateIndexProperties();
		
		/*Terrier4 converter = new Terrier4();
		Properties p = new Properties();
		p.put("index.terrier.version", "3.0a");
		p.put("index.direct-inputstream.parameter_values", "index,structureName,uk.ac.gla.terrier.structures.postings.BlockIterablePosting");
		p.put("index.inverted.class", "uk.ac.gla.terrier.structures.InvertedIndex");
		p.put("index.direct.parameter_types", "org.terrier.structures.Index,java.lang.String");
		p.put("index.direct.parameter_values", "index,structureName");
		converter.updateIndexProperties(p);*/
	}
	
	public void updateIndexProperties() {
		if (properties == null)
			throw new IllegalStateException();
		
		updateIndexProperties(properties);
		try {
			FileWriter writer = new FileWriter(path
					+ApplicationSetup.FILE_SEPARATOR
					+prefix+".properties");
			properties.store(writer, "Index("+path+","+prefix+") converted from Terrier 3.x -> " + ApplicationSetup.TERRIER_VERSION);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * This method takes re-writes the index properties for a Terrier 3.x
	 * index to making compatible with Terrier 4.x
	 * @param indexProperties properties object to alter
	 */
	public void updateIndexProperties(Properties indexProperties) {
		
		boolean blocks = false;
		boolean fields = false;
		
		for (Object k : indexProperties.keySet()) {
			String key = (String)k;
			String oldvalue = indexProperties.getProperty(key);
			if (oldvalue.contains("Block")) blocks=true;
			if (oldvalue.contains("Field")) fields=true;
		}
		
		for (Object k : indexProperties.keySet()) {
			String key = (String)k;
			String oldvalue = indexProperties.getProperty(key);
			
			oldvalue = oldvalue.replace("uk.ac.gla.terrier", "org.terrier");
			
			String newvalue = getNewPropertyValue(key, oldvalue, blocks, fields);
			if (newvalue != null && ! oldvalue.equals(newvalue))
			{
				log.debug("Rewriting index property "+key+" from '" + indexProperties.getProperty(key) + "' to '" + newvalue+ "'");
				indexProperties.put(key, newvalue);
			}
						
		}
		indexProperties.setProperty("index.terrier.version", ApplicationSetup.TERRIER_VERSION);
		
	}
	
	
	
	private String getNewPropertyValue(String propertyName, String currentPropertyValue, boolean blocks, boolean fields) {
		
		if (propertyMap==null) {
			initaliseNewPropertyMapping();
		}
		
		String newProperty =currentPropertyValue;
		if (propertyMap.containsKey(propertyName)) {
			for (PropertySwap swap : propertyMap.get(propertyName)) {
				if (swap.getTerrier3().equals(currentPropertyValue)) {
					//System.err.println("INFO: Match for "+propertyName+"="+swap.getTerrier3()+", swapping for "+swap.getTerrier4());
					newProperty = swap.getTerrier4();
					
					if (blocks && !fields)  newProperty=newProperty.replace("BasicIterablePosting", "BlockIterablePosting");
					if (!blocks && fields)  newProperty=newProperty.replace("BasicIterablePosting", "FieldIterablePosting");
					if (blocks && fields)  newProperty=newProperty.replace("BasicIterablePosting", "BlockFieldIterablePosting");
				}
			}
		} 
		return newProperty;
		
	}
	
	
	Map<String,List<PropertySwap>> propertyMap = null;
	
	/**
	 * Creates a mapping between the properties in and their new values
	 * in Terrier 4.x 
	 */
	private void initaliseNewPropertyMapping() {
		if (propertyMap==null) {
			propertyMap = new HashMap<String,List<PropertySwap>>();
		}
		propertyMap.put("index.terrier.version", new ArrayList<PropertySwap>());
		propertyMap.put("index.direct.class", new ArrayList<PropertySwap>());
		propertyMap.put("index.direct-inputstream.class", new ArrayList<PropertySwap>());
		propertyMap.put("index.direct-inputstream.parameter_types", new ArrayList<PropertySwap>());
		propertyMap.put("index.direct-inputstream.parameter_values", new ArrayList<PropertySwap>());
		propertyMap.put("index.direct.parameter_values", new ArrayList<PropertySwap>());
		propertyMap.put("index.inverted.class", new ArrayList<PropertySwap>());
		propertyMap.put("index.inverted-inputstream.class", new ArrayList<PropertySwap>());
		propertyMap.put("index.inverted-inputstream.parameter_values", new ArrayList<PropertySwap>());
		propertyMap.put("index.inverted.parameter_types", new ArrayList<PropertySwap>());
		propertyMap.put("index.inverted.parameter_values", new ArrayList<PropertySwap>());
		propertyMap.put("index.lexicon-valuefactory.class", new ArrayList<PropertySwap>());
		propertyMap.put("index.direct.parameter_types", new ArrayList<PropertySwap>());
		propertyMap.put("index.inverted.parameter_types", new ArrayList<PropertySwap>());
		propertyMap.put("index.meta.parameter_types", new ArrayList<PropertySwap>());
		propertyMap.put("index.document.parameter_types", new ArrayList<PropertySwap>());
		propertyMap.put("index.meta-inputstream.parameter_types", new ArrayList<PropertySwap>());
		propertyMap.put("index.document-inputstream.parameter_types", new ArrayList<PropertySwap>());
		propertyMap.put("index.inverted-inputstream.parameter_types", new ArrayList<PropertySwap>());
		propertyMap.put("index.lexicon.parameter_types", new ArrayList<PropertySwap>());
		propertyMap.put("index.lexicon-inputstream.parameter_types", new ArrayList<PropertySwap>());
		propertyMap.put("index.lexicon-entry-inputstream.parameter_types", new ArrayList<PropertySwap>());

		propertyMap.get("index.direct.class").add(new PropertySwap("org.terrier.structures.DirectIndex", "org.terrier.structures.bit.BitPostingIndex"));
		propertyMap.get("index.direct-inputstream.class").add(new PropertySwap("org.terrier.structures.DirectIndexInputStream", "org.terrier.structures.bit.BitPostingIndexInputStream"));
		propertyMap.get("index.direct-inputstream.parameter_types").add(new PropertySwap("org.terrier.structures.Index,java.lang.String,java.lang.Class", "org.terrier.structures.IndexOnDisk,java.lang.String,java.util.Iterator,java.lang.Class"));
		propertyMap.get("index.direct-inputstream.parameter_types").add(new PropertySwap("org.terrier.structures.Index,java.lang.String", "org.terrier.structures.IndexOnDisk,java.lang.String,java.util.Iterator,java.lang.Class"));
		propertyMap.get("index.direct-inputstream.parameter_values").add(new PropertySwap("index,structureName,org.terrier.structures.postings.BasicIterablePosting", "index,structureName,document-inputstream,org.terrier.structures.postings.bit.BasicIterablePosting"));
		propertyMap.get("index.direct-inputstream.parameter_values").add(new PropertySwap("index,structureName", "index,structureName,document-inputstream,org.terrier.structures.postings.bit.BasicIterablePosting"));
		propertyMap.get("index.direct.parameter_values").add(new PropertySwap("index,structureName,org.terrier.structures.postings.BasicIterablePosting", "index,structureName,org.terrier.structures.postings.bit.BasicIterablePosting"));
		propertyMap.get("index.direct.parameter_values").add(new PropertySwap("index,structureName", "index,structureName,org.terrier.structures.postings.bit.BasicIterablePosting"));
		
		propertyMap.get("index.inverted.class").add(new PropertySwap("org.terrier.structures.InvertedIndex", "org.terrier.structures.bit.BitPostingIndex"));
		propertyMap.get("index.inverted-inputstream.class").add(new PropertySwap("org.terrier.structures.InvertedIndexInputStream", "org.terrier.structures.bit.BitPostingIndexInputStream"));
		propertyMap.get("index.inverted-inputstream.parameter_values").add(new PropertySwap("index,structureName,lexicon-entry-inputstream,org.terrier.structures.postings.BasicIterablePosting", "index,structureName,lexicon-entry-inputstream,org.terrier.structures.postings.bit.BasicIterablePosting"));
		propertyMap.get("index.inverted.parameter_types").add(new PropertySwap("org.terrier.structures.Index,java.lang.String,org.terrier.structures.DocumentIndex,java.lang.Class", "org.terrier.structures.IndexOnDisk,java.lang.String,java.lang.Class"));
		propertyMap.get("index.inverted.parameter_values").add(new PropertySwap("index,structureName,document,org.terrier.structures.postings.BasicIterablePosting", "index,structureName,org.terrier.structures.postings.bit.BasicIterablePosting"));
		
		propertyMap.get("index.direct-inputstream.parameter_values").add(new PropertySwap("index,structureName,org.terrier.structures.postings.FieldIterablePosting", "index,structureName,document-inputstream,org.terrier.structures.postings.bit.FieldIterablePosting"));
		propertyMap.get("index.direct.parameter_values").add(new PropertySwap("index,structureName,org.terrier.structures.postings.FieldIterablePosting", "index,structureName,org.terrier.structures.postings.bit.FieldIterablePosting"));
		propertyMap.get("index.inverted-inputstream.parameter_values").add(new PropertySwap("index,structureName,lexicon-entry-inputstream,org.terrier.structures.postings.FieldIterablePosting", "index,structureName,lexicon-entry-inputstream,org.terrier.structures.postings.bit.FieldIterablePosting"));
		propertyMap.get("index.inverted.parameter_values").add(new PropertySwap("index,structureName,document,org.terrier.structures.postings.FieldIterablePosting", "index,structureName,org.terrier.structures.postings.bit.FieldIterablePosting"));
		
		propertyMap.get("index.direct.class").add(new PropertySwap("org.terrier.structures.BlockDirectIndex", "org.terrier.structures.bit.BitPostingIndex"));
		propertyMap.get("index.direct-inputstream.class").add(new PropertySwap("org.terrier.structures.BlockDirectIndexInputStream", "org.terrier.structures.bit.BitPostingIndexInputStream"));
		propertyMap.get("index.direct-inputstream.parameter_values").add(new PropertySwap("index,structureName,org.terrier.structures.postings.BlockIterablePosting", "index,structureName,document-inputstream,org.terrier.structures.postings.bit.BlockIterablePosting"));
		propertyMap.get("index.direct.parameter_values").add(new PropertySwap("index,structureName,org.terrier.structures.postings.BlockIterablePosting", "index,structureName,org.terrier.structures.postings.bit.BlockIterablePosting"));
		
		propertyMap.get("index.inverted.class").add(new PropertySwap("org.terrier.structures.BlockInvertedIndex", "org.terrier.structures.bit.BitPostingIndex"));
		propertyMap.get("index.inverted-inputstream.class").add(new PropertySwap("org.terrier.structures.BlockInvertedIndexInputStream", "org.terrier.structures.bit.BitPostingIndexInputStream"));
		propertyMap.get("index.inverted-inputstream.parameter_values").add(new PropertySwap("index,structureName,lexicon-entry-inputstream,org.terrier.structures.postings.BlockIterablePosting", "index,structureName,lexicon-entry-inputstream,org.terrier.structures.postings.bit.BlockIterablePosting"));
		propertyMap.get("index.inverted.parameter_values").add(new PropertySwap("index,structureName,document,org.terrier.structures.postings.BlockIterablePosting", "index,structureName,org.terrier.structures.postings.bit.BlockIterablePosting"));
		
		propertyMap.get("index.direct-inputstream.parameter_values").add(new PropertySwap("index,structureName,org.terrier.structures.postings.BlockFieldIterablePosting", "index,structureName,document-inputstream,org.terrier.structures.postings.bit.BlockFieldIterablePosting"));
		propertyMap.get("index.direct.parameter_values").add(new PropertySwap("index,structureName,org.terrier.structures.postings.BlockFieldIterablePosting", "index,structureName,org.terrier.structures.postings.bit.BlockFieldIterablePosting"));
		propertyMap.get("index.inverted-inputstream.parameter_values").add(new PropertySwap("index,structureName,lexicon-entry-inputstream,org.terrier.structures.postings.BlockFieldIterablePosting", "index,structureName,lexicon-entry-inputstream,org.terrier.structures.postings.bit.BlockFieldIterablePosting"));
		propertyMap.get("index.inverted.parameter_values").add(new PropertySwap("index,structureName,document,org.terrier.structures.postings.BlockFieldIterablePosting", "index,structureName,org.terrier.structures.postings.bit.BlockFieldIterablePosting"));

		propertyMap.get("index.direct.parameter_types").add(new PropertySwap("org.terrier.structures.Index,java.lang.String", "org.terrier.structures.IndexOnDisk,java.lang.String,java.lang.Class"));
		propertyMap.get("index.direct.parameter_values").add(new PropertySwap("index,structureName", "index,structureName,org.terrier.structures.postings.bit.BasicIterablePosting"));
		propertyMap.get("index.inverted.parameter_types").add(new PropertySwap("org.terrier.structures.Index,java.lang.String", "org.terrier.structures.IndexOnDisk,java.lang.String,java.lang.Class"));
		propertyMap.get("index.inverted.parameter_values").add(new PropertySwap("index,structureName", "index,structureName,org.terrier.structures.postings.bit.BasicIterablePosting"));
		
		propertyMap.get("index.meta.parameter_types").add(new PropertySwap("org.terrier.structures.Index,java.lang.String", "org.terrier.structures.IndexOnDisk,java.lang.String"));
		propertyMap.get("index.document.parameter_types").add(new PropertySwap("org.terrier.structures.Index,java.lang.String", "org.terrier.structures.IndexOnDisk,java.lang.String"));
		propertyMap.get("index.meta-inputstream.parameter_types").add(new PropertySwap("org.terrier.structures.Index,java.lang.String", "org.terrier.structures.IndexOnDisk,java.lang.String"));
		propertyMap.get("index.document-inputstream.parameter_types").add(new PropertySwap("org.terrier.structures.Index,java.lang.String", "org.terrier.structures.IndexOnDisk,java.lang.String"));
		propertyMap.get("index.inverted-inputstream.parameter_types").add(new PropertySwap("org.terrier.structures.Index,java.lang.String,java.util.Iterator,java.lang.Class", "org.terrier.structures.IndexOnDisk,java.lang.String,java.util.Iterator,java.lang.Class"));
		propertyMap.get("index.direct.parameter_types").add(new PropertySwap("org.terrier.structures.Index,java.lang.String,java.lang.Class", "org.terrier.structures.IndexOnDisk,java.lang.String,java.lang.Class"));

		propertyMap.get("index.lexicon.parameter_types").add(new PropertySwap("java.lang.String,org.terrier.structures.Index", "java.lang.String,org.terrier.structures.IndexOnDisk"));
		propertyMap.get("index.lexicon-inputstream.parameter_types").add(new PropertySwap("java.lang.String,org.terrier.structures.Index", "java.lang.String,org.terrier.structures.IndexOnDisk"));
		propertyMap.get("index.lexicon-entry-inputstream.parameter_types").add(new PropertySwap("java.lang.String,org.terrier.structures.Index", "java.lang.String,org.terrier.structures.IndexOnDisk"));
	}
	
	private class PropertySwap {
		
		String terrier3;
		String terrier4;
		public String getTerrier3() {
			return terrier3;
		}
		public String getTerrier4() {
			return terrier4;
		}
		public PropertySwap(String terrier3, String terrier4) {
			super();
			this.terrier3 = terrier3;
			this.terrier4 = terrier4;
		}
		
		
	}
	
	
	/**
	 * loads in the properties file, falling back to the Terrier 1.xx log file
	 * if no properties exist.
	 */
	protected boolean loadProperties(String path, String prefix) {
		try {
			String propertiesFilename = path + ApplicationSetup.FILE_SEPARATOR
					+ prefix + ".properties";
			if (!Index.allExists(propertiesFilename)) {
				System.err.println("INFO: Index not found: " + propertiesFilename
						+ " not found.");
				return false;
			} else {
				InputStream propertyStream = Files
						.openFileStream(propertiesFilename);
				properties.load(propertyStream);
				propertyStream.close();
			}

		} catch (IOException ioe) {
			System.err.println("Problem loading index properties");
			ioe.printStackTrace();
			return false;
		}
		if (properties.getProperty("index.terrier.version", null) == null) {
			System.err.println("index.terrier.version not set in index, invalid index?");
			return false;
		}
		final String versionString = properties.getProperty(
				"index.terrier.version", null);
		final String[] versionStringParts = versionString.split("\\.");
		int MAJOR_VERSION = Integer.parseInt(versionStringParts[0]);
		if (MAJOR_VERSION < 3) {
			System.err.println("This index is too old. Need a version "
					+ 3 + " index to upgrade to version 4");
			return false;
		} else if (MAJOR_VERSION > 3) {
			System.err.println("This index is too new. Can only upgrade a version "
					+ 3 + " index to version 4");
			return false;
		}
		return true;
	}
	

}
