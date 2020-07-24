package org.terrier.structures.indexing;
import org.terrier.structures.indexing.CompressionFactory.SpecificCompressionConfiguration;
import org.terrier.structures.*;
import org.terrier.structures.bit.*;
import org.terrier.structures.postings.bit.*;
import org.terrier.compression.bit.BitIn;

public  class BitIdOnlyCompressionConfiguration extends SpecificCompressionConfiguration
{
    public BitIdOnlyCompressionConfiguration(String structureName, String[] fieldNames, int hasBlocks, int maxBlocks)
    {
        super(
            structureName, fieldNames, 0, 0,
            DirectInvertedDocidOnlyOuptutStream.class,
            BasicIterablePostingDocidOnly.class,
            BitPostingIndex.class, 
            BitPostingIndexInputStream.class,
            new SimpleBitIndexPointer.Factory(),
            fieldNames.length > 0 ? new FieldLexiconEntry.Factory(fieldNames.length) : new BasicLexiconEntry.Factory(),
            fieldNames.length > 0 ? new FieldDocumentIndexEntry.Factory(fieldNames.length) : new BasicDocumentIndexEntry.Factory(),
            BitIn.USUAL_EXTENSION
        );
    }
}