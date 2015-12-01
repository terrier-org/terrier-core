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
 * The Original Code is BaseTestDocument.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.terrier.tests.ApplicationSetupBasedTest;
public abstract class BaseTestDocument extends ApplicationSetupBasedTest {

	public static void testDocument(Document d, String... terms) {
		assertFalse(d.endOfDocument());
		int i = 0;
		while(i < terms.length && ! d.endOfDocument())
		{
			String t = d.getNextTerm();
			if (t != null)
			{
				String[] parts = terms[i].split(":");
				assertEquals("Term mismatch on token "+ i + " for " + d.getProperty("filename"), parts[0], t);
				for(int j=1;j<parts.length;j++)
					assertTrue("Term " +  parts[0]  + " was not found to occur in field " + parts[j], d.getFields().contains(parts[j]));
				assertEquals("Term " +  parts[0]  + " did not have correct fields", parts.length-1, d.getFields().size());
				i++;
			}
		}
		assertEquals(terms.length, i);
		while(! d.endOfDocument())
		{
			String t = d.getNextTerm();
			assertNull("Trailing term " + t + " was unexpected", t);
		}
		assertTrue(d.endOfDocument());
	}

}
