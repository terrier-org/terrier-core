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
 * The Original Code is LGD.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 */
package org.terrier.matching.models;


/**
 * This class implements the LGD weighting model. For more information about
 * this model, see:
 * <ol>
 * <li>Stephane Clinchant and Eric Gaussier. 
 * <a href="http://www.springerlink.com/content/f66q1228432w6605/">Bridging 
 * Language Modeling and Divergence From Randomness Approaches: A Log-logistic
 *  Model for IR</a>. ICTIR 2009, London, UK.</li>
 * <li>Stephane Clinchant and Eric Gaussier. Information-Based Models for Ad Hoc 
 * Information Retrieval. SIGIR 2010, Geneva, Switzerland.</li>
 * </ol>
 * @author Gianni Amati
 */
public class LGD extends WeightingModel {

	private static final long serialVersionUID = 1L;

	/** 
	 * A default constructor. This must be followed 
	 * by specifying the c value.
	 */
	public LGD() {
		super();
		this.c = 1.0d;
	}

	/** 
	 * Constructs an instance of this class with the 
	 * specified value for the parameter c.
	 * @param c the term frequency normalisation parameter value.
	 */
	public LGD(double c) {
		this();
		this.c = c;
	}
	/**
	 * Returns the name of the model.
	 * @return the name of the model
	 */
	public final String getInfo() {
		return "LGDc" + c;
	}
	/**
	 * Uses LGD to compute a weight for a term in a document.
	 * @param tf The term frequency in the document
	 * @param docLength the document's length
	 * @return the score assigned to a document with the given 
	 *         tf and docLength, and other preset parameters
	 */
	public final double score(double tf, double docLength) {
		double TF =
			tf * WeightingModelLibrary.log(1.0d + (c * averageDocumentLength) / docLength);
		double  freq = (1.0D * documentFrequency) / (1.0D * numberOfDocuments);
 		return 
			keyFrequency
			* WeightingModelLibrary.log( ( freq + TF)/freq);
	}
}
