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
 * The Original Code is IncrementalFlushDocs.java.
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

/**
 * An IncrementalFlushPolicy that will flush an index to disk after
 * a fixed number of documents have been emitted.
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class IncrementalFlushDocs extends IncrementalFlushPolicy {

	/*
	 * Maximum documents to index before flushing to disk.
	 */
	private int maxDocs;
	private int maxDocCount = 0;

	/**
	 * Get max docs from terrier.properties.
	 */
	public IncrementalFlushDocs(IncrementalIndex index) {
		super(index);
		maxDocs = Integer.parseInt(ApplicationSetup.getProperty(
				"incremental.flushdocs", "1000"));
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
		if (++maxDocCount == maxDocs) {
			maxDocCount = 0;
			return true;
		} else
			return false;
	}
}
