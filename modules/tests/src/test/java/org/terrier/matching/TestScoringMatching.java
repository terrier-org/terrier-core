package org.terrier.matching;
import static org.junit.Assert.*;
import org.junit.Test;
import org.terrier.matching.*;
import org.terrier.querying.*;
import org.terrier.structures.*;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.structures.outputformat.TRECDocnoOutputFormat;
import java.io.*;
import org.terrier.utility.ApplicationSetup;

public class TestScoringMatching extends ApplicationSetupBasedTest {

    @Test public void doTest() throws Exception {
        ApplicationSetup.setProperty("indexer.meta.reverse.keys","docno");
        Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1", "doc2"}, 
                new String[]{"The quick brown fox jumps over the lazy dog",
                    "information retrieval experiments dog"});
        ResultSet qr = new QueryResultSet(new int[]{0}, new double[]{0d}, new short[1]);
        Manager m = ManagerFactory.from(index.getIndexRef());
        SearchRequest srq = m.newSearchRequest("q1", "fox");
        m.runSearchRequest(srq);
        assertEquals(1, srq.getResults().size());

        TRECDocnoOutputFormat dof = new TRECDocnoOutputFormat(index);
        File resFile = super.tmpfolder.newFile("tmp.res");
        PrintWriter pw = new PrintWriter(new FileWriter(resFile));
        dof.printResults(pw, srq, "run", "Q0", 1000);
        pw.close();

        ApplicationSetup.setProperty("matching.trecresults.file", resFile.toString());
        srq = m.newSearchRequest("q1", "dog");
        srq.setControl("matching", ScoringMatching.class.getName() + "," + TRECResultsMatching.class.getName());
        m.runSearchRequest(srq);
        assertEquals(1, srq.getResults().size());
        resFile.delete();
    }

}