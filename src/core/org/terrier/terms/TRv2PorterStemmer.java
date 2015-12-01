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
 * The Original Code is TRv2PorterStemmer.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Gianni Amati <gba{a.}fub.it> (original author)
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk>   
 */
package org.terrier.terms;
/**
 * This is the Porter stemming algorithm, coded up in JAVA by Gianni Amati.
 * All comments were made by Porter, but few ones due to some implementation
 * choices. For Porter's implementation in Java, see PorterStemmer
 * <br>
 * Porter says "It may be be regarded as cononical, in that it follows the
 * algorithm presented in Porter, 1980, An algorithm for suffix stripping, 
 * Program, Vol. 14, no. 3, pp 130-137, only differing from it at the 
 * points marked --DEPARTURE-- below. The algorithm as described in the 
 * paper could be exactly replicated by adjusting the points of DEPARTURE, 
 * but this is barely necessary, because (a) the points of DEPARTURE are 
 * definitely improvements, and (b) no encoding of the Porter stemmer I 
 * have seen is anything like as exact as this version, even with the 
 * points of DEPARTURE!".
 * <br>
 * This class is not thread safe.
 * @author Gianni Amati, modified into a TermPipeline and (Java) optimised by Craig Macdonald
  */
public class TRv2PorterStemmer extends StemmerTermPipeline {
	/** A buffer for word to be stemmed.*/
	protected char[] b;
	protected int k;
	protected int k0;
	
	/** A general offset into the string.*/
	protected int j; 
	/** 
	 * Constructs an instance of the TRv2PorterStemmer.
	 * @param next
	 */
	public TRv2PorterStemmer(TermPipeline next)
	{
		super(next);
	}
	
	/** 
	 * cons(i) is TRUE <=> b[i] is a consonant. 
	 */
	protected boolean cons(int i) {
		switch (b[i]) {
			case 'a' :
			case 'e' :
			case 'i' :
			case 'o' :
			case 'u' :
				return false;
			case 'y' :
				return (i == k0) ? true : !cons(i - 1);
			default :
				return true;
		}
	}
	
	protected boolean consonantinstem() {
		int i;
		for (i = k0; i <= j; i++)
			if (cons(i))
				return true;
		return false;
	}
	
	/** 
	 * Returns true if i-2,i-1,i has the form consonant - vowel - consonant
	 * and also if the second character is not w,x or y. 
	 * This is used when trying to restore an e at the end of a short word.
	 * For example:<br>
	 * <ul>
	 * <li>cav(e)</li>
	 * <li>lov(e)</li>
	 * <li>hop(e)</li>
	 * <li>crim(e)</li>
	 * </ul>
	 * but keep terms snow, box, tray as they are.	
	 */
	protected final boolean cvc(int i) {
		if (i < k0 + 2 || !cons(i) || cons(i - 1) || !cons(i - 2))
			return false;
		{
			int ch = b[i];
			if (ch == 'w' || ch == 'x' || ch == 'y')
				return false;
		}
		return true;
	}
	
	protected final void defineBuffer(String s) {
		final int length = s.length();
		b = new char[length];
		s.getChars(0, length, b, 0);
	}
	
	/**
	 * Returns true if j,(j-1) contain a double consonant. 
	 */
	protected final boolean doublec(int _j) {
		if (_j < k0 + 1)
			return false;
		if (b[_j] != b[_j - 1])
			return false;
		return cons(_j);
	}
	
	/**
	 * Returns true if k0,...k ends with the string s. 
	 */
	protected final boolean ends(String s) 
	{  
		final int length = s.length();
		final int offset = k-length+1;
		if (offset < 0) return false;
		for (int i = 0; i < length; i++) 
			if (b[offset+i] != s.charAt(i)) 
				return false;
		j = k-length;
		return true;
	}
	
	/** Measures the number of consonant sequences between k0 and j. 
	 * If c is a consonant sequence and v a vowel sequence, and 
	 * <..> indicates arbitrary presence:
	 * <ul> 
	 * <li><c><v> gives 0</li>
	 * <li><c>vc<v> gives 1</li>
	 * <li><c>vcvc<v> gives 2</li>
	 * <li><c>vcvcvc<v> gives 3</li>
	 * </ul>
	 */
	protected final int m() {
		int n = 0;
		int i = k0;
		while (true) {
			if (i > j)
				return n;
			if (!cons(i))
				break;
			i++;
		}
		i++;
		while (true) {
			while (true) {
				if (i > j)
					return n;
				if (cons(i))
					break;
				i++;
			}
			i++;
			n++;
			while (true) {
				if (i > j)
					return n;
				if (!cons(i))
					break;
				i++;
			}
			i++;
		}
	}
	
