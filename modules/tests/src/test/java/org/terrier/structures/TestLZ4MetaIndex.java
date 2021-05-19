package org.terrier.structures;
import org.terrier.structures.indexing.*;

public class TestLZ4MetaIndex extends BaseTestCompressedMetaIndex {

    public TestLZ4MetaIndex() {
        metaBuilderClass = LZ4MetaIndexBuilder.class;
    }

}