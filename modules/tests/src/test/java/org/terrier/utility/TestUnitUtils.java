package org.terrier.utility;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestUnitUtils {

	@Test public void testInt()
	{
		assertEquals(1000, UnitUtils.parseInt("1000"));
		assertEquals(1000, UnitUtils.parseInt("1K"));
		assertEquals(1024, UnitUtils.parseInt("1Ki"));
	}
	
	@Test public void testDouble()
	{
		assertEquals(1000, UnitUtils.parseDouble("1000"), 0.0d);
		assertEquals(1000, UnitUtils.parseDouble("1K"), 0.0d);
		assertEquals(1000, UnitUtils.parseDouble("1K"), 0.0d);
	}
	
	@Test public void testLong()
	{
		assertEquals(1000, UnitUtils.parseLong("1000"));
		assertEquals(1000, UnitUtils.parseLong("1K"));
		assertEquals(1024, UnitUtils.parseLong("1Ki"));
	}
	
	@Test public void testFloat()
	{
		assertEquals(1000, UnitUtils.parseFloat("1000"), 0.0f);
		assertEquals(1000, UnitUtils.parseFloat("1K"), 0.0f);
		assertEquals(1024, UnitUtils.parseFloat("1Ki"), 0.0f);
	}
	
	
}
