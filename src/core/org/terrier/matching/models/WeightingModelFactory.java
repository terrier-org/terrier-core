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
 * The Original Code is WeightingModelFactory.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Rodrygo Santos <rodrygo{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   
 */
package org.terrier.matching.models;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.Index;

/**
 * A factory method for handling the initialisation of weighting models.
 * 
 * @author Rodrygo Santos, Craig Macdonald
 */
public class WeightingModelFactory {
	
	/** The default namespace for weighting models. */
	public static final String NAMESPACE = "org.terrier.matching.models.";
	/** A cache for instantiated weighting models. */
	private static Map<Index, Map<String, WeightingModel>> cache = new HashMap<Index, Map<String, WeightingModel>>();

	/** Clear the cache underlying this WeightingModelFactory */
	public static void clearCache() {
		cache.clear();
	}
	
	/**
	 * Returns the requested weighting model.
	 * @param name The name of the weighting model to instantiate.
	 */	
	public static WeightingModel newInstance(String name) {
		return newInstance(name, null);
	}
	
	/**
	 * Returns the requested weighting model for the specified index.
	 * @param name The name of the weighting model to instantiate.
	 * @param index The index where the weighting model should be applied.
	 */
	public static WeightingModel newInstance(String name, Index index) {
		final Logger logger = LoggerFactory.getLogger(WeightingModelFactory.class);
		WeightingModel model = null;
		
		name = name.replaceFirst("^([^\\.]+(\\(|$))", NAMESPACE + "$1");
		name = name.replaceAll("uk.ac.gla.terrier", "org.terrier");
		
		// check for an already instantiated model
		if (!cache.containsKey(index)) {
			cache.put(index, new HashMap<String, WeightingModel>());
		}
		model = cache.get(index).get(name);
		if (model == null) {
			try {
				if (name.indexOf("(") > 0) {
					String params = name.substring(name.indexOf("(")+1, name.indexOf(")"));
					String[] parameters = params.split("\\s*,\\s*");
					
					model = Class.forName(name.substring(0,name.indexOf("(")))
							.asSubclass(WeightingModel.class)
							.getConstructor(new Class[]{String[].class})
							.newInstance(new Object[]{parameters});
				}
				else{						
					model = Class.forName(name).asSubclass(WeightingModel.class).newInstance();
				}
				
			} catch(InvocationTargetException e) {
				logger.error("Recursive problem with weighting model named: "+name, e);
			} catch(Exception e) {
				logger.error("Problem with weighting model named: " + name, e);
			}
			cache.get(index).put(name, model);
		}
		
		return model;
	}
	
}
