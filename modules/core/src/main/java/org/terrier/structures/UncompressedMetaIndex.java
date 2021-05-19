package org.terrier.structures;
import java.io.IOException;
public class UncompressedMetaIndex extends CompressingMetaIndex {

    public static class InputStream extends CompressingMetaIndex.InputStream {

        public InputStream(IndexOnDisk _index, String structureName) throws IOException
		{
            super(_index, structureName);
        }

        public InputStream(IndexOnDisk _index, String _structureName, int _startingId, int _endId) throws IOException
		{
            super(_index, _structureName, _startingId, _endId);
        }

        protected byte[] decode(byte[] input) throws IOException {
            return input;
        }
    }

    public UncompressedMetaIndex(IndexOnDisk index, String structureName)
        throws IOException
    {
        super(index, structureName);
    }


    protected byte[] decode(byte[] input) throws IOException {
		return input;
	}
}