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
