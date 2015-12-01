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
 * The Original Code is TestUtils.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.ArrayUtils;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.FieldScore;
import org.terrier.utility.Files;


public class OnDiskShakespeareEndToEndTest extends BasicShakespeareEndToEndTest {

	@Override
	protected void doIndexing(String... trec_terrier_args) throws Exception
	{
		
		String indextype = "basic";
		if (ArrayUtils.toString(trec_terrier_args).contains("FieldTags.process")) indextype="fields";
		else FieldScore.FIELDS_COUNT=0;
		
		Files.copyFile("share/tests/shakespeare/indices/terrier-3.x/shak-"+indextype+".direct.bf", ApplicationSetup.TERRIER_INDEX_PATH+ApplicationSetup.FILE_SEPARATOR+ApplicationSetup.TERRIER_INDEX_PREFIX+".direct.bf");
		Files.copyFile("share/tests/shakespeare/indices/terrier-3.x/shak-"+indextype+".document.fsarrayfile", ApplicationSetup.TERRIER_INDEX_PATH+ApplicationSetup.FILE_SEPARATOR+ApplicationSetup.TERRIER_INDEX_PREFIX+".document.fsarrayfile");
		Files.copyFile("share/tests/shakespeare/indices/terrier-3.x/shak-"+indextype+".inverted.bf", ApplicationSetup.TERRIER_INDEX_PATH+ApplicationSetup.FILE_SEPARATOR+ApplicationSetup.TERRIER_INDEX_PREFIX+".inverted.bf");
		Files.copyFile("share/tests/shakespeare/indices/terrier-3.x/shak-"+indextype+".lexicon.fsomapfile", ApplicationSetup.TERRIER_INDEX_PATH+ApplicationSetup.FILE_SEPARATOR+ApplicationSetup.TERRIER_INDEX_PREFIX+".lexicon.fsomapfile");
		Files.copyFile("share/tests/shakespeare/indices/terrier-3.x/shak-"+indextype+".lexicon.fsomaphash", ApplicationSetup.TERRIER_INDEX_PATH+ApplicationSetup.FILE_SEPARATOR+ApplicationSetup.TERRIER_INDEX_PREFIX+".lexicon.fsomaphash");
		Files.copyFile("share/tests/shakespeare/indices/terrier-3.x/shak-"+indextype+".lexicon.fsomapid", ApplicationSetup.TERRIER_INDEX_PATH+ApplicationSetup.FILE_SEPARATOR+ApplicationSetup.TERRIER_INDEX_PREFIX+".lexicon.fsomapid");
		Files.copyFile("share/tests/shakespeare/indices/terrier-3.x/shak-"+indextype+".meta-0.fsomapfile", ApplicationSetup.TERRIER_INDEX_PATH+ApplicationSetup.FILE_SEPARATOR+ApplicationSetup.TERRIER_INDEX_PREFIX+".meta-0.fsomapfile");
		Files.copyFile("share/tests/shakespeare/indices/terrier-3.x/shak-"+indextype+".meta.idx", ApplicationSetup.TERRIER_INDEX_PATH+ApplicationSetup.FILE_SEPARATOR+ApplicationSetup.TERRIER_INDEX_PREFIX+".meta.idx");
		Files.copyFile("share/tests/shakespeare/indices/terrier-3.x/shak-"+indextype+".meta.zdata", ApplicationSetup.TERRIER_INDEX_PATH+ApplicationSetup.FILE_SEPARATOR+ApplicationSetup.TERRIER_INDEX_PREFIX+".meta.zdata");
		Files.copyFile("share/tests/shakespeare/indices/terrier-3.x/shak-"+indextype+".properties", ApplicationSetup.TERRIER_INDEX_PATH+ApplicationSetup.FILE_SEPARATOR+ApplicationSetup.TERRIER_INDEX_PREFIX+".properties");
		
		//check that indexing actually created an index
		assertTrue("Index does not exist at ["+ApplicationSetup.TERRIER_INDEX_PATH+","+ApplicationSetup.TERRIER_INDEX_PREFIX+"]", Index.existsIndex(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX));
		IndexOnDisk i = Index.createIndex();
		assertNotNull(Index.getLastIndexLoadError(), i);
		assertEquals(ApplicationSetup.TERRIER_VERSION,i.getIndexProperty("index.terrier.version", ""));
		assertTrue("Index does not have an inverted structure", i.hasIndexStructure("inverted"));
		assertTrue("Index does not have an lexicon structure", i.hasIndexStructure("lexicon"));
		assertTrue("Index does not have an document structure", i.hasIndexStructure("document"));
		assertTrue("Index does not have an meta structure", i.hasIndexStructure("meta"));
		addDirectStructure(i);
		i.close();
		finishIndexing();
	}
	
}
