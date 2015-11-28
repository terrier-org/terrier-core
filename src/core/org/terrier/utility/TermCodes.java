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
 * The Original Code is TermCodes.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.utility;
import gnu.trove.TObjectIntHashMap;
/**
 * <p>This class is used for assigning codes to terms as we 
 * index a document collection.</p>
 * <p>It makes use of two properties from the default 
 * properties file. The first one is <tt>termcodes.initialcapacity</tt>, 
 * which specifies the initial capacity of the used hash map. The default 
 * value is 3000000.</p>
 * <p>The second property is <tt>termcodes.garbagecollect</tt>, 
 * which enables or disables garbage collection during the call 
 * of the method reset(). The default value is <tt>true</tt>.
 *
 * @author Vassilis Plachouras
 */
public class TermCodes {
	/** The initial capacity of the hashmap.*/
	private static int hashMapCapacity;
	
	/** 
	 * The hashmap that stores the mapping 
	 * from terms hash codes to code.
	 */
	private static final TObjectIntHashMap<String> map = new TObjectIntHashMap<String>(hashMapCapacity);
	/** 
	 * The counter that represents the new 
	 * code for the next not already encountered term.
	 */
	private static int counter = 0;
	/** A buffer variable.*/
	private static int code = 0;
	/** 
	 * The property that enables or disables 
	 * garbage collection during reseting.
	 */
	private static boolean garbageCollection;
	/** 
	 * Static initialisation of the class properties from 
	 * the properties file. It calls the method initialise().
	 */
	static {
		initialise();
	}
	/** 
	 * Initialises the properties from the property file.
	 * The initial capacity of the hash map, is set to the 
	 * value of the property <tt>termcodes.initialcapacity</tt>.
	 * The default value is <tt>3000000</tt>. The second property 
	 * is related to the method reset() and enables or disables 
	 * garbage collection when the reset method is called. 
	 * The corresponding property is <tt>termcodes.garbagecollect</tt>, 
	 * and its default property is <tt>true</tt>.
	 */
	 public static void initialise() {
		hashMapCapacity = Integer.parseInt(
			ApplicationSetup.getProperty("termcodes.initialcapacity",
					                                  "3000000"));
		garbageCollection =
			Boolean.parseBoolean(ApplicationSetup.getProperty("termcodes.garbagecollect","true"));
			
	 }

	/**
	 * Returns the code for a given term.
	 * @param term String the term for which 
	 *        the code will be returned.
	 * @return int the code for the given term
	 */
	public static final int getCode(final String term) {
		/* if we have encountered a new term, add it to the
		 * hash map and return the new term code, otherwise
		 * return the already assigned term code */
		if ((code = map.get(term)) == 0)
			map.put(term, (code = ++counter ));
		return --code;

		/* NB: because the GNU trove TObjectIntHashMap returns 0
		 * for not found, we store 1 above the true termcode for
		 * every term. Eg the first term has true termcode 0, but
		 * the value 1 is stored in the map*/
	}
	
	
	/**
	 * Resets the hashmap that contains the mapping 
	 * from the terms to the term ids. If the property 
	 * <tt>garbageCollection</tt> is <tt>true</tt>, 
	 * then it performs garbage collection in order to 
	 * free alocated memory. This method should be 
	 * called after the creation of the lexicon.
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
			value="DM_GC",
			justification="Forcing GC is an essential part of releasing" +
					"memory for further indexing")
	public static void reset() {
		if (counter == 0)
			return;
		map.clear();

		if (garbageCollection)
			System.gc();

		counter = 0;
		code = 0;
	}

	/** For when you manually want to set the term for a given term, and you
	  * know that this term and termcodes do NOT exist, then you can use
	  * this method. <b>NB:</b> counter variable above probably needs to be
	  * considered in this method. */
	public static void setTermCode(final String term, final int termCode) {
		map.put(term, termCode+1);
	}
}
