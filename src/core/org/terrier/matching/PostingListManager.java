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
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Nicola Tonellotto (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   
 */
package org.terrier.matching;

import gnu.trove.TDoubleArrayList;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.ORIterablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;

/** The PostingListManager is reponsible for opening the appropriate posting lists {@link IterablePosting} given
 * the MatchingQueryTerms object. Moreover, it knows how each Posting should be scored.
 * <p>
 * Plugins are also supported by PostingListManager. Each plugin class should implement the PostingListManagerPlugin
 * interface, and be named explicitly in the <tt>matching.postinglist.manager.plugins</tt> property.
 * <p><b>Properties:</b>
 * <ul>
 * <li> <tt>ignore.low.idf.terms</tt> - should terms with low IDF (i.e. very frequent) be ignored? Defaults to true, i.e. ignored</li>
 * <li> <tt>matching.postinglist.manager.plugins</tt> - Comma delimited list of PostingListManagerPlugin classes to load.</li>
 * </ul>
 * <p><b>Example Usage</b></p>
 * Following code shows how term-at-a-time matching may occur using the PostingListManager:
 * <pre> 
 * MatchingQueryTerms mqt;
 * Index index;
 * PostingListManager plm = new PostingListManager(index, index.getCollectionStatistics(), mqt);
 * plm.prepare(false);
 * for(int term = 0;term < plm.size(); term++)
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
	protected static final Logger logger = LoggerFactory.getLogger(PostingListManager.class);
	/** A property that enables to ignore the terms with a low IDF. Controlled by <tt>ignore.low.idf.terms</tt>
	 * property, defualts to true. */
	protected static boolean IGNORE_LOW_IDF_TERMS = Boolean.parseBoolean(ApplicationSetup.getProperty("ignore.low.idf.terms","true"));
	
	
	/** Interface for plugins to further alter the posting lists managed by the PostingListManager */
	public static interface PostingListManagerPlugin
	{
		/** process the query, given matchign query terms, index and posting list manager */
		void processQuery(MatchingQueryTerms mqt, Index index, PostingListManager plm);
	}
	
	static PostingListManagerPlugin[] plugins;
	
	static {
		String[] pluginNames = ArrayUtils.parseCommaDelimitedString(ApplicationSetup.getProperty("matching.postinglist.manager.plugins", ""));
		List<PostingListManagerPlugin> _plugins = new ArrayList<PostingListManagerPlugin>();
		for (String pluginClass : pluginNames)
		{
			if (! pluginClass.contains("."))
				pluginClass = "org.terrier.matching." + pluginClass;
			try {
				PostingListManagerPlugin p = Class.forName(pluginClass).asSubclass(PostingListManagerPlugin.class).newInstance();
				_plugins.add(p);
			} catch (Exception e) {
				logger.warn("Problem loading PostingListManagerPlugin called "+ pluginClass, e);
			}
		}
		plugins = _plugins.toArray(new PostingListManagerPlugin[ _plugins.size() ]);
	}
	
	/** posting lists for each term */
	protected final List<IterablePosting> termPostings = new ArrayList<IterablePosting>();
	/** weighting models for each term */
	protected final List<WeightingModel[]> termModels = new ArrayList<WeightingModel[]>();
	/** EntryStatistics for each term */
	protected final List<EntryStatistics> termStatistics = new ArrayList<EntryStatistics>();
	/** String form for each term */
	protected final List<String> termStrings = new ArrayList<String>();
	
	/** String form for each term */
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
		this(_index, _cs, mqt, true);
	}
	
	/** Create a posting list manager for the given index and statistics, and populated using the specified
	 * MatchingQueryTerms.
	 * @param _index - index to obtain postings from
	 * @param _cs - collection statistics to obtain 
	 * @param mqt - MatchingQueryTerms object calculated for the query
	 * @param splitSynonyms - allows the splitting of synonym groups (i.e. singleTermAlternatives) to be disabled
	 */
	public PostingListManager(Index _index, CollectionStatistics _cs, MatchingQueryTerms mqt, boolean splitSynonyms) throws IOException
	{
		this(_index, _cs);
		for(String queryTerm : mqt.getTerms())
		{
			if (splitSynonyms && queryTerm.contains("|"))
			{
				String[] alternatives = queryTerm.split("\\|");
				addSingleTermAlternatives(alternatives, queryTerm,
						mqt.getTermWeight(queryTerm), 
						mqt.getStatistics(queryTerm), 
						mqt.getTermWeightingModels(queryTerm));
			}
			else
			{
				addSingleTerm(queryTerm, 
					mqt.getTermWeight(queryTerm), 
					mqt.getStatistics(queryTerm), 
					mqt.getTermWeightingModels(queryTerm));
			}
		}
		for(PostingListManagerPlugin p : plugins)
		{
			p.processQuery(mqt, index, this);
		}
		logger.info("Query " + mqt.getQueryId() + " with "+ mqt.getTerms().length +" terms has " + termPostings.size() + " posting lists");
		assert termPostings.size() == termStatistics.size();
	}
	
	/** Add a single term to those to be matched for this query.
	 * Those with more occurrences than the number of documents will be ignored if IGNORE_LOW_IDF_TERMS
	 * is enabled.
	 * @param queryTerm String form of the query term
	 * @param weight influence of this query term in scoring
	 * @param entryStats statistics to be used for this query term. If null, these will be obtained 
	 * from the local Lexicon
	 * @param wmodels weighting models to be applied for this query term
	 * @throws IOException
	 */
	public void addSingleTerm(String queryTerm, double weight, EntryStatistics entryStats, WeightingModel[] wmodels) throws IOException
	{
		LexiconEntry t = lexicon.getLexiconEntry(queryTerm);
		if (t == null) {
			logger.debug("Term Not Found: " + queryTerm);
			//previousTerm = false;			
		} else if (IGNORE_LOW_IDF_TERMS && collectionStatistics.getNumberOfDocuments() < t.getFrequency()) {
			logger.warn("query term " + queryTerm + " has low idf - ignored from scoring.");
			//previousTerm = false;
		} else if (wmodels.length == 0) {
			logger.warn("No weighting models for term " + queryTerm +", skipping scoring");
			//previousTerm = false;
		} else {
			termStrings.add(queryTerm);
			termPostings.add(invertedIndex.getPostings((Pointer) t));
			if (entryStats == null)
				entryStats = t;
			if (logger.isDebugEnabled())
				logger.debug("Term " + queryTerm + " stats" + entryStats.toString());
			termStatistics.add(entryStats);
			termKeyFreqs.add(weight);
			for (WeightingModel w : wmodels)
			{
				w.setEntryStatistics(entryStats);				
				w.setKeyFrequency(weight);
				w.setCollectionStatistics(collectionStatistics);
				IndexUtil.configure(index, w);
				w.prepare();			
			}
			termModels.add(wmodels);
		}
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
	
	/** Adds a synonym group to the matching process.
	 * EntryStatistics for all terms in the group will be combined using mergeStatistics()
	 * @param terms String of the terms in the synonym group
	 * @param weight influence of this synonym group during retrieval
	 * @param entryStats statistics of the terms in the synonym group. If null, these will
	 * be obtained from the local Lexicon. 
	 * @param wmodels WeightingModels for the synonym group (NOT one per member).
	 * @throws IOException
	 */
	public void addSingleTermAlternatives(String[] terms, String stringForm,  double weight, EntryStatistics[] entryStats, WeightingModel[] wmodels) throws IOException
	{		
		EntryStatistics joined = mergeStatistics(entryStats);
		addSingleTermAlternatives(terms, stringForm, weight, joined, wmodels);
	}
	
	/** Adds a synonym group to the matching process. 
	 * @param terms String of the terms in the synonym group
	 * @param weight influence of this synonym group during retrieval
	 * @param entryStats statistics of the whole synonym group. If null, these will
	 * be obtained from the local Lexicon for all terms in the group will be combined using mergeStatistics()
	 * @param wmodels WeightingModels for the synonym group (NOT one per member).
	 * @throws IOException
	 */
	public void addSingleTermAlternatives(String[] terms, String stringForm, double weight, EntryStatistics entryStats, WeightingModel[] wmodels) throws IOException
	{
		List<LexiconEntry> _le = new ArrayList<LexiconEntry>(terms.length);
		List<IterablePosting> _joinedPostings = new ArrayList<IterablePosting>(terms.length);
				
		for(String alternative : terms)
		{
			LexiconEntry t = lexicon.getLexiconEntry(alternative);
			if (t == null) {
				logger.debug("Alternative term Not Found: " + alternative);
				//previousTerm = false;			
			} else if (IGNORE_LOW_IDF_TERMS && collectionStatistics.getNumberOfDocuments() < t.getFrequency()) {
				logger.warn("query term " + alternative + " has low idf - ignored from scoring.");
				//previousTerm = false;
			} else if (wmodels.length == 0) {
				logger.warn("No weighting models for term " + alternative +", skipping scoring");
				//previousTerm = false;
			} else {
				_le.add(t);
				_joinedPostings.add(invertedIndex.getPostings((BitIndexPointer) t));
			}
		}
		if (_le.size() == 0)
		{
			logger.warn("No alternatives matched in " + Arrays.toString(terms));
			return;
		}
		if (entryStats == null)
			entryStats = mergeStatistics(_le.toArray(new LexiconEntry[_le.size()]));
		if (logger.isDebugEnabled())
			logger.debug("Dijunctive term " + Arrays.toString(terms) + " stats" + entryStats.toString());
		termStrings.add(stringForm);
		termStatistics.add(entryStats);
		termKeyFreqs.add(weight);
		//System.err.println(entryStats.toString());
		IterablePosting[] joinedPostings = _joinedPostings.toArray(new IterablePosting[_joinedPostings.size()]);
		termPostings.add(ORIterablePosting.mergePostings(joinedPostings));
		for (WeightingModel w : wmodels)
		{
			w.setEntryStatistics(entryStats);
			w.setKeyFrequency(weight);
			w.setCollectionStatistics(collectionStatistics);
			IndexUtil.configure(index, w);
			w.prepare();			
		}
		termModels.add(wmodels);
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
	
	
	/** Returns the score using all weighting models for the current posting of the
	 * specified term
	 * @param i Which term to score
	 * @return score obtained from all weighting models for that term
	 */
	public double score(int i)
	{
		if (i >= 0)
			if (i < numTerms)
			{
				double score = 0.0d;
				for (WeightingModel w : termModels.get(i))
					score += w.score(termPostings.get(i));
				return score;
			}

		throw new IllegalArgumentException("Looking for posting list " + i + " out of " + (numTerms) + " posting lists.");
	}
	
	
	@Override
	/** Closes all postings that are open */
	public void close() throws IOException
	{
		for (IterablePosting ip: termPostings)
			ip.close();
	}


	public String getTerm(int i) {
		return termStrings.get(i);
	}
	
	public double getKeyFrequency(int i) {
		return termKeyFreqs.get(i);
	}
}
