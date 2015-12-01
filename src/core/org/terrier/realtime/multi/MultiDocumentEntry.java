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
 * The Original Code is MultiDocumentEntry.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.multi;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.terrier.structures.BitFilePosition;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.Pointer;

/**
 * A document index entry that represents a document within a multi-index
 * It stores information about the shard that the document comes from.
 * 
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 *
 */
public class MultiDocumentEntry extends DocumentIndexEntry {

	DocumentIndexEntry innerDocumentIndexEntry;
	int documentIndexShardIndex;

	public MultiDocumentEntry(DocumentIndexEntry entry, int index) {
		innerDocumentIndexEntry = entry;
		documentIndexShardIndex = index;
	}
	
	@Override
	public void setBitIndexPointer(BitIndexPointer pointer) {
		innerDocumentIndexEntry.setBitIndexPointer(pointer);
		
	}

	@Override
	public void setOffset(BitFilePosition pos) {
		innerDocumentIndexEntry.setOffset(pos);
	}

	@Override
	public void readFields(DataInput arg0) throws IOException {
		innerDocumentIndexEntry.readFields(arg0);
		
	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		innerDocumentIndexEntry.write(arg0);
		
	}

	@Override
	public void setNumberOfEntries(int n) {
		innerDocumentIndexEntry.setNumberOfEntries(n);
		
	}

	@Override
	public String pointerToString() {
		return innerDocumentIndexEntry.pointerToString();
	}

	@Override
	public void setPointer(Pointer p) {
		innerDocumentIndexEntry.setPointer(p);
		
	}

	public int getDocumentIndexShardIndex() {
		return documentIndexShardIndex;
	}

	public void setDocumentIndexShardIndex(int documentIndexShardIndex) {
		this.documentIndexShardIndex = documentIndexShardIndex;
	}
	
	

}
