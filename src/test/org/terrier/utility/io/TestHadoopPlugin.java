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
 * The Original Code is TestHadoopPlugin.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.utility.io;

import junit.framework.TestCase;

import org.apache.hadoop.mapred.JobConf;
import org.junit.Test;

import org.terrier.utility.io.HadoopPlugin.JobFactory;

/** Test HadoopPlugin works as expected */
@SuppressWarnings("deprecation")
public class TestHadoopPlugin extends TestCase {

	final static String RANDOM_PROPERTY = "random.property.name";	

	static boolean validPlatform()
	{
		String osname = System.getProperty("os.name");
		if (osname.contains("Windows"))
			return false;
		return true;
	}
	
	
	protected void checkTwoJC(JobConf jc1, JobConf jc2)
	{
		jc1.set(RANDOM_PROPERTY, "notnull");
		jc1.setMaxMapAttempts(3014);
	
		assertNull(jc2.get(RANDOM_PROPERTY, null));
		assertNotSame(3014, jc2.getMaxMapAttempts());
	}
	
	@Test public void testNotGlobalConfTwoSessions() throws Exception
	{
		JobFactory jf1 = HadoopPlugin.getJobFactory("session1");
		JobConf jc1 = jf1.newJob();
				
		JobFactory jf2 = HadoopPlugin.getJobFactory("session2");
		JobConf jc2 = jf2.newJob();
		
		checkTwoJC(jc1, jc2);
	}
	
	@Test public void testNotGlobalConfOneSession() throws Exception
	{
		JobFactory jf = HadoopPlugin.getJobFactory("session1");
		JobConf jc1 = jf.newJob();
		JobConf jc2 = jf.newJob();
		
		checkTwoJC(jc1, jc2);
	}



	@Override
	protected void setUp() throws Exception {
		System.setProperty("terrier.home", System.getProperty("user.dir"));
		System.err.println("terrier.home assumed to be "+ System.getProperty("user.dir"));
		org.terrier.utility.ApplicationSetup.bootstrapInitialisation();
	}
}
