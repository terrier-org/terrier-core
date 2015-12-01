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
 * The Original Code is FeedbackSelector.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
  *   Rodrygo Santos <rodrygo{a.}dcs.gla.ac.uk>
 */
package org.terrier.querying;

import org.terrier.structures.Index;

/**
 * Implements of this class can be used to select feedback documents. Feedback
 * documents are represented by the FeedbackDocument instances.
 * 
 * @author Craig Macdonald
 * @since 3.0
 */
public abstract class FeedbackSelector {
	protected Index index;

	/** Set the index to be used */
	public void setIndex(Index _index) {
		this.index = _index;
	}

	/** Obtain feedback documents for the specified query request */
	public abstract FeedbackDocument[] getFeedbackDocuments(Request request);
}
