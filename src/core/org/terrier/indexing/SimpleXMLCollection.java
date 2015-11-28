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
 * The Original Code is SimpleXMLCollection.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author) 
 */


package org.terrier.indexing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.indexing.tokenisation.TokenStream;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

/** Initial implementation of a class that generates a Collection with Documents from a 
  * series of XML files.<p>
  * <b>Properties:</b><ul>
  * <li><tt>indexing.simplexmlcollection.reformxml</tt> - will try to reform broken &amp;AMP; entities.</li>
  * <li><tt>xml.blacklist.docids</tt> - docnos of documents that will not be indexed.</li>
  * <li><tt>xml.doctag</tt> - tag that marks a document.</li>
  * <li><tt>xml.idtag</tt> - tag that contains the docno. Attribute are specified as "element.attribute".</li>
  * <li><tt>xml.terms</tt> - list of tags whose children contain terms that should be indexed.</li>
  * </ul>
  */
public class SimpleXMLCollection implements Collection
{
	protected static final Logger logger = LoggerFactory.getLogger(SimpleXMLCollection.class);
	/**
	 * element attribute separator
	 */
	public static final String ELEMENT_ATTR_SEPARATOR = ".";

	/** Reform invalid XML by copying to temporary file. NB This may be dangerous */	
	protected static final boolean bReformXML = Boolean.parseBoolean(
			ApplicationSetup.getProperty("indexing.simplexmlcollection.reformxml", "false"));
	
	class XMLDocument implements Document
	{
		private ArrayList<String> terms = new ArrayList<String>();
		private String currentHolder = null;
		private int termNumber = 0;
		protected String ThisDocId = null;
		
		protected Tokeniser tokeniser = Tokeniser.getTokeniser();
		protected TokenStream currentTokenStream;
		
		protected Map<String,String> props = new HashMap<String,String>();
		
		public XMLDocument(Node root)
		{
			//for (String s : PropertyElements.keySet()) System.err.println(s);
			
			this.doRecursive(root);
			if (logger.isDebugEnabled())
				logger.debug("Found " + terms.size() + " terms");
			/* by the time we're here, the entire document has been parsed
			 * all terms are in the terms list */
		}

		/** Process all child nodes of Node p, recursively. 
		  * @param p the node to process */
		private void doRecursive(Node p) {
			if (p == null) {
				return;
			}
			//System.err.println("doRecursive for " + p.getNodeName());
			NamedNodeMap attributes = p.getAttributes();
			for(int i=0; i<attributes.getLength(); i++) {
				Node n = attributes.item(i);
				if (n == null) {
					continue;
				}
				//System.err.println("ATTRIBUTE " + p.getNodeName() + " - " + n.getNodeName());				
				this.doNode(n, p);
			}
			
			NodeList nodes = p.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.item(i);
				if (n == null) {
					continue;
				}
				//System.err.println("CHILD " + p.getNodeName() + " - " + n.getNodeName());				
				this.doNode(n, p);
			}
			if (nodes.getLength() != 0 && !holderStack.isEmpty()) {
				//System.out.println("End node : " + p.getNodeName());
				//System.out.println("Length : " + nodes.getLength());
				holderStack.removeLast();
			}
 		}

