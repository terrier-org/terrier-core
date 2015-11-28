/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is FSOMapFileLexiconOutputStream.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import java.io.IOException;

import org.apache.hadoop.io.Text;

import org.terrier.structures.collections.FSOrderedMapFile;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
/** A LexiconOutputStream for FSOMapFileLexicon. Writes to a FSOrderedMapFile.
 * @author Craig Macdonald
 * @since 3.0
 */
public class FSOMapFileLexiconOutputStream extends LexiconOutputStream<String>
{
	protected FixedSizeWriteableFactory<Text> keyFactory;
	protected Text tempKey = null;
	protected final FSOrderedMapFile.MapFileWriter mapFileWriter;
	protected IndexOnDisk index = null;
	protected String leValueClassname = null;
	protected final String structureName;
	/**
	 * Construct an instance of the class with
	 * @param filename
	 * @param _keyFactory
	 * @throws IOException
	 */
	public FSOMapFileLexiconOutputStream(String filename, FixedSizeWriteableFactory<Text> _keyFactory) throws IOException
	{
		mapFileWriter = FSOrderedMapFile.mapFileWrite(filename);
		structureName = null;
		leValueClassname = null;
		index = null;
		keyFactory = _keyFactory;
		tempKey = keyFactory.newInstance();
	}
	/**
	 * Construct an instance of the class with
	 * @param path
	 * @param prefix
	 * @param _structureName
	 * @param _keyFactory
	 * @throws IOException
	 */
	public FSOMapFileLexiconOutputStream(String path, String prefix, String _structureName, 
			FixedSizeWriteableFactory<Text> _keyFactory) throws IOException
	{
		super();
		this.structureName = _structureName;
		mapFileWriter = FSOrderedMapFile.mapFileWrite(FSOMapFileLexicon.constructFilename(structureName, path, prefix, FSOMapFileLexicon.MAPFILE_EXT));
		keyFactory = _keyFactory;
		tempKey = keyFactory.newInstance();
	}
	
	@SuppressWarnings("unchecked")
	static FixedSizeWriteableFactory<Text> getKeyFactory(Index _index, String _structureName) throws IOException
	{
		_index.addIndexStructure(_structureName+"-keyfactory", 
				org.terrier.structures.seralization.FixedSizeTextFactory.class.getName(),
				"java.lang.String", "${max.term.length}");
		_index.flush();
		return (FixedSizeWriteableFactory<Text>)_index.getIndexStructure(_structureName+"-keyfactory");
	}

	/**
	 * Construct an instance of the class with
	 * @param _index
	 * @param _structureName
	 * @param valueFactoryClass
	 * @throws IOException
	 */
	public FSOMapFileLexiconOutputStream(IndexOnDisk _index, String _structureName, 
			Class<? extends FixedSizeWriteableFactory<LexiconEntry>>valueFactoryClass) throws IOException
	{
		this(_index.getPath(), _index.getPrefix(), _structureName, getKeyFactory(_index, _structureName));
		this.index = _index;
		leValueClassname = valueFactoryClass.getName();
	}

	/**
	 * Construct an instance of the class with
	 * @param _index
	 * @param _structureName
	 * @param _keyFactory
	 * @param valueFactoryClass
	 * @throws IOException
	 */
	public FSOMapFileLexiconOutputStream(IndexOnDisk _index, String _structureName, 
			FixedSizeWriteableFactory<Text> _keyFactory,
			Class<? extends FixedSizeWriteableFactory<LexiconEntry>>valueFactoryClass) throws IOException
	{
		this(_index.getPath(), _index.getPrefix(), _structureName, _keyFactory);
		this.index = _index;
		leValueClassname = valueFactoryClass.getName();
				
	}
	/**
	 * Construct an instance of the class with
	 * @param _index
	 * @param _structureName
	 * @param _keyFactory
	 * @param valueFactoryClassName
	 * @throws IOException
	 */
	public FSOMapFileLexiconOutputStream(IndexOnDisk _index, String _structureName, 
			FixedSizeWriteableFactory<Text> _keyFactory,
			String valueFactoryClassName) throws IOException
	{
		this(_index.getPath(), _index.getPrefix(), _structureName, _keyFactory);
		this.index = _index;
		leValueClassname = valueFactoryClassName;				
	}
	
	@Override
	public int writeNextEntry(String _key, LexiconEntry _value) throws IOException {
		tempKey.set(_key);
		mapFileWriter.write(tempKey, _value);
		super.incrementCounters(_value);
		return keyFactory.getSize() /* + TODO */;
	}
	
	@Override
	public void close()
	{
		try{
			mapFileWriter.close();
		} catch (IOException ioe) {}
		
		if (index != null)
		{
			addLexiconToIndex(index, this.structureName, this.leValueClassname);
		}
	}
	/**
	 * Adds Lexicon to index
	 * @param index
	 * @param structureName
	 * @param leValueClassname
	 */
	public static void addLexiconToIndex(IndexOnDisk index, String structureName, String leValueClassname)
	{
		index.addIndexStructure(
				structureName+"-valuefactory",
				leValueClassname,
				"", "");
		index.addIndexStructure(
				structureName, 
				"org.terrier.structures.FSOMapFileLexicon",
				"java.lang.String,org.terrier.structures.IndexOnDisk",
				"structureName,index");
		index.addIndexStructureInputStream(
				structureName, 
				"org.terrier.structures.FSOMapFileLexicon$MapFileLexiconIterator",
				"java.lang.String,org.terrier.structures.IndexOnDisk",
				"structureName,index");
		index.addIndexStructureInputStream(
				structureName+"-entry", 
				"org.terrier.structures.FSOMapFileLexicon$MapFileLexiconEntryIterator",
				"java.lang.String,org.terrier.structures.IndexOnDisk",
				"structureName,index");
	}

}
