/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is DFRee.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (Original author)
 */
package org.terrier.matching.models;


/**
 * This class implements the DFRee weighting model. DFRee stands for DFR free from parameters.
 * In particular, the DFRee model computes an average number of extra bits (as information 
 * divergence) that are necessary to code one extra token of the query term with respect to 
 * the probability distribution observed in the document. There are two possible populations 
 * to sample the probability distribution: considering only the document and no other document 
 * in the colection, or the document considered as sample drawn from the entire collection 
 * statistics. DFRee takes an average of these two information measures, that is their inner product.
 * @author Gianni Amati
 */
public class DFRee extends WeightingModel {
	private static final long serialVersionUID = 1L;
	/** model name */
	private static final String name = "DFRee";


	/** 
	 * A default constructor to make this model.
	 */
	public DFRee() {
		super();
	}
	/**
	 * Returns the name of the model, in this case "DFRee"
	 * @return the name of the model
	 */
	public final String getInfo() {
		return name;
	}

	/**
	 * Uses DFRee to compute a weight for a term in a document.
	 * @param tf The term frequency of the term in the document
	 * @param docLength the document's length
	 * @return the score assigned to a document with the given 
	 *         tf and docLength, and other preset parameters
	 */
		public final double score(double tf, double docLength) {
			/**
			 * DFRee model with the log normalisation function.
			 */
                double prior = tf/docLength;
        		double posterior  = (tf +1d)/(docLength +1);
        		double InvPriorCollection = numberOfTokens/termFrequency;
        		//double alpha = 1d/docLength; //0 <= alpha <= posterior
        					
        					
        		double norm = tf*WeightingModelLibrary.log(posterior/prior)  ; 
        		 
        		return keyFrequency * norm *(
        					     tf *( 
        			   - WeightingModelLibrary.log (prior *InvPriorCollection) 
        					     )
        				      +
        					     (tf+1d) *  ( 
        			   + WeightingModelLibrary.log ( posterior*InvPriorCollection) 
        					     )
        					     + 0.5*WeightingModelLibrary.log(posterior/prior)
        			    );
 	}

}
