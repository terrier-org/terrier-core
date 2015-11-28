/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is PostingIdComparator.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.postings;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.Serializable;
import java.util.Comparator;

import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.WritableUtils;

/** A comparator object for Posting objects, where they are sorted by id.
 * @since 3.0 */
public class PostingIdComparator implements Comparator<Posting>, RawComparator<Posting>, Serializable
{
	private static final long serialVersionUID = 1L;

	/** Compare Posting objects by id */
	public int compare(Posting o1, Posting o2) {
		return o1.getId() - o2.getId();
	}

	/** Decode Writable postings and compare by id */
	public int compare(byte[] arg0, int arg1, int arg2, byte[] arg3, int arg4, int arg5) {
		try{
			DataInputStream di1 = new DataInputStream(new ByteArrayInputStream(arg0, arg1, arg2));
			DataInputStream di2 = new DataInputStream(new ByteArrayInputStream(arg3, arg4, arg5));
			return WritableUtils.readVInt(di1) - WritableUtils.readVInt(di2);
		} catch (Exception e) {
			return 0;
		}
	}		
}
