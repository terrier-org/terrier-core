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
 * The Original Code is FSOMapFileLexiconOutputStreamGeneric.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.WritableComparable;
import org.terrier.structures.collections.FSOrderedMapFile;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;

@SuppressWarnings("unchecked")
public abstract class FSOMapFileLexiconOutputStreamGeneric<T1, T2 extends WritableComparable<?>>
	extends LexiconOutputStream<T1>
{

	protected FixedSizeWriteableFactory<T2> keyFactory;
	protected final T2 tempKey;
	protected final FSOrderedMapFile.MapFileWriter mapFileWriter;
	protected IndexOnDisk index = null;
	protected String leValueClassname = null;
	protected final String structureName;

	public FSOMapFileLexiconOutputStreamGeneric(String path, String prefix,
			String _structureName, FixedSizeWriteableFactory<T2> _keyFactory) throws IOException {
		super();
		this.structureName = _structureName;
		mapFileWriter = FSOrderedMapFile.mapFileWrite(FSOMapFileLexicon.constructFilename(structureName, path, prefix, FSOMapFileLexicon.MAPFILE_EXT));
		keyFactory = _keyFactory;
		tempKey = keyFactory.newInstance();
		
	}

	public FSOMapFileLexiconOutputStreamGeneric(
			IndexOnDisk _index,
			String _structureName,
			Class<? extends FixedSizeWriteableFactory<LexiconEntry>> valueFactoryClass) throws IOException {
		
		this(_index.getPath(), _index.getPrefix(), _structureName, (FixedSizeWriteableFactory<T2>) getKeyFactory(_index, _structureName));
		this.index = _index;
		leValueClassname = valueFactoryClass.getName();
	}

	public FSOMapFileLexiconOutputStreamGeneric(
			IndexOnDisk _index,
			String _structureName,
			FixedSizeWriteableFactory<T2> _keyFactory,
			Class<? extends FixedSizeWriteableFactory<LexiconEntry>> valueFactoryClass) throws IOException {
		this(_index.getPath(), _index.getPrefix(), _structureName, _keyFactory);
		this.index = _index;
		leValueClassname = valueFactoryClass.getName();
	}

	public FSOMapFileLexiconOutputStreamGeneric(IndexOnDisk _index,
			String _structureName, FixedSizeWriteableFactory<T2> _keyFactory,
			String valueFactoryClassName) throws IOException {
		this(_index.getPath(), _index.getPrefix(), _structureName, _keyFactory);
		this.index = _index;
		leValueClassname = valueFactoryClassName;
	}

	public FSOMapFileLexiconOutputStreamGeneric(String filename,
			FixedSizeWriteableFactory<T2> _keyFactory) throws IOException {
		
		mapFileWriter = FSOrderedMapFile.mapFileWrite(filename);
		structureName = null;
		leValueClassname = null;
		index = null;
		keyFactory = _keyFactory;
		tempKey = keyFactory.newInstance();
	}

	@Override
	public int writeNextEntry(T1 _key, LexiconEntry _value) throws IOException {
		setKey(_key);
		//System.err.println(_key.toString() + " => " + _value.toString());
		mapFileWriter.write(tempKey, _value);
		super.incrementCounters(_value);
		return keyFactory.getSize() /* + TODO */;
	}
	
	protected abstract void setKey(T1 k);
	
	@Override
	public void close() {
		super.close();
		try{
			mapFileWriter.close();
		} catch (Exception ioe) {}
	}
	
	static FixedSizeWriteableFactory<?> getKeyFactory(Index _index, String _structureName) throws IOException
	{
		_index.addIndexStructure(_structureName+"-keyfactory", 
				org.terrier.structures.seralization.FixedSizeTextFactory.class.getName(),
				"java.lang.String", "${max.term.length}");
		_index.flush();
		return (FixedSizeWriteableFactory<?>)_index.getIndexStructure(_structureName+"-keyfactory");
	}
	
	/**
	 * Adds Lexicon to index
	 * @param index
	 * @param structureName
	 * @param leValueClassname
	 */
	public static void addLexiconToIndex(IndexOnDisk index, 
			String structureName,
			Class<? extends Lexicon<?>> lexClass,
			Class<? extends Iterator<?>> lexInputStreamClass,
			Class<? extends Iterator<LexiconEntry>> lexEntryInputStreamClass,
			String leValueClassname)
	{
		index.addIndexStructure(
				structureName+"-valuefactory",
				leValueClassname,
				"", "");
		index.addIndexStructure(
				structureName, 
				lexClass.getName(),
				"java.lang.String,org.terrier.structures.IndexOnDisk",
				"structureName,index");
		
		if (lexInputStreamClass != null)
			index.addIndexStructureInputStream(
					structureName, 
					lexInputStreamClass.getName(),
					"java.lang.String,org.terrier.structures.IndexOnDisk",
					"structureName,index");
		if (lexEntryInputStreamClass != null)
			index.addIndexStructureInputStream(
					structureName+"-entry", 
					lexEntryInputStreamClass.getName(),
					"java.lang.String,org.terrier.structures.IndexOnDisk",
					"structureName,index");
	}
	
	/**
	 * Adds Lexicon to index
	 * @param index
	 * @param structureName
	 * @param leValueClassname
	 */
//	public static void addLexiconToIndex(IndexOnDisk index, 
//			String structureName, String leValueClassname)
//	{
//		index.addIndexStructure(
//				structureName+"-valuefactory",
//				leValueClassname,
//				"", "");
//		index.addIndexStructure(
//				structureName, 
//				"org.terrier.structures.FSOMapFileLexicon",
//				"java.lang.String,org.terrier.structures.IndexOnDisk",
//				"structureName,index");
//		index.addIndexStructureInputStream(
//				structureName, 
//				"org.terrier.structures.FSOMapFileLexicon$MapFileLexiconIterator",
//				"java.lang.String,org.terrier.structures.IndexOnDisk",
//				"structureName,index");
//		index.addIndexStructureInputStream(
//				structureName+"-entry", 
//				"org.terrier.structures.FSOMapFileLexiconGeneric$MapFileLexiconEntryIterator",
//				"java.lang.String,org.terrier.structures.IndexOnDisk",
//				"structureName,index");
//	}

}
