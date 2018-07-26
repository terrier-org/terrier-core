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
 * The Original Code is UnorderedWindowOp.java.
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
import org.terrier.structures.SimpleNgramEntryStatistics;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.ProximityIterablePosting;
import org.terrier.utility.ArrayUtils;
/** This combines multiple operators into a single op, where they occur within a specified
 * window of tokens. It is logically equivalent 
 * to Indri's #uwN() operator.
 * @since 5.0
 */
public class UnorderedWindowOp extends ANDQueryOp {

	public static final String STRING_PREFIX = "#uw";
	private static final long serialVersionUID = 1L;
	
	
	int distance;
	
	public UnorderedWindowOp(Operator[] ts, int dist) {
		super(ts);
		this.distance = dist;
	}
	
	public UnorderedWindowOp(String[] ts, int dist) {
		super(ts);
		this.distance = dist;
	}
	
	public int getDistance()
	{
		return this.distance;
	}
	
	
	@Override
	protected EntryStatistics mergeStatistics(EntryStatistics[] entryStats, CollectionStatistics collStats) {
		EntryStatistics parent = super.mergeStatistics(entryStats, collStats);
		SimpleNgramEntryStatistics nes = new SimpleNgramEntryStatistics(parent);
		nes.setWindowSize(distance);
		
		//this heuristical definition comes from Ivory - see
		//https://github.com/lintool/Ivory/blob/cbef55fb0f608e078c064883f6bb5f7b85b4bdb4/src/java/main/ivory/core/RetrievalEnvironment.java#L133
		//see also discussion in http://www.dcs.gla.ac.uk/~craigm/publications/macdonald10_prox.pdf
		int defaultDf = collStats.getNumberOfDocuments() / 100;
		int defaultCf = defaultDf * 2;
		nes.setFrequency( defaultCf );
		nes.setDocumentFrequency( defaultDf );
		return nes;
	}

	@Override
	public String toString() {
		return STRING_PREFIX +distance+"("+ArrayUtils.join(terms, ' ')+")";
	}

	@Override
	protected IterablePosting createFinalPostingIterator(
			List<IterablePosting> postings, List<EntryStatistics> pointers)
			throws IOException {
		assert postings.size() <= distance;
		return new ProximityIterablePosting(
				postings.toArray(new IterablePosting[postings.size()]),
				pointers.toArray(new EntryStatistics[pointers.size()]), 
				distance);
	}

}
