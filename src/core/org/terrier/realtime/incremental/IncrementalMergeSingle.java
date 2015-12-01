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
 * The Original Code is IncrementalMergeSingle.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime.incremental;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.merging.StructureMerger;

/**
 * Merge flushed index partitions into a single partition.
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class IncrementalMergeSingle extends IncrementalMergePolicy implements
		Runnable {
	private static final Logger logger = LoggerFactory
			.getLogger(IncrementalMergeSingle.class);

	public IncrementalMergeSingle(IncrementalIndex index) {
		super(index);
	}
	
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
		return indices.size() > 2;
	}

	/** Merge flushed index partitions into a single partition. */
	public void run() {

		// Partitions to merge.
		int partition1 = index.prefixID - 2;
		int partition2 = index.prefixID - 1;

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
			indices.remove(0);
			indices.remove(0);
			indices.add(0, indexD);
		}
	}
}
