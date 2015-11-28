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
 * The Original Code is TRECCollection.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author) 
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richardm{a.}dcs.gla.ac.uk>
 */
package org.terrier.indexing;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.terrier.utility.LookAheadStream;
import org.terrier.utility.LookAheadStreamCaseInsensitive;
import org.terrier.utility.TagSet;
import org.terrier.utility.io.CountingInputStream;
/**
 * Models a TREC test collection by implementing the interfaces
 * Collection and DocumentExtractor. It provides sequential access
 * to the documents in the collection and also it can return the text
 * of a document as a String. The precise {@link Document} class to be
 * used can be specified with the <tt>trec.document.class</tt> property.
 * 
 * TREC format files are opened using the default encoding unless the
 * <tt>trec.encoding</tt> has been set to a valid supported encoding.
 * 
 * Since 3.5, the contents of tags can be added to the meta index instead of being indexed normally. This is useful
 * to hold URLs or dates that you need to later during retrieval. To use this, the fields in the TREC file
 * need to be ordered and the tags to add need to be specified in <tt>TrecDocTags.propertytags</tt> and <tt>indexer.meta.forward.keys</tt>
 * and the maximum length of the tags given in <tt>indexer.meta.forward.keylens</tt>.
 * 
 * <p><b>Properties</b>:
 * <ul> You also need to 
 * add the keys to indexer.meta.forward.keys and give them lengths (indexer.meta.forward.keylens)
 * <li><tt>trec.document.class</tt> the {@link Document} class to parse individual documents (defaults to {@link TaggedDocument}).</li> 
 * <li><tt>trec.encoding</tt> - encoding to use to open all files. Leave unset for System default encoding.</li>
 * <li><tt>(tagset).propertytags</tt> - list of tags to add to the meta index rather than to index. Tags are assumed to be IN ORDER after the docid.</li>
 * <li><tt>indexer.meta.forward.keys</tt> - list of keys to add to the meta index, remember to put any property tags here as well.</li>
 * <li><tt>indexer.meta.forward.keylens</tt> - lengths of each of the the meta keys, remember to put the lengths of the property tags here as well.</li>
 * </ul>
 * @author Craig Macdonald &amp; Vassilis Plachouras &amp; Richard McCreadie
 */
public class TRECCollection extends MultiDocumentFileCollection {
	
	/** Tag names for tags that should be added as properties **/
	String[] propertyTags = new String[0];

	/**
	 * Counts the documents that are found in the collection, ignoring those
	 * documents that appear in the black list
	 */
	protected int documentCounter = 0;

	protected HashSet<String> DocIDBlacklist = new HashSet<String>();

	/** The string identifier of the current document.*/
	protected String ThisDocID;
	/** The inputstream used for reading data.*/
	protected CountingInputStream br;
	
	/** The opening document tag.*/
	protected char[] start_docTag;
	/** The length of the opening document tag.*/
	protected int start_docTagLength;
	/** The closing document tag.*/
	protected String end_docTag;
	/** The length of the closing document tag.*/
	protected int end_docTagLength;
	/** The opening document number tag.*/
	protected char[] start_docnoTag;
	/** The length of the opening document number tag.*/
	protected int start_docnoTagLength;
	/** The closing document number tag.*/
	protected char[] end_docnoTag;
	/** The length of the closing document number tag.*/
	protected int end_docnoTagLength;
	/** Is the markup case-sensitive? */
	protected boolean tags_CaseSensitive;
	/** Do we ignore properties? */
	protected boolean ignoreProperties = false;
	/** The docno tag */
	protected String docnotag;
	/** The length of each property tag */
	protected int[] propertyTagLengths;
	/** The start property tags */
	protected char[][] startPropertyTags;
	/** The end property tag */
	protected char[][] endPropertyTags;


