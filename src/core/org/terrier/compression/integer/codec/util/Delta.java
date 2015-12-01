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
 * The Original Code is Delta.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 *   
 */

package org.terrier.compression.integer.codec.util;

/**
 * Utility class to calculate d-gaps in an array
 * @author Matteo Catena
 * @since 4.0
 */
public final class Delta {

	/**
	 * 
	 * @param data
	 * @param len the number of elements to consider
	 */
	public final static void delta(final int[] data, final int len) {
				
		for (int i = len - 1; i > 0; --i) {
			data[i] -= data[i - 1];
		}
		data[0]++; //to deal with gamma and unary coding
	}

	/**
	 * 
	 * @param data
	 * @param len the number of elements to consider
	 */
	public final static void delta(final long[] data, final int len) {
		
		for (int i = len - 1; i > 0; --i) {
			data[i] -= data[i - 1];
		}
		data[0]++; //to deal with gamma and unary coding
	}	

	/**
	 * 
	 * @param data
	 * @param len the number of elements to consider
	 */
	public final static void inverseDelta(final int[] data, final int len) {
				
		data[0]--; //to deal with gamma and unary coding
		for (int i = 1; i < len; ++i) {
			data[i] += data[i - 1];
		}
	}

	/**
	 * 
	 * @param data
	 * @param len the number of elements to consider
	 */
	public final static void inverseDelta(final long[] data, final int len) {
		
		data[0]--; //to deal with gamma and unary coding
		for (int i = 1; i < len; ++i) {
			data[i] += data[i - 1];
		}
	}	
}
