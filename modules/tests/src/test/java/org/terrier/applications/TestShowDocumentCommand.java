package org.terrier.applications;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestShowDocumentCommand extends ApplicationSetupBasedTest {

	@Test public void testNoBlocks() throws Exception
	{
		String mydoc = "hello there";
		ApplicationSetup.setProperty("termpipelines", "");
		Index index = IndexTestUtils.makeIndex(new String[]{"doc1"}, new String[]{mydoc});
		ApplicationSetup.TERRIER_INDEX_PATH = ((IndexOnDisk)index).getPath();
		ApplicationSetup.TERRIER_INDEX_PREFIX = ((IndexOnDisk)index).getPrefix();
		new ShowDocumentCommand().run(new String[]{"--docid", "0"});
	}
	
	@Test public void testBlocks() throws Exception
	{
		String mydoc = "hello there";
		ApplicationSetup.setProperty("termpipelines", "");
		Index index = IndexTestUtils.makeIndexBlocks(new String[]{"doc1"}, new String[]{mydoc});
		ApplicationSetup.TERRIER_INDEX_PATH = ((IndexOnDisk)index).getPath();
		ApplicationSetup.TERRIER_INDEX_PREFIX = ((IndexOnDisk)index).getPrefix();
		new ShowDocumentCommand().run(new String[]{"--docid", "0"});
	}
}
