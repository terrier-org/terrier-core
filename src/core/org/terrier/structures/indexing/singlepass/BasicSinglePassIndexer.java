
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
 * The Original Code is BasicSinglePassIndexer.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Roi Blanco
 *  Craig Macdonald
 */

package org.terrier.structures.indexing.singlepass;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.terrier.indexing.Collection;
import org.terrier.indexing.Document;
import org.terrier.structures.BasicLexiconEntry;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.FSOMapFileLexiconOutputStream;
import org.terrier.structures.FieldDocumentIndexEntry;
import org.terrier.structures.FieldLexiconEntry;
import org.terrier.structures.Index;
import org.terrier.structures.LexiconOutputStream;
import org.terrier.structures.SimpleDocumentIndexEntry;
import org.terrier.structures.indexing.CompressionFactory.BitCompressionConfiguration;
import org.terrier.structures.indexing.DocumentIndexBuilder;
import org.terrier.structures.indexing.DocumentPostingList;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.structures.postings.bit.BasicIterablePosting;
import org.terrier.structures.postings.bit.FieldIterablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.FieldScore;
import org.terrier.utility.Files;
import org.terrier.utility.MemoryChecker;
import org.terrier.utility.RuntimeMemoryChecker;
import org.terrier.utility.UnitUtils;
/**
 * This class indexes a document collection (skipping the direct file construction). It implements a single-pass algorithm,
 * that operates in two phases:<br>
 * First, it traverses the document collection, passes the terms through the TermPipeline and builds an in-memory
 * representation of the posting lists. When it has exhausted the main memory, it flushes the sorted postings to disk, along
 * with the lexicon (collectively known as a <i>run</i>, and continues traversing the collection.<br>
 * The second phase, merges the sorted runs (with their partial lexicons) in disk to create the final inverted file.
 * This class follows the template pattern, so the main bulk of the code is reused for block (and fields) indexing. There are a few hook methods,
 * that chooses the right classes to instantiate, depending on the indexing options defined.
 * <p>
 * Memory tracking is a key concern in this class. Four properties are provided for checking the amount of memory
 * consumed, how regularly to check the memory, and (optional) maximums on the amount of memory that
 * can be used for the postings, or on the number of documents before a flush is comitted.
 * <p>
 * <b>Properties:</b>
 * <ul>
 * <li><tt>memory.reserved</tt> - amount of free memory threshold before a run is committed. 
 * Default is 50 000 000 (50MB) and 100 000 000 (100MB) for 32bit and 64bit JVMs respectively.</li>
 * <li><tt>memory.heap.usage</tt> - proportion of max heap allocated to JVM before a run is committed. Default 0.70.</li>
 * <li><tt>indexing.singlepass.max.postings.memory</tt> - maximum amount of memory that the postings can consume before a run is committed. Default is 0, which is no limit.</li>
 * <li><tt>indexing.singlepass.max.documents.flush</tt> - maximum number of documents before a run is committed. Default is 0, which is no limit.</li>
 * <li><tt>docs.check</tt> - interval of how many documents indexed should the amount of free memory be checked. Default is 20 - check memory consumption every 20 documents.</li>
 * </ul> 
 * @author Roi Blanco
 */
public class BasicSinglePassIndexer extends BasicIndexer{

	/** Current document Id */
	protected int currentId = 0;

	protected long maxMemory = 0;	
	
	/** Memory Checker - provides the method for checking to see if
	 * the system is running low on memory */
	protected MemoryChecker memoryCheck = null;
	
	/** Number of documents read per memory check */
	protected int docsPerCheck;
	
	protected int maxDocsPerFlush;
	
	/** Runtime system JVM running this instance of Terrier */
	protected static final Runtime runtime = Runtime.getRuntime();

