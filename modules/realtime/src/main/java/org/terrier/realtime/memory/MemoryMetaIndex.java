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
 * The Original Code is MemoryMetaIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory;

import gnu.trove.TObjectIntHashMap;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.terrier.structures.MetaIndex;
import org.terrier.structures.indexing.MetaIndexBuilder;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;

/**
 * An in-memory version of a Meta-data index. It stores additional information
 * about each document, e.g. the docno or title. Access to the memory versions 
 * of the meta index are faster than the on-disk versions, but can use up large
 * amounts of RAM to store.
 * 
 * <p><b>Properties</b></p>
 * <ul><li>indexer.meta.forward.keys</tt> - key names to store in the meta index</li>
 * <li>indexer.meta.forward.keylens</tt> - max key lengths for keys to store (this is ignored unless metaindex.crop is set)</li>
 * <li>metaindex.compressed.crop.long</tt> - should the content to store be cropped down to the length specified in indexer.meta.forward.keylens?</li>
 * </ul>
 * @author Richard McCreadie, Stuart Mackie, Craig Macdonald
 * @since 4.0
 */
public class MemoryMetaIndex extends MetaIndexBuilder implements MetaIndex,Serializable {

	private static final long serialVersionUID = 8260494137553522514L;

	/*
	 * Meta-data index structures.
	 */
	private List<String[]> metadata;
	private TObjectIntHashMap<String> key2meta;
	private int[] keylengths;
	private boolean[] isReverse;

	private Map<String,TObjectIntHashMap<String>> key2value2id;
	/*
	 * Keys and key lengths.
	 */
	private String[] keys ;
	private String[] revkeys;
	/*
	 * Crop keys?
	 */
	private final static boolean crop = Boolean.parseBoolean(ApplicationSetup.getProperty("metaindex.compressed.crop.long", "false"));

	public MemoryMetaIndex()
	{
		this(
			ArrayUtils.parseCommaDelimitedString(ApplicationSetup.getProperty("indexer.meta.forward.keys", "")),
			ArrayUtils.parseCommaDelimitedInts(ApplicationSetup.getProperty("indexer.meta.forward.keylens", "")), 
			ArrayUtils.parseCommaDelimitedString(ApplicationSetup.getProperty("indexer.meta.reverse.keys", ""))
			);
	}
	
	public MemoryMetaIndex(String[] metaKeys, int[] metaLengths) {
		this(metaKeys, metaLengths, new String[0]);
	}
	
	public MemoryMetaIndex(String[] metaKeys, int[] metaLengths, String[] revKeys) {
		this.keys = metaKeys;
		this.keylengths = metaLengths;
		this.revkeys = revKeys;
		if (keys.length != keylengths.length) {
			throw new IllegalArgumentException("Meta keys and keylens mismatch.");
		}
		
		metadata = new ArrayList<String[]>();
		key2meta = new TObjectIntHashMap<String>();
		int i = 0;
		for (String key : keys)
			key2meta.put(key, i++);
		
		key2value2id = new HashMap<String,TObjectIntHashMap<String>>(revKeys.length);
		for (String revkey : this.revkeys)
			key2value2id.put(revkey, new TObjectIntHashMap<String>());
		isReverse = new boolean[keys.length];
		for(i=0;i<keys.length;i++){
			isReverse[i] = key2value2id.containsKey(keys[i]);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String[] getKeys() {
		return keys;
	}

	/** {@inheritDoc} */
	@Override
	public String getItem(String key, int docid) throws IOException {
		return metadata.get(docid)[key2meta.get(key)];
	}

	/** {@inheritDoc} */
	@Override
	public String[] getAllItems(int docid) throws IOException {
		return metadata.get(docid);
	}

	/** {@inheritDoc} */
	@Override
	public String[] getItems(String key, int[] docids) throws IOException {
		String[] data = new String[docids.length];
		int index = key2meta.get(key);
		for (int i = 0; i < docids.length; i++)
			data[i] = metadata.get(docids[i])[index];
		return data;
	}

	/** {@inheritDoc} */
	@Override
	public String[] getItems(String[] keys, int docid) throws IOException {
		String[] data = new String[keys.length];
		for (int i = 0; i < keys.length; i++)
			data[i] = metadata.get(docid)[key2meta.get(keys[i])];
		return data;
	}

	/** {@inheritDoc} */
	@Override
	public String[][] getItems(String[] keys, int[] docids) throws IOException {
		String[][] data = new String[docids.length][keys.length];
		for (int i = 0; i < docids.length; i++)
			data[i] = getItems(keys, docids[i]);
		return data;
	}

	/**
	 * Write meta-data for document.
	 */
	@Override
	public void writeDocumentEntry(Map<String, String> data) {
		if (data == null)
			return;

		String[] metadata = new String[keys.length];
		
		int i = 0;
		for (String key : keys) {
			String metaitem = data.get(key);
			if (metaitem == null)
				metaitem = "";
			if (metaitem.length() > keylengths[i++])
				if (!crop)
					throw new RuntimeException("Metaitem " + key + " keylength ("
							+ metaitem.length() + ") exceeded limit ("
							+ keylengths[i - 1] + ").");
				else
					metaitem = metaitem.substring(0, keylengths[i - 1]);
			metadata[key2meta.get(key)] = metaitem;
		}
		writeDocumentEntry(metadata);
	}

	/*
	 * Write meta-data.
	 */
	/**
	 * This method has been made public for creating 
	 * 
	 * @param data
	 */
	@Override
	public void writeDocumentEntry(String[] data) {
		//forward metadata
		metadata.add(data);
		
		//reverse metadata
		if (revkeys.length == 0)
			return;
		for(int i=0;i<data.length;i++)
		{
			if (! isReverse[i])
				continue;
			int docid = metadata.size() -1;
			key2value2id.get(this.keys[i]).put(data[i], docid+1);
		}
	}
	
	@Override
	public int getDocument(String key, String value) throws IOException {
		TObjectIntHashMap<String> map = key2value2id.get(key);
		if (map == null)
			return -1;
		return map.get(value) -1;
	}
	
	@Override
	public String[] getReverseKeys()
	{
		return key2value2id.keySet().toArray(new String[key2value2id.size()]);
	}

	/**
	 * Delete contents of metadata index (but keep the configured keys).
	 */
	@Override
	public void close() throws IOException {
		metadata.clear();
		if (key2meta!=null) key2meta.clear();
		for (TObjectIntHashMap<String> map : key2value2id.values())
			map.clear();
	}

	/**
	 * Return iterator over meta-data index.
	 */
	public Iterator<String[]> iterator() {
		return new MetaIterator();
	}

	/**
	 * Meta-data index iterator.
	 */
	private class MetaIterator implements Iterator<String[]> {
		Iterator<String[]> iter = metadata.iterator();

		public boolean hasNext() {
			return iter.hasNext();
		}

		public String[] next() {
			return iter.next();
		}

		public void remove() {
			throw new UnsupportedOperationException("Not supported");
		}
	}
	
	public int[] getKeyLengths() {
		return this.keylengths;
	}

}
