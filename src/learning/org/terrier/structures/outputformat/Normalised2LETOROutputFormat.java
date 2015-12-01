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
 * The Original Code is Normalised2LETOROutputFormat.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Rodrygo Santos <rodrygo@dcs.gla.ac.uk>
 */

package org.terrier.structures.outputformat;

import java.io.IOException;
import java.io.PrintWriter;

import org.terrier.learning.FeaturedResultSet;
import org.terrier.matching.ResultSet;
import org.terrier.querying.SearchRequest;
import org.terrier.structures.Index;
import org.terrier.utility.StaTools;

/** As LETOROutputFormat, but uses normalisation 2 to normalise all scores in [0,1] for each query 
 * @since 4.0 
 */
public class Normalised2LETOROutputFormat extends LETOROutputFormat {

	public Normalised2LETOROutputFormat(Index index) {
		super(index);
	}

	@Override
	public void printResults(PrintWriter pw, SearchRequest q, String method,
			String iteration, int numberOfResults) throws IOException
	{
		ResultSet r = q.getResultSet();
		StaTools.standardNormalisation(r.getScores());
		
		if (r instanceof FeaturedResultSet)
		{
			final FeaturedResultSet frs = (FeaturedResultSet)r;
			final String[] featNames = frs.getFeatureNames();
			final int f = featNames.length;
			for (int j = 0; j < f; j++) 
			{
				StaTools.standardNormalisation(frs.getFeatureScores(featNames[j]));
			}
		}
		
		super.printResults(pw, q, method, iteration, numberOfResults);
	}

	
	
}
