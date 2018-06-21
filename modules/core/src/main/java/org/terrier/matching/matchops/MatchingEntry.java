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
 * The Original Code is MatchingEntry.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.matching.matchops;

import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.postings.IterablePosting;

/** A MatchingEntry is the application of a matching Operator to the index at hand, 
 * i.e. with statistics, weighting models, and the posting list operator opened 
 * @since 5.0 
 */
public class MatchingEntry {

	IterablePosting postingIterator;
	EntryStatistics entryStats;
	double keyFreq;
	WeightingModel[] wmodels;
	boolean required;
	String tag;
	
	public MatchingEntry(IterablePosting postingIterator,
			EntryStatistics entryStats, double keyFreq, WeightingModel[] wmodels, boolean required, String tag) {
		super();
		this.postingIterator = postingIterator;
		this.entryStats = entryStats;
		this.keyFreq = keyFreq;
		this.wmodels = wmodels;
		this.required = required;
		this.tag = tag;
	}

	public IterablePosting getPostingIterator() {
		return postingIterator;
	}

	public EntryStatistics getEntryStats() {
		return entryStats;
	}

	public double getKeyFreq() {
		return keyFreq;
	}

	public WeightingModel[] getWmodels() {
		return wmodels;
	}
	
	public boolean getRequired() {
		return required;
	}
	
	public String getTag() {
		return tag;
	}
	
}
