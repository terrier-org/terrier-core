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
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;
import gnu.trove.TIntObjectHashMap;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.collections.FSOrderedMapFile;
import org.terrier.structures.indexing.LexiconBuilder;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.Files;
import org.terrier.utility.io.RandomDataInput;
import org.terrier.utility.io.RandomDataInputMemory;
import org.terrier.utility.io.WrappedIOException;

/** Instance of a Lexicon where a FSOrderedMapFile is always used as a backing store.
 * <p><b>Index Properties</b>:
 * <ul>
 * <li><tt>index.STRUCTURENAME.termids</tt> - one of {aligned, file, fileinmem, disabled}. Depicts how termid to term lookups are handled.</li>
 * <li><tt>index.STRUCTURENAME.data-source</tt> - one of {file, fileinmem}. Depicts how the lexicon will be read. file means on disk, fileinmem means the entire
 * lexicon file will be read into memory and read from there. Defaults to file.</li>
 * <li><tt>index.STRUCTURENAME.bsearchshortcut</tt> - depicts if the String lookups can be sped up using a binary search shortcut. Possible 
 * values are {charmap,default}. Charmap means a hashmap object will be read into memory that defines where to look for a given starting character of the lookup string.</li>
 * </ul>
 * @author Craig Macdonald
 * @since 3.0 */
public class FSOMapFileLexicon extends MapLexicon
{
	static final Logger logger = LoggerFactory.getLogger(FSOMapFileLexicon.class);
	static final String MAPFILE_EXT = FSOrderedMapFile.USUAL_EXTENSION;
	static final String ID_EXT = ".fsomapid";
	static final String HASH_EXT = ".fsomaphash";
	
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
	
    static class OnDiskLookup implements Id2EntryIndexLookup, java.io.Closeable
    {
        final RandomDataInput lexIdFile;
        protected static final long SIZE_OF_INT = 4;
        public OnDiskLookup(String path, String prefix, String structureName) throws IOException
        {
            lexIdFile = Files.openFileRandom(
            		constructFilename(structureName, path, prefix, ID_EXT));
        }
        
        public int getIndex(int termid) throws IOException
        {
            lexIdFile.seek(SIZE_OF_INT * (long)termid);
            return lexIdFile.readInt();
        }
        
        public void close() throws IOException
        {
            lexIdFile.close();
        }
    }
    
    static class InMemoryLookup implements Id2EntryIndexLookup
    {
        protected final int[] id2index;
        public InMemoryLookup(String path, String prefix, String structureName, int size) 
            throws IOException
        {
        	String filename = constructFilename(structureName, path, prefix, ID_EXT);
            size = (int)(Files.length(filename) / (long)4);
        	DataInputStream lexIdFile = new DataInputStream(Files.openFileStream(filename));
            id2index = new int[size];
            for(int i=0;i<size;i++)
            {
                id2index[i] = lexIdFile.readInt();
            }
            lexIdFile.close();
        }
        
        public int getIndex(int termid)
        {
            return id2index[termid];
        }
    }
    
