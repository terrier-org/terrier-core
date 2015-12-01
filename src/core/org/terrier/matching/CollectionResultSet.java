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
 * The Original Code is CollectionResultSet.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching;
import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.terrier.utility.HeapSort;

//import org.apache.log4j.Logger;
/**
 * This class implements the interface ResultSet and
 * models the set of all documents in the 
 * collection. It encapsulates two arrays, one for 
 * the docids and one for the scores. It has also the 
 * occurrences matrix which counts how many query terms 
 * appear in each of the retrieved documents. The 
 * metadata related methods are empty. 
 * <br>
 * This class is only used internally by the Matching 
 * class and the classes that extent it, because the
 * arrays for the docids and scores contain one entry
 * for each document in the collection. Therefore, the
 * instantiation of an object of this class can be
 * expensive. Access to the retrieved documents is enabled
 * by using the method GetResultSet that returns a cropped
 * result set.
 * @author Vassilis plachouras
  */
public class CollectionResultSet implements ResultSet, Serializable{
	private static final long serialVersionUID = 1L;

	/** The logger used */
	//private static Logger logger = Logger.getLogger(CollectionResultSet.class);
	
	/** The array that stores the document ids.*/
	protected int[] docids;
	/** An array holding the scores of documents in the collection.*/
	protected double[] scores;
	/** 
	 * The occurrences of the query terms in a document. This allows to 
	 * use Boolean operators and filter documents. If the i-th query term
	 * appears in the j-th document, then the i-th bit of occurrences[j]
	 * is set, otherwise it is zero. Using a 2-byte long integer allows 
	 * to check for the occurence of up to 16 query terms.
	 */
	protected short[] occurrences;
	/** 
	 * A static boolean flag indicating whether the arrays of docids and scores
	 * have been initialized (memory allocated for them) or not.
	 */
	protected boolean arraysInitialised = false;
	/**
	 * The number of documents that have been ranked and 
	 * sorted according to their scores.
	 */
	protected int resultSize;
	/**
	 * The number of retrieved documents. This may be
	 * higher that &lt;tt&gt;resultSize&lt;tt&gt;, and corresponds to
	 * the number of documents that contain at least one query
	 * term.
	 */
	protected int exactResultSize;
	
	/** 
	 * A lock for enabling access of the result set by different threads
	 */
	protected Lock lock;
	
	/**
	 * Returns the lock for enabling the modification of the result set by
	 * more than one threads.
	 */
	public Lock getLock() {
		return lock;
	}
	
	/**
	 * A status code for the result set.
	 */
	protected int statusCode = 0;
	
	/**
	 * Returns the status code for the current result set.
	 */
	public int getStatusCode() {
		return statusCode;
	}
	
	/**
	 * Sets the status code for the current result set.
	 */
	public void setStatusCode(int _statusCode) {
		statusCode = _statusCode;
	}
	
	/** Construct a resultset from the following components */
	public CollectionResultSet(int[] _docids, double[] _scores, short[] _occurrences)
	{
		this.lock = new ReentrantLock();
		this.docids = _docids;
		this.scores = _scores;
		this.occurrences = _occurrences;
		this.resultSize = this.exactResultSize = this.docids.length;
	}

	/**
	 * A default constructor for the result set with a given
	 * number of documents.
	 * @param numberOfDocuments the number of documents contained in the result set.
	 */
	public CollectionResultSet(int numberOfDocuments) {
		lock = new ReentrantLock();
		docids = new int[numberOfDocuments];
		scores = new double[numberOfDocuments];
		occurrences = new short[numberOfDocuments];
		resultSize = numberOfDocuments;
		exactResultSize = numberOfDocuments;
	}
	/**
	 * A default constructor for the result set with a given
	 * instance of the result set. The constructor creates a 
	 * clone of the given result set.
	 * @param resultSet The given result set.
	 */
	public CollectionResultSet(ResultSet resultSet){
		lock = new ReentrantLock();
		resultSize = resultSet.getResultSize(); 
		exactResultSize = resultSet.getExactResultSize();
		docids = resultSet.getDocids();
		scores = resultSet.getScores();
		occurrences = resultSet.getOccurrences();
	}
	/**
	 * Returns the documents ids after retrieval
	 */
	public int[] getDocids() {
		return docids;
	}
	/**
	 * Returns the effective size of the result set.
	 * @return int the effective size of the result set
	 */
	public int getResultSize() {
		return resultSize;
	}
	
	/**
	 * Returns the occurrences array.
	 * @return int[] the array the occurrences array.
	 */
	public short[] getOccurrences() {
		return occurrences;
	}
	/**
	 * Returns the exact size of the result set.
	 * @return int the exact size of the result set
	 */
	public int getExactResultSize() {
		return exactResultSize;
	}
	/**
	 * Returns the documents scores after retrieval
	 */
	public double[] getScores() {
		return scores;
	}
	
