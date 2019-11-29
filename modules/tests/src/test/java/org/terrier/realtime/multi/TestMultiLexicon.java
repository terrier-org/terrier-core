package org.terrier.realtime.multi;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestMultiLexicon {

	@Test public void testHashcode()
	{
		check("a");
		check("abaca");
		check("b");
		check("various");
		
	}
	
	void check(String t) {
		int hashcode = MultiLexicon.hashCode(t);
		//System.out.println(hashcode);
		assertEquals(t.charAt(0), MultiLexicon.hashCodePrefix(hashcode));
	}
	
}
