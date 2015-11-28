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
 * The Original Code is IncrementalDeletePolicy.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime.incremental;

import java.util.ArrayList;
import java.util.List;

import org.terrier.structures.Index;

/** This class represents a policy for deleting index shards during a flush.
 * This is useful if you want to want to discard older index shards to avoid
 * the index growing infinitely.
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class IncrementalDeletePolicy {

	public static IncrementalDeletePolicy get(String policy) {
		if (policy.equals("deleteFixedSize"))
			return new IncrementalDeletePolicy();
		else return new IncrementalDeletePolicy();
	}
	
	public IncrementalDeletePolicy() {}
	
	/**
	 * No delete.
	 */
	public boolean deletePolicy() {
		return false;
	}
	
	/**
	 * Get a list of indices to delete (if any)
	 */
	protected List<Integer> getIndicesToDelete(List<Index> indices) {
		List<Integer> indicesToDelete = new ArrayList<Integer>(0);
		return indicesToDelete;
	}
	
	public void runPolicy(List<Index> indices) {
		// Run the delete policy
		List<Integer> indicesToDelete = getIndicesToDelete(indices);
		if (indicesToDelete.size()>0) {
			synchronized (indices) {
				int numDeleted = 0;
				for (Integer i : indicesToDelete) {
					indices.remove(i-numDeleted);
					numDeleted++;
				}
			}
		}
	}
}
