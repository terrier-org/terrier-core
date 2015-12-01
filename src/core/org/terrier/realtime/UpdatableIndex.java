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
 * The Original Code is UpdatableIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime;

import java.util.Map;

import org.terrier.indexing.Document;
import org.terrier.structures.indexing.DocumentPostingList;

/**
 * Interface describing an index that can be updated
 * @author Richard McCreadie
 * @since 4.0
 */
public interface UpdatableIndex {
	
	/**
	 * Add a new document to the index.
	 */
	public void indexDocument(Document doc) throws Exception;
	
	/**
	 * Add a new pre-parsed document to the index.
	 */
	public void indexDocument(Map<String, String> docProperties,
			DocumentPostingList docContents) throws Exception;

	
	/** Removes a document from the index. Returns true if successful.
	 * No known operable implementations at this time. */
	public boolean removeDocument(int docid);
	
	/** Adds specified content contents to the named document id.
	 *  Returns true if supported & successful. */
	public boolean addToDocument(int docid, Document doc) throws Exception;
	
	/** Adds relevant terms to the named document id.
	 * Returns true if supported & successful. */
	public boolean addToDocument(int docid, DocumentPostingList docContents) throws Exception;
	
	
}
