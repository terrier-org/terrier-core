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
 * The Original Code is ConcurrentDocumentIndex.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures.concurrent;

import java.io.IOException;

import org.terrier.structures.ConcurrentReadable;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.FieldDocumentIndex;

@ConcurrentReadable
class ConcurrentDocumentIndex implements DocumentIndex {

	DocumentIndex parent;
	ConcurrentDocumentIndex(DocumentIndex _parent) {
		this.parent = _parent;
	}
	
	public DocumentIndexEntry getDocumentEntry(int docid) throws IOException {
		synchronized (parent) {
			return parent.getDocumentEntry(docid);
		}
	}

	public int getDocumentLength(int docid) throws IOException {
		synchronized (parent) {
			return parent.getDocumentLength(docid);
		}		
	}

	public int getNumberOfDocuments() {
		return parent.getNumberOfDocuments();
	}
	
	static class ConcurrentFieldDocumentIndex extends ConcurrentDocumentIndex implements FieldDocumentIndex
	{
		FieldDocumentIndex fparent;
		ConcurrentFieldDocumentIndex(FieldDocumentIndex _fdoi) {
			super(_fdoi);
			fparent = _fdoi;
		}
		
		public int[] getFieldLengths(int docid) throws IOException {
			synchronized (super.parent) {
				return fparent.getFieldLengths(docid);
			}
		}		
	}

}
