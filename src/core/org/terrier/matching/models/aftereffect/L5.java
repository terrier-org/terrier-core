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
 * The Original Code is L5.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 */
package org.terrier.matching.models.aftereffect;

/**
 * This class implements the L5 model for the first normalisation by 
 * after effect. L5 stands for the Laplace succession with a prior.
 * @author Ben He
 */
public class L5 extends AfterEffect{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The name of the model. */
	protected final String methodName = "L5";
	/**
	 * This method returns the name of the model.
	 * @return String Return the name of the model.
	 */
	public String getInfo(){
		return this.methodName;
	}
	/**
	 * This method computes the gain of encountering an extra token of the query term.
	 * @param tf The term frequency in the document
	 * @param documentFrequency The document frequency of the term
	 * @param termFrequency the term frequency in the collection
	 * @return the gain returned by the implemented formula.
	 */
	public double gain(double tf, double documentFrequency, double termFrequency){
		double prior = tf/(parameter * avl);
		return Math.pow(1-prior, 2)/(1+tf);
	}
}
