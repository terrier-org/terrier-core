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
 * The Original Code is IncrementalFlushPolicy.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime.incremental;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.realtime.memory.MemoryIndex;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;

/**
 * Policy for flushing out documents in an index to disks
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class IncrementalFlushPolicy implements Runnable {
	private static final Logger logger = LoggerFactory
			.getLogger(IncrementalFlushPolicy.class);
	
	IncrementalIndex index;
	
	public IncrementalFlushPolicy(IncrementalIndex index) {
		this.index = index;
	}

	/*
	 * List of indices to flush. (Reference to list in MultiIndex).
	 */
	private static List<Index> indices;
	
	/**
	 * Create a new flush thread.
	 */
	public static IncrementalFlushPolicy get(String policy, List<Index> indices, IncrementalIndex index) {
		IncrementalFlushPolicy.indices = indices;
		if (policy.equals("flushdocs"))
			return new IncrementalFlushDocs(index);
		if (policy.equals("flushmem"))
			return new IncrementalFlushMemory(index);
		if (policy.equals("flushtime"))
			return new IncrementalFlushTime(index);
		return new IncrementalFlushPolicy(index);
	}

	/**
	 * No flush.
	 */
	public boolean flushPolicy() {
		return false;
	}

	/**
	 * Never flush.
	 */
	public boolean flushCheck() {
		return false;
	}

	/**
	 * Flush contents of in-memory index to disk.
	 */
	public void run() {

		// Index prefix and prefix ID.
		String partition = index.prefix + "-"
				+ index.prefixID;

		// Write in-memory index to disk.
		// List position indices.size()-2.
		try {
			((MemoryIndex) indices.get(indices.size() - 2)).write(
					index.path, partition);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Increment prefix ID.
		index.prefixID++;
		
		// Update list of indices (replace memory with the disk index).
		IndexOnDisk indexOnDisk = (IndexOnDisk) Index.createIndex(
				index.path, partition);
		synchronized (indices) {
			indices.set(indices.size() - 2, indexOnDisk);
		}

		logger.info("***REALTIME*** IncrementalIndex flushed: " + partition);
	}
}
