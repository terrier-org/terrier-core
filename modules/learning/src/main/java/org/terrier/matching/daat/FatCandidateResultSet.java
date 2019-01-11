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
 * The Original Code is FatCandidateResultSet.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Eric Sutherland
 */

package org.terrier.matching.daat;

import gnu.trove.TIntIntHashMap;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.io.Writable;
import org.terrier.matching.FatQueryResultSet;
import org.terrier.matching.FatResultSet;
import org.terrier.matching.FatUtils;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.utility.HeapSort;

@SuppressWarnings("serial")
/** A version of {@link CandidateResultSet} suitable for use within the Fat framework
 * 
 * @author Eric Sutherland, Craig Macdonald
 * @since 4.0
 */
public class FatCandidateResultSet extends CandidateResultSet implements Writable, FatResultSet {

	
	CollectionStatistics collStats;
	protected WritablePosting[][] postings;
	String[] queryTerms;
	double[] keyFrequency;
	Set<String>[] tags;
	EntryStatistics[] entryStats;


	public FatCandidateResultSet(){
		super();
	}	
	
	public FatCandidateResultSet(Collection<CandidateResult> q, CollectionStatistics cs, String[] queryTerms, EntryStatistics[] entryStats, double[] keyFrequency, Set<String>[] tags) {
		super(q);
		postings = new WritablePosting[q.size()][];
		this.queryTerms = queryTerms;
		this.entryStats = entryStats;
		this.keyFrequency = keyFrequency;
		this.collStats = cs;
		this.tags = tags;
		int i=0;
		for (CandidateResult cc: q)
		{
			postings[i] = ((FatCandidateResult) cc).getPostings();
			i++;
		}
	}

	@SuppressWarnings("unchecked")
	@Deprecated
	public FatCandidateResultSet(List<CandidateResult> q, CollectionStatistics cs, String[] queryTerms, EntryStatistics[] entryStats, double[] keyFrequency) {
		super(q);
		postings = new WritablePosting[q.size()][];
		this.queryTerms = queryTerms;
		this.entryStats = entryStats;
		this.keyFrequency = keyFrequency;
		this.collStats = cs;
		this.tags = new Set[queryTerms.length];
		int i=0;
		for (CandidateResult cc: q)
		{
			postings[i] = ((FatCandidateResult) cc).getPostings();
			i++;
		}
	}
	
	@Override
	public WritablePosting[][] getPostings() {
		return postings;
	}
	
	@Override
	public double[] getKeyFrequencies() {
		return keyFrequency;
	}
	
	@Override
	public EntryStatistics[] getEntryStatistics() {
		return entryStats;
	}
	
	@Override
	public CollectionStatistics getCollectionStatistics()
	{
		return collStats;
	}
	
	@Override
	public String[] getQueryTerms() {
		return queryTerms;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		FatUtils.readFields(this, in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		FatUtils.write(this, out);
	}
	
	@Override
	public FatResultSet getResultSet(int start, int length) {
		length = length < docids.length ? length : docids.length;
		FatQueryResultSet resultSet = new FatQueryResultSet(
				length, 
				this.getCollectionStatistics(), 
				this.getQueryTerms(), 
				this.getEntryStatistics(), 
				this.getKeyFrequencies(),
				this.getTags());
		resultSet.setExactResultSize(this.exactResultSize);
		System.arraycopy(docids, start, resultSet.getDocids(), 0, length);
		System.arraycopy(scores, start, resultSet.getScores(), 0, length);
		System.arraycopy(occurrences, start, resultSet.getOccurrences(), 0, length);
		System.arraycopy(postings, start, resultSet.getPostings(), 0, length);
		return resultSet;
	}

	@Override
	public void sort(int topDocs) {		
		HeapSort.descendingHeapSort(getScores(), getDocids(), getOccurrences(), topDocs);
		TIntIntHashMap sortedOrder = new TIntIntHashMap(postings.length);
		for(int i=0;i<docids.length;i++)
		{
			sortedOrder.put(docids[i], i);
		}
		WritablePosting[][] tmp = new WritablePosting[postings.length][];
		for(int i=0;i<docids.length;i++)
		{
			int docid = -1;
			for(int j=0;j<postings[i].length;j++)
				if (postings[i][j] != null)
				{
					docid = postings[i][j].getId();
					break;
				}
			assert docid != -1;
			tmp[sortedOrder.get(docid)] = postings[i];
		}
		postings = tmp;
	}

	@Override
	public void setPostings(WritablePosting[][] wp) {
		postings = wp;
	}

	@Override
	public void setKeyFrequencies(double[] ks) {
		keyFrequency = ks;
	}

	@Override
	public void setEntryStatistics(EntryStatistics[] es) {
		entryStats = es;
	}

	@Override
	public void setCollectionStatistics(CollectionStatistics cs) {
		collStats = cs;
	}

	@Override
	public void setQueryTerms(String[] qs) {
		queryTerms = qs;
	}

	@Override
	public void setDocids(int[] ds) {
		docids = ds;
		resultSize = ds.length;
	}

	@Override
	public void setOccurrences(short[] os) {
		occurrences = os;
	}

	@Override
	public void setScores(double[] ss) {
		scores = ss;
	}

	@Override
	public Set<String>[] getTags() {
		return tags;
	}

	@Override
	public void setTags(Set<String>[] tags) {
		this.tags = tags;
	}
	
	
	

}
