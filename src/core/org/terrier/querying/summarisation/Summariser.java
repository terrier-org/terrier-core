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
 * The Original is in 'Summariser.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.querying.summarisation;

/** Base class for query biased summarisers.
 * @since 3.6
 */
public abstract class Summariser {

	/** Returns the query biased summary extracted from the sample of the text
	 * of the document, based on the query terms.
	 * @param extract sample of document text
	 * @param queryTerms 
	 * @return Query biased summary.
	 */
	public abstract String generateSummary(String extract, String[] queryTerms);
	
	/** Obtain an instance of the current default summarised */
	public static Summariser getSummariser()
	{
		return new DefaultSummariser();
	}
	
}