	/**
	 * protected method for initialising the
	 * opening and closing document and document number
	 * tags.
	 */
	protected void setTags(String TagSet)
	{
		TagSet tagSet = new TagSet(TagSet);
		tags_CaseSensitive = tagSet.isCaseSensitive();
		docnotag = tagSet.getDocTag();
		String tmpDocTag = "<" + tagSet.getDocTag() + ">";
		String tmpEndDocTag = "</" + tagSet.getDocTag() + ">";
		String tmpDocnoTag = "<" + tagSet.getIdTag() + ">";
		String tmpEndDocnoTag = "</" + tagSet.getIdTag() + ">";
		start_docTag = tmpDocTag.toCharArray();
		start_docTagLength = start_docTag.length;
		start_docnoTag = tmpDocnoTag.toCharArray();
		start_docnoTagLength = start_docnoTag.length;
		end_docTag = tmpEndDocTag;
		end_docTagLength = end_docTag.length();
		end_docnoTag = tmpEndDocnoTag.toCharArray();
		end_docnoTagLength = end_docnoTag.length;

		// TREC-178: Adding in property tags that can be added to the meta index
		if (ApplicationSetup.getProperty(TagSet+".propertytags", "").compareTo("")==0) ignoreProperties = true;
		else {
			// The user has specified some special tags to add to the Meta Index
			this.propertyTags = ApplicationSetup.getProperty(TagSet+".propertytags", "").split(",");
			// for each tag
			propertyTagLengths = new int[this.propertyTags.length];
			startPropertyTags = new char[this.propertyTags.length][];
			endPropertyTags = new char[this.propertyTags.length][];
			for (int t = 0; t<this.propertyTags.length; t++) {
				// store the length and start and end tags so we don't need to do this later
				startPropertyTags[t] = ("<" + this.propertyTags[t] + ">").toCharArray();
				propertyTagLengths[t] = startPropertyTags[t].length;
				endPropertyTags[t] = ("</" + this.propertyTags[t] + ">").toCharArray();
			}
		}
		
		logger.debug("There are "+propertyTags.length+" special property fields to be added to the meta index.");
	}

	protected void readDocumentBlacklist(String BlacklistSpecFilename)
	{
		//read the document blacklist
		if (BlacklistSpecFilename != null && BlacklistSpecFilename.length() >0)
		{
			try {
				DocIDBlacklist = new HashSet<String>();
				if (Files.exists(BlacklistSpecFilename)) {
					BufferedReader br2 = Files.openFileReader(BlacklistSpecFilename);
					String blackListedDocid = null;
					while ((blackListedDocid = br2.readLine()) != null) {
						blackListedDocid = blackListedDocid.trim();
						if (!blackListedDocid.startsWith("#")
								&& !blackListedDocid.equals(""))
							DocIDBlacklist.add(blackListedDocid);
					}
					br2.close();
				}
			} catch (IOException ioe) {
				logger.error("Input/Output exception while reading the document black list.", ioe);
			}
		}
	}

	/** Specific constructor: reads the files listed in CollectionSpecFilename,
	 *  the Blacklist of Document IDs in BlacklistSpecFilename, and stores document
	 *  offsets and lengths in the document pointers file docPointersFilename. The collection
	 *  will be parsed according to the TagSet specified by TagSet string
	 *  @param CollectionSpecFilename The collections specification filename. The file contains
	 *  a list of filenames to read. Must be specified, fatal error otherwise.
	 *  @param TagSet the TagSet constructor string to use to obtain the tags to parse for.
	 *  @param BlacklistSpecFilename A filename to a file containing a list of document identifiers
	 *  thay have NOT to be processed. Not loaded if null or length 0
	 *  @param ignored no longer used
	*/
	public TRECCollection(String CollectionSpecFilename, String TagSet, String BlacklistSpecFilename,
		 String ignored) {
		
		loadDocumentClass();		
		setTags(TagSet);
		readCollectionSpec(CollectionSpecFilename);
		readDocumentBlacklist(BlacklistSpecFilename);
			
		//open the first file
		try {
			openNextFile();
		} catch (IOException ioe) {
			logger.error("IOException opening first file of collection - is the collection.spec correct?", ioe);
		}
	}

