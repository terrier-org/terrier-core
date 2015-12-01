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
 * The Original Code is TestMemoryIndexer.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory;

import org.junit.Test;
import org.terrier.indexing.Collection;
import org.terrier.indexing.CollectionDocumentList;
import org.terrier.indexing.Document;
import org.terrier.indexing.TestIndexers;
import org.terrier.realtime.MemoryIndexer;
import org.terrier.structures.Index;
import org.terrier.structures.indexing.Indexer;
import org.terrier.utility.ApplicationSetup;

public class TestMemoryIndexer extends TestIndexers {

	@Test
	public void testBasicNoFields() throws Exception {
		ApplicationSetup.setProperty("FieldTags.process", "");
		testIndexer(new MemoryIndexer(), false, false, true);
	}
	
	protected Index doIndexing(Indexer indexer, boolean fieldsExpected,	Document[] sourceDocs) {
		Collection col = new CollectionDocumentList(sourceDocs, "filename");
		indexer.createDirectIndex(new Collection[] { col });
		indexer.createInvertedIndex();
		return ((MemoryIndexer) indexer).getIndex();
	}

}
