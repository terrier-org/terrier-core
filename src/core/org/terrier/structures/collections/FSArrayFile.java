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
 * The Original Code is FSArrayFile.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.collections;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.hadoop.io.Writable;

import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.Skipable;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.Files;
import org.terrier.utility.io.RandomDataInput;

/** A file for accessing Writable classes written on disk. These must be of fixed size.
 * This implementation is read-only, but does implement the List interface.
 * @author Craig Macdonald
 * @since 3.0
 * @param <V> Type of Writable
 */
public class FSArrayFile<V extends Writable> extends AbstractList<V> implements Closeable
{
	/** USUAL_EXTENSION */
	public static final String USUAL_EXTENSION = ".fsarrayfile";
	protected FixedSizeWriteableFactory<V> valueFactory;
	
	/** The number of entries in the file.*/
	protected int numberOfEntries;
	/** total size of one key,value pair */
	protected int entrySize;
	/** actual underlying data file */
	protected RandomDataInput dataFile = null;
	/** filename of the underlying file */
	protected String dataFilename;
	
	protected FSArrayFile() {}
	/** constructor
	 * 
	 * @param index
	 * @param structureName
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public FSArrayFile(IndexOnDisk index, String structureName) throws IOException
	{
		this(
				index.getPath() + "/" + index.getPrefix() + "." + structureName + FSArrayFile.USUAL_EXTENSION,
				false,
				(FixedSizeWriteableFactory<V>)index.getIndexStructure(structureName + "-factory")
				);
	}
	/** default constructor
	 * 
	 * @param filename
	 * @param updateable
	 * @param _valueFactory
	 * @throws IOException
	 */
	public FSArrayFile(
            String filename,
            boolean updateable,
            FixedSizeWriteableFactory<V> _valueFactory)
        throws IOException
    {
        this.dataFile = updateable
            ? Files.writeFileRandom(this.dataFilename = filename)
            : Files.openFileRandom(this.dataFilename = filename);
        this.valueFactory = _valueFactory;
        this.entrySize = _valueFactory.getSize();
        this.numberOfEntries = (int) (dataFile.length() / (long)entrySize);  
    }
	
	@Override
	public int size()
	{
		return numberOfEntries;
	}
	
	@Override
	public V get(int entryNumber)
	{
		try{
			V value = valueFactory.newInstance();
			if (entryNumber > numberOfEntries)
			  throw new NoSuchElementException("Entry too big : " + entryNumber + " > " + numberOfEntries);
			dataFile.seek((long)entryNumber * entrySize);
			value.readFields(dataFile);
			return value;
		} catch (NoSuchElementException nsee) {
			throw nsee;
		} catch (Exception e) {
			throw new NoSuchElementException("For entry number "+entryNumber +" : " + e);
		}
	}
	
	@Override
	public Iterator<V> iterator() {
		try{
			return new ArrayFileIterator<V>(this.dataFilename, this.valueFactory);
		} catch (IOException ioe) {
			throw new Error(ioe);
		}
	}
	/** interface ArrayFileWriter */
	public interface ArrayFileWriter extends java.io.Closeable {
		/** write 
		 * 
		 * @param w
		 * @throws IOException
		 */
		void write(Writable w) throws IOException;
	}
	/** writeFSArrayFile
	 * 
	 * @param filename
	 * @return fixed size array file
	 * @throws IOException
	 */
	public static ArrayFileWriter writeFSArrayFile(String filename) throws IOException
	{
		final DataOutputStream dos = new DataOutputStream(Files.writeFileStream(filename));
		return new ArrayFileWriter() {

			public void write(Writable w) throws IOException {
				w.write(dos);
			}

			public void close() throws IOException {
				dos.close();
			}
		};	
	}
	
	/** ArrayFileIterator class
	 *
	 * @param <V>
	 */
	public static class ArrayFileIterator<V extends Writable> implements Iterator<V>, Closeable, Skipable
	{
		FixedSizeWriteableFactory<V> valueFactory;
		DataInputStream dis;
		int numberOfEntries;
		int count =0;
		/** constructor
		 * 
		 * @param index
		 * @param structureName
		 * @throws IOException
		 */
		@SuppressWarnings("unchecked")
		public ArrayFileIterator(IndexOnDisk index, String structureName) throws IOException
		{
			this(
					index.getPath() + "/" + index.getPrefix() + "." + structureName + FSArrayFile.USUAL_EXTENSION,
					(FixedSizeWriteableFactory<V>)index.getIndexStructure(structureName + "-factory")
					);
		}
		/** constructor
		 * 
		 * @param filename
		 * @param _valueFactory
		 * @throws IOException
		 */
		public ArrayFileIterator(String filename, FixedSizeWriteableFactory<V> _valueFactory) throws IOException
		{
			valueFactory = _valueFactory;
			dis = new DataInputStream(Files.openFileStream(filename));
			numberOfEntries = (int) (Files.length(filename) / ((long) valueFactory.getSize()));
		}
		/** 
		 * {@inheritDoc} 
		 */
		public void skip(int numEntries) throws IOException
		{
			final long targetSkip = numEntries * ((long) valueFactory.getSize());
			long skipped = 0;
			do{
				skipped += dis.skip(targetSkip);
			} while(skipped < targetSkip);
			count += numEntries;
		}
		/** 
		 * {@inheritDoc} 
		 */
		public boolean hasNext() {
			return count < numberOfEntries;
		}
		/** 
		 * {@inheritDoc} 
		 */
		public V next() {
			count++;
			V value = valueFactory.newInstance();
			try{
				value.readFields(dis);
			}catch (IOException ioe) {
				throw new NoSuchElementException("IOException in ArrayFileIterator while reading element "+count+" of "+numberOfEntries+" : " + ioe);
			}
			return value;
		}
		/** 
		 * {@inheritDoc} 
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
		/** 
		 * {@inheritDoc} 
		 */
		public void close() {
			try{
				dis.close();
			}catch (IOException ioe) {
				
			}
		}
		
	}
	@Override
	public void close() throws IOException {
		this.dataFile.close();
		
	}
	
}