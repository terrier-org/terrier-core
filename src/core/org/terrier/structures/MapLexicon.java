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
 * The Original Code is MapLexicon.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.hadoop.io.Text;
import org.terrier.structures.collections.OrderedMap;
import org.terrier.structures.seralization.WriteableFactory;

/** Implementation of a lexicon. This class should be subclassed by
 * any lexicon implementation which use a java.util.Map for storing
 * entries.
 * @author Craig Macdonald
 * @since 3.0
 */
public abstract class MapLexicon extends Lexicon<String> implements Closeable
{
	protected WriteableFactory<Text> keyFactory = null;
	
	protected Object modificationLock = new Object();
	
	/**
	 * Interface for getting the lexicon term index for a given term id
	 * @author Richard McCreadie
	 *
	 */
    protected interface Id2EntryIndexLookup
    {
        int getIndex(int termid) throws IOException; 
    }
    
    /**
	 * Lexicon map where the termid is the term index
	 * @author Richard McCreadie
	 *
	 */
    protected static class IdIsIndex implements Id2EntryIndexLookup
    {
        public int getIndex(int termid)
        {
            return termid;
        }
    }
    
    protected final Map<Text,LexiconEntry> map;
    Id2EntryIndexLookup idlookup;
    /**
     * Construct an instance of the class with
     * @param backingMap
     */
    public MapLexicon(Map<Text,LexiconEntry> backingMap)
    {
        this.map = backingMap;
        this.idlookup = new IdIsIndex();
    }
    /**
     * Construct an instance of the class with
     * @param backingMap
     * @param idlookupobject
     */
    public MapLexicon(Map<Text,LexiconEntry> backingMap,
        Id2EntryIndexLookup idlookupobject)
    {
        this.map = backingMap;
        this.idlookup = idlookupobject;
    }
    
    protected void setTermIdLookup(Id2EntryIndexLookup idlookupobject)
    {
    	synchronized(modificationLock) {
    	
        this.idlookup = idlookupobject;
        
    	}
    }
	/** 
	 * {@inheritDoc} 
	 */
    public LexiconEntry getLexiconEntry(String term)
    {
    	synchronized(modificationLock) {
    	
    	Text key = keyFactory.newInstance();
    	key.set(term);
    	if (!map.containsKey(key)) return null;
        return map.get(key);
    	}
    }
	/** 
	 * {@inheritDoc} 
	 */
    public Map.Entry<String,LexiconEntry> getIthLexiconEntry(int index) 
    {
    	synchronized(modificationLock) {
    	
        if (! (map instanceof OrderedMap))
            throw new UnsupportedOperationException();
        return toStringEntry(((OrderedMap<Text, LexiconEntry>)map).get(index));
    	}
    }
    
    /** 
	 * {@inheritDoc} 
	 */
    public Iterator<Map.Entry<String,LexiconEntry>> getLexiconEntryRange(String from, String to)
    {
    	synchronized(modificationLock) {
    	if (! (map instanceof SortedMap))
    		throw new UnsupportedOperationException();
		Text key1 = keyFactory.newInstance();
		key1.set(from);
		Text key2 = keyFactory.newInstance();
		key2.set(to);
		final Iterator<Map.Entry<Text,LexiconEntry>> iter = ((SortedMap<Text,LexiconEntry>)map).subMap(key1, key2).entrySet().iterator();
		return new Iterator<Map.Entry<String,LexiconEntry>>()
		{
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}
			
			@Override
			public Entry<String, LexiconEntry> next() {
				return toStringEntry(iter.next());
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
    	}
    }
    
	/** 
	 * {@inheritDoc} 
	 */
    public Map.Entry<String,LexiconEntry> getLexiconEntry(int termid)
    {
    	synchronized(modificationLock) {
    	int id;
    	try{
    		id = idlookup.getIndex(termid);
    	} catch (IOException ioe) {
    		return null;
    	}
    	if (id == -1)
    		return null;
        return getIthLexiconEntry(id);
    	}
    }
	/** 
	 * {@inheritDoc} 
	 */
    public int numberOfEntries()
    {
    	return this.map.size();
    }
    
    static Map.Entry<String,LexiconEntry> toStringEntry (Map.Entry<Text,LexiconEntry> a)
    {
    	return new LexiconFileEntry<String>(a.getKey().toString(), a.getValue());
    }
	/** 
	 * {@inheritDoc} 
	 */
    public void close() throws IOException
    {
        if (map instanceof Closeable)
            ((Closeable)map).close();
        if (idlookup instanceof Closeable)
            ((Closeable)idlookup).close();
    }
}
