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
 * The Original Code is QueryResultSet.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching;
import gnu.trove.TObjectIntHashMap;

//import org.apache.log4j.Logger;
/**
 * A result set for a given query. This result set is created for
 * a given number of documents, usually the number of retrieved
 * documents for a query. Initially, it is created by cropping
 * an instance of the CollectionResultSet, that is used in the
 * Matching classes.
 * <br>
 * This class has support for adding metadata as well.
 * @author Vassilis Plachouras, Craig Macdonald
  */
public class QueryResultSet extends CollectionResultSet {
	private static final long serialVersionUID = 1L;
	/**
	 * The logger used
	 */
	//private static Logger logger = Logger.getLogger(QueryResultSet.class);
	/** 
	 * The structure that holds the metadata about the results. 
	 * It maps the metadata name, a string, to an array index of
	 * the metadata array that contains the particular metadata.
	 */
	protected TObjectIntHashMap<String> metaMap;
	/** The structure holding the metadata. Each column is associated with a
	  * named metavalue type, as known by metaMap. Each row is for a given document. */
	protected String[][] metadata;
	/**
	 * A default constructor for the result set with a given
	 * number of documents.
	 * @param numberOfDocuments the number of documents contained in the result set.
	 */
	public QueryResultSet(final int numberOfDocuments) {
		super(numberOfDocuments);
		metaMap = new TObjectIntHashMap<String>();
		metadata = new String[0][];
	}
	public QueryResultSet(int[] docids, double[] ds, short[] occurrences) {
		super(docids, ds, occurrences);
		metaMap = new TObjectIntHashMap<String>();
		metadata = new String[0][];
	}
	/**
	 * Initialises the arrays prior of retrieval. Only the first time it is called,
	 * it will allocate memory for the arrays.
	 */
	public void initialise() {
		super.initialise();
		metaMap.clear();
		metadata = new String[0][];
	}
	/**
	 * Adds a metadata value for a given document
	 * @param name the name of the metadata type. For example, it can be url for adding the URLs of documents.
	 * @param index the position in the resultset array of the given document
	 * @param value the metadata value.
	 */
	public void addMetaItem(final String name, final int index, final String value) {
		int place = addMetaType(name);
		metadata[place][index] = value;
	}
	
	/**
	 * Adds the metadata values for all the documents in the result set.
	 * The length of the metadata array values should be equal to the
	 * length of the docids array. The array must be sorted in the same
	 * way as the resultset (descending score)
	 *
	 * @param name the name of the metadata type. For example, it can be url for adding the URLs of documents.
	 * @param values the metadata values.
	 */
	public void addMetaItems(final String name, final String[] values) {
		int place = addMetaType(name);
		metadata[place] = values;
	}
	/**
	 * Gets a metadata value for a given document. If the requested
	 * metadata information is not specified, then we return null.
	 * @param name the name of the metadata type.
	 * @param index the postition in the array
	 * @return a string with the metadata information, or null of the metadata is not available.
	 */
	public String getMetaItem(final String name, final int index) {
		if (!metaMap.containsKey(name))
			return null;
		return metadata[metaMap.get(name)][index];
	}
	/**
	 * Gets the metadata information for all documents. If the requested
	 * metadata information is not specified, then we return null.
	 * @param name the name of the metadata type.
	 * @return an array of strings with the metadata information, or null of the metadata is not available.
	 */
	public String[] getMetaItems(final String name) {
		if (!metaMap.containsKey(name))
			return null;
		return metadata[metaMap.get(name)];
	}
	
	@Override
	public boolean hasMetaItems(String name) {
		return metaMap.containsKey(name);
	}
	
	/** {@inheritDoc} */
	@Override
	public String[] getMetaKeys() {
		return metaMap.keys(new String[metaMap.size()]);
	}
	
	/** Get the metadata index for the given name
	  * @param name The name of the metadata type to add on
	  * @return Integer of the place in the metadata array it should be stored in */
	protected int addMetaType(final String name) {
		if (! metaMap.containsKey(name)) {
			String [][] tmp = metadata;
			int length = tmp.length;
			metadata = new String[length+1][resultSize];
			for(int i=0;i<length;i++)
			{
				metadata[i] = tmp[i];
			}
			metadata[length] = new String[resultSize];
		
			metaMap.put(name, length);
			return length;
		}
		return metaMap.get(name);
	}
	
	protected QueryResultSet makeNewResultSet(int length)
	{
		return new QueryResultSet(length);
	}
	/**
	 * Crops the existing result file and extracts a subset
	 * from the given starting point to the ending point.
	 * @param startPosition the beginning of the subset.
	 * @param length the number of entries to get.
	 * @return a subset of the current result set.
	 */
	public ResultSet getResultSet(final int startPosition, final int length) {
		int length1 = length < docids.length ? length : docids.length;
		QueryResultSet resultSet = makeNewResultSet(length);
		resultSet.setExactResultSize(this.exactResultSize);
		System.arraycopy(docids, startPosition, resultSet.getDocids(), 0, length1);
		System.arraycopy(scores, startPosition, resultSet.getScores(), 0, length1);
		System.arraycopy(occurrences, startPosition, resultSet.getOccurrences(), 0, length1);
		resultSet.metaMap = (TObjectIntHashMap<String>)metaMap.clone();
		resultSet.metadata = new String[metadata.length][length1];
		for(int i=0;i<metadata.length;i++)
		{
			System.arraycopy(metadata[i], startPosition, resultSet.metadata[i], 0, length1);
		}
		return resultSet;
	}
	/**
	 * Extracts a subset of the resultset given by the list parameter,
	 * which contains a list of <b>positions</b> in the resultset that
	 * should be saved.
	 * @param positions int[] the list of elements in the current 
	 *        list that should be kept.
	 * @return a subset of the current result set specified by 
	 *         the list.
	 */
	public ResultSet getResultSet(final int[] positions) {
		int NewSize = positions.length;
		//if (logger.isDebugEnabled()){
		//	logger.debug("New results size is "+NewSize);
		//}
		QueryResultSet resultSet = makeNewResultSet(NewSize);
		resultSet.setExactResultSize(this.exactResultSize);
		int newDocids[] = resultSet.getDocids();
		double newScores[] = resultSet.getScores();
		short newOccurs[] = resultSet.getOccurrences();
		resultSet.metaMap = (TObjectIntHashMap<String>)metaMap.clone();
		int metaLength = metadata.length;
		resultSet.metadata = new String[metaLength][];
		String[][] tmpMeta = resultSet.metadata;
		int thisPosition;
		for(int j=0;j<metaLength;j++)
		{
			tmpMeta[j] = new String[NewSize];
		}
		for(int i=0;i<NewSize;i++)
		{
			thisPosition = positions[i];
			//System.err.println("adding result at "+i);
			newDocids[i] = docids[thisPosition];
			newScores[i] = scores[thisPosition];
			newOccurs[i] = occurrences[thisPosition];
			for(int j=0;j<metaLength;j++)
			{
				tmpMeta[j][i] = metadata[j][thisPosition];
			}
		}
		return resultSet;
	}
	
}
