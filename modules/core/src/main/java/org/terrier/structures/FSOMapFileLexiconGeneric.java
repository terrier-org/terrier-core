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
 * The Original Code is FSOMapFileLexiconGeneric.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.WritableComparable;
import org.terrier.structures.collections.FSOrderedMapFile;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.terrier.utility.io.RandomDataInput;
import org.terrier.utility.io.RandomDataInputMemory;
import org.terrier.utility.io.WrappedIOException;

@SuppressWarnings("rawtypes")
public abstract class FSOMapFileLexiconGeneric<K1,K2 extends WritableComparable> extends MapLexicon<K1,K2> {

	public static final String ID_EXT = ".fsomapid";
	public static final String MAPFILE_EXT = FSOrderedMapFile.USUAL_EXTENSION;

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
	 * @param <K>
     * @param filename - filename of mapfile
     * @param keyFactory - factory to create keys
     * @param valueFactory - factory to create values
     * @param dataSource - what type of object to instantiate
     * @return - the created FSOrderedMapFile<Text,LexiconEntry> 
     * @throws IOException - if any problems occur
     */
    @SuppressWarnings("unchecked")
	static <K extends WritableComparable<K>> FSOrderedMapFile<K,LexiconEntry> loadMapFile(String filename, FixedSizeWriteableFactory<K> keyFactory, 
    		FixedSizeWriteableFactory<LexiconEntry> valueFactory, String dataSource) throws IOException
    {
    	if (dataSource.equals("fileinmem"))
    		return new FSOrderedMapFile<K,LexiconEntry>(
					new RandomDataInputMemory(filename),
					filename,
					keyFactory,
                    valueFactory);
    	if (dataSource.equals("file"))
    		return new FSOrderedMapFile<K,LexiconEntry>(
					filename,
					false,
					keyFactory,
					valueFactory);
    	//else, we've been given a class name to instantiate
    	FSOrderedMapFile<K,LexiconEntry>rtr = null;
    	
    	try {
    		Class<?> mapClass = ApplicationSetup.getClass(dataSource).asSubclass(FSOrderedMapFile.class);
			rtr = (FSOrderedMapFile<K, LexiconEntry>) mapClass
				.getConstructor(String.class, Boolean.TYPE, FixedSizeWriteableFactory.class, FixedSizeWriteableFactory.class)
				.newInstance(filename, false, keyFactory, valueFactory);
    	}
    	catch (Exception e)
    	{
			throw new WrappedIOException("Could not find a class for FSOMapFileLexicon", e);
		}
    	return rtr;
    }

	@SuppressWarnings("unchecked")
	public FSOMapFileLexiconGeneric(String structureName, String path,
			String prefix, FixedSizeWriteableFactory<K2> _keyFactory,
			FixedSizeWriteableFactory<LexiconEntry> _valueFactory,
			String termIdLookup, String dataFile) throws IOException
	{
		
		/* if dataSource is fileinmem, then the file will be wholly loaded into memory.
    	 * file means use on disk. Otherwise use as a class name */
    	super( loadMapFile(
    			constructFilename(structureName, path, prefix, MAPFILE_EXT), 
    			_keyFactory, _valueFactory, 
    			dataFile));
    	this.keyFactory = _keyFactory;	
    	if ("aligned".equals(termIdLookup))
        {
            setTermIdLookup(new IdIsIndex());
        }
        else if ("file".equals(termIdLookup))
        {
            setTermIdLookup(new OnDiskLookup(path, prefix, structureName));
        }
        else if ("fileinmem".equals(termIdLookup))
        {
            setTermIdLookup(new InMemoryLookup(path, prefix, structureName, this.map.size()));
        }
        else if ("disabled".equals(termIdLookup))
        {
        	setTermIdLookup(null);
        }
        else
        {
            throw new IOException("Unrecognised value ("+termIdLookup+") for termIdlookup for structure "+structureName);
        }
		
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
	
	/** 
	 * An iterator over the lexicon
	 */
	public abstract static class MapFileLexiconIterator<T1,T2 extends WritableComparable<?>> 
		implements Iterator<Entry<String, LexiconEntry>>, Closeable
	{
		protected Iterator<Entry<T2, LexiconEntry>> parent;
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
	    		(FixedSizeWriteableFactory<T2>)index.getIndexStructure(structureName+"-keyfactory"),
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
	    		FixedSizeWriteableFactory<T2> keyFactory,
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
		public MapFileLexiconIterator(String filename, FixedSizeWriteableFactory<T2> keyFactory,
	    		FixedSizeWriteableFactory<LexiconEntry> valueFactory) throws IOException
	    {
			this(new FSOrderedMapFile.EntryIterator<T2, LexiconEntry>(filename, keyFactory, valueFactory));
	    }
		/**
		 * Construct an instance of the class with
		 * @param _parent
		 */
		public MapFileLexiconIterator(Iterator<Entry<T2, LexiconEntry>> _parent)
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
	
	/** Iterate through the values in order */
	public static class MapFileLexiconEntryIterator<T extends WritableComparable<?>>
		implements Iterator<LexiconEntry>, Closeable, Skipable
	{
		protected Iterator<Map.Entry<T, LexiconEntry>> internalIterator;
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
		    		(FixedSizeWriteableFactory<T>)index.getIndexStructure(structureName.replaceFirst("-entry", "")+"-keyfactory"),
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
	    		FixedSizeWriteableFactory<T> keyFactory,
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
		public MapFileLexiconEntryIterator(String filename, FixedSizeWriteableFactory<T> keyFactory,
	    		FixedSizeWriteableFactory<LexiconEntry> valueFactory) throws IOException
	    {
			//this(new FSOrderedMapFile.EntryIterator<T, LexiconEntry>(filename, keyFactory, valueFactory));
			this(getTotalIterator(filename, keyFactory, valueFactory));
	    }
		
		static  <T  extends WritableComparable<?>> Iterator<Map.Entry<T, LexiconEntry>> 
			getTotalIterator(String filename, FixedSizeWriteableFactory<T> keyFactory,
	    		FixedSizeWriteableFactory<LexiconEntry> valueFactory) throws IOException
		{
			Iterator<Map.Entry<T, LexiconEntry>> rtr = new FSOrderedMapFile.EntryIterator<T, LexiconEntry>(filename, keyFactory, valueFactory);
			return rtr;
		}
		/**
		 * Construct an instance of the class with
		 * @param _internalIterator
		 */
		public MapFileLexiconEntryIterator(Iterator<Map.Entry<T, LexiconEntry>> _internalIterator)
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

}
