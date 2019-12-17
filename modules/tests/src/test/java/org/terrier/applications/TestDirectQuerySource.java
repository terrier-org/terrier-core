package org.terrier.applications;

import static org.junit.Assert.*;

import org.junit.Test;
import org.terrier.applications.batchquerying.QuerySource;
import org.terrier.applications.batchquerying.QuerySourceUtils;

public class TestDirectQuerySource {

	@Test public void theTest() {
		QuerySource qs = QuerySourceUtils.create(
				new String[]{"q1", "q2"},
				new String[]{"one", "one  two"}, true);
		assertNotNull(qs);
		assertTrue(qs.hasNext());
		assertEquals("one", qs.next());
		assertEquals("q1", qs.getQueryId());
		assertTrue(qs.hasNext());
		assertEquals("one two", qs.next());
		assertEquals("q2", qs.getQueryId());
		assertFalse(qs.hasNext());
	}
}
