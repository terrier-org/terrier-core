package org.terrier.querying;

import java.util.ServiceLoader;

public class ManagerFactory {
	
	/** interface for Builders of Managers */
	public static interface Builder{
		boolean supports(IndexRef ref);
		Manager fromIndex(IndexRef ref);
	}
	
	/** Load a manager suitable to retrieve from the specified index reference */
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
			+ ref.toString() + " (" +ref.getClass().getSimpleName()+ ") - Do you need to import another package? Or perhaps the index location is wrong. Found builders were " + seenList());
	}
	
	static String seenList()
	{
		StringBuilder s = new StringBuilder();
		Iterable<Builder> iter = ServiceLoader.load(Builder.class);
		for(Builder b : iter)
		{
			s.append(b.getClass().getName());
			s.append(",");
		}
		s.setLength(s.length()-1);
		return s.toString();
	}
	
	
}
