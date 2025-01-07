package org.terrier.structures.concurrent;
import org.terrier.structures.*;
import org.terrier.tests.BatchEndToEndTest;
import org.terrier.tests.BatchEndToEndTest.BatchEndToEndTestEventHooks;

import static org.junit.Assert.assertEquals;
public class TestShakParallelTRECQueryingMem extends TestShakParallelTRECQuerying {

    static class Hook extends BatchEndToEndTestEventHooks
    {
        public void finishedIndexing(BatchEndToEndTest test) throws Exception
		{
            IndexOnDisk iod = IndexOnDisk.createIndex();
            iod.setIndexProperty("index.inverted.data-source", "fileinmem");
		    iod.flush();
            iod.close();
        }

        public void checkIndex(BatchEndToEndTest test, Index index) throws Exception
        {
            IndexOnDisk iod = (IndexOnDisk) index;
            assertEquals("fileinmem", iod.getIndexProperty("index.inverted.data-source", null));
        }
    }

    public TestShakParallelTRECQueryingMem() {
        super();
        this.testHooks.add(new Hook());
    }
}
