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
