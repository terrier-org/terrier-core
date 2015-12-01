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
 * The Original Code is MemoryCollectionStatistics.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime.memory;

import java.io.Serializable;

import org.terrier.structures.CollectionStatistics;

/**
 * This class provides basic statistics in memory for a collection of documents, 
 * such as the average length of documents, or the total number of documents in 
 * the collection.
 * 
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
@SuppressWarnings("serial")
public class MemoryCollectionStatistics extends CollectionStatistics implements Serializable {

	/**
	 * Constructor.
	 */
	public MemoryCollectionStatistics(int numDocs, int numTerms,
			long numTokens, long numPointers, long[] fieldTokens) {
		super(numDocs, numTerms, numTokens, numPointers, fieldTokens);
	}

	/**
	 * Increment doc, token and pointers counters.
	 */
	public void update(int docs, int tokens, int terms) {
		numberOfDocuments += docs;
		numberOfTokens += tokens;
		// FIXME: numberOfPointers
		numberOfPointers += terms;
		relcaluateAverageLengths();
	}

	/**
	 * Increment unique terms.
	 */
	public void updateUniqueTerms(int numTerms) {
		numberOfUniqueTerms = numTerms;
		// FIXME: numberOfPointers?
		// numberOfPointers = numTerms;
	}
	
	/** Increment field tokens. */
    public void updateFields(long[] ftokens) {
        for (int fi = 0; fi < numberOfFields; fi++)
            fieldTokens[fi] += ftokens[fi];
    }
    
    /** Relcaluate average lengths. */
    public void relcaluate() {
        this.relcaluateAverageLengths();
    }
}