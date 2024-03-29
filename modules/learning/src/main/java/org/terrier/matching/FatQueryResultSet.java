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
 * The Original Code is FatQueryResultSet.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.matching;

import gnu.trove.TIntIntHashMap;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;

import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.utility.StableSort;

/** An implementation of {@link FatResultSet}.
 * 
 * @author Craig Macdonald
 * @since 4.0
 */
public class FatQueryResultSet extends QueryResultSet implements FatResultSet {

	
	CollectionStatistics collStats;
	WritablePosting[][] postings;
	String[] queryTerms;
	double[] keyFrequency;
	EntryStatistics[] entryStats;
	Set<String>[] tags;
	
	public FatQueryResultSet(
			int numberOfDocuments,
			CollectionStatistics cs,
			String[] qs,
			EntryStatistics[] es,
			double[] ks,
			Set<String>[] ts
			) 
	{
		super(numberOfDocuments);
		collStats = cs;
		queryTerms = qs;
		entryStats = es;
		keyFrequency = ks;
		postings = new WritablePosting[numberOfDocuments][];
		tags = ts;
	}

	private static final long serialVersionUID = 1L;

	/** {@inheritDoc} */
	@Override
	public WritablePosting[][] getPostings() {
		return postings;
	}
	
	/** {@inheritDoc} */
	@Override
	public double[] getKeyFrequencies() {
		return keyFrequency;
	}
	
	/** {@inheritDoc} */
	@Override
	public EntryStatistics[] getEntryStatistics() {
		return entryStats;
	}
	
	/** {@inheritDoc} */
	@Override
	public CollectionStatistics getCollectionStatistics()
	{
		return collStats;
	}
	
	/** {@inheritDoc} */	
	@Override
	public String[] getQueryTerms() {
		return queryTerms;
	}

	/** {@inheritDoc} */
	@Override
	public void readFields(DataInput in) throws IOException {
		FatUtils.readFields(this, in);
	}

	/** {@inheritDoc} */
	@Override
	public void write(DataOutput out) throws IOException {
		FatUtils.write(this, out);
	}

	/** {@inheritDoc} */
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
		resultSet.metadata = new String[this.metadata.length][];
		resultSet.metaMap = this.metaMap.clone();
		for(int i=0;i<this.metadata.length;i++)
		{
			if (this.metadata[i] != null)
			{
				resultSet.metadata[i] = new String[length];
				assert this.metadata[i].length == length;
				System.arraycopy(this.metadata[i], 0, resultSet.metadata[i], 0, length);
			}
		}
		return resultSet;
	}

	/** {@inheritDoc} */
	@Override
	public void sort(int topDocs) {
		int[] oldDocids = new int[getDocids().length];
		System.arraycopy(getDocids(), 0, oldDocids, 0, getDocids().length);
		StableSort.sortDescending(getScores(), getDocids(), getOccurrences(), topDocs);
		TIntIntHashMap sortedOrder = new TIntIntHashMap(postings.length);
		for(int i=0;i<docids.length;i++)
		{
			sortedOrder.put(docids[i], i);
		}
		WritablePosting[][] tmp = new WritablePosting[topDocs][];//shouldnt this just be topDocs in length?
		for(int i=0;i<docids.length;i++)
		{
			int docid = oldDocids[i];
			
			assert allIdsEqual(postings[i], docid) : "Posting id mismatch at rank " + i + " docid " + docid;

			if (sortedOrder.containsKey(docid))
			{
				assert getDocids()[sortedOrder.get(docid)] == docid;

				tmp[sortedOrder.get(docid)] = postings[i];
			}
		}
		postings = tmp;
	}

	static boolean allIdsEqual(Posting[] ps, int id) {
		for (Posting p : ps)
		{
			if (p.getId() != id)
				return false;
		}
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public void setPostings(WritablePosting[][] wp) {
		postings = wp;
	}

	/** {@inheritDoc} */
	@Override
	public void setKeyFrequencies(double[] ks) {
		keyFrequency = ks;
	}

	/** {@inheritDoc} */
	@Override
	public void setEntryStatistics(EntryStatistics[] es) {
		entryStats = es;
	}

	/** {@inheritDoc} */
	@Override
	public void setCollectionStatistics(CollectionStatistics cs) {
		collStats = cs;
	}

	/** {@inheritDoc} */
	@Override
	public void setQueryTerms(String[] qs) {
		queryTerms = qs;
	}
	
	/** {@inheritDoc} */
	@Override
	public void setDocids(int[] ds) {
		docids = ds;
		resultSize = ds.length;
	}

	/** {@inheritDoc} */
	@Override
	public void setOccurrences(short[] os) {
		occurrences = os;
	}

	/** {@inheritDoc} */
	@Override
	public void setScores(double[] ss) {
		scores = ss;
	}
	
	@Override
	public Set<String>[] getTags() {
		return tags;
	}

	@Override
	public void setTags(Set<String>[] ks) {
		this.tags = ks;
	}
}
