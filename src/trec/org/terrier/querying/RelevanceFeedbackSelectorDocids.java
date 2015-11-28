/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is RelevanceFeedbackSelectorDocids.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.querying;
import gnu.trove.THashMap;
import gnu.trove.TIntByteHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Index;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
/** A feedback document selector that operates as RelevanceFeedbackSelector, except
  * that this should be used when docids are specified in the qrels file, not docnos.
  * <p>
  * <b>Properties:</b>
  * <ul><li><tt>qe.feedback.filename</tt> - filename of qrels file to use for feedback.</li>
  * </ul>
  * @since 3.0
  * @author Craig Macdonald
  */
public class RelevanceFeedbackSelectorDocids extends FeedbackSelector
{
	protected static final Logger logger = LoggerFactory.getLogger(RelevanceFeedbackSelectorDocids.class);
	protected DocumentIndex doi;
	protected THashMap<String, TIntByteHashMap> queryidRelDocumentMap;
	/** 
	 * default constructor 
	 */
	public RelevanceFeedbackSelectorDocids()
	{	
		String feedbackFilename = ApplicationSetup.getProperty("qe.feedback.filename",
				ApplicationSetup.TERRIER_ETC+
				ApplicationSetup.FILE_SEPARATOR+"feedback");
		this.loadRelevanceInformation(feedbackFilename);
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void setIndex(Index index){
		doi = index.getDocumentIndex();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public FeedbackDocument[] getFeedbackDocuments(Request request)
	{
		// get docids of the feedback documents
		String queryid = request.getQueryID();
		TIntByteHashMap list = queryidRelDocumentMap.get(queryid);
		//deal with undefined case
		if (list == null)
			return null;
		//deal with empty case
		if (list.size() == 0)
			return new FeedbackDocument[0];
		final List<FeedbackDocument> rtrList = new ArrayList<FeedbackDocument>(list.size());
		for(int id: list.keys())
		{
			FeedbackDocument doc = new FeedbackDocument();
			doc.docid = id;
			doc.score = -1;
			doc.rank = -1;
			doc.relevance = list.get(id);
			rtrList.add(doc);
		}
		logger.info("Found "+(rtrList.size())+" feedback documents for query "+request.getQueryID());
		return rtrList.toArray(new FeedbackDocument[0]);
	}

	private void loadRelevanceInformation(String filename){
		logger.info("Loading relevance feedback assessments from "+ filename);
		try{
			queryidRelDocumentMap = new THashMap<String, TIntByteHashMap>();
			BufferedReader br = Files.openFileReader(filename);
			String line = null;
			int assessmentsCount =0;
			while ((line=br.readLine())!=null){
				line=line.trim();
				if (line.length()==0)
					continue;
				String[] parts = line.split("\\s+");
				TIntByteHashMap list = queryidRelDocumentMap.get(parts[0]);
				if (list == null)
				{
					queryidRelDocumentMap.put(parts[0], list = new TIntByteHashMap());
				}
				list.put(Integer.parseInt(parts[2]), Byte.parseByte(parts[3]));
				assessmentsCount++;
			}
			br.close();
			logger.info("Total "+ assessmentsCount+ " assessments found");
		}catch(IOException ioe){
			logger.error("Problem loading relevance feedback assessments from "+ filename, ioe);
		}
	}
}
