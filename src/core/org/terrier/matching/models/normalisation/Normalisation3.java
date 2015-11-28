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
 * The Original Code is Normalisation3.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 */
package org.terrier.matching.models.normalisation;

/**
 * This class implements the Dirichlet Priors normalisation.
 * @author Gianni Amati, Ben He
  */
public class Normalisation3 extends Normalisation{

	private static final long serialVersionUID = 1L;
	/** The name of the normalisation method .*/
	protected final String methodName = "3";
	/**
	 * The default constructor.
	 *
	 */
	public Normalisation3(){
		super(1000d);
	}
	/**
	 * The constructor that also sets the parameter mu of the Dirichlet Normalisation.
	 * @param value The specified value for the parameter mu.
	 */
	public Normalisation3(double value){
		super(value);
	}
	/**
	 * Get the name of the normalisation method.
	 * @return Return the name of the normalisation method as a string
	 */
	public String getInfo(){
		String info = this.methodName+"mu"+parameter;
		return info;
	}
	/**
	 * This method gets the normalised term frequency.
	 * @param tf The frequency of the query term in the document.
	 * @param docLength The number of tokens in the document.
	 * @param termFrequency The frequency of the query term in the collection.
	 * @return The normalised term frequency.
	 */
	public double normalise(double tf, double docLength, double termFrequency){
		return parameter*(tf+parameter*termFrequency/numberOfTokens)/(docLength+parameter);
	}
}
