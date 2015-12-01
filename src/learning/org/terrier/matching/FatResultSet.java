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
 * The Original Code is FatResultSet.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.matching;

import org.apache.hadoop.io.Writable;
import org.terrier.matching.ResultSet;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.postings.WritablePosting;

/** A result set that encapsulates the postings for terms within the query 
  * @author Craig Macdonald
  * @since 4.0
  */
public interface FatResultSet extends ResultSet, Writable {

	/** Returns a 2D array of posting objects for the document and terms in this result set.
	 * The array is indexed by document then by term. The first dimension retains the same
	 * order as the Docids array of the result set. The second dimension has the same ordering
	 * as the QueryTerms/EntryStatistics arrays.
	 * @return 2D array of postings.
	 */
	public WritablePosting[][] getPostings();	
	void setPostings(WritablePosting[][] wp);

	/** Return the frequencies of each of the query terms in the query */	
	public double[] getKeyFrequencies();
	void setKeyFrequencies(double[] ks);
	
	/** Return the EntryStatistics of each of the query terms in the query */
	public EntryStatistics[] getEntryStatistics();	
	void setEntryStatistics(EntryStatistics[] es);
	
	/** Get the collection statistics that should be applied when (re)scoring any
	 *  documents in this query */
	public CollectionStatistics getCollectionStatistics();
	void setCollectionStatistics(CollectionStatistics cs);
	
	/** Get the query terms in this query. This might be encoded in PostingListManager form */
	public String[] getQueryTerms();
	void setQueryTerms(String[] qs);
	
	void setDocids(int[] ds);
	void setOccurrences(short[] os);
	void setScores(double[] ss);
}
