package org.terrier.structures.indexing;
import org.terrier.structures.UncompressedMetaIndex;
import org.terrier.structures.IndexOnDisk;
import java.io.IOException;
/**
 * Writes all metadata as uncompressed
 * @since 5.5
 */
public class UncompressedMetaIndexBuilder extends BaseMetaIndexBuilder {

    public UncompressedMetaIndexBuilder(IndexOnDisk _index, String[] _keyNames, int[] _valueLens, String[] _reverseKeys)
	{
		this(_index, "meta", _keyNames, _valueLens, _reverseKeys);
	}

    public UncompressedMetaIndexBuilder(IndexOnDisk _index, String _structureName, String[] _keyNames, int[] _valueLens, String[] _reverseKeys)
	{
        super(_index, _structureName, _keyNames, _valueLens, _reverseKeys);
        this.structureClass = UncompressedMetaIndex.class;
        this.structureInputStreamClass = UncompressedMetaIndex.InputStream.class;
    }

    protected int writeData(byte[] data) throws IOException {
        dataOutput.write(data);
        return data.length;
    }
    
}