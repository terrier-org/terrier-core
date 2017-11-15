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

}
