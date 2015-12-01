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
 * The Original Code is BasicModel.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */

package org.terrier.matching.models.basicmodel;


import java.io.Serializable;
import org.terrier.matching.models.Idf;
import static org.terrier.matching.models.WeightingModelLibrary.log;

/**
 * This class provides a contract for implementing the basic models for randomness in the DFR framework, 
 * for use with the DFRWeightingModel class.
 * This is referred to as the component -log(prob1) in the DFR framework.
 * @author Ben He
  * @see org.terrier.matching.models.DFRWeightingModel
 */
public abstract class BasicModel implements Serializable, Cloneable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The class used for computing the idf values.*/
	protected Idf i;
	/** The number of documents in the whole collection. */
	protected double numberOfDocuments;
	/** The number of tokens in the whole collection */
	protected double numberOfTokens;

	/**
	 * A default constructor that initialises the idf i attribute
	 */
	public BasicModel() {
		i = new Idf();
	}
	
	/** Clone this weighting model */
	@Override
	public BasicModel clone() {
		try{
			BasicModel newModel = (BasicModel)super.clone();
			newModel.i = (Idf)this.i.clone();
			return newModel;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
	
	/**
	 * Returns the name of the model.
	 * @return java.lang.String
	 */
	public abstract String getInfo();

	/**
	 * Sets the number of documents in the collection.
	 * @param numOfDocs the number of documents in the collection.
	 */
	public void setNumberOfDocuments(double numOfDocs) {
		this.numberOfDocuments = numOfDocs;
		this.i.setNumberOfDocuments(numOfDocs);
	}

	/**
	 * Set the number of tokens in the collection.
	 * @param numTokens double The number of tokens in the collection.
	 */
	public void setNumberOfTokens(double numTokens) {
		this.numberOfTokens = numTokens;	
	}

	/**
	 * This method provides the contract for implementing weighting models.
	 * @param tf The term frequency in the document
	 * @param documentFrequency The document frequency of the term
	 * @param termFrequency The term frequency of the term in the collection
	 * @param keyFrequency The normalised query term frequency.
	 * @param documentLength The length of the document.
	 * @return the score returned by the implemented weighting model.
	 */
	public abstract double score(
		double tf,
		double documentFrequency,
		double termFrequency,
		double keyFrequency,
		double documentLength);

	/**
	* This method provides the contract for implementing the 
	* Stirling formula for the power series.
	* @param n The parameter of the Stirling formula.
	* @param m The parameter of the Stirling formula.
	* @return the approximation of the power series
	*/
	public double stirlingPower(double n, double m) {
		double dif = n - m;
		return (m + 0.5d) * log(n / m) + dif * log(n);
	}
}