		//boolean innerState = false, trigger = false;
		private LinkedList<String> holderStack = new LinkedList<String>();		
		/** Processes a node: eg Text, element, attribute. */
		private void doNode(Node n, Node parent)
		{
			String parentNodeName = parent.getNodeName();
			switch(n.getNodeType()) {
			case Node.ELEMENT_NODE:
				if (TermElements.contains(parentNodeName.toLowerCase()) || PropertyElements.containsKey(parentNodeName.toLowerCase()) || !holderStack.isEmpty()) {
					//if (PropertyElements.containsKey(parentNodeName.toLowerCase())) System.err.println("Found "+parentNodeName+" as property");
					pushHolder(parentNodeName);
					currentHolder = null;
				}
				this.doRecursive(n);
				break;
			case Node.TEXT_NODE:
				//System.err.println("TEXT NODE: " + parentNodeName.toLowerCase());
				if(! DocIdIsAttribute && parentNodeName.toLowerCase().equals(DocIdLocation))
					ThisDocId = n.getNodeValue();

				if (TermElements.contains(parentNodeName.toLowerCase()) || !holderStack.isEmpty())
				{
					currentHolder = parentNodeName;
					doText(n.getNodeValue());
					currentHolder = null;
				}
				
				if (PropertyElements.containsKey(parentNodeName.toLowerCase())) {
					doProperty(parentNodeName, n.getNodeValue());
				}
				
				break;
			case Node.ATTRIBUTE_NODE:	
				
				if (DocIdIsAttribute || TermsInAttributes || PropertiesInAttibutes)
				{
					String attributeName = (parentNodeName + 
						ELEMENT_ATTR_SEPARATOR + n.getNodeName()).toLowerCase();
					//System.err.println("ATTRIBUTE NODE " + attributeName+" "+n.getNodeValue());
					if(DocIdIsAttribute && attributeName.equals(DocIdLocation))
						ThisDocId = n.getNodeValue().replaceAll("\n","");
					if(PropertiesInAttibutes && PropertyElements.containsKey(attributeName))
					{
						doProperty(attributeName, n.getNodeValue());
					}
					if(TermsInAttributes && TermElements.contains(attributeName))
					{
						currentHolder = attributeName;
						doText(n.getNodeValue());
						currentHolder = null;
					}
					
				}
				break;
			case Node.CDATA_SECTION_NODE:
				//System.err.println("CDATA NODE");
				if (TermElements.contains(parentNodeName.toLowerCase()))
				{
					currentHolder = parentNodeName;
					doText(n.getNodeValue());
					currentHolder = null;
				}
				
				if (PropertyElements.containsKey(parentNodeName.toLowerCase())) {
					doProperty(parentNodeName, n.getNodeValue());
				}
				
				break;
			default:
				if(logger.isDebugEnabled()){
				logger.debug("OTHER NODE"+ n.getNodeType() + " : "+ n.getClass());
				}
			}		
		}
		
		private LinkedList<Set<String>> fields = new LinkedList<Set<String>>();
		private Set<String> currentFields = new HashSet<String>(); 
		
		private void pushHolder(String field) {
			if (holderStack.contains(field)) {
				while (holderStack.getLast() != field) {
					holderStack.removeLast();
				}
			}
			else 
				holderStack.add(field);
		}
		
		private void pushField(String field) {
			pushHolder(field);
			HashSet<String> set = new HashSet<String>();
			set.addAll(holderStack);
			fields.add(set);
		}

		/** Breaks up String t into terms, and saves them */
		private void doText(String t)
		{
			if (t == null || t.length() == 0) {
				pushField(currentHolder);
				return;
			}
			currentTokenStream = tokeniser.tokenise(new StringReader(t));			
			while (currentTokenStream.hasNext()) {
				String term = currentTokenStream.next();
				if (term != null) {
					terms.add(term);
					pushField(currentHolder);
				}
			}
		}
		
		private void doProperty(String nodeName, String value) {
			if (nodeName != null & nodeName.length() > 0 && value != null && value.length() > 0) {
				
				// if properties already has something with this key
				// append value
				if (props.containsKey(nodeName)) {
					value = props.get(nodeName) + value;
				}
				
				// truncate value to max key length
				value = value.substring(0, Math.min(value.length(), PropertyElements.get(nodeName.toLowerCase()).intValue()));

				// add to properties HashMap
				props.put(nodeName, value);
			}
		}

		/** Returns true if no more terms can be fetched from this document */
		public boolean endOfDocument()
		{
			return termNumber == terms.size();
		}

		/** Returns a single item set, containing the name of the current node or attribute */
		public Set<String> getFields(){
			return currentFields;
		}

		/** Returns the next term from the document */
		public String getNextTerm()
		{
			currentFields = fields.get(termNumber);
			return terms.get(termNumber++);
		}
		public Reader getReader(){ return null; }
	
		public String getProperty(String name){
			Map<String, String> props = this.getAllProperties();
			return props.get(name);			
		}
		
