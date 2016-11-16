package org.terrier.matching.indriql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.MatchingQueryTerms.QueryTermProperties;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.FieldOnlyIterablePosting;
import org.terrier.structures.postings.IterablePosting;

public abstract class MultiQueryTerm extends QueryTerm {

	private static final long serialVersionUID = 1L;
	protected static final Logger logger = LoggerFactory.getLogger(MultiQueryTerm.class);
	String[] terms;
	public MultiQueryTerm(String[] ts)
	{
		this.terms = ts;
	}
	
	/** Knows how to merge several EntryStatistics for a single effective term */
	public static EntryStatistics mergeStatistics(EntryStatistics[] entryStats)
	{
		if (entryStats == null)
			return null;
		EntryStatistics rtr = entryStats[0];
		for(int i=1;i<entryStats.length;i++)
			rtr.add(entryStats[i]);
		return rtr;
	}
	
	protected abstract IterablePosting createFinalPostingIterator(
			List<IterablePosting> postings,
			List<LexiconEntry> pointers) throws IOException;
	
	@Override
	public MatchingEntry getMatcher(QueryTermProperties qtp, Index index,
			Lexicon<String> lexicon, PostingIndex<Pointer> invertedIndex,
			CollectionStatistics collectionStatistics) throws IOException 
	{
		
		List<LexiconEntry> _le = new ArrayList<LexiconEntry>(terms.length);
		List<IterablePosting> _joinedPostings = new ArrayList<IterablePosting>(terms.length);
				
		WeightingModel[] wmodels = qtp.termModels.toArray(new WeightingModel[0]);
		if (wmodels.length == 0) {
			logger.warn("No weighting models for multi-term query group "+toString()+" , skipping scoring");
			return null;
		}
		EntryStatistics entryStats = qtp.stats;
		
		int fieldId = -1;
		if (qtp.field != null)
		{
			fieldId = IndexUtil.getFieldId(index, "inverted", qtp.field);
			if (fieldId == -1)
				throw new IOException("Unknown field " + qtp.field);
			//TODO do we correct field stats
		}
		for(String alternative : terms)
		{
			LexiconEntry t = lexicon.getLexiconEntry(alternative);
			if (t == null) {
				logger.debug("Component term Not Found: " + alternative);
			} else if (IGNORE_LOW_IDF_TERMS && collectionStatistics.getNumberOfDocuments() < t.getFrequency()) {
				logger.warn("query term " + alternative + " has low idf - ignored from scoring.");
			} else {
				_le.add(t);
				IterablePosting postingList = invertedIndex.getPostings((Pointer) t);
				if (qtp.field != null)
					postingList = new FieldOnlyIterablePosting(postingList, fieldId);
				_joinedPostings.add(postingList);
			}
		}
		if (_le.size() == 0)
		{
			//TODO consider if we should return an empty posting list iterator instead
			logger.warn("No alternatives matched in " + Arrays.toString(terms));
			return null;
		}
		if (entryStats == null)
			entryStats = mergeStatistics(_le.toArray(new LexiconEntry[_le.size()]));
		if (logger.isDebugEnabled())
			logger.debug("Dijunctive term " + Arrays.toString(terms) + " stats" + entryStats.toString());
		for (WeightingModel w : wmodels)
		{
			w.setEntryStatistics(entryStats);
			w.setKeyFrequency(qtp.weight);
			w.setCollectionStatistics(collectionStatistics);
			IndexUtil.configure(index, w);
			w.prepare();			
		}
		boolean required = false;
		if (qtp.required != null && qtp.required)
			required = true;
		return new MatchingEntry(createFinalPostingIterator(_joinedPostings, _le), entryStats, qtp.weight, wmodels, required);
	}
	
	public MultiQueryTerm clone()
	{
		throw new UnsupportedOperationException(); //TODO
	}
	
}