	/** Number of documents read since the memory consumption was last checked */
	protected int numberOfDocsSinceCheck = 0;
	/** Number of documents read since the memory runs were last flushed to disk */
	protected int numberOfDocsSinceFlush = 0;
	/** Memory status after flush */
	protected long memoryAfterFlush = -1;
	/** Queue with the file names for the runs in disk */
	protected Queue<String[]> fileNames = new LinkedList<String[]>();
	/** Number of the current Run to be written in disk */
	protected int currentFile = 0;
	/** Structure that keeps the posting lists in memory */
	protected MemoryPostings mp;
	/** Structure for merging the run */
	protected RunsMerger merger;

	/** Number of documents indexed */
	protected int numberOfDocuments = 0;
	/** Number of tokens indexed */
	protected long numberOfTokens = 0;
	/** Number of unique terms indexed */
	protected int numberOfUniqueTerms = 0;
	/** Number of pointers indexed */
	protected long numberOfPointers = 0;
	/** what class should be used to read the generated inverted index? */
	protected String invertedIndexClass = org.terrier.structures.bit.BitPostingIndex.class.getName();
	protected String basicInvertedIndexPostingIteratorClass = BasicIterablePosting.class.getName();
	protected String fieldInvertedIndexPostingIteratorClass = FieldIterablePosting.class.getName();
	/** what class should be used to read the inverted index as a stream? */
	protected String invertedIndexInputStreamClass = org.terrier.structures.bit.BitPostingIndexInputStream.class.getName();
	/**
	 * Constructs an instance of a BasicSinglePassIndexer, using the given path name
	 * for storing the data structures.
	 * @param pathname String the path where the datastructures will be created. This is assumed to be
	 * absolute.
	 * @param prefix String the prefix of the index, usually "data".
	 */
	public BasicSinglePassIndexer(String pathname, String prefix) {
		super(pathname, prefix);
		//delay the execution of init() if we are a parent class
        if (this.getClass() == BasicSinglePassIndexer.class) 
            init();
        if (! (this.compressionInvertedConfig instanceof BitCompressionConfiguration ))
        {
        	throw new Error("Sorry, only default BitCompressionConfiguration is supported by " + this.getClass().getName() 
        			+ " - you can recompress the inverted index later using IndexRecompressor");
        }
	}

	/** Protected do-nothing constructor for use by child classes */
	protected BasicSinglePassIndexer(long a, long b, long c) {
		super(a,b,c);
	}


	@Override
	public void createDirectIndex(Collection[] collections) {
		createInvertedIndex(collections);
	}
	@Override
	public void createInvertedIndex(){}




