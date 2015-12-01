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
 * The Original Code is MemoryMetaIndexMap.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;
import gnu.trove.TIntObjectIterator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.realtime.memory.MemoryMetaIndex;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;

/**
 * A memory meta index structure that supports fast look-ups via a map. It stores additional information
 * about each document, e.g. the docno or title. Access to the memory versions 
 * of the meta index are faster than the on-disk versions, but can use up large
 * amounts of RAM to store.
 * 
 * <p><b>Properties</b></p>
 * <ul><li>indexer.meta.forward.keys</tt> - key names to store in the meta index</li>
 * <li>indexer.meta.forward.keylens</tt> - max key lengths for keys to store (this is ignored unless metaindex.crop is set)</li>
 * <li>metaindex.crop</tt> - should the content to store be cropped down to the length specified in indexer.meta.forward.keylens?</li>
 * 
 * structure
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 *
 */
public class MemoryMetaIndexMap extends MemoryMetaIndex implements MetaIndexMap {

	private static final long serialVersionUID = 8860561423432492113L;

	private static final Logger logger = LoggerFactory
			.getLogger(MemoryMetaIndex.class);

	/*
	 * Meta-data index structures.
	 */
	private TIntObjectHashMap<String[]> metadata;
	private TObjectIntHashMap<String> key2meta;
	private int[] keylengths;

	/*
	 * Keys and key lengths.
	 */
	private static String[] _keys = ArrayUtils
			.parseCommaDelimitedString(ApplicationSetup.getProperty(
					"indexer.meta.forward.keys", ""));
	private static String[] _keylens = ArrayUtils
			.parseCommaDelimitedString(ApplicationSetup.getProperty(
					"indexer.meta.forward.keylens", ""));

	private String[] keys;
	private String[] keylens;
	
	/*
	 * Crop keys?
	 */
	private final static boolean crop = Boolean.parseBoolean(ApplicationSetup
			.getProperty("metaindex.crop", "false"));

	public MemoryMetaIndexMap(String[] keys, int[] keylengths) {
		if (keys.length != keylengths.length) {
			logger.error("Meta keys and keylens mismatch.");
			System.exit(-1);
		}
		this.keys = keys;
		metadata = new TIntObjectHashMap<String[]>();
		key2meta = new TObjectIntHashMap<String>();
		this.keylengths = keylengths;
		int i = 0;
		for (String key : keys)
			key2meta.put(key, i++);
	}
	
	/**
	 * Constructor.
	 */
	public MemoryMetaIndexMap() {
		keys = _keys;
		keylens = _keylens;
		if (keys.length != keylens.length) {
			logger.error("Meta keys and keylens mismatch.");
			System.exit(-1);
		}
		metadata = new TIntObjectHashMap<String[]>();
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
		String[] props = metadata.get(docid);
		if (props==null || props.length==0) {
			System.err.println("No metadata for docid "+docid);
			return "NOMETA-DATA";
		}
		return props[key2meta.get(key)];
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
	public void writeDocumentEntry( int docid, Map<String, String> data) throws Exception {
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
		writeDocumentEntry(docid, metadata);
	}

	/*
	 * Write meta-data.
	 */
	public  void writeDocumentEntry(int docid, String[] data) {
		metadata.put(docid, data);
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
		//return new MetaIterator();
		return new MetaIterator2();
	}

	/**
	 * Meta-data index iterator.
	 */
	@SuppressWarnings("unused")
	private class MetaIterator implements Iterator<String[]> {
		TIntObjectIterator<String[]> iter = metadata.iterator();

		public boolean hasNext() {
			return iter.hasNext();
		}

		public String[] next() {
			iter.advance();
			return iter.value();
		}

		public void remove() {
		}
	}
	
	private class MetaIterator2 implements Iterator<String[]> {
		//TIntObjectIterator<String[]> iter = metadata.iterator();

		int[] docids;
		int current=0;
		
		public MetaIterator2() {
			// TODO Auto-generated constructor stub
			docids = metadata.keys();
			Arrays.sort(docids);
			System.out.print(docids.length);
		}
		
		public boolean hasNext() {
			//return iter.hasNext();
			return current<docids.length;
		}

		public String[] next() {
			
			return metadata.get(docids[current++]);
		}

		public void remove() {
		}
	}
	

	
}
