package org.terrier.structures;
import java.io.IOException;
import com.github.luben.zstd.ZstdDecompressCtx;

/** MetaIndex implementation for when records are compressed using Zstandard 
 * @since 5.5
*/
public class ZStdCompressedMetaIndex extends CompressingMetaIndex {

    public static class InputStream extends CompressingMetaIndex.InputStream {
        ZstdDecompressCtx z = new ZstdDecompressCtx();

        public InputStream(IndexOnDisk _index, String structureName) throws IOException
		{
            super(_index, structureName);
        }

        public InputStream(IndexOnDisk _index, String _structureName, int _startingId, int _endId) throws IOException
		{
            super(_index, _structureName, _startingId, _endId);
        }

        protected byte[] decode(byte[] input) throws IOException {
            byte[] rtr = new byte[recordLength];
            z.decompress(rtr, input);
            return rtr;
        }
    }

    final ThreadLocal<ZstdDecompressCtx> zstDecompressCache = new ThreadLocal<ZstdDecompressCtx>() {
        protected final synchronized ZstdDecompressCtx initialValue() {
			return new ZstdDecompressCtx();
		}
    };

    public ZStdCompressedMetaIndex(IndexOnDisk index, String structureName)
		throws IOException
	{
        super(index, structureName);
    }

    protected byte[] decode(byte[] input) throws IOException {
        ZstdDecompressCtx z = zstDecompressCache.get();
        byte[] rtr = new byte[recordLength];
		z.decompress(rtr, input);
        return rtr;
	}
}