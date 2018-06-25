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
 * The Original Code is ConcurrentIndexLoader.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures;

import org.terrier.querying.IndexRef;
import org.terrier.structures.IndexFactory.DirectIndexRef;
import org.terrier.structures.concurrent.ConcurrentIndexUtils;

/** An index loader for index references for indices that we wish to be thread safe */
public class ConcurrentIndexLoader implements IndexFactory.IndexLoader {

	private static final String PREFIX = "concurrent:";

	public static IndexRef makeConcurrent(IndexRef ref) {
		
		if (IndexFactory.isLoaded(ref))
			return new DirectIndexRef( ConcurrentIndexUtils.makeConcurrentForRetrieval( ((DirectIndexRef)ref).underlyingIndex )){
				private static final long serialVersionUID = 1L;

				@Override
				public String toString() {
					return PREFIX + super.toString();
				}
			};
		return IndexRef.of(PREFIX + ref.toString());
	}
	
	public static boolean isConcurrent(IndexRef ref) {
		return ref.toString().startsWith(PREFIX);
	}
	
	@Override
	public boolean supports(IndexRef ref) {
		return isConcurrent(ref) || IndexFactory.isLoaded(ref);
	}

	@Override
	public Index load(IndexRef ref) {
		if (! supports(ref))
			throw new IllegalArgumentException(ref.toString() + " not supported by " + this.getClass().getSimpleName());
		
		Index index;
		if (IndexFactory.isLoaded(ref))
		{
			System.err.println(ref +" is a directindexref, making sure its concurrent");
			index = IndexFactory.of(ref);
		}
		else
		{
			System.err.println("loading indexref " + ref + " to make it concurrent");
			index = IndexFactory.of(getUnderlyingRef(ref));
		}
		if (index == null)
			return null;
		ConcurrentIndexUtils.makeConcurrentForRetrieval(index);
		return index;
	}

	protected IndexRef getUnderlyingRef(IndexRef ref) {
		String underlyinglocation = ref.toString().replaceFirst(PREFIX, "");
		return IndexRef.of(underlyinglocation);
	}

	@Override
	public Class<? extends Index> indexImplementor(IndexRef ref) {
		return IndexFactory.whoSupports(getUnderlyingRef(ref));
	}

}
