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
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.matching;

import gnu.trove.TIntIntHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.learning.FeaturedQueryResultSet;
import org.terrier.learning.FeaturedResultSet;
import org.terrier.matching.dsms.DocumentScoreModifier;
import org.terrier.matching.models.WeightingModel;
import org.terrier.matching.models.WeightingModelFactory;
import org.terrier.sorting.MultiSort;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.postings.BlockFieldPostingImpl;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.Files;

/** Makes a {@link FeaturedResultSet} by applying a list of features. The input from a parent matching class is a {@link FatResultSet}. 
 * <p>
 * Feature names have a particular format: 
 * <ul>
 * <li><tt>WMODEL:</tt> defines a weighting model for each matching query term, i.e. a query dependent feature. </li>
 * <li><tt>QI:</tt> defined a weighting model called once for each matching document, i.e. a query independent feature.</li>
 * <li><tt>DSM:</tt> applies a document score modifier.</li>.
 * <li><tt>SAMPLE</tt> the scoring method used by the parent {@link Matching} class becomes a feature.</li>
 * </ul>
 * 
 * 
 * 
 * <b>Properties</b>:
 * <ul>
 * <li><tt>fat.featured.scoring.matching.features</tt> - a comma delimited list of features OR the word <tt>FILE</tt> 
 * to load the feature list from a file.</li>
 * <li><tt>fat.featured.scoring.matching.features.file</tt> - a filename containing a newline delimited list of feature.</li>
 * </ul>
 * 
 * @author Craig Macdonald
 * @since 4.0
 * @see "About Learning Models with Multiple Query Dependent Features. Craig Macdonald, Rodrygo L.T. Santos, Iadh Ounis and Ben He. Transactions on Information Systems. 31(3). 2013. <a href="http://www.dcs.gla.ac.uk/~craigm/publications/macdonald13multquerydf.pdf">[PDF]</a>"
 */
public class FatFeaturedScoringMatching implements Matching {

	static Logger logger = LoggerFactory.getLogger(FatFeaturedScoringMatching.class);
	
	final Matching parent;
	
	FatScoringMatching[] wModels;
	String[] wModelNames;
	DocumentScoreModifier[] dsms;
	String[] dsmNames;	
	WeightingModel[] qiFeatures;
	String[] qiFeatureNames;
	boolean sampleFeature = false;
	
	static String[] getModelNames() throws Exception
	{
		String[] modelNames = 
			ArrayUtils.parseCommaDelimitedString(
					ApplicationSetup.getProperty("fat.featured.scoring.matching.features", ""));
		if (modelNames.length == 1 && modelNames[0].equals("FILE"))
		{
			String filename = ApplicationSetup.getProperty("fat.featured.scoring.matching.features.file", null);
			if (filename == null)
				throw new IllegalArgumentException();
			filename = ApplicationSetup.makeAbsolute(filename, ApplicationSetup.TERRIER_ETC);
			String line = null;
			final BufferedReader br = Files.openFileReader(filename);
			final List<String> models = new ArrayList<String>();
			while((line = br.readLine()) != null)
			{
				//ignore linee starting with comments
				if (line.startsWith("#"))
					continue;
				//remove trailing comments
				line = line.replaceAll("#.+$", "");
				models.add(line.trim());
			}
			br.close();
			modelNames = models.toArray(new String[models.size()]);
		}
		return modelNames;
	}
	
	public FatFeaturedScoringMatching(Index _index, Matching _parent, String[] _featureNames) throws Exception
	{
		this.parent = _parent;		
		loadFeatures(_featureNames);	
	}
	
	public FatFeaturedScoringMatching(Index _index, Matching _parent) throws Exception
	{
		this(_index, _parent, getModelNames());
	}
	
