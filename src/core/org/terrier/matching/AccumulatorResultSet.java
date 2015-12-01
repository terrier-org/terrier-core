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
 * The Original Code is AccumulatorResultSet.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Nicola Tonellotto (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   
 */
package org.terrier.matching;

import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntShortHashMap;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.utility.HeapSort;


/** A result set instance that uses maps internally until initialise() is called
 * @since 3.0
 * @author Nicola Tonelotto
 */
@SuppressWarnings("serial")
public class AccumulatorResultSet implements ResultSet, Serializable
{	
	private static Logger logger = LoggerFactory.getLogger(AccumulatorResultSet.class);
	/**
	 * docid[]
	 */
	public int[] docids;
	/**
	 * scores[]
	 */
	public double[] scores;
	/**
	 * occurrences
	 */
	public short[] occurrences;
	protected boolean arraysInitialised = false;
	
	/**
	 * score map
	 */
	public TIntDoubleHashMap scoresMap;
	/**
	 * occurrence map
	 */
	public TIntShortHashMap  occurrencesMap;
	protected boolean mapsInitialised = false;

	protected int resultSize;
	protected int exactResultSize;
	
	protected Lock lock;	
	/**
	 * get lock
	 */
	public Lock getLock() { return lock; }
	
	protected int statusCode = 0;
	/**
	 * get status code
	 */
	public int getStatusCode() { return statusCode; }
	/**
	 * set status code
	 */
	public void setStatusCode(int _statusCode) { statusCode = _statusCode; }


	/**
	 * Constructs an instance of the AccumulatorResultSet
	 * @param numberOfDocuments maximum size of the result set.
	 */
	public AccumulatorResultSet(int numberOfDocuments) 
	{
		lock = new ReentrantLock();
		
		scoresMap = new TIntDoubleHashMap();
		occurrencesMap = new TIntShortHashMap();

		resultSize = numberOfDocuments;
		exactResultSize = numberOfDocuments;
	}
	
	/** This method initialises the arrays to be sorted, after the matching phase has been completed */
	public void initialise() 
	{
		this.docids = scoresMap.keys();
		this.scores = scoresMap.getValues();
		this.occurrences = occurrencesMap.getValues();		
		resultSize = this.docids.length;
		exactResultSize = this.docids.length;

		scoresMap.clear();
		occurrencesMap.clear();
		this.arraysInitialised = true;
		
		HeapSort.descendingHeapSort(this.getScores(), this.getDocids(), this.getOccurrences(), resultSize);
	}
	
	/** Unsupported */
	public void initialise(double[] scs) 
	{
		throw new UnsupportedOperationException("This method is not available for class " + AccumulatorResultSet.class);
	}

	/** {@inheritDoc} */
	public int[] getDocids() 
	{
		if (arraysInitialised)
			return docids;
		else
			throw new UnsupportedOperationException("");
	}

	/** {@inheritDoc} */
	public int getResultSize() 
	{
		return resultSize;
	}
	
	/** {@inheritDoc} */
	public short[] getOccurrences() 
	{
		if (arraysInitialised)
			return occurrences;
		else
			throw new UnsupportedOperationException("");
	}

	/** {@inheritDoc} */
	public int getExactResultSize() 
	{
		return exactResultSize;
	}

	/** {@inheritDoc} */
	public double[] getScores() 
	{
		if (arraysInitialised)
			return scores;
		else
			throw new UnsupportedOperationException("");
	}
	
	/** {@inheritDoc} */
	public void setResultSize(int newResultSize) 
	{
		resultSize = newResultSize;
	}

	/** {@inheritDoc} */
	public void setExactResultSize(int newExactResultSize) 
	{
		exactResultSize = newExactResultSize;
	}
	
	@Override
	public void sort() {
		sort(this.docids.length);
	}

	@Override
	public void sort(int topDocs) {
		if (! arraysInitialised)
			throw new UnsupportedOperationException("");
		HeapSort.descendingHeapSort(getScores(), getDocids(), getOccurrences(), topDocs);
	}
	
	
	/** Unsupported */
	public void addMetaItem(String name, int docid, String value) {}
	/** Unsupported */
	public void addMetaItems(String name, String[] values) {}	
	/** Unsupported */
	public String getMetaItem(String name, int docid) {	return null; }
	/** Unsupported */
	public String[] getMetaItems(String name) {	return null; }
	/** Unsupported */
	public boolean hasMetaItems(String name) { return false; }
	/** Unsupported */
	public String[] getMetaKeys() { return new String[0]; }
	
	/** {@inheritDoc} */
	public ResultSet getResultSet(int start, int length) 
	{
		if (arraysInitialised) {
			length = length < docids.length ? length : docids.length;
			QueryResultSet resultSet = new QueryResultSet(length);
			resultSet.setExactResultSize(this.getExactResultSize());
			System.arraycopy(docids, start, resultSet.getDocids(), 0, length);
			System.arraycopy(scores, start, resultSet.getScores(), 0, length);
			System.arraycopy(occurrences, start, resultSet.getOccurrences(), 0, length);
			return resultSet;
		} else
			throw new UnsupportedOperationException("");
	}
	
	/** {@inheritDoc} */
	public ResultSet getResultSet(int[] positions) 
	{
		if (arraysInitialised) {
			int NewSize = positions.length;
			if (logger.isDebugEnabled())
				logger.debug("New results size is "+NewSize);
			QueryResultSet resultSet = new QueryResultSet(NewSize);
			resultSet.setExactResultSize(this.getExactResultSize());
			int newDocids[] = resultSet.getDocids();
			double newScores[] = resultSet.getScores();
			short newOccurs[] = resultSet.getOccurrences();
			int thisPosition;
			for(int i=0;i<NewSize;i++)
			{
				thisPosition = positions[i];
				if (logger.isDebugEnabled())
					logger.debug("adding result at "+i);
				newDocids[i] = docids[thisPosition];
				newScores[i] = scores[thisPosition];
				newOccurs[i] = occurrences[thisPosition];
			}
			return resultSet;
		} else
			throw new UnsupportedOperationException("");		
	}
	
	/** @param start the starting offset
	 *  @param length the number of results to keep 
	  * @see #getResultSet(int,int) Returns a ResultSet starting at the
 	  * pre-determined position, of the specified size.
 	  * 
	  */
	public AccumulatorResultSet getAccumulatorResultSet(int start, int length) 
	{
		if (arraysInitialised) {
			length = length < docids.length ? length : docids.length;
			AccumulatorResultSet resultSet = new AccumulatorResultSet(length);
			
			for (int i = start; i < start + length; i++) {
				resultSet.scoresMap.put(docids[i], scores[i]);
				resultSet.occurrencesMap.put(docids[i], occurrences[i]);
			}
			resultSet.initialise();
			return resultSet;
		} else
			throw new UnsupportedOperationException("");
	}
}
