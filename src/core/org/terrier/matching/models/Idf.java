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
 * The Original Code is Idf.java.
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

import static org.terrier.matching.models.WeightingModelLibrary.LOG_2_OF_E;
import java.io.Serializable;

/**
 * This class computes the idf values for specific terms in the collection.
 * @author Gianni Amati, Ben He, Vassilis Plachouras
 */
public final class Idf implements Serializable, Cloneable{
	private static final long serialVersionUID = 1L;

	/** The number of documents in the collection.*/
	private double numberOfDocuments;

	/** A default constructor. NOTE: You must set the number of documents
	  * if you intend to use the idf* functions in this class */
	public Idf() {}

	/** Make a perfect clone of this object */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
	
	/** 
	 * A constructor specifying the number of documents in the collection.
	 * @param docs The number of documents.
	 */
	public Idf(double docs) {
		numberOfDocuments = docs;
	}
	
	/**
	 * Returns the idf of d.
	 * @param d The given term frequency
	 * @return the base 2 log of numberOfDocuments/d
	 */
	public double idf(double d) {
		return (Math.log(numberOfDocuments/d) * LOG_2_OF_E);
	}
	
	/**
	 * Returns the idf of the given number d.
	 * @param d the number for which the idf will be computed.
	 * @return the idf of the given number d.
	 */
	public double idf(int d) {
		return (Math.log(numberOfDocuments/((double)d)) * LOG_2_OF_E);
	}
	
	/**
	 * Returns the idf of d.
	 * @param d The given term frequency
	 * @return the base 2 log of numberOfDocuments/d
	 */
	public double idfDFR(double d) {
		return (Math.log((numberOfDocuments+1)/(d+0.5)) * LOG_2_OF_E);
	}
	
	/**
	 * Returns the idf of the given number d.
	 * @param d the number for which the idf will be computed.
	 * @return the idf of the given number d.
	 */
	public double idfDFR(int d) {
		return (Math.log((numberOfDocuments+1)/((double)d+0.5d)) * LOG_2_OF_E);
	}
	
	/**
	 * The INQUERY idf formula. We need to check again this formula, 
	 * as it seems that there is a bug in the expression
	 * numberOfDocuments - d / d.
	 * @param d the number for which the idf will be computed
	 * @return the INQUERY idf of the number d
	 */
	public double idfENQUIRY(double d) {
		return (Math.log(numberOfDocuments - d / d) * LOG_2_OF_E);
	}
	
	/**
	 * Return the normalised idf of the given number.
	 * @param d The number of which the idf is computed.
	 * @return the normalised idf of d
	 */
	public double idfN(double d) {
		return (WeightingModelLibrary.log(numberOfDocuments, d) / log(numberOfDocuments));
	}
	/**
	 * Set number of documents
	 * @param N the number of ducuments
	 */
	public void setNumberOfDocuments(double N){
		this.numberOfDocuments = N;
	}
	
	/**
	 * Return the normalised idf of the given number.
	 * @param d The number of which the idf is computed.
	 * @return the normalised idf of d
	 */
	public double idfN(int d) {
		return (WeightingModelLibrary.log(numberOfDocuments, (double)d) / log(numberOfDocuments));
	}
	
	/**
	 * The normalised INQUERY idf formula
	 * @param d the number for which we will compute the normalised idf
	 * @return the normalised INQUERY idf of d
	 */
	public double idfNENQUIRY(double d) {
		return (WeightingModelLibrary.log(numberOfDocuments + 1.0D, d + 0.5D) / log(numberOfDocuments+1.0D));
	}

	/**
	 * main
	 * @param args
	 */
	public static void main(String[] args)
	{
		for(String v : args)
		{
			System.out.println("log_2("+v+")=" +log(Double.parseDouble(v)));
		}
	}
	
	/**
	 *Returns the base 2 log of the given double precision number.
	 * Method has been moved to WeightingModelLibrary
	 * @param d The number of which the log we will compute
	 * @return the base 2 log of the given numbers
	 */
	@Deprecated
	public static double log(double d) {
		return WeightingModelLibrary.log(d);
	}
	
	/**
	 * Returns the base 2 log of d1 over d2. Do not use, moved to WeightingModelLibrary
	 * @param d1 the numerator
	 * @param d2 the denominator
	 * @return the base 2 log of d1/d2
	 * @return the base 2 log of the given numbers
	 *  Do not use, moved to WeightingModelLibrary
	 */
	@Deprecated 
	public static double log(double d1, double d2) {
		return WeightingModelLibrary.log(d1,d2);
	}
	
	/** The natural logarithm of 2, used to change the base of logarithms.
	 Do not use, moved to WeightingModelLibrary*/
	@Deprecated
	public static final double LOG_E_OF_2 = Math.log(2.0D);
	/** The logarithm in base 2 of e, used to change the base of logarithms.
	 * Do not use, moved to WeightingModelLibrary*/
	@Deprecated
	public static final double REC_LOG_2_OF_E = 1.0D / LOG_E_OF_2;
	
}
