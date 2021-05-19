package org.terrier.structures.indexing;
import java.io.IOException;
import org.terrier.structures.LZ4CompressedMetaIndex;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4Compressor;
import org.terrier.structures.IndexOnDisk;

/**
 * Writes all metadata using Zstandard compression.
 * @since 5.5
 */
public class LZ4MetaIndexBuilder extends BaseMetaIndexBuilder {

    LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();
    byte[] compressedBuffer;
    
    public LZ4MetaIndexBuilder(IndexOnDisk _index, String[] _keyNames, int[] _valueLens, String[] _reverseKeys)
	{
		this(_index, "meta", _keyNames, _valueLens, _reverseKeys);
	}

    public LZ4MetaIndexBuilder(IndexOnDisk _index, String _structureName, String[] _keyNames, int[] _valueLens, String[] _reverseKeys)
	{
        super(_index, _structureName, _keyNames, _valueLens, _reverseKeys);
        this.structureClass = LZ4CompressedMetaIndex.class;
        this.structureInputStreamClass = LZ4CompressedMetaIndex.InputStream.class;
        this.compressedBuffer = new byte[this.compressor.maxCompressedLength(entryLengthBytes)];
    }

    protected int writeData(byte[] data) throws IOException {
        int numBytes = compressor.compress(data, compressedBuffer);
        dataOutput.write(compressedBuffer, 0, numBytes);        
        return numBytes;
    }

}