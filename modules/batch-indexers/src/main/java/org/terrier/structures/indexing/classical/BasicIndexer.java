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
 * The Original Code is BasicIndexer.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures.indexing.classical;
import gnu.trove.TIntHashSet;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.terrier.indexing.Collection;
import org.terrier.indexing.Document;
import org.terrier.structures.BasicDocumentIndexEntry;
import org.terrier.structures.BasicLexiconEntry;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.FieldDocumentIndexEntry;
import org.terrier.structures.FieldLexiconEntry;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.Index;
import org.terrier.structures.collections.MapEntry;
import org.terrier.structures.indexing.CompressionFactory;
import org.terrier.structures.indexing.DocumentIndexBuilder;
import org.terrier.structures.indexing.DocumentPostingList;
import org.terrier.structures.indexing.FieldDocumentPostingList;
import org.terrier.structures.indexing.FieldLexiconMap;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.LexiconBuilder;
import org.terrier.structures.indexing.LexiconMap;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;
import org.terrier.terms.TermPipeline;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.FieldScore;
import org.terrier.utility.TermCodes;
/** 
 * BasicIndexer is the default indexer for Terrier. It takes 
 * terms from each Document object provided by the collection, and 
 * adds terms to temporary Lexicons, and into the DirectFile. 
 * The documentIndex is updated to give the pointers into the Direct
 * file. The temporary lexicons are then merged into the main lexicon.
 * Inverted Index construction takes place as a second step.
 * <br>
 * <b>Properties:</b>
 * <ul>
 * <li><tt>indexing.max.encoded.documentindex.docs</tt> - how many docs before the DocumentIndexEncoded is dropped in favour of the DocumentIndex (on disk implementation).
 * <li><i>See Also: Properties in </i><a href="Indexer.html">org.terrier.indexing.Indexer</a> <i>and</i> <a href="BlockIndexer.html">org.terrier.indexing.BlockIndexer</a></li>
 * </ul>
 * @author Craig Macdonald &amp; Vassilis Plachouras
 * @see org.terrier.structures.indexing.Indexer
 * @see org.terrier.structures.indexing.classical.BlockIndexer
 */
public class BasicIndexer extends Indexer
{
	
	/**
	 * This class implements an end of a TermPipeline that adds the
	 * term to the DocumentTree. This TermProcessor does NOT have field
	 * support.
	 */
	protected class BasicTermProcessor implements TermPipeline
	{
		//term pipeline implementation
		public void processTerm(String term)
		{
			/* null means the term has been filtered out (eg stopwords) */
			if (term != null)
			{
				//add term to thingy tree
				termsInDocument.insert(term);
				numOfTokensInDocument++;
			}
		}
		
		public boolean reset() {
			return true;
		}
	}
	/** This class implements an end of a TermPipeline that adds the
	 *  term to the DocumentTree. This TermProcessor does have field
	 *  support.
	 */
	protected class FieldTermProcessor implements TermPipeline
	{
		final TIntHashSet fields = new TIntHashSet(numFields);
		final boolean ELSE_ENABLED = fieldNames.containsKey("ELSE");
		final int ELSE_FIELD_ID = fieldNames.get("ELSE") -1;
		public void processTerm(String term)
		{
			/* null means the term has been filtered out (eg stopwords) */
			if (term != null)
			{
				/* add term to Document tree */
				for (String fieldName: termFields)
				{
					int tmp = fieldNames.get(fieldName);
					if (tmp > 0)
					{
						fields.add(tmp -1);
					}
				}
				if (ELSE_ENABLED && fields.size() == 0)
				{
					fields.add(ELSE_FIELD_ID);
				}
				((FieldDocumentPostingList)termsInDocument).insert(term,fields.toArray());
				numOfTokensInDocument++;
				fields.clear();
			}
		}
		
		public boolean reset() {
			return true;
		}
	}
	
	/** 
	 * A private variable for storing the fields a term appears into.
	 */
	protected Set<String> termFields;
	
	/** 
	 * The structure that holds the terms found in a document.
	 */
	protected DocumentPostingList termsInDocument;
	
