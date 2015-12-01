/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is TestTermCodes.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original contributor)
  *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.utility;

import gnu.trove.TObjectIntHashMap;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * Tests the functionality of the org.terrier.utility.TermCodes class.
 * Creation date: (05/08/2003 10:06:23)
 * @author Vassilis Plachouras
 */
public class TestTermCodes extends TestCase {
	
	static final String[] terms =
	{
		"counter",
		"map",
		"main",
		"getCode",
		"arity",
		"pair",
		"world",
		"java" };
	
	@Test
	public void testTermCodes() {
		TermCodes.reset();
		TObjectIntHashMap<String> check = new TObjectIntHashMap<String>();
		final int termsLength = terms.length;
		for (int i = 0; i < termsLength; i++) {
			int id = TermCodes.getCode(terms[i]);
			check.put(terms[i], id);
		}
		int code;
		for (int j = 0; j < 1000000; j++) {
			for (int i = 0; i < termsLength; i++) {
				code = TermCodes.getCode(terms[i]);
				assertEquals(check.get(terms[i]), code);
			}
		}
		//TODO: get a new term, then check that the new id is unique

	}
	
	@Test
	public void testTermCodesPut() {
		TermCodes.reset();
		final int termsLength = terms.length;
		TObjectIntHashMap<String> check = new TObjectIntHashMap<String>();
		for (int i = 0; i < termsLength; i++) {
			TermCodes.setTermCode(terms[i], i);
			check.put(terms[i], i);
		}
		for (int j = 0; j < 1000000; j++) {
			for (int i = 0; i < termsLength; i++) {
				int code = TermCodes.getCode(terms[i]);
				assertEquals(check.get(terms[i]), code);
			}
		}
	}
}
