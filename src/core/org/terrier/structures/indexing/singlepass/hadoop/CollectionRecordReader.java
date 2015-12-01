/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.uk
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
 * The Original Code is CollectionRecordReader.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Richard McCreadie <richardm{a.}dcs.gla.ac.uk> 
 *   
 */

package org.terrier.structures.indexing.singlepass.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;

import org.terrier.indexing.Collection;
import org.terrier.indexing.Document;
import org.terrier.utility.io.HadoopUtility;
import org.terrier.utility.io.WrappedIOException;

/** An abstract RecordReader class which provides methods to read a collection
 * within the Hadoop framework. Note that the collection will be split based on
 * a predetermined InputSplit type which must contain positional information, i.e.
 * which split it is in the list of all splits.
 * @author Craig Madonald and Richard McCreadie
  * @param <SPLITTYPE> The subclass of InputSplit that this class should work with
 */
@SuppressWarnings("deprecation")
public abstract class CollectionRecordReader<SPLITTYPE extends PositionAwareSplit<?>>
	implements RecordReader<Text, SplitAwareWrapper<Document>>
{
	/** document collection currently being iterated through. starts as null */
	protected Collection documentCollection = null;
	/** the files in this split */
	protected SPLITTYPE split;
	/** the configuration of this job */
	protected Configuration config;
	
	/** the number of documents extacted thus far */
	protected int currentDocument;

	/** number of collections obtained thus far by this record reader */
	protected int collectionIndex;
	/** 
	 * constructor
	 * @param _jobConf
	 * @param _split
	 * @throws IOException
	 */
	public CollectionRecordReader(JobConf _jobConf, SPLITTYPE _split) throws IOException {
		this.config = _jobConf;
		this.split = _split;
		this.collectionIndex = 0;
		this.currentDocument = 0;
		try{
			HadoopUtility.loadTerrierJob(_jobConf);
		} catch (Exception e) {
			throw new WrappedIOException("Cannot load ApplicationSetup", e);
		}
	}

	/**
	 * Closes the document collection if it exists
	 */
	public void close() throws IOException {
		closeCollectionSplit();
	}

	/** Create a new Key, each key
	 * is a Document Number
	 */
	public Text createKey() {
		return new Text();
	}

	/** Create a new Text value,
	 * each value is a document
	 */
	public SplitAwareWrapper<Document> createValue() {
		return new SplitAwareWrapper<Document>(split.getSplitIndex());
	}
	
	/**
	 * Returns the number of bits the recordreader has
	 * accessed, thereby giving the position in
	 * the input data.
	 */
	public abstract long getPos() throws IOException;

	/**
	 * Returns the progress of the reading
	 */
	public abstract float getProgress() throws IOException;

	/** Moves to the next Document in the Collections accessing this InputSplit
	 * if one exists, setting DocID to the property
	 * "DOCID" and Document to the text within the
	 * document. Returns true iff there was indeed a document to read,
	 * and hence this document is now in the DocID and document arguments.
	 */
	@Override
	public boolean next(Text DocID, SplitAwareWrapper<Document> document) throws IOException
	{ 	
		while(true)
		{
			if (documentCollection == null)
			{	
				documentCollection = openCollectionSplit(collectionIndex);
				if (documentCollection == null)//no files in this split
					return false;
			}
			assert documentCollection != null;
			while (documentCollection.nextDocument()) 
			{
				// Get Document
				Document tempDoc = documentCollection.getDocument();
				if (tempDoc != null)
				{
					// Retrieve Document's Unique ID
					if (tempDoc.getProperty("docno") == null)
					{
						throw new IOException("Collection returned null as docno");
					}
					else
					{
						DocID.set(tempDoc.getProperty("docno"));
					}
					document.setObject(tempDoc);
					currentDocument++;
					return true;
				}				
			}
			//no more documents in documentCollection
			closeCollectionSplit();
			collectionIndex++;
			documentCollection = null;
		}
	}

	/** open a collection for the index'th parth of the current split */
	protected abstract Collection openCollectionSplit(int index) throws IOException;
	
	/** closes the current collection */
	protected void closeCollectionSplit() throws IOException {
		if (documentCollection != null) 
			documentCollection.close();
	}

}
