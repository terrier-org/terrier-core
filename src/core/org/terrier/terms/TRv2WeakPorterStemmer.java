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
 * The Original Code is TRv2WeakPorterStemmer.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Gianni Amati <gba{a.}fub.it>   
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.terms;
/**
 * An implementation of the Porter stemming algorithm that uses only the first
 * step of the algorithm.
 * @author Craig Macdonald &amp; Gianni Amati &amp; Vassilis Plachouras
  */
public class TRv2WeakPorterStemmer extends TRv2PorterStemmer {
	/** 
	 * Constructs an instance of the TRv2WeakPorterStemmer.
	 * @param next
	 */
	public TRv2WeakPorterStemmer(TermPipeline next)
	{
		super(next);
	}
	/**
	 * Returns the stem of a given term, after applying 
	 * the first step of Porter's stemming algorithm.
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
		/*
		 * With this line, strings of length 1 or 2 don't go through the
		 * stemming process, although no mention is made of this in the
		 * published algorithm. Remove the line to match the published
		 * algorithm.
		 */
		step1ab();
		step1c();
		return new String(b, 0, k+1);
	}
}
