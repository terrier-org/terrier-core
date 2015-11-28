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
 * The Original Code is LETOROutputFormat.java.
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
import java.util.HashSet;

import org.terrier.learning.FeaturedResultSet;
import org.terrier.matching.ResultSet;
import org.terrier.querying.Request;
import org.terrier.querying.SearchRequest;
import org.terrier.structures.Index;
import org.terrier.structures.MetaIndex;
import org.terrier.utility.ApplicationSetup;

/**
 * Outputs a featured result set in the LETOR format. If the resultset is not featured,
 * then only the score (and docid) are output as features. <b>NB:</b> This class uses
 * Double.toString() to render document scores and feature values, as this canonical output 
 * is easier to obtain and more precise than %f without any decimal places noted. Moreover,
 * it will not display unnecessary ending zeros.
 * 
 * @see <a href="http://research.microsoft.com/en-us/um/beijing/projects/letor/">
 * http://research.microsoft.com/en-us/um/beijing/projects/letor/</a>
 * @author Rodrygo Santos
 * @since 4.0
 */
public class LETOROutputFormat implements OutputFormat {

	protected HashSet<String> seenQueries;
	protected boolean test;
	protected boolean display_scores = Boolean.parseBoolean(ApplicationSetup.getProperty("LETOROutputFormat.show.score", "true"));
	protected boolean feature_docids = Boolean.parseBoolean(ApplicationSetup.getProperty("LETOROutputFormat.feature.docids", "false"));
	protected boolean display_docids = Boolean.parseBoolean(ApplicationSetup.getProperty("LETOROutputFormat.show.docids", "true"));
	protected boolean display_docnos = Boolean.parseBoolean(ApplicationSetup.getProperty("LETOROutputFormat.show.docnos", "true"));
	protected int fOffset = 0;
	public LETOROutputFormat(Index index) {
		seenQueries = new HashSet<String>();
		
		String path = ApplicationSetup.getProperty("learning.labels.source", "");
		String type = ApplicationSetup.getProperty("learning.labels.class", 
				path.isEmpty() ? "org.terrier.learning.TestLabelSet"
						: "org.terrier.learning.TRECQrelsLabelSet");
		
		if (type.equals("org.terrier.learning.TestLabelSet")) {
			this.test = true;
		}
		
		if (display_scores)
			fOffset++;
		if (feature_docids)
			fOffset++;
		
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
		
		boolean rs_has_docnos = rs.hasMetaItems("docno");
		
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
		
		

		// example:
		// 2 qid:10032 1:0.056537 2:0.000000 ... 46:0.076923 #docid = GX029-35-5894638
		for (int i = 0; i < n; i++) {
			if (defLabel != null && !test && labels[i].equals(defLabel)) {
				continue;
			}
			
			pw.print(labels == null ? "-1" : labels[i]);
			pw.print(" qid:" + q.getQueryID());
			//pw.printf("%s qid:%s", labels == null ? "-1" : labels[i], q.getQueryID());
						
			if (display_scores)
			{
				pw.print(" 1:" + Double.toString(scores[i]) );
			}
			
			if (feature_docids)
			{
				pw.printf(" %d:%d ", display_scores ? 2 : 1, docids[i]);
			}
			
			for (int j = 0; j < f; j++) {
				final String featName = featNames[j];
				double[] featScores = frs.getFeatureScores(featName);
				pw.print(' ' + String.valueOf(j+1+fOffset) + ':' + Double.toString(featScores[i]));
			}
			pw.print(" #");
			if (display_docids)
				pw.print("docid = "+ docids[i]);
			if (display_docnos)
				pw.print(" docno = "+ 
					(rs_has_docnos 
						? rs.getMetaItems("docno")[i] 
						: meta.getItem("docno", docids[i]))
				);
			pw.println();
		}		
	}
	
	protected void printHeader(PrintWriter pw, FeaturedResultSet rs) {
		
		if (display_scores)
		{
			pw.printf("# 1:score\n");
		}
		if (feature_docids)
		{
			pw.printf("# %d:docid\n", display_scores ? 2 : 1);
		}
		if (rs == null)
			return;
		
		String[] featNames = rs.getFeatureNames();
		for (int i = 0; i < featNames.length; i++) {
			// TODO: have learning.feature.${name}.type as a property (REAL, DISCRETE, ...?)
			pw.printf("# %d:%s\n", i+1+fOffset, featNames[i]);		
		}
	}
	
}

