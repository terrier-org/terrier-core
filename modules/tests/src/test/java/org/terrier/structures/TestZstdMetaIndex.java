package org.terrier.structures;
import org.terrier.structures.indexing.*;

public class TestZstdMetaIndex extends BaseTestCompressedMetaIndex {

    public TestZstdMetaIndex() {
        metaBuilderClass = ZstdMetaIndexBuilder.class;
    }

}