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
 * The Original Code is MapEntry.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.collections;

import java.util.Map;
import java.util.Map.Entry;
/** Implementation of java.util.Map.Entry.
 * @since 3.0
 * @param <K> type of the Key
 * @param <V> type of the Value
 */
public class MapEntry<K, V> implements Entry<K, V> {
	protected K key;
	protected V value;
    
    /** Construct a new Entry */
    public MapEntry(K _key, V _value)
    {
        this.key = _key;
        this.value = _value;
    }
    
    /** {@inheritDoc} */
    public K getKey()
    {
        return key;
    }
  
    /** {@inheritDoc} */
    public V getValue()
    {
        return value;
    }

    /** {@inheritDoc} */
	public V setValue(V _value) {
		V oldValue = value;
		value = _value;
		return oldValue;
	}
	
	/** {@inheritDoc} */
	public String toString()
    {
        return "Entry<"+key.toString() + ","+value.toString()+">";
    }
	
	/** Allow the key to be changed */
	public K setKey(K _key)
	{
		K oldKey = key;
		key = _key;
		return oldKey;
	}
	
	/** Checks if another object is equal to this one, i.e.
	 * all keys are equal and all values are equal
	 */
	@SuppressWarnings("unchecked")
	public boolean equals(Object o)
    {
    	if (! (o instanceof Map.Entry))
    		return false;
        Map.Entry<K,V> e1 = this;
        Map.Entry<K,V> e2 = (Map.Entry<K,V>)o;
        return (e1.getKey()==null ?
            e2.getKey()==null : e1.getKey().equals(e2.getKey()))  &&
            (e1.getValue()==null ?
            e2.getValue()==null : e1.getValue().equals(e2.getValue()));
    }
	
	/** Creates a hashcode combining both keys and values. */
	public int hashCode()
    {
        return 
            (getKey()==null   ? 0 : getKey().hashCode()) ^
            (getValue()==null ? 0 : getValue().hashCode());
    }
}
