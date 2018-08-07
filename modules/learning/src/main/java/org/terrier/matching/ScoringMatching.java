package org.terrier.matching;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;

/** Matching implementation that uses a parent Matching instance to get the docids to work with.
 * Scores are replaced using the specified weighting model. Scoring is done in a DAAT fashion.
 * 
 * @author craigm
 *
 */
public class ScoringMatching extends AbstractScoringMatching {

	Lexicon<String> lexicon;
	PostingIndex<?> invertedIndex;
	CollectionStatistics cs; 
	ResultSet rs_input;
	double[] scores;
	int scored = 0;
	
	public ScoringMatching(Index _index, Matching _parent, WeightingModel _wm, Predicate<Pair<String,Set<String>>> _filter)
	{
		super(_index, _parent, _wm, _filter);
		if (this.index != null)
		{
			this.lexicon = index.getLexicon();
			this.invertedIndex = index.getInvertedIndex();
			this.cs = index.getCollectionStatistics();
		}
	}
	
	public ScoringMatching(Index _index, Matching _parent, WeightingModel _wm)
	{
		super(_index, _parent, _wm);
		if (this.index != null)
		{
			this.lexicon = index.getLexicon();
			this.invertedIndex = index.getInvertedIndex();
			this.cs = index.getCollectionStatistics();
		}
	}
	
	public ScoringMatching(Index _index, Matching _parent)
	{
		super(_index, _parent, null);
		if (this.index != null)
		{
			this.lexicon = index.getLexicon();
			this.invertedIndex = index.getInvertedIndex();
			this.cs = index.getCollectionStatistics();
		}
	}
	
		
	public ResultSet doMatch(String queryNumber, MatchingQueryTerms queryTerms, ResultSet rsInput) throws IOException
	{
		if (this.cs == null)
			this.cs = index.getCollectionStatistics();
		
		rs_input = rsInput;
		final int[] docids = rs_input.getDocids();
		final int docCount = docids.length;
		scores = new double[docCount];
		org.terrier.sorting.HeapSort.heapSort(docids, scores, docCount);
	
		System.err.println("ScoringMatching running for " + wm.getInfo());
		
		//this smells like a hack
		if (super.wm != null)
			queryTerms.forEach( qtPair -> qtPair.getValue().termModels = Arrays.asList(wm));
		
		Iterator<MatchingTerm> iter = queryTerms.iterator();
		while(iter.hasNext())
		{
			MatchingTerm term = iter.next();
			
			//check if this term has been suppressed by the filter
			boolean okToScore = true;
			if (filterTerm != null)
				okToScore = filterTerm.test(Pair.of(term.getKey().toString(),term.getValue().getTags()));
			if (! okToScore)
			{
				System.err.println("Term: "+term.getKey().toString()+" not scored for wm " + wm.getInfo());
				iter.remove();
				continue;
			}			
		}
			
		PostingListManager plm = new PostingListManager(index, this.cs, queryTerms, true, null, null);
		
		plm.prepare(true);
		final int terms = plm.getNumTerms();
		assert(terms > 0);
		String[] qTerms = new String[terms];
		EntryStatistics[] entryStats = new EntryStatistics[terms];
		double[] keyFreqs = new double[terms];
		
		for (int i=0; i<terms; i++) {
			qTerms[i] = plm.getTerm(i);
			entryStats[i] = plm.getStatistics(i).getWritableEntryStatistics();
			keyFreqs[i] = plm.getKeyFrequency(i);
		}
		
		makeResultSet(qTerms, entryStats, keyFreqs);
		
		System.err.println(this.getClass().getSimpleName() + " is rescoring " + docCount + " documents");
		scored = 0;
		int matchingCount = 0;
		for(int i=0;i<docCount;i++)
		{
			final int docid = docids[i];
			final IterablePosting [] matching = new IterablePosting[terms];
			double score = 0;
			boolean anyTermMatch = false;
			for(int t=0;t<terms;t++)
			{
				final IterablePosting ip = plm.getPosting(t);
				//seek ip to next document >= docid
				//if (ip.next(docid) == docid)
				while(ip.getId() < docid)
				{
					if (ip.next() == IterablePosting.EOL)
						break;
				}
				if (ip.getId() == docid)
				{//only if this posting list has a posting for docid					
					//save the posting for this
					matching[t] = ip;					
					anyTermMatch = true;
					score += plm.score(t);
				}
			}
			if (anyTermMatch) {
				matchingCount++;
				assignScore(i, docid, score, matching);
			}		
 		}
		assert matchingCount <= docids.length;	
		System.err.println(this.getClass().getSimpleName() + " for "+this.wm.getInfo()+" on "+terms+" terms, scored " + matchingCount + " of " + docids.length + " retrieved documents docCount="+docCount + " matchingCount="+matchingCount);
		finalise(matchingCount);
		
		plm.close();
		return getFinalResultSet();
	}

	protected void finalise(final int numScored)
	{
		if (numScored == getFinalResultSet().getResultSize())
		{
			getFinalResultSet().sort();
		}
		else
		{
			getFinalResultSet().sort(numScored);
			rs_input = rs_input.getResultSet(0, numScored);
		}
	}

	protected void makeResultSet(String[] qs, EntryStatistics[] es, double[] ks)
	{
		
	}
	
	protected void assignScore(int offset, int docid, double score, IterablePosting[] postings)
	{
		scores[offset] = score;
		if (score != 0.0d)
			scored++;
	}
	
	protected ResultSet getFinalResultSet()
	{
		QueryResultSet rtr = new QueryResultSet(scores.length);
		rtr.docids = rs_input.getDocids();
		rtr.scores = scores;
		return rtr;
		//return rs_input;
	}
	
	@Override
	public String getInfo() {
		return "ScoringMatching";
	}

	@Override
	public void setCollectionStatistics(CollectionStatistics cs) {
		this.cs = cs;
	}

}
