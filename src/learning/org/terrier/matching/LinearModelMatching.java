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
 * The Original Code is LinearModelMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.matching;

import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntDoubleProcedure;

import java.io.BufferedReader;
import java.io.IOException;

import org.terrier.learning.FeaturedResultSet;
import org.terrier.structures.Index;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;


/** Applies a linear learned model to a {@link FeaturedResultSet}. 
 * Learned model files take the format featureid:weight 
 * @since 4.0
 * @author Craig Macdonald
 */
public class LinearModelMatching extends LearnedModelMatching  {

	final double[] weights;
	
	public LinearModelMatching(Index _index, Matching _parent, double[] _weights) throws Exception
	{
		super(_parent);
		weights = _weights;
	}
	
	public LinearModelMatching(Index _index, Matching _parent, String _modelFilename) throws Exception
	{
		this(_index, _parent, loadFeatureWeights(_modelFilename));
	}
	
	public LinearModelMatching(Index _index, Matching _parent) throws Exception
	{
		this(_index, _parent, ApplicationSetup.getProperty("fat.matching.model.file", null));
	}
	
	
	
	@Override
	protected void applyModel(final int N, final double[] in_scores,
			final int F, final double[][] features,
			final double[] out_scores) {
		
		if (score_is_feature && weights[0] != 0.0d)
		{
			System.arraycopy(in_scores, 0, out_scores, 0, N);
			for(int i=0;i<N;i++)
				out_scores[i] *= weights[0];
		}
		for(int j=0;j<F;j++)
		{
			if (j+increment >= weights.length)
			{
				System.err.println("Trailing feature " + j + " not present in learned model.");
				continue;
			}
			if (weights[j+increment] == 0)
				continue;
			System.err.println("Feature " + j + " with first score of " + features[j][0]);

			for(int i=0;i<N;i++)
			{
				out_scores[i] += weights[j+increment] * features[j][i];
			}
		}
	}

	public static double[] loadFeatureWeights(String input) throws IOException {
		TIntDoubleHashMap featuresWeights = new TIntDoubleHashMap();

		BufferedReader br = Files.openFileReader(input);
		String line = null;
		int maxFid = -1;
		while ((line = br.readLine()) != null) {
			// e.g., 21:0.564891 25:0.0000256
			for (String feature : line.trim().split("\\s+")) {
				final String[] pieces = feature.split(":", 2);
				int fid = Integer.parseInt(pieces[0]);			
				double weight = Double.parseDouble(pieces[1]);
				featuresWeights.put(fid, weight);
				if (fid > maxFid)
					maxFid = fid;
			}
		}
		final double[] weights = new double[maxFid];
		featuresWeights.forEachEntry(new TIntDoubleProcedure() {			
			@Override
			public boolean execute(int fid, double weight) {
				weights[fid-1] = weight;
				return true;
			}
		});
		
		return weights;
	}
	
}
