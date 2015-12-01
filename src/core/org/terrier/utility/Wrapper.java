
/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.uk
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
 * The Original Code is RunIteratorFactory.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richardm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.utility;
 
/**
 * Generic mutatble Wrapper class  - allows non-mutatable class
 * to be wrapped in mutatable classes, and re-accessed later.
 * @author Richard McCreadie
 * @param <T> type that is wrapped
 * @since 2.2
 */
public class Wrapper<T> {

	/** IntObjectWrapper class */
	public static class IntObjectWrapper<K> extends Wrapper<K>
	{
		int value;
		/**
		 * default constructor
		 */
		public IntObjectWrapper(){}
		
		/**
		 * constructor
		 * @param v
		 * @param o
		 */
		public IntObjectWrapper(int v, K o)
		{
			super(o);
			value = v;
		}
		/**
		 * get value
		 * @return int
		 */
		public int getInt()
		{
			return value;
		}
		/**
		 * set value
		 * @param v
		 */
		public void setInt(int v)
		{
			value = v;
		}
	}
	
	protected T o;
	/**
	 * default constructor
	 */
	public Wrapper(){
		o=null;
	}
	/**
	 * constructor
	 * @param O
	 */
	public Wrapper(T O) {
		o=O;
	}
	/**
	 * return object
	 * @return object
	 */
	public T getObject() {
		return o;
	}
	/**
	 * set object
	 * @param O
	 */
	public void setObject(T O) {
		o=O;
	}
	/**
	 * get created wrapper
	 * @param O
	 * @return wrapper
	 */
	public Wrapper<T> createWrapper(T O){
		Wrapper<T> tempWrapper = new Wrapper<T>();
		tempWrapper.setObject(O);
		return tempWrapper;
	}
}