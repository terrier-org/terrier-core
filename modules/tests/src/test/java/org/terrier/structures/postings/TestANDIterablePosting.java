package org.terrier.structures.postings;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.terrier.structures.Pointer;
import org.terrier.structures.SimpleBitIndexPointer;

public class TestANDIterablePosting {
	
	@Test public void testSingleDisjoint() throws Exception
	{
		IterablePosting[] ips = new IterablePosting[]{
				new ArrayOfBasicIterablePosting(new int[]{0}, new int[]{1}, new int[]{3}),
				new ArrayOfBasicIterablePosting(new int[]{1}, new int[]{2}, new int[]{4}),
		};
		IterablePosting joined = new ANDIterablePosting(ips, new Pointer[]{new SimpleBitIndexPointer(0, (byte) 0, 1),new SimpleBitIndexPointer(0,(byte)  0, 1)});
		assertEquals(IterablePosting.EOL, joined.next());		
		joined.close();
	}
	
	@Test public void testSingleOverlap() throws Exception
	{
		IterablePosting[] ips = new IterablePosting[]{
				new ArrayOfBasicIterablePosting(new int[]{1}, new int[]{1}, new int[]{3}),
				new ArrayOfBasicIterablePosting(new int[]{1}, new int[]{2}, new int[]{4}),
		};
		IterablePosting joined = new ANDIterablePosting(ips, new Pointer[]{new SimpleBitIndexPointer(0, (byte) 0, 1),new SimpleBitIndexPointer(0,(byte)  0, 1)});
		assertEquals(1, joined.next());
		assertEquals(IterablePosting.EOL, joined.next());		
		joined.close();
	}
	
	@Test public void testTwoOverlap() throws Exception
	{
		IterablePosting[] ips = new IterablePosting[]{
				new ArrayOfBasicIterablePosting(new int[]{0,1}, new int[]{1,1}, new int[]{3}),
				new ArrayOfBasicIterablePosting(new int[]{1,2}, new int[]{2,1}, new int[]{4}),
		};
		IterablePosting joined = new ANDIterablePosting(ips, new Pointer[]{new SimpleBitIndexPointer(0, (byte) 0, 2),new SimpleBitIndexPointer(0,(byte)  0, 2)});
		assertEquals(1, joined.next());
		assertEquals(IterablePosting.EOL, joined.next());		
		joined.close();
	}
	
	@Test public void testTwoOverlapSkip() throws Exception
	{
		ArrayOfBasicIterablePosting[] ips = new ArrayOfBasicIterablePosting[]{
				new ArrayOfBasicIterablePosting(new int[]{0,1}, new int[]{1,1}, new int[]{3}),
				new ArrayOfBasicIterablePosting(new int[]{1,2}, new int[]{2,1}, new int[]{4}),
		};
		IterablePosting joined;
		
		joined = new ANDIterablePosting(ips, new Pointer[]{new SimpleBitIndexPointer(0, (byte) 0, 2),new SimpleBitIndexPointer(0,(byte)  0, 2)});
		assertEquals(1, joined.next(1));
		assertEquals(IterablePosting.EOL, joined.next());	
		joined.close();	
		ips[0].reset();
		ips[1].reset();
		
		joined = new ANDIterablePosting(ips, new Pointer[]{new SimpleBitIndexPointer(0, (byte) 0, 2),new SimpleBitIndexPointer(0,(byte)  0, 2)});
		assertEquals(1, joined.next(0));
		assertEquals(IterablePosting.EOL, joined.next());	
		
		joined.close();
	}
}
