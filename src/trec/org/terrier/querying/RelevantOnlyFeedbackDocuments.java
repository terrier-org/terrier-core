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
 * The Original Code is RelevantOnlyFeedbackDocuments.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.querying;
import org.terrier.structures.Index;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/** Select only feedback documents which have relevance &tg; 0
  * @author Craig Macdonald
  * @since 3.0
  */
public class RelevantOnlyFeedbackDocuments extends FeedbackSelector
{
	protected static final Logger logger = LoggerFactory.getLogger(RelevantOnlyFeedbackDocuments.class);
	protected final FeedbackSelector parent;
	/** 
	 * Constructs an instance of RelevantOnlyFeedbackDocuments.
	 * @param _parent
	 */
	public RelevantOnlyFeedbackDocuments(FeedbackSelector _parent)
	{
		this.parent = _parent;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void setIndex(Index index){
		parent.setIndex(index);
	}
	/** 
	 * {@inheritDoc} 
	 */
	public FeedbackDocument[] getFeedbackDocuments(Request request)
	{
		FeedbackDocument[] parentReturn = parent.getFeedbackDocuments(request);
		List<FeedbackDocument> rtr = new ArrayList<FeedbackDocument>(parentReturn.length);
		for(FeedbackDocument candidateDocument : parentReturn)
		{
			if (candidateDocument.relevance > 0)
				rtr.add(candidateDocument);
		}
		logger.info("Dropped "+(parentReturn.length - rtr.size())+" irrelevant feedback documents");
		return rtr.toArray(new FeedbackDocument[0]);
	}
}