	/** 
	 * Sets (j+1),...k to the characters in the string s, readjusting
	 * k and j. 
	 */
	protected final void setto(int i1, int i2, String str) {
		/* b[k - i1 + 1 .. k - i1 + i2] = str[0.. k - i1 + i2 - (k - i1 + 1)];
		  =>
			b[k - i1 + 1 .. k - i1 + i2] = str[0.. i2 -1]; 
		  =>
		*/
		str.getChars(0, i2 , b, k - i1 + 1);
		k = k - i1 + i2;
		j = k;
	}
	
	/** 
	 * Returns the stem of a given term
	 * @param s String the term to be stemmed.
	 * @return String the stem of a given term.
	 */
	public String stem(String s) {
		k = s.length() - 1;
		k0 = 0;
		j = k;
		defineBuffer(s);
		if (k <= k0 + 1)
			return s; /*-DEPARTURE-*/
		/* With this line, strings of length 1 or 2 don't go through the
		stemming process, although no mention is made of this in the
		published algorithm. Remove the line to match the published
		algorithm. */
		step1ab();
		step1c();
		step2();
		step3();
		step4();
		step5();
		return new String(b, 0, k+1);
	}
	
	/**
	 * Removes the plurals and -ed or -ing. For example,
	 * <ul>	 
	 *    <li>caresses  becomes  caress</li>
	 *	  <li>ponies	becomes  poni</li>
	 *	  <li>ties	  becomes  ti</li>
	 *	  <li>caress	becomes  caress</li>
	 *	  <li>cats	  becomes  cat</li>
	 * 
	 *	  <li>feed	  becomes  feed</li>
	 *	  <li>agreed	becomes  agree</li>
	 *	  <li>disabled  becomes  disable</li>
	 * 
	 *	  <li>matting   becomes  mat</li>
	 *	  <li>mating	becomes  mate</li>
	 *	  <li>meeting   becomes  meet</li>
	 *	  <li>milling   becomes  mill</li>
	 *	  <li>messing   becomes  mess</li>
	 *	
	 *	  <li>meetings  becomes  meet</li>
	 * </ul>
	 */ 
	protected final void step1ab() {
		if (b[k] == 's') {
			if (ends("sses"))
				k -= 2;
			else
				if (ends("ies") && k > 2)
					setto(3, 1, "i");
				else
					if (b[k - 1] != 's' && k > 2)
						k--;
		}
		if (ends("eed")) {
			if (m() > 0)
				k--;
		} else
			if ((ends("ed") || ends("ing")) && vowelinstem() && consonantinstem()) {
				k = j;
				if (ends("at"))
					setto(2, 3, "ate");
				else
					if (ends("bl"))
						setto(2, 3, "ble");
					else
						if (ends("iz"))
							setto(2, 3, "ize");
						else
							if (doublec(k)) {
								k--;
								{
									int ch = b[k];
									if (ch == 'l' || ch == 's' || ch == 'z')
										k++;
								}
							} else
								if (m() == 1 && cvc(k))
									setto(0, 1, "e");
			}
	}
	/** 
	 * Turns terminal y to i when there is another vowel in the stem. 
	 */
	protected final void step1c() {
		if (ends("y") && vowelinstem())
			b[k] = 'i';
	}
	/** 
	 * Maps double suffices to single ones. So -ization ( = -ize plus
	 * -ation) maps to -ize etc. note that the string before the suffix must give
	 * m() > 0. 
	 */
	protected final void step2() {
		switch (b[k - 1]) {
			case 'a' :
				if (ends("ational")) {
					if (m() > 0)
						setto(7, 3, "ate");
					break;
				}
				if (ends("tional")) {
					if (m() > 0)
						setto(6, 4, "tion");
					break;
				}
				break;
			case 'c' :
				if (ends("enci")) {
					if (m() > 0)
						setto(4, 4, "ence");
					break;
				}
				if (ends("anci")) {
					if (m() > 0)
						setto(4, 4, "ance");
					break;
				}
				break;
			case 'e' :
				if (ends("izer")) {
					if (m() > 0)
						setto(4, 3, "ize");
					break;
				}
				break;
			case 'l' :
				if (ends("bli")) {
					if (m() > 0)
						setto(3, 3, "ble");
					break;
				} /*-DEPARTURE-*/
				/* To match the published algorithm, replace this line with
				case 'l': if (ends("\04" "abli")) { r("\04" "able"); break; } */
				if (ends("alli")) {
					if (m() > 0)
						setto(4, 2, "al");
					break;
				}
				if (ends("entli")) {
					if (m() > 0)
						setto(5, 3, "ent");
					break;
				}
				if (ends("eli")) {
					if (m() > 0)
						setto(3, 1, "e");
					break;
				}
				if (ends("ousli")) {
					if (m() > 0)
						setto(5, 3, "ous");
					break;
				}
				break;
			case 'o' :
				if (ends("ization")) {
					if (m() > 0)
						setto(7, 3, "ize");
					break;
				}
				if (ends("ation")) {
					if (m() > 0)
						setto(5, 3, "ate");
					break;
				}
				if (ends("ator")) {
					if (m() > 0)
						setto(4, 3, "ate");
					break;
				}
				break;
			case 's' :
				if (ends("alism")) {
					if (m() > 0)
						setto(5, 2, "al");
					break;
				}
				if (ends("iveness")) {
					if (m() > 0)
						setto(7, 3, "ive");
					break;
				}
				if (ends("fulness")) {
					if (m() > 0)
						setto(7, 3, "ful");
					break;
				}
				if (ends("ousness")) {
					if (m() > 0)
						setto(7, 3, "ous");
					break;
				}
				break;
			case 't' :
				if (ends("aliti")) {
					if (m() > 0)
						setto(5, 2, "al");
					break;
				}
				if (ends("iviti")) {
					if (m() > 0)
						setto(5, 3, "ive");
					break;
				}
				if (ends("biliti")) {
					if (m() > 0)
						setto(6, 3, "ble");
					break;
				}
				break;
			case 'g' :
				if (ends("logi")) {
					if (m() > 0)
						setto(4, 3, "log");
					break;
				}
				/*-DEPARTURE-*/
				/* To match the published algorithm, delete this line */
		}
	}
	
