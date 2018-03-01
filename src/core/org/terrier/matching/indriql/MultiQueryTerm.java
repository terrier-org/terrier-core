package org.terrier.matching.indriql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.MatchingQueryTerms.QueryTermProperties;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Lexicon;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;

public abstract class MultiQueryTerm extends QueryTerm {

	private static final long serialVersionUID = 1L;
	protected static final Logger logger = LoggerFactory.getLogger(MultiQueryTerm.class);
	QueryTerm[] terms;
	public MultiQueryTerm(String[] ts)
	{
		this(getSingleTerms(ts));	
	}
	
	public MultiQueryTerm(QueryTerm[] _ts)
	{
		this.terms = _ts;
	}
	
	public QueryTerm[] getConstituents() {
		return terms;
	}
	
	static QueryTerm[] getSingleTerms(String[] ts) {
		 QueryTerm[] rtr = new QueryTerm[ts.length];
		 for(int i=0;i<ts.length;i++)
			 rtr[i] = new SingleQueryTerm(ts[i]);
		 return rtr;
	}
	
	/** merges several EntryStatistics for a single effective term simply by adding */
	public static EntryStatistics addStatistics(EntryStatistics[] entryStats)
	{
		if (entryStats == null)
			return null;
		EntryStatistics rtr = entryStats[0];
		for(int i=1;i<entryStats.length;i++)
			rtr.add(entryStats[i]);
		return rtr;
	}
	
	protected EntryStatistics mergeStatistics(EntryStatistics[] entryStats)
	{
		return addStatistics(entryStats);
	}
	
	protected abstract IterablePosting createFinalPostingIterator(
			List<IterablePosting> postings,
			List<EntryStatistics> pointers) throws IOException;
	
	@Override
	public Pair<EntryStatistics,IterablePosting> getPostingIterator(Index index) throws IOException
	{
		List<EntryStatistics> _le = new ArrayList<EntryStatistics>(terms.length);
		List<IterablePosting> _joinedPostings = new ArrayList<IterablePosting>(terms.length);
		for(QueryTerm ts : terms) {
			Pair<EntryStatistics,IterablePosting> pair = ts.getPostingIterator(index);
			if (pair == null || pair.getLeft() == null)
			{
				logger.debug("Component term Not Found: " + ts);
			} else if (IGNORE_LOW_IDF_TERMS && index.getCollectionStatistics().getNumberOfDocuments() < pair.getKey().getFrequency()) {
				logger.warn("query term " + ts + " has low idf - ignored from scoring.");
			} else {
				//assert pair.getLeft() != null : "query term " + ts + " has null entrystatistics?";
				_le.add(pair.getLeft());
				_joinedPostings.add(pair.getRight());
			}			
		}
		
		if (_le.size() == 0)
		{
			//TODO consider if we should return an empty posting list iterator instead
			logger.warn("No alternatives matched in " + Arrays.toString(terms));
			return null;
		}
		EntryStatistics entryStats = mergeStatistics(_le.toArray(new EntryStatistics[_le.size()]));
		
		IterablePosting ip = createFinalPostingIterator(_joinedPostings, _le);
		return Pair.of(entryStats, ip);
	}
	
	@Override
	public MatchingEntry getMatcher(QueryTermProperties qtp, Index index,
			Lexicon<String> lexicon, PostingIndex<Pointer> invertedIndex,
			CollectionStatistics collectionStatistics) throws IOException 
	{
		WeightingModel[] wmodels = qtp.termModels.toArray(new WeightingModel[0]);
		if (wmodels.length == 0) {
			logger.warn("No weighting models for multi-term query group "+toString()+" , skipping scoring");
			return null;
		}
		EntryStatistics entryStats = qtp.stats;
		
		Pair<EntryStatistics,IterablePosting> pair = this.getPostingIterator(index);
		if (pair == null)
			return null;
		
		if (entryStats == null)
			qtp.stats = entryStats = pair.getKey();
		if (logger.isDebugEnabled())
			logger.debug(this.getClass().getSimpleName() + " term " + Arrays.toString(terms) + " stats " + entryStats.toString() + " weight " + qtp.weight);
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
		return new MatchingEntry(pair.getRight(), entryStats, qtp.weight, wmodels, required, qtp.tag);
	}
	
	public MultiQueryTerm clone()
	{
		throw new UnsupportedOperationException(); //TODO
	}
	
}