	protected void loadFeatures(final String[] featureNames) throws Exception 
	{ 		
		final int featureCount = featureNames.length;
		
		final List<FatScoringMatching> _childrenWmodels = new ArrayList<FatScoringMatching>();
		final List<String> _childrenWmodelNames = new ArrayList<String>();
		
		final List<WeightingModel> _childrenQiModels = new ArrayList<WeightingModel>();
		final List<String> _childrenQiNames = new ArrayList<String>();
				
		final List<DocumentScoreModifier> _childrenDsms = new ArrayList<DocumentScoreModifier>();
		final List<String> _childrenDsmNames = new ArrayList<String>();
		
		for(int i=0;i<featureCount;i++)
		{
			if (featureNames[i].startsWith("#"))
				continue;
			if (featureNames[i].equals("SAMPLE"))
				sampleFeature = true;
			if (featureNames[i].startsWith("DSM:"))
			{
				String dsmName = featureNames[i].replaceFirst("DSM:", "");
				if (dsmName.contains("%"))
				{
					String[] parts = dsmName.split("%", 2);
					dsmName = parts[0];
					String[] props = parts[1].split(" ");
					for(String kv: props)
					{
						String[] part2 = kv.split("=");
						ApplicationSetup.setProperty(part2[0], part2[1]);
					}
				}
				final DocumentScoreModifier dsm = Class.forName(dsmName).asSubclass(DocumentScoreModifier.class).newInstance();				
				_childrenDsms.add(dsm);
				_childrenDsmNames.add(featureNames[i]);
			}
			else if (featureNames[i].startsWith("QI:"))
			{
				final String qiName = featureNames[i].replaceFirst("QI:", "");
				final WeightingModel wm = WeightingModelFactory.newInstance(qiName);
				_childrenQiModels.add(wm);
				_childrenQiNames.add(featureNames[i]);
			}			
			else//assume WMODEL:
			{
				final String wModelName = featureNames[i].replaceFirst("WMODEL:", "");
				WeightingModel wm = WeightingModelFactory.newInstance(wModelName);
				FatScoringMatching fsm = new FatScoringMatching(null, parent, wm);
				fsm.sort = false;
				_childrenWmodels.add(fsm);
				_childrenWmodelNames.add(featureNames[i]);
			}
								
		}		
		dsms = _childrenDsms.toArray(new DocumentScoreModifier[0]);
		dsmNames = _childrenDsmNames.toArray(new String[0]);
		
		qiFeatures = _childrenQiModels.toArray(new WeightingModel[0]);
		qiFeatureNames = _childrenQiNames.toArray(new String[0]);
		
		wModels = _childrenWmodels.toArray(new FatScoringMatching[0]);
		wModelNames = _childrenWmodelNames.toArray(new String[0]);
		
	}
	
	@Override
	public String getInfo() {
		return this.getClass().getSimpleName() +
			"["+ArrayUtils.join(wModelNames, ','+ArrayUtils.join(dsmNames, ','))+"]";
	}

	public ResultSet doMatch(String queryNumber, MatchingQueryTerms queryTerms, final FatResultSet fat)
		throws IOException
	{
		final int numResults = fat.getResultSize();
		final FeaturedQueryResultSet rtr = new FeaturedQueryResultSet(fat);
		if (fat.getResultSize() == 0)
		{
			rtr.scores = new double[0];
			rtr.docids = new int[0];
			rtr.occurrences = new short[0];
			return rtr;
		}
		
		if (sampleFeature)
			rtr.putFeatureScores("SAMPLE", fat.getScores());
		
		//for each WMODEL feature
		for(int fid=0;fid<wModels.length;fid++)
		{
			final ResultSet thinChild = wModels[fid].doMatch(queryNumber, queryTerms, fat);
			rtr.putFeatureScores(wModelNames[fid], thinChild.getScores());
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
			}
		}
		
		//for each DSM feature
		if (dsms.length > 0)
		{
			TIntIntHashMap docidMap = new TIntIntHashMap(numResults);
			int position = 0;
			for(int docid : fat.getDocids())
			{
				docidMap.put(docid, position++);
			}
			final Index fatIndex = FatUtils.makeIndex(fat);		
			for(int fid=0;fid<dsms.length;fid++)
			{
				final double[] scores = new double[numResults];
				final int[] docids = new int[numResults];
				final short[] occurrences = new short[numResults];
				System.arraycopy(fat.getDocids(), 0, docids, 0, numResults);
				System.arraycopy(fat.getOccurrences(), 0, occurrences, 0, numResults);
				
				// Sort by docid so that term postings we have a recoverable score ordering
				MultiSort.ascendingHeapSort(docids, scores, occurrences, docids.length);
				
				final ResultSet thinChild = new QueryResultSet(docids, scores, occurrences);
				final MatchingQueryTerms mqtLocal = new MatchingQueryTerms(queryNumber);
				mqtLocal.setDefaultTermWeightingModel(queryTerms.defaultWeightingModel);
				int ti = 0;
				for(String t : fat.getQueryTerms())
				{
					mqtLocal.setTermProperty(t, fat.getKeyFrequencies()[ti]);
					mqtLocal.setTermProperty(t, fat.getEntryStatistics()[ti]);
					ti++;
				}
				//apply the dsm on the temporary resultset
				dsms[fid].modifyScores(fatIndex, mqtLocal, thinChild);						
				
				//map scores back into original ordering
				double[] scoresFinal = new double[numResults];
				for(int i=0;i<numResults;i++)
				{
					scoresFinal[ docidMap.get(docids[i])] = scores[i];
				}
				//add the feature, regardless of whether it has scores or not			
				rtr.putFeatureScores(dsmNames[fid], scoresFinal);
			}
		}
		final String[] labels = new String[rtr.getResultSize()];
		Arrays.fill(labels, "-1");
		rtr.setLabels(labels);
		
		if (fat.hasMetaItems("docno"))
		{
			rtr.addMetaItems("docno", fat.getMetaItems("docno"));
		}
		if (fat.hasMetaItems("label"))
			rtr.setLabels(fat.getMetaItems("label"));
		
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

	@Override
	public void setCollectionStatistics(CollectionStatistics cs) {
		throw new UnsupportedOperationException();
	}

}
