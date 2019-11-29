package org.terrier.realtime.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.PostingUtil;
import org.terrier.utility.ApplicationSetup;

import gnu.trove.TIntHashSet;

/**
 * Unit tests for the Memory Direct Index Structures 
 * @author richardm
 *
 */
public class TestMemoryDirect {

	@Test
	public void testDocumentContentAccess() throws Exception {
		String[] DOCS = new String[]{"hello there fox", "the lazy fox"};
		String[] DOCNOS = new String[]{"doc1", "doc2"};
		
		ApplicationSetup.setProperty("termpipelines", "");
		MemoryIndex index = new MemoryIndex();
		for (int i =0; i<DOCS.length; i++) {
			Map<String,String> docProperties = new HashMap<String,String>();
			docProperties.put("docno", DOCNOS[i]);
			index.indexDocument(IndexTestUtils.makeDocumentFromText(DOCS[i], docProperties));
		}
		
		
		checkDocContents(0, index, new String[]{"hello", "there", "fox"});
		checkDocContents(1, index, new String[]{"the", "lazy", "fox"});
	}
	
	@Test
	public void testMemoryDirectPostingIteration() throws Exception {
		
		MemoryDirectIndex mdi = new MemoryDirectIndex(null);
		
		// Posting 1
		int[] termids1 = { 0, 1, 2, 3, 4 };
		int[] freqs1 = { 1, 2, 3, 4, 5 };
		for (int i =0; i<termids1.length; i++) {
			mdi.add(0, termids1[i], freqs1[i]);
		}
		
		// Posting 2
		int[] termids2 = { 5, 6, 1, 2, 3 };
		int[] freqs2 = { 1, 2, 3, 4, 5 };
		for (int i =0; i<termids2.length; i++) {
			mdi.add(1, termids2[i], freqs2[i]);
		}
		
		// Check that the ordering has not changed
		IterablePosting postingIteratorFor1 = mdi.getPostings(0);
		int previousId = -1;
		while (!postingIteratorFor1.endOfPostings()) {
			int nextTerm = postingIteratorFor1.next();
			assertTrue(nextTerm>previousId);
			checkIDHasTheCorrectFrequency(nextTerm, postingIteratorFor1.getFrequency(), termids1, freqs1);
			previousId = nextTerm;
		}
		
		// Check that terms were re-ordered
		IterablePosting postingIteratorFor2 = mdi.getPostings(1);
		previousId = -1;
		while (!postingIteratorFor2.endOfPostings()) {
			int nextTerm = postingIteratorFor2.next();
			assertTrue(nextTerm>previousId);
			checkIDHasTheCorrectFrequency(nextTerm, postingIteratorFor2.getFrequency(), termids2, freqs2);
			previousId = nextTerm;
		}
		
		mdi.close();
		
	}
	
	public void checkIDHasTheCorrectFrequency(int termid, int freq, int[] termids, int[] freqs) {
		for (int i=0; i<termids.length; i++) {
			if (termids[i]==termid) {
				//System.out.println(termid+" "+freq+" "+freqs[i]);
				assertEquals(freq, freqs[i]);
			}
		}
	}
	
	public void checkDocContents(int docid, MemoryIndex index, String[] terms) throws Exception {
        assertNotNull(index.getDirectIndex());
        DocumentIndexEntry die = index.getDocumentIndex().getDocumentEntry(docid);
        assertNotNull(die);
        IterablePosting ip = ((MemoryDirectIndex)index.getDirectIndex()).getPostings(docid);
        assertNotNull(ip);
        TIntHashSet id1 = new TIntHashSet( PostingUtil.getIds(ip) );
        assertEquals(terms.length, id1.size());
        for(String t : terms)
        {
            LexiconEntry le = index.getLexicon().getLexiconEntry(t);
            assertNotNull("LexiconEntry for term " + t + " not found", le);
            assertTrue("Term " + t + " is missing in document " +docid, id1.contains(le.getTermId()));
        }
    }

	
}
