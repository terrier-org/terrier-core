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
 * The Original Code is NormalisedLETOROutputFormat.java.
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
import org.terrier.querying.Request;
import org.terrier.querying.SearchRequest;
import org.terrier.structures.Index;
import org.terrier.structures.MetaIndex;
import org.terrier.utility.StaTools;
/** As LETOROutputFormat, but normalises all scores in [0,1] for each query 
 * @since 4.0 
 */
public class NormalisedLETOROutputFormat extends LETOROutputFormat {

	public NormalisedLETOROutputFormat(Index index) {
		super(index);
	}

	@Override
	public void printResults(PrintWriter pw, SearchRequest q, String method,
			String iteration, int numberOfResults) throws IOException 
	{
		ResultSet rs = q.getResultSet();
		FeaturedResultSet frs = null;
		int f;
		if (rs instanceof FeaturedResultSet)
		{
			frs = (FeaturedResultSet) rs;
			f = frs.getNumberOfFeatures();
		}
		else
		{
			f = 0;
		}				
		
		String qid = q.getQueryID();
		if (seenQueries.contains(qid)) {
			seenQueries.clear();
		}
		
		if (seenQueries.size() == 0) {
			printHeader(pw, frs);
		}
		
		seenQueries.add(qid);
		Request rq = (Request) q;
		MetaIndex meta = rq.getIndex().getMetaIndex();
		
		final int[] docids = rs.getDocids();
		final double[] scores = rs.getScores();
		final int n = rs.getResultSize();
		
		String[] labels = null;
		String defLabel = null;
		String[] featNames = null;
		
		if (f>0)
		{
			labels = frs.getLabels();
			defLabel = frs.getDefaultLabel();
			featNames = frs.getFeatureNames();
		}
		
		StaTools.standardNormalisation(scores);
		double[][] featScores = new double[f][];
		for (int j = 0; j < f; j++) {
			featScores[j] = frs.getFeatureScores(featNames[j]);
			StaTools.standardNormalisation(featScores[j]);
		}
		final boolean docnosInResultSet = rs.hasMetaItems("docno");

		// example:
		// 2 qid:10032 1:0.056537 2:0.000000 ... 46:0.076923 #docid = GX029-35-5894638
		for (int i = 0; i < n; i++) {
			if (defLabel != null && !test && labels[i].equals(defLabel)) {
				continue;
			}
			
			pw.printf("%s qid:%s", labels == null ? "-1" : labels[i], q.getQueryID());
						
			if (display_scores)
			{
				pw.printf(" 1:%s ", Double.toString(scores[i]));
			}
			
			if (display_docids)
			{
				pw.printf(" %d:%d ", display_scores ? 2 : 1, docids[i]);
			}
			
			for (int j = 0; j < f; j++) {
				pw.printf(" %d:%s", j+1+fOffset, Double.toString(featScores[j][i]));
			}
			pw.printf(" #docid = %d", docids[i]);
			if (display_docnos)
				pw.printf(" docno = %s", 
						docnosInResultSet
						? frs.getMetaItems("docno")[i] : meta.getItem("docno", docids[i]));
			pw.println();
		}		
	}
	

}
