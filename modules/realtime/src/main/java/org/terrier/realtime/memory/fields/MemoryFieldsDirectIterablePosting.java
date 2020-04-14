package org.terrier.realtime.memory.fields;

import org.terrier.realtime.memory.MemoryDirectIterablePosting;
import org.terrier.structures.postings.FieldPostingImpl;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.structures.postings.FieldPosting;
import java.util.List;
import gnu.trove.TIntArrayList;


public class MemoryFieldsDirectIterablePosting extends MemoryDirectIterablePosting implements FieldPosting {

    List<int[]> pl_fields;
    public MemoryFieldsDirectIterablePosting(TIntArrayList pl_termids,
            TIntArrayList pl_freq, List<int[]> _pl_fields) 
    {
        super(pl_termids, pl_freq);
        this.pl_fields = _pl_fields;
    }

    public WritablePosting asWritablePosting() {
		FieldPostingImpl bp = new FieldPostingImpl();
		bp.setId(getId());
		bp.setTf(getFrequency());
		return bp;
    }
    
    /** Returns the frequencies of the term in each field of the document */
	public int[] getFieldFrequencies() {
        return pl_fields.get(super.index);
    }
	
	/** Returns the lengths of the each fields in the current document */
    public int[] getFieldLengths()
    {
        throw new UnsupportedOperationException();
    }
	
	public void setFieldLengths(int[] newLengths){
        throw new UnsupportedOperationException();
    }
}