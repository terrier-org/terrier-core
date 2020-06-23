package org.terrier.fat;
import org.terrier.utility.ApplicationSetup;
import org.terrier.structures.*;
import org.terrier.matching.*;
import org.terrier.matching.daat.Full;
import org.terrier.matching.models.*;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.tests.*;
import org.junit.*;
import static org.junit.Assert.*;

public class TestScoringMatchingWithFat extends ApplicationSetupBasedTest {

    @Test public void singleDocumentSingleTerm() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		ApplicationSetup.setProperty("ignore.low.idf.terms", "false");
		Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1"}, 
				new String[]{"term"});
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setQueryId("test");
		mqt.setTermProperty("term", 1.0d);
		mqt.setDefaultTermWeightingModel(new TF_IDF());
		Matching m = new ScoringMatchingWithFat(index, new Full(index), new PL2());
		
        ResultSet r1 = m.match("test", mqt);
        assertTrue(r1 instanceof FatResultSet);
		FatResultSet fr1 = (FatResultSet)r1;
        
    }
}