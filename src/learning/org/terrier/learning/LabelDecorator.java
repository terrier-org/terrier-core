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
 * The Original Code is LabelDecorator.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Rodrygo Santos <rodrygo@dcs.gla.ac.uk>
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.learning;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.evaluation.TRECQrelsInMemory;
import org.terrier.querying.Manager;
import org.terrier.querying.PostProcess;
import org.terrier.querying.Request;
import org.terrier.querying.SearchRequest;
import org.terrier.structures.MetaIndex;
import org.terrier.utility.ApplicationSetup;
/** 
* A post-process to expand an existing result set to a FeaturedResultSet and add labels
* <p>
* <b>Label Related Properties</b>:
* <ul>
* <li><tt>learning.labels.file</tt> - the filename of the qrels file containing the labels
* <li><tt>learning.labels.default</tt> - default feature label, if a document has not label in the specified file. Defaults to -1.</li>
* </ul>
* </p>
* @author Rodrygo Santos, Craig Macdonald
* @since 4.0
*/ 
public class LabelDecorator implements PostProcess {

	protected TRECQrelsInMemory qrels;
	protected String defLabel;
	protected Logger logger = LoggerFactory.getLogger(LabelDecorator.class);
	
	
	public LabelDecorator() throws Exception
	{
		String filename = ApplicationSetup.getProperty("learning.labels.file", null);
		if (filename == null)
			throw new IllegalArgumentException("No qrels file specified in property learning.labels.file");
		qrels = new TRECQrelsInMemory(filename);
		this.defLabel = ApplicationSetup.getProperty("learning.labels.default", "0");

	}
	
	public String[] getValues(Request rq, int[] targetIds) {
		final int n = targetIds.length;
		final String[] targetValues = new String[n];
		
		try {
			String[] docnos = rq.getResultSet().getMetaItems("docno");
			if (docnos == null)
			{			
				final MetaIndex mi = rq.getIndex().getMetaIndex();
				docnos = mi.getItems("docno", targetIds);
			}

			final String qid = TRECQrelsInMemory.parseTRECQueryNo(rq.getQueryID());
			for (int i = 0; i < n; i++) {
				targetValues[i] = String.valueOf(qrels.getGrade(qid, docnos[i], Integer.valueOf(defLabel)));
			}		

			return targetValues;
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	@Override
	public void process(Manager manager, SearchRequest q)
	{
		logger.info("Applying " + this.getInfo());
		Request rq = (Request) q;
		//make the current resultset into a FeaturedResultSet
		
		FeaturedResultSet rs = q.getResultSet() instanceof FeaturedResultSet
			? (FeaturedResultSet) q.getResultSet()
			: new FeaturedQueryResultSet(q.getResultSet());
		rq.setResultSet(rs);
		
		//add labels to the resultset
		int[] docids = rs.getDocids();
		rs.setLabels(getValues(rq, docids));
		rs.setDefaultLabel(defLabel);
		
	}

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName();
	}

}
