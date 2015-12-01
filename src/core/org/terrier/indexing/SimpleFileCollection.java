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
 * The Original Code is SimpleFileCollection.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.indexing;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
/** 
 * Implements a collection that can read arbitrary files on disk. It will 
 * use the file list given to it in the constructor, or it will read the
 * file specified by the property <tt>collection.spec</tt>.
 * <b>Properties:</b>
 * <ul>
 * <li><tt>indexing.simplefilecollection.extensionsparsers</tt> - a comma delimited lists of tuples, in the form "extension:DocumentClass". 
 * For instance, one tuple could be "txt:FileDocument". The default <tt>txt:FileDocument,text:FileDocument,tex:FileDocument,bib:FileDocument,pdf:PDFDocument,html:TaggedDocument,htm:TaggedDocument,xhtml:TaggedDocument,xml:TaggedDocument,doc:MSWordDocument,ppt:MSPowerpointDocument,xls:MSExcelDocument</tt>.
 * </li>
 * <li><tt>indexing.simplefilecollection.defaultparser</tt> - the default parser for any unknown extensions.
 * If this property is empty, then such documents will not be opened.</li>
 * <li><tt>indexing.simplefilecollection.recurse</tt> - whether directories should be opened looking for
 * files.</li>
 * </ul>
 * @author Craig Macdonald &amp; Vassilis Plachouras
 */
public class SimpleFileCollection implements Collection/*, DocumentExtractor*/
{
	protected static final Logger logger = LoggerFactory.getLogger(SimpleFileCollection.class);
	/** The default namespace for all parsers to be loaded from. Only used if
	  * the class name specified does not contain any periods ('.') */
	public final static String NAMESPACE_DOCUMENTS = "org.terrier.indexing.";

	/** The list of files to index.*/
	protected LinkedList<String> FileList = new LinkedList<String>();

	/** Contains the list of files first handed to the SimpleFileCollection, allowing
	  * the SimpleFileCollection instance to be simply reset. */
	protected List<String> firstList;

	/** This is filled during traversal, so document IDs can be matched with filenames */
	protected List<String> indexedFiles = new ArrayList<String>();

	/** The identifier of a document in the collection.*/
	protected int Docid = 0;

	/** Whether directories should be recursed into by this class */
	protected boolean Recurse = Boolean.parseBoolean(ApplicationSetup.getProperty("indexing.simplefilecollection.recurse", "true"));

	/** Maps filename extensions to Document classes. 
	  * The entry |DEFAULT| maps to the default document parser, specified 
	  * by <tt>indexing.simplefilecollection.defaultparser</tt> */
	protected Map<String,Class<? extends Document>> extension_DocumentClass = new HashMap<String,Class<? extends Document>>();

	/** The filename of the current file we are processing. */
	protected String thisFilename;

	/** The InputStream of the most recently opened document. This
	  * is used to ensure that files are closed once they have been
	  * finished reading. */
	protected InputStream currentStream = null;
	
	protected Tokeniser tokeniser = Tokeniser.getTokeniser();

	/**
	 * Constructs an instance of the class with the given list of files.
	 * @param filelist ArrayList the files to be processed by this collection.
	 */
	public SimpleFileCollection(List<String> filelist, boolean recurse) {
		FileList = new LinkedList<String>(filelist);
		//keep a backup copy for reset()
		firstList = new LinkedList<String>(filelist);
		createExtensionDocumentMapping();
	}

	/**
	 * A default constructor that uses the files to be processed
	 * by this collection, as specified by the property
	 * <tt>collection.spec</tt>
	 */
	public SimpleFileCollection()
	{
		this(ApplicationSetup.COLLECTION_SPEC);
	}


	/**
	 * Creates an instance of the class. The files to be processed are
	 * specified in the file with the given name. 
	 * @param addressCollectionFilename String the name of the file that 
	 *        contains the list of files to be processed by this collecion.
	 */
	public SimpleFileCollection(String addressCollectionFilename)
	{
		ArrayList<String> generatedFileList = new ArrayList<String>();
		try{
			//opening the address_collection file
			BufferedReader br = Files.openFileReader(addressCollectionFilename);
			//iterate through each entry of the address_collection file
			String filename = br.readLine();
			while (filename != null) {
				//if the line starts with #, then assume it is 
				//a comment and proceed to the next one
				if (filename.startsWith("#")) {
					filename = br.readLine();
					continue;
				}
				if(logger.isDebugEnabled()){
				logger.debug("Added "+filename+" to filelist for SimpleFileCollection");
				}
				generatedFileList.add(filename);
				filename = br.readLine();
			}
		
		}catch(IOException ioe) {
			logger.error("problem opening address list of files in SimpleFileCollectio: ",ioe);	
		}
		FileList = new LinkedList<String>(generatedFileList);
		firstList = new LinkedList<String>(generatedFileList);
		createExtensionDocumentMapping();
	}


