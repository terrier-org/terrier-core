package org.terrier.structures;
import org.terrier.structures.indexing.*;

public class TestCompressingMetaIndex extends BaseTestCompressedMetaIndex {

    public TestCompressingMetaIndex() {
        metaBuilderClass = CompressingMetaIndexBuilder.class;
    }

}