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
 * The Original Code is DFR_BM.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 */
package org.terrier.matching.models.basicmodel;

import static org.terrier.matching.models.WeightingModelLibrary.log;

/**
 * This class implements the DFR BM weighting model, which is an approximation of 
 * BM25 in the DFR framework. 
 * Feel free to combine the DFR_BM model with any frequency normalisation method. 
 * However, it is NOT recommended to use DFR_BM with the first normalisation for 
 * after effect. For example, to use the DFR_BM model with the  normalisation 2, 
 * add the following line in file etc/trec.models:
 * DFRWeightingModel(DFR_BM, , 2) Leave the space between the comas blank so 
 * that the first normalisation is disabled. 
 * @author Ben He
  */
public class DFR_BM extends BasicModel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The constant k_1.*/
	private double k_1 = 1.2d;
	/** The constant k_3.*/
	private double k_3 = 1000d;
	/** The name of the model. */
	protected String modelName = "DFRBM";
	/** 
	 * A default constructor.
	 */
	public DFR_BM(){
		super();
	}
	/**
	 * Returns the name of the model.
	 * @return the name of the model
	 */
	public String getInfo(){
		return this.modelName;
	}
	/**
	 * This method computes the score for the implemented weighting model.
	 * @param tf The term frequency in the document
	 * @param documentFrequency The document frequency of the term
	 * @param termFrequency the term frequency in the collection
	 * @param keyFrequency The normalised query term frequency.
	 * @param documentLength The length of the document.
	 * @return the score returned by the implemented weighting model.
	 */
	public double score(
		double tf,
		double documentFrequency,
		double termFrequency,
		double keyFrequency,
		double documentLength){
		double NORM = 1d / (tf + k_1);
		return  ( (k_3 + 1d) * keyFrequency / (k_3 + keyFrequency)) * NORM 
				* tf * log((numberOfDocuments - documentFrequency + 0.5d) / 
						(documentFrequency + 0.5d));
	}
}
