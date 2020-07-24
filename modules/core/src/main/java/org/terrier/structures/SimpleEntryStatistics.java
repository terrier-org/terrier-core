package org.terrier.structures;
import org.apache.hadoop.io.Writable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
public class SimpleEntryStatistics implements EntryStatistics, Writable {
    
    int F;
    int Nt;
    int id;
    int max_f;

    @Override
    public int getFrequency() {
        return F;
    }

    @Override
    public void setFrequency(int F) {
        this.F = F;
    }

    @Override
    public int getDocumentFrequency() {
       return Nt;
    }

    @Override
    public void setDocumentFrequency(int nt) {
       Nt = nt;
    }

    @Override
    public int getTermId() {
        return id;
    }

    @Override
    public int getMaxFrequencyInDocuments() {
        return max_f;
    }

    @Override
    public void setMaxFrequencyInDocuments(int max) {
        this.max_f = max;
    }

    @Override
    public EntryStatistics getWritableEntryStatistics() {
        return this;
    }

    public void readFields(DataInput in) throws IOException {
		id = in.readInt();
		F = in.readInt();
		Nt = in.readInt();
		max_f = in.readInt();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void write(DataOutput out) throws IOException {
		out.writeInt(id);
		out.writeInt(F);
		out.writeInt(Nt);
		out.writeInt(max_f);
		
	}
}