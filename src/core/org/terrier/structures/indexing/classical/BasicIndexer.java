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
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures.indexing.classical;
import gnu.trove.TIntHashSet;

import java.io.IOException;
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
import org.terrier.structures.Index;
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
		
	/** 
	 * Creates the direct index, the document index and the lexicon.
	 * Loops through each document in each of the collections, 
	 * extracting terms and pushing these through the Term Pipeline 
	 * (eg stemming, stopping, lowercase).
	 * @param collections Collection[] the collections to be indexed.
	 */
	
	public void createDirectIndex(Collection[] collections)
	{
		currentIndex = Index.createNewIndex(path, prefix);
		lexiconBuilder = FieldScore.FIELDS_COUNT > 0
			? new LexiconBuilder(currentIndex, "lexicon", 
					new FieldLexiconMap(FieldScore.FIELDS_COUNT), 
					FieldLexiconEntry.class.getName(), "java.lang.String", "\""+ FieldScore.FIELDS_COUNT + "\"")
			: new LexiconBuilder(currentIndex, "lexicon", new LexiconMap(), BasicLexiconEntry.class.getName());
		
		try{
			directIndexBuilder = compressionDirectConfig.getPostingOutputStream(
				currentIndex.getPath() + ApplicationSetup.FILE_SEPARATOR + currentIndex.getPrefix() + "." + "direct" + compressionDirectConfig.getStructureFileExtension());
		} catch (Exception ioe) {
			logger.error("Cannot make PostingOutputStream:", ioe);
		}
			//	new DirectIndexBuilder(currentIndex, "direct");
		docIndexBuilder = new DocumentIndexBuilder(currentIndex, "document");
		metaBuilder = createMetaIndexBuilder();
		emptyDocIndexEntry = (FieldScore.FIELDS_COUNT > 0) ? new FieldDocumentIndexEntry(FieldScore.FIELDS_COUNT) : new BasicDocumentIndexEntry();
				
		//int LexiconCount = 0;
		int numberOfDocuments = 0; int numberOfTokens = 0;
		//final long startBunchOfDocuments = System.currentTimeMillis();
		final int collections_length = collections.length;
		final boolean boundaryDocsEnabled = BUILDER_BOUNDARY_DOCUMENTS.size() > 0;
		boolean stopIndexing = false;
		for(int collectionNo = 0; ! stopIndexing && collectionNo < collections_length; collectionNo++)
		{
			final Collection collection = collections[collectionNo];
			long startCollection = System.currentTimeMillis();
			boolean notLastDoc = false;
			//while(notLastDoc = collection.hasNext()) {
			while ((notLastDoc = collection.nextDocument())) {
				//get the next document from the collection

				Document doc = collection.getDocument();
				
				if (doc == null)
					continue;
				
				numberOfDocuments++; 
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
					}
					else
					{	/* index this docuent */
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
			}


			if (! notLastDoc)
			{
				try{
					collection.close();
				} catch (IOException e) {
					logger.warn("Couldnt close collection", e);
				}
			}

			long endCollection = System.currentTimeMillis();
			long secs = ((endCollection-startCollection)/1000);
			logger.info("Collection #"+collectionNo+ " took "+secs+" seconds to index "
				+"("+numberOfDocuments+" documents)");
			if (secs > 3600)
				 logger.info("Rate: "+((double)numberOfDocuments/((double)secs/3600.0d))+" docs/hour"); 
		}
		finishedDirectIndexBuild();
		/*end of all the collections has been reached */
		/* flush the index buffers */
		compressionDirectConfig.writeIndexProperties(currentIndex, "document-inputstream");
//		currentIndex.addIndexStructure(
//				"direct", 
//				compressionDirectConfig.getStructureClass().getName(), 
//				"org.terrier.structures.Index,java.lang.String,java.lang.Class", 
//				"index,structureName,"+ 
//					compressionDirectConfig.getPostingIteratorClass().getName() );
//		currentIndex.addIndexStructureInputStream(
//				"direct",
//				compressionDirectConfig.getStructureInputStreamClass().getName(), 
//				"org.terrier.structures.Index,java.lang.String,java.util.Iterator,java.lang.Class",
//				"index,structureName,document-inputstream,"+ 
//					compressionDirectConfig.getPostingIteratorClass().getName() );
//		currentIndex.setIndexProperty("index.direct.fields.count", ""+FieldScore.FIELDS_COUNT );
//		currentIndex.setIndexProperty("index.direct.fields.names", ArrayUtils.join(FieldScore.FIELD_NAMES, ","));
		
		//directIndexBuilder.finishedCollections();
		directIndexBuilder.close();
		docIndexBuilder.finishedCollections();
		
		if (FieldScore.FIELDS_COUNT > 0)
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
		if (FieldScore.FIELDS_COUNT > 0)
		{
			currentIndex.addIndexStructure("lexicon-valuefactory", FieldLexiconEntry.Factory.class.getName(), "java.lang.String", "${index.direct.fields.count}");
		}
		/* reset the in-memory mapping of terms to term codes.*/
		TermCodes.reset();
		/* and clear them out of memory */
		System.gc();
		/* record the fact that these data structures are complete */
		try{
			currentIndex.flush();
		} catch (IOException ioe) {
			logger.error("Problem flushing changes to index", ioe);
		}
		
	}
	

	/** 
	 * This adds a document to the direct and document indexes, as well 
	 * as it's terms to the lexicon. Handled internally by the methods 
	 * indexFieldDocument and indexNoFieldDocument.
	 * @param docProperties Map<String,String> properties of the document
	 * @param _termsInDocument DocumentPostingList the terms in the document.
	 * 
	 */
	protected void indexDocument(Map<String,String> docProperties, DocumentPostingList _termsInDocument) throws Exception 
	{
		/* add words to lexicontree */
		lexiconBuilder.addDocumentTerms(_termsInDocument);
		/* add doc postings to the direct index */
		BitIndexPointer dirIndexPost = directIndexBuilder.writePostings(_termsInDocument.getPostings2());
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
			currentIndex = Index.createIndex(path,prefix);
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
		logger.info("Started building the inverted index...");
		invertedIndexBuilder = new InvertedIndexBuilder(currentIndex, "inverted", compressionInvertedConfig);
		
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
