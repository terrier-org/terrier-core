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
 * The Original Code is PostingListManager.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Nicola Tonellotto (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   
 */
package org.terrier.matching;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.matchops.MatchingEntry;
import org.terrier.matching.matchops.Operator;
import org.terrier.matching.models.WeightingModel;
import org.terrier.querying.Request;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;

/** The PostingListManager is responsible for opening the appropriate posting lists {@link IterablePosting} given
 * the MatchingQueryTerms object. Moreover, it knows how each Posting should be scored.
 * <p>
 * Plugins are also supported by PostingListManager. Each plugin class should implement the PostingListManagerPlugin
 * interface, and be named explicitly in the <tt>matching.postinglist.manager.plugins</tt> property.
 * <p><b>Properties:</b>
 * <ul>
 * <li> <tt>ignore.low.idf.terms</tt> - should terms with low IDF (i.e. very frequent) be ignored? Defaults to false, i.e. ignored</li>
 * <li> <tt>matching.postinglist.manager.plugins</tt> - Comma delimited list of PostingListManagerPlugin classes to load.</li>
 * </ul>
 * <p><b>Example Usage</b></p>
 * Following code shows how term-at-a-time matching may occur using the PostingListManager:
 * <pre> 
 * MatchingQueryTerms mqt;
 * Index index;
 * PostingListManager plm = new PostingListManager(index, index.getCollectionStatistics(), mqt);
 * plm.prepare(false);
 * for(int term = 0;term &gt; plm.size(); term++)
 * {
 *   IterablePosting ip = plm.get(term);
 *   while(ip.next() != IterablePosting.EOL)
 *   {
 *     double score = plm.score(term);
 *     int id = ip.getId();
 *   }
 * }
 * plm.close();
 * </pre>
 * @author Nicola Tonellotto and Craig Macdonald
 * @since 3.5
 * @see org.terrier.matching.Matching
 */
public class PostingListManager implements Closeable
{
	static class WeightingModelMultiProxy extends WeightingModel {

		static WeightingModel getModel(WeightingModel[] _parents)
		{
			if (_parents.length == 1)
				return _parents[0];
			return new WeightingModelMultiProxy(_parents);
		}
		
		static WeightingModel getModel(List<WeightingModel> _parents)
		{
			if (_parents.size() == 1)
				return _parents.get(0);
			return new WeightingModelMultiProxy(_parents.toArray(new WeightingModel[_parents.size()]));
		}
		
		private static final long serialVersionUID = 1L;
		WeightingModel[] parents;
		
		WeightingModelMultiProxy(WeightingModel[] _parents)
		{
			this.parents = _parents;
		}
		
		@Override
		public String getInfo() {
			StringBuilder s = new StringBuilder();
			for(WeightingModel w : parents)
			{
				s.append(w.getInfo());
				s.append(",");
			}
			s.setLength(s.length() -1);
			return s.toString();
		}		

		@Override
		public void prepare() {
			for(WeightingModel w : parents)
			{
				w.prepare();
			}
		}

		@Override
		public void setCollectionStatistics(CollectionStatistics _cs) {
			for(WeightingModel w : parents)
			{
				w.setCollectionStatistics(_cs);
			}
		}

		@Override
		public void setEntryStatistics(EntryStatistics _es) {
			for(WeightingModel w : parents)
			{
				w.setEntryStatistics(_es);
			}
		}

		@Override
		public void setRequest(Request _rq) {
			for(WeightingModel w : parents)
			{
				w.setRequest(_rq);
			}
		}

		@Override
		public void setParameter(double _c) {
			for(WeightingModel w : parents)
			{
				w.setParameter(_c);
			}
		}

		@Override
		public void setKeyFrequency(double keyFreq) {
			for(WeightingModel w : parents)
			{
				w.setKeyFrequency(keyFreq);
			}
		}

		@Override
		public double score(Posting p) {
			double score = 0;
			for(WeightingModel w : parents)
			{
				score += w.score(p);
			}
			return score;
		}

		@Override
		public double score(double tf, double docLength) {
			double score = 0;
			for(WeightingModel w : parents)
			{
				score += w.score(tf, docLength);
			}
			return score;
		}
	}
	
	protected static final Logger logger = LoggerFactory.getLogger(PostingListManager.class);
	/** A property that enables to ignore the terms with a low IDF. Controlled by <tt>ignore.low.idf.terms</tt>
	 * property, defualts to false. */
	protected static boolean IGNORE_LOW_IDF_TERMS = Boolean.parseBoolean(ApplicationSetup.getProperty("ignore.low.idf.terms","false"));
	
	
	/** Interface for plugins to further alter the posting lists managed by the PostingListManager */
	public static interface PostingListManagerPlugin
	{
		/** process the query, given matchign query terms, index and posting list manager */
		void processQuery(MatchingQueryTerms mqt, Index index, PostingListManager plm);
	}
	
	protected static PostingListManagerPlugin[] plugins;
	
	static {
		String[] pluginNames = ArrayUtils.parseCommaDelimitedString(ApplicationSetup.getProperty("matching.postinglist.manager.plugins", ""));
		List<PostingListManagerPlugin> _plugins = new ArrayList<PostingListManagerPlugin>();
		for (String pluginClass : pluginNames)
		{
			if (! pluginClass.contains("."))
				pluginClass = "org.terrier.matching." + pluginClass;
			try {
				PostingListManagerPlugin p = ApplicationSetup.getClass(pluginClass).asSubclass(PostingListManagerPlugin.class).newInstance();
				_plugins.add(p);
			} catch (Exception e) {
				logger.warn("Problem loading PostingListManagerPlugin called "+ pluginClass, e);
			}
		}
		plugins = _plugins.toArray(new PostingListManagerPlugin[ _plugins.size() ]);
	}
	
	/** posting lists for each term */
	protected final List<IterablePosting> termPostings = new ArrayList<>();
	/** weighting models for each term */
	protected final List<WeightingModel> termModels = new ArrayList<>();
	/** EntryStatistics for each term */
	protected final List<EntryStatistics> termStatistics = new ArrayList<>();
	/** String form for each term */
	protected final List<String> termStrings = new ArrayList<>();
	
	/** String form for each term */
	protected final List<Set<String>> termTags = new ArrayList<>();
	
	
	protected final TIntArrayList matchOnTerms = new TIntArrayList();
	protected final TIntArrayList nonMatchOnTerms = new TIntArrayList();
	
	/** key (query) frequencies for each term */
	protected final TDoubleArrayList termKeyFreqs = new TDoubleArrayList();
	
	/** number of terms */
	protected int numTerms = 0;
	/** underlying index */
	protected Index index;
	/** lexicon for the index */
	protected Lexicon<String> lexicon;
	/** inverted index of the index */
	protected PostingIndex<Pointer> invertedIndex;
	/** statistics of the collection */
	protected CollectionStatistics collectionStatistics;
	/** which terms are positively required to match in retrieved documents */
	protected long requiredBitMask = 0;
	protected long negRequiredBitMask = 0;

	
	/** Create a posting list manager for the given index and statistics */
	@SuppressWarnings("unchecked")
	protected PostingListManager(Index _index, CollectionStatistics cs) throws IOException
	{
		index = _index;
		lexicon = index.getLexicon();
		invertedIndex = (PostingIndex<Pointer>) index.getInvertedIndex();
		collectionStatistics = cs;
	}
	
	
	/** Create a posting list manager for the given index and statistics, and populated using the specified
	 * MatchingQueryTerms.
	 * @param _index - index to obtain postings from
	 * @param _cs - collection statistics to obtain 
	 * @param mqt - MatchingQueryTerms object calculated for the query
	 */
	public PostingListManager(Index _index, CollectionStatistics _cs, MatchingQueryTerms mqt) throws IOException 
	{
		this(_index, _cs, mqt, true, BaseMatching.BASE_MATCHING_TAG, BaseMatching.NONMATCHING_TAG);
	}
	
	/** Create a posting list manager for the given index and statistics, and populated using the specified
	 * MatchingQueryTerms.
	 * @param _index - index to obtain postings from
	 * @param _cs - collection statistics to obtain 
	 * @param mqt - MatchingQueryTerms object calculated for the query
	 * @param splitSynonyms - allows the splitting of synonym groups (i.e. singleTermAlternatives) to be disabled
	 */
	public PostingListManager(Index _index, CollectionStatistics _cs, MatchingQueryTerms mqt, boolean splitSynonyms, String scoringTag, String additionalTag) throws IOException
	{
		this(_index, _cs);
		
		int termIndex = -1;
		
		for(Map.Entry<Operator, MatchingQueryTerms.QueryTermProperties> entry : mqt)
		{
			termIndex++;
			Operator term = entry.getKey();
			if (splitSynonyms)
			{
				MatchingEntry me = term.getMatcher(
					entry.getValue(), 
					index, 
					lexicon, 
					invertedIndex, 
					collectionStatistics);
				if (me == null)
					continue;
				termStrings.add(term.toString());
				termKeyFreqs.add(me.getKeyFreq());
				termPostings.add(me.getPostingIterator());
				termStatistics.add(me.getEntryStats());
				termModels.add(WeightingModelMultiProxy.getModel(me.getWmodels()));
				termTags.add(me.getTags());
				if (me.isRequired())
				{
					requiredBitMask |= 1 << termIndex;
					if (termIndex >= 64)
					{
						logger.warn("A requirement was found for the "+termIndex+"-th query term (" 
							+ term.toString() + "), which was past the maximum supported 64");
					}
				}
				if (me.isNegRequired())
				{
					negRequiredBitMask |= 1 << termIndex;
					if (termIndex >= 64)
					{
						logger.warn("A negative requirement was found for the "+termIndex+"-th query term (" 
							+ term.toString() + "), which was past the maximum supported 64");
					}
				}
				if (me.getTags().size() == 0 && scoringTag != null ) {
					logger.warn(me + " did not contain any tags, it will not be matched!");
				}
				if (scoringTag == null  || me.getTags().contains(scoringTag))
				{
					matchOnTerms.add(termPostings.size() -1);
				}
				else if (me.getTags().size() > 1 && me.getTags().contains(additionalTag))
				{
					nonMatchOnTerms.add(termPostings.size() -1);
				}
			} else {
				//this provides support for Fat indices	
				LexiconEntry le = _index.getLexicon().getLexiconEntry(entry.getKey().toString());
				if (le == null)
					continue;
				termPostings.add(_index.getInvertedIndex().getPostings(le));
				termStatistics.add(entry.getValue().stats != null ? entry.getValue().stats : le);	
				termKeyFreqs.add(entry.getValue().weight);
				termStrings.add(term.toString());
				termTags.add(entry.getValue().getTags());
				termModels.add(WeightingModelMultiProxy.getModel(new WeightingModel[0]));
				if (scoringTag == null || entry.getValue().getTags().size() == 0 || entry.getValue().getTags().contains(scoringTag))
				{
					matchOnTerms.add(termPostings.size() -1);
				}
				else if (entry.getValue().getTags().size() > 1 && entry.getValue().getTags().contains(additionalTag))
				{
					nonMatchOnTerms.add(termPostings.size() -1);
				}
			}
		}
		
		//TR-472 Request not passed to the WeightingModel
		for(WeightingModel wmodel : termModels)
			wmodel.setRequest(mqt.getRequest());
		
		for(PostingListManagerPlugin p : plugins)
		{
			p.processQuery(mqt, index, this);
		}
		logger.info("Query " + mqt.getQueryId() + " with "+ mqt.getMatchingTerms().length +" terms has " + termPostings.size() + " posting lists");
		assert termPostings.size() == termStatistics.size();
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
	
	/** Counts the number of terms active. If firstMove is true,
	 * it will move each posting to the first posting.
	 * @param firstMove move all postings to the start?
	 * @throws IOException
	 */
	public void prepare(boolean firstMove) throws IOException
	{		
		for(IterablePosting ip : termPostings)
		{
			numTerms++;
			if (firstMove)
				ip.next();
		}
	}
	
	/** Returns the EntryStatistics corresponding to the specified term 
	 * @param i term to obtain statistics for
	 * @return Statistics for this i-1th term
	 */
	public EntryStatistics getStatistics(int i)
	{
		return termStatistics.get(i);
	}
	
	/** Returns the IterablePosting corresponding to the specified term 
	 * @param i term to obtain the posting list for
	 * @return Posting list for this i-1th term
	 */
	public IterablePosting getPosting(int i)
	{
		return termPostings.get(i);
	}	

	/** Returns the number of posting lists for this query */
	public int size()
	{
		return numTerms ;
	}
	
	/** Returns the number of postings lists (that are terms) for this query */
	public int getNumTerms()
	{
		return numTerms;
	}
	
	/** Returns the indices of the terms that are considered (i.e. scored) during matching */
	public int[] getMatchingTerms()
	{
		return matchOnTerms.toNativeArray();
	}
	
	/** Returns the indices of the terms that must be called through assignScore() but not actually used to match documents. */
	public int[] getNonMatchingTerms()
	{
		return nonMatchOnTerms.toNativeArray();
	}
	
	
	/** Returns the score using all weighting models for the current posting of the
	 * specified term
	 * @param i Which term to score
	 * @return score obtained from all weighting models for that term
	 */
	public double score(int i)
	{
		assert i>=0 && i < numTerms: "Looking for posting list " + i + " out of " + (numTerms) + " posting lists.";
		assert termPostings.get(i).getId() != IterablePosting.EOL : "Term " + i + ", posting list at EOL";
		double score = 0.0d;
		score = termModels.get(i).score(termPostings.get(i));
		//System.err.println("For term " + i + " scoring " 
		//	+ termPostings.get(i).getId() + "; got score " + score);
		return score;
	}
	
	
	@Override
	/** Closes all postings that are open */
	public void close() throws IOException
	{
		for (IterablePosting ip: termPostings)
			ip.close();
	}

	
	public long getRequiredBitMask() {
		return this.requiredBitMask;
	}
	
	public long getNegRequiredBitMask() {
		return this.negRequiredBitMask;
	}

	public String getTerm(int i) {
		return termStrings.get(i);
	}
	
	public Set<String> getTags(int i) {
		return termTags.get(i);
	}
	
	public double getKeyFrequency(int i) {
		return termKeyFreqs.get(i);
	}
}
