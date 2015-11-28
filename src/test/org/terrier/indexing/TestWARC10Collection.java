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
 * The Original is in 'TestWARC10Collection.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

/** We acknowledge Jamie Callan from Carnegie Mellon University for 
 * kindly allowing a small sample of ClueWeb12 being shared. This can
 * found at share/tests/cw12.sample
 * @author Craig Macdonald
 * @since 3.6
 */
public class TestWARC10Collection extends ApplicationSetupBasedTest {

	@Test public void testDocuments() throws Exception {
		InputStream is = Files.openFileStream(ApplicationSetup.TERRIER_SHARE + "/tests/cw12.sample");
		class RedirWARC10Collection extends WARC10Collection
		{
			Map<String,String> redirects = new HashMap<String,String>();
			public RedirWARC10Collection(InputStream input) {
				super(input);				
			}

			@Override
			protected void processRedirect(String source, String target) {
				redirects.put(source, target);
			}
			
		}
		RedirWARC10Collection coll = new RedirWARC10Collection(is);
		assertTrue(coll.nextDocument());
		Document d = coll.getDocument();
		assertNotNull(d);
		assertEquals("clueweb12-0000tw-00-00000", d.getProperty("docno"));
		assertEquals("http://tsawer.net/2012/02/10/france-image-pool-2012-02-10-162252/", d.getProperty("url"));
		while(! d.endOfDocument())
			d.getNextTerm();
		
		assertTrue(coll.nextDocument());
		d = coll.getDocument();
		assertEquals("clueweb12-0000tw-00-00010", d.getProperty("docno"));
		assertEquals("http://tsawer.net/2012/02/10/france-image-pool-2012-02-10-162252/", d.getProperty("url"));
		while(! d.endOfDocument())
			d.getNextTerm();
		
		
		assertFalse(coll.nextDocument());
		assertEquals(2, coll.redirects.size());
		assertEquals("http://www.telegraaf.nl/s/11489878",coll.redirects.get("http://telegraaf.nl/s/11489878"));
		assertEquals("http://www.telegraaf.nl/s/11487424",coll.redirects.get("http://telegraaf.nl/s/11487424"));
		
		coll.close();
		is.close();
	}
	
	@Test public void testDocumentSpecific() throws Exception {
		String filename = ApplicationSetup.getProperty("TestWARC10Collection.specific.file", null);
		if (filename == null)
			return;
		InputStream is = Files.openFileStream(filename);
		class RedirWARC10Collection extends WARC10Collection
		{
			Map<String,String> redirects = new HashMap<String,String>();
			public RedirWARC10Collection(InputStream input) {
				super(input);				
			}

			@Override
			protected void processRedirect(String source, String target) {
				redirects.put(source, target);
			}
			
		}
		RedirWARC10Collection coll = new RedirWARC10Collection(is);
		assertTrue(coll.nextDocument());
		Document d = coll.getDocument();
		assertNotNull(d);
		assertEquals("clueweb12-1905wb-32-00000", d.getProperty("docno"));
		while(! d.endOfDocument())
			d.getNextTerm();
		
		assertTrue(coll.nextDocument());
		d = coll.getDocument();
		assertEquals("clueweb12-1905wb-32-00001", d.getProperty("docno"));
		while(! d.endOfDocument())
			d.getNextTerm();

		assertTrue(coll.nextDocument());
		d = coll.getDocument();
		assertEquals("clueweb12-1905wb-32-00002", d.getProperty("docno"));
		while(! d.endOfDocument())
			d.getNextTerm();


		int count = 31907;
		count -= 3;
		while(count > 0)
		{
			assertTrue(coll.nextDocument());
			count--;
		}
		
		assertFalse(coll.nextDocument());
		assertEquals(4, coll.redirects.size());
		
		coll.close();
		is.close();
	}
	
}
