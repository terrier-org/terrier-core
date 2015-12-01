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
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.MetaIndex;
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
 * <li>metaindex.crop</tt> - should the content to store be cropped down to the length specified in indexer.meta.forward.keylens?</li>
 * </ul>
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class MemoryMetaIndex implements MetaIndex,Serializable {

	private static final long serialVersionUID = 8260494137553522514L;

	private static final Logger logger = LoggerFactory
			.getLogger(MemoryMetaIndex.class);

	public static String[] reverse = {};

	/*
	 * Meta-data index structures.
	 */
	private List<String[]> metadata;
	private TObjectIntHashMap<String> key2meta;
	private int[] keylengths;

	/*
	 * Keys and key lengths.
	 */
	public String[] keys = ArrayUtils
			.parseCommaDelimitedString(ApplicationSetup.getProperty(
					"indexer.meta.forward.keys", ""));
	public String[] keylens = ArrayUtils
			.parseCommaDelimitedString(ApplicationSetup.getProperty(
					"indexer.meta.forward.keylens", ""));

	/*
	 * Crop keys?
	 */
	private final static boolean crop = Boolean.parseBoolean(ApplicationSetup
			.getProperty("metaindex.crop", "false"));

	/**
	 * Constructor.
	 */
	public MemoryMetaIndex(String[] metaKeys, int[] metaLengths) {
		keys = metaKeys;
		keylengths = metaLengths;
		if (keys.length != keylengths.length) {
			logger.error("Meta keys and keylens mismatch.");
			System.exit(-1);
		}
		metadata = new ArrayList<String[]>();
		key2meta = new TObjectIntHashMap<String>();
		int i = 0;
		for (String key : keys)
			key2meta.put(key, i++);
	}
	
	/**
	 * Constructor.
	 */
	public MemoryMetaIndex() {
		if (keys.length != keylens.length) {
			logger.error("Meta keys and keylens mismatch.");
			System.exit(-1);
		}
		metadata = new ArrayList<String[]>();
		key2meta = new TObjectIntHashMap<String>();
		keylengths = new int[keylens.length];
		int i = 0;
		for (String key : keys)
			key2meta.put(key, i++);
		i = 0;
		for (String length : keylens)
			keylengths[i++] = Integer.parseInt(length);
	}

	/** {@inheritDoc} */
	public String[] getKeys() {
		return keys;
	}

	/** {@inheritDoc} */
	public String getItem(String key, int docid) throws IOException {
		return metadata.get(docid)[key2meta.get(key)];
	}

	/** {@inheritDoc} */
	public String[] getAllItems(int docid) throws IOException {
		return metadata.get(docid);
	}

	/** {@inheritDoc} */
	public String[] getItems(String key, int[] docids) throws IOException {
		String[] data = new String[docids.length];
		int index = key2meta.get(key);
		for (int i = 0; i < docids.length; i++)
			data[i] = metadata.get(docids[i])[index];
		return data;
	}

	/** {@inheritDoc} */
	public String[] getItems(String[] keys, int docid) throws IOException {
		String[] data = new String[keys.length];
		for (int i = 0; i < keys.length; i++)
			data[i] = metadata.get(docid)[key2meta.get(keys[i])];
		return data;
	}

	/** {@inheritDoc} */
	public String[][] getItems(String[] keys, int[] docids) throws IOException {
		String[][] data = new String[docids.length][keys.length];
		for (int i = 0; i < docids.length; i++)
			data[i] = getItems(keys, docids[i]);
		return data;
	}

	/**
	 * Write meta-data for document.
	 */
	public void writeDocumentEntry(Map<String, String> data) throws Exception {
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
					throw new Exception("Metaitem " + key + " keylength ("
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
	public void writeDocumentEntry(String[] data) {
		metadata.add(data);
	}
	
	/** Not implemented. */
	public int getDocument(String key, String value) throws IOException {
		return -1;
	}

	/**
	 * Delete contents of metadata index (but keep keys).
	 */
	public void close() throws IOException {
		metadata.clear();
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
		}
	}
	
	public int[] getKeyLengths() {
		int[] kl = new int[keylens.length];
		int i = 0;
		for (String k : keylens) {
			kl[i] = Integer.parseInt(k);
			i++;
		}
		return kl;
	}

}
