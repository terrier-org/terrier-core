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
 * The Original Code is WeightingModel.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching.models;

import java.io.Serializable;

import org.terrier.matching.Model;
import org.terrier.querying.Request;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.postings.Posting;

/**
 * This class should be extended by the classes used
 * for weighting terms and documents.
 * @author Gianni Amati, Ben He, Vassilis Plachouras
  */
public abstract class WeightingModel implements Model, Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	/** The class used for computing the idf values.*/
	protected Idf i;
	/** The average length of documents in the collection.*/
	protected double averageDocumentLength;
	/** The term frequency in the query.*/
	protected double keyFrequency;
	/** The document frequency of the term in the collection.*/
	protected double documentFrequency;
	/** The term frequency in the collection.*/
	protected double termFrequency;
	/** The number of documents in the collection.*/
	protected double numberOfDocuments;
	/** The number of tokens in the collections. */
	protected double numberOfTokens;
	/** The parameter c. This defaults to 1.0, but should be set using in the constructor
	  * of each child weighting model to the sensible default for that weighting model. */
	protected double c = 1.0d;
	/** Number of unique terms in the collection */
	protected double numberOfUniqueTerms;	
	/** The number of distinct entries in the inverted file. This figure can be calculated
	  * as the sum of all Nt over all terms */
	protected double numberOfPointers;

	/**
	 * A default constructor that initialises the idf i attribute
	 */
	public WeightingModel() {
		i = new Idf();
	}

	/** Clone this weighting model */
	@Override
	public WeightingModel clone() {
		try{
			WeightingModel newModel = (WeightingModel)super.clone();
			newModel.i = (Idf)this.i.clone();
			return newModel;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	/**
	 * Returns the name of the model.
	 * @return java.lang.String
	 */
	public abstract String getInfo();

	/**
	 * prepare
	 */
	public void prepare() {
		averageDocumentLength = cs.getAverageDocumentLength();
		numberOfDocuments = (double)cs.getNumberOfDocuments();
		i.setNumberOfDocuments(numberOfDocuments);
		numberOfTokens = (double)cs.getNumberOfTokens();
		numberOfUniqueTerms = (double)cs.getNumberOfUniqueTerms();
		numberOfPointers = (double)cs.getNumberOfPointers();
		documentFrequency = (double)getOverflowed(es.getDocumentFrequency());
		termFrequency = (double)getOverflowed(es.getFrequency());		
	}

	/**
	 * Returns overflow
	 * @param o
	 * @return overflow
	 */
	public static long getOverflowed(int o) {		
		return o < 0 ? (o - Integer.MIN_VALUE) + (long)Integer.MAX_VALUE + 1l : (long)o;
	}

	/**
	 * Returns score
	 * @param p
	 * @return score
	 */
	public double score(Posting p) {
		return this.score(p.getFrequency(), p.getDocumentLength());
	}
	
	protected CollectionStatistics cs;
	/**
	 * Sets collection statistics
	 * @param _cs
	 */
	public void setCollectionStatistics(CollectionStatistics _cs) {
		cs = _cs;
	}

	protected EntryStatistics es;
	/**
	 * Sets entry statistics.
	 * @param _es
	 */
	public void setEntryStatistics(EntryStatistics _es) {
		es = _es;
	}
	
	protected Request rq;
	/**
	 * Sets request
	 * @param _rq
	 */
	public void setRequest(Request _rq) {
		rq = _rq;
	}

	/**
	 * This method provides the contract for implementing weighting models.
	 * @param tf The term frequency in the document
	 * @param docLength the document's length
	 * @return the score assigned to a document with the given tf 
	 * and docLength, and other preset parameters
	 */
	public abstract double score(double tf, double docLength);


	/**
	 * Sets the c value
	 * @param _c the term frequency normalisation parameter value.
	 */
	public void setParameter(double _c) {
		this.c = _c;
	}

	/**
	 * Returns the parameter as set by setParameter()
	 */
	public double getParameter() {
		return this.c;
	}

	/**
	 * Sets the term's frequency in the query.
	 * @param keyFreq the term's frequency in the query.
	 */
	public void setKeyFrequency(double keyFreq) {
		keyFrequency = keyFreq;
	}


}
