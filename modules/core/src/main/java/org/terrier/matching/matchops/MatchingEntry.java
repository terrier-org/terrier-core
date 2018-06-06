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
