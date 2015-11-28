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
 * The Original Code is BA.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 */
package org.terrier.matching.models.queryexpansion;

import org.terrier.matching.models.WeightingModelLibrary;

/** 
 * This class implements an approximation of the binomial distribution through 
 * the Kullback-Leibler divergence to weight query terms for query expansion.
 * The class is named BA, which standard for Binomial Approximation.
 * That is F * D(f, p)+0.5*log_2 (2*PI �tf(1-f)) with D the 
 * Kullback Leibler divergence, f the MLE estimate of the term frequency in 
 * the retrieved set (sample), F the sample size, p the prior of the term
 * See Equation (8) on page 365 of the paper:
 * Gianni Amati and Cornelis Joost Van Rijsbergen. 2002. Probabilistic models
 *  of information retrieval based on measuring the divergence from randomness. 
 * ACM Trans. Inf. Syst. 20, 4 (October 2002), 357-389. 
 * DOI=10.1145/582415.582416 http://doi.acm.org/10.1145/582415.582416
 * 
 * The description of the query expansion technique and models can be found in
 * Amati, Giambattista (2003),�Probability Models for Information Retrieval 
 * based on Divergence from Randomness (pdf). PhD thesis, University of Glasgow.
 *
 * @author Gianni Amati
  */
public class BA extends QueryExpansionModel {
	/** A default constructor.*/
	public BA() {
		super();
	}
	/**
	 * Returns the name of the model.
	 * @return the name of the model
	 */
	public final String getInfo() {
		return "BA";
	}
	/** 
	 * {@inheritDoc} 
	 */
	public final double parameterFreeNormaliser(){
		return 1d;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public final double parameterFreeNormaliser(double maxTermFrequency, double collectionLength, double totalDocumentLength){
		return 1d;
	}
	/** This method implements the query expansion model.
	 *  @param withinDocumentFrequency double The term frequency in the X top-retrieved documents.
	 *  @param termFrequency double The term frequency in the collection.
	 *  @return double The query expansion weight using he complete 
	 *  Kullback-Leibler divergence.
	 */
	public final double score(
		double withinDocumentFrequency,
		double termFrequency) {
		/**	    return 1- (Math.pow(2d, - this.totalDocumentLength*D(withinDocumentFrequency/this.totalDocumentLength,this.termFrequency/this.collectionLength))/Math.sqrt(2*Math.PI*this.termFrequency*(1d- withinDocumentFrequency/this.totalDocumentLength))); */
		if (withinDocumentFrequency / this.totalDocumentLength
			< termFrequency / this.collectionLength)
			return 0;
		double f = withinDocumentFrequency / this.totalDocumentLength;
		double p = termFrequency / this.collectionLength;
		double D = f * WeightingModelLibrary.log(f, p) + f * WeightingModelLibrary.log(1 - f, 1 - p);
		return this.totalDocumentLength * D
		//D(withinDocumentFrequency / this.totalDocumentLength, termFrequency / this.collectionLength)
		+0.5d
			* WeightingModelLibrary.log(
				2
					* Math.PI
					* termFrequency
					* (1d - withinDocumentFrequency / this.totalDocumentLength));
	}
	/**
	 * This method implements the query expansion model.
	 * @param withinDocumentFrequency double The term frequency in the X top-retrieved documents.
	 * @param termFrequency double The term frequency in the collection.
	 * @param totalDocumentLength double The sum of length of the X top-retrieved documents.
	 * @param collectionLength double The number of tokens in the whole collection.
	 * @param averageDocumentLength double The average document length in the collection.
	 * @return double The score returned by the implemented model.
	 */
	public final double score(
		double withinDocumentFrequency,
		double termFrequency,
		double totalDocumentLength,
		double collectionLength,
		double averageDocumentLength) {
		/**	    return 1- (Math.pow(2d, - this.totalDocumentLength*D(withinDocumentFrequency/this.totalDocumentLength,this.termFrequency/this.collectionLength))/Math.sqrt(2*Math.PI*this.termFrequency*(1d- withinDocumentFrequency/this.totalDocumentLength))); */
		if (withinDocumentFrequency / totalDocumentLength
			< termFrequency / collectionLength)
			return 0;
		double f = withinDocumentFrequency / totalDocumentLength;
		double p = termFrequency / collectionLength;
		double D = f * WeightingModelLibrary.log(f, p) + f * WeightingModelLibrary.log(1 - f, 1 - p);
		return totalDocumentLength * D
		//D(withinDocumentFrequency / this.totalDocumentLength, termFrequency / this.collectionLength)
		+0.5d
			* WeightingModelLibrary.log(
				2
					* Math.PI
					* withinDocumentFrequency //totalDocumentLength* f
					* (1d - withinDocumentFrequency / totalDocumentLength));
	}
}
