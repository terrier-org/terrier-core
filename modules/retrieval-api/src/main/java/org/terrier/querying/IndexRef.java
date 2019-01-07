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
 * The Original Code is IndexRef.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.querying;

import java.io.Serializable;
import java.util.Arrays;

public class IndexRef implements Serializable {
	private static final long serialVersionUID = 1L;
	
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
	public int hashCode() {
		return toString().hashCode();
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
		if (path.startsWith("http"))
			return new IndexRef(path);
		return new IndexRef(path + "/" + prefix + ".properties");
	}
	
}
