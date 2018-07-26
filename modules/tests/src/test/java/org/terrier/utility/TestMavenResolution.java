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
 * The Original Code is TestMavenResolution.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.utility;

import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;

public class TestMavenResolution extends ApplicationSetupBasedTest {

	@Test public void testImportSingleDirect() throws Exception
	{
		new MavenResolver().initialise("com.harium.database:sqlite:1.0.5");
		assertNotNull(Thread.currentThread().getContextClassLoader().loadClass("com.harium.database.sqlite.module.SQLiteDatabaseModule"));
		//Class.forName("com.harium.database.sqlite.module.SQLiteDatabaseModule");
	}
	
	@Test public void testImportSingleIndirectWithClassifier() throws Exception
	{
		new MavenResolver().initialise("org.nd4j:nd4j-native-platform:0.8.0,org.nd4j:nd4j-native:0.8.0");
		Class<?> clz = Thread.currentThread().getContextClassLoader().loadClass("org.nd4j.linalg.factory.Nd4j");
		assertNotNull(clz);
		Object instance = clz.newInstance();
		assertNotNull(instance);
	}
	
	@Test public void testImportSingleViaTerrierProperties() throws Exception
	{
		assertNotNull(Thread.currentThread().getContextClassLoader().loadClass("org.sqlite.SQLiteConnection"));
		//System.err.println(Thread.currentThread().getContextClassLoader());
		//System.err.println(this.getClass().getClassLoader());
		
		//Class.forName("org.sqlite.SQLiteConnection");
		//System.err.println()")
	}
	
	@Override
	protected void addGlobalTerrierProperties(Properties p) throws Exception {
		super.addGlobalTerrierProperties(p);
		p.setProperty("terrier.mvn.coords", "org.xerial:sqlite-jdbc:3.20.1");
	}

}
