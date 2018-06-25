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
 * The Original Code is ConcurrentLexicon.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures.concurrent;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.terrier.structures.ConcurrentReadable;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;

@ConcurrentReadable
class ConcurrentLexicon extends Lexicon<String>  
{
	Lexicon<String> parent;
	
	public ConcurrentLexicon(Lexicon<String> _parent){
		this.parent = _parent;
	}	  
	
	public void close() throws IOException {
		parent.close();
	}

	public Iterator<Entry<String, LexiconEntry>> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int numberOfEntries() {
		return parent.numberOfEntries();
	}

	@Override
	public LexiconEntry getLexiconEntry(String term) {
		synchronized (parent) {
			return parent.getLexiconEntry(term);
		}
	}

	@Override
	public Entry<String, LexiconEntry> getLexiconEntry(int termid) {
		synchronized (parent) {
			return parent.getLexiconEntry(termid);
		}
	}

	@Override
	public Entry<String, LexiconEntry> getIthLexiconEntry(int index) {
		synchronized (parent) {
			return parent.getIthLexiconEntry(index);
		}
	}

	@Override
	public Iterator<Entry<String, LexiconEntry>> getLexiconEntryRange(
			String from, String to) {
		throw new UnsupportedOperationException();
	}
	
}
