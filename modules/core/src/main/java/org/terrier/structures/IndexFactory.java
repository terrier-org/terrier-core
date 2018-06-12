package org.terrier.structures;

import java.io.File;
import java.util.ServiceLoader;

import org.terrier.querying.IndexRef;
import org.terrier.utility.Files;

public class IndexFactory {
	
	static class DirectIndexRef extends IndexRef
	{
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
			if (l.startsWith("http") || l.startsWith("https"))
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
		Iterable<IndexLoader> loaders = ServiceLoader.load(IndexLoader.class);
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
		Iterable<IndexLoader> loaders = ServiceLoader.load(IndexLoader.class);
		for(IndexLoader l : loaders)
		{
			if (l.supports(ref))
				return l.load(ref);
		}
		return null;
	}
	
}
