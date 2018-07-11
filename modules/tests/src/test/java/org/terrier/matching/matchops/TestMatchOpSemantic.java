package org.terrier.matching.matchops;

import static org.junit.Assert.*;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.tests.ApplicationSetupBasedTest;

public class TestMatchOpSemantic extends ApplicationSetupBasedTest {

	@Test
	public void testPrefix() throws Exception {
		
		Index index = IndexTestUtils.makeIndex(new String[]{"doc1"}, new String[]{"aa ab"});
		Pair<EntryStatistics, IterablePosting> pair = new PrefixTermOp("a").getPostingIterator(index);
		EntryStatistics es = pair.getLeft();
		assertEquals(2, es.getFrequency());
		IterablePosting ip = pair.getRight();
		assertEquals(0, ip.next());
		assertEquals(2, ip.getFrequency());
		assertEquals(IterablePosting.EOL, ip.next());
		
	}

}
