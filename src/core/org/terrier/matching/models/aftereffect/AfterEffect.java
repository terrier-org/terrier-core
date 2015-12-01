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
 * The Original Code is AfterEffect.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 */
package org.terrier.matching.models.aftereffect;

import java.io.Serializable;

/**
 * This class provides a contract for implementing the first normalisation by 
 * after effect models for the DFR framework. This is referred to as the component (1-prob2) in the DFR framework.
 * Classes implementing this interface are used by the DFRWeightingModel.
 * @author Ben He
  * @see org.terrier.matching.models.DFRWeightingModel
 */
public abstract class AfterEffect implements Serializable, Cloneable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The average document length in collection. */
	protected double avl;
	/** The term frequency normalisation parameter used for method L5 */
	protected double parameter;
	/**
	 * A default constructor
	 */
	public AfterEffect() {/* An empty constructor */
		
	}
	/** Clone this weighting model */
	@Override
	public AfterEffect clone() {
		try{
			return (AfterEffect) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.getMessage());
		}
	}
	
	/**
	 * Set the average document length, which is used for computing the
	 * prior for the first normalisation.
	 * @param value The average document length.
	 */
	public void setAverageDocumentLength(double value){
		this.avl = value;	
	}
	/**
	 * @return the term frequency normalisation parameter
	 */
	public double getParameter() {
		return parameter;
	}
	/**
	 * @param _parameter the term frequency normalisation parameter value to set
	 */
	public void setParameter(double _parameter) {
		this.parameter = _parameter;
	}
	/**
	 * Returns the name of the model.
	 * @return java.lang.String
	 */
	public abstract String getInfo();
	/**
	 * This method provides the contract for implementing first normalisation
	 * by after effect.
	 * @param tf The term frequency in the document
	 * @param documentFrequency The document frequency of the given query term
	 * @param termFrequency The frequency of the given term in the whole collection.
	 * @return The gain of having one more occurrence of the query term.
	 */
	public abstract double gain(double tf, double documentFrequency, double termFrequency);

}
