package org.terrier.indexing;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.Files;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestFilteringCollection extends ApplicationSetupBasedTest {
    
    @Test public void testTwoCollectionFourDocs() throws Exception
	{
		String dataFilename = writeTemporaryFile("test.trec", new String[]{
				"<DOC>",
				"<DOCNO>doc1</DOCNO>",
				"test",
				"</DOC>",
				"<DOC>",
				"<DOCNO>doc2</DOCNO>",
				"test this here now",
                "</DOC>",
                "<DOC>",
				"<DOCNO>doc3</DOCNO>",
				"test this here now",
                "</DOC>",
                "<DOC>",
				"<DOCNO>doc4</DOCNO>",
				"test this here now",
				"</DOC>"
            });
            
		Collection c1 = new FilteringCollection( openCollection(dataFilename), 0, 2);
        Collection c2 = new FilteringCollection( openCollection(dataFilename), 1, 2);
        assertTrue(c1.nextDocument());
        assertEquals("doc1", c1.getDocument().getProperty("docno"));
        assertTrue(c2.nextDocument());
        assertEquals("doc2", c2.getDocument().getProperty("docno"));
        assertTrue(c1.nextDocument());
        assertEquals("doc3", c1.getDocument().getProperty("docno"));
        assertTrue(c2.nextDocument());
        assertEquals("doc4", c2.getDocument().getProperty("docno"));
        assertFalse(c1.nextDocument());
        assertFalse(c2.nextDocument());
        assertTrue(c1.endOfCollection());
        assertTrue(c2.endOfCollection());
        
        Collection c5 = new FilteringCollection( openCollection(dataFilename), 0, 5);
        assertTrue(c5.nextDocument());
        assertEquals("doc1", c5.getDocument().getProperty("docno"));
        assertFalse(c5.nextDocument());
        assertTrue(c5.endOfCollection());

        Collection c55 = new FilteringCollection( openCollection(dataFilename), 5, 5);
        assertFalse(c55.nextDocument());
        assertTrue(c55.endOfCollection());
    }
    

    protected Collection openCollection(String dataFilename) throws Exception
	{
		return new TRECCollection(Files.openFileStream(dataFilename));
	}

}
