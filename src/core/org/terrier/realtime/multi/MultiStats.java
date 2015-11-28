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
 * The Original Code is MultiStats.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.multi;

import org.terrier.structures.CollectionStatistics;

/**
 *  A collection statistics class for use with a MultiIndex. This class
 *  aggregates the statistics from each index shard within the MultiIndex.
 *  
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
@SuppressWarnings("serial")
public class MultiStats extends CollectionStatistics {

	/**
	 * Constructor.
	 */
	public static MultiStats factory(CollectionStatistics[] stats) {
		int numDocs = 0, numTerms = 0;
		long numTokens = 0, numPointers = 0;
		long[] fieldTokens = new long[] { 0 };
		for (CollectionStatistics stat : stats) {
			numDocs += stat.getNumberOfDocuments();
			numTokens += stat.getNumberOfTokens();
			numPointers += stat.getNumberOfPointers();
			if (stat.getNumberOfUniqueTerms() > numTerms)
				numTerms = stat.getNumberOfUniqueTerms();
		}

		return new MultiStats(numDocs, numTerms, numTokens, numPointers,
				fieldTokens);
	}

	/*
	 * Private constructor.
	 */
	private MultiStats(int numDocs, int numTerms, long numTokens,
			long numPointers, long[] fieldTokens) {
		super(numDocs, numTerms, numTokens, numPointers, fieldTokens);
	}
}
