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
 * The Original Code is ResultSet.java.
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
import java.util.concurrent.locks.Lock;

/**
 * The interface that defines the functionalities of a 
 * result set. 
 * @author Vassilis Plachouras
  */
public interface ResultSet extends Serializable {
	
	
	/** Returns the names of the meta keys which this resultset has 
	 * @return the list of key names */
	String[] getMetaKeys();
	
	/**
	 * Adds a metadata value for a given document
	 * @param name the name of the metadata type. For example, it can be url for adding the URLs of documents.
	 * @param docid the document identifier of the document.
	 * @param value the metadata value.
	 */
	void addMetaItem(String name, int docid, String value);
	/**
	 * Adds the metadata values for all the documents in the result set.
	 * The length of the metadata array values should be equal to the 
	 * length of the docids array.
	 *
	 * @param name the name of the metadata type. For example, it can be url for adding the URLs of documents.
	 * @param values the metadata values.
	 */
	void addMetaItems(String name, String[] values);
	
	/** Returns true if the resultset already has a set of metaitems with
	 * the specified name.
	 * @param name of the desired metaitem set
	 * @return true if the set exists.
	 */
	boolean hasMetaItems(String name);
	
	/**
	 * Returns the documents ids after retrieval
	 * @return the docids
	 */
	int[] getDocids();
	/**
	 * Returns the exact size of the result set.
	 * @return int the exact size of the result set
	 */
	int getExactResultSize();
	/**
	 * Gets a metadata value for a given document. If the requested
	 * metadata information is not specified, then we return null.
	 * @param name the name of the metadata type. 
	 * @param docid the document identifier of the document.
	 * @return a string with the metadata information, or null of the metadata is not available.
	 */
	String getMetaItem(String name, int docid);
	
	/**
	 * Gets the metadata information for all documents. If the requested
	 * metadata information is not specified, then we return null.
	 * @param name the name of the metadata type. 
	 * @return an array of strings with the metadata information, or null of the metadata is not available.
	 */
	String[] getMetaItems(String name); 
	/**
	 * Returns the occurrences array.
	 * @return short[] the array the occurrences array.
	 */
	short[] getOccurrences();
	
	/**
	 * Returns the effective size of the result set.
	 * @return int the effective size of the result set
	 */
	int getResultSize();
	
	/**
	 * Returns the documents scores after retrieval
	 * @return score list in same order as docids array
	 */
	double[] getScores();
	
	/**
	 * Initialises the arrays prior of retrieval. 
	 */
	void initialise();
	
	/**
	 * Initialises the result set with the given scores. If the 
	 * length of the given array is different than the length
	 * of the internal arrays, then we re-allocate memory
	 * and create the arrays.
	 * @param scs double[] the scores to initiliase the result set with.
	 */
	void initialise(double[] scs);
	
	/** Sorts all documents in this resultset by descending score */
	void sort();
	
	/** Sorts the top <tt>topDocs</tt> document in this resultset be first.
	 * The order of the remaining documents is undefined.
	 * @param topDocs number of documents to top-rank
	 */
	void sort(int topDocs);
	
	
	/**
	 * Sets the exact size of the result set, that is 
	 * the number of documents  that contain at least one query term.
	 * @param newExactResultSize int the effective size of the result set.
	 */
	void setExactResultSize(int newExactResultSize);
	
	/**
	 * Sets the effective size of the result set, that 
	 * is the number of documents to be sorted after retrieval.
	 * @param newResultSize int the effective size of the result set.
	 */
	void setResultSize(int newResultSize);
	
	/**
	 * Crops the existing result file and extracts a subset
	 * from the given starting point, with the given length.
	 * @param start the beginning of the subset.
	 * @param length the length of the subset.
	 * @return a subset of the current result set.
	 */
	ResultSet getResultSet(int start, int length);
	/**
	 * Extracts a subset of the resultset given by the list parameter,
	 * which contains a list of <b>positions</b> in the resultset that
	 * should be saved.
	 * @param list the list of elements in the current list that should be kept.
	 * @return a subset of the current result set specified by the list.
	 */
	ResultSet getResultSet(int[] list);
	
	/**
	 * Returns the lock associated with the result set. 
	 * The lock is used for modifying the result set concurrently by
	 * more than one threads.
	 * @return the lock.
	 */
	Lock getLock();
	
	/** 
	 * Returns a status code for the result set
	 * @return a integer status code. <tt>0</tt> stands success. 
	 * <tt>1</tt> stands for empty result set. <tt>2</tt> stands for
	 * wrong setting of start/end parameters. <tt>3</tt> stands for
	 * query timeout. The values assigned to the status codes are increasing
	 * accordingly to the severity of the status.
	 */
	int getStatusCode();
	
	/** 
	 * Sets the status code.
	 * @param _statusCode - the code to return to the user
	 */
	void setStatusCode(int _statusCode);
}
