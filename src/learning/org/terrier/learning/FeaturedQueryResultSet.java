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
 * The Original Code is FeaturedQueryResultSet.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Rodrygo Santos <rodrygo@dcs.gla.ac.uk>
 */

package org.terrier.learning;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.terrier.matching.QueryResultSet;
import org.terrier.matching.ResultSet;

/**
 * A result set implementation that accommodates multiple query feature scores,
 * besides the original relevance scores.
 * 
 * @author Rodrygo Santos
 * @since 4.0
 */
public class FeaturedQueryResultSet extends QueryResultSet implements FeaturedResultSet {

	private static final long serialVersionUID = -3901317640015640668L;
	private String[] labels;
	private String defLabel;
	private Map<String, double[]> features;

	public FeaturedQueryResultSet(ResultSet resultSet) {
		super(resultSet.getResultSize());
		super.docids = resultSet.getDocids();
		super.scores = resultSet.getScores();
		super.occurrences = resultSet.getOccurrences();
		
		features = new LinkedHashMap<String, double[]>();
	}

	public FeaturedQueryResultSet(int length) {
		super(length);
		features = new LinkedHashMap<String, double[]>();
	}
	
	/** {@inheritDoc} */
	@Override
	public String[] getFeatureNames() {
		return features.keySet().toArray(new String[0]);
	}
	
	/** {@inheritDoc} */
	@Override
	public void setLabels(String[] labels) {
		this.labels = labels;
	}
	
	/** {@inheritDoc} */
	@Override
	public String[] getLabels() {
		return labels;
	}
	
	/** {@inheritDoc} */
	@Override
	public void setDefaultLabel(String defLabel) {
		this.defLabel = defLabel;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getDefaultLabel() {
		return defLabel;
	}
	
	/** {@inheritDoc} */
	@Override
	public void putFeatureScores(String name, double[] scores) {
		features.put(name, scores);
	}

	/** {@inheritDoc} */
	@Override
	public double[] getFeatureScores(String name) {
		return features.get(name);
	}

	/** {@inheritDoc} */
	@Override
	public double[] getFeatureScores(int id) {
		Iterator<double[]> iter = features.values().iterator();
		int i = 0;
		double[] scores = null;
		while (iter.hasNext() && i < id) {
			scores = iter.next();
			i++;
		}
		
		return (i == id) ? scores : null;
	}
	
	/** {@inheritDoc} */
	@Override
	public int getNumberOfFeatures() {
		return features.size();
	}

	/** {@inheritDoc} */
	@Override
	public ResultSet getResultSet(int startPosition, int length) {
		FeaturedQueryResultSet rs = (FeaturedQueryResultSet) super.getResultSet(startPosition, length);
		
		rs.labels = new String[length];
		rs.defLabel = defLabel;
		if (labels != null)
			System.arraycopy(labels, startPosition, rs.labels, 0, length);
		for (String name : features.keySet()) {
			rs.features.put(name, new double[length]);
			System.arraycopy(features.get(name), startPosition, rs.features.get(name), 0, length);
		}
			
		return rs;
	}

	/** {@inheritDoc} */
	@Override
	public ResultSet getResultSet(int[] positions) {
		int length = positions.length;
		
		FeaturedQueryResultSet rs = (FeaturedQueryResultSet) super.getResultSet(positions);
		
		rs.labels = new String[length];
		rs.defLabel = new String(defLabel);
		for (int i = 0; i < length; i++) {
			rs.labels[i] = labels[positions[i]];
		}
		
		for (String name : features.keySet()) {
			rs.features.put(name, new double[length]);
			for (int i = 0; i < length; i++) {
				rs.features.get(name)[i] = features.get(name)[positions[i]];
			}
		}
		
		return rs;
	}

	/** {@inheritDoc} */
	@Override
	protected QueryResultSet makeNewResultSet(int length) {
		return new FeaturedQueryResultSet(length);
	}	
	
}
