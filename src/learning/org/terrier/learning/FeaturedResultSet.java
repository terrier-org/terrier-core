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
 * The Original Code is FeaturedResultSet.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Rodrygo Santos <rodrygo@dcs.gla.ac.uk>
 */

package org.terrier.learning;

import org.terrier.matching.ResultSet;

/**
 * A result set to accommodate multiple feature scores.
 * Features scores are managed as "dense" arrays, ordered by the current ordering of
 * docids in the ResultSet (i.e. as returned by getDocids()).
 * 
 * @author Rodrygo Santos
 * @since 4.0
 */
public interface FeaturedResultSet extends ResultSet {
	
	/** Number of features decorated for this resultset */
	public int getNumberOfFeatures();
	/** Gets the names of the features that have been added to this ResultSet */
	public String[] getFeatureNames();
	/** Add a feature to this result set */
	public void putFeatureScores(String name, double[] scores);	
	
	/** Get all scores for the enabled docids given a feature name */
	public double[] getFeatureScores(String name);
	

	//TODO from here down.
	//id is NOT a docid. Consider renaming.
	/** Get the feature scores for a given feature id */
	public double[] getFeatureScores(int feature_id);
	

	public void setLabels(String[] labels);
	public String[] getLabels();	
	public void setDefaultLabel(String label);
	public String getDefaultLabel();
}
