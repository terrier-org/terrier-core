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
 * The Original Code is WeightingModelLibrary.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Francois Rousseau <rousseau{a.}lix.polytechnique.fr> (original author)
 *   
 */
package org.terrier.matching.models;


/**
 * A library of tf normalizations for weighting models such as the pivoted length normalization
 * described in Singhal et al., 1996.
 * 
 * @since 4.0
 * @author Francois Rousseau
 */
public class WeightingModelLibrary {

	/** The natural logarithm of 2, used to change the base of logarithms.*/
	public static final double LOG_E_OF_2 = Math.log(2.0D);
	/** The logarithm in base 2 of e, used to change the base of logarithms.*/
	public static final double LOG_2_OF_E = 1.0D / LOG_E_OF_2;

	/**
	 * Returns the base 2 log of the given double precision number.
	 * @param d The number of which the log we will compute
	 * @return the base 2 log of the given number
	 */
	public static double log(double d) {
		return (Math.log(d) * LOG_2_OF_E);
	}
	
	/**
	 * Returns the base 2 log of d1 over d2
	 * @param d1 the numerator
	 * @param d2 the denominator
	 * @return the base 2 log of d1/d2
	 */
	public static double log(double d1, double d2) {
		return (Math.log(d1/d2) * LOG_2_OF_E);
	}

	/**
	 * Returns a modified tf with pivot length normalization as described in Singhal et al., 1996.
	 * Pivoted document length normalization (SIGIR '96), pages 21-29.
	 * @param tf the term frequency to modify
	 * @param slope the slope
	 * @param dl the document length
	 * @param avdl the average document length in the collection
	 * @return a pivot length normalized tf
	 */
	public static double tf_pivoted(double tf, double slope, double dl, double avdl) {
		return tf / (1 - slope + slope * dl/avdl);
	}

	/**
	 * Returns a concave tf as described in Robertson and Walker, 1994.
	 * Some simple effective approximations to the 2-poisson model for probabilistic weighted
	 * retrieval (SIGIR '94), page 232-241.
	 * @param tf the term frequency to modify
	 * @param k the concavity coefficient
	 * @return a concave tf
	 */
	public static double tf_concave_k(double tf, double k) {
		return (k + 1) * tf / (k + tf);
	}

	/**
	 * Returns a concave tf as described in Singhal et al., 1999. AT&T at TREC-7.
	 * In Proceedings of the Seventh Text REtrieval Conference (TREC-7), pages 239-252.
	 * @param tf the term frequency to modify
	 * @return a concave tf
	 */
	public static double tf_concave_log(double tf) {
		return 1 + log(1 + log(tf));
	}
	
	/**
	 * Computes relative term frequency.
	 * When tf == docLength we return 0.99999 because relative frequency of 1 produces
	 * Not a Number (NaN) or Negative Infinity as scores in hyper-geometric models (DPH, DLH and DLH13).
	 *
	 * @param tf        raw term frequency
	 * @param docLength length of the document
	 * @return relative term frequency
	 */
	public static final double relativeFrequency(double tf, double docLength) {
		assert tf <= docLength : "tf cannot be greater than docLength";
		double f = tf < docLength ? tf / docLength : 0.99999;
		assert f > 0 : "relative frequency must be greater than zero: " + f;
		assert f < 1 : "relative frequency must be less than one: " + f;
		return f;
	}

	/**
	 * Returns a concave pivot length normalized tf as described in Robertson et al., 1999.
	 * Okapi at TREC-7: automatic ad hoc, filtering, VLC and filtering tracks.
	 * In Proceedings of the Seventh Text REtrieval Conference (TREC-7), pages 253-264
	 * @param tf the term frequency to modify
	 * @param b the slope
	 * @param dl the document length
	 * @param avdl the average document length in the collection
	 * @param k1 the concavity coefficient
	 * @return a concave pivot length normalized tf
	 */
	public static double tf_robertson(double tf, double b, double dl, double avdl, double k1) {
		return tf_concave_k(tf_pivoted(tf, b, dl, avdl), k1);
	}

	/**
	 * Returns a concave pivot length normalized tf as described in Singhal et al., 1999.
	 * AT&T at TREC-7.
	 * In Proceedings of the Seventh Text REtrieval Conference (TREC-7), pages 239-252.
	 * @param tf the term frequency to modify
	 * @param s the slope
	 * @param dl the document length
	 * @param avdl the average document length in the collection
	 * @return a concave pivot length normalized tf
	 */
	public static double tf_cornell(double tf, double s, double dl, double avdl) {
		return tf_pivoted(tf_concave_log(tf), s, dl, avdl);
	}
	
	/**
	* This method provides the contract for implementing the 
	* Stirling formula for the power series.
	* @param n The parameter of the Stirling formula.
	* @param m The parameter of the Stirling formula.
	* @return the approximation of the power series
	*/
	public static double stirlingPower(double n, double m) {
		double dif = n - m;
		return (m + 0.5d) * log(n / m) + dif * log(n);
	}
}
