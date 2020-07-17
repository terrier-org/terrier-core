package org.terrier.structures.postings;
import java.io.IOException;
/** Chains together several IterablePostings. The order of the IterablePosting objects is assumed to be valid. */
class ChainIterablePosting  extends IterablePostingImpl {

    static class FieldChainIterablePosting extends ChainIterablePosting implements FieldPosting {
        FieldPosting[] fps;
        FieldChainIterablePosting(IterablePosting[] _ips) {
            super(_ips);
            fps = new FieldPosting[_ips.length];
            for(int i=0;i<_ips.length;i++)
                fps[i] = (FieldPosting)_ips[i];
        }

        @Override
        public int[] getFieldFrequencies() {
            return fps[index].getFieldFrequencies();
        }
        
        @Override
        public int[] getFieldLengths() {
            return fps[index].getFieldLengths();
        }

        @Override
        public WritablePosting asWritablePosting()
        {
            return new FieldPostingImpl(getId(), getFrequency(), getFieldFrequencies());
        }
    }

    static class BlockFieldChainIterablePosting extends ChainIterablePosting implements FieldPosting, BlockPosting {
        FieldPosting[] fps;
        BlockPosting[] bps;
        BlockFieldChainIterablePosting(IterablePosting[] _ips) {
            super(_ips);
            fps = new FieldPosting[_ips.length];
            bps = new BlockPosting[_ips.length];
            for(int i=0;i<_ips.length;i++) {
                fps[i] = (FieldPosting)_ips[i];
                bps[i] = (BlockPosting)_ips[i];
            }
        }

        @Override
        public int[] getPositions() {
            return bps[index].getPositions();
        }

        @Override
        public int[] getFieldFrequencies() {
            return fps[index].getFieldFrequencies();
        }
        
        @Override
        public int[] getFieldLengths() {
            return fps[index].getFieldLengths();
        }        
    }

    static class BlockChainIterablePosting extends ChainIterablePosting implements BlockPosting {
        BlockPosting[] bps;

        BlockChainIterablePosting(IterablePosting[] _ips) {
            super(_ips);
            bps = new BlockPosting[_ips.length];
            for(int i=0;i<_ips.length;i++) {
                bps[i] = (BlockPosting)_ips[i];
            }
        }

        @Override
        public int[] getPositions() {
            return bps[index].getPositions();
        }
    }

    static IterablePosting of(IterablePosting[] _ips, boolean blocks, boolean fields)  {
        if (blocks && fields)
            return new BlockFieldChainIterablePosting(_ips);
    
        if (blocks)
            return new BlockChainIterablePosting(_ips);
        
        if (fields)
            return new FieldChainIterablePosting(_ips);
        
        return new ChainIterablePosting(_ips);
        
    }


    IterablePosting[] ips;
    int index = 0;
    int lastid = -1;

    public ChainIterablePosting(IterablePosting[] _ips) 
	{
        ips = _ips;
	}
	
	@Override
	public boolean endOfPostings() {
		return index == ips.length || ips[index].endOfPostings();
    }
    
    @Override
	public int next() throws IOException {
        int rtr = ips[index].next();
        if (rtr == EOL)
        {
            index++;
            if (index == ips.length)
                return EOL;
            rtr =  ips[index].next();

            //this class isnt designed for contituents that are empty
            assert rtr != EOL;
        }
        assert lastid < rtr : "Chained IterablePostings require docids to be ascending: last was " + lastid + " next was " + rtr + " index is now " + index;
        lastid = rtr;
        return rtr;
    }
    
    @Override
    public int getFrequency() {
        return ips[index].getFrequency();
    }

    @Override
    public int getDocumentLength() {
        return ips[index].getDocumentLength();
    }

    @Override
    public WritablePosting asWritablePosting()
	{
		return new BasicPostingImpl(getId(), getFrequency());
    }
    
    @Override
    public int getId() {
		return ips[index].getId();
	}

	@Override
	public void close() throws IOException {
        for (IterablePosting ip : ips)
            ip.close();
	}
	
    
}