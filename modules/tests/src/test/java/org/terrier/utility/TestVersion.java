package org.terrier.utility;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestVersion {

	@Test public void testVersion()
	{
		String versionString = ApplicationSetup.TERRIER_VERSION;
		assertTrue(versionString.contains("."));
		
	}
	
}
