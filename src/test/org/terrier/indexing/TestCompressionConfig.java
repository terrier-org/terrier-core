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
 * The Original Code is TestCompressionConfig.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */

package org.terrier.indexing;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.terrier.structures.AbstractPostingOutputStream;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.indexing.CompressionFactory;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;
import org.terrier.structures.postings.ArrayOfBasicIterablePosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestCompressionConfig extends ApplicationSetupBasedTest {
	
	protected CompressionConfiguration getConfig(String structure, String[] fieldNames,int hasBlocks, int maxBlocks)
	{
		return new CompressionFactory.BitCompressionConfiguration(structure, fieldNames, hasBlocks, maxBlocks);
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testSimple() throws IOException
	{
		Index index = Index.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
		CompressionConfiguration cc =  getConfig("inverted", new String[0], 0,0);
		
		AbstractPostingOutputStream pos = cc.getPostingOutputStream(((IndexOnDisk)index).getPath() + "/" + ((IndexOnDisk)index).getPrefix() + ".inverted" + cc.getStructureFileExtension());
		Pointer p = pos.writePostings(new ArrayOfBasicIterablePosting(new int[]{0, 1}, new int[]{1,2}));
		pos.close();
		cc.writeIndexProperties(index, "lexicon-entry-inputstream");
		index.flush();
		
		assertTrue(index.hasIndexStructure("inverted"));
		PostingIndex<Pointer> inv = (PostingIndex<Pointer>) index.getIndexStructure("inverted");
		
		IterablePosting ip  = inv.getPostings(p);
		assertNotNull(ip);
		assertEquals(0, ip.next());
		assertEquals(1, ip.getFrequency());
		assertEquals(1, ip.next());
		assertEquals(2, ip.getFrequency());
		index.close();
		IndexUtil.deleteIndex(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
	}
}
