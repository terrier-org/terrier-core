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
 * The Original Code is WTreeMap.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.terrier.structures.collections.MapEntry;
import org.terrier.structures.collections.OrderedMap;

/**
 * Wrapper around TreeMap implementing OrderedMap.
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
@SuppressWarnings("serial")
public class WTreeMap<K, V> extends TreeMap<K, V> implements OrderedMap<K, V> {

	@SuppressWarnings("unused")
	private List<K> ordering = new ArrayList<K>();

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	public java.util.Map.Entry<K, V> get(int index) {
		//from the JDK documentation of keySet() The set's iterator returns the keys in ascending order.
		final K _key = (K) super.keySet().toArray()[index];
		return new MapEntry<K, V>(_key, super.get(_key));
	}
}