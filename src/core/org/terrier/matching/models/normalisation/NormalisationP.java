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
 * The Original Code is NormalisationF.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 */
package org.terrier.matching.models.normalisation;

/**
 * This class implements Term Frequency Normalisation via 
 * Pareto Distributions
 * @author Ben He
  */
public class NormalisationP extends Normalisation{

	private static final long serialVersionUID = 1L;
	/** The name of the normalisation method .*/
	protected final String methodName = "P";
	/**
	 * The default constructor.
	 *
	 */
	public NormalisationP(){
		super();
	}
	/**
	 * Constructor that also sets the parameter z (the power in the normalisation).
	 * @param value The specified value for the parameter z.
	 */
	public NormalisationP(double value){
		super(value);
	}
	/**
	 * Get the name of the normalisation method.
	 * @return Return the name of the normalisation method.
	 */
	public String getInfo(){
		String info = this.methodName+"z"+parameter;
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
		double tfn = tf*Math.pow(averageDocumentLength/docLength, parameter);
		return tfn;
	}



}
