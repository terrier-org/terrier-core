package org.terrier.querying;

import java.util.Arrays;

public class IndexRef {

	String[] location;
	
	protected IndexRef(String _location) {
		this.location = new String[]{_location};
	}
	
	protected IndexRef(String[] _location) {
		this.location = _location;
	}
	
	public int size()
	{
		return location.length;
	}
	
	@Override
	public String toString() {
		if (location.length == 1)
			return location[0];
		return Arrays.toString(location);
	}
	
	public static IndexRef of(String location){
		return new IndexRef(location);
	}
	
	@Deprecated
	/** This is NOT intended for long term use. */
	public static IndexRef of(String path, String prefix){
		return new IndexRef(path + "/" + prefix + ".properties");
	}
	
}