    /** loads an appropriate FSOrderedMapFile<Text,LexiconEntry> implementation,
     * depending on the value of dataSource.
     * <ol>
     * <li>fileinmem - use a RandomDataInputMemory instance over the file</li>
     * <li>file - use file on disk, as normal.</li>
     * <li>anything else: assume to be a class name, and instantiate using the
     * expected constructor.</li>
     * </ol>
     * @param filename - filename of mapfile
     * @param keyFactory - factory to create keys
     * @param valueFactory - factory to create values
     * @param dataSource - what type of object to instantiate
     * @return - the created FSOrderedMapFile<Text,LexiconEntry> 
     * @throws IOException - if any problems occur
     */
    @SuppressWarnings("unchecked")
	static FSOrderedMapFile<Text,LexiconEntry> loadMapFile(String filename, FixedSizeWriteableFactory<Text> keyFactory, 
    		FixedSizeWriteableFactory<LexiconEntry> valueFactory, String dataSource) throws IOException
    {
    	if (dataSource.equals("fileinmem"))
    		return new FSOrderedMapFile<Text,LexiconEntry>(
					new RandomDataInputMemory(filename),
					filename,
					keyFactory,
                    valueFactory);
    	if (dataSource.equals("file"))
    		return new FSOrderedMapFile<Text,LexiconEntry>(
					filename,
					false,
					keyFactory,
					valueFactory);
    	//else, we've been given a class name to instantiate
    	FSOrderedMapFile<Text,LexiconEntry>rtr = null;
    	
    	try {
    		if (dataSource.startsWith("uk.ac.gla.terrier"))
    			dataSource = dataSource.replaceAll("uk.ac.gla.terrier", "org.terrier");				
			
    		Class<?> mapClass = Class.forName(dataSource).asSubclass(FSOrderedMapFile.class);
			rtr = (FSOrderedMapFile<Text, LexiconEntry>) mapClass
				.getConstructor(String.class, Boolean.TYPE, FixedSizeWriteableFactory.class, FixedSizeWriteableFactory.class)
				.newInstance(filename, false, keyFactory, valueFactory);
    	}
    	catch (Exception e)
    	{
			throw new WrappedIOException("Could not find a class for FSOMapFileLexicon", e);
		}
    	return rtr;
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
    /**
     * Construct an instance of the class with
     * @param structureName
     * @param path
     * @param prefix
     * @param _keyFactory
     * @param _valueFactory
     * @param termIdLookup
     * @param termLookup
     * @param dataFile
     * @throws IOException
     */
    public FSOMapFileLexicon(String structureName, String path, String prefix, 
    		FixedSizeWriteableFactory<Text> _keyFactory,
    		FixedSizeWriteableFactory<LexiconEntry> _valueFactory,
    		String termIdLookup, String termLookup, String dataFile) throws IOException
    {
    	/* if dataSource is fileinmem, then the file will be wholly loaded into memory.
    	 * file means use on disk. Otherwise use as a class name */
    	super( loadMapFile(
    			constructFilename(structureName, path, prefix, MAPFILE_EXT), 
    			_keyFactory, _valueFactory, 
    			dataFile));
    	this.keyFactory = _keyFactory;
    	if (termIdLookup.equals("aligned"))
        {
            setTermIdLookup(new IdIsIndex());
        }
        else if (termIdLookup.equals("file"))
        {
            setTermIdLookup(new OnDiskLookup(path, prefix, structureName));
        }
        else if (termIdLookup.equals("fileinmem"))
        {
            setTermIdLookup(new InMemoryLookup(path, prefix, structureName, this.map.size()));
        }
        else if (termIdLookup.equals("disabled"))
        {
        	setTermIdLookup(null);
        }
        else
        {
            throw new IOException("Unrecognised value ("+termIdLookup+") for termIdlookup for structure "+structureName);
        }
    	
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

	@Override
	public void close() throws IOException {
		super.close();
	}
	
	/** Iterate through the values in order */
	public static class MapFileLexiconEntryIterator
		implements Iterator<LexiconEntry>, Closeable, Skipable
	{
		protected Iterator<Map.Entry<Text, LexiconEntry>> internalIterator;
		/**
		 * Construct an instance of the class with
		 * @param structureName
		 * @param index
		 * @throws IOException
		 */
		@SuppressWarnings("unchecked")
		public MapFileLexiconEntryIterator(String structureName, IndexOnDisk index) throws IOException
		{
			this(	structureName.replaceFirst("-entry", ""), 
					index.getPath(), 
					index.getPrefix(), 
		    		(FixedSizeWriteableFactory<Text>)index.getIndexStructure(structureName.replaceFirst("-entry", "")+"-keyfactory"),
		    		(FixedSizeWriteableFactory<LexiconEntry>)index.getIndexStructure(structureName.replaceFirst("-entry", "")+"-valuefactory"));
		}
		/**
		 * Construct an instance of the class with
		 * @param structureName
		 * @param path
		 * @param prefix
		 * @param keyFactory
		 * @param valueFactory
		 * @throws IOException
		 */
		public MapFileLexiconEntryIterator(String structureName, String path, String prefix, 
	    		FixedSizeWriteableFactory<Text> keyFactory,
	    		FixedSizeWriteableFactory<LexiconEntry> valueFactory) throws IOException
		{
			this(constructFilename(structureName, path, prefix, MAPFILE_EXT), keyFactory, valueFactory);
		}
		/**
		 * Construct an instance of the class with
		 * @param filename
		 * @param keyFactory
		 * @param valueFactory
		 * @throws IOException
		 */
		public MapFileLexiconEntryIterator(String filename, FixedSizeWriteableFactory<Text> keyFactory,
	    		FixedSizeWriteableFactory<LexiconEntry> valueFactory) throws IOException
	    {
			this(new FSOrderedMapFile.EntryIterator<Text, LexiconEntry>(filename, keyFactory, valueFactory));
	    }
		/**
		 * Construct an instance of the class with
		 * @param _internalIterator
		 */
		public MapFileLexiconEntryIterator(Iterator<Map.Entry<Text, LexiconEntry>> _internalIterator)
		{
			internalIterator = _internalIterator;
		}
		/** 
		 * {@inheritDoc} 
		 */
		public boolean hasNext() {
			return internalIterator.hasNext();
		}
		/** 
		 * {@inheritDoc} 
		 */
		public LexiconEntry next() {
			return internalIterator.next().getValue();
		}
		/** 
		 * {@inheritDoc} 
		 */
		public void remove() {
			internalIterator.remove();
		}
		/** 
		 * {@inheritDoc} 
		 */
		public void close() throws IOException {
			if (internalIterator instanceof java.io.Closeable)
				((java.io.Closeable)internalIterator).close();
		}
		/** 
		 * {@inheritDoc} 
		 */
		public void skip(int numEntries) throws IOException {
			if (numEntries == 0)
				return;
			if (! (internalIterator instanceof Skipable))
				throw new UnsupportedOperationException("Skipping not supported");
			((Skipable)internalIterator).skip(numEntries);
		}
	
	}
	/** 
	 * An iterator over the lexicon
	 */
	public static class MapFileLexiconIterator 
		implements Iterator<Entry<String, LexiconEntry>>, Closeable
	{
		protected Iterator<Entry<Text, LexiconEntry>> parent;
		/**
		 * Construct an instance of the class with
		 * @param structureName
		 * @param index
		 * @throws IOException
		 */
		@SuppressWarnings("unchecked")
		public MapFileLexiconIterator(String structureName, IndexOnDisk index) throws IOException
		{
			this(
				structureName, 
				index.getPath(), 
				index.getPrefix(), 
	    		(FixedSizeWriteableFactory<Text>)index.getIndexStructure(structureName+"-keyfactory"),
	    		(FixedSizeWriteableFactory<LexiconEntry>)index.getIndexStructure(structureName+"-valuefactory"));
		}
		/**
		 * Construct an instance of the class with
		 * @param structureName
		 * @param path
		 * @param prefix
		 * @param keyFactory
		 * @param valueFactory
		 * @throws IOException
		 */
		public MapFileLexiconIterator(String structureName, String path, String prefix, 
	    		FixedSizeWriteableFactory<Text> keyFactory,
	    		FixedSizeWriteableFactory<LexiconEntry> valueFactory) throws IOException
		{
			this(constructFilename(structureName, path, prefix, MAPFILE_EXT), keyFactory, valueFactory);
		}
		/**
		 * Construct an instance of the class with
		 * @param filename
		 * @param keyFactory
		 * @param valueFactory
		 * @throws IOException
		 */
		public MapFileLexiconIterator(String filename, FixedSizeWriteableFactory<Text> keyFactory,
	    		FixedSizeWriteableFactory<LexiconEntry> valueFactory) throws IOException
	    {
			this(new FSOrderedMapFile.EntryIterator<Text, LexiconEntry>(filename, keyFactory, valueFactory));
	    }
		/**
		 * Construct an instance of the class with
		 * @param _parent
		 */
		public MapFileLexiconIterator(Iterator<Entry<Text, LexiconEntry>> _parent)
		{
			parent = _parent;
		}
		/** 
		 * {@inheritDoc} 
		 */
		public boolean hasNext() {
			return parent.hasNext();
		}
		/** 
		 * {@inheritDoc} 
		 */
		public Entry<String, LexiconEntry> next() {
			return MapLexicon.toStringEntry(parent.next());
		}
		/** 
		 * {@inheritDoc} 
		 */
		public void remove() {
			parent.remove();
		}
		/** 
		 * {@inheritDoc} 
		 */
		public void close() throws IOException {
			if (parent instanceof Closeable)
				((Closeable)parent).close();
		}
	}
	/** 
	 * {@inheritDoc} 
	 */
	public Iterator<Entry<String, LexiconEntry>> iterator() {
		return new MapFileLexiconIterator(this.map.entrySet().iterator());
	}
	
	
	/** Does two things to a FSOMapFileLexicon: adds the termid lookup file (if required),
	 * and also creates the lexicon has file.
	 * @param structureName - name of the index structure that this FSOMapFileLexicon represents
	 * @param index - the index that the index belongs
	 * @throws IOException if an IO problem occurs
	 */
	@SuppressWarnings("unchecked")
	public static void optimise(
			String structureName, 
			IndexOnDisk index, 
			LexiconBuilder.CollectionStatisticsCounter statsCounter) 
		throws IOException
	{
		final String mapFileFilename = constructFilename(structureName, index.getPath(), index.getPrefix(), MAPFILE_EXT);
		final FixedSizeWriteableFactory<Text> keyFactory = 
			(FixedSizeWriteableFactory<Text>)index.getIndexStructure(structureName+"-keyfactory");
		final FixedSizeWriteableFactory<LexiconEntry> valueFactory = 
			(FixedSizeWriteableFactory<LexiconEntry>)index.getIndexStructure(structureName+"-valuefactory");
		final int numEntries = FSOrderedMapFile.numberOfEntries(mapFileFilename, keyFactory, valueFactory);
		optimise(structureName, index, statsCounter, numEntries);
	}
	/** 
	 * optimise
	 * @param structureName
	 * @param index
	 * @param statsCounter
	 * @param numEntries
	 * @throws IOException
	 */
	@SuppressWarnings({"unchecked", "resource"})
	public static void optimise(
			String structureName, 
			IndexOnDisk index,
			LexiconBuilder.CollectionStatisticsCounter statsCounter,
			int numEntries) 
		throws IOException
	{
	
		final String mapFileFilename = constructFilename(structureName, index.getPath(), index.getPrefix(), MAPFILE_EXT);
		final FixedSizeWriteableFactory<Text> keyFactory = 
			(FixedSizeWriteableFactory<Text>)index.getIndexStructure(structureName+"-keyfactory");
		final FixedSizeWriteableFactory<LexiconEntry> valueFactory = 
			(FixedSizeWriteableFactory<LexiconEntry>)index.getIndexStructure(structureName+"-valuefactory");
		logger.info("Optimising lexicon with "+ numEntries + " entries");
		//term id lookups
		boolean termIdsAligned = true;
		int[] termid2index = new int[numEntries];
		Arrays.fill(termid2index, -1);
		int counter= 0; int lastTermId = -1;
		
		//bsearch reduction
		int previousFirstChar = -1;
		int firstChar = 0;
		final TIntObjectHashMap<int[]> map = new TIntObjectHashMap<int[]>();
		
		
		Iterator<Map.Entry<Text,LexiconEntry>> iterator = 
			new FSOrderedMapFile.EntryIterator<Text, LexiconEntry>(mapFileFilename, keyFactory, valueFactory);
		Map.Entry<Text,LexiconEntry> lee = null;
		int termId = Integer.MIN_VALUE;
		try {
			while(iterator.hasNext())
			{
				lee = iterator.next();
				//System.err.println(lee.toString());
				//System.err.println(lee.toString() +" "+lee.getValue().getTermId()+" "+lee.getValue().getFrequency());
				
				//term id
				termId = lee.getValue().getTermId();
				if (! (termId == lastTermId+1))
					termIdsAligned = false;
				if (termid2index[termId] != -1)
				{
					throw new WrappedIOException(new IllegalArgumentException("Termid " + termId + " is not unique - used at entries " +termid2index[termId]+ " and" + counter));
				}
				termid2index[termId] = counter;
				lastTermId = termId;
				
				//bsearch reduction optimisaion
				firstChar = lee.getKey().charAt(0);
				if (firstChar!=previousFirstChar) {
					int[] boundaries = new int[] {counter, 0};
					map.put(firstChar, boundaries);
					previousFirstChar = firstChar;
				}
				
				//increments
				statsCounter.count(lee.getValue());
				counter++;
			}
		} catch (ArrayIndexOutOfBoundsException ae) {
			logger.error("Termid " + termId + " is  too large (expected only "
					+termid2index.length +" entries). Bad lexicon entry is: " 
					+ lee.getKey().toString() + " -> " + lee.getValue().toString() );
			throw ae;
		}
		
		if (counter != numEntries)
			termIdsAligned = false;
		IndexUtil.close(iterator);
		
		//deal with termids
		if (termIdsAligned)
		{
			index.setIndexProperty("index."+structureName+".termids", "aligned");
			logger.info("All ids for structure "+structureName+ " are aligned, skipping "
				+ID_EXT+ " file");
		}
		else
		{
			DataOutputStream dos = new DataOutputStream(Files.writeFileStream(
					constructFilename(structureName, ((IndexOnDisk) index).getPath(), ((IndexOnDisk) index).getPrefix(), ID_EXT)));
			for(int indexof : termid2index)
				dos.writeInt(indexof);
			dos.close();
			index.setIndexProperty("index."+structureName+".termids", (numEntries > 15000000) ? "file" : "fileinmem");
		}
		
		
		int[] mapKeys = map.keys();
		Arrays.sort(mapKeys);
		final int mapKeysSize = mapKeys.length;
		for (int i=0; i<mapKeysSize-1; i++) {
			int nextLowerBoundary = (map.get(mapKeys[i+1]))[0];
			int[] currentBoundaries = map.get(mapKeys[i]);
			currentBoundaries[1] = nextLowerBoundary;
			map.put(mapKeys[i], currentBoundaries);
		}
		//do something about the last entry
		int nextLowerBoundary = counter;
		int[] currentBoundaries = (int[])map.get(mapKeys[mapKeysSize-1]);
		currentBoundaries[1] = nextLowerBoundary;
		map.put(mapKeys[mapKeysSize-1], currentBoundaries);
		
		final ObjectOutputStream oos = new ObjectOutputStream(Files.writeFileStream(
				constructFilename(structureName, ((IndexOnDisk) index).getPath(), ((IndexOnDisk) index).getPrefix(), HASH_EXT)));
		oos.writeObject(map);
		oos.close();
		index.setIndexProperty("index."+structureName+".bsearchshortcut", "charmap");
		index.flush();
	}
	/** 
	 * Constructs a filename
	 * @param structureName
	 * @param path
	 * @param prefix
	 * @param extension
	 * @return filename
	 */
	public static String constructFilename(String structureName, String path, String prefix, String extension)
	{
		return path 
	        + "/"+ prefix 
	        +"." + structureName + extension;
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