	/**
	 * A default constructor that reads the collection specification
	 * file, as configured by the property <tt>collection.spec</tt>,
	 * reads a list of blacklisted document numbers, specified by the
	 * property <tt>trec.blacklist.docids</tt> and opens the
	 * first collection file to process. TagSet TagSet.TREC_DOC_TAGS is used to tokenize
	 * the collection.
	 */
	public TRECCollection()
	{
		this(
			ApplicationSetup.COLLECTION_SPEC, 
			TagSet.TREC_DOC_TAGS, 
			ApplicationSetup.makeAbsolute(
				ApplicationSetup.getProperty("trec.blacklist.docids", ""), 
					ApplicationSetup.TERRIER_ETC), 
			ApplicationSetup.makeAbsolute(
				ApplicationSetup.getProperty("trec.collection.pointers", "docpointers.col"), 
					ApplicationSetup.TERRIER_INDEX_PATH)
			);
	}
	/**
	 * A constructor that reads only the document in the specificed
	 * InputStream. Also reads a list of blacklisted document numbers, specified by the
	 * property <tt>trec.blacklist.docids</tt> and opens the
	 * first collection file to process. */
	public TRECCollection(InputStream input)
	{
		super(input instanceof CountingInputStream ? (CountingInputStream)input : new CountingInputStream(input));
		loadDocumentClass(); 
		setTags(TagSet.TREC_DOC_TAGS); 
		readDocumentBlacklist(ApplicationSetup.makeAbsolute(
			ApplicationSetup.getProperty("trec.blacklist.docids", ""), 
		ApplicationSetup.TERRIER_ETC));
		
		documentsInThisFile = 0;
	}
	/**
	 * Check whether it is the end of the collection
	 * @return boolean
	 */
	public boolean hasNext() {
		return ! endOfCollection();
	}
	/**
	 * Return next document
	 * @return next document
	 */
	public Document next()
	{
		nextDocument();
		return getDocument();
	}

	
	/**
	 * Moves to the next document to process from the collection.
	 * @return boolean true if there are more documents to process in the 
	 *		 collection, otherwise it returns false.
	 */
	public boolean nextDocument() {
		//move the stream to the start of the next document
		//try next file if no DOC tag found. (and set endOfCOllection if
		//no files left)

		DocProperties = new HashMap<String,String>(15);

		/* state of the parser. This is equal to how many characters have been
		 * found of the currently desired string */
		int State = 0;
		// the most recently found character
		int c;
		boolean bScanning = true;
		scanning:
		while(bScanning)
		{
			try {
				State = 0;
				StringBuilder tagcontent = null;
				StringBuilder[] properties = new StringBuilder[propertyTags.length];
				//looking for doc tag
				while (State < start_docTagLength) {
					if ((c = br.read()) == -1) {
						//print a warning if no documents found in that file!
						if (documentsInThisFile == 0)
						{
							logger.warn(this.getClass().getSimpleName() + " found no documents in " + currentFilename + ". "
								+"Perhaps trec.collection.class is wrongly set, TrecDocTags are incorrect, or decompression failed");
						}
						
						if (openNextFile()) {
							continue scanning;//continue;
						} else {
							eoc = true;
							return false;
						}
					}
					char cc = (char)c;
					char cu = start_docTag[State];
					
					if (!tags_CaseSensitive) {
						cc= Character.toUpperCase((char)c);
						cu=Character.toUpperCase((char)cu);
					}
					
					if (cc==cu) {
						State++;
					} else {
						State = 0;
					}
				}
				//looking for docno tag
				tagcontent = getTag(start_docnoTagLength, start_docnoTag, end_docnoTag);
				if (tagcontent == null)
				{
					if (eoc) return false;
					continue scanning;
				}
				else 
					ThisDocID = tagcontent.toString().trim();				
				
				// now looking for useful property tags which should be stored as document properties
				// assumes that properties come after the docno, but before the document text
				
				// for each property
				if (!ignoreProperties){
					for (int pt = 0; pt<propertyTags.length; pt++) {
						tagcontent = getTag(propertyTagLengths[pt], startPropertyTags[pt], endPropertyTags[pt]);
						if (tagcontent == null) {
							if (eoc) return false;
							continue scanning;
						}
						else properties[pt] = tagcontent;
					}
				}
				
				DocProperties.put("charset", desiredEncoding);
				afterPropertyTags();
				
				//got all of the document, phew!
				
				State = 0;
				documentsInThisFile++;
				
				/* we check the document blacklist, and if docid matches
				 * move on to the next document */
				if (DocIDBlacklist.contains(ThisDocID)) {
					continue scanning;
				}
				
				DocProperties.put("docno", ThisDocID);
				DocProperties.put("filename", currentFilename);
				DocProperties.put("offsetInFile", Long.toString(br.getPos()));
				DocProperties.put("documentInFileIndex",  Integer.toString(documentsInThisFile));
				DocProperties.put("filenumber", Integer.toString(FileNumber));
				int propertyIndex = 0;
				for(StringBuilder property : properties) {
					DocProperties.put(propertyTags[propertyIndex], property.toString().trim());
					propertyIndex++;
				}
			} catch (IOException ioe) {
				
				logger.warn("Error Reading "
					+ currentFilename + " : " + ioe
					+ ", skipping rest of file", ioe);
				FileNumber++;
				SkipFile = true;
				if (eoc) return false;
				continue scanning;
			}
			bScanning = false;
		}
		return true;
	}
		
