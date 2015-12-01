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
 * The Original is in 'TestDefaultSummariser.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.querying.summarisation;

import static org.junit.Assert.*;

import org.junit.Test;


public class TestDefaultSummariser {

	
	public final static String doc1 = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. "
		+"Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, "
		+"when an unknown printer took a galley of type and scrambled it to make a type specimen book."
		+"It has survived not only five centuries, "
		+"but also the leap into electronic typesetting, remaining essentially unchanged.";
	
	@Test public void testTwoOfThreeSentences() {
		Summariser s = new DefaultSummariser();
		String summary = s.generateSummary(doc1,				
				new String[]{"lorem", "ipsum"});
		String expected = "Lorem Ipsum is simply dummy text of the printing and typesetting industry...Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown";
		assertEquals(expected, summary);
	}
	
}
