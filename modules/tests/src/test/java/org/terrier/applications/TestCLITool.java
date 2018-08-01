package org.terrier.applications;

import static org.junit.Assert.assertEquals;

import org.apache.commons.cli.CommandLine;
import org.junit.Test;
import org.terrier.applications.CLITool.CLIParsedCLITool;
import org.terrier.utility.ApplicationSetup;

public class TestCLITool {

	public static class testTool extends CLIParsedCLITool
	{
		public testTool(){}
		
		@Override public int run(CommandLine line) throws Exception {
			return 0;
		}	
	}
	
	@Test public void testOneProperty() throws Exception {
		ApplicationSetup.clearAllProperties();
		CLITool.run(testTool.class,new String[]{"-Dfoo=bar"});
		assertEquals("bar", ApplicationSetup.getProperty("foo", null));
		
	}
	
	@Test public void testTwoProperties() throws Exception {
		ApplicationSetup.clearAllProperties();
		CLITool.run(testTool.class,new String[]{"-Dfoo=bar", "-Dfoo2=bar2"});
		assertEquals("bar", ApplicationSetup.getProperty("foo", null));
		assertEquals("bar2", ApplicationSetup.getProperty("foo2", null));
		
	}
	
}