	/**
	 *  Builds the inverted file and lexicon file for the given collections
	 * Loops through each document in each of the collections,
	 * extracting terms and pushing these through the Term Pipeline
	 * (eg stemming, stopping, lowercase).
	 *  @param collections Collection[] the collections to be indexed.
	 */
	public void createInvertedIndex(Collection[] collections) {
		logger.info("Creating IF (no direct file)..");
		long startCollection, endCollection;
		fileNames = new LinkedList<String[]>();	
		numberOfDocuments = currentId = numberOfDocsSinceCheck = numberOfDocsSinceFlush = numberOfUniqueTerms = 0;
		numberOfTokens = numberOfPointers = 0;
		createMemoryPostings();
		currentIndex = Index.createNewIndex(path, prefix);
		docIndexBuilder = new DocumentIndexBuilder(currentIndex, "document");
		metaBuilder = createMetaIndexBuilder();
		
		emptyDocIndexEntry = (FieldScore.FIELDS_COUNT > 0) ? new FieldDocumentIndexEntry(FieldScore.FIELDS_COUNT) : new SimpleDocumentIndexEntry();
		
		MAX_DOCS_PER_BUILDER = UnitUtils.parseInt(ApplicationSetup.getProperty("indexing.max.docs.per.builder", "0"));
		maxMemory = UnitUtils.parseLong(ApplicationSetup.getProperty("indexing.singlepass.max.postings.memory", "0"));
		final boolean boundaryDocsEnabled = BUILDER_BOUNDARY_DOCUMENTS.size() > 0;
		final int collections_length = collections.length;
		boolean stopIndexing = false;
		System.gc();
		memoryAfterFlush = runtime.freeMemory();
		logger.debug("Starting free memory: "+memoryAfterFlush/1000000+"M");

		for(int collectionNo = 0; ! stopIndexing && collectionNo < collections_length; collectionNo++)
		{
			Collection collection = collections[collectionNo];
			startCollection = System.currentTimeMillis();
			while(collection.nextDocument())
			//while(collection.hasNext())
			{
				/* get the next document from the collection */
				//Document doc = collection./next();
				Document doc = collection.getDocument();
				if (doc == null)
					continue;
				//numberOfDocuments++;
				/* setup for parsing */
				createDocumentPostings();

				String term; //term we're currently processing
				numOfTokensInDocument = 0;
				//get each term in the document
				while (!doc.endOfDocument()) {

					if ((term = doc.getNextTerm())!=null && !term.equals("")) {
						termFields = doc.getFields();
						/* pass term into TermPipeline (stop, stem etc) */
						pipeline_first.processTerm(term);
						/* the term pipeline will eventually add the term to this object. */
					}
					if (MAX_TOKENS_IN_DOCUMENT > 0 &&
							numOfTokensInDocument > MAX_TOKENS_IN_DOCUMENT)
						break;
				}
				//if we didn't index all tokens from document,
				//we need to get to the end of the document.
				while (!doc.endOfDocument())
					doc.getNextTerm();
				
				pipeline_first.reset();
				/* we now have all terms in the DocumentTree, so we save the document tree */
				try
				{
					if (termsInDocument.getDocumentLength() == 0)
					{	/* this document is empty, add the minimum to the document index */
						indexEmpty(doc.getAllProperties());
						if (IndexEmptyDocuments)
						{
							currentId++;
							numberOfDocuments++;
						}
					}
					else
					{	/* index this document */
						numberOfTokens += numOfTokensInDocument;
						indexDocument(doc.getAllProperties(), termsInDocument);
					}
				}
				catch (Exception ioe)
				{
					logger.error("Failed to index "+doc.getProperty("docno"),ioe);
					throw new RuntimeException(ioe);
				}

				if (MAX_DOCS_PER_BUILDER>0 && numberOfDocuments >= MAX_DOCS_PER_BUILDER)
				{
					stopIndexing = true;
					break;
				}

				if (boundaryDocsEnabled && BUILDER_BOUNDARY_DOCUMENTS.contains(doc.getProperty("docno")))
				{
					logger.warn("Document "+doc.getProperty("docno")+" is a builder boundary document. Boundary forced.");
					stopIndexing = true;
					break;
				}
				termsInDocument.clear();
			}
			
			try{
				forceFlush();
				endCollection = System.currentTimeMillis();
				long partialTime = (endCollection-startCollection)/1000;
				logger.info("Collection #"+collectionNo+ " took "+partialTime+ " seconds to build the runs for "+numberOfDocuments+" documents\n");
							
				
				
				docIndexBuilder.finishedCollections();
				if (FieldScore.FIELDS_COUNT > 0)
				{
					currentIndex.addIndexStructure("document-factory", FieldDocumentIndexEntry.Factory.class.getName(), "java.lang.String", "${index.inverted.fields.count}");
				}
				else
				{
					currentIndex.addIndexStructure("document-factory", SimpleDocumentIndexEntry.Factory.class.getName(), "", "");
				}
				metaBuilder.close();
				currentIndex.flush();
				
				logger.info("Merging "+fileNames.size()+" runs...");
				startCollection = System.currentTimeMillis();
				
				performMultiWayMerge();
				currentIndex.flush();
				endCollection = System.currentTimeMillis();
				logger.info("Collection #"+collectionNo+" took "+((endCollection-startCollection)/1000)+" seconds to merge\n ");
				logger.info("Collection #"+collectionNo+" total time "+( (endCollection-startCollection)/1000+partialTime));
				long secs = ((endCollection-startCollection)/1000);
				if (secs > 3600)
	                 logger.info("Rate: "+((double)numberOfDocuments/((double)secs/3600.0d))+" docs/hour");
			} catch (Exception e) {
				logger.error("Problem finishing index", e);
			}
		}
		finishedInvertedIndexBuild();
	}

