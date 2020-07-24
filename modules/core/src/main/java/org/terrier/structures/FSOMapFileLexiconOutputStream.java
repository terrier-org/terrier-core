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
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.terrier.utility.ApplicationSetup;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
/** A LexiconOutputStream for FSOMapFileLexicon. Writes to a FSOrderedMapFile.
 * @author Craig Macdonald
 * @since 3.0
 */
public class FSOMapFileLexiconOutputStream extends FSOMapFileLexiconOutputStreamGeneric<String, Text>
{
	FixedSizeWriteableFactory<LexiconEntry> valueFactory;

	public FSOMapFileLexiconOutputStream(
			IndexOnDisk _index,
			String _structureName,
			FixedSizeWriteableFactory<LexiconEntry> valueFactory)
			throws IOException {
		super(_index, _structureName, null);
		this.valueFactory = valueFactory;
	}

	public FSOMapFileLexiconOutputStream(
			IndexOnDisk _index,
			String _structureName,
			FixedSizeWriteableFactory<Text> keyFactory,
			FixedSizeWriteableFactory<LexiconEntry> valueFactory)
			throws IOException {
		super(_index, _structureName, keyFactory, (Class<? extends FixedSizeWriteableFactory<LexiconEntry>>) null);
		this.valueFactory = valueFactory;
	}

	@Deprecated
	public FSOMapFileLexiconOutputStream(
			IndexOnDisk _index,
			String _structureName,
			Class<? extends FixedSizeWriteableFactory<LexiconEntry>> valueFactoryClass)
			throws IOException {
		super(_index, _structureName, valueFactoryClass);
		try{
			valueFactory = valueFactoryClass.getConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Deprecated
	public FSOMapFileLexiconOutputStream(
			IndexOnDisk _index,
			String _structureName,
			FixedSizeWriteableFactory<Text> _keyFactory,
			Class<? extends FixedSizeWriteableFactory<LexiconEntry>> valueFactoryClass)
			throws IOException {
		super(_index, _structureName, _keyFactory, valueFactoryClass);
		try{
			valueFactory = valueFactoryClass.getConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Deprecated
	public FSOMapFileLexiconOutputStream(IndexOnDisk _index,
			String _structureName, FixedSizeWriteableFactory<Text> _keyFactory,
			String valueFactoryClassName) throws IOException {
		super(_index, _structureName, _keyFactory, valueFactoryClassName);
		try{
			valueFactory = ApplicationSetup.getClass(valueFactoryClassName).asSubclass(FixedSizeWriteableFactory.class).getConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}	
	}

	public FSOMapFileLexiconOutputStream(String filename,
			FixedSizeWriteableFactory<Text> _keyFactory) throws IOException {
		super(filename, _keyFactory);
	}

	public FSOMapFileLexiconOutputStream(String path, String prefix,
			String _structureName, FixedSizeWriteableFactory<Text> _keyFactory)
			throws IOException {
		super(path, prefix, _structureName, _keyFactory);
	}

	@Override
	protected void setKey(String k) {
		tempKey.set(k);
	}
	
	@Override
	public void close() {
		super.close();
		if (index != null)
		{
			addLexiconToIndex(index, this.structureName, this.valueFactory);
		}
	}

	/**
	 * Adds Lexicon to index
	 * @param index
	 * @param structureName
	 * @param leValueClassname
	 */
	@Deprecated
	public static void addLexiconToIndex(IndexOnDisk index, String structureName, String leValueClassname)
	{
		try {
			FSOMapFileLexiconOutputStreamGeneric.addLexiconToIndex(index, structureName,
				FSOMapFileLexicon.class, 
				FSOMapFileLexicon.MapFileLexiconIterator.class,
				FSOMapFileLexicon.MapFileLexiconEntryIterator.class,				
				ApplicationSetup.getClass(leValueClassname).asSubclass(FixedSizeWriteableFactory.class).getConstructor().newInstance());

		} catch (Exception e) {
			throw new IllegalStateException(e);
		}			
	}

	public static void addLexiconToIndex(IndexOnDisk index, String structureName, FixedSizeWriteableFactory<LexiconEntry> valueFactory)
	{
		FSOMapFileLexiconOutputStreamGeneric.addLexiconToIndex(index, structureName,
				FSOMapFileLexicon.class, 
				FSOMapFileLexicon.MapFileLexiconIterator.class,
				FSOMapFileLexicon.MapFileLexiconEntryIterator.class,				
				valueFactory);
	}

}
