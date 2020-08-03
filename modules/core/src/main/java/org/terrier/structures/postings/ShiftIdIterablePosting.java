package org.terrier.structures.postings;
import java.io.IOException;
class ShiftIdIterablePosting extends IterablePostingImpl {

    static class FShiftIdIterablePosting extends ShiftIdIterablePosting implements FieldPosting {

        FieldPosting fp;
        FShiftIdIterablePosting(IterablePosting _parent, int ids_delta)
        {
            super(_parent, ids_delta);
            this.fp = (FieldPosting) _parent;
        }

        @Override
        public int[] getFieldFrequencies() {
           return fp.getFieldFrequencies();
        }

        @Override
        public int[] getFieldLengths() {
            return fp.getFieldLengths();
        }
    }

    static class BFShiftIdIterablePosting extends ShiftIdIterablePosting implements FieldPosting, BlockPosting {

        FieldPosting fp;
        BlockPosting bp;
        BFShiftIdIterablePosting(IterablePosting _parent, int ids_delta)
        {
            super(_parent, ids_delta);
            this.fp = (FieldPosting) _parent;
            this.bp = (BlockPosting) _parent;
        }

        @Override
        public int[] getPositions() {
            return bp.getPositions();
        }

        @Override
        public int[] getFieldFrequencies() {
           return fp.getFieldFrequencies();
        }

        @Override
        public int[] getFieldLengths() {
            return fp.getFieldLengths();
        }
    }

    static class BShiftIdIterablePosting extends ShiftIdIterablePosting implements BlockPosting {

        BlockPosting bp;
        BShiftIdIterablePosting(IterablePosting _parent, int ids_delta)
        {
            super(_parent, ids_delta);
            this.bp = (BlockPosting) _parent;
        }

        @Override
        public int[] getPositions() {
            return bp.getPositions();
        }
    }

    public static IterablePosting of(IterablePosting _parent, int ids_delta, boolean blocks, boolean fields) {
        if (blocks && fields)
            return new BFShiftIdIterablePosting(_parent, ids_delta);
        if (blocks)
            return new BShiftIdIterablePosting(_parent, ids_delta);
        if (fields)
            return new FShiftIdIterablePosting(_parent, ids_delta);
        return new ShiftIdIterablePosting(_parent, ids_delta);
    }
    
    IterablePosting parent;
    int delta;
    int id;

    ShiftIdIterablePosting(IterablePosting _parent, int ids_delta)
    {
        this.parent = _parent;
        this.delta = ids_delta;
    }

    @Override
    public int next() throws IOException {
        int rtr = parent.next();
        if (rtr != EOL)
            rtr += delta;
        id = rtr;
        return rtr;
    } 

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getFrequency() {
        return parent.getFrequency();
    }

    @Override
    public int getDocumentLength() {
        return parent.getDocumentLength();
    }

    @Override
    public WritablePosting asWritablePosting() {
        WritablePosting wp = parent.asWritablePosting();
        wp.setId(this.getId());
        return wp;
    }

    @Override
    public void close() throws IOException {
        parent.close();
    }

    @Override
    public boolean endOfPostings() {
        return parent.endOfPostings();
    }
}