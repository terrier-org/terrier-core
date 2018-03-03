package org.terrier.querying;

import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.structures.IndexOnDisk;
import org.terrier.terms.PorterStemmer;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestQueryStemmer extends ApplicationSetupBasedTest {

	@Before public void config() throws Exception
	{
		Properties p = new Properties();
		p.setProperty("termpipelines", "Stopwords");
		super.addGlobalTerrierProperties(p);
	}
	
	@Test public void testSingleDcoument() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "Stopwords");
		IndexOnDisk index = (IndexOnDisk) IndexTestUtils.makeIndex(new String[]{"doc1"}, new String[]{"the quick brown fox jumped over the foxed jumping dog"});
		QueryTimeStemmer.createStemEquivsIndex(index, new PorterStemmer());
		
		QueryTimeStemmer qts = new QueryTimeStemmer(index);
		String[] found = qts.getEquiv("fox");
		Arrays.sort(found);
		System.out.println(Arrays.toString(found));
		assertArrayEquals(new String[]{"fox", "foxed"}, found);
	}
	
	
}
