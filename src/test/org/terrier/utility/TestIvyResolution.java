package org.terrier.utility;

import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;

public class TestIvyResolution extends ApplicationSetupBasedTest {

	@Test public void testImportSingleDirect() throws Exception
	{
		new IvyResolver().initialise("com.harium.database:sqlite:1.0.5");
		assertNotNull(Thread.currentThread().getContextClassLoader().loadClass("com.harium.database.sqlite.module.SQLiteDatabaseModule"));
		//Class.forName("com.harium.database.sqlite.module.SQLiteDatabaseModule");
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
		p.setProperty("terrier.ivy.coords", "org.xerial:sqlite-jdbc:3.20.1");
	}

}
