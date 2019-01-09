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
 * The Original Code is ConcurrentMetaIndex.java.
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
import org.terrier.structures.MetaIndex;

@ConcurrentReadable
class ConcurrentMetaIndex implements MetaIndex {

	MetaIndex parent;
	
	ConcurrentMetaIndex(MetaIndex _parent)
	{
		this.parent = _parent;
	}
	
	public void close() throws IOException {
		parent.close();
	}

	public String getItem(String Key, int docid) throws IOException {
		synchronized (parent) {
			return parent.getItem(Key, docid);
		}
	}

	public String[] getAllItems(int docid) throws IOException {
		synchronized (parent) {
			return parent.getAllItems(docid);
		}
	}

	public String[] getItems(String Key, int[] docids) throws IOException {
		synchronized (parent) {
			return parent.getItems(Key, docids);
		}
	}

	public String[] getItems(String[] keys, int docid) throws IOException {
		synchronized (parent) {
			return parent.getItems(keys, docid);
		}
	}

	public String[][] getItems(String[] Key, int[] docids) throws IOException {
		synchronized (parent) {
			return parent.getItems(Key, docids);
		}
	}

	public int getDocument(String key, String value) throws IOException {
		synchronized (parent) {
			return getDocument(key, value);
		}
	}

	public String[] getKeys() {
		return parent.getKeys();
	}

	@Override
	public String[] getReverseKeys() {
		return parent.getKeys();
	}

}
