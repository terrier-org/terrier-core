/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is TwitterJSONDocument.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richardm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.indexing;

import gnu.trove.TLongHashSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

/**
 * This class represents a collection of tweets stored in JSON
 * format. Like TRECCollection, it expects a collection specification
 * containing all of the files to be read. Each file is assumed to be in
 * gzip format, with one tweet per line. The google.gson parser is used
 * to read the tweet JSON. The TwitterJSONDocument representation is used.
 * 
 * @author Richard McCreadie
 * @since 4.0
 *
 */
public class TwitterJSONCollection implements Collection {
	
	/** logger for this class */	
	protected static final Logger logger = LoggerFactory.getLogger(TwitterJSONCollection.class);
	/** The list of files to process. */
	protected ArrayList<String> FilesToProcess = null;
	/** A boolean which is true when a new file is open.*/
	protected boolean SkipFile = false;
	/** The JSON stream containing the tweets */
	protected JsonStreamParser JSONStream = null;
	/** The underlying file stream reading tweets from the current file */
	protected BufferedReader currentTweetStream = null;
	/** The current document */
	protected Document currentDocument = null;
	/** The name of the current file */
	protected String currentFilename = null;
	/** The index in the FilesToProcess of the currently processed file.*/
	protected int FileNumber = -1;
	/** Have we reached the end of the collection yet? */
	protected boolean endOfCollection = false;
	
	TLongHashSet alldocnos = new TLongHashSet(); 
	
	public TwitterJSONCollection(String CollectionSpecFile) {
		
		readCollectionSpec(CollectionSpecFile);
			
		//open the first file
		try {
			openNextFile();
		} catch (IOException ioe) {
			logger.error("IOException opening first file of collection - is the collection.spec correct?", ioe);
		}
	}

	public TwitterJSONCollection() {}
	
	public void init() {
		
		
		
		readCollectionSpec(ApplicationSetup.COLLECTION_SPEC);
		
		//open the first file
		try {
			openNextFile();
		} catch (IOException ioe) {
			logger.error("IOException opening first file of collection - is the collection.spec correct?", ioe);
		}
	}
	
	protected void loadJSON(String file) throws IOException {
		currentTweetStream = Files.openFileReader(file, "UTF-8");
		JSONStream = new JsonStreamParser(currentTweetStream);
	}
	
	public void addFileToProcess(String JSONFile) {
		if (FilesToProcess==null) FilesToProcess = new ArrayList<String>();
		FilesToProcess.add(JSONFile);
	}
	
	protected void readCollectionSpec(String CollectionSpecFilename)
	{
		//reads the collection specification file
		try {
			BufferedReader br2 = Files.openFileReader(CollectionSpecFilename);
			String filename = null;
			FilesToProcess = new ArrayList<String>();
			while ((filename = br2.readLine()) != null) {
				filename = filename.trim();
				if (!filename.startsWith("#") && !filename.equals(""))
					FilesToProcess.add(filename);
			}
			br2.close();
			logger.info("TRECCollection read collection specification ("+FilesToProcess.size()+" files)");
		} catch (IOException ioe) {
			logger.error("Input output exception while loading the collection.spec file. "
							+ "("+CollectionSpecFilename+")", ioe);
		}
	}
	
	/**
	 * Opens the next document from the collection specification.
	 * @return boolean true if the file was opened successufully. If there
	 *		 are no more files to open, it returns false.
	 * @throws IOException if there is an exception while opening the 
	 *		 collection files.
	 */
	public boolean openNextFile() throws IOException {
		//try to close the currently open file
		if (currentTweetStream!=null && FilesToProcess.size() > 0)
			try{
				close();
			}catch (IOException ioe) {
				logger.warn("IOException while closing file being read", ioe);
			}
		//keep trying files
		boolean tryFile = true;
		//return value for this fn
		boolean rtr = false;
		while(tryFile)
		{
			if (FileNumber < FilesToProcess.size() -1 ) {
				SkipFile = true;
				FileNumber++;
				String filename = (String) FilesToProcess.get(FileNumber);
				//check the filename is sane
				if (! Files.exists(filename))
				{
					logger.warn("Could not open "+filename+" : File Not Found");
				}
				else if (! Files.canRead(filename))
				{
					logger.warn("Could not open "+filename+" : Cannot read");
				}
				else
				{	//filename seems ok, open it
					loadJSON(filename); //throws an IOException, throw upwards
					logger.info("Processing "+filename);
					currentFilename = filename;
					//no need to loop again
					tryFile = false;
					//return success
					rtr = true;
				}
			} else {
				//last file of the collection has been read, EOC
				endOfCollection = true;
				rtr = false;
				tryFile = false;
			}
		}
		return rtr;
	}
	
	@Override
	public void close() throws IOException {
		if (currentTweetStream!=null) currentTweetStream.close();
	}
	
	
	
	@Override
	public boolean nextDocument() {
		if (FilesToProcess==null) init();
		
		boolean nextOK = false;
		try {
			nextOK = JSONStream.hasNext();
		} catch (Exception e1) {}
		
		if (nextOK) {
			currentDocument = new TwitterJSONDocument(readTweet());
			return true;
		} else {
			try {
				return openNextFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
		
	}
	
	public JsonObject readTweet() {
	    JsonObject json = JSONStream.next().getAsJsonObject();
	    return json;
	}

	@Override
	public Document getDocument() {
		
		long docno;
		try {
			docno = Long.parseLong(((TwitterJSONDocument)currentDocument).getProperty("docno") );
		} catch (Exception e) {
			System.err.println("WARN: Parsing failure... skipping document");
			return null;
		}
		
		if(alldocnos.contains(docno))
			return null;
		alldocnos.add(docno);
		return currentDocument;
	}

	@Override
	public boolean endOfCollection() {
		return endOfCollection;
	}

	@Override
	public void reset() {
		logger.error("WARN: TwitterJSONCollection.reset() was called but it has not been implemented.");
		
	}

}
