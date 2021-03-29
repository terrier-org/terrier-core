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
 * The Original Code is TestIndexOnDisk.java.
 *
 * The Original Code is Copyright (C) 2017-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures;

import org.junit.Test;
import org.terrier.querying.IndexRef;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;
import static org.junit.Assert.*;

public class TestIndexOnDisk extends ApplicationSetupBasedTest {

	@Test public void testIndexRefVariants() throws Exception
	{
		IndexOnDisk i1 = IndexOnDisk.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH, "data");
		i1.close();
		IndexRef ir1 = IndexRef.of(ApplicationSetup.TERRIER_INDEX_PATH + "/data.properties");
		assertTrue(new IndexOnDisk.DiskIndexLoader().supports(ir1));
		assertNotNull(IndexFactory.of(ir1));
		IndexRef ir2 = IndexRef.of(ApplicationSetup.TERRIER_INDEX_PATH);
		assertTrue(new IndexOnDisk.DiskIndexLoader().supports(ir2));
		assertNotNull(IndexFactory.of(ir2));
	}

	@Test(expected=IllegalArgumentException.class) public void dirNotExists() throws Exception {
		IndexOnDisk newIndex = IndexOnDisk.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH + "/tmp/", "data");
		newIndex.setIndexProperty("hello", "there");
		newIndex.flush();
		newIndex.close();
	}
	
}
