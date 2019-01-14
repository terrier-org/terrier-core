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
 * The Original Code is FatFeaturedScoringMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.matching;

import java.io.IOException;
import java.util.Arrays;

import org.terrier.learning.FeaturedQueryResultSet;
import org.terrier.learning.FeaturedResultSet;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.Index;
import org.terrier.structures.postings.BlockFieldPostingImpl;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.WritablePosting;

/** Makes a {@link FeaturedResultSet} by applying a list of features. The input from a parent matching class is a {@link FatResultSet}. 
 * <p>
 * Feature names have a particular format: 
 * <ul>
 * <li><tt>WMODEL:</tt> defines a weighting model for all matching query terms (or other operator), i.e. a query dependent feature. </li>
 * <li><tt>WMODELt:</tt> defines a weighting model for all matching single terms, i.e. a query dependent feature. </li>
 * <li><tt>WMODELp1:</tt> defines a weighting model for all matching #1 proximity ops (query dependent). </li>
 * <li><tt>WMODELuw8:</tt> defines a weighting model for all matching #uw8 proxity op (query dependent). </li>
 * <li><tt>WMODEL$tag:</tt> defines a weighting model for all the matching op tagged "tag" - see #tag() in the MatchOp ql (query dependent). </li>
 * <li><tt>QI:</tt> defined a weighting model called once for each matching document, i.e. a query independent feature.</li>
 * <li><tt>DSM:</tt> applies a document score modifier.</li>
 * <li><tt>SAMPLE</tt> the scoring method used by the parent {@link Matching} class becomes a feature.</li>
 * </ul>
 * 
 * 
 * 
 * <b>Properties</b>:
 * <ul>
 * <li><tt>fat.featured.scoring.matching.features</tt> - a semicolon delimited list of features OR the word <tt>FILE</tt> 
 * to load the feature list from a file.</li>
 * <li><tt>fat.featured.scoring.matching.features.file</tt> - a filename containing a newline delimited list of feature.</li>
 * </ul>
 * <p>
 * <b>See also:</b> "About Learning Models with Multiple Query Dependent Features. Craig Macdonald, Rodrygo L.T. Santos, Iadh Ounis and Ben He. Transactions on Information Systems. 31(3). 2013. <a href="http://www.dcs.gla.ac.uk/~craigm/publications/macdonald13multquerydf.pdf">[PDF]</a>
 * @author Craig Macdonald
 * @since 4.0
 * 
 */
public class FatFeaturedScoringMatching extends FeaturedScoringMatching {

	public FatFeaturedScoringMatching(Index _index, Matching _parent, String[] _featureNames) throws Exception
	{
		super(_index, _parent, _featureNames, FatScoringMatching.class);
	}
	
	public FatFeaturedScoringMatching(Index _index, Matching _parent) throws Exception
	{
		super(_index, _parent, FatScoringMatching.class);
	}
	
	public ResultSet doMatch(String queryNumber, MatchingQueryTerms queryTerms, final ResultSet res)
		throws IOException
	{
		final FatResultSet fat = (FatResultSet)res;
		final int numResults = fat.getResultSize();
		final FeaturedQueryResultSet rtr = new FeaturedQueryResultSet(fat);
		int featureCount = 0;
		if (fat.getResultSize() == 0)
		{
			rtr.scores = new double[0];
			rtr.docids = new int[0];
			rtr.occurrences = new short[0];
			return rtr;
		}
		
		if (sampleFeature)
		{
			rtr.putFeatureScores("SAMPLE", fat.getScores());
			featureCount++;
		}
		
		//for each WMODEL feature
		for(int fid=0;fid<wModels.length;fid++)
		{
			final ResultSet thinChild = wModels[fid].doMatch(queryNumber, queryTerms, fat);
			rtr.putFeatureScores(wModelNames[fid], thinChild.getScores());
			featureCount++;
		}
		
		//for each QI features
		if (qiFeatures.length > 0)
		{
			WritablePosting[][] postings = fat.getPostings();
			int[] docids = fat.getDocids();
			for(int fid=0;fid<qiFeatures.length;fid++)
			{
				WeightingModel wm = qiFeatures[fid];
				double[] scores = new double[numResults];
				for(int di=0;di<numResults;di++)
				{
					WritablePosting p = FatUtils.firstPosting(postings[di]);
					if (p == null){
						p = new BlockFieldPostingImpl(docids[di], 0, new int[0], new int[4]);//hack
						((FieldPosting)p).setFieldLengths(new int[4]);
					}
					scores[di] = wm.score(p);
				}
				rtr.putFeatureScores(qiFeatureNames[fid], scores);
				featureCount++;
			}
		}
		
		//for each DSM feature
		if (dsms.length > 0)
		{
			final Index fatIndex = FatUtils.makeIndex(fat);
			final MatchingQueryTerms mqtLocal = new MatchingQueryTerms(queryNumber);
			mqtLocal.setDefaultTermWeightingModel(queryTerms.defaultWeightingModel);
			int ti = 0;
			for(String t : fat.getQueryTerms())
			{
				mqtLocal.setTermProperty(t, fat.getKeyFrequencies()[ti]);
				mqtLocal.setTermProperty(t, fat.getEntryStatistics()[ti]);
				ti++;
			}
			featureCount += applyDSMs(fatIndex, queryNumber, mqtLocal, numResults, fat.getDocids(), fat.getOccurrences(), rtr);
		}
		
		//labels
		final String[] labels = new String[rtr.getResultSize()];
		Arrays.fill(labels, "-1");
		rtr.setLabels(labels);
		
		//metadata
		if (fat.hasMetaItems("docno"))
		{
			rtr.addMetaItems("docno", fat.getMetaItems("docno"));
		}
		if (fat.hasMetaItems("label"))
			rtr.setLabels(fat.getMetaItems("label"));
		logger.info("Finished decorating " + queryNumber + " with " + featureCount + " features");
		return rtr;
	}
	
	
	
	@Override
	public ResultSet match(String queryNumber, MatchingQueryTerms queryTerms)
			throws IOException 
	{
		final FatResultSet fat = (FatResultSet) parent.match(queryNumber, queryTerms);
		if (fat == null)
		{
			logger.warn("I got NO ResultSet from parent " + parent.getInfo() );
			return new FeaturedQueryResultSet(0);
		}
		return doMatch(queryNumber, queryTerms, fat);
	}

}
