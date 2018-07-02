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
 * The Original Code is TestTRECQueryingMatchOpQL.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.matching.matchops;

import static org.junit.Assert.*;

import org.junit.Test;
import org.terrier.applications.batchquerying.SingleLineTRECQuery;
import org.terrier.applications.batchquerying.TRECQuerying;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.structures.Index;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestTRECQueryingMatchOpQL extends ApplicationSetupBasedTest {

	@Test public void testTopics() throws Exception
	{
		ApplicationSetup.setProperty("FieldTags.process", "TITLE");
		ApplicationSetup.setProperty("termpipelines", "");
		Index index = IndexTestUtils.makeIndexFieldsBlocks(new String[]{"doc1"}, new String[]{"the fox jumped <TITLE>over</TITLE>"});
		assertEquals(1, index.getCollectionStatistics().getNumberOfFields());
		ApplicationSetup.setProperty("trec.topics.parser", SingleLineTRECQuery.class.getName());
		ApplicationSetup.setProperty("SingleLineTRECQuery.tokenise", "false");
		ApplicationSetup.setProperty("trec.topics.matchopql", "true");
		String f = super.writeTemporaryFile("x.topics", new String[]{
				"1 fox",
				"2 #1(fox jumped)",
				"3 over.TITLE"
		});
		ApplicationSetup.setProperty("trec.topics", f);
		TRECQuerying tq = new TRECQuerying(index.getIndexRef());
		tq.intialise();
		tq.processQueries();
	}
	
}