	protected void afterPropertyTags() throws IOException {
		
	}

	/**
	 * Scans through a document reading in the first occurrence of the specified tag,
	 * returning its contents as a StringBuilder object
	 * @param taglength - the length of the start tag
	 * @param startTag - the start tag
	 * @param endTag - the end tag
	 * @return - the tag contents
	 * @throws IOException
	 */
	protected StringBuilder getTag(int taglength, char[] startTag, char[] endTag) throws IOException {
		int readerState = 0;
		int c;
		StringBuilder string = new StringBuilder();
		while (readerState < taglength) {
			if ((c = br.read()) == -1) {
				if (openNextFile()) {
					logger.warn("Forced a skip (1: looking for open "+new String(startTag)+" tag) - is the collection corrupt or do the property tags exist?");
					continue;
				} else {
					eoc = true;
					return null;
				}
			}
			
			char cc = (char)c;
			char cu = startTag[readerState];
			
			if (!tags_CaseSensitive) {
				cc= Character.toUpperCase((char)c);
				cu=Character.toUpperCase((char)cu);
			}
			
			if (cc==cu)
				readerState++;
			else
				readerState = 0;
		}
		//looking for end of docno
		readerState = 0;
		while (readerState < (taglength+1)) {
			if ((c = br.read()) == -1) {
				if (openNextFile()) {
					logger.warn("Forced a skip (2: looking for end of "+new String(startTag)+" tag) - is the collection corrupt?");
					continue;
				} else {
					eoc = true;
					return null;
				}
			}
			
			char cc = (char)c;
			char cu = endTag[readerState];
			
			//System.err.println((char)cc+" "+cu);
			
			if (!tags_CaseSensitive) {
				cc= Character.toUpperCase((char)c);
				cu=Character.toUpperCase((char)cu);
			}
			
			if (cc==cu) {
				readerState++;
			} else {
				readerState = 0;
				string.append((char)c);
			}
		}
		return string;
		
	}
	
	protected void openNewFile() throws Exception 
	{
		if (is instanceof CountingInputStream)
			br = (CountingInputStream) is;
		else
			br = new CountingInputStream(is);
	}

	/**
	 * Returns the current document to process.
	 * @return Document the object of the current document to process.
	 */
	@Override
	public Document getDocument() {
		try{
			InputStream is = tags_CaseSensitive
		        ? new LookAheadStream(br, desiredEncoding == null ? end_docTag.getBytes() : end_docTag.getBytes(desiredEncoding))
		        : new LookAheadStreamCaseInsensitive(br, end_docTag);
	    	return documentClass.getConstructor(InputStream.class, Map.class, Tokeniser.class).newInstance(is, DocProperties, tokeniser);
	    } catch (Exception e) {
	    	throw new RuntimeException(e);
	    }
	}	


	@Override
	public void reset() {
		super.reset();
		ThisDocID = "";		
	}

}
