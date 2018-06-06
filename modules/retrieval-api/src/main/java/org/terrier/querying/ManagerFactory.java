package org.terrier.querying;

import java.util.ServiceLoader;

public class ManagerFactory {
	
	public static interface Builder{
		boolean supports(IndexRef ref);
		Manager fromIndex(IndexRef ref);
	}
	
	public static Manager from(IndexRef ref)
	{
		Iterable<Builder> iter = ServiceLoader.load(Builder.class);
		boolean any = false;
		for(Builder b : iter)
		{
			any = true;
			if (b.supports(ref))
				return b.fromIndex(ref);
		}
		if (! any)
			throw new UnsupportedOperationException("No Manager implementations found. Do you need to import terrer-core?");
		throw new IllegalArgumentException("No Manager implementation found for index " 
			+ ref.toString() + " - Do  you need to import another package");
	}
	
	
}
