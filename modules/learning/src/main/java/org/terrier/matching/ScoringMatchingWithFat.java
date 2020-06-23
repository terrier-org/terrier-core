package org.terrier.matching;

import org.terrier.structures.*;
import org.terrier.structures.postings.*;
import org.terrier.matching.models.WeightingModel;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.commons.lang3.tuple.Pair;


public class ScoringMatchingWithFat extends ScoringMatching {

    boolean[] fields;
	final int fieldCount;
	WritablePosting[][] postingsCache;
	String[] qs; 
	EntryStatistics[] es; 
	double[] ks;
	Set<String>[] tags;

    public ScoringMatchingWithFat(Index _index, Matching _parent, WeightingModel _wm, Predicate<Pair<String,Set<String>>> _filter)
	{
        super(_index, _parent, _wm, _filter);
        fieldCount = super.index.getCollectionStatistics().getNumberOfFields(); 
	}
	
	public ScoringMatchingWithFat(Index _index, Matching _parent, WeightingModel _wm)
	{
        super(_index, _parent, _wm);
        fieldCount = super.index.getCollectionStatistics().getNumberOfFields(); 
	}
	
	public ScoringMatchingWithFat(Index _index, Matching _parent)
	{
        super(_index, _parent, null);
        fieldCount = super.index.getCollectionStatistics().getNumberOfFields();     
    }
    
    @Override
	public String getInfo() {
		return "ScoringMatchingWithFat";
	}

	protected void makeResultSet(int docCount, String[] qs, EntryStatistics[] es, double[] ks, Set<String>[] tags)
	{
		super.makeResultSet(docCount, qs, es, ks, tags);
		fields = new boolean[es.length];
		for(int i=0;i<es.length;i++) {
			fields[i] = es[i] instanceof FieldEntryStatistics;
		}
		this.qs = qs;
		this.es = es;
		this.ks = ks;
		this.tags = tags;
		this.postingsCache = new WritablePosting[docCount][];
	}

	protected void assignScore(int offset, int docid, double score, IterablePosting[] postings)
	{
		super.assignScore(offset, docid, score, postings);
		final int termCount = postings.length;
		WritablePosting[] termPostings = new WritablePosting[termCount];
		for(int i=0;i<termCount;i++)
		{
			if (postings[i] == null)
				continue;
			final Posting p = postings[i];
			final WritablePosting wp = postings[i].asWritablePosting();

			wp.setDocumentLength(p.getDocumentLength());
			if (fields[i])
			{
				final int[] fieldLengths =  ((FieldPosting)p).getFieldLengths();
				final int[] newFieldLengths = new int[fieldCount];
				System.arraycopy(fieldLengths, 0, newFieldLengths, 0, fieldCount);
				assert fieldLengths.length == super.cs.getNumberOfFields() 
					: " posting "+p +" for docid " + p.getId() + " has wrong number of fields for length";
				((FieldPosting)wp).setFieldLengths(newFieldLengths);
			}
			termPostings[i] = wp;
		}
		postingsCache[offset] = termPostings;
	}

	protected ResultSet getFinalResultSet()
	{
		FatQueryResultSet rtr = new FatQueryResultSet(
			scores.length,
			super.cs,
			this.qs,
			this.es,
			this.ks,
			this.tags
			);
		rtr.postings = postingsCache;
		rtr.docids = docids;
		rtr.scores = scores;
		return rtr;
	}
}