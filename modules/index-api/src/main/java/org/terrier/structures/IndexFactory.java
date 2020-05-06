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
 * The Original Code is Copyright (C) 2017-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures;

import java.util.ServiceLoader;

import org.terrier.querying.IndexRef;
import org.terrier.structures.Index.DirectIndexRef;

/** This is the main developer API for loading an index.
 * You pass an {@link IndexRef} to the of() static method,
 * and are return the corresponding {@link Index}, if any can be found. 
 * 
 * Different indices are supported: an {@link IndexOnDisk}, 
 * as well as memory indices. An IndexRef can also 
 * target a remote REST server, but no Index can be loaded for 
 * such an IndexRef.
 * 
 * {@link IndexLoader}s are used to load indices; They are found
 * using service discovery. 
 */
public class IndexFactory 
{
    public static ClassLoader cl = null;
    
    public static interface IndexLoader
    {
        /** Does this IndexLoader support this IndexRef */
        boolean supports(IndexRef ref);

        /** Returns the Index for this IndexRef */
        Index load(IndexRef ref);

        /** Returns the class that will implement the Index for the passed reference */
        Class<? extends Index> indexImplementor(IndexRef ref);
    }

    public static class DirectIndexLoader implements IndexLoader
    {
        @Override
        public boolean supports(IndexRef ref) 
        {
            return ref instanceof DirectIndexRef;
        }

        @Override
        public Index load(IndexRef ref) 
        {
            return ((DirectIndexRef)ref).underlyingIndex;
        }

        @Override
        public Class<? extends Index> indexImplementor(IndexRef ref) 
        {
            return load(ref).getClass();
        }
        
    }
    
    /** Is this index already loaded? */
    public static boolean isLoaded(IndexRef ref) 
    {
        return ref instanceof DirectIndexRef;
    }

    private static ClassLoader getClassLoader() 
    {
        return cl == null ? IndexFactory.class.getClassLoader() : cl;
    }
    
    /** Is this a local index, or a remote one? */
    public static boolean isLocal(IndexRef ref) 
    {
        String l = ref.toString();
        if (l.startsWith("http") || l.startsWith("https"))
            return false;
        return true;
    }
    
    public static Class<? extends Index> whoSupports(IndexRef ref) 
    {
        Iterable<IndexLoader> loaders = ServiceLoader.load(IndexLoader.class, getClassLoader());
        for (IndexLoader l : loaders)
            if (l.supports(ref))
                return l.indexImplementor(ref);
        return null;
    }

    /** Shortcut method to avoid instantiating an IndexRef */
	public static Index of(String indexRefString) {
		return of(IndexRef.of(indexRefString));
	}
    
    /** Load the index for the specified reference. */
    public static Index of(IndexRef ref)
    {
        if (ref instanceof DirectIndexRef)
            return ((DirectIndexRef)ref).underlyingIndex;
        // System.err.println(DirectIndexRef.class.getClassLoader());
        // System.err.println(ApplicationSetup.getClassLoader());
        // System.err.println(IndexFactory.class.getClassLoader());
        Iterable<IndexLoader> loaders = ServiceLoader.load(IndexLoader.class, getClassLoader());
        for (IndexLoader l : loaders)
            if (l.supports(ref))
                return l.load(ref);
        return null;
    }
}