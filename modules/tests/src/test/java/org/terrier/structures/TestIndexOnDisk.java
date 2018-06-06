package org.terrier.structures;

import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestIndexOnDisk extends ApplicationSetupBasedTest {

	@Test(expected=IllegalArgumentException.class) public void dirNotExists() throws Exception {
		Index newIndex = IndexOnDisk.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH + "/tmp/", "data");
		newIndex.setIndexProperty("hello", "there");
		newIndex.flush();
		newIndex.close();
	}
	
}
