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
			System.err.println("loading indexref " + ref + "to make it concurrent");
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
