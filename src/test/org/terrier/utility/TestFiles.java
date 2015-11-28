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
 * The Original Code is TestFiles.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;

public class TestFiles extends ApplicationSetupBasedTest {

	static final String[] TESTCASE_EXTENSIONS = new String[]{
		"", ".bz2", ".gz"
	};
	
	static final String[] SUPPORTED_EXTENSIONS = new String[]{
		"", ".bz2", ".gz", ".BZ2", ".GZ"
	};
	
	@Test public void testExists()
	{
		for (String suffix : TESTCASE_EXTENSIONS)
		{
			assertTrue(Files.exists(ApplicationSetup.TERRIER_SHARE + "/tests/files/helloworld.txt" + suffix));
		}
	}
	
	@Test public void testRead() throws Exception
	{
		for (String suffix : TESTCASE_EXTENSIONS)
		{
			assertEquals("hello world", readFirstLineReader(ApplicationSetup.TERRIER_SHARE + "/tests/files/helloworld.txt" + suffix));
			assertEquals("hello world", readFirstLineStream(ApplicationSetup.TERRIER_SHARE + "/tests/files/helloworld.txt" + suffix));
		}
	}
	
	@Test public void testReadBGZ() throws Exception
	{
		
		/*BufferedReader br = Files.openFileReader(ApplicationSetup.TERRIER_SHARE + "/tests/files/helloworld.txt");
		Writer wr = Files.writeFileWriter(ApplicationSetup.TERRIER_SHARE + "/tests/files/helloworld.txt.bgz");
		wr.write(br.readLine());
		br.close();
		wr.close();*/
		
		assertEquals("hello world", readFirstLineReader(ApplicationSetup.TERRIER_SHARE + "/tests/files/helloworld.txt.bgz"));
	}
	
	@Test public void testWrite() throws Exception
	{
		for (String suffix : SUPPORTED_EXTENSIONS)
		{
			File f = super.tmpfolder.newFile("test.txt"+suffix);
			Writer w = Files.writeFileWriter(f.toString());
			w.write("hello world\n");
			w.close();
			assertTrue(Files.exists(f.toString()));
			assertEquals("hello world", readFirstLineReader(f.toString()));
			assertEquals("hello world", readFirstLineStream(f.toString()));
			f.delete();
		}
	}
	
	protected String readFirstLineReader(String filename) throws Exception
	{
		BufferedReader br = Files.openFileReader(filename);
		String rtr = br.readLine();
		br.close();
		return rtr;
	}
	
	protected String readFirstLineStream(String filename) throws Exception
	{
		InputStream is = Files.openFileStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String rtr = br.readLine();
		br.close();
		return rtr;
	}
}