	/**
	 * Mapping of terms 2 termids
	 */
	protected TermCodes termCodes = new TermCodes();
	
	/** 
	 * The number of tokens found in the current document so far/
	 */
	protected int numOfTokensInDocument = 0;
	
	/** The compression configuration for the direct index */
	protected CompressionConfiguration compressionDirectConfig;
	
	/** The compression configuration for the inverted index */
	protected CompressionConfiguration compressionInvertedConfig;
	
	/** Protected do-nothing constructor for use by child classes. Classes which
	  * use this method must call init() */
	protected BasicIndexer(long a, long b, long c) {
		super(a,b,c);
	}

	/** 
	 * Constructs an instance of a BasicIndexer, using the given path name
	 * for storing the data structures.
	 * @param path String the path where the data structures will be created. This is assumed to be
	 * absolute.
	 * @param prefix String the filename component of the data structures
	 */
	public BasicIndexer(String path, String prefix) {
		super(path, prefix);
		//delay the execution of init() if we are a parent class
		if (this.getClass() == BasicIndexer.class) 
			init();
		compressionDirectConfig = CompressionFactory.getCompressionConfiguration("direct", FieldScore.FIELD_NAMES, 0, 0);
		compressionInvertedConfig = CompressionFactory.getCompressionConfiguration("inverted", FieldScore.FIELD_NAMES, 0, 0);
		super.blocks = false;
	}

	

	/** 
	 * Returns the end of the term pipeline, which corresponds to 
	 * an instance of either BasicIndexer.BasicTermProcessor, or 
	 * BasicIndexer.FieldTermProcessor, depending on whether 
	 * field information is stored.
	 * @return TermPipeline the end of the term pipeline.
	 */
	protected TermPipeline getEndOfPipeline()
	{
		if(FieldScore.USE_FIELD_INFORMATION)
			return new FieldTermProcessor();
		return new BasicTermProcessor();
	}

	public void indexDocuments(Iterator<Map.Entry<Map<String,String>, DocumentPostingList>> iterDocs) {
		currentIndex = IndexOnDisk.createNewIndex(path, prefix);
		final boolean FIELDS = FieldScore.FIELDS_COUNT > 0;
		lexiconBuilder = FIELDS
			? new LexiconBuilder(currentIndex, "lexicon", 
					new FieldLexiconMap(FieldScore.FIELDS_COUNT), 
					FieldLexiconEntry.class.getName(), "java.lang.String", "\""+ FieldScore.FIELDS_COUNT + "\"",
					termCodes)
			: new LexiconBuilder(currentIndex, "lexicon", new LexiconMap(), BasicLexiconEntry.class.getName(), termCodes);
		emptyDocIndexEntry = FIELDS ? new FieldDocumentIndexEntry(FieldScore.FIELDS_COUNT) : new BasicDocumentIndexEntry();

		try{
			directIndexBuilder = compressionDirectConfig.getPostingOutputStream(
				currentIndex.getPath() + ApplicationSetup.FILE_SEPARATOR + currentIndex.getPrefix() + "." + "direct" + compressionDirectConfig.getStructureFileExtension());
		} catch (Exception ioe) {
			logger.error("Cannot make PostingOutputStream:", ioe);
		}
		docIndexBuilder = new DocumentIndexBuilder(currentIndex, "document", FIELDS);
		metaBuilder = createMetaIndexBuilder();

		long numberOfTokens = 0;
		while(iterDocs.hasNext()) {
			Map.Entry<Map<String,String>, DocumentPostingList> me = iterDocs.next();
			if (me == null) {
				continue;
			}
			DocumentPostingList _termsInDocument = me.getValue();
			Map<String,String> props = me.getKey();

			try
			{
				if (_termsInDocument.getDocumentLength() == 0)
				{	/* this document is empty, add the minimum to the document index */
					indexEmpty(props);
				}
				else
				{	/* index this docuent */
					numberOfTokens += numOfTokensInDocument;
					indexDocument(props, _termsInDocument);
				}
			}
			catch (Exception ioe)
			{
				logger.error("Failed to index "+props.get("docno"),ioe);
				throw new RuntimeException(ioe);
			}
		}

		finishedDirectIndexBuild();
		/*end of all the collections has been reached */
		/* flush the index buffers */
		compressionDirectConfig.writeIndexProperties(currentIndex, "document-inputstream");

		directIndexBuilder.close();
		docIndexBuilder.finishedCollections();
		
		if (FIELDS)
		{
			currentIndex.addIndexStructure("document-factory", FieldDocumentIndexEntry.Factory.class.getName(), "java.lang.String", "${index.direct.fields.count}");
		}
		else
		{
			currentIndex.addIndexStructure("document-factory", BasicDocumentIndexEntry.Factory.class.getName(), "", "");
		}
		try{
			metaBuilder.close();
		} catch (IOException ioe) {
			logger.error("Could not finish MetaIndexBuilder: ", ioe);
		}
	
		/* and then merge all the temporary lexicons */
		lexiconBuilder.finishedDirectIndexBuild();
		currentIndex.setIndexProperty("num.Tokens", ""+numberOfTokens);
		currentIndex.setIndexProperty("termpipelines", ApplicationSetup.getProperty("termpipelines", "Stopwords,PorterStemmer"));
		if (FieldScore.FIELDS_COUNT > 0)
		{
			currentIndex.addIndexStructure("lexicon-valuefactory", FieldLexiconEntry.Factory.class.getName(), "java.lang.String", "${index.direct.fields.count}");
		}
		/* reset the in-memory mapping of terms to term codes.*/
		termCodes.reset();
		/* and clear them out of memory */
		System.gc();
		/* record the fact that these data structures are complete */
		try{
			currentIndex.flush();
		} catch (IOException ioe) {
			logger.error("Problem flushing changes to index", ioe);
		};
		createInvertedIndex();
	}