	/** Parses the properties <tt>indexing.simplefilecollection.extensionsparsers</tt>
	  * and <tt>indexing.simplefilecollection.defaultparser</tt> and attempts to load
	  * all the mentioned classes, in a hashtable mapping filename extension to their
	  * respective parsers. If <tt>indexing.simplefilecollection.defaultparser</tt>
	  * is set, then that class will be used to attempt to parse documents that no
	  * explicit parser is set. */
	protected void createExtensionDocumentMapping()
	{
		String staticMappings = ApplicationSetup.getProperty("indexing.simplefilecollection.extensionsparsers",
			"txt:FileDocument,text:FileDocument,tex:FileDocument,bib:FileDocument," +
			"pdf:PDFDocument,html:TaggedDocument,htm:TaggedDocument,xhtml:TaggedDocument,xml:TaggedDocument,"+
			"doc:POIDocument,ppt:POIDocument,xls:POIDocument,"+
			"docx:POIDocument,pptx:POIDocument,xlsx:POIDocument,"+
			"pub:POIDocument,vsd:POIDocument"
			);
		String defaultMapping = ApplicationSetup.getProperty("indexing.simplefilecollection.defaultparser","");
		if (staticMappings.length() > 0)
		{
			String[] mappings = staticMappings.split("\\s*,\\s*");
			for(int i=0;i<mappings.length;i++)
			{
				if (mappings[i].indexOf(":") < 1)
					continue;
				String[] mapping = mappings[i].split(":");
				if (mapping.length == 2 && mapping[0].length() > 0
					&& mapping[1].length() > 0)
				{
					if (mapping[1].indexOf(".") == -1)
						mapping[1] = NAMESPACE_DOCUMENTS + mapping[1];
					else if (mapping[1].startsWith("uk.ac.gla.terrier"))
						mapping[1] = mapping[1].replaceAll("uk.ac.gla.terrier", "org.terrier");
					try{
						Class<? extends Document> d = Class.forName(mapping[1], false, this.getClass().getClassLoader()).asSubclass(Document.class);
						extension_DocumentClass.put(mapping[0].toLowerCase(), d);
					}catch (Exception e){
						/*warning, just ignore */
						logger.warn("Missing class " + mapping[1] + " for " +
							mapping[0].toLowerCase() + " files.",e);
					}
				}
			}
		}
		//set the mapping for the default parser
		if (!defaultMapping.equals("")) {
  			if (defaultMapping.indexOf(".") == -1)
  				defaultMapping = NAMESPACE_DOCUMENTS + defaultMapping;
  			else if (defaultMapping.startsWith("uk.ac.gla.terrier"))
  				defaultMapping = defaultMapping.replaceAll("uk.ac.gla.terrier", "org.terrier");
			
  			try{
  				Class<? extends Document> d =  Class.forName(defaultMapping, false, this.getClass().getClassLoader()).asSubclass(Document.class);
  				extension_DocumentClass.put("|DEFAULT|", d);
  			}catch (Exception e){
  				logger.warn("Missing default class " + defaultMapping, e);
  			}  			
  		}
	}

	/**
	 * Check whether there is a next document in the collection to be processed
	 * @return has next
	 */
	public boolean hasNext() {
		return ! endOfCollection();
	}
	
	/**
	 * Move onto the next document in the collection to be processed.
	 * @return next document
	 */
	public Document next()
	{
		nextDocument();
		return getDocument();
	}
	
	/**
	 * This is unsupported by this Collection implementation, and
	 * any calls will throw UnsupportedOperationException
	 * Throws UnsupportedOperationException on all invocations */
	public void remove()
	{
		throw new UnsupportedOperationException("Iterator.remove() not supported");
	}

	/** 
	 * Move onto the next document in the collection to be processed.
	 * @return boolean true if there are more documents in the collection, 
	 *         otherwise return false.*/
	public boolean nextDocument()
	{
		if (FileList.size() == 0)
			return false;
		boolean rtr = false;
		thisFilename = null;
		while(FileList.size() > 0 && ! rtr)
		{
			thisFilename = FileList.removeFirst();
			logger.info("NEXT: "+thisFilename);
				
			if (! Files.exists(thisFilename) || ! Files.canRead(thisFilename) )
			{
				if (! Files.exists(thisFilename))
					logger.warn("File doesn't exist: "+thisFilename);
				else if (! Files.canRead(thisFilename) )
					logger.warn("File cannot be read: "+thisFilename);
				rtr = nextDocument();
			}
			else if (Files.isDirectory(thisFilename))
			{
				//we're allowed to recurse into directories
				if(Recurse)
					addDirectoryListing();
			}
			else
			{	//this file is fine - use it!
				//this block ensures that DocId is only increased once per file
				Docid++;
				rtr = true;
			}
		}//loop ends
		return rtr;
	}
	/**
	 * Return the current document in the collection.
	 * @return Document the next document object from the collection.
	 */
	public Document getDocument()
	{
		InputStream in = null;
		if (currentStream != null)
		{
			try{
				currentStream.close();
				currentStream = null;
			}catch (IOException ioe) {
				logger.warn("IOException while closing file being read", ioe);
			}
		}
		if (thisFilename == null)
		{
			return null;
		}
		String filename = null;
		try{
			in = Files.openFileStream(thisFilename);
			filename = thisFilename.replaceAll("\\.gz$","");
		}catch(IOException ioe){
			logger.warn("Problem reading "+thisFilename+" in "+
			"SimpleFileCollection.getDocuent() : ",ioe);
		}
		currentStream = in;
		return makeDocument(filename, in);

	}


