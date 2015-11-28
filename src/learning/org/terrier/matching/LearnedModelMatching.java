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
 * The Original Code is LearnedModelMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.matching;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.learning.FeaturedResultSet;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.Index;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.StaTools;

/** An abstract class for applying a learned model onto a {@link FeaturedResultSet}.
 * 
 * @author Craig Macdonald
 * @since 4.0
 */
public abstract class LearnedModelMatching implements Matching {

	protected final Matching parent;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/** Apply the loaded learned model for identifying the top N documents.
	 * @param N - number of documents
	 * @param in_scores - sample scores
	 * @param F number of features
	 * @param features indexed by feature THEN document
	 * @param out_scores - final scores
	 */
	protected abstract void applyModel(final int N, final double[] in_scores,
			final int F, final double[][] features, final double[] out_scores);

	final boolean normalise = Boolean.parseBoolean(ApplicationSetup.getProperty("fat.matching.model.normalise", "true"));
	protected final boolean score_is_feature;
	protected final int increment;

	protected LearnedModelMatching(Matching _parent) {
		super();
		score_is_feature = Boolean.parseBoolean(ApplicationSetup.getProperty("fat.matching.model.score_is_feature", "true"));
		increment = score_is_feature ? 1 : 0;
		this.parent = _parent;
	}
	
	protected LearnedModelMatching(Index _index, Matching _parent) throws Exception
	{
		this(_parent);
	}

	@Override
	public ResultSet match(String queryNumber, MatchingQueryTerms queryTerms) throws IOException {
		final ResultSet _rs = parent.match(queryNumber, queryTerms);
		final int N = _rs.getResultSize();
		if (N == 0)
			return _rs;
		final FeaturedResultSet frs;
		try{
			frs = (FeaturedResultSet)_rs;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Pass parent class of " + this.getInfo() + " should return a FeaturedResultSet", e);
		}
		final int[] docids = frs.getDocids();
		final double[] in_scores = frs.getScores();
		final int F = frs.getNumberOfFeatures();
		
		
		final double[][] features = new double[F][];
		
		for(int i=1;i<=F;i++)
		{
			features[i-1] = frs.getFeatureScores(i);
			if (normalise)
				StaTools.standardNormalisation(features[i-1]);
		}
		if (score_is_feature && normalise)
			StaTools.standardNormalisation(in_scores);
		
		final QueryResultSet rrs = new QueryResultSet(N);
		final double[] out_scores = rrs.getScores();
		System.arraycopy(docids, 0, rrs.getDocids(), 0, N);
		
		logger.info("Applying learned model to "+ N + " documents");
		applyModel(N, in_scores, F, features, out_scores);
		rrs.sort();
		
		return rrs;
	}

	@Override
	public void setCollectionStatistics(CollectionStatistics cs) {
		parent.setCollectionStatistics(cs);
	}

	@Override
	public String getInfo() {
		return this.getClass().getName();
	}

}