	protected class CollectionConsumer implements Iterator<Map.Entry<Map<String,String>, DocumentPostingList>>
	{
		boolean breakHere = false;
		final Collection collection;
		int numberOfDocuments = 0;
		final boolean boundaryDocsEnabled = BUILDER_BOUNDARY_DOCUMENTS.size() > 0;

		public CollectionConsumer(Collection c) {
			this.collection = c;
		}

		public boolean hasNext() {
			if (breakHere)
				return false;
			if (collection.endOfCollection())
				return false;
			return true;

		}

		public Map.Entry<Map<String,String>, DocumentPostingList> next()
		{
			boolean gotDoc = collection.nextDocument();
			if (! gotDoc) {
				breakHere = true;
				return null;
			}
			numberOfDocuments++;
			//get the next document from the collection
			Document doc = collection.getDocument();		
			if (doc == null) {
				logger.warn("skipping null document"); 
				return null;
			}
			/* setup for parsing */
			createDocumentPostings();
			String term; //term we're currently processing
			int numOfTokensInDocument = 0;

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
			
			
			if (MAX_DOCS_PER_BUILDER>0 && numberOfDocuments >= MAX_DOCS_PER_BUILDER)
			{
				breakHere = true;
			}

			if (boundaryDocsEnabled && BUILDER_BOUNDARY_DOCUMENTS.contains(doc.getProperty("docno")))
			{
				logger.warn("Document "+doc.getProperty("docno")+" is a builder boundary document. Boundary forced.");
				breakHere = true;
			}
			return new MapEntry<Map<String,String>, DocumentPostingList>(doc.getAllProperties(), termsInDocument);
		}
	}

		
	/** 
	 * Creates the direct index, the document index and the lexicon.
	 * Loops through each document in each of the collections, 
	 * extracting terms and pushing these through the Term Pipeline 
	 * (eg stemming, stopping, lowercase).
	 * @param collections Collection[] the collections to be indexed.
	 */
	
