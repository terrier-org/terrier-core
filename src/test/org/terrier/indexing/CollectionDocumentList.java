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
 * The Original Code is CollectionDocumentList.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.indexing;

import java.io.IOException;

public class CollectionDocumentList implements Collection {

	Document[] docs;
	int index = -1;
	String docidPropertyName;
	
	public CollectionDocumentList(Document[] _docs, String _docidPropertyName)
	{
		docs = _docs;
		docidPropertyName = _docidPropertyName;
	}
	
	public boolean endOfCollection() {
		return index >= docs.length -1;
	}

	public String getDocid() {
		return docs[index].getProperty(docidPropertyName);
	}

	public Document getDocument() {
		return docs[index];
	}

	public boolean nextDocument() {
		if (index < docs.length -1)
		{
			index++;
			return true;
		}
		return false;
	}

	public void reset() {
		index = -1;
	}

	public void close() throws IOException {}

}