	/** check to see if a flush is required, and perform if necessary */
	protected void checkFlush() throws IOException
	{
		if(numberOfDocsSinceCheck >= docsPerCheck)
			return;
		numberOfDocsSinceCheck = 0;
		final long consumed = mp.getMemoryConsumption();
		boolean doFlush = false;
		final boolean memCheck = memoryCheck.checkMemory();
		String msg = null; 
		logger.debug(msg="Run "+currentFile+" maxAllowedMemory="+maxMemory + " consumed="+consumed + " maxDocsPerFlush="+maxDocsPerFlush
				+" numberOfDocsSinceFlush="+numberOfDocsSinceFlush + " memcheck="+ memCheck);
		if (memCheck)
		{
			doFlush = true;
			msg += " (memory check threshold hit)";
		}
		if (maxDocsPerFlush > 0 && numberOfDocsSinceFlush >= maxDocsPerFlush)
		{
			msg += " (doc threhold hit)";
			doFlush = true;
		}
		if (maxMemory > 0 && consumed > maxMemory )
		{
			msg += " (posting memory threshold hit)";
			doFlush = true;
		}
		if (doFlush)
		{
			logger.info("Flush forced: " + msg);
			forceFlush();
		}
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
			value="DM_GC",
			justification="Forcing GC is an essential part of releasing" +
					"memory for further indexing")
	/** causes the posting lists built up in memory to be flushed out */
	protected void forceFlush() throws IOException
	{	
		mp.finish(finishMemoryPosting());
		System.gc();
		createMemoryPostings();
		memoryCheck.reset();
		numberOfDocsSinceFlush = 0;	
	}
	
	/**
	 * {@inheritDoc}.
	 * This implementation only places content in the runs in memory, which will eventually be flushed to disk.
	 */
	@Override
	protected void indexDocument(Map<String,String> docProperties, DocumentPostingList termsInDocument) throws Exception
	{
		if (termsInDocument.getDocumentLength() > 0) {
			numberOfDocsSinceCheck++;
			numberOfDocsSinceFlush++;
			
			checkFlush();
			mp.addTerms(termsInDocument, currentId);
			DocumentIndexEntry die = termsInDocument.getDocumentStatistics();
			docIndexBuilder.addEntryToBuffer((FieldScore.FIELDS_COUNT > 0) ? die : new SimpleDocumentIndexEntry(die));
			metaBuilder.writeDocumentEntry(docProperties);
			currentId++;
			numberOfDocuments++;
		}
	}

	/**
	 * Adds the name of the current run + partial lexicon to be flushed in disk.
	 * @return the two dimensional String[] array with the names of the run and partial lexicon to write.
	 */
	protected String[] finishMemoryPosting(){
		String[] names = new String[2];
		names[0] = fileNameNoExtension + "Run."+(currentFile);
		names[1] = fileNameNoExtension + "Run."+(currentFile++)+".str";
		fileNames.add(names);
		return names;
	}