	public void createDirectIndex(Collection collection)
	{
		// this iterator consumes the Collection object
		CollectionConsumer iterDocs = new CollectionConsumer(collection);
						
		boolean stopIndexing = false;
		long startCollection = System.currentTimeMillis();
		
		// this performs the actual indexing
		indexDocuments(iterDocs);
		int numberOfDocuments = iterDocs.numberOfDocuments; 
		long endCollection = System.currentTimeMillis();
		long secs = ((endCollection-startCollection)/1000);
		logger.info("Collection took "+secs+" seconds to index "
			+"("+numberOfDocuments+" documents)");
		if (secs > 3600)
				logger.info("Rate: "+((double)numberOfDocuments/((double)secs/3600.0d))+" docs/hour");
		if (emptyDocCount > 0)
			logger.warn("Indexed " + emptyDocCount + " empty documents");
		
		
	}
	

	/** 
	 * This adds a document to the direct and document indexes, as well 
	 * as it's terms to the lexicon. Handled internally by the methods 
	 * indexFieldDocument and indexNoFieldDocument.
	 * @param docProperties Map&lt;String,String&gt; properties of the document
	 * @param _termsInDocument DocumentPostingList the terms in the document.
	 * 
	 */
	protected void indexDocument(Map<String,String> docProperties, DocumentPostingList _termsInDocument) throws Exception 
	{
		/* add words to lexicontree */
		lexiconBuilder.addDocumentTerms(_termsInDocument);
		/* add doc postings to the direct index */
		BitIndexPointer dirIndexPost = directIndexBuilder.writePostings(_termsInDocument.getPostings2(termCodes));
			//.addDocument(termsInDocument.getPostings());
		/* add doc to documentindex */
		DocumentIndexEntry die = _termsInDocument.getDocumentStatistics();
		die.setBitIndexPointer(dirIndexPost);
		docIndexBuilder.addEntryToBuffer(die);
		/** add doc metadata to index */
		metaBuilder.writeDocumentEntry(docProperties);		
	}
	
	/**
	 * Creates the inverted index after having created the 
	 * direct index, document index and lexicon.
	 */
	public void createInvertedIndex() {
		if (currentIndex == null)
		{
			currentIndex = IndexOnDisk.createIndex(path,prefix);
			if (currentIndex == null)
			{
				logger.error("No index at ("+path+","+prefix+") to build an inverted index for ");
				return;
			}
		}
		final long beginTimestamp = System.currentTimeMillis();
		logger.info("Started building the inverted index...");

		if (currentIndex.getCollectionStatistics().getNumberOfUniqueTerms() == 0)
        {
            logger.error("Index has no terms. Inverted index creation aborted.");
			return;
        }
		if (currentIndex.getCollectionStatistics().getNumberOfDocuments() == 0)
		{
			logger.error("Index has no documents. Inverted index creation aborted.");
			return;
		}

		//generate the inverted index
		invertedIndexBuilder = new InvertedIndexBuilder(currentIndex, "inverted", compressionInvertedConfig);
		invertedIndexBuilder.setExternalParalllism(this.externalParalllism);
		invertedIndexBuilder.createInvertedIndex();
		finishedInvertedIndexBuild();
		
		long endTimestamp = System.currentTimeMillis();
		logger.info("Finished building the inverted index...");
		long seconds = (endTimestamp - beginTimestamp) / 1000;
		//long minutes = seconds / 60;
		logger.info("Time elapsed for inverted file: " + seconds);
		try{
			currentIndex.flush();
		} catch (IOException ioe) {
			logger.warn("Problem flushin index", ioe);
		}
	}
	
	/**
	 * Hook method that creates the right type of DocumentTree class.
	 */
	protected void createDocumentPostings(){
		if (FieldScore.FIELDS_COUNT > 0)
			termsInDocument = new FieldDocumentPostingList(FieldScore.FIELDS_COUNT);
		else
			termsInDocument = new DocumentPostingList();		
	}

	/** Hook method, called when the inverted index is finished - ie the lexicon is finished */
	protected void finishedInvertedIndexBuild()
	{
		if (invertedIndexBuilder != null)
			try{
				invertedIndexBuilder.close();
			} catch (IOException ioe) {
				logger.warn("Problem closing inverted index builder", ioe);
			}
		LexiconBuilder.optimise(currentIndex, "lexicon");		
	}
}
