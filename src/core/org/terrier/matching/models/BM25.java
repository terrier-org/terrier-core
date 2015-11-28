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
 * The Original Code is BM25.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching.models;


/**
 * This class implements the Okapi BM25 weighting model. The
 * default parameters used are:<br>
 * k_1 = 1.2d<br>
 * k_3 = 8d<br>
 * b = 0.75d<br> The b parameter can be altered by using the setParameter method.
 * @author Gianni Amati, Ben He, Vassilis Plachouras
  */
public class BM25 extends WeightingModel {
	private static final long serialVersionUID = 1L;

	/** The constant k_1.*/
	private double k_1 = 1.2d;
	
	/** The constant k_3.*/
	private double k_3 = 8d;
	
	/** The parameter b.*/
	private double b;
	
	/** A default constructor.*/
	public BM25() {
		super();
		b=0.75d;
	}
	/**
	 * Returns the name of the model.
	 * @return the name of the model
	 */
	public final String getInfo() {
		return "BM25b"+b;
	}
	/**
	 * Uses BM25 to compute a weight for a term in a document.
	 * @param tf The term frequency in the document
	 * @param docLength the document's length
	 * @return the score assigned to a document with the given 
	 *         tf and docLength, and other preset parameters
	 */
	public double score(double tf, double docLength) {
		final double K = k_1 * ((1 - b) + b * docLength / averageDocumentLength);
		return WeightingModelLibrary.log((numberOfDocuments - documentFrequency + 0.5d) / (documentFrequency + 0.5d)) *
				((k_1 + 1d) * tf / (K + tf)) *
				((k_3+1)*keyFrequency/(k_3+keyFrequency));
	}


	/**
	 * Sets the b parameter to BM25 ranking formula
	 * @param _b the b parameter value to use.
	 */
	public void setParameter(double _b) {
	    this.b = _b;
	}


	/**
	 * Returns the b parameter to the BM25 ranking formula as set by setParameter()
	 */
	public double getParameter() {
	    return this.b;
	}
	
}
