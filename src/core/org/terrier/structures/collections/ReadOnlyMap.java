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
 * The Original is in 'ReadOnlyMap.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.structures.collections;

import java.util.Map;

/** A null implementation of java.util.Map that does nothing.
 * @author Craig Macdonald
 * @since 3.0
 * @param <K> Type of the keys
 * @param <V> Type of the values
 */
public abstract class ReadOnlyMap<K,V> implements Map<K,V> {

	public ReadOnlyMap() {
		super();
	}

	/** Remove all entries from this map */
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/** 
	 * {@inheritDoc} 
	 */
	public V put(K key, V value) {
	    throw new UnsupportedOperationException();
	}

	/** 
	 * {@inheritDoc} 
	 */
	public V remove(Object _key) {
	    throw new UnsupportedOperationException();
	}

	

}