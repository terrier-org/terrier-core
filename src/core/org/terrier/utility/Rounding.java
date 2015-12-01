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
 * The Original Code is Rounding.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *	Gianni Amati <gba{a.}fub.it> (original author)
 *	Ben He <ben{a.}dcs.gla.ac.uk>
 *  Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.utility;
/**
 * A class for performing the rounding of a number 
 * in various ways for various applications.
 * @author Gianni Amati, Ben He, Vassilis Plachouras, Craig Macdonald
  */
public class Rounding {

	
	/**
	 * Rounds to place digits and transforms the double number d 
	 * to a string for printing
	 * @param d double the number to transform
	 * @param place int the number of decimal digits
	 * @return String the string representing the rounded number.
	 */
	public static String toString(double d, int place) {
		if (place > 9)
			throw new IllegalArgumentException("Decimal places must be less than 10");

		if (place <= 0)
			return "" + (int) (d + ((d > 0) ? 0.5 : -0.5));
		StringBuilder s = new StringBuilder(place+4);
		if (d < 0) {
			s.append('-');
			d = -d;
		}
		d += 0.5 * Math.pow(10, -place);
		if (d > 1) {
			int i = (int) d;
			s.append(i);
			d -= i;
		} else
			s.append('0');
		if (d > 0) {
			d += 1.0;
			String f = "" + (int) (d * Math.pow(10, place));
			s.append('.');
			s.append(f.substring(1));
		}
		return s.toString();
	}

	/**
	 * Round a double alue to a specified number of decimal places.
	 * @param val the value to be rounded.
	 * @param places the number of decimal places to round to.
	 * @return val rounded to places decimal places.
	 * @since 1.1.0
	 */
	public static double round(double val, int places) {
		if (places > 9)
			throw new IllegalArgumentException("Decimal places must be less than 10");
		long factor = (long)Math.pow(10,places);

		// Shift the decimal the correct number of places
		// to the right.
		val = val * factor;

		// Round to the nearest integer.
		long tmp = Math.round(val);

		// Shift the decimal the correct number of places
		// back to the left.
		return (double)tmp / factor;
	}

	/**
	 * Round a float value to a specified number of decimal
	 * places. Calls round(double,int) internally.
	 *
	 * @param val the value to be rounded.
	 * @param places the number of decimal places to round to.
	 * @return val rounded to places decimal places.
	 * @since 1.1.0
	 */
	public static float round(float val, int places) {
		return (float)round((double)val, places);
	}

}
