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
 * The Original Code is LemurTF_IDF.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Ben He <ben{a.}dcs.gla.ac.uk> (original author)
 *   Gianni Amati <gba{a.}fub.it> 
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching.models;
/**
 * This class implements the TF_IDF weighting model as it is implemented in <a href="http://www.lemurproject.org">Lemur</a>.
 * See <a href="http://www.cs.cmu.edu/~lemur/1.0/tfidf.ps">Notes on the Lemur TFIDF model. Chenxiang Zhai, 2001</a>.
 * @author Ben He, Gianni Amati, Vassilis Plachouras
  */
public class LemurTF_IDF extends WeightingModel {
	private static final long serialVersionUID = 1L;

	/** The constant k_1.*/
	private double k_1 = 1.2d;
	
	/** The constant b.*/
	private double b = 0.75d;
	/** 
	 * A default constructor. This must be followed by 
	 * specifying the c value.
	 */
	public LemurTF_IDF() {
		super();
	}
	/**
	 * Returns the name of the model.
	 * @return the name of the model
	 */
	public final String getInfo() {
		return "LemurTF_IDF";
	}
	/**
	 * Uses LemurTF_IDF to compute a weight for a term in a document.
	 * @param tf The term frequency in the document
	 * @param docLength the document's length
	 * @return the score assigned to a document with the given 
	 *         tf and docLength, and other preset parameters
	 */
	public final double score(double tf, double docLength) {
		double Robertson_tf = k_1*tf/(tf+k_1*(1-b+b*docLength/averageDocumentLength));
		return keyFrequency*Robertson_tf * 
				Math.pow(WeightingModelLibrary.log(numberOfDocuments/documentFrequency), 2);
	}
}
