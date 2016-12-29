package org.terrier.structures;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class SimpleNgramEntryStatistics implements NgramEntryStatistics, Writable {
	
	private static final long serialVersionUID = 1L;
	
	int maxtf;
	int ws;
	int nt;
	
	public SimpleNgramEntryStatistics(EntryStatistics e){
		nt = e.getDocumentFrequency();
		maxtf = e.getMaxFrequencyInDocuments();
	}
	
	public SimpleNgramEntryStatistics(){}
	public SimpleNgramEntryStatistics(int ws) {
		this.ws = ws;
	}
	
	@Override
	public int getFrequency() {
		return 0;
	}

	@Override
	public int getDocumentFrequency() {
		return nt;
	}

	@Override
	public int getTermId() {
		return 0;
	}

	@Override
	public void add(EntryStatistics e) {
		nt += e.getDocumentFrequency();
	}

	@Override
	public void subtract(EntryStatistics e) {
		nt -= e.getDocumentFrequency();
	}

	@Override
	public EntryStatistics getWritableEntryStatistics() {
		return new SimpleNgramEntryStatistics(ws);
	}

	@Override
	public int getWindowSize() {
		return ws;
	}
	
	@Override
	public void setWindowSize(int ws) {
		this.ws = ws;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		ws = in.readInt();
		nt = in.readInt();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(ws);
		out.writeInt(nt);
	}

	@Override
	public int getMaxFrequencyInDocuments() {
		return maxtf;
	}
	
	@Override
	public void setMaxFrequencyInDocuments(int max) {
		maxtf = max;
	}

}
