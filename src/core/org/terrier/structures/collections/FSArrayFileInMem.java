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
 * The Original Code is FSArrayFileInMem.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.collections;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.hadoop.io.Writable;

import org.terrier.structures.collections.FSArrayFile;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.Files;
import org.terrier.utility.io.RandomDataInputMemory;

/** Version of FSArrayFile that keeps the file contents in memory, and decodes the bytes
 * into object as required.
 * @author Craig Macdonald
 * @since 3.0
 * @param <V> Type of Writable
 */
public class FSArrayFileInMem<V extends Writable> extends FSArrayFile<V>
{
	V value;
	/** constructor
	 * 
	 * @param filename
	 * @param updateable
	 * @param factory
	 * @throws IOException
	 */
	public FSArrayFileInMem(String filename, boolean updateable,
			FixedSizeWriteableFactory<V> factory) throws IOException
	{
		super();
		this.valueFactory = factory;
		long len = Files.length(filename);
		if (len > Integer.MAX_VALUE)
			throw new IOException("ArrayFileInMem too big: > Integer.MAX_VALUE");
		byte b[] = new byte[(int)len];
		DataInputStream dis = new DataInputStream(Files.openFileStream(filename));
		dis.readFully(b);
		dis.close();
		this.dataFile = new RandomDataInputMemory(b);
		this.entrySize = factory.getSize();
		this.numberOfEntries = (int)(len / (long)entrySize);
		//System.err.println("document index: "+ this.numberOfEntries + " entries of size "+ entrySize);
		value = factory.newInstance();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public V get(int entryNumber)
	{
		try{
			if (entryNumber > numberOfEntries)
				throw new NoSuchElementException();
			dataFile.seek((long)entryNumber * entrySize);
			value.readFields(dataFile);
			return value;
		} catch (Exception e) {
			throw new NoSuchElementException(e.getMessage());
		}
	}
}