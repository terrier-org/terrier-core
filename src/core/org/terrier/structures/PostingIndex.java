/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is PostingIndex.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;
import java.io.Closeable;
import java.io.IOException;

import org.terrier.structures.postings.IterablePosting;
/** An interface for accessing a posting list.
 * @param <POINTERTYPE> the type of pointer required to access this posting source 
 */
public interface PostingIndex<POINTERTYPE extends Pointer> extends Closeable {
	public static class DocidSpecificDocumentIndex implements FieldDocumentIndex {
		DocumentIndexEntry die;
		DocumentIndex di;
		
		public DocidSpecificDocumentIndex(DocumentIndex _di, DocumentIndexEntry _die)
		{
			di = _di;
			die = _die;
		}
		
		public DocumentIndexEntry getDocumentEntry(int docid) throws IOException {
			return die;
		}
	
		public int getDocumentLength(int docid) throws IOException {
			return die.getDocumentLength();
		}
	
		public int getNumberOfDocuments() {
			return di.getNumberOfDocuments();
		}
	
		public int[] getFieldLengths(int docid) throws IOException {
			return ((FieldDocumentIndexEntry)die).getFieldLengths();
		}
	}

	/** 
	 * Get the posting given a pointer
	 */
	IterablePosting getPostings(Pointer lEntry) throws IOException;
}
