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
 * The Original Code is QueryExpansionModelFactory.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Rodrgyo Santos <rodrygo{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.matching.models.queryexpansion;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory method for handling the initialisation of expansion models.
 * 
 * @author Rodrygo Santos
 */
public class QueryExpansionModelFactory {
	
	/** The default namespace for expansion models. */
	public static final String NAMESPACE = "org.terrier.matching.models.queryexpansion.";
	/** A cache for instantiated weighting models. */
	private static Map<String, QueryExpansionModel> cache = new HashMap<String, QueryExpansionModel>();
	
	/**
	 * Returns the requested weighting model.
	 * @param name The name of the weighting model to instantiate.
	 */	
	public static QueryExpansionModel newInstance(String name) {
		
		Logger logger = LoggerFactory.getLogger(QueryExpansionModelFactory.class);
		QueryExpansionModel model = null;
		
		if (name.indexOf(".") < 0) {
			name = NAMESPACE + name;
		}
		else if (name.startsWith("uk.ac.gla.terrier")) {
			name = name.replaceAll("uk.ac.gla.terrier", "org.terrier");
		}
		
		//check for already instantiated model
		if (cache.containsKey(name)) {
			model = cache.get(name);
		}
		else {

			try {
				if (name.indexOf("(") > 0) {
					String params = name.substring(name.indexOf("(")+1, name.indexOf(")"));
					String[] parameters = params.split("\\s*,\\s*");
					
					model = (QueryExpansionModel) Class.forName(name.substring(0,name.indexOf("(")))
							.getConstructor(new Class[]{String[].class})
							.newInstance(new Object[]{parameters});
				}
				else{						
					model = (QueryExpansionModel) Class.forName(name).newInstance();
				}
				
			} catch(InvocationTargetException e) {
				logger.error("Recursive problem with weighting model named: "+name, e);
			} catch(Exception e) {
				logger.error("Problem with weighting model named: " + name, e);
			}
			cache.put(name, model);

		}
		
		return model;
	}
	
}
