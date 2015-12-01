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
 * The Original Code is FSOrderedMapFile.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.collections;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Skipable;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.structures.seralization.WriteableFactory;
import org.terrier.utility.Files;
import org.terrier.utility.io.RandomDataInput;
import org.terrier.utility.io.RandomDataOutput;

/** An implementation of java.util.Map that can be accessed from disk.
 * Key and value types are assumed to have a fixed size. Their factories
 * must be passed to the constructor. In the name, FSOrderedMapFile,
 * FS stands for Fixed Size.
 * @author Craig Macdonald
 * @since 3.0
 * @param <K> Type of the keys
 * @param <V> Type of the values
 */
//unchecked warnings are suppressed because WritableComparable should be parameterised. I have no idea how though.
@SuppressWarnings("rawtypes")
public class FSOrderedMapFile<
        K extends WritableComparable,
        V extends Writable
        > extends ReadOnlyMap<K, V> 
    implements OrderedMap<K,V>, Closeable, SortedMap<K, V>
{
	/** USUAL_EXTENSION */
	public static final String USUAL_EXTENSION = ".fsomapfile";
	
    /** The logger used for this class */
	protected static final Logger logger = LoggerFactory.getLogger(FSOrderedMapFile.class);
	
	/** This is a file lock used to stop multiple threads from attempting to traverse the underlying file at once */
	Object fileAccessLock = new Object();

	/** interface FSOMapFileBSearchShortcut */
    public interface FSOMapFileBSearchShortcut<KEY>
    {
    	/**
    	 * Returns bounds of a given key
    	 * @param key
    	 * @return bounds
    	 * @throws IOException
    	 */
        int[] searchBounds(KEY key) throws IOException;
    }

    interface OrderedMapEntry<K,V> extends Entry<K,V>
    {
        int getIndex();
    }

    
    
    class DefaultMapFileBSearchShortcut<KEY> implements FSOMapFileBSearchShortcut<KEY>
    {
		final int[] defaultBounds = new int[]{0,numberOfEntries};
        public int[] searchBounds(KEY key)
        {
            return defaultBounds;
        }
    }
    /** MapFileInMemory class */
    public static class MapFileInMemory<IK extends Writable,IV extends Writable>
    	extends TreeMap<IK, IV>
    	implements Map<IK,IV>
    {
		private static final long serialVersionUID = 1L;

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		/** constructor
		 * 
		 * @param filename
		 * @param _keyFactory
		 * @param _valueFactory
		 * @throws IOException
		 */
		public MapFileInMemory(String filename, FixedSizeWriteableFactory<IK> _keyFactory, FixedSizeWriteableFactory<IV> _valueFactory)
    		throws IOException
	    {
	    	this(
				new DataInputStream(Files.openFileStream(filename)),
				(int)(Files.length(filename)/( _keyFactory.getSize() + _valueFactory.getSize() )),
				_keyFactory, 
				_valueFactory
				);
	    }

		/** constructor
		 * 
		 * @param dataInputStream
		 * @param length
		 * @param keyfactory
		 * @param valuefactory
		 * @throws IOException
		 */
		public MapFileInMemory(DataInputStream dataInputStream, int length,
				FixedSizeWriteableFactory<IK> keyfactory,
				FixedSizeWriteableFactory<IV> valuefactory) throws IOException
		{
			for(int i=0;i<length;i++)
			{
				IK key = keyfactory.newInstance();
				key.readFields(dataInputStream);
				IV value = valuefactory.newInstance();
				value.readFields(dataInputStream);
				this.put(key, value);
			}
		}  	
    }
    
    /** an iterator for entries. */
    public static class EntryIterator<IK extends Writable,IV extends Writable> 
    	implements Iterator<Entry<IK,IV>>, java.io.Closeable, Skipable
    {
        protected DataInput di;
        protected int numEntries;
        protected int counter = 0;
        protected FixedSizeWriteableFactory<IK> keyFactory;
    	protected FixedSizeWriteableFactory<IV> valueFactory;
        /**
         * constructor
         * @param filename
         * @param _keyFactory
         * @param _valueFactory
         * @throws IOException
         */
        public EntryIterator(String filename, FixedSizeWriteableFactory<IK> _keyFactory, FixedSizeWriteableFactory<IV> _valueFactory)
        	throws IOException
        {
        	this(
    			new DataInputStream(Files.openFileStream(filename)),
    			(int)(Files.length(filename)/( _keyFactory.getSize() + _valueFactory.getSize() )),
    			_keyFactory, 
    			_valueFactory
    			);
        }
       
        EntryIterator(DataInput _di, int _numEntries, FixedSizeWriteableFactory<IK> _keyFactory, FixedSizeWriteableFactory<IV> _valueFactory)
        {
            di = _di;
            numEntries = _numEntries;
            this.keyFactory = _keyFactory;
        	this.valueFactory = _valueFactory;
        }
    	/** 
    	 * {@inheritDoc} 
    	 */
        public void close() throws IOException
        {
        	((Closeable)di).close();
        }
    	/** 
    	 * {@inheritDoc} 
    	 */
        public boolean hasNext()
        {
        	//System.err.println(this.toString()+"check:"+(counter < numEntries)+" counter="+counter + " numEntries="+numEntries);
        	//new Exception().printStackTrace();
            return counter < numEntries;
        }
    	/** 
    	 * {@inheritDoc} 
    	 */
        public Entry<IK,IV> next()
        {
        	//System.err.println(this.toString()+"counter="+counter + " numEntries="+numEntries);
            if (counter >= numEntries)
            {
            	//System.err.println(this.toString()+"ERROR counter="+counter + " numEntries="+numEntries);
                throw new NoSuchElementException();
            }
            IK key = keyFactory.newInstance();
            IV value = valueFactory.newInstance();
            try{
                key.readFields(di);
                value.readFields(di);
                counter++;
            } catch (IOException ioe) {
                logger.error("IOException while iterating", ioe); 
                throw new NoSuchElementException("IOException while iterating");
            }
            if ((counter == numEntries) && di instanceof Closeable)
                try{
                    ((Closeable)di).close();
                } catch (IOException ioe) {
                	logger.error("Could not close input file", ioe);
                }
            return new MapFileEntry<IK,IV>(key,value,counter-1);
        }
    	/** 
    	 * {@inheritDoc} 
    	 */
        public void remove() { throw new UnsupportedOperationException();}
    	/** 
    	 * {@inheritDoc} 
    	 */
		public void skip(int _numEntries) throws IOException {
			if (_numEntries == 0)
				return;
			int entrySize = keyFactory.getSize() + valueFactory.getSize();
			long targetSkipped = (long)_numEntries * (long)entrySize;
			long actualSkipped = 0;
			while(actualSkipped < targetSkipped)
			{
				int toSkip = targetSkipped - actualSkipped > (long)Integer.MAX_VALUE
					? Integer.MAX_VALUE
					: (int)(targetSkipped - actualSkipped);
				actualSkipped += di.skipBytes(toSkip);
			}
			counter += _numEntries;
		}
    }
    
    /** an iterator for entries. */
    class valueIterator implements Iterator<V>, Skipable
    {
        DataInput di;
        int numEntries;
        int count = 0;
        K uselessKey;
       
        valueIterator(DataInput _di, int _numEntries)
        {
            di = _di;
            numEntries = _numEntries;
            uselessKey = keyFactory.newInstance();
        }
        
        public boolean hasNext()
        {
            return count < numEntries;
        }
        
        public V next()
        {
            if (count++ >= numEntries)
                throw new NoSuchElementException();
            V value = valueFactory.newInstance();
            try{
                uselessKey.readFields(di);
                value.readFields(di);
            } catch (IOException ioe) {
                logger.error("IOException while iterating", ioe); 
                throw new NoSuchElementException("IOException while iterating");
            }
            if ((count == numEntries) && di instanceof Closeable)
                try{
                    ((Closeable)di).close();
                } catch (IOException ioe) {}
            return value;
        }
        
        public void remove() { throw new UnsupportedOperationException();}

		@Override
		public void skip(int _numEntries) throws IOException {
			if (_numEntries == 0)
				return;
			int entrySize = keyFactory.getSize() + valueFactory.getSize();
			long targetSkipped = (long)_numEntries * (long)entrySize;
			long actualSkipped = 0;
			while(actualSkipped < targetSkipped)
			{
				int toSkip = targetSkipped - actualSkipped > (long)Integer.MAX_VALUE
					? Integer.MAX_VALUE
					: (int)(targetSkipped - actualSkipped);
				actualSkipped += di.skipBytes(toSkip);
			}
			count += _numEntries;
		}
    }
    
    /** an iterator for entries. */
    class keyIterator implements Iterator<K>, Closeable, Skipable
    {
        DataInput di;
        int numEntries;
        int count = 0;
        V uselessValue;
       
        keyIterator(DataInput _di, int _numEntries)
        {
            di = _di;
            numEntries = _numEntries;
            uselessValue = valueFactory.newInstance();
        }
        
        @Override
		public void skip(int _numEntries) throws IOException {
        	if (_numEntries == 0)
				return;
			int entrySize = keyFactory.getSize() + valueFactory.getSize();
			long targetSkipped = (long)_numEntries * (long)entrySize;
			long actualSkipped = 0;
			while(actualSkipped < targetSkipped)
			{
				int toSkip = targetSkipped - actualSkipped > (long)Integer.MAX_VALUE
					? Integer.MAX_VALUE
					: (int)(targetSkipped - actualSkipped);
				actualSkipped += di.skipBytes(toSkip);
			}
			count += _numEntries;
		}
        
        public boolean hasNext()
        {
            return count < numEntries;
        }
        
        public K next()
        {
            if (count++ >= numEntries)
                throw new NoSuchElementException();
            K key = keyFactory.newInstance();
            try{
                key.readFields(di);
                uselessValue.readFields(di);
            } catch (IOException ioe) {
                logger.error("IOException while iterating", ioe); 
                throw new NoSuchElementException("IOException while iterating");
            }
            if ((count == numEntries) && di instanceof Closeable)
                try{
                    ((Closeable)di).close();
                } catch (IOException ioe) {}
            return key;
        }
        
        public void remove() { throw new UnsupportedOperationException();}
        public void close() throws IOException 
        {
        	if (di instanceof Closeable)
        		((Closeable)di).close();
        }
    }

    
    class MapFileEntrySet extends AbstractSet<Entry<K,V>>
    {
    	int first = 0;
    	int last = numberOfEntries-1;
    	
    	public MapFileEntrySet(){}
    	
    	public MapFileEntrySet(int _first, int _last) {
    		this.first = _first;
    		this.last = _last;
    	}
    	
    	
    	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
    				value="DMI_UNSUPPORTED_METHOD",
    				justification="May be implemented in future release")
        public boolean add(Map.Entry<K,V> e)
        {
            put(e.getKey(), e.getValue());
            return true;
        }
        
        public int size()
        {
            return last - first +1;
        }
        
        public boolean isEmpty()
        {
            return size() == 0;
        }
        
        public Iterator<Map.Entry<K,V>> iterator()
        {
            try{
            	EntryIterator<K,V> ei = new EntryIterator<K,V>(
                    new DataInputStream(Files.openFileStream(dataFilename)),
                    last+1,
                    keyFactory,
                    valueFactory
                    );
            	if (first > 0)
            		ei.skip(first);
            	return ei;
            } catch (IOException ioe) {
                return null;
            }
        }
        
        //@SuppressWarnings("unchecked")
		@SuppressWarnings("unchecked")
		public boolean contains(Object o)
        {
			K key = (K)o;
            if (get(key) == null)
                return false;
            return true;
        }
        
        public boolean remove(Map.Entry<K,V> e)
        {
            remove(e.getKey());
            return true;
        }
        
        public void clear()
        {
            _clear();
        }
    }
    
    class MapFileKeySet extends AbstractSet<K>
    {
    	int first = 0;
    	int last = numberOfEntries-1;
    	
    	public MapFileKeySet()
    	{}
    	
    	public MapFileKeySet(int _first, int _last)
    	{
    		this.first = _first;
    		this.last = _last;
    	}
    	
        public int size()
        {
            return last - first +1;
        }
        
        public boolean isEmpty()
        {
            return size() == 0;
        }
        
        public Iterator<K> iterator()
        {
            try{
            	keyIterator k = new keyIterator(
                    new DataInputStream(Files.openFileStream(dataFilename)),
                    last+1
                );
            	if (first > 0)
            		k.skip(first);
            	return k;
            } catch (IOException ioe) {
                return null;
            }
        }
        
        //@SuppressWarnings("unchecked")
		@SuppressWarnings("unchecked")
		public boolean contains(Object o)
        {
            K key = (K)o;
            if (get(key) == null)
                return false;
            return true;
        }
    }
    
    class SubMap extends ReadOnlyMap<K, V> implements SortedMap<K,V>
	{
		final FSOrderedMapFile<K,V> parent;
		K headKey;
		int headKeyIndex;
		K tailKey;
		int tailKeyIndex;
		
		public SubMap(FSOrderedMapFile<K,V> _parent, K _headKey, K _tailKey)
		{
			this.parent = _parent;
			if (_headKey != null)
			{
				this.headKey = _headKey;
				this.headKeyIndex = parent.getEntry(headKey).index;
				if (this.headKeyIndex < 0)
				{
					this.headKeyIndex = -this.headKeyIndex -1;
				}
			} else {
				this.headKeyIndex = 0;
				this.headKey = parent.get(this.headKeyIndex).getKey();
			}
			if (_tailKey != null)
			{
				this.tailKey = _tailKey;
				this.tailKeyIndex = parent.getEntry(tailKey).index;
				if (this.tailKeyIndex < 0)
				{
					this.tailKeyIndex = -this.tailKeyIndex -2;
					this.tailKey = parent.get(this.tailKeyIndex).getKey();
				}
				else
				{
					this.tailKeyIndex = this.tailKeyIndex -1;
					this.tailKey = parent.get(this.tailKeyIndex).getKey();
				}
			}
			else
			{
				this.tailKeyIndex = parent.size() -1;
				assert this.tailKeyIndex >= 0;
				this.tailKey = parent.get(this.tailKeyIndex).getKey();
			}
//			System.err.println("sumap: " + this.headKeyIndex +"("+parent.get(headKeyIndex)+")" + " - " + this.tailKeyIndex +"("+parent.get(tailKeyIndex)+")");
		}
		
		public SubMap(FSOrderedMapFile<K,V> _parent, K _headKey, int _headKeyIndex, K _tailKey, int _tailKeyIndex)
		{
			this.parent = _parent;
			this.headKey = _headKey;
			this.headKeyIndex = _headKeyIndex;
			this.tailKey = _tailKey;
			this.tailKeyIndex = _tailKeyIndex;
		}
		
		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean containsKey(Object k) {
			if (headKey != null && (headKey.compareTo(k) > 0))
			{
				//System.err.println("testKey " + k.toString() + " is too early compared to headkey " + headKey.toString());
				return false;
			}
			if (tailKey != null && (tailKey.compareTo(k) < 0))
			{
				//System.err.println("testKey " + k.toString() + " is too late compared to tailkey " + tailKey.toString());
				return false;
			}
			//System.err.println("checking parent for " + k.toString());
			return parent.containsKey(k);
		}

		@Override
		public boolean containsValue(Object arg0) {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public V get(Object k) {
			if (headKey != null && (headKey.compareTo(k) > 0))
			{
				//System.err.println("testKey " + arg0.toString() + " is too early compared to headkey " + headKey.toString());
				return null;
			}
			if (tailKey != null && (tailKey.compareTo(k) < 0))
			{
				//System.err.println("testKey " + arg0.toString() + " is too late compared to tailkey " + tailKey.toString());
				return null;
			}
			//System.err.println("checking parent for " + arg0.toString());
			return parent.get(k);
		}

		@Override
		public boolean isEmpty() {
			return headKeyIndex < tailKeyIndex;
		}

		@Override
		public V put(K arg0, V arg1) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V remove(Object arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			return this.tailKeyIndex - this.headKeyIndex +1 ;
		}

		@Override
		public Comparator<? super K> comparator() {
			return null;
		}

		@Override
		public Set<java.util.Map.Entry<K, V>> entrySet() {
			return new MapFileEntrySet(this.headKeyIndex, this.tailKeyIndex);
		}

		@Override
		public K firstKey() {
			return parent.get(headKeyIndex).getKey();
		}
		

		@Override
		public Set<K> keySet() {
			return new MapFileKeySet(this.headKeyIndex, this.tailKeyIndex);
		}

		@Override
		public K lastKey() {
			return parent.get(tailKeyIndex).getKey();
		}

		
		@Override
		public SortedMap<K, V> headMap(K to) {
			return new SubMap(parent, headKey, to);
		}
		
		@Override
		public SortedMap<K, V> subMap(K from, K to) {
			return new SubMap(parent, from, to);
		}

		@Override
		public SortedMap<K, V> tailMap(K from) {
			return new SubMap(parent, from, tailKey);
		}

		@Override
		public Collection<V> values() {
			return new MapFileValueCollection(headKeyIndex, tailKeyIndex);
		}
		
	}

    static class MapFileEntry<EK,EV> extends MapEntry<EK,EV> implements OrderedMapEntry<EK,EV>
    {
        int index;
        MapFileEntry(EK _key, EV _value, int _index)
        {
            super(_key, _value);
            index = _index;
        }
                
        public int getIndex()
        {
            return index;
        }
        
        public EV setValue(EV value)
        {            
            //TODO why does this cause exception?
            //put(this.key, value);
            return null;
        }

		@Override
		public boolean equals(Object o) {
			return super.equals(o);
		}

		@Override
		public int hashCode() {
			return index;
		}
        
        
    }
    
    class MapFileValueCollection
        extends AbstractCollection<V>
        implements Collection<V>
    {
    	int first = 0;
    	int last = numberOfEntries-1;
    	
    	public MapFileValueCollection() {}
    	
    	public MapFileValueCollection(int _first, int _last) {
    		this.first = _first;
    		this.last = _last;
    	}
    	
        public int size()
        {
            return last - first +1;
        }
        
        public Iterator<V> iterator()
        {
            try{
            	valueIterator v = new valueIterator(
            			new DataInputStream(Files.openFileStream(dataFilename)),
            			last+1);
            	if (first > 0)
            		v.skip(first);
            	return v;
            } catch (IOException ioe) {
                logger.error("Problem reading FSOrderedMapFile "+dataFilename+" as stream", ioe);
                return null;
            }
        }
    }

    /** actual underlying data file */
	protected RandomDataInput dataFile = null;
	/** filename of the underlying file */
	protected String dataFilename;
	
	/** The number of entries in the file.*/
	protected int numberOfEntries;
	/** total size of one key,value pair */
	protected int entrySize;
	
	protected FSOMapFileBSearchShortcut<K> shortcut;
	
	protected FixedSizeWriteableFactory<K> keyFactory;
	protected FixedSizeWriteableFactory<V> valueFactory;
	
	protected RandomDataOutput write()
	{
	   if (! (dataFile instanceof RandomDataOutput))
	       throw new UnsupportedOperationException();
	   return (RandomDataOutput)dataFile;
	}
	/** Return number of entries
	 * 
	 * @param filename
	 * @param _keyFactory
	 * @param _valueFactory
	 * @return number of entries
	 */
	public static int numberOfEntries(
			String filename, 
			FixedSizeWriteableFactory<?> _keyFactory,
            FixedSizeWriteableFactory<?> _valueFactory)
	{
		long length = Files.length(filename);
		long entrySize = _keyFactory.getSize() + _valueFactory.getSize();
		return (int)(length/entrySize);
	}
	/**
	 * constructor
	 * @param index
	 * @param structureName
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public FSOrderedMapFile(IndexOnDisk index, String structureName) throws IOException
	{
		this(
				index.getPath() + "/" + index.getPrefix() + "." + structureName + FSOrderedMapFile.USUAL_EXTENSION,
				false,
				(FixedSizeWriteableFactory<K>)index.getIndexStructure(structureName + "-keyfactory"),
				(FixedSizeWriteableFactory<V>)index.getIndexStructure(structureName + "-valuefactory")
				);
	}
	
	/** Construct a new object to access the underlying file data structure
	 * 
	 * @param filename Filename of the file containing the structure
	 * @param updateable Whether the file can be updated in this JVM
	 * @param _keyFactory factory object for keys
	 * @param _valueFactory factory object for values
	 * @throws IOException thrown if an IO problem occurs
	 */
    public FSOrderedMapFile(
            String filename,
            boolean updateable,
            FixedSizeWriteableFactory<K> _keyFactory,
            FixedSizeWriteableFactory<V> _valueFactory)
        throws IOException
    {
        this.dataFile = updateable
            ? Files.writeFileRandom(this.dataFilename = filename)
            : Files.openFileRandom(this.dataFilename = filename);
        this.keyFactory = _keyFactory;
        this.valueFactory = _valueFactory;
        this.entrySize = _keyFactory.getSize() + _valueFactory.getSize();
        //System.err.println("FSOrderedMapFile entrySize is "+ this.entrySize);
        this.numberOfEntries = (int) (dataFile.length() / (long)entrySize);  
        this.shortcut = new DefaultMapFileBSearchShortcut<K>();
    }
    /**
     * constructor
     * @param file
     * @param filename
     * @param _keyFactory
     * @param _valueFactory
     * @throws IOException
     */
    public FSOrderedMapFile(RandomDataInput file, String filename, FixedSizeWriteableFactory<K> _keyFactory,
            FixedSizeWriteableFactory<V> _valueFactory)
    	throws IOException
    {
    	this.dataFile = file;
    	this.dataFilename = filename;
    	this.keyFactory = _keyFactory;
	    this.valueFactory = _valueFactory;
	    this.entrySize = _keyFactory.getSize() + _valueFactory.getSize();
	    this.numberOfEntries = (int) (dataFile.length() / (long)entrySize);  
	    this.shortcut = new DefaultMapFileBSearchShortcut<K>();
    }
	/** 
	 * Get the key factory 
	 */
    public WriteableFactory<K> getKeyFactory() {
    	return this.keyFactory;
    }
	/** 
	 * Get the value factory
	 */
    public WriteableFactory<V> getValueFactory() {
    	return this.valueFactory;
    }
    
    
    
    /** Remove all entries from this map */
	public void clear() {
	    _clear();
	}
	
	/** 
	 * {@inheritDoc} 
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DMI_UNSUPPORTED_METHOD", justification = "May be implemented in future release")
	public void putAll(Map<? extends K,? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
			put(e.getKey(), e.getValue()); 
	}
	
	//renamed so that inner classes can access
    protected void _clear()
    {
        RandomDataOutput _dataFile = write();
        try{
            _dataFile.setLength(0);
            numberOfEntries = 0;
        } catch (IOException ioe) {
            logger.warn("Could not clear FSOrderedMapFile", ioe);
        }
    }
	/** 
	 * {@inheritDoc} 
	 */
    public Set<Entry<K,V>> entrySet()
    {
        return new MapFileEntrySet();
    }
	/** 
	 * {@inheritDoc} 
	 */
    public Set<K> keySet()
    {
        return new MapFileKeySet();
    }
	/** 
	 * {@inheritDoc} 
	 */
    public Collection<V> values()
    {
        return new MapFileValueCollection();
    }
    
    /** Returns the number of entries in this map */
    public int size()
    {
        return numberOfEntries;
    }
	/** 
	 * {@inheritDoc} 
	 */
    public boolean containsValue(Object o)
    {
        throw new UnsupportedOperationException();
    }
	/** 
	 * {@inheritDoc} 
	 */
    @SuppressWarnings("unchecked")
	public boolean containsKey(Object o)
    {
        return getEntry((K)o).index >= 0;
    }
	/** 
	 * {@inheritDoc} 
	 */
    public boolean isEmpty()
    {
        return numberOfEntries == 0;
    }
	/** 
	 * Set the FSOMapFileBSearchShortcut
	 */
    public void setBSearchShortcut(FSOMapFileBSearchShortcut<K> _shortcut)
    {
        this.shortcut = _shortcut;
    }
    
    /** this method is the one which does the actual disk lookup of entries.
     * If an entry is not found, then a MapFileEntry is returned
     * where the index field indicates the (-(insertion point) -1)
     * of the specified key. See also Arrays.binarySearch() */
    @SuppressWarnings("unchecked")
	protected MapFileEntry<K,V> getEntry(K key)
    {
    	synchronized(fileAccessLock) {
    	
    	int[] bounds;
    	try{
    		bounds = shortcut.searchBounds(key);
    	} catch (IOException ioe) {
    		bounds = new int[]{0, numberOfEntries};
    	}
        int low = bounds[0];
		int high = bounds[1];
		
		int i;
		int compareEntry;
		
		K testKey = keyFactory.newInstance();
		V value = valueFactory.newInstance();	
		
		try{
		
			while (low < high) {
			    //System.err.println("high="+high + " low="+low);
			    i = (low + high) >>> 1;
                //System.err.println("i="+i);
                dataFile.seek((long)i * entrySize);
                testKey.readFields(dataFile);
                //System.err.println("Checking "+testKey.toString() + " cmp="+key.compareTo(testKey));
                if ((compareEntry = testKey.compareTo(key))< 0)
                	low = i + 1;
                else if (compareEntry > 0)
                	high = i /*- 1*/;
                else 
                {
                    //read the rest and return the data
                    value.readFields(dataFile);
                    return new MapFileEntry<K,V>(testKey, value, i);
                }
                //System.err.println("high="+high + " low="+low);
            }
        
            if (high == numberOfEntries)
                return new MapFileEntry<K,V>(testKey, null, -(numberOfEntries) -1);
            
            if (high == 0) {
                i = 0;
                dataFile.seek(0);
            } else {
                i = high;
                dataFile.seek((long)high * entrySize);
            }
            testKey.readFields(dataFile);
            value.readFields(dataFile);
        
            if (key.compareTo(testKey) == 0) {
                return new MapFileEntry<K,V>(testKey, value, i);
            }
            return new MapFileEntry<K,V>(testKey, null, -(i) -1);
		} catch (IOException ioe) {
		  logger.error("IOException reading FSOrderedMapFile", ioe);
		  return new MapFileEntry<K,V>(testKey, null, Integer.MIN_VALUE);
		}
    	}
		
    }
    
    
    /** 
	 * {@inheritDoc}
	 */
	@Override
	public K firstKey() {
		return this.get(0).getKey();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public K lastKey() {
		return this.get(this.size() -1).getKey();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public SortedMap<K, V> headMap(K to) {
		return new SubMap(this, null, to);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public SortedMap<K, V> subMap(K from, K to) {
		return new SubMap(this, from, to);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public SortedMap<K, V> tailMap(K from) {
		return new SubMap(this, from, null);
	}

	/** 
	 * {@inheritDoc}
	 * Always returns null, as keys for FSOMapFile are
	 * always Comparable, and their Comparable implementation are
	 * used.
	 */
	@Override
	public final Comparator<? super K> comparator() {
		return null;
	}
    
    
	/** 
	 * {@inheritDoc} 
	 */
    @SuppressWarnings("unchecked")
	public V get(Object _key)
    {
        K key = (K)_key;
        MapFileEntry<K,V> entry = getEntry(key);
        if (entry.index < 0)
            return null;
        return entry.getValue();
    }
	/** 
	 * {@inheritDoc} 
	 */
    public Entry<K,V> get(int entryNumber)
    {
    	synchronized(fileAccessLock) {
    	
        K key = keyFactory.newInstance();
		V value = valueFactory.newInstance();
		if (entryNumber >= numberOfEntries)
		  throw new NoSuchElementException("Entry number "+ entryNumber + " is larger than map size of "+ numberOfEntries);
		
		try{
            dataFile.seek((long)entryNumber * entrySize);
            key.readFields(dataFile);
            value.readFields(dataFile);
        } catch (IOException ioe) {
            throw new NoSuchElementException(
                "IOException reading FSOrderedMapFile for entry number "+ entryNumber +" : "+ioe);
        }
        return new MapFileEntry<K,V>(key, value, entryNumber);
        
    	}
    }
	/** 
	 * {@inheritDoc} 
	 */
    public void close() throws IOException
    {
        dataFile.close();
    }
    
    /** writes an entire map FSOrderedMapFile at once, to the specified filename,
      * and using the data contained in the specified iterator
      */
    public static void mapFileWrite(String filename,
            Iterable<Entry<WritableComparable, Writable>> t) throws IOException
    {
        mapFileWrite(filename, t.iterator());
    }
    /** writes an entire map FSOrderedMapFile at once, to the specified filename,
     * and using the data contained in the specified iterator
     */ 
    public static void mapFileWrite(String filename,
            Iterator<Entry<WritableComparable, Writable>> ti)
        throws IOException
    {
        DataOutputStream out = new DataOutputStream(Files.writeFileStream(filename));
        while (ti.hasNext())
        {   
            Entry<WritableComparable, Writable> e = ti.next();
            e.getKey().write(out);
            e.getValue().write(out);
        }
        out.close();
    }
    
    /** returns a utility class which can be used to write a FSOrderedMapFile. 
     * Input data MUST be sorted by key. */
    public static MapFileWriter mapFileWrite(final String filename)
        throws IOException
    {
        return new MapFileWriter(){
            DataOutputStream out = new DataOutputStream(Files.writeFileStream(filename));            
            public void write(WritableComparable key, Writable value)
                throws IOException
            {
            	//System.err.println("writing key "+ key.toString());
                key.write(out);
                //System.err.println("writing value "+ value.toString());
                value.write(out);
            }
            
            public void close() throws IOException
            {
                out.close();
            }
        };
    }
    
    /** Interface for writing a FSOMapFile */
    static public interface MapFileWriter extends Closeable
    {
    	/** Add this (key, value) tuple to the MapFile */
        void write(WritableComparable key, Writable value)
            throws IOException;
    }
    
    /** Writes out a FSOMapFile, but assumes that input data need not be sorted by key. */
    public static class MultiFSOMapWriter implements MapFileWriter, Flushable
    {
    	final String targetFilename;
    	Map<WritableComparable, Writable> cache;
    	int maxCacheSize;
    	int flushCount = 0;
    	boolean allowdups = false;

    	protected FixedSizeWriteableFactory keyFactory;
    	protected FixedSizeWriteableFactory valueFactory;	
    	
    	/** 
    	 * Constructs an instance of the MultiFSOMapWriter.
    	 * @param filename
    	 * @param numberOfValuesInMemory
    	 * @param _keyFactory
    	 * @param _valueFactory
    	 */
    	public MultiFSOMapWriter(
    			String filename, 
    			int numberOfValuesInMemory, 
    			FixedSizeWriteableFactory _keyFactory, 
    			FixedSizeWriteableFactory _valueFactory)
    	{
    		this.cache = new TreeMap<WritableComparable, Writable>();
    		this.maxCacheSize = numberOfValuesInMemory;
    		this.targetFilename = filename;
    		this.keyFactory = _keyFactory;
    		this.valueFactory = _valueFactory;
    		this.allowdups = false;
    	}
    	
    	/** 
    	 * Constructs an instance of the MultiFSOMapWriter.
    	 * @param filename
    	 * @param numberOfValuesInMemory
    	 * @param _keyFactory
    	 * @param _valueFactory
    	 * @param dupsAllows are duplicates allowed
    	 */
    	public MultiFSOMapWriter(
    			String filename, 
    			int numberOfValuesInMemory, 
    			FixedSizeWriteableFactory _keyFactory, 
    			FixedSizeWriteableFactory _valueFactory, 
    			boolean dupsAllows)
    	{
    		this.cache = new TreeMap<WritableComparable, Writable>();
    		this.maxCacheSize = numberOfValuesInMemory;
    		this.targetFilename = filename;
    		this.keyFactory = _keyFactory;
    		this.valueFactory = _valueFactory;
    		this.allowdups = dupsAllows;
    	}

    	/** {@inheritDoc} */
    	public void write(WritableComparable key, Writable value)
    		throws IOException
    	{
    		cache.put(key, value);
    		if (cache.size() == maxCacheSize)
    		{
    			//logger.info("Max cache size hit ("+maxCacheSize+"), having a flush");
    			flushCache();
    		}
    	}
    	
    	/** {@inheritDoc} */
    	public void flush() throws IOException {
    		logger.info("Flush forced");
			flushCache();
		}

    	protected void flushCache()
    		throws IOException
    	{
    		MapFileWriter writer = FSOrderedMapFile.mapFileWrite(targetFilename + "." + flushCount);
    		for(Map.Entry<WritableComparable,Writable> entry : cache.entrySet())
    		{
    			writer.write(entry.getKey(), entry.getValue());
    		}
    		writer.close();
    		cache.clear();
    		flushCount++;
    	}
    	/** 
    	 * {@inheritDoc} 
    	 */
    	public void close() throws IOException
    	{
    		//logger.info("MultiFSOMapWriter.close called for file "+ targetFilename, new Exception());
    		//If the object is already closed then invoking this method has no effect. 
    		if (cache == null)
    			return;
    		//flush and close the cache
    		if (cache.size() > 0)
    			flushCache();
    		cache = null;
    		//deal with the single flush case
    		if (flushCount == 1)
    		{
    			Files.rename(targetFilename + ".0", targetFilename);
    			return;
    		}
    		
    		int mergeTmp = -1;
    		LinkedList<Integer> mergeTodo = new LinkedList<Integer>();
    		for(int i=0;i<flushCount;i++)
    			mergeTodo.add(i);
    		while(mergeTodo.size() > 1)
    		{
    			int id1 = mergeTodo.removeFirst();
    			int id2 = mergeTodo.removeFirst();
    			String mergeTo;
    			if (mergeTodo.size() == 0)
    			{
    				mergeTo = this.targetFilename;
    			}
    			else
    			{
    				mergeTo = this.targetFilename + "." + mergeTmp;
    				mergeTodo.add(mergeTmp);
    				mergeTmp--;
    			}
    			mergeTwo(id1, id2, mergeTo);
    			Files.delete(this.targetFilename + "." + id1);
    			Files.delete(this.targetFilename + "." + id2);
    		}
    		if (mergeTodo.size() == 1)
    		{
    			 Files.rename(targetFilename + "." + mergeTodo.removeFirst(), this.targetFilename);
    		}
    	}

    	@SuppressWarnings({ "unchecked", "resource"})
		protected void mergeTwo(int id1, int id2, String filename) throws IOException
    	{
    		Iterator<Map.Entry<WritableComparable,Writable>> i1 = new FSOrderedMapFile.EntryIterator<WritableComparable,Writable>(
    			targetFilename + "." + id1, keyFactory, valueFactory);
    		Iterator<Map.Entry<WritableComparable,Writable>> i2 = new FSOrderedMapFile.EntryIterator<WritableComparable,Writable>(
    			targetFilename + "." + id2, keyFactory, valueFactory);	
    		MapFileWriter writer = FSOrderedMapFile.mapFileWrite(filename);
    		boolean hasMore1 = i1.hasNext();
    		boolean hasMore2 = i2.hasNext();
    		Map.Entry<WritableComparable,Writable> e1 = null;
    		Map.Entry<WritableComparable,Writable> e2 = null;
    		if (hasMore1) {
    			e1 = i1.next();
       		}
       		if (hasMore2) {
    			e2 = i2.next();
       		}

    		while(hasMore1 && hasMore2)
    		{
    			int compare = e1.getKey().compareTo(e2.getKey());
    			if(compare < 0)
    			{
    				writer.write(e1.getKey(), e1.getValue());
    				hasMore1 = i1.hasNext();
    				if (hasMore1)
    					e1 = i1.next();	
    			}
    			else if (compare > 0) 
    			{
    				writer.write(e2.getKey(), e2.getValue());
    				hasMore2 = i2.hasNext();
    				if (hasMore2) 
    					e2 = i2.next();
    			}
    			else //compare = 0
    			{
    				if (this.allowdups)
    				{
    					logger.warn("Key "+e1.getKey()+" is not unique: " 
    						+ e2.getValue().toString() + "," 
    						+ e1.getValue().toString() + " - keeping "+e2.getValue().toString());
    					writer.write(e2.getKey(), e2.getValue());
    					hasMore1 = i1.hasNext();
        				if (hasMore1)
        					e1 = i1.next();
        				hasMore2 = i2.hasNext();
        				if (hasMore2) 
        					e2 = i2.next();
    				}
    				else
    				{
    					IndexUtil.close(i1);
    		    		IndexUtil.close(i2);
    					throw new IOException("Key "+e1.getKey()+" is not unique: " 
    						+ e2.getValue().toString() + "," + e1.getValue().toString() + "\n"
    						+ "For MetaIndex, to suppress, set metaindex.compressed.reverse.allow.duplicates=true");
    				}
    			}
    		}
    		while(hasMore1)
    		{
    			writer.write(e1.getKey(), e1.getValue());
    			hasMore1 = i1.hasNext();
    				if (hasMore1)
    					e1 = i1.next();	
    		}
    		while(hasMore2)
    		{
    			writer.write(e2.getKey(), e2.getValue());
    			hasMore2 = i2.hasNext();
    				if (hasMore2)
    					e2 = i2.next();
    		}
    		writer.close();
    		IndexUtil.close(i1);
    		IndexUtil.close(i2);			
    	}

		
    }
}
