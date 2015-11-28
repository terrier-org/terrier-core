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
 * The Original Code is Hiemstra_LM.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s): Jie Peng <pj{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching.models;


/**
 * This class implements the Hiemstra LM weighting model.
 * A default lambda value of 0.15 is used, according to section
 * 5.2.3 of Djoerd Hiemstra's thesis:
 * Using language models for information retrieval. PhD Thesis, 
 * Centre for Telematics and Information Technology, University of 
 * Twente, 2001.
 * @author Jie Peng
  */
public class Hiemstra_LM extends WeightingModel {

	private static final long serialVersionUID = 1L;

	/** 
	 * A default constructor. Uses the default value of lambda=0.15.
	 */
	public Hiemstra_LM() {
		super();
		this.c = 0.15;
	}

	/** 
	 * Constructs an instance of this class with the 
	 * specified value for the parameter lambda.
	 * @param lambda the smoothing parameter.
	 */
	public Hiemstra_LM(double lambda) {
		this();
		this.c = lambda;
	}
	/**
	 * Returns the name of the model.
	 * @return the name of the model
	 */
	
	public final String getInfo(){
		return "Hiemstra_LM" + c;
	}
	/**
	 * Uses Hiemestra_LM to compute a weight for a term in a document.
	 * @param tf The term frequency in the document
	 * @param docLength the document's length
	 * @return the score assigned to a document with the given 
	 *         tf and docLength, and other preset parameters
	 */
	public final double score(double tf, double docLength) {

		return 
		  
				WeightingModelLibrary.log(1 + (c * tf * numberOfTokens) 
	/ ((1-c) * termFrequency * docLength ))
	        ;	
		
	/*	Idf.log(((1 - c) * (tf / docLength)) 
		/ (c * (termFrequency / numberOfTokens)) 
		+ 1)
		+ Idf.log(c * (termFrequency / numberOfTokens))
		;
	*/	
	}
}
