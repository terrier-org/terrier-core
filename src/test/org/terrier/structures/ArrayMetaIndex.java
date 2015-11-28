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
 * The Original Code is ArrayMetaIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures;

import java.io.IOException;

/** A simple MetaIndex that is backed by an array specified at construction time. Useful
 * for unit tests, etc.
 * @author Craig Macdonald
 * @since 3.5
 */
public class ArrayMetaIndex implements MetaIndex {

	protected final String[] meta;
	
	public ArrayMetaIndex(String[] _meta)
	{
		meta = _meta;
	}
	
	@Override
	public String[] getAllItems(int docid) throws IOException {
		return new String[]{meta[docid]};
	}

	@Override
	public int getDocument(String key, String value) throws IOException {
		int i=0;
		for(String row : meta)
		{
			if (value.equals(row))
				return i;				
			i++;
		}
		return -1;
	}

	@Override
	public String getItem(String Key, int docid) throws IOException {
		return meta[docid];
	}

	@Override
	public String[] getItems(String Key, int[] docids) throws IOException {
		String[] rtr = new String[docids.length];
		for(int i=0;i<docids.length;i++)
		{
			rtr[i] = meta[docids[i]];
		}
		return rtr;
	}

	@Override
	public String[] getItems(String[] keys, int docid) throws IOException {
		return new String[]{ getItem(keys[0], docid) };
	}

	@Override
	public String[][] getItems(String[] Keys, int[] docids) throws IOException {
		String[][] rtr = new String[docids.length][];
		for(int j=0;j<docids.length;j++)
		{
			rtr[j] = getItems(Keys, docids[j]);
		}
		return rtr;
	}

	@Override
	public String[] getKeys() {
		return new String[]{"docno"};
	}

	@Override
	public void close() throws IOException {}

}
