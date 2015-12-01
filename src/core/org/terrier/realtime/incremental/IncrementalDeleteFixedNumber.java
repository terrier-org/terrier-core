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
 * The Original Code is IncrementalDeleteFixedNumber.java.
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
import org.terrier.utility.ApplicationSetup;

/**
 * Within an incremental index, this is a possible policy for deleting old index shards.
 * We will only hold a fixed number of index shards at any one time.
 * Always drop the oldest one.
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class IncrementalDeleteFixedNumber extends IncrementalDeletePolicy {

	int maxNumberOfIndices;
	
	public IncrementalDeleteFixedNumber() {
		super();
		maxNumberOfIndices = Integer.parseInt(ApplicationSetup.getProperty("incremental.delete.mostrecent", "24"));
	}
	
	/**
	 * Get a list of indices to delete (if any)
	 */
	protected List<Integer> getIndicesToDelete(List<Index> indices) {
		List<Integer> indicesToDelete = new ArrayList<Integer>();
		int numToDelete = 0;
		if (indices.size()>maxNumberOfIndices) numToDelete=indices.size()-maxNumberOfIndices;
		for (int i =0; i<numToDelete; i++) {
			indicesToDelete.add(i);
		}
		return indicesToDelete;
	}
	
}
