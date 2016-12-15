package org.terrier.indexing;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestCollectionFactory {

	@Test public void testSplitList()
	{
		assertEquals(1, CollectionFactory.splitList(makeList(1), 1).size());
		assertEquals(1, CollectionFactory.splitList(makeList(2), 1).size());
		assertEquals(1, CollectionFactory.splitList(makeList(1), 2).size());
		assertEquals(2, CollectionFactory.splitList(makeList(2), 2).size());
		
		assertEquals(2, CollectionFactory.splitList(makeList(10), 2).size());
		assertEquals(3, CollectionFactory.splitList(makeList(10), 3).size());
		
	}
	
	List<Object> makeList(int size) {
		List<Object> l = new ArrayList<Object>(size);
		for(int i=0;i<size;i++)
			l.add(new Object());
		return l;
	}
	
}
