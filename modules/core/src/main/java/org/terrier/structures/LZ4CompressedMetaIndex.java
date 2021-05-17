package org.terrier.structures;
import java.io.IOException;
import com.github.luben.zstd.ZstdDecompressCtx;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

/** MetaIndex implementation for when records are compressed using LZ4 
 * @since 5.5
*/
public class LZ4CompressedMetaIndex extends CompressingMetaIndex {

    static final LZ4FastDecompressor decompressor = LZ4Factory.fastestInstance().fastDecompressor();

    public static class InputStream extends CompressingMetaIndex.InputStream {
        byte[] buffer;

        public InputStream(IndexOnDisk _index, String structureName) throws IOException
		{
            super(_index, structureName);
            buffer = new byte[recordLength];
        }

        public InputStream(IndexOnDisk _index, String _structureName, int _startingId, int _endId) throws IOException
		{
            super(_index, _structureName, _startingId, _endId);
            buffer = new byte[recordLength];
        }

        protected byte[] decode(byte[] input) throws IOException {
            decompressor.decompress(input, buffer);
            return buffer;
        }
    }

    public LZ4CompressedMetaIndex(IndexOnDisk index, String structureName)
		throws IOException
	{
        super(index, structureName);
    }

    protected byte[] decode(byte[] input) throws IOException {
        byte[] buffer = new byte[recordLength];
        int read = decompressor.decompress(input, buffer);
        return buffer;
	}
}