		public Map<String,String> getAllProperties(){
			// make sure props always has docno
			props.put("docno", ThisDocId);
			return props;
		}

	}

	/* We need to define:
	 * what elements contains an entire document
	 * where the docid is
	 * what elements contain terms
	 * what attributes of which elements also contain terms
	*/

	/** Contains the names of tags that encapsulate entire documents */	
	protected HashSet<String> DocumentElements = new HashSet<String>();
	/** Set if DocumentElements.size &gt; 0 */
	protected boolean DocumentTags = false;
	/** Contains the names of tags and attributes that encapsulate terms */
	protected HashSet<String> TermElements = new HashSet<String>();
	/** Contains the name of the tag that contains the document name */
	protected String DocIdLocation = "DOCNO";
	/** set if DocIdLocation contains ELEMENT_ATTR_SEPARATOR */
	protected boolean DocIdIsAttribute = false; //set if DocIdLocation contains ELEMENT_ATTR_SEPARATOR
	/** set if any TermElements contains ELEMENT_ATTR_SEPARATOR */
	protected boolean TermsInAttributes = false;//set if any TermElements contains ELEMENT_ATTR_SEPARATOR
	
	/** set if any PropertyElements contains ELEMENT_ATTR_SEPARATOR */
	protected boolean PropertiesInAttibutes = false;//set if any MetaElements contains ELEMENT_ATTR_SEPARATOR
	
	/** Contains the names of tags and attributes that encapsulate meta properties with their lengths */
	//protected HashSet<String> PropertyElements = new HashSet<String>();
	protected Map<String,Integer> PropertyElements = new HashMap<String,Integer>();
	
	/** The xml parser factory for DOM */
	protected DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	/** the xml parser */
	protected DocumentBuilder dBuilder = null;
	/** the parsed structure of the XML file we currently have open */
	protected org.w3c.dom.Document xmlDoc = null;
	
	/** A list of all the document objects in this XML file */
	protected LinkedList<XMLDocument> Documents = new LinkedList<XMLDocument>();
	/** the current XML document that is being read by the indexer */
	protected XMLDocument thisDoc = null;
	/* set if no more documents in the current file and no more files left to process */
	protected boolean EOC = false;
	
	/**
	 * A black list of document to ignore.
	 */
	protected HashSet<String> DocIDBlacklist = new HashSet<String>();
	/**
	 * The list of files to process.
	 */
	protected LinkedList<String> FilesToProcess = new LinkedList<String>();
	/** 
	 * Construct a SimpleXMLCollection
	 * @param filesToProcess
	 */
	public SimpleXMLCollection(List<String> filesToProcess)
	{
		FilesToProcess.addAll(filesToProcess);
		initialiseTags();
		initialiseParser();
	}
	/** 
	 * Construct a SimpleXMLCollection
	 */
	public SimpleXMLCollection()
	{
		this(ApplicationSetup.COLLECTION_SPEC, 
			ApplicationSetup.makeAbsolute(
				ApplicationSetup.getProperty("xml.blacklist.docids", ""), 
					ApplicationSetup.TERRIER_ETC));
	}
	/** 
	 * Construct a SimpleXMLCollection
	 * @param CollectionSpecFilename
	 * @param BlacklistSpecFilename
	 */
	public SimpleXMLCollection(String CollectionSpecFilename, String BlacklistSpecFilename)
	{
		//load up the list of files to be processed from the collection.spec
		//reads the collection specification file
		try {
			BufferedReader br = Files.openFileReader(CollectionSpecFilename); 
			String filename = null;
			while ((filename = br.readLine()) != null) {
				if (!filename.startsWith("#") && !filename.equals(""))
					FilesToProcess.addLast(filename);
			}
			br.close();
			if(logger.isInfoEnabled()){
			logger.info("Finished reading collection specification");
			}
		} catch (IOException ioe) {
			logger.error("Input output exception while loading the collection.spec file. "
				+ "("+CollectionSpecFilename+").", ioe);
		}
		
		//reads the trec_blacklist_docid file
		if (BlacklistSpecFilename != null && BlacklistSpecFilename.length() >0)
		{
			try {
				if (Files.exists(BlacklistSpecFilename)) {
					BufferedReader br =Files.openFileReader(BlacklistSpecFilename); 
					String blackListedDocid = null;
					while ((blackListedDocid = br.readLine()) != null) {
						if (!blackListedDocid.startsWith("#")
								&& !blackListedDocid.equals(""))
							DocIDBlacklist.add(blackListedDocid);
					}
					br.close();					
				}
			} catch (IOException ioe) {
				logger.error("Input/Output exception while reading the document black list."
							+ "Stack trace follows", ioe);
			}
		}
		
		
		initialiseTags();
		initialiseParser();
		
	}
	
	protected void initialiseParser()
	{
		try{
			dbFactory.setValidating(false);
			dBuilder = dbFactory.newDocumentBuilder();
			dBuilder.setErrorHandler(new DefaultHandler());
		}catch (javax.xml.parsers.ParserConfigurationException pce) {
			logger.error("ERROR: Couldn't build a DOM parser : ",pce);
		}
	}
	
	protected void initialiseTags()
	{
		//get all the stuff from the properties file

		//1. document elements
		String[] docElements = ApplicationSetup.getProperty("xml.doctag", "DOC").split("\\s*,\\s*");
		for(int i=0;i<docElements.length;i++)
			DocumentElements.add(docElements[i].trim().toLowerCase());

		if(DocumentElements.size() > 0)
			DocumentTags = true;

		//2. what's the name of the docno tag
		DocIdLocation = ApplicationSetup.getProperty("xml.idtag", "DOCNO").trim().toLowerCase();
		if(DocIdLocation.indexOf(ELEMENT_ATTR_SEPARATOR) != -1)
			DocIdIsAttribute = true;
			
		//3. term elements
		String[] termElements = ApplicationSetup.getProperty("xml.terms", "").toLowerCase().split("\\s*,\\s*");
		for(int i=0;i<termElements.length;i++)
		{
			TermElements.add(termElements[i].trim());
			if (termElements[i].indexOf(ELEMENT_ATTR_SEPARATOR) != -1)
				TermsInAttributes = true;			
		}
		
		//4. property elements and lengths
		String[] propertyElements = ApplicationSetup.getProperty("indexer.meta.forward.keys", DocIdLocation).toLowerCase().split("\\s*,\\s*");
		String[] propertyLengths = ApplicationSetup.getProperty("indexer.meta.forward.keylens", "20").toLowerCase().split("\\s*,\\s*");
				
		if (propertyElements.length != propertyLengths.length) {
			logger.error("Number of keys in indexer.meta.forward.keys must equal number of lengths specified in indexer.meta.forward.keylens");
		}
		
		// add to HashMap
		for(int i=0;i<propertyElements.length;i++) {
			Integer keyLen = Integer.parseInt(propertyLengths[i]);
			PropertyElements.put(propertyElements[i].trim(), keyLen);
			if (propertyElements[i].indexOf(ELEMENT_ATTR_SEPARATOR) != -1)
				PropertiesInAttibutes = true;	
		}
	}
	
	/** 
	 * {@inheritDoc} This is not supported in this implemented class.
	 */
	public void close()
	{
		//TODO
	}
	/**
	 * Chech whether there is a next document in the collection
	 * @return boolean
	 */
	public boolean hasNext() {
		return ! endOfCollection();
	}
	/**
	 * get the next document
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
	 * {@inheritDoc} 
	 */
	public boolean endOfCollection()
	{
		return EOC;	
	}

	/** 
	 * {@inheritDoc} 
	 */
	@Override
	public boolean nextDocument()
	{//get into the state where getDocument() will return the next document
		if (EOC)
			return false;

		while (Documents.size() == 0)
			// keep opening the next file until no more document found.
			if (!openNextFile())
				break;
		
		if (Documents.size() > 0)
		{
			thisDoc = (XMLDocument)Documents.removeFirst();
			if (DocIDBlacklist.contains(thisDoc.ThisDocId))
				return nextDocument();//hmm, bad recursion
			return true;
		}
		EOC = true;
		return false;
	}

	protected boolean findDocumentElement(Node n)
	{
		if(n == null)
			return false;
		
		if (DocumentElements.contains(n.getNodeName().toLowerCase()) && n.getNodeType() != Node.DOCUMENT_TYPE_NODE)
		{
			Documents.addLast(new XMLDocument(n));
			return true;//assume no other documents are contained in documents
		}

		NodeList nodes = n.getChildNodes();
		for(int i=0;i<nodes.getLength();i++)
		{
			Node c = nodes.item(i);
			if(n == null)
				continue;
			findDocumentElement(c);
		}
		return false; //dont think this rtr really matters	
	}

	/** 
	 * {@inheritDoc} 
	 */
	public Document getDocument()
	{
		return (Document)thisDoc;
	}

	/** 
	 * {@inheritDoc}. This Collection implementation does not support reset. 
	 */
	public void reset()
	{
		//not implemented
	}

	protected boolean openNextFile()
	{
		if (FilesToProcess.size() == 0)
			return false;
		String filename = (String)FilesToProcess.removeFirst();
		if(logger.isDebugEnabled()){
			logger.debug("Processing file "+filename);
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
				
				
				File temp = File.createTempFile("simpleXMLcollection", ".xml");
				Files.copyFile(new File(filename), temp);
				
				if(logger.isDebugEnabled()){
					logger.debug("parsing "+temp.getAbsoluteFile());
				}
				xmlDoc = dBuilder.parse(temp);
				if (! temp.delete())
				{
					logger.debug("Temporary file could not be deleted");
				}
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
			logger.info("Found "+Documents.size() + " documents in "+filename);
		}
		xmlDoc = null;
		return true;
	}

	/**
	 * main
	 * @param args
	 */
	public static void main(String[] args) throws IOException
	{
		Collection c = new SimpleXMLCollection();
		while(c.nextDocument())
		{	
			Document d = c.getDocument();
			
			if(logger.isInfoEnabled()){
				logger.info("DOCID: "+d.getProperty("docno"));
			}
			if(logger.isInfoEnabled()){
				while(! d.endOfDocument())
				{
					System.out.println(d.getNextTerm());
				}
			}
		}
		c.close();
	}

}
