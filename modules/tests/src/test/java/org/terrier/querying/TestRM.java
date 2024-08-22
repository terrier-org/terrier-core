package org.terrier.querying;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.matching.BaseMatching;
import org.terrier.structures.Index;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestRM extends ApplicationSetupBasedTest
{
    @Test public void testItWorksRM1() throws Exception
    {
        testModel("RM1");
    }

    @Test public void testItWorksRM3() throws Exception
    {
        testModel("RM3");
    }

    protected void testModel(String clzName) throws Exception
    {
        ApplicationSetup.setProperty("termpipelines", "");
        ApplicationSetup.setProperty("prf.mindf", "0");
        ApplicationSetup.setProperty("prf.maxdp", "1");
        ApplicationSetup.setProperty("querying.processes",
            "terrierql:TerrierQLParser,parsecontrols:TerrierQLToControls,parseql:TerrierQLToMatchingQueryTerms,matchopql:MatchingOpQLParser,applypipeline:ApplyTermPipeline,localmatching:LocalManager$ApplyLocalMatching,rm:"+clzName+",qe:QueryExpansion,labels:org.terrier.learning.LabelDecorator,filters:LocalManager$PostFilterProcess");
        Index indx = IndexTestUtils.makeIndex(
            new String[]{"doc1", "doc2"}, 
            new String[]{"the lazy fox jumped over the dog", "but had the presence of mind"});
        Manager m = ManagerFactory._from_(indx.getIndexRef());
        SearchRequest srq = m.newSearchRequest("testQ", "fox");
        srq.setControl("rm", "on");
        m.runSearchRequest(srq);
        assertTrue( ((Request)srq).getMatchingQueryTerms().size() > 1);
        assertTrue( ((Request)srq).getMatchingQueryTerms().get(0).getValue().getTags().contains(BaseMatching.BASE_MATCHING_TAG));
    }

}
