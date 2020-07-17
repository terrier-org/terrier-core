package org.terrier.structures.postings;
import org.junit.Test;

public class TestChainIterablePosting {
    @Test public void testTwo() throws Exception {

        ArrayOfBasicIterablePosting ip1 = new ArrayOfBasicIterablePosting(new int[]{0,1}, new int[2]);
        ArrayOfBasicIterablePosting ip2 = new ArrayOfBasicIterablePosting(new int[]{2,3}, new int[2]);
        IterablePosting ip = ChainIterablePosting.of(new IterablePosting[]{ip1, ip2}, false, false);
        PostingTestUtils.testPostingIds(ip, new int[]{0,1,2,3});
    }    
}