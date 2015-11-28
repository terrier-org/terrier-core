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
 * The Original Code is StringComparator.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk
 */
package org.terrier.utility;
import java.util.Comparator;
/**
 * Compares two strings which may have fixed length fields separated 
 * with a non word character (eg a dash), and a last field which corresponds 
 * to an integer. Two examples of such strings are <tt>XXX-XXX-012389</tt> and 
 * <tt>XXX-XXX-1242</tt> (<tt>XXX-XXX-1242</tt> &lt; <tt>XXX-XXX-012389</tt>
 * when compared using this comparator.
 * 
 * <p>
 * This class is primarily used for comparing docnos, especially for
 * TREC like collections. The docnos in the DocumentIndex as expected
 * to be sorted in an order compatible with this comparator.
 * <p><b>Sorting Algorithm:</b><br>
 * <ul>
 * <li>Split strings on non word characters</li>
 * <li>For each field, left-most first:</li>
 * 	<ul>
 * 	<li>Compare as number if both field contains only numbers, return if not equal</li>
 * 	<li>Compare as string if both fields do not contain only numbers, return if not equal</li>
 * 	</ul>
 * </ul>
 * @author Vassilis Plachouras, Craig Macdonald
  */
public class StringComparator implements Comparator<String> {
	/** An instantiation of this class. */
	public static final StringComparator Me = new StringComparator();
	
	/**
	 * Compares two Strings, which have a number of fields that
	 * are separated by one or more non-alphanumeric characters.
	 * @param s1 the first string object to compare.
	 * @param s2 the second string object to compare.
	 * @return int -1, zero, or 1 if the first argument is 
	 *         less than, equal to, or greater than the second 
	 *         argument, respectively.
	 */
	public int compare(String s1, String s2) {
		
		//we assume fields are separated by one or more 
		//non-alphanumeric characters
		final String[] f1 = s1.split("\\W+");
		final String[] f2 = s2.split("\\W+");
		
		final int numOfFields = Math.min(f1.length, f2.length);
		int compareResult = 0;
		int i1; 
		int i2;
		for (int i=0; i<numOfFields; i++) {
			//if the fields are of different lengths
			//then check whether the fields contain only 
			//numerical digits
			if (f1[i].length()!=f2[i].length()) {
				//if the fields are numerical, then compare
				//their numerical values
				if (f1[i].matches("^\\d+$") && f2[i].matches("^\\d+$")) {
					i1 = Integer.parseInt(f1[i]);
					i2 = Integer.parseInt(f2[i]);
					if (i1 == i2) 
						return 0;
					return (i1 > i2) ? 1 : -1; //ensure its in documented range
				} else { //otherwise compare them as strings
					compareResult = f1[i].compareTo(f2[i]);
				}
			} else { //otherwise compare them as strings
				compareResult = f1[i].compareTo(f2[i]);
			}
			if (compareResult!=0)
				return compareResult < 0 ? -1 : 1; //ensure its in documented range
		}
		return 0;
	}

	/** A static access method, to prevent having to instantiate a comparator 
	  * This has the same parameters, return and implementation as compare(Object,Object) 
	  * @since 1.1.0 */
	public static int compareObjects(final Object o1, final Object o2)
	{
		return compareStrings((String)o1,(String)o2);
	}

	/** A static access method, to prevent having to instantiate a comparator
	  * This has the same parameters, return and implementation as compare(Object,Object)
	  * @since 1.1.0 */
	public static int compareStrings(final String s1, final String s2)
	{
		return Me.compare(s1,s2);
	}
	
	/** Will display the comparator value between two strings from the command line arguments. */
	public static void main(String args[])
	{
		System.out.println((new StringComparator()).compare(args[0], args[1]));
	}
} 
