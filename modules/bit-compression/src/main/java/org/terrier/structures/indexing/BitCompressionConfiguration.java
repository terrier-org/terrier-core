package org.terrier.structures.indexing;
import org.terrier.structures.indexing.CompressionFactory.SpecificCompressionConfiguration;
import org.terrier.structures.*;
import org.terrier.structures.bit.*;
import org.terrier.structures.postings.bit.*;
import org.terrier.compression.bit.BitIn;

public class BitCompressionConfiguration extends SpecificCompressionConfiguration
{
    public BitCompressionConfiguration(String structureName, String[] fieldNames, int hasBlocks, int maxBlocks)
    {
        super(
            structureName, fieldNames, hasBlocks, maxBlocks,
            fieldNames.length > 0 ? hasBlocks > 0 ? BlockFieldDirectInvertedOutputStream.class : FieldDirectInvertedOutputStream.class : hasBlocks > 0 ? BlockDirectInvertedOutputStream.class : DirectInvertedOutputStream.class,
            fieldNames.length > 0 ? hasBlocks > 0 ? BlockFieldIterablePosting.class : FieldIterablePosting.class : hasBlocks > 0 ? BlockIterablePosting.class : BasicIterablePosting.class,
            BitPostingIndex.class, 
            BitPostingIndexInputStream.class,
            new SimpleBitIndexPointer.Factory(),
            fieldNames.length > 0 ? new FieldLexiconEntry.Factory(fieldNames.length) : new BasicLexiconEntry.Factory(),
            structureName.equals("inverted")
                //single pass indexer
                ? fieldNames.length > 0 ? new FieldDocumentIndexEntry.Factory(fieldNames.length) : new SimpleDocumentIndexEntry.Factory()
                //classical multipass indexer
                : fieldNames.length > 0 ? new FieldDocumentIndexEntry.Factory(fieldNames.length) : new BasicDocumentIndexEntry.Factory(),
            BitIn.USUAL_EXTENSION
        );
    }
}