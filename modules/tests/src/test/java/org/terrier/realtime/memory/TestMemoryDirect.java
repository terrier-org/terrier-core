package org.terrier.realtime.memory;

import static org.junit.Assert.*;

import org.junit.Test;
import org.terrier.structures.postings.IterablePosting;

public class TestMemoryDirect {

	@Test
	public void test_MemoryDirectPostingIteration() throws Exception {
		
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
	
}
