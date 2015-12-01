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
 * The Original Code is NonIncrementalDocumentIndexEntry.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Dyaa Albakour <dyaa.albakour@glasgow.ac.uk>
 */

package org.terrier.structures;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.terrier.structures.seralization.FixedSizeWriteableFactory;

/**
 * This class is created to represent a <code>DocumentIndexEntry</code> that 
 * do not assume an incremental docids, but instead maintains the docid. 
 * 
 * 
 * @author Dyaa Albakour
 * @since 4.0
 */
public class NonIncrementalDocumentIndexEntry extends BasicDocumentIndexEntry {

	public int getDocid() {
		return docid;
	}


	int docid;
	
	
	/** 
	 * Returna a factory for creating document index entries
	 */
	public static class Factory implements FixedSizeWriteableFactory<DocumentIndexEntry>
	{
		/** 
		 * Returns 21? 
		 */
		public int getSize() {
			return 4 + 4 + 8 + + 4 + 1;
		}
		/** 
		 * Creates a document index entry
		 */
		public DocumentIndexEntry newInstance() {
			return new NonIncrementalDocumentIndexEntry();
		}
	}


	
	public NonIncrementalDocumentIndexEntry() {
		super();
	}


	public NonIncrementalDocumentIndexEntry(int docid) {
		super();
		this.docid = docid;
	}


	public NonIncrementalDocumentIndexEntry(DocumentIndexEntry in, int docid) {
		super(in);
		this.docid = docid;
	}


	public NonIncrementalDocumentIndexEntry(int length, byte fileId,
			long byteOffset, byte bitOffset, int numberOfTerms, int docid) {
		super(length, fileId, byteOffset, bitOffset, numberOfTerms);
		this.docid = docid;
	}


	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		super.readFields(in);
		docid=in.readInt();
	}


	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		super.write(out);
		out.writeInt(docid);
	}


	public void setDocid(int docid) {
		this.docid = docid;
	}
	
	
	
	
}