	/** 
	 * Deals with -ic-, -full, -ness etc, similarly to the strategy of step2. 
	 */
	protected final void step3() {
		switch (b[k]) {
			case 'e' :
				if (ends("icate")) {
					if (m() > 0)
						setto(5, 2, "ic");
					break;
				}
				if (ends("ative")) {
					if (m() > 0)
						setto(5, 0, "");
					break;
				}
				if (ends("alize")) {
					if (m() > 0)
						setto(5, 2, "al");
					break;
				}
				break;
			case 'i' :
				if (ends("iciti")) {
					if (m() > 0)
						setto(5, 2, "ic");
					break;
				}
				break;
			case 'l' :
				if (ends("ical")) {
					if (m() > 0)
						setto(4, 2, "ic");
					break;
				}
				if (ends("ful")) {
					if (m() > 0)
						setto(3, 0, "");
					break;
				}
				break;
			case 's' :
				if (ends("ness")) {
					if (m() > 0)
						setto(4, 0, "");
					break;
				}
				break;
		}
	}
	
	/**
	 * Takes off -ant, -ence etc., in context <c>vcvc<v>. 
	 */
	protected final void step4() {
		switch (b[k - 1]) {
			case 'a' :
				if (ends("al"))
					break;
				return;
			case 'c' :
				if (ends("ance"))
					break;
				if (ends("ence"))
					break;
				return;
			case 'e' :
				if (ends("er"))
					break;
				return;
			case 'i' :
				if (ends("ic"))
					break;
				return;
			case 'l' :
				if (ends("able"))
					break;
				if (ends("ible"))
					break;
				return;
			case 'n' :
				if (ends("ant"))
					break;
				if (ends("ement"))
					break; /*-DEPARTURE-*/
				if (ends("ment"))
					break;
				/* To match the published algorithm, replace the previous two lines with
				
							 if (ends("\05" "ement")) if (m()>1) { k=j; return; };
							 if (ends("\04" "ment")) if (m()>1) { k=j; return; }; */
				if (ends("ent"))
					break;
				return;
			case 'o' :
				if (ends("tion"))
					break;
				if (ends("sion"))
					break;
				if (ends("ou"))
					break;
				return;
				/* takes care of -ous */
			case 's' :
				if (ends("ism"))
					break;
				return;
			case 't' :
				if (ends("ate"))
					break;
				if (ends("iti"))
					break;
				return;
			case 'u' :
				if (ends("ous"))
					break;
				return;
			case 'v' :
				if (ends("ive"))
					break;
				return;
			case 'z' :
				if (ends("ize"))
					break;
				return;
			default :
				return;
		}
		if (m() > 1)
			k = j;
	}
	
	/** 
	 * Removes a final -e if m() > 1, 
	 * and changes -ll to -l if m() > 1.
	 */
	protected final void step5() {
		j = k;
		if (b[k] == 'e') {
			int a = m();
			if (a > 1 || a == 1 && !cvc(k - 1))
				k--;
		}
		if (b[k] == 'l' && doublec(k) && m() > 1)
			k--;
	}
	
	/** 
	 * Returns TRUE if  k0,...j contains a vowel. 
	 * @return true if k0,...,j contains a vowel.
	 */
	protected final boolean vowelinstem() {
		int i;
		for (i = k0; i <= j; i++)
			if (!cons(i))
				return true;
		return false;
	}

	/**
	 * main
	 * @param args
	 */
	public static void main(String args[])
	{
		TRv2PorterStemmer ps = new TRv2PorterStemmer(null);
		for (String term: args)
		{
			System.out.println(term+ " -> "+ ps.stem(term));
		}
	}
}
