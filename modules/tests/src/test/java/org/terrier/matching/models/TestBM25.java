package org.terrier.matching.models;

import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.utility.ApplicationSetup;
import org.terrier.structures.Index;
import org.terrier.querying.Manager;
import org.terrier.querying.ManagerFactory;
import org.terrier.querying.SearchRequest;
import java.io.IOException;

import static org.junit.Assert.*;

public class TestBM25 extends ApplicationSetupBasedTest {
    public void testK1() throws Exception
    {
        ApplicationSetup.setProperty("termpipelines", "");
        Index i = IndexTestUtils.makeIndex(
            new String[]{"doc1"},
            new String[]{"this is a document document"} 
            );
        Manager m = ManagerFactory.from(i.getIndexRef());
        SearchRequest srq;
        srq = m.newSearchRequest("q1", "document");
        srq.setControl("wmodel", "BM25");
        m.runSearchRequest(srq);
        double score1 = srq.getResults().get(0).getScore();

        srq = m.newSearchRequest("q1", "document");
        srq.setControl("wmodel", "BM25");
        srq.setControl("bm25.k_1", "1.9");
        
        m.runSearchRequest(srq);
        double score2 = srq.getResults().get(0).getScore();
        assertTrue( Math.abs(score1 - score2) > 0.0d);
    }
}