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
 * The Original Code is SimpleMedlineXMLCollection.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Ben He <ben{a.}dcs.gla.ac.uk> (original author) 
 */
package org.terrier.indexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

/** Initial implementation of a class that generates a Collection with Documents from a 
  * series of XML files in the Medline format. It process a limited number of documents 
  * in an XML file to avoid OutOfMemory problem in case the XML file is too large.<p>
  * <b>Properties:</b>&lt;ul&gt;
  * <li><tt>lowercase</tt> - lower case all terms obtained. Highly recommended.</li>
  * <li><tt>indexing.simplexmlcollection.reformxml</tt> - will try to reform broken &amp;AMP; entities.</li>
  * <li><tt>xml.doc.buffer.size</tt> - The maximum number of documents to process per interation.</li>
  * @author Ben He
  */
public class SimpleMedlineXMLCollection extends SimpleXMLCollection
{
	/** The BufferedReader of the XML file to be processed. */
	private BufferedReader br = null;
	/** The name of the currently processed XML file. */
	private String currentFilename = null;
	/** The number of documents processed in the current XML file. */
	protected int currentFileDocCounter = 0;
	/** The tag of documents in the XML files. */
	public final String docTag = "<MedlineCitation ";
	/** The end tag of documents in the XML files. */
	public final String docEndTag = "</MedlineCitation>";
	/** The tag indicating the start of an XML file. */
	public final String fileTag = "<MedlineCitationSet>";
	/** The tag indicating the end of an XML file. */
	public final String fileEndTag = "</MedlineCitationSet>";
	/** The end of line string. */
	public final String EOL = ApplicationSetup.EOL;
	/** The number of documents to process per iteration. */
	protected final int NUMBER_OF_DOCS_IN_BUFFER = Integer.parseInt(ApplicationSetup.getProperty("xml.doc.buffer.size", "2000"));
	/**
	 * The default constructor.
	 */
	public SimpleMedlineXMLCollection()
	{
		super();
	}
	/**
	 * An alternative constructor.
	 * @param CollectionSpecFilename The name of the file containing the location of XML files in the collection.
	 * @param BlacklistSpecFilename The name of the file containing the location of the blacklisted XML files
	 * in the collection.
	 */
	public SimpleMedlineXMLCollection(String CollectionSpecFilename, String BlacklistSpecFilename)
	{
		super(CollectionSpecFilename, BlacklistSpecFilename);
	}

	/**
	 * Parse through up to a limited number of documents in the XML file. The limit is
	 * specified by property <tt>xml.doc.buffer.size</tt>.
	 */
	protected boolean openNextFile()
	{
		if (FilesToProcess.size() == 0&&br==null)
			return false;
		if (br == null) {
			currentFilename = (String)FilesToProcess.removeFirst();
			try{
				br = Files.openFileReader(currentFilename);
				currentFileDocCounter=0;
			}catch(IOException ioe){
				logger.error("Could not open next file", ioe);
				throw new RuntimeException(ioe);
			}
		}
		String filename = currentFilename+".tmp.gz";
		BufferedWriter bw = null;
		int docCounter = 0;
		int foundBefore = currentFileDocCounter;
		try{
			bw = (BufferedWriter)Files.writeFileWriter(filename);
			// create the temporiary file
			bw.write(fileTag+EOL);
			String str = null;
			// read until the first doc
			while ((str=br.readLine())!=null){
				str = str.trim();
				if (str.startsWith(docTag)){
					bw.write(str+EOL);
					break;
				}
			}
			String currentStr = str;
			if (str == null)
				currentStr = "";
			while ((str=br.readLine())!=null){
				currentStr = str.trim();
				if (currentStr.startsWith(docEndTag)){
					// write the line, count++
					bw.write(currentStr+EOL);
					docCounter++;
					currentFileDocCounter++;
					if (docCounter == NUMBER_OF_DOCS_IN_BUFFER){
						break;
					}
				}else{
					bw.write(currentStr+EOL);
				}
			}
			
			
			// set br = null if end of file
			if (str==null)
				br = null;
			if (!currentStr.startsWith(fileEndTag)) 
				bw.write(fileEndTag);
			bw.close(); bw = null;
		}catch(IOException ioe){
			logger.warn("Problem reading", ioe);
		}
		
		if(logger.isDebugEnabled()){
			logger.debug("Processing file "+currentFilename+" for docs "+foundBefore+" - "+currentFileDocCounter);
		}
		try{

			if(bReformXML)
			{
				//this replaces all &AMP; with &amp;
				/* NB: This operation MAY be dangerous, as it MAY disrupt the encoding
				 * of strings in the document while copying
				 * Use at your own discretion, and test thoroughly.
				 * TODO check */
				if(logger.isDebugEnabled()){
					logger.debug("Copying xml to temporary file");
				}
				
				
				File temp = File.createTempFile("simpleMedlineXMLcollection", ".xml");
				Files.copyFile(new File(filename), temp);
				
				//if(logger.isDebugEnabled()){
					//logger.debug("parsing "+temp.getAbsoluteFile());
				//}
				xmlDoc = dBuilder.parse(temp);
				if (! temp.delete())
					logger.debug("Problem delete temp file");
			}
			else
			{
				xmlDoc = dBuilder.parse(Files.openFileStream(filename));
			}
		} catch (org.xml.sax.SAXException saxe) {
			logger.error("WARNING: Error parsing XML file "+ filename+ " : ", saxe);
			return openNextFile(); //bad: Recursion
		}
		catch (IOException ioe) {
			logger.error("WARNING: Error opening XML file "+ filename+ " : ",ioe);
			return openNextFile(); //bad: recursion
		}
		
		if (DocumentTags)
		{
			findDocumentElement(xmlDoc);
		}
		else
		{
			Documents.addLast(new XMLDocument(xmlDoc));
		}
		if(logger.isInfoEnabled()){
			logger.info("Found "+Documents.size() + " more documents in "+currentFilename);
		}
		xmlDoc = null;
		// remove the temporiary file
		if (!(new File(filename)).delete()){
			logger.error("Unable to delete file "+filename);
		}
		filename = null;		
		return true;
	}

}
