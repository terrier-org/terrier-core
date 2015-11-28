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
 * The Original Code is TestTagSet.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.utility;

import static org.junit.Assert.*;

import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;

public class TestTagSet extends ApplicationSetupBasedTest {

	@Test public void testSimple() {
		ApplicationSetup.setProperty("TrecDocTags.process", "TEXT");
		TagSet t = new TagSet(TagSet.TREC_DOC_TAGS);
		assertTrue(t.isTagToProcess("text"));
		assertFalse(t.isTagToProcess("abstract"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testConfiguration() {
		ApplicationSetup.setProperty("TrecDocTags.process", "TEXT");
		ApplicationSetup.setProperty("TrecDocTags.skip", "TEXT");
		new TagSet(TagSet.TREC_DOC_TAGS);
	}
}
