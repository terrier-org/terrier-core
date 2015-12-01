/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - Department of Computing Science
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
 * The Original Code is XSqrA_M.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (Original author)
 */
package org.terrier.matching.models;


/**
 * This class implements the XSqrA_M weighting model, which computed the 
 * inner product of Pearson's X^2 with the information growth computed 
 * with the multinomial M.
 * It is an unsupervised DFR model of IR (free from parameters), which 
 * can be used on short or medium verbose queries. 
 * <p> XSqrA_M has a high
 * performance, and in particular has statistically significant better 
 * MAP performance than all other supervised models on the GOV2 collection.
 * MAP for short (title only) and medium (title+description)
 * topics, and comparative p values (two-tailed paired t-test) compared 
 * to supervised models (with optimal MAP parameter values) are as follows:
 * <table BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0">
 * <thead><tr CLASS="TableHeadingColor"><td>Queries</td><td>MAP of XSqrA_M</td><td>LGD</td><td>Dirichlet_LM</td><td>PL2</td><td>BM25</td><td>In_expB2</td></tr></thead>
 * <tr><td>short</td><td>0.3156</td> <td>p=0.3277</td><td>p=0.0075</td><td>p=0.0055</td><td>p=0.0064</td><td>p=0.0002</td></tr>
 * <tr><td>medium</td><td>0.3311</td><td>p=2.3E-07</td><td>p=0.0002</td><td>p=0.0395</td><td>p=0.0025</td><td>p=2.4E-10</td></tr>
 * </table>
 * <p><b>References</b>
 * Frequentist and Bayesian approach to  Information Retrieval. G. Amati. In 
 * Proceedings of the 28th European Conference on IR Research (ECIR 2006). 
 * LNCS vol 3936, pages 13--24.
 * @since 3.5
 * @author Gianni Amati
 */
public class XSqrA_M extends WeightingModel {
	private static final long serialVersionUID = 1L;
	/** 
	 * A default constructor to make this model.
	 */
	public XSqrA_M() {
		super();
	}
	/**
	 * Returns the name of the model, in this case "XSqrA_M"
	 * @return the name of the model
	 */

	public final String getInfo() {
		return "XSqrA_M" ;
	}
	/**
	 * Uses XSqrA_M to compute a weight for a term in a document.
	 * @param tf The term frequency of the term in the document
	 * @param docLength the document's length
	 * @return the score assigned to a document with the given 
	 *         tf and docLength, and other preset parameters
	 */
	public final double score(double tf, double docLength) {
	    
	    //Two neighbouring distributions in the document: the true and the smoothed one.
	    double maximumLikelihoodEstimate = tf/docLength;                     //the true probability
	    double smoothedProbability  = (tf  +1d)/(docLength +1d);              //the smoothed probability
	    
	    //  The true distribution in the collection:
	    double collectionPrior = termFrequency/numberOfTokens;
	    
	    /** The divergence measure (Pearson) of the two neighbouring distributions*/
	    double XSqrA =   Math.pow(1d-maximumLikelihoodEstimate,2)/(tf+1d)  ;  
	    // The information growth in the document from the the true probability to the smoothed one and wrt the collection priors
	    double InformationDelta =  ((tf+1d) * WeightingModelLibrary.log (smoothedProbability/collectionPrior) -tf*WeightingModelLibrary.log (maximumLikelihoodEstimate /collectionPrior) +0.5*WeightingModelLibrary.log(smoothedProbability/maximumLikelihoodEstimate));
	    //the inner product
	    return keyFrequency * tf*XSqrA *InformationDelta;	
	 }
}
