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
 * The Original Code is In_expC2.java.
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
 * This class implements the In_expC2 weighting model.
 * @author Gianni Amati, Ben He, Vassilis Plachouras
  */
public class In_expC2 extends WeightingModel {

	private static final long serialVersionUID = 1L;

	/** 
	 * A default constructor. This must be followed 
	 * by specifying the c value.
	 */
	public In_expC2() {
		super();
		this.c = 1.0d;
	}

	/** 
	 * Constructs an instance of this class 
	 * with the specified value for the parameter beta.
	 * @param c the term frequency normalisation parameter value.
	 */
	public In_expC2(double c) {
		this();
		this.c = c;
	}
	/**
	 * Returns the name of the model.
	 * @return the name of the model
	 */
	public final String getInfo() {
		return "In_expC2c" + c;
	}
	/**
	* Computes the score according to the weighting models In_expC2.
	* @param tf The term frequency in the document
	* @param docLength the document's length
	* @return the score assigned to a document with the 
	*         given tf and docLength, and other preset parameters
	*/
	public final double score(double tf, double docLength) {
		double TF =
			tf * Math.log(1d + c * averageDocumentLength / docLength);
		double NORM = (termFrequency + 1d) / (documentFrequency * (TF + 1d));
		double f = termFrequency / numberOfDocuments;
		double n_exp = numberOfDocuments * (1 - Math.exp(-f));
		return TF * i.idfDFR(n_exp) * keyFrequency * NORM;
	}
}
