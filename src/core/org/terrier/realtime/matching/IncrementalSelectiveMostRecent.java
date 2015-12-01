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
 * The Original Code is IncrementalSelectiveMostRecent.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime.matching;

import java.util.ArrayList;
import java.util.List;

import org.terrier.structures.Index;
import org.terrier.utility.ApplicationSetup;
/**
 * This class enables search over a subset of the shards in an incremental index.
 * This is useful if the shard boundaries are relevant, e.g. represent time boundaries.
 * 
 * <p><b>Properties</b></p>
 * <ul><li><tt>incremental.selectivematch.mostrecent</tt> - number of indices to select, defaults to 6</li></ul>
 * 
 * @author Richard McCreadie
 * @since 4.0
 */
public class IncrementalSelectiveMostRecent extends IncrementalSelectiveMatching {
	
	int maxNumberOfIndices;
	
	/**
	 * Returns only the incremental.selectivematch.mostrecent indices. (default: 6 most recent)
	 */
	public IncrementalSelectiveMostRecent() {
		super();
	}
	
	/**
	 * Returns only the incremental.selectivematch.mostrecent indices. (default: 6 most recent)
	 * <tt>incremental.selectivematch.mostrecent</tt> is checked during each call such that it can be updated
	 * on the fly.
	 * @param indices
	 * @return only the N most indices, as defined by the <tt>incremental.selectivematch.mostrecent</tt> property
	 */
	public List<Index> getSelectedIndices(List<Index> indices) {
		maxNumberOfIndices = Integer.parseInt(ApplicationSetup.getProperty("incremental.selectivematch.mostrecent", "6"));
		if (indices.size()<=maxNumberOfIndices) return indices;
		else {
			List<Index> selectedIndices = new ArrayList<Index>();
			for (int i = indices.size()-1; selectedIndices.size()<maxNumberOfIndices; i--) {
				selectedIndices.add(indices.get(i));
			}
			return selectedIndices;
		}
	}

}
