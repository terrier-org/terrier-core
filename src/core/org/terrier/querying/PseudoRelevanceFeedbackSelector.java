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
 * The Original Code is PseudoRelevanceFeedbackSelector.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.querying;
import org.terrier.utility.ApplicationSetup;
import org.terrier.matching.ResultSet;
/** A feedback selector for pseudo-relevance feedback. Selects the top ApplicationSetup.EXPANSION_DOCUMENTS
  * documents from the ResultSet attached to the specified request.
  * @since 3.0
  * @author Craig Macdonald
  */
public class PseudoRelevanceFeedbackSelector extends FeedbackSelector
{
	/** 
	 * default constructor 
	 */
	public PseudoRelevanceFeedbackSelector(){}
	/** 
	 * {@inheritDoc} 
	 */
	public FeedbackDocument[] getFeedbackDocuments(Request request)
	{
		final ResultSet rs = request.getResultSet();
		if (rs.getResultSize() == 0)
			return null;

		final int[] docIDs = rs.getDocids();
		final double[] scores = rs.getScores();

		// if the number of retrieved documents is lower than the parameter
        // EXPANSION_DOCUMENTS, reduce the number of documents for expansion
        // to the number of retrieved documents.
		final int effDocuments = Math.min(docIDs.length, ApplicationSetup.EXPANSION_DOCUMENTS);
		final FeedbackDocument[] rtr = new FeedbackDocument[effDocuments];
        for (int i = 0; i < effDocuments; i++)
		{
			rtr[i] = new FeedbackDocument();
			rtr[i].rank = i;
			rtr[i].score = scores[i];
			rtr[i].docid = docIDs[i];
        }
		return rtr;
	}
}
