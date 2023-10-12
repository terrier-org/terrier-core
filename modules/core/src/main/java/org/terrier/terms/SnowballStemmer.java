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
 * The Original Code is SnowballStemmer.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.terms;

import java.lang.reflect.Method;

import org.terrier.tartarus.snowball.SnowballProgram;
import org.terrier.utility.ApplicationSetup;

  /** Classic Snowball stemmer implmented by Snowball.
  * @author Craig Macdonald
    */
abstract public class SnowballStemmer extends StemmerTermPipeline {
	/** The actual Snowball object that does the stemming */
	protected SnowballProgram stemmer = null;
	/** The appropriate method. Damn reflection APIs. */
	protected Method stemMethod = null;
	/** The language that we're currently stemming in */
	protected String language = null;

	protected final static Object [] emptyArgs = new Object[0];

	/** Creates a new stemmer object for the language StemLanguage. 
	  * @param StemLanguage Name of the language to generate the stemmer for. Must be a valid Snowball stemmer language.
	  * @param next The next object in the term pipeline
	  */
	protected SnowballStemmer(String StemLanguage, TermPipeline next)
	{
		super(next);
		try{
			Class<? extends SnowballProgram> stemClass = ApplicationSetup.getClass("org.terrier.tartarus.snowball.ext." + StemLanguage + "Stemmer").asSubclass(SnowballProgram.class);
			stemmer = (SnowballProgram) stemClass.newInstance();
			stemMethod = stemClass.getMethod("stem", new Class[0]);
		}catch(Exception e){
			System.err.println("ERROR: Cannot generate snowball stemmer "+StemLanguage+" : "+e);
			e.printStackTrace();
		}
		language = StemLanguage;
	}


	/** Stems the given term and returns the stem 
	  * @param term the term to be stemmed.
	  * @return the stemmed form of term */
	public String stem(String term) {
		stemmer.setCurrent(term);
		/* one can only imagine why a stemmer abstract class wouldn't
		   wouldn't have a stem() method. */
		try{
			stemMethod.invoke(stemmer, emptyArgs); //stemmer.stem();		
		}catch (Exception e) {
			System.err.println("ERROR: Cannot use snowball stemmer "+language+" : "+e);
			e.printStackTrace();
		}
		return stemmer.getCurrent();
	}
}
