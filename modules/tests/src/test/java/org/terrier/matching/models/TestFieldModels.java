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

public class TestFieldModels extends ApplicationSetupBasedTest {
    @Test(expected=IllegalStateException.class) public void checkNonFieldIndex_PFN() throws Exception
    {
        ApplicationSetup.setProperty("termpipelines", "");
        Index i = IndexTestUtils.makeIndex(
            new String[]{"doc1"},
            new String[]{"this is a document"} 
            );
        Manager m = ManagerFactory.from(i.getIndexRef());
        SearchRequest srq;
        srq = m.newSearchRequest("q1", "document");
        srq.setControl("wmodel", "PL2F");
        m.runSearchRequest(srq);

        srq = m.newSearchRequest("q1", "document.TITLE");
        srq.setControl("terrierql","off");
        srq.setControl("matchopql","on");
        m.runSearchRequest(srq);
    }

    @Test(expected=IllegalStateException.class) public void checkNonFieldIndex_MatchopQL() throws Exception
    {
        ApplicationSetup.setProperty("termpipelines", "");
        Index i = IndexTestUtils.makeIndex(
            new String[]{"doc1"},
            new String[]{"this is a document"} 
            );
        Manager m = ManagerFactory.from(i.getIndexRef());
        SearchRequest srq;
        srq = m.newSearchRequest("q1", "document.TITLE");
        srq.setControl("terrierql","off");
        srq.setControl("matchopql","on");
        srq.setControl("parsecontrols","off");
        srq.setControl("parseql","off");
        m.runSearchRequest(srq);
    }
}