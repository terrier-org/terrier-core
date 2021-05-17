package org.terrier.structures;
import org.terrier.structures.indexing.*;

public class TestUncompressingMetaIndex extends BaseTestCompressedMetaIndex {

    public TestUncompressingMetaIndex() {
        metaBuilderClass = UncompressedMetaIndexBuilder.class;
    }

}