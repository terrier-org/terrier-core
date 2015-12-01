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
 * The Original Code is Js_KLs.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (Original author)
 */
package org.terrier.matching.models;


/**
 * This class implements the Js_KLs weighting model, which is the product 
 * of two measures: the Jefrreys' divergence with the Kullback Leibler's divergence.
 * The two measures are obtained by the addition of one query token. Then Jefrreys'
 * divergence and the information growth in the document by Kullback Leibler's 
 * divergence are computed. The model computes the product of these two information
 *  measures as amount of information carried by a single query token.
 * Js_KLs is an unsupervised model (parameter free model) of IR. 
 * <p>
 * Js_KLs has a high 
 * performance but it can be used with verbose queries. In particular, it has 
 * statistically or moderately significant better MAP performance than most of the
 * supervised models with long queries on the terabyte collection (GOV2) with the 
 * exception of PL2.
 * MAP for long topics, and comparative p values (two-tailed paired t-test) 
 * compared to supervised models (with optimal MAP parameter values) are as follows:
 * <table BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0">
 * <thead><tr CLASS="TableHeadingColor"><td>Queries</td><td>MAP of JS_KLs</td><td>LGD</td><td>Dirichlet_LM</td><td>PL2</td><td>BM25</td><td>In_expB2</td></tr></thead>
 * <tr><td>long</td><td>0.3178</td> <td>(&gt;) p=1.7E-17</td><td>(&gt;) p=0.0544</td><td>(&lt;) p=0.3155</td><td>(&gt;) p=0.7866</td><td>(&gt;) p=5151</td></tr>
 * </table>
 * <p><b>References</b>
 * <ol>
 * <li>Frequentist and Bayesian approach to  Information Retrieval. G. Amati. In 
 * Proceedings of the 28th European Conference on IR Research (ECIR 2006). 
 * LNCS vol 3936, pages 13--24.</li>
 * </ol>
 * @since 3.5
 * @author Gianni Amati
 */
public class Js_KLs extends WeightingModel {
	private static final long serialVersionUID = 1L;
	/** 
	 * A default constructor to make this model.
	 */
	public Js_KLs() {
		super();
	}
	/**
	 * Returns the name of the model, in this case "Js_KLs"
	 * @return the name of the model
	 */

	public final String getInfo() {
		return  "Js_KLs" ;
	}
	/**
	 * Uses Js_KLs to compute a weight for a term in a document.
	 * @param tf The term frequency of the term in the document
	 * @param docLength the document's length
	 * @return the score assigned to a document with the given 
	 *         tf and docLength, and other preset parameters
	 */
	public final double score(double tf, double docLength) {
	    
	    //Two neighbouring distributions in the document: the true and the smoothed one.
	    double maximumLikelihoodEstimate = tf/docLength;                     //the true probability
	    double smoothedProbability  = (tf  +1d)/(docLength +1d);         //the smoothed probability
	    
	    //  The true distribution in the collection:
	    double collectionPrior = termFrequency/numberOfTokens;
	    
	    /** The divergence measure in the document between neighbouring distributions. */
 	    double Js =   (docLength /(docLength+1))*(1 - maximumLikelihoodEstimate) * WeightingModelLibrary.log ((tf+1d)/tf);
	    // The information of the sample wrt  collection priors
 	    double KLs =    
 	    		WeightingModelLibrary.log ( smoothedProbability/collectionPrior) + tf*WeightingModelLibrary.log (1+1d/tf);
	     return keyFrequency   *   tf * Js *KLs  ;
	}
}
