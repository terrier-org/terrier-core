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
 * The Original Code is FSOMapFileLexicon.java
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;
import gnu.trove.TIntObjectHashMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.collections.FSOrderedMapFile;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.Files;

/** Instance of a Lexicon&lt;String&gt; where a FSOrderedMapFile is always used as a backing store.
 * <p><b>Index Properties</b>:
 * <ul>
 * <li><tt>index.STRUCTURENAME.bsearchshortcut</tt> - depicts if the String lookups can be sped up using a binary search shortcut. Possible 
 * values are {charmap,default}. Charmap means a hashmap object will be read into memory that defines where to look for a given starting character of the lookup string.</li>
 * <li>See also the super-class</li>
 * </ul>
 * @author Craig Macdonald
 * @since 3.0 */
public class FSOMapFileLexicon extends FSOMapFileLexiconGeneric<String,Text>
{
	static final Logger logger = LoggerFactory.getLogger(FSOMapFileLexicon.class);
	
	public static final String HASH_EXT = ".fsomaphash";
	
	static class CharMapBSearchShortcut implements FSOrderedMapFile.FSOMapFileBSearchShortcut<Text>
	{
		final TIntObjectHashMap<int[]> map;
		final int[] defaultReturn;
		@SuppressWarnings("unchecked")
		public CharMapBSearchShortcut(String path, String prefix, String structureName, int size) throws Exception
		{
			ObjectInputStream ois = new ObjectInputStream(Files.openFileStream(constructFilename(structureName, path, prefix, HASH_EXT)));
			map = (TIntObjectHashMap<int[]>)ois.readObject();
			ois.close();
			defaultReturn = new int[]{0,size};
		}
		
		public int[] searchBounds(Text key) throws IOException {
			if (key.getLength() == 0)
				return defaultReturn;
			int[] boundaries = map.get(key.charAt(0));
			if (boundaries == null)
				return defaultReturn;
			return boundaries;
		}	
	}
    
    /** Construct a new FSOMapFileLexicon */
    @SuppressWarnings("unchecked")
	public FSOMapFileLexicon(String structureName, IndexOnDisk index) throws IOException
    {
    	this(
    		structureName, 
    		index.getPath(), 
    		index.getPrefix(), 
    		(FixedSizeWriteableFactory<Text>)index.getIndexStructure(structureName+"-keyfactory"),
    		(FixedSizeWriteableFactory<LexiconEntry>)index.getIndexStructure(structureName+"-valuefactory"),
    		index.getIndexProperty("index."+structureName+".termids", "aligned"),
    		index.getIndexProperty("index."+structureName+".bsearchshortcut", "default"),
    		index.getIndexProperty("index."+structureName+".data-source", "file")
    		);
    }
    
    public FSOMapFileLexicon(String structureName, String path, String prefix, 
    		FixedSizeWriteableFactory<Text> _keyFactory,
    		FixedSizeWriteableFactory<LexiconEntry> _valueFactory,
    		String termIdLookup, String termLookup, String dataFile) throws IOException
    {
    	super(structureName, path, prefix, _keyFactory, _valueFactory, termIdLookup, dataFile);
    	if (termLookup.equals("charmap"))
    	{
    		try{
    			((FSOrderedMapFile<Text,LexiconEntry>)this.map).setBSearchShortcut(
    				new CharMapBSearchShortcut(path, prefix, structureName, this.map.size()));
    		} catch (Exception e) {
    			throw new IOException("Problem loading FSOMapFileBSearchShortcut for "+structureName+": "+ e.getMessage()); 
    		}
    	}
    	else if (termLookup.equals("default"))
    	{
    		//do nothing
    	}
    	else
    	{
    		throw new IOException("Unrecognised value ("+termLookup+") for termLookup for structure "+structureName);
    	}
    }
    
    /** 
	 * {@inheritDoc} 
	 */
	public Iterator<Entry<String, LexiconEntry>> iterator() {
		return new MapFileLexiconIterator(this.map.entrySet().iterator());
	}

	@Override
	protected String toK1(Text key) {
		return key.toString();
	}

	@Override
	protected void setK2(String key, Text instance) {
		instance.set(key);		
	}
	
	public static class MapFileLexiconEntryIterator extends FSOMapFileLexiconGeneric.MapFileLexiconEntryIterator<Text>{

		public MapFileLexiconEntryIterator(
				Iterator<Entry<Text, LexiconEntry>> _internalIterator) {
			super(_internalIterator);
		}

		public MapFileLexiconEntryIterator(String filename,
				FixedSizeWriteableFactory<Text> keyFactory,
				FixedSizeWriteableFactory<LexiconEntry> valueFactory)
				throws IOException {
			super(filename, keyFactory, valueFactory);
		}

		public MapFileLexiconEntryIterator(String structureName,
				IndexOnDisk index) throws IOException {
			super(structureName, index);
		}

		public MapFileLexiconEntryIterator(String structureName, String path,
				String prefix, FixedSizeWriteableFactory<Text> keyFactory,
				FixedSizeWriteableFactory<LexiconEntry> valueFactory)
				throws IOException {
			super(structureName, path, prefix, keyFactory, valueFactory);
		}}
	
	public static class MapFileLexiconIterator extends FSOMapFileLexiconGeneric.MapFileLexiconIterator<String, Text>
	{
		public MapFileLexiconIterator(
				Iterator<Entry<Text, LexiconEntry>> _parent) {
			super(_parent);
		}

		public MapFileLexiconIterator(String filename,
				FixedSizeWriteableFactory<Text> keyFactory,
				FixedSizeWriteableFactory<LexiconEntry> valueFactory)
				throws IOException {
			super(filename, keyFactory, valueFactory);
			// TODO Auto-generated constructor stub
		}

		public MapFileLexiconIterator(String structureName, IndexOnDisk index)
				throws IOException {
			super(structureName, index);
			// TODO Auto-generated constructor stub
		}

		public MapFileLexiconIterator(String structureName, String path,
				String prefix, FixedSizeWriteableFactory<Text> keyFactory,
				FixedSizeWriteableFactory<LexiconEntry> valueFactory)
				throws IOException {
			super(structureName, path, prefix, keyFactory, valueFactory);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Entry<String, LexiconEntry> next() {
			Map.Entry<Text, LexiconEntry> lee = super.parent.next();
			return new LexiconFileEntry<String>(lee.getKey().toString(), lee.getValue());
		}		
	}
	
	
	/** Rename a FSOMapFileLexicon within the specified index location */
	public static void renameMapFileLexicon(
			String SrcStructureName, String SrcPath, String SrcPrefix,
			String destStructureName, String destPath, String destPrefix)
	{
		for(String extension : new String[]{HASH_EXT, ID_EXT, MAPFILE_EXT })
		{
			Files.rename(
					constructFilename(SrcStructureName, SrcPath, SrcPrefix, extension),
					constructFilename(destStructureName, destPath, destPrefix, extension)
				);
		}
	}
	
	/** Delete a FSOMapFileLexicon within the specified index location */
	public static void deleteMapFileLexicon(String structureName, String path, String prefix)
	{
		for(String extension : new String[]{HASH_EXT, ID_EXT, MAPFILE_EXT })
		{
			Files.delete(constructFilename(structureName, path, prefix, extension));
		}
	}

}
