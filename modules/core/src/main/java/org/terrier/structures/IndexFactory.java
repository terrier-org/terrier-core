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
 * The Original Code is IndexFactory.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures;

import java.io.File;
import java.util.ServiceLoader;

import org.terrier.querying.IndexRef;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

public class IndexFactory {
	
	public static class DirectIndexRef extends IndexRef
	{
		private static final long serialVersionUID = 1L;
		Index underlyingIndex;
		
		DirectIndexRef(Index i)
		{
			super(i.toString());//THIS IS A HACK
			this.underlyingIndex = i;
		}
	}
	
	public static interface IndexLoader
	{
		boolean supports(IndexRef ref);
		Index load(IndexRef ref);
		Class<? extends Index> indexImplementor(IndexRef ref);
	}
	
	public static class DirectIndexLoader implements IndexLoader
	{

		@Override
		public boolean supports(IndexRef ref) {
			return ref instanceof DirectIndexRef;
		}

		@Override
		public Index load(IndexRef ref) {
			return ((DirectIndexRef)ref).underlyingIndex;
		}

		@Override
		public Class<? extends Index> indexImplementor(IndexRef ref) {
			return load(ref).getClass();
		}
		
	}
	
	public static class DiskIndexLoader implements IndexLoader
	{
		@Override
		public boolean supports(IndexRef ref) {
			String l = ref.toString();
			if (ref.size() > 1)
				return false; //this is a multi-index
			if (l.startsWith("http") || l.startsWith("https") || l.startsWith("concurrent"))
				return false;
			if (! l.endsWith(".properties"))
				return false;
			return Files.exists(l);
		}

		@Override
		public Index load(IndexRef ref) {
			String l = ref.toString();
			File file = new File(l);
			String path = file.getParent(); 
			String prefix = file.getName().replace(".properties", "");
			return IndexOnDisk.createIndex(path, prefix);			
		}

		@Override
		public Class<? extends Index> indexImplementor(IndexRef ref) {
			return IndexOnDisk.class;
		}		
	}
	
	public static boolean isLoaded(IndexRef ref) {
		return ref instanceof DirectIndexRef;
	}
	
	public static boolean isLocal(IndexRef ref) {
		String l = ref.toString();
		if (l.startsWith("http") || l.startsWith("https"))
			return false;
		return true;
	}
	
	public static Class<? extends Index> whoSupports(IndexRef ref) {
		Iterable<IndexLoader> loaders = ServiceLoader.load(IndexLoader.class, ApplicationSetup.getClassLoader());
		for(IndexLoader l : loaders)
		{
			if (l.supports(ref))
				return l.indexImplementor(ref);
		}
		return null;
	}
	
	public static Index of(IndexRef ref)
	{
		if (ref instanceof DirectIndexRef)
			return ((DirectIndexRef)ref).underlyingIndex;
//		System.err.println(DirectIndexRef.class.getClassLoader());
//		System.err.println(ApplicationSetup.getClassLoader());
		
		Iterable<IndexLoader> loaders = ServiceLoader.load(IndexLoader.class, ApplicationSetup.getClassLoader());
		for(IndexLoader l : loaders)
		{
			if (l.supports(ref))
				return l.load(ref);
		}
		return null;
	}
	
}
