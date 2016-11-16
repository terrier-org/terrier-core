package org.terrier.matching.indriql;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.MatchingQueryTerms;
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

public class SingleQueryTerm extends QueryTerm {

	private static final long serialVersionUID = 1L;
	
	
	protected static final Logger logger = LoggerFactory.getLogger(SingleQueryTerm.class);
	
	String queryTerm;
	
	public SingleQueryTerm(String t)
	{
		queryTerm = t;
	}
	
	public String getTerm() {
		return queryTerm;
	}
	
	public void setTerm(String newTerm) {
		queryTerm = newTerm;
	}
	
	@Override
	public String toString() {
		return queryTerm;
	}
	
	public SingleQueryTerm clone()
	{
		SingleQueryTerm rtr = (SingleQueryTerm) super.clone();
		rtr.queryTerm = queryTerm; //strings are immutable
		return rtr;
	}

	@Override
	public MatchingEntry getMatcher(
			MatchingQueryTerms.QueryTermProperties qtp,
			Index index,
			Lexicon<String> lexicon,
			PostingIndex<Pointer> invertedIndex, 
			CollectionStatistics collectionStatistics) throws IOException {
		
		WeightingModel[] wmodels = qtp.termModels.toArray(new WeightingModel[0]);
		EntryStatistics entryStats = qtp.stats;
		LexiconEntry t = lexicon.getLexiconEntry(queryTerm);
		
		if (t == null) {
			logger.debug("Term Not Found: " + queryTerm);
			//previousTerm = false;	
			return null;
		} else if (IGNORE_LOW_IDF_TERMS && collectionStatistics.getNumberOfDocuments() < t.getFrequency()) {
			logger.warn("query term " + queryTerm + " has low idf - ignored from scoring.");
			//previousTerm = false;
			return null;
		} else if (wmodels.length == 0) {
			logger.warn("No weighting models for term " + queryTerm +", skipping scoring");
			//previousTerm = false;
			return null;
		} else {
			
			IterablePosting postingList = invertedIndex.getPostings((Pointer) t);
			if (qtp.field != null)
			{
				int fieldId = IndexUtil.getFieldId(index, "inverted", qtp.field);
				if (fieldId == -1)
					throw new IOException("Unknown field " + qtp.field);
				postingList = new FieldOnlyIterablePosting(postingList, fieldId);
				//TODO do we correct field stats
			}
			
			if (entryStats == null)
				entryStats = t;
			for (WeightingModel w : wmodels)
			{
				w.setEntryStatistics(entryStats);
				w.setKeyFrequency(qtp.weight);
				w.setCollectionStatistics(collectionStatistics);
				IndexUtil.configure(index, w);
				w.prepare();			
			}
			
			if (logger.isDebugEnabled())
				logger.debug("Term " + queryTerm + " field "+qtp.field+" stats " + entryStats.toString() + " weight " + qtp.weight);
			
			boolean required = false;
			if (qtp.required != null && qtp.required)
				required = true;
			
			return new MatchingEntry(postingList, 
					entryStats, qtp.weight, wmodels, required);
		}
	}

}