	/**
	 * Uses the merger class to perform a k multiway merge
	 * in a set of previously written runs.
	 * The file names and the number of runs are given by the private queue
	 */
	public void performMultiWayMerge() throws IOException {
		String[][] _fileNames = getFileNames();
		this.currentIndex.setIndexProperty("max.term.length", ApplicationSetup.getProperty("max.term.length", ""+20));
		LexiconOutputStream<String> lexStream = new FSOMapFileLexiconOutputStream(this.currentIndex, "lexicon", 
				(super.numFields > 0 ? FieldLexiconEntry.Factory.class : BasicLexiconEntry.Factory.class));
		
		try{
			if (useFieldInformation)
				createFieldRunMerger(_fileNames);
			else
				createRunMerger(_fileNames);
			merger.beginMerge(_fileNames.length, path + ApplicationSetup.FILE_SEPARATOR + prefix +  ".inverted.bf");
			while(!merger.isDone()){
				merger.mergeOne(lexStream);
			}
			merger.endMerge(lexStream);
			lexStream.close();
			//the constructor for FieldLexiconEntry is wrong - replace it
			if (super.numFields > 0)
			{
				this.currentIndex.addIndexStructure("lexicon-valuefactory", FieldLexiconEntry.Factory.class.getName(), "java.lang.String", "${index.inverted.fields.count}");
			}
			numberOfUniqueTerms = merger.getNumberOfTerms();
			numberOfPointers = merger.getNumberOfPointers();
			// Delete the runs files
			for(int i = 0; i < _fileNames.length; i++)
			{
				Files.delete(_fileNames[i][0]);
				Files.delete(_fileNames[i][1]);
			}
			currentIndex.setIndexProperty("num.Terms", ""+numberOfUniqueTerms);
			currentIndex.setIndexProperty("num.Pointers", ""+numberOfPointers);
			currentIndex.setIndexProperty("num.Tokens", ""+numberOfTokens);
			currentIndex.addIndexStructure(
					"inverted",
					invertedIndexClass,
					"org.terrier.structures.IndexOnDisk,java.lang.String,org.terrier.structures.DocumentIndex,java.lang.Class", 
					"index,structureName,document,"+ 
						(FieldScore.FIELDS_COUNT > 0
							? fieldInvertedIndexPostingIteratorClass
							: basicInvertedIndexPostingIteratorClass ));
			currentIndex.addIndexStructureInputStream(
                    "inverted",
                    invertedIndexInputStreamClass,
                    "org.terrier.structures.IndexOnDisk,java.lang.String,java.util.Iterator,java.lang.Class",
                    "index,structureName,lexicon-entry-inputstream,"+
                    	(FieldScore.FIELDS_COUNT > 0
                    		? fieldInvertedIndexPostingIteratorClass
							: basicInvertedIndexPostingIteratorClass ));
			currentIndex.setIndexProperty("index.inverted.fields.count", ""+FieldScore.FIELDS_COUNT );
			currentIndex.setIndexProperty("index.inverted.fields.names", ArrayUtils.join(FieldScore.FIELD_NAMES, ","));
		}catch(Exception e){
			logger.error("Problem in performMultiWayMerge", e);
		}
	}

	/**
	 * @return the String[][] structure with the name of the runs files and partial lexicons.
	 */
	protected String[][] getFileNames(){
		String[][] files =  new String[fileNames.size()][2];
		int i = 0;
		while(!fileNames.isEmpty()){
			files[i++] = fileNames.poll();
		}
		return files;
	}


	/**
	 * Hook method that creates a FieldRunMerger instance
	 * @throws IOException if an I/O error occurs.
	 */
	protected void createFieldRunMerger(String[][] files) throws Exception{
		merger = new RunsMerger(new FileRunIteratorFactory(files, FieldPostingInRun.class, super.numFields));
	}


	/**
	 * Hook method that creates a RunsMerger instance
	 * @throws IOException if an I/O error occurs.
	 */
	protected void createRunMerger(String[][] files) throws Exception{
		merger = new RunsMerger(new FileRunIteratorFactory(files, 
				useFieldInformation ? FieldPostingInRun.class : SimplePostingInRun.class, 0));
	}

	/**
	 * Hook method that creates the right type of MemoryPostings class.
	 */
	protected void createMemoryPostings(){
		if (useFieldInformation)
			mp = new FieldsMemoryPostings();
		else
			mp = new MemoryPostings();
	}

	@Override
	protected void load_indexer_properties() {
		super.load_indexer_properties();
		docsPerCheck = ApplicationSetup.DOCS_CHECK_SINGLEPASS;
		maxDocsPerFlush = Integer.parseInt(ApplicationSetup.getProperty("indexing.singlepass.max.documents.flush", "0"));
		memoryCheck = new RuntimeMemoryChecker();
		logger.info("Checking memory usage every " + docsPerCheck + " maxDocPerFlush=" + maxDocsPerFlush);
	}


}
