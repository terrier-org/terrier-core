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
 * The Original Code is IncrementalMergePolicy.java.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.Index;

/**
 * A policy for merging different indices together on disk
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class IncrementalMergePolicy {
	private static final Logger logger = LoggerFactory
			.getLogger(IncrementalMergePolicy.class);

	IncrementalIndex index;
	
	public IncrementalMergePolicy(IncrementalIndex index) {
		this.index = index;
	}
	
	/*
	 * List of indices to merge. (Reference to list in MultiIndex).
	 */
	protected static List<Index> indices;

	/*
	 * List of merged indices.
	 */
	protected static List<Integer> merged = new ArrayList<Integer>();

	/**
	 * Create a new merge thread.
	 */
	public static IncrementalMergePolicy get(String policy, List<Index> indices, IncrementalIndex index) {
		IncrementalMergePolicy.indices = indices;
		if (policy.equals("single"))
			return new IncrementalMergeSingle(index);
		if (policy.equals("geometric"))
			return new IncrementalMergeGeometric(index);
		return new IncrementalMergePolicy(index);
	}

	/**
	 * Delete indices which have been merged.
	 */
	public void purgeMerged() {
		// FIXME
		logger.info("***REALTIME*** IncrementalIndex delete merged partitions:");
		for (int index : merged)
			logger.info(String.valueOf(index));
	}

	/**
	 * No merge.
	 */
	public boolean mergePolicy() {
		return false;
	}

	/**
	 * Never merge.
	 */
	public boolean mergeCheck() {
		return false;
	}
}
