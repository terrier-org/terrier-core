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
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
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
 * This Class allows the updating of a Terrier 4.x index to the specification of 
 * Terrier 5.x index. The main difference between these versions from a structure
 * perspective was that addition of pluggable compression within the IndexOnDisk.
 * 
 * 
 * @author Richard McCreadie and Craig Macdonald
 * @since 5.0
 */
public class Terrier5 {
	
	Logger log = LoggerFactory.getLogger(Terrier5.class);
	
	protected Properties properties = null;
	protected String path = null;
	protected String prefix = null;
	
	/** Use this method when you wish to specify the properties object to rewrite */
	public Terrier5()
	{
		
	}
	
	/** Use this method to load an index data.properties file and rewrite it */
	public Terrier5(String path, String prefix) {
		this.path = path;
		this.prefix = prefix;
		properties = new Properties();
		boolean ok = loadProperties(path,prefix);
		if (!ok) System.exit(1);
	}
	
	
	public static void main(String[] args) {
		Terrier5 converter = new Terrier5(args[0],args[1]);
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
			properties.store(writer, "Index("+path+","+prefix+") converted from Terrier 4.x -> " + ApplicationSetup.TERRIER_VERSION);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * This method takes re-writes the index properties for a Terrier 4.x
	 * index to making compatible with Terrier 5.x
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
				if (swap.getOld().equals(currentPropertyValue)) {
					//System.err.println("INFO: Match for "+propertyName+"="+swap.getTerrier3()+", swapping for "+swap.getTerrier4());
					newProperty = swap.getNew();
					
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
	 * in Terrier 5.x 
	 */
	private void initaliseNewPropertyMapping() {
		if (propertyMap==null) {
			propertyMap = new HashMap<String,List<PropertySwap>>();
		}
		propertyMap.put("index.terrier.version", new ArrayList<PropertySwap>());
		propertyMap.put("index.lexicon-valuefactory.class",new ArrayList<PropertySwap>());
		
		propertyMap.get("index.lexicon-valuefactory.class").add(new PropertySwap("org.terrier.structures.FieldLexiconEntry$Factory", "org.terrier.structures.restructure.Tr4FieldLexiconEntry$Factory"));
		propertyMap.get("index.lexicon-valuefactory.class").add(new PropertySwap("org.terrier.structures.BasicLexiconEntry$Factory", "org.terrier.structures.restructure.Tr4BasicLexiconEntry$Factory"));
		//this latter class has no factory, so not even sure it can happen
		propertyMap.get("index.lexicon-valuefactory.class").add(new PropertySwap("org.terrier.structures.BasicTermStatsLexiconEntry$Factory", "org.terrier.structures.restructure.Tr4BasicTermStatsLexiconEntry$Factory"));
	}
	
	private class PropertySwap {
		
		String terrier3;
		String terrier4;
		public String getOld() {
			return terrier3;
		}
		public String getNew() {
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