	/** Given the opened document in, of Filename and File f, work out which
	  * parser to try, and instantiate it. If you wish to use a different
	  * constructor for opening documents, then you need to subclass this method.
	  * @param Filename the filename of the currently open document
	  * @param in The stream of the currently open document 
	  * @return Document object to parse the document, or null if no suitable parser
      * exists.*/
	protected Document makeDocument(String Filename, InputStream in)
	{
		if (Filename == null || in == null)
			return null;
		String[] splitStr = Filename.split("\\.");
		String ext = splitStr[splitStr.length-1].toLowerCase();
		Class<? extends Document> reader = extension_DocumentClass.get(ext);
		Document rtr = null;
		
		/*If a document doesn't have an associated parser,
		  check the default one */
		if (reader == null) {
			reader = extension_DocumentClass.get("|DEFAULT|");
		} 
		/*if there is no default parser, then tough luck for that file,
		  but it's ignored */
		if (reader == null) {
			logger.warn("No available parser for file " + Filename + ", file is ignored.");
			return null;
		}
		logger.debug("Using "+reader.getName() + " to read "+ Filename);
			
		/* now attempt to instantiate the class */	
		try{
			Map<String,String> docProperties = new HashMap<String,String>(5);
			docProperties.put("filename", Filename);
			//and instantiate
			rtr = reader.getConstructor(InputStream.class, Map.class, Tokeniser.class).newInstance(in, docProperties, tokeniser);
			indexedFiles.add(thisFilename);
			rtr.getAllProperties().put("docno", this.getDocid());
		}catch (OutOfMemoryError e){
			logger.warn("Problem instantiating a document class; Out of memory error occured: ",e);			
			System.gc();
		}catch (StackOverflowError e){
			logger.warn("Problem instantiating a document class; Stack Overflow error occured: ",e);			
		}catch (Exception e){
			logger.warn("Problem instantiating a document class: ",e);			
		}
		return rtr;
	}

	/** 
	 * Checks whether there are more documents in the colection.
	 * @return boolean true if there are no more documents in the collection,
	 *         otherwise it returns false.
	 */	
	public boolean endOfCollection()
	{
		return (FileList.size() == 0);
	}

	/** 
	 * Starts again from the beginning of the collection.
	 */
	public void reset()
	{
		Docid = 0;
		FileList = new LinkedList<String>(firstList);
		indexedFiles = new ArrayList<String>();
	}

	/** 
	 * Returns the current document's identifier string.
	 * @return String the identifier of the current document.
	 */	
	public String getDocid()
	{
		return Docid+"";
	}

	@Override
	public void close() 
	{
		if (currentStream != null)
		{
			try{
				currentStream.close();
			} catch (IOException ioe) {
					logger.error("Exception occured while trying to close an IO stream",ioe);
			}
		}
	}

	/** Returns the ist of indexed files in the order they were indexed in. */
	public List<String> getFileList()
	{
		return indexedFiles;
	}

	/** Called when <tt>thisFile</tt> is identified as a directory, this adds the entire
	  * contents of the directory onto the list to be processed. */
	protected void addDirectoryListing()
	{
		//File[] contents = thisFile.listFiles();
		String[] dirContents = Files.list( thisFilename );
		if (dirContents == null)
			return;
		for(String e : dirContents)
		{
			if (e.equals(".") || e.equals(".."))
				continue;
			FileList.add(thisFilename + ApplicationSetup.FILE_SEPARATOR + e);
		}
		/*for(int i=0;i<contents.length;i++)
		{
			FileList.add(contents[i].getAbsolutePath());
		}*/
	}

	/**
	 * Simple test case. Pass the filename of a file that lists files
	 * to be processed to this test case.
	 */
	public static void main(String[] args) {
		Indexer in = new BasicIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
		in.createDirectIndex(new Collection[] {new SimpleFileCollection(args[0])});
		in.createInvertedIndex();
	}

}
