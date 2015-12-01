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
 * The Original Code is MultiDocumentFileCollection.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 *   Ben He <ben{a.}dcs.gla.ac.uk>
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.indexing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.terrier.utility.StringTools;
import org.terrier.utility.io.WrappedIOException;

public abstract class MultiDocumentFileCollection implements Collection {

	/** logger for this class */
	protected static final Logger logger = LoggerFactory.getLogger(MultiDocumentFileCollection.class);

	public abstract Document getDocument();

	/** Counts the number of documents that have been found in this file. */
	protected int documentsInThisFile = 0;
	/** are we at the end of the collection? */
	protected boolean eoc = false;
	/** has the end of the current input file been reached? */
	protected boolean eof = false;
	/** A boolean which is true when a new file is open.*/
	protected boolean SkipFile = false;
	/** Filename of current file */
	protected String currentFilename;
	
	/** should UTF8 encoding be assumed? */
	protected final boolean forceUTF8 = Boolean.parseBoolean(ApplicationSetup.getProperty("trec.force.utf8", "false"));
	
	/** the input stream of the current input file */
	protected InputStream is = null;
	/** properties for the current document */
	protected Map<String,String> DocProperties = null;
	/** The list of files to process. */
	protected ArrayList<String> FilesToProcess = new ArrayList<String>();
	/** The index in the FilesToProcess of the currently processed file.*/
	protected int FileNumber = 0;
	/** Encoding to be used to open all files. */
	protected String desiredEncoding = ApplicationSetup.getProperty("trec.encoding", Charset.defaultCharset().name());
	/** Class to use for all documents parsed by this class */
	protected Class<? extends Document> documentClass;
	/** Tokeniser to use for all documents parsed by this class */
	protected Tokeniser tokeniser = Tokeniser.getTokeniser();

	protected MultiDocumentFileCollection(){}
	
	/** construct a collection from the denoted collection.spec file */
	MultiDocumentFileCollection(final String CollectionSpecFilename)
	{
		readCollectionSpec(CollectionSpecFilename);
		loadDocumentClass();
		try{
			openNextFile();
		} catch (IOException ioe) {
			logger.error("Problem opening first file ", ioe);
		}
	}

	/**
    * A constructor that reads only the specified InputStream.*/
   MultiDocumentFileCollection(InputStream input)
   {
       is = input;
       try{
    	   openNewFile();
       } catch(Exception e) {
    	   logger.error("", e);
       }
       loadDocumentClass();
   }
   
   /** Loads the class that will supply all documents for this Collection.
	 * Set by property <tt>trec.document.class</tt>
	 */
	protected void loadDocumentClass() {
		try{
			documentClass = Class.forName(ApplicationSetup.getProperty("trec.document.class", TaggedDocument.class.getName())).asSubclass(Document.class);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Check whether it is the last document in the collection
	 * @return boolean
	 */
	public boolean hasNext() {
		return ! endOfCollection();
	}

	/**
	 * Return the next document
	 * @return next document
	 */
	public Document next() {
		nextDocument();
		return getDocument();
	}

	/** Closes the collection, any files that may be open. */
	public void close() {
		try{
			is.close();
		} catch (IOException ioe) { 
			logger.warn("Problem closing collection",ioe);
		}
	}

	/** Returns true if the end of the collection has been reached */
	public boolean endOfCollection() {
		return eoc;
	}
	
	/**
	 * Opens the next document from the collection specification.
	 * @return boolean true if the file was opened successufully. If there
	 *	   are no more files to open, it returns false.
	 * @throws IOException if there is an exception while opening the
	 *	   collection files.
	 */
	protected boolean openNextFile() throws IOException {
		//try to close the currently open file
		if (is!=null)
			try{
				is.close();
			}catch (IOException ioe) {
				logger.warn("IOException while closing file being read", ioe);
			}
		//keep trying files
		boolean tryFile = true;
		//return value for this fn
		boolean rtr = false;
		while(tryFile)
		{
			if (FileNumber < FilesToProcess.size()) {
				SkipFile = true;
				String filename = currentFilename = (String) FilesToProcess.get(FileNumber);
				FileNumber++;
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
				{//filename seems ok, open it
					is = Files.openFileStream(filename); //throws an IOException, throw upwards
					logger.info(this.getClass().getSimpleName() + " processing "+filename);
					//no need to loop again
					tryFile = false;
					//return success
					rtr = true;
					//accurately record file offset
					documentsInThisFile = 0;
					eof = false;
					try{
						openNewFile();
					}catch (IOException e) {
						throw e;
					}catch (Exception e) {
						throw new WrappedIOException(e);
					}
				}
			} else {
				//last file of the collection has been read, EOC
				eoc = true;
				rtr = false;
				tryFile = false;
			}
		}
		return rtr;
	}
	
	static final Pattern charsetMatchPattern = Pattern.compile("charset[:=]\\s*['\"]?([0-9a-zA-Z_\\-]+)['\"]?");

	
	protected void extractCharset() {
		DocProperties.put("charset", desiredEncoding);
		//obtain the character set of the document and put in the charset property
		String cType = DocProperties.get("content-type");
		//force UTF-8 for english documents - webpage isnt clear:
		//http://boston.lti.cs.cmu.edu/Data/clueweb09/dataset.html#encodings
		if (cType != null)
		{
			cType = cType.toLowerCase();
			if (cType.contains("charset"))
			{
				final Matcher m = charsetMatchPattern.matcher(cType);
				if (m.find() && m.groupCount() > 0) {
					String charset = StringTools.normaliseEncoding(m.group(1));							
					if (CharEncoding.isSupported(charset))
						DocProperties.put("charset", charset);
				}
			}
		}
		if (forceUTF8)
			DocProperties.put("charset", "utf-8");
	}
	
	protected void openNewFile() throws Exception {}

	/** Move the collection to the start of the next document. */
	public abstract boolean nextDocument();
	
	/** read in the collection.spec */
	protected void readCollectionSpec(String CollectionSpecFilename) {
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
			logger.info(this.getClass().getSimpleName() + " read collection specification");
		} catch (IOException ioe) {
			logger.error("Input output exception while loading the collection.spec file. "
							+ "("+CollectionSpecFilename+")", ioe);
		}
	}

	/** Resets the Collection iterator to the start of the collection. */
	public void reset() {
		FileNumber = -1; 
		eoc = false;
		try {
			openNextFile();
		} catch (IOException ioe) {
			logger.warn("IOException while resetting collection - ie re-opening first file", ioe);
		}
		
	}

}
