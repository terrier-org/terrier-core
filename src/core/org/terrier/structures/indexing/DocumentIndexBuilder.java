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
 * The Original Code is DocumentIndexBuilder.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.structures.indexing;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.collections.FSArrayFile;
import org.terrier.utility.ApplicationSetup;

/**
 * A builder for the document index. 
 * @author Vassilis Plachouras
  */
public class DocumentIndexBuilder {
	/** The logger used */
	protected static final Logger logger = LoggerFactory.getLogger(DocumentIndexBuilder.class);
	
	/** Name of the structure to which we're writing */
	protected String structureName;
	/** The stream to which we write the data. */
	protected DataOutputStream dos;
	/** The total number of entries in the document index.*/
	protected int numberOfDocumentIndexEntries;
	/** index object of the index currently being created */
	protected IndexOnDisk index;
	
	FSArrayFile.ArrayFileWriter fileWriter;
	
	/** Construct a DocumentIndex associated with the specified index
	  * @param i Index being constructed
	  * @param _structureName the name of the structure being created
	  */	
	public DocumentIndexBuilder(IndexOnDisk i, String _structureName) 
	{
		this.index = i;
		this.structureName = _structureName;
		try{
			fileWriter = FSArrayFile.writeFSArrayFile(index.getPath() + "/" + index.getPrefix() + "."+ structureName + FSArrayFile.USUAL_EXTENSION);
		} catch (IOException ioe) {
			logger.error("Could not make FSArrayFile.ArrayFileWriter", ioe);
		}
	}

	/**
	 * Adds to the index a new entry, giving to it the next 
	 * available document id. 
	 * @param die The document index entry being written
	 * @exception java.io.IOException Throws an exception in the 
	 *			case of an IO error. 
	 */
	public void addEntryToBuffer(Writable die)
		throws java.io.IOException 
	{	
		fileWriter.write(die);
		numberOfDocumentIndexEntries++;
	}
	/**
	 * Closes the random access file.
	 */
	public void close() {
		try {
			fileWriter.close();
		} catch (IOException ioe) {
			logger.error("Input/Output exception while closing docIndex file. Stack trace follows", ioe);
		}
	}
	/**
	 * Closes the underlying file after finished processing the collections.
	 */
	public void finishedCollections() {
		final int maxDocsEncodedDocid = Integer.parseInt(
				ApplicationSetup.getProperty("indexing.max.encoded."+structureName+"index.docs","5000"));
		if (index != null)
		{
			if (structureName.equals("document"))
				index.setIndexProperty("num.Documents", ""+numberOfDocumentIndexEntries);
			index.addIndexStructure(structureName, 
				numberOfDocumentIndexEntries > maxDocsEncodedDocid 
					? "org.terrier.structures.FSADocumentIndex"
					: "org.terrier.structures.FSADocumentIndexInMem",
				"org.terrier.structures.IndexOnDisk,java.lang.String",
				"index,structureName");
			index.addIndexStructureInputStream(structureName, 
					"org.terrier.structures.FSADocumentIndex$FSADocumentIndexIterator",
					"org.terrier.structures.IndexOnDisk,java.lang.String",
					"index,structureName");
		}
		close();
	}
}
