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
 * The Original Code is LRUMap.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Rodrygo Santos <rodrygo{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.collections;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**A map with a fixed maximum size. The eldest entry will be removed
 * when the maximum size is reached
 * @author Rodrygo Santos
 *
 * @param <K> type of the key
 * @param <V> type of the parameter
 * @since 3.0
 */
public final class LRUMap<K, V> extends LinkedHashMap<K, V> {

	/** DEFAULT_SIZE */
	public static final int DEFAULT_SIZE = 1000;
	private static final long serialVersionUID = 1L;
	private int maxSize;

	/**
	 * default constructor
	 */
	public LRUMap() {
		this(DEFAULT_SIZE);
	}
	
	/**
	 * constructor
	 * @param sMaxSize
	 */
	public LRUMap(String sMaxSize) {
		this(Integer.parseInt(sMaxSize));
	}
	/**
	 * constructor
	 * @param _maxSize
	 */
	public LRUMap(int _maxSize) {
		super(_maxSize, 0.75f, true);
		this.maxSize = _maxSize;
	}

	protected boolean removeEldestEntry(Entry<K, V> eldest) {
		return size() > maxSize;
	}

}
