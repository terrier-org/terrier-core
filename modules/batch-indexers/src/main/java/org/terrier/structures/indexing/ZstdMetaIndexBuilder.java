package org.terrier.structures.indexing;
import org.terrier.structures.ZstdCompressedMetaIndex;
import com.github.luben.zstd.ZstdCompressCtx;
import org.terrier.structures.IndexOnDisk;
import java.io.IOException;
/**
 * Writes all metadata using Zstandard compression.
 * @since 5.5
 */
public class ZstdMetaIndexBuilder extends BaseMetaIndexBuilder {

    ZstdCompressCtx compressor = new ZstdCompressCtx();

    public ZstdMetaIndexBuilder(IndexOnDisk _index, String[] _keyNames, int[] _valueLens, String[] _reverseKeys)
	{
		this(_index, "meta", _keyNames, _valueLens, _reverseKeys);
	}

    public ZstdMetaIndexBuilder(IndexOnDisk _index, String _structureName, String[] _keyNames, int[] _valueLens, String[] _reverseKeys)
	{
        super(_index, _structureName, _keyNames, _valueLens, _reverseKeys);
        this.structureClass = ZstdCompressedMetaIndex.class;
        this.structureInputStreamClass = ZstdCompressedMetaIndex.InputStream.class;
        this.compressor.setChecksum(false);
    }

    protected int writeData(byte[] data) throws IOException {
        byte[] compressed = compressor.compress(data);
        dataOutput.write(compressed);        
        return compressed.length;
    }

}