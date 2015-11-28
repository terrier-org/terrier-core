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
 * The Original Code is MultiDoc.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.multi;

import java.io.IOException;

import org.terrier.structures.DocumentIndex;
import org.terrier.structures.DocumentIndexEntry;

/**
 * A Document index class that represents multiple document indices from
 * different shards. It is used within MultiIndex.
 * 
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class MultiDoc implements DocumentIndex {

	private DocumentIndex[] docs;
	private int[] offsets;

	/**
	 * constructor.
	 */
	public MultiDoc(DocumentIndex[] docs, int[] offsets) {
		this.docs = docs;
		this.offsets = offsets;
	}

	/** {@inheritDoc} */
	public DocumentIndexEntry getDocumentEntry(int docid) throws IOException {
		int offset = 0, i = 0;
		//System.err.println("MultiDoc: Getting docid="+docid);
		for (DocumentIndex doc : docs) {
			if (docid < (offsets[i] + offset)) {
				//System.err.println("Found docid="+docid+", is local docid "+(docid-offset)+" in shard "+i+" and has length "+d.getDocumentLength()+" ("+d.pointerToString()+")");
				return new MultiDocumentEntry(doc.getDocumentEntry(docid - offset),i);
			}
			offset += offsets[i++];
			
		}
		return null;
	}

	/** {@inheritDoc} */
	public int getDocumentLength(int docid) throws IOException {
		int offset = 0, i = 0;
		for (DocumentIndex doc : docs) {
			if (docid < (offsets[i] + offset))
				return doc.getDocumentLength(docid - offset);
			offset += offsets[i++];
		}
		return 0;
	}

	/** {@inheritDoc} */
	public int getNumberOfDocuments() {
		int numberofdocs = 0;
		for (int i = 0; i < docs.length; i++)
			numberofdocs += docs[i].getNumberOfDocuments();
		return numberofdocs;
	}

}
