package org.terrier.matching.indriql;

import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.postings.IterablePosting;


public class MatchingEntry {

	IterablePosting postingIterator;
	EntryStatistics entryStats;
	double keyFreq;
	WeightingModel[] wmodels;
	boolean required;
	
	public MatchingEntry(IterablePosting postingIterator,
			EntryStatistics entryStats, double keyFreq, WeightingModel[] wmodels, boolean required) {
		super();
		this.postingIterator = postingIterator;
		this.entryStats = entryStats;
		this.keyFreq = keyFreq;
		this.wmodels = wmodels;
		this.required = required;
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
	
}
