package org.terrier.structures.postings;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestFieldOnlyIterablePosting {

	@Test public void doTest() throws Exception
	{
		ArrayOfFieldIterablePosting fips = getFips();
		IterablePosting ip1 = new FieldOnlyIterablePosting(fips, 0);
		assertEquals(1, ip1.next());
		assertEquals(1, ip1.getFrequency());
		assertEquals(2, ip1.getDocumentLength());
		
		assertEquals(2, ip1.next());
		assertEquals(8, ip1.getFrequency());
		assertEquals(3, ip1.getDocumentLength());
		assertEquals(IterablePosting.EOL, ip1.next());
		ip1.close();
	}

	protected ArrayOfFieldIterablePosting getFips() {
		return new ArrayOfFieldIterablePosting(
				new int[]{0,1,2},
				new int[]{0,0,0}, //doesnt matter,
				new int[]{1,2,3},
				new int[][]{ new int[]{0,1},  new int[]{1,9},  new int[]{8,8}},//tff
				new int[][]{ new int[]{1,1}, new int[]{2,2}, new int[]{3,3}});//lf
	}
	
}
