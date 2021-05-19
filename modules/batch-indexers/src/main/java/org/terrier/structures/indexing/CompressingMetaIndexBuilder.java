package org.terrier.structures.indexing;
import org.terrier.structures.CompressingMetaIndex;
import java.util.zip.Deflater;
import org.terrier.structures.IndexOnDisk;
import java.io.IOException;

/**
 *  Creates a metaindex structure that compresses all values using Deflate. 
 */
public class CompressingMetaIndexBuilder extends BaseMetaIndexBuilder {

    protected Deflater zip = new Deflater();
    protected final int ZIP_COMPRESSION_LEVEL = 5;

    public CompressingMetaIndexBuilder(IndexOnDisk _index, String[] _keyNames, int[] _valueLens, String[] _reverseKeys)
	{
		this(_index, "meta", _keyNames, _valueLens, _reverseKeys);
	}

    public CompressingMetaIndexBuilder(IndexOnDisk _index, String _structureName, String[] _keyNames, int[] _valueLens, String[] _reverseKeys)
	{
        super(_index, _structureName, _keyNames, _valueLens, _reverseKeys);
        this.zip.setLevel(ZIP_COMPRESSION_LEVEL);
        this.structureClass = CompressingMetaIndex.class;
        this.structureInputStreamClass = CompressingMetaIndex.InputStream.class;
    }

    protected int writeData(byte[] data) throws IOException {
        try{
            zip.reset();
            zip.setInput(data);
            zip.finish();
            int compressedEntrySize = 0;
            while(! zip.finished())
            {
                final int numOfCompressedBytes = zip.deflate(compressedBuffer);
                dataOutput.write(compressedBuffer, 0, numOfCompressedBytes);
                compressedEntrySize += numOfCompressedBytes;
            }
            return compressedEntrySize;
        } catch (Exception e) {
            throw new IOException("Could not compress metadata", e);
        }
    }

    public void close() throws IOException
	{
        index.setIndexProperty("index."+structureName+".compression-level", ""+ZIP_COMPRESSION_LEVEL);
        super.close();
    }
    
}