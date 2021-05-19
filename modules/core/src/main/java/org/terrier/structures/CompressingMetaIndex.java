package org.terrier.structures;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/** Implementation of BaseCompressingMetaIndex that uses Zlib deflate algorithm to compress entries */
public class CompressingMetaIndex extends BaseCompressingMetaIndex {

	/** thread-local cache of Inflaters to be re-used for decompression */
	protected static final ThreadLocal<Inflater> inflaterCache = new ThreadLocal<Inflater>() 
	{
		protected final synchronized Inflater initialValue() {
			return new Inflater();
		}
	};

    public static class InputStream extends BaseCompressingMetaIndex.InputStream {

        protected Inflater inflater;
        public InputStream(IndexOnDisk _index, String _structureName, int _startingId, int _endId) throws IOException
		{
            super(_index, _structureName, _startingId, _endId);
            inflater = inflaterCache.get();
        }

        public InputStream(IndexOnDisk _index, String _structureName) throws IOException
        {
            super(_index, _structureName);
            inflater = inflaterCache.get();
        }

        byte[] decode(byte[] input) throws Exception {
			byte[] bOut = new byte[recordLength];
			inflater.reset();
			inflater.setInput(input);
			inflater.inflate(bOut);
			return bOut;
		}

    }

    public CompressingMetaIndex(IndexOnDisk index, String structureName)
        throws IOException
    {
        super(index, structureName);
    }

    protected byte[] decode(byte[] input) throws IOException {
		try{
			byte[] bOut = new byte[recordLength];
			Inflater unzip = inflaterCache.get();
			unzip.reset();		
			unzip.setInput(input);
			unzip.inflate(bOut);
			return bOut;
		} catch(DataFormatException dfe) {
			throw new IOException("Failed to inflate compressed meta data", dfe);
		}
	}

}