	/**
	 * Initialises the arrays prior of retrieval. 
	 */
	public void initialise() {
		int numberOfDocuments = docids.length;
		for (int i = 0; i < numberOfDocuments; i++) {
			docids[i] = i;
		}
		Arrays.fill(scores, 0.0D);
		Arrays.fill(occurrences, (short)0);
	}
	
	/**
	 * Initialises the result set with the given scores. If the 
	 * length of the given array is different than the length
	 * of the internal arrays, then we re-allocate memory
	 * and create the arrays.
	 * @param scs double[] the scores to initiliase the result set with.
	 */
	public void initialise(double[] scs) {
		if (scores.length != scs.length) {
			resultSize = scs.length;
			exactResultSize = scs.length;
			docids  =new int[resultSize];
			scores = new double[resultSize];
			occurrences = new short[resultSize];
		}
		int numberOfDocuments = docids.length;
		for (int i = 0; i < numberOfDocuments; i++) {
			docids[i] = i;
		}
		scores = scs;
		Arrays.fill(occurrences, (short)0);
	}
	
	/**
	 * Sets the effective size of the result set, that is the number of documents
	 * to be sorted after retrieval.
	 * @param newResultSize int the effective size of the result set.
	 */
	public void setResultSize(int newResultSize) {
		resultSize = newResultSize;
	}
	/**
	 * Sets the exact size of the result set, that is the 
	 * number of documents that would be retrieved, if the result 
	 * set was not truncated.
	 * @param newExactResultSize int the effective size of the result set.
	 */
	public void setExactResultSize(int newExactResultSize) {
		exactResultSize = newExactResultSize;
	}
	
	/**
	 * Empty method.
	 * @param name the name of the metadata type. For example, it can be 
	 * the url for adding the URLs of documents.
	 * @param docid the document identifier of the document.
	 * @param value the metadata value.
	 */
	public void addMetaItem(String name, int docid, String value) {
	}
	
	/**
	 * Empty method.
	 *
	 * @param name the name of the metadata type. For example, it can 
	 * be the url for adding the URLs of documents.
	 * @param values the metadata values.
	 */
	public void addMetaItems(String name, String[] values) {
	}
	
	/**
	 * Empty method.
	 * @param name the name of the metadata type. 
	 * @param docid the document identifier of the document.
	 * @return a string with the metadata information, or null of the 
	 *         metadata is not available.
	 */
	public String getMetaItem(String name, int docid) {
		return null;
	}
	
	/**
	 * Empty method.
	 * @param name the name of the metadata type. 
	 * @return an array of strings with the metadata information, 
	 * or null of the metadata is not available.
	 */
	public String[] getMetaItems(String name) {
		return null;
	}
	
	/**
	 * Crops the existing result file and extracts a subset
	 * from the given starting point to the ending point.
	 * @param start the beginning of the subset.
	 * @param length the end of the subset.
	 * @return ResultSet a subset of the current result set.
	 */
	public ResultSet getResultSet(int start, int length) {
		length = length < docids.length ? length : docids.length;
		QueryResultSet resultSet = new QueryResultSet(length);
		resultSet.setExactResultSize(this.exactResultSize);
		System.arraycopy(docids, start, resultSet.getDocids(), 0, length);
		System.arraycopy(scores, start, resultSet.getScores(), 0, length);
		System.arraycopy(occurrences, start, resultSet.getOccurrences(), 0, length);
		return resultSet;
	}
	/**
	 * Extracts a subset of the resultset given by the list parameter,
	 * which contains a list of <b>positions</b> in the resultset that
	 * should be saved. <br><b>NB:</b>The metadata hashtable is NOT reduced.
	 * @param positions int[] the list of elements in the current list 
	 *        that should be kept.
	 * @return a subset of the current result set specified by 
	 *         the list.
	 */
	public ResultSet getResultSet(int[] positions) {
		int NewSize = positions.length;
		//if (logger.isDebugEnabled())
		//	logger.debug("New results size is "+NewSize);
		QueryResultSet resultSet = new QueryResultSet(NewSize);
		resultSet.setExactResultSize(this.exactResultSize);
		int newDocids[] = resultSet.getDocids();
		double newScores[] = resultSet.getScores();
		short newOccurs[] = resultSet.getOccurrences();
		int thisPosition;
		for(int i=0;i<NewSize;i++)
		{
			thisPosition = positions[i];
			//if (logger.isDebugEnabled())
			//	logger.debug("adding result at "+i);
			newDocids[i] = docids[thisPosition];
			newScores[i] = scores[thisPosition];
			newOccurs[i] = occurrences[thisPosition];
		}
		return resultSet;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public boolean hasMetaItems(String name) {
		return false;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public String[] getMetaKeys() {
		return null;
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
