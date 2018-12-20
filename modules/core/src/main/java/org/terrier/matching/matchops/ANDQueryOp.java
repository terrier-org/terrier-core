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
 * The Original Code is ANDQueryOp.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.matching.matchops;

import java.io.IOException;
import java.util.List;

import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Pointer;
import org.terrier.structures.postings.ANDIterablePosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.ArrayUtils;

/** This combines multiple operators into a single op, where must all occur
 * in a document. It is logically equivalent to Indri's #band() operator.
 * @since 5.0
 */
public class ANDQueryOp extends MultiTermOp {
	
	public static final String STRING_PREFIX = "#band";

	private static final long serialVersionUID = 1L;

	public ANDQueryOp(String[] ts) {
		super(ts);
	}
	
	public ANDQueryOp(Operator[] ts) {
		super(ts);
	}

	@Override
	protected IterablePosting createFinalPostingIterator(
			List<IterablePosting> postings, List<EntryStatistics> pointers)
			throws IOException {
		return new ANDIterablePosting(
				postings.toArray(new IterablePosting[0]), 
				pointers.toArray(new Pointer[0]));		
	}
	
	
	/** Adjust the statistics for this operator:
	 * 1. The number of occurrences is the minimum of the constituent frequencies (handled by super).
	 * 2. The in-document maxTF is the minimum of the constituent maxTFs */
	@Override
	protected EntryStatistics mergeStatistics(EntryStatistics[] entryStats, CollectionStatistics collStats) {
		int minTF = Integer.MAX_VALUE;
		int minNt = Integer.MAX_VALUE;
		for(EntryStatistics e : entryStats)
		{
			if (e.getMaxFrequencyInDocuments() < minTF)
				minTF = e.getMaxFrequencyInDocuments();
			if (e.getDocumentFrequency() < minNt)
				minNt = e.getDocumentFrequency();
		}
		
		//TODO update minNt
		EntryStatistics rtr = super.mergeStatistics(entryStats, collStats);		
		rtr.setMaxFrequencyInDocuments(minTF);
		return rtr;
	}

	@Override
	public String toString() {
		return STRING_PREFIX + "(" + ArrayUtils.join(terms, ' ') + ")";
	}

}
