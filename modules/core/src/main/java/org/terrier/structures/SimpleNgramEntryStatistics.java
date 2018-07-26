/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is SimpleNgramEntryStatistics.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
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
	int f;
	
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
		return f;
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
		f += e.getFrequency();
	}

	@Override
	public void subtract(EntryStatistics e) {
		nt -= e.getDocumentFrequency();
		f -= e.getFrequency();
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
		f = in.readInt();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(ws);
		out.writeInt(nt);
		out.writeInt(f);
	}

	@Override
	public int getMaxFrequencyInDocuments() {
		return maxtf;
	}
	
	@Override
	public void setMaxFrequencyInDocuments(int max) {
		maxtf = max;
	}
	
	public String toString() {
		return "F="+f+" Nt=" + this.getDocumentFrequency() + " ws=" + this.getWindowSize();
	}

	@Override
	public void setFrequency(int F) {
		this.f = F;
	}

	@Override
	public void setDocumentFrequency(int nt) {
		this.nt = nt;
	}

}
