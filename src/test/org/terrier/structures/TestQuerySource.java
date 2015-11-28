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
 * The Original Code is TestQuerySource.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.PrintWriter;

import org.terrier.applications.batchquerying.QuerySource;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.Files;

/** Base class for testing query sources */
public abstract class TestQuerySource extends ApplicationSetupBasedTest {

	public TestQuerySource() {
		super();
	}

	protected QuerySource processString(String fileContents) throws Exception
	{
		File tmpFile = tmpfolder.newFile("tmpQueries.trec");
		PrintWriter pw = new PrintWriter(Files.writeFileWriter(tmpFile));
		pw.print(fileContents);
		pw.close();
		QuerySource rtr = getQuerySource(tmpFile.toString());
		assertNotNull(rtr);
		return rtr;
	}
	
	protected abstract QuerySource getQuerySource(String filename) throws Exception;

}