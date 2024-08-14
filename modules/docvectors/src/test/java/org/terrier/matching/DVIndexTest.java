package org.terrier.matching;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.matching.matchops.PhraseOp;
import org.terrier.matching.matchops.UnorderedWindowOp;
import org.terrier.matching.models.Tf;
import org.terrier.querying.parser.Query.QTPBuilder;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.FilterIndex;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.NgramEntryStatistics;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.tests.ApplicationSetupBasedTest;

public class DVIndexTest extends ApplicationSetupBasedTest
{
	@Test public void testTwo() throws Exception
	{
		Index index = IndexTestUtils.makeIndex(new String[]{"a", "b"}, new String[]{"aa bb aa cc", "bb"});
		PostingIndex<Pointer> newInv = Direct2InvIndex.process(index, new int[]{0,1}, false, false);
		Lexicon<String> lex = index.getLexicon();
		LexiconEntry le;
		IterablePosting ip;
		le = lex.getLexiconEntry("aa");
		ip = newInv.getPostings(le);
		assertEquals(0, ip.next());
		assertEquals(2, ip.getFrequency());
		assertEquals(4, ip.getDocumentLength());
		assertEquals(IterablePosting.EOL, ip.next());
		
		ip = newInv.getPostings(le);
		assertEquals(0, ip.next());
		assertEquals(2, ip.getFrequency());
		assertEquals(4, ip.getDocumentLength());
		assertEquals(IterablePosting.EOL, ip.next());
		
		le = lex.getLexiconEntry("bb");
		ip = newInv.getPostings(le);
		assertEquals(0, ip.next());
		assertEquals(1, ip.getFrequency());
		assertEquals(4, ip.getDocumentLength());
		assertEquals(1, ip.next());
		assertEquals(1, ip.getFrequency());
		assertEquals(1, ip.getDocumentLength());
		assertEquals(IterablePosting.EOL, ip.next());
		
	}
	
	@Test public void testTwoMatchOpsBlocks() throws Exception
	{
		Index index = IndexTestUtils.makeIndexBlocks(new String[]{"a", "b"}, new String[]{"aa bb aa cc", "bb"});
		PostingIndex<Pointer> newInv = Direct2InvIndex.process(index, new int[]{0,1}, true, false);
		
		Index dvIndex = new FilterIndex(index){
			@Override public PostingIndex<?> getInvertedIndex() {return newInv;}
		};
		
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setQueryId("w-matchops");
		mqt.setTermProperty("aa", 1.0d);
		mqt.setTermProperty("bb", 1.0d);		
		mqt.add(QTPBuilder
				.of(new PhraseOp(new String[]{"aa", "bb"}))
				.setWeight(0.1).setTag("sdm")
				.setWeightingModels(Arrays.asList(new Tf()))
				.build());
		mqt.add(QTPBuilder
				.of(new UnorderedWindowOp(new String[]{"aa", "bb"}, 2))
				.setWeight(0.1).setTag("sdm")
				.setWeightingModels(Arrays.asList(new Tf()))
				.build());
		mqt.setDefaultTermWeightingModel(new Tf());
		PostingListManager plm = new PostingListManager(dvIndex, index.getCollectionStatistics(), mqt);
		plm.prepare(false);
		
		EntryStatistics es;
		IterablePosting ip;
		
		
		ip = plm.getPosting(0);
		assertEquals(0, ip.next());
		assertEquals(2, ip.getFrequency());
		assertEquals(4, ip.getDocumentLength());
		assertEquals(IterablePosting.EOL, ip.next());
		
		ip = plm.getPosting(1);
		assertEquals("bb", plm.getTerm(1));
		assertEquals(0, ip.next());
		assertEquals(1, ip.getFrequency());
		assertEquals(4, ip.getDocumentLength());
		assertEquals(1, ip.next());
		assertEquals(1, ip.getFrequency());
		assertEquals(1, ip.getDocumentLength());
		assertEquals(IterablePosting.EOL, ip.next());
		
		ip = plm.getPosting(2);
		es = plm.getStatistics(2);
		assertTrue(es instanceof NgramEntryStatistics);
		assertEquals(0, ip.next());
		assertEquals(1, ip.getFrequency());
		assertEquals(4, ip.getDocumentLength());
		assertEquals(IterablePosting.EOL, ip.next());
		
		ip = plm.getPosting(3);
		es = plm.getStatistics(2);
		assertTrue(es instanceof NgramEntryStatistics);
		assertEquals(0, ip.next());
		assertTrue(ip.getFrequency() >= 1); //TODO check with Nic
		assertEquals(4, ip.getDocumentLength());
		assertEquals(IterablePosting.EOL, ip.next());
		
		plm.close();
		
	}
}