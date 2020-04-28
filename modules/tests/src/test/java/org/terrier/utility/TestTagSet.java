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
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
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

	@Test public void testSimpleWithFactory() {
		TagSet t = TagSet.factory()
			.setDocTag("DOC")
			.setIdTag("DOCNO")
			.setWhitelist("TEXT")
			.setCaseSensitive(true)
			.build();

		assertTrue(t.isIdTag("DOCNO"));
		assertFalse(t.isIdTag("docno"));
		assertTrue(t.isDocTag("DOC"));
		assertFalse(t.isDocTag("doc"));
		
		assertTrue(t.isTagToProcess("TEXT"));
		assertFalse(t.isTagToProcess("text"));
		assertFalse(t.isTagToProcess("abstract"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testConfiguration() {
		ApplicationSetup.setProperty("TrecDocTags.process", "TEXT");
		ApplicationSetup.setProperty("TrecDocTags.skip", "TEXT");
		new TagSet(TagSet.TREC_DOC_TAGS);
	}

	public void testFields() {
		ApplicationSetup.setProperty("FieldTags.process", "");
		FieldScore.init();
		assertFalse(FieldScore.USE_FIELD_INFORMATION);

		ApplicationSetup.setProperty("FieldTags.process", "TEXT");
		FieldScore.init();
		assertTrue(FieldScore.USE_FIELD_INFORMATION);
		assertEquals(1, FieldScore.FIELDS_COUNT);
		assertEquals(1, FieldScore.FIELD_NAMES.length);

		ApplicationSetup.setProperty("FieldTags.process", "TEXT,H1");
		FieldScore.init();
		assertTrue(FieldScore.USE_FIELD_INFORMATION);
		assertEquals(2, FieldScore.FIELDS_COUNT);
		assertEquals(2, FieldScore.FIELD_NAMES.length);
		assertEquals("TEXT", FieldScore.FIELD_NAMES[0]);
		assertEquals("H1", FieldScore.FIELD_NAMES[1]);
		
	}
}
