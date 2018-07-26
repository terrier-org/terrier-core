package org.terrier.matching.matchops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.matching.models.PL2;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.IndexUtil;
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
	
	@Test
	public void testStatsPhraseUW() throws Exception {
		
		Index index = IndexTestUtils.makeIndexBlocks(new String[]{"doc1"}, new String[]{"aa ab"});
		int numdocs = 10000;
		IndexUtil.forceStructure(index, "collectionstatistics", new CollectionStatistics(numdocs, 50, 20000, 100, new long[0]));
		
		Operator[] ops = new Operator[]{
				 new PhraseOp(new String[]{"aa", "bb"}),
				 new UnorderedWindowOp(new String[]{"aa", "bb"}, 8)
		};
		for (Operator op : ops)
		{
			Pair<EntryStatistics, IterablePosting> pair = op.getPostingIterator(index);
			assertNotNull(pair);
			EntryStatistics es = pair.getLeft();
			assertEquals(1, es.getMaxFrequencyInDocuments());
			assertEquals(numdocs/100, es.getDocumentFrequency());
			assertEquals(numdocs/50, es.getFrequency());
			
			assertEquals(0, pair.getRight().next());
			
			//next, check that PL2 can score such postings and stats
			WeightingModel wmodel = new PL2();
			wmodel.setCollectionStatistics(index.getCollectionStatistics());
			wmodel.setEntryStatistics(es);
			wmodel.setKeyFrequency(1);
			wmodel.prepare();
			double score = wmodel.score(pair.getRight());
			assertTrue(Double.isFinite(score));
			assertTrue(score > 0d);
			assertEquals(IterablePosting.EOL, pair.getRight().next());
		}
		
	}

}
