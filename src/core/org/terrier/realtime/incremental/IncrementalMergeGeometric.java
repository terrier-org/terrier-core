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
 * The Original Code is IncrementalMergeGeometric.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime.incremental;


import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.merging.StructureMerger;
import org.terrier.utility.ApplicationSetup;

/**
 * Geometric merge implementation.
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class IncrementalMergeGeometric extends IncrementalMergePolicy implements
		Runnable {
	
	public IncrementalMergeGeometric(IncrementalIndex index) {
		super(index);
	}
	
	private static final Logger logger = LoggerFactory
			.getLogger(IncrementalMergeGeometric.class);

	/*
	 * The geometric parameter.
	 */
	private final int g = Integer.parseInt(ApplicationSetup.getProperty(
			"incremental.geometric", "3"));

	/*
	 * Maintain state about merged partitions.
	 */
	private TIntIntHashMap parts = new TIntIntHashMap(0);
	private TIntIntHashMap sizes = new TIntIntHashMap(0);

	/**
	 * Is merging configured?
	 */
	public boolean mergePolicy() {
		return true;
	}

	/**
	 * Is merging required?
	 */
	public boolean mergeCheck() {
		return true;
	}

	/**
	 * Merge flushed index partitions.
	 */
	public void run() {

		// 1
		if (parts.size() == 0) {
			parts.put(1, index.prefixID - 1);
			sizes.put(1, 1);
			state();
			return;
		} else {
			if (sizes.get(1) < Math.pow(g, 1)) {
				parts.put(1, merge(parts.get(1), index.prefixID - 1));
				sizes.put(1, sizes.get(1) + 1);
				state();
				return;
			}
		}

		// 2
		if (parts.size() == 1) {
			parts.put(2, index.prefixID - 1);
			sizes.put(2, 1);
			state();
			return;
		} else {
			if (sizes.get(2) < Math.pow(g, 2)) {
				parts.put(2, merge(parts.get(2), index.prefixID - 1));
				sizes.put(2, sizes.get(2) + 1);
				state();
				return;
			}
		}

		// 3
		if (parts.size() == 2) {
			parts.put(3, index.prefixID - 1);
			sizes.put(3, 1);
			state();
			return;
		} else {
			if (sizes.get(3) < Math.pow(g, 3)) {
				parts.put(3, merge(parts.get(3), index.prefixID - 1));
				sizes.put(3, sizes.get(3) + 1);
				state();
				return;
			}
		}

		state();
	}

	/*
	 * Merge.
	 */
	private int merge(int partition1, int partition2) {

		// Source index 1.
		IndexOnDisk src1 = Index.createIndex(index.path,
				index.prefix + "-" + partition1);

		// Source index 2.
		IndexOnDisk src2 = Index.createIndex(index.path,
				index.prefix + "-" + partition2);

		// Destination index.
		IndexOnDisk indexD = Index.createNewIndex(index.path,
				index.prefix + "-" + index.prefixID);

		// Merge the index structures.
		StructureMerger merger = new StructureMerger(src1, src2, indexD);
		merger.mergeStructures();

		logger.info("***REALTIME*** IncrementalIndex merged: " + partition1
				+ " and " + partition2 + " into " + index.prefixID);

		// Increment prefix ID.
		index.prefixID++;

		// Update list of indices.
		merged.add(partition1);
		merged.add(partition2);
		purgeMerged();
		synchronized (indices) {
			// FIXME
		}

		// Return prefixID of new partition.
		return index.prefixID - 1;
	}

	/*
	 * Print out state.
	 */
	private void state() {
		logger.debug("***REALTIME*** Geometric merge state");
		logger.debug("Number of columns: " + parts.size());
		TIntIntIterator it = parts.iterator();
		while (it.hasNext()) {
			it.advance();
			logger.debug("Column: " + it.key());
			logger.debug("Column prefixID: " + it.value());
			logger.debug("Column merges: " + sizes.get(it.key()));
			logger.debug("Column merges (max): " + Math.pow(g, it.key()));
		}
	}
}
