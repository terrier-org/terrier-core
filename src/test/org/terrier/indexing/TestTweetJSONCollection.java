package org.terrier.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

public class TestTweetJSONCollection extends ApplicationSetupBasedTest {

	final String text = "If only Bradley's arm was longer. Best photo ever. #oscars http://t.co/C9U5NOtGap";
	final String[] terms = new String[]{
		"if", "only", "bradley", "s", "arm", "was", "longer", "best", "photo", "ever", "oscars", "http", "t", "co", "c9u5notgap"
	};
	
	
	@Test public void testTokenisation() throws IOException
	{
		Writer w = Files.writeFileWriter(ApplicationSetup.COLLECTION_SPEC);
		w.write(ApplicationSetup.TERRIER_SHARE + "/tests/tweets/oscars.json.gz");
		w.close();
		
		TwitterJSONCollection tjc = new TwitterJSONCollection();
		assertFalse(tjc.endOfCollection());
		assertTrue(tjc.nextDocument());
		Document d = tjc.getDocument();
		assertNotNull(d);
		int offset = 0;
		while(! d.endOfDocument())
		{
			String t = d.getNextTerm();
			System.err.println(t);
			assertEquals(terms[offset++], t);
		}
		
		Map<String,String> p = d.getAllProperties();
		assertNotNull(p);
		assertTrue(p.containsKey("text"));
		assertEquals(text, p.get("text"));		
		assertEquals("Mon Mar 03 03:06:13 +0000 2014", p.get("created_at"));
		
		assertFalse(tjc.nextDocument());
		tjc.close();
	}
	
}
