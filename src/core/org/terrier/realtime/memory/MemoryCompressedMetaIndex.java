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
 * The Original Code is MemoryCompressedMetaIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Dyaa Albakour <dyaa.albakour@glasgow.ac.uk>
 */

package org.terrier.realtime.memory;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import gnu.trove.TObjectIntHashMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;

/**
 * An in-memory version of a Meta-data index. It stores additional information
 * about each document, e.g. the docno or title. Access to the memory versions 
 * of the meta index are faster than the on-disk versions, but can use up large
 * amounts of RAM to store.
 * 
 * This is a Meta index that heavily compresses its content. This will make it
 * slower to access but is useful when storing large volumes of content.
 * 
 * It uses GZIP compression on each record in the meta index 
 * 
 * <p><b>Properties</b></p>
 * <ul><li>indexer.meta.forward.keys</tt> - key names to store in the meta index</li>
 * <li>indexer.meta.forward.keylens</tt> - max key lengths for keys to store (this is ignored unless metaindex.crop is set)</li>
 * <li>metaindex.crop</tt> - should the content to store be cropped down to the length specified in indexer.meta.forward.keylens?</li>
 * </ul>
 * 
 * @author Richard McCreadie, Dyaa Albakour
 * @since 4.0
 *
 */
public class MemoryCompressedMetaIndex extends MemoryMetaIndex implements MetaIndexMap {

	private static final long serialVersionUID = -4026137439218916329L;


	/*
	 * Meta-data index structures.
	 */
	private TIntObjectHashMap<byte[]> metadata;
	private TObjectIntHashMap<String> key2meta;
	private int[] keylengths;

	private int currentDocId=0;
	
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
	

	public MemoryCompressedMetaIndex(String[] keys, int[] keylengths) {
		this.keys = keys;
		metadata = new TIntObjectHashMap<byte[]>();
		key2meta = new TObjectIntHashMap<String>();
		this.keylengths = keylengths;
		int i = 0;
		for (String key : keys)
			key2meta.put(key, i++);
	}
	
	/**
	 * Constructor.
	 */
	public MemoryCompressedMetaIndex() {
		keys = _keys;
		keylens = _keylens;
		metadata = new TIntObjectHashMap<byte[]>();
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
		byte[] compressed = metadata.get(docid);
		
		if (compressed==null || compressed.length==0) {
			System.err.println("No metadata for docid "+docid);
			return "NOMETA-DATA";
		}
		String[] props = uncompress(compressed);
		return props[key2meta.get(key)];
	}

	/** {@inheritDoc} */
	public String[] getAllItems(int docid) throws IOException {
		return uncompress(metadata.get(docid));
	}

	/** {@inheritDoc} */
	public String[] getItems(String key, int[] docids) throws IOException {
		String[] data = new String[docids.length];
		int index = key2meta.get(key);
		for (int i = 0; i < docids.length; i++)
			data[i] = uncompress(metadata.get(docids[i]))[index];
		return data;
	}

	/** {@inheritDoc} */
	public String[] getItems(String[] keys, int docid) throws IOException {
		String[] data = new String[keys.length];
		String[] m = uncompress(metadata.get(docid));
		for (int i = 0; i < keys.length; i++)
			data[i] = m[key2meta.get(keys[i])];
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

		ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
		
		DataOutputStream compressedStream = new DataOutputStream(new GZIPOutputStream(compressedData));

		for (String key : keys) {
			String metaitem = data.get(key);
			if (metaitem == null)
				compressedStream.writeUTF("");
			else 
				compressedStream.writeUTF(metaitem);
		}
		compressedStream.flush();
		compressedStream.close();
		byte[] compressed = compressedData.toByteArray();
		compressedData.close();
		
		writeDocumentEntry(docid,compressed );
	}
	
	@Override
	public void writeDocumentEntry(int docid, String[] data) {
		if (data == null)
			return;

		ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
		
		DataOutputStream compressedStream;
		try {
			compressedStream = new DataOutputStream(new GZIPOutputStream(compressedData));
		

			for (String value : data) {
				
				if (value == null)
					compressedStream.writeUTF("");
				else 
					compressedStream.writeUTF(value);
			}
			compressedStream.flush();
			compressedStream.close();
			byte[] compressed = compressedData.toByteArray();
			compressedData.close();
			writeDocumentEntry(docid,compressed );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	/*
	 * Write meta-data.
	 */
	private void writeDocumentEntry(int docid, byte[] data) {
		if (metadata.contains(docid)) System.err.println("WARNING!: Overwritting a meta index entry");
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
	
	
	@Override
	public void writeDocumentEntry(String[] data) {
		writeDocumentEntry(currentDocId++,data );		
	}
	

	/**
	 * Return iterator over meta-data index.
	 */
	@Override
	public Iterator<String[]> iterator() {
		return new MetaIterator();
	}

	/**
	 * Meta-data index iterator.
	 */
	private class MetaIterator implements Iterator<String[]> {
		TIntObjectIterator<byte[]> iter = metadata.iterator();

		public boolean hasNext() {
			return iter.hasNext();
		}

		public String[] next() {
			iter.advance();
			return uncompress(iter.value());
		}

		public void remove() {
		}
	}
	
	
	public String[] uncompress(byte[] compressed) {
		String[] props = new String[keys.length];
		try {
			DataInputStream dis = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(compressed)));
			
			for (int i =0;i<keys.length; i++) {
				props[i] = dis.readUTF();
				//System.err.println(props[i]);
				i++;
			}
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}

	
	

}
