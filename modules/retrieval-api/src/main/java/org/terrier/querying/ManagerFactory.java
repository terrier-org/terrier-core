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
 * The Original Code is ManagerFactory.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.querying;

import java.util.ServiceLoader;

public class ManagerFactory {
	
	/** interface for Builders of Managers */
	public static interface Builder{
		boolean supports(IndexRef ref);
		Manager fromIndex(IndexRef ref);
	}

	/** utility method that doesnt conflict with a Python reserved keyword */
	public static Manager _from_(IndexRef ref) { return from(ref); }
	
	/** Load a manager suitable to retrieve from the specified index reference */
	public static Manager from(IndexRef ref)
	{
		Iterable<Builder> iter = ServiceLoader.load(Builder.class, IndexRef.class.getClassLoader());
		boolean any = false;
		for(Builder b : iter)
		{
			any = true;
			if (b.supports(ref))
				return b.fromIndex(ref);
		}
		if (! any)
			throw new UnsupportedOperationException("No Manager implementations found. Do you need to import terrer-core or terrier-rest-client?");
		throw new IllegalArgumentException("No Manager implementation found for index " 
			+ ref.toString() + " (" +ref.getClass().getSimpleName()+ ") - Do you need to import another package (terrer-core or terrier-rest-client)? "
			+"Or perhaps the index location is wrong. Found builders were " + seenList(IndexRef.class.getClassLoader()));
	}
	
	static String seenList(ClassLoader clzLoader)
	{
		StringBuilder s = new StringBuilder();
		Iterable<Builder> iter = ServiceLoader.load(Builder.class, clzLoader);
		for(Builder b : iter)
		{
			s.append(b.getClass().getName());
			s.append(",");
		}
		s.setLength(s.length()-1);
		return s.toString();
	}
	
	
}
