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
 * The Original Code is IncrementalFlushMemory.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime.incremental;

import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.RuntimeMemoryChecker;

/**
 * An IncrementalFlushPolicy that will flush an index to disk after
 * a memory-used threshold has been reached.
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class IncrementalFlushMemory extends IncrementalFlushPolicy {

	/*
	 * Maximum memory to use before flushing to disk.
	 */
	private double maxMem;

	/**
	 * Get max memory from terrier.properties.
	 */
	public IncrementalFlushMemory(IncrementalIndex index) {
		super(index);
		maxMem = Double.parseDouble(ApplicationSetup.getProperty(
				"incremental.flushmemory", "0.70"));
	}

	/**
	 * Is flushing configured?
	 */
	public boolean flushPolicy() {
		return true;
	}

	/**
	 * Is flushing required?
	 */
	public boolean flushCheck() {
		return new RuntimeMemoryChecker(
				ApplicationSetup.MEMORY_THRESHOLD_SINGLEPASS, maxMem)
				.checkMemory();
	}
}