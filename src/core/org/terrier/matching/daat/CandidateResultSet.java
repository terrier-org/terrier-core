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
 * The Original Code is CandidateResultSet.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Nicola Tonellotto (original author)
 *   
 */
package org.terrier.matching.daat;

import java.io.Serializable;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.terrier.matching.QueryResultSet;
import org.terrier.matching.ResultSet;
import org.terrier.utility.HeapSort;

/** ResultSet which is created from a set of CandidateResults.
 * Used by DAAT matching strategies.
 * @since 3.5
 * @author Nicola Tonellotto
 * @see CandidateResult
 */
@SuppressWarnings("serial")
public class CandidateResultSet implements ResultSet, Serializable
{	
	protected int[] docids;
	protected double[] scores;
	protected short[] occurrences;
	
	protected int resultSize;
	protected int exactResultSize;
	
	
	protected Lock lock;	
	/** {@inheritDoc} */
	public Lock getLock() { return lock; }
	
	protected int statusCode = 0;
	/** {@inheritDoc}*/ @Override 
	public int getStatusCode() { return statusCode; }
	/** {@inheritDoc}*/ @Override 
	public void setStatusCode(int _statusCode) { statusCode = _statusCode; }
	@Override /** {@inheritDoc}. Unsupported for this implementation */
	public void addMetaItem(String name, int docid, String value) {}
	@Override /** {@inheritDoc}. Unsupported for this implementation */
	public void addMetaItems(String name, String[] values) {}	
	@Override /** {@inheritDoc}. Unsupported for this implementation */
	public String getMetaItem(String name, int docid) {	return null; }
	@Override /** {@inheritDoc}. Unsupported for this implementation */
	public String[] getMetaItems(String name) {	return null; }
	@Override /** {@inheritDoc}. Unsupported for this implementation */
	public boolean hasMetaItems(String name) { return false; }
	@Override /** {@inheritDoc}. Unsupported for this implementation */
	public String[] getMetaKeys() { return null; }
	
	protected CandidateResultSet(){}
	
	/** Create a ResultSet from the specified queue of results */
	public CandidateResultSet(Queue<CandidateResult> q)
	{
		lock = new ReentrantLock();
		
		resultSize = q.size();
		exactResultSize = resultSize;

		docids	    = new int[resultSize];
		scores 	    = new double[resultSize];
		occurrences = new short[resultSize];
		
		int i = 0;
		for (CandidateResult cc: q)
		{
			docids[i] 	   = cc.getDocId();
			scores[i] 	   = cc.getScore();
			occurrences[i] = cc.getOccurrence();
			i++;
		}
	}

	/** Create a ResultSet from the specified list of results */
	public CandidateResultSet(List<CandidateResult> q)
	{
		lock = new ReentrantLock();
		
		resultSize = q.size();
		exactResultSize = resultSize;

		docids	    = new int[exactResultSize];
		scores 	    = new double[exactResultSize];
		occurrences = new short[exactResultSize];
		
		int i = 0;
		for (CandidateResult cc: q)
		{
			docids[i] 	   = cc.getDocId();
			scores[i] 	   = cc.getScore();
			occurrences[i] = cc.getOccurrence();
			i++;
		}
	}

	@Override /** {@inheritDoc} */
	public int[]    getDocids()      { return docids;      }
	@Override /** {@inheritDoc} */
	public double[] getScores()      { return scores;      }
	@Override /** {@inheritDoc} */
	public short[]  getOccurrences() { return occurrences; }
	
	@Override /** {@inheritDoc} */
	public int getResultSize() 		{ return resultSize;      }
	@Override /** {@inheritDoc} */
	public int getExactResultSize() { return exactResultSize; }
	/** {@inheritDoc}*/ @Override 
	public ResultSet getResultSet(int start, int length) 
	{
		length = length < docids.length ? length : docids.length;
		QueryResultSet resultSet = new QueryResultSet(length);
		System.arraycopy(docids, start, resultSet.getDocids(), 0, length);
		System.arraycopy(scores, start, resultSet.getScores(), 0, length);
		System.arraycopy(occurrences, start, resultSet.getOccurrences(), 0, length);
		return resultSet;
	}

	@Override /** {@inheritDoc}. Unsupported for this implementation */
	public ResultSet getResultSet(int[] positions) 
	{
		throw new UnsupportedOperationException("This method is not available for class " + CandidateResultSet.class);
	}

	@Override /** {@inheritDoc}. Unsupported for this implementation */
	public void initialise() 
	{
		throw new UnsupportedOperationException("This method is not available for class " + CandidateResultSet.class);
	}

	@Override /** {@inheritDoc}. Unsupported for this implementation */
	public void initialise(double[] scs) 
	{
		throw new UnsupportedOperationException("This method is not available for class " + CandidateResultSet.class);
	}

	@Override /** {@inheritDoc}. */
	public void setExactResultSize(int newExactResultSize)
	{
		//throw new UnsupportedOperationException("This method is not available for class " + CandidateResultSet.class);
		exactResultSize = newExactResultSize;
	}
	
	@Override /** {@inheritDoc}. */
	public void setResultSize(int newResultSize) 
	{
		this.resultSize = newResultSize;
	}
	@Override
	public void sort() {
		sort(this.docids.length);
	}
	
	@Override
	public void sort(int topDocs) {
		HeapSort.descendingHeapSort(getScores(), getDocids(), getOccurrences(), topDocs);
	}
	
}
