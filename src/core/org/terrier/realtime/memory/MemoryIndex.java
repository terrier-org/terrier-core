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
 * The Original Code is MemoryIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 *   Dyaa Albakour <dyaa.albakour@glasgow.ac.uk>
 */

package org.terrier.realtime.memory;

import gnu.trove.TObjectIntHashMap;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.indexing.Document;
import org.terrier.realtime.UpdatableIndex;
import org.terrier.realtime.WritableIndex;
import org.terrier.structures.AbstractPostingOutputStream;
import org.terrier.structures.BasicLexiconEntry;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.FSOMapFileLexiconOutputStream;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.NonIncrementalDocumentIndexEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.indexing.CompressingMetaIndexBuilder;
import org.terrier.structures.indexing.CompressionFactory;
import org.terrier.structures.indexing.DocumentIndexBuilder;
import org.terrier.structures.indexing.DocumentPostingList;
import org.terrier.structures.indexing.LexiconBuilder;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.seralization.FixedSizeTextFactory;
import org.terrier.terms.SkipTermPipeline;
import org.terrier.terms.TermPipeline;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;

/**
 * An index held in fully memory. This is an updatable index, i.e. it supports
 * indexDocument() methods which can be called at any time to add new documents.
 * A MemoryIndex is also writable, i.e. it has a write() method that will convert
 * it to an IndexOnDisk and write it out to the location specified by terrier.index.path
 * and with prefix terrier.index.prefix.
 * 
 * @author Richard McCreadie, Dyaa Albakour 
 * @since 4.0
 */
public class MemoryIndex extends Index implements UpdatableIndex,WritableIndex {
	protected static final Logger logger = LoggerFactory.getLogger(MemoryIndex.class);

	/*
	 * Index data structures.
	 */
	protected MemoryLexicon lexicon;
	protected MemoryInvertedIndex inverted;
	protected MemoryMetaIndex metadata;
	protected MemoryDocumentIndex document;
	protected MemoryCollectionStatistics stats;
	
    // Blocks and fields.
    public final static boolean      blocks    = (ApplicationSetup.getProperty("block.indexing", "").equals("")) ? false : true;
    public final static boolean      fields    = (ApplicationSetup.getProperty("FieldTags.process", "").equals("")) ? false : true;
    public final static String[]     fieldtags = ArrayUtils.parseCommaDelimitedString(ApplicationSetup.getProperty("FieldTags.process", ""));
    public TObjectIntHashMap<String> fieldIDs;
	
    /** A lock that stops multiple indexing operations from happening at once **/
    protected Object indexingLock = new Object();
    
    // Compression code for writing
    protected CompressionConfiguration compressionConfig;
    
    
    public MemoryIndex(MemoryLexicon tmplex, MemoryDocumentIndexMap document, MemoryInvertedIndex inverted, MemoryMetaIndex metadata, MemoryCollectionStatistics stats) {
    	this();
    	lexicon = tmplex;
    	this.document = document;
    	this.inverted = inverted;
    	this.metadata = metadata;
    	this.stats = stats;
    }

	/**
	 * Constructor.
	 */
	public MemoryIndex() {
		super(0l, 0l, 0l); // Do nothing.
		fieldIDs = new TObjectIntHashMap<String>(fieldtags.length);
        for (int i = 0; i < fieldtags.length; i++)
            fieldIDs.put(fieldtags[i], i);
        if (blocks && fields) {
        	logger.warn("MemoryIndex: Creating new MemoryIndex, properties indicate that blocks and fields should be selected, but are not supported.");
            for (String field : fieldtags)
                logger.info("***MemIndex*** fields: " + field);
        } else if (blocks)
        	logger.warn("MemoryIndex: Creating new MemoryIndex, properties indicate that blocks should be selected, but are not supported.");
        else if (fields) {
        	//logger.warn("MemoryIndex: Creating new MemoryIndex, properties indicate that fields should be selected, but are not supported by MemoryIndex, use MemoryFieldsIndex instead.");
            for (String field : fieldtags)
                logger.info("***MemIndex*** fields: " + field);
        }
		lexicon = new MemoryLexicon();
		document = new MemoryDocumentIndex();
		inverted = new MemoryInvertedIndex(lexicon, document);
		metadata = new MemoryMetaIndex();
		stats = new MemoryCollectionStatistics(0, 0, 0, 0, new long[] { 0 });
		load_pipeline(); // For term processing (stemming, stop-words).

		logger.info("***REALTIME*** MemoryIndex (NEW)");
	}

	/** {@inheritDoc} */
	public Object getIndexStructure(String structureName) {
		if (structureName.equalsIgnoreCase("lexicon"))
			return getLexicon();
		if (structureName.equalsIgnoreCase("inverted"))
			return getInvertedIndex();
		if (structureName.equalsIgnoreCase("meta"))
			return getMetaIndex();
		if (structureName.equalsIgnoreCase("document"))
			return getDocumentIndex();
		if (structureName.equalsIgnoreCase("collectionstatistics"))
			return getCollectionStatistics();
		else
			return null;
	}

	/** {@inheritDoc} */
	public Lexicon<String> getLexicon() {
		return lexicon;
	}

	/** {@inheritDoc} */
	public PostingIndex<?> getInvertedIndex() {
		return inverted;
	}

	/** {@inheritDoc} */
	public MetaIndex getMetaIndex() {
		return metadata;
	}

	/** {@inheritDoc} */
	public DocumentIndex getDocumentIndex() {
		return document;
	}

	/** {@inheritDoc} */
	public CollectionStatistics getCollectionStatistics() {
		return stats;
	}

	/** Not implemented. */
	public PostingIndex<?> getDirectIndex() {
		return null;
	}

	/** {@inheritDoc} */
	public Object getIndexStructureInputStream(String structureName) {
		if (structureName.equalsIgnoreCase("lexicon"))
			return lexicon.iterator();
		if (structureName.equalsIgnoreCase("inverted"))
			return inverted.iterator();
		if (structureName.equalsIgnoreCase("meta"))
			return metadata.iterator();
		if (structureName.equalsIgnoreCase("document"))
			return document.iterator();
		else
			return null;
	}

	/**
	 * Index a new document.
	 */
	public void indexDocument(Document doc) throws Exception {

		synchronized(indexingLock) {
			// Don't index null documents.
			if (doc == null)
				return;
	
			// Process terms through term pipeline.
			docPostings = new DocumentPostingList();
			while (!doc.endOfDocument())
				pipeline_first.processTerm(doc.getNextTerm());
	
			indexDocument(doc.getAllProperties(), docPostings);
		}
	}
	
	

	/**
	 * Index a new document.
	 */
	public void indexDocument(Map<String, String> docProperties,
			DocumentPostingList docContents) throws Exception {

		synchronized(indexingLock) {
		
		// Don't index null documents.
		if (docContents == null || docProperties == null)
			return;

		// Write the document's properties to the meta index.
		metadata.writeDocumentEntry(docProperties);	

		// Add the document's length to the document index.
		document.addDocument(docContents.getDocumentLength());

		// For each term in the document:
		for (String term : docContents.termSet()) {

			// Add/update term in lexicon.
			int termid = lexicon.term(term, new MemoryLexiconEntry(1,
					docContents.getFrequency(term)));

			// Add document posting to inverted file.
			inverted.add(termid, stats.getNumberOfDocuments(),
					docContents.getFrequency(term));
		}

		// Update collection statistics.
		stats.update(1, docContents.getDocumentLength(),
				docContents.termSet().length);
		stats.updateUniqueTerms(lexicon.numberOfEntries());

		logger.debug("***REALTIME*** MemoryIndex indexDocument ("
				+ stats.getNumberOfDocuments() + ")");
		
		}
	}

	/** {@inheritDoc}
	 * <p>NB: This implementation uses addToDocument(int, DocumentPostingList)
	 * internally.
	 */
	@Override
	public boolean addToDocument(int docid, Document doc) throws Exception {
		
		// Don't index null documents.
		if (doc == null)
			return false;
		
		synchronized(indexingLock) {
			// Process terms through term pipeline.
			docPostings = new DocumentPostingList();
			while (!doc.endOfDocument())
				pipeline_first.processTerm(doc.getNextTerm());
			return addToDocument(docid, docPostings);
		}
	}

	/** {@inheritDoc}
	 *  Updates DocumentIndex and CollectionStatistics appropriately.
	 */
	@Override
	public boolean addToDocument(int docid, DocumentPostingList docContents)
		throws Exception
	{
		if (docid >= this.stats.getNumberOfDocuments())
			throw new IllegalArgumentException("docid " + docid + " too large");
		
		synchronized(indexingLock) {
			
			// Don't index null documents.
			if (docContents == null)
				return false;

			
			// Add the document's length to the document index.
			document.setLength(docid, 
					docContents.getDocumentLength() + document.getDocumentLength(docid));

			int pointers = 0;
			// For each term in the document:
			for (String term : docContents.termSet()) {

				// Add/update term in lexicon.
				int termid = lexicon.term(term, new MemoryLexiconEntry(1,
						docContents.getFrequency(term)));

				// Add document posting to inverted file.
				boolean newPtr = inverted.addOrUpdate(termid, docid,
						docContents.getFrequency(term));
				if (newPtr) pointers++;
			}

			// Update collection statistics.
			stats.update(0, docContents.getDocumentLength(),
					pointers);
			stats.updateUniqueTerms(lexicon.numberOfEntries());

			logger.debug("***REALTIME*** MemoryIndex addToDocument ("
					+ stats.getNumberOfDocuments() + ")");
			
		}
		return true;
	}

	/**
	 * Write index structures to disk.
	 */
	@SuppressWarnings("unchecked")
	public Index write(String path, String prefix) throws IOException {

		synchronized(indexingLock) {
		
		logger.info("***REALTIME*** MemoryIndex write path: " + path
				+ " prefix: " + prefix);

		compressionConfig = CompressionFactory.getCompressionConfiguration("inverted", new String[0], 0,0);
		
		// FIXME: increase visibility using logger.debug

		
		
		// Make a new index to flush memory to.
		IndexOnDisk newIndex = Index.createNewIndex(path, prefix);

		/*
		 * document index & meta index
		 */

		Iterator<String[]> metaIter = (Iterator<String[]>) this
				.getIndexStructureInputStream("meta");

		Iterator<DocumentIndexEntry> docIter = (Iterator<DocumentIndexEntry>) this
				.getIndexStructureInputStream("document");

		String temp = "";
		for (String a : ((MetaIndex)this.getIndexStructure("meta")).getKeys()) temp = temp+" "+a;
		//logger.info("Meta Keys: "+temp);
		
		CompressingMetaIndexBuilder metaOut = new CompressingMetaIndexBuilder(
				newIndex, ((MetaIndex)this.getIndexStructure("meta")).getKeys(),
				ArrayUtils.parseCommaDelimitedInts(ApplicationSetup
						.getProperty("indexer.meta.forward.keylens", "")),
				ArrayUtils.parseCommaDelimitedString(ApplicationSetup
						.getProperty("indexer.meta.reverse.keys", "")));

		DocumentIndexBuilder docOut = new DocumentIndexBuilder(newIndex,
				"document");

		System.out.println(this.getCollectionStatistics().getNumberOfDocuments());
		while (docIter.hasNext()) {
			
			
			docOut.addEntryToBuffer(docIter.next());
		}
		while(metaIter.hasNext()){
			metaOut.writeDocumentEntry(metaIter.next());
		}
		docOut.close();
		metaOut.close();

		/*
		 * inverted index and lexicon
		 */

		PostingIndexInputStream piis = (PostingIndexInputStream) this
				.getIndexStructureInputStream("inverted");

		Iterator<Entry<String, LexiconEntry>> lexIn = (Iterator<Entry<String, LexiconEntry>>) this
				.getIndexStructureInputStream("lexicon");

		FSOMapFileLexiconOutputStream lexOut = new FSOMapFileLexiconOutputStream(
				newIndex, "lexicon", new FixedSizeTextFactory(
						ApplicationSetup.MAX_TERM_LENGTH),
				BasicLexiconEntry.Factory.class);

		BasicLexiconEntry.Factory lexiconEntryFactory = new BasicLexiconEntry.Factory();

		
		AbstractPostingOutputStream invOut = compressionConfig.getPostingOutputStream(
				path + ApplicationSetup.FILE_SEPARATOR + prefix + "." + "inverted" + compressionConfig.getStructureFileExtension());
		

		// write lexicon & inverted
		while (lexIn.hasNext()) {

			// get the next term from the mem:lexicon
			Entry<String, LexiconEntry> lexTermEntry = lexIn.next();

			// get its corresponding posting list from the mem: inverted index
			IterablePosting postings = piis.next();

			// write the posting to disk, and get the pointer for these
			BitIndexPointer newPointer = invOut.writePostings(postings);

			// make new lexicon entry
			LexiconEntry newLe = lexiconEntryFactory.newInstance();

			// populate its statistics
			newLe.add(lexTermEntry.getValue());
			newLe.setTermId(lexTermEntry.getValue().getTermId());

			// populate its pointer
			newLe.setPointer(newPointer);

			// write out to disk
			lexOut.writeNextEntry(lexTermEntry.getKey(), newLe);
		}
		IndexUtil.close(lexIn);
		IndexUtil.close(lexOut);
		IndexUtil.close(piis);
		IndexUtil.close(invOut);

		/*
		 * Index properties.
		 */

		// write out index properties to file
		newIndex.flush();

		// setting index properties appropriately
		collectProperties(this, newIndex, compressionConfig);

		/*
		 * Tidy up.
		 */

		// optimise lexicon
		LexiconBuilder.optimise(newIndex, "lexicon");

		// final write
		newIndex.flush();

		// FIXME: why?
		logger.debug("***REALTIME*** MemoryIndex write END");
		return newIndex;
		
		}
	}

	/**
	 * Collect index properties.
	 */
	public void collectProperties(Index memory, Index newIndex, CompressionConfiguration compressionConfig) {

		/*
		 * index
		 */
		newIndex.getProperties().put("index.terrier.version",
				ApplicationSetup.TERRIER_VERSION);
		newIndex.getProperties().put("index.created",
				String.valueOf(System.currentTimeMillis()));

		/*
		 * num.{Documents,Pointers,Terms,Tokens} max.term.length
		 */
		newIndex.getProperties().put(
				"num.Documents",
				String.valueOf(memory.getCollectionStatistics()
						.getNumberOfDocuments()));
		newIndex.getProperties().put(
				"num.Pointers",
				String.valueOf(memory.getCollectionStatistics()
						.getNumberOfPointers()));
		newIndex.getProperties().put(
				"num.Terms",
				String.valueOf(memory.getCollectionStatistics()
						.getNumberOfUniqueTerms()));
		newIndex.getProperties().put(
				"num.Tokens",
				String.valueOf(memory.getCollectionStatistics()
						.getNumberOfTokens()));
		newIndex.getProperties().put("max.term.length",
				String.valueOf(ApplicationSetup.MAX_TERM_LENGTH));

		/*
		 * index.lexicon
		 */
		newIndex.addIndexStructure(
				// structureName,className,paramTypes,paramValues
				"lexicon", "org.terrier.structures.FSOMapFileLexicon",
				new String[] { "java.lang.String",
						"org.terrier.structures.IndexOnDisk" }, new String[] {
						"structureName", "index" });
		newIndex.addIndexStructure(
				// structureName,className,paramTypes,paramValues
				"lexicon-keyfactory",
				"org.terrier.structures.seralization.FixedSizeTextFactory",
				new String[] { "java.lang.String" },
				new String[] { "${max.term.length}" });
		newIndex.addIndexStructureInputStream(
				// structureName,className,paramTypes,paramValues
				"lexicon",
				"org.terrier.structures.FSOMapFileLexicon$MapFileLexiconIterator",
				new String[] { "java.lang.String",
						"org.terrier.structures.IndexOnDisk" }, new String[] {
						"structureName", "index" });
		newIndex.addIndexStructureInputStream(
				// structureName,className,paramTypes,paramValues
				"lexicon-entry",
				"org.terrier.structures.FSOMapFileLexicon$MapFileLexiconEntryIterator",
				new String[] { "java.lang.String",
						"org.terrier.structures.IndexOnDisk" }, new String[] {
						"structureName", "index" });

		// newIndex.getProperties().put("index.lexicon.bsearchshortcut","charmap");
		// newIndex.getProperties().put("index.lexicon.termids","aligned");

		/*
		 * index.document
		 */
		newIndex.addIndexStructure(
				// structureName,className,paramTypes,paramValues
				"document", "org.terrier.structures.FSADocumentIndex",
				new String[] { "org.terrier.structures.IndexOnDisk",
						"java.lang.String" }, new String[] { "index",
						"structureName" });
		
		if(memory.getDocumentIndex() instanceof MemoryDocumentIndexMap)
			newIndex.addIndexStructure(
					// structureName,className,paramTypes,paramValues
					"document-factory",
					"org.terrier.structures.NonIncrementalDocumentIndexEntry$Factory",
					new String[] {}, new String[] {});
		else
			newIndex.addIndexStructure(
					// structureName,className,paramTypes,paramValues
					"document-factory",
					"org.terrier.structures.BasicDocumentIndexEntry$Factory",
					new String[] {}, new String[] {});
		
		newIndex.addIndexStructureInputStream(
				// structureName,className,paramTypes,paramValues
				"document",
				"org.terrier.structures.FSADocumentIndex$FSADocumentIndexIterator",
				new String[] { "org.terrier.structures.IndexOnDisk",
						"java.lang.String" }, new String[] { "index",
						"structureName" });

		/*
		 * index.inverted
		 */
		compressionConfig.writeIndexProperties(newIndex, "lexicon-entry-inputstream");
		/*newIndex.addIndexStructure(
				// structureName,className,paramTypes,paramValues
				"inverted",
				"org.terrier.structures.BitPostingIndex",
				new String[] { "org.terrier.structures.Index",
						"java.lang.String",
						"org.terrier.structures.DocumentIndex",
						"java.lang.Class" },
				new String[] { "index", "structureName", "document",
						"org.terrier.structures.postings.BasicIterablePosting" });
		newIndex.addIndexStructureInputStream(
				// structureName,className,paramTypes,paramValues
				"inverted",
				"org.terrier.structures.InvertedIndexInputStream",
				new String[] { "org.terrier.structures.Index",
						"java.lang.String", "java.util.Iterator",
						"java.lang.Class" },
				new String[] { "index", "structureName",
						"lexicon-entry-inputstream",
						"org.terrier.structures.postings.BasicIterablePosting" });

		newIndex.getProperties().put("index.inverted.fields.count", "0");
		newIndex.getProperties().put("index.inverted.fields.names", "");*/

		/*
		 * index.meta
		 */
		newIndex.addIndexStructure(
				// structureName,className,paramTypes,paramValues
				"meta", "org.terrier.structures.CompressingMetaIndex",
				new String[] { "org.terrier.structures.IndexOnDisk",
						"java.lang.String" }, new String[] { "index",
						"structureName" });
		newIndex.addIndexStructureInputStream(
				// structureName,className,paramTypes,paramValues
				"meta",
				"org.terrier.structures.CompressingMetaIndex$InputStream",
				new String[] { "org.terrier.structures.IndexOnDisk",
						"java.lang.String" }, new String[] { "index",
						"structureName" });
	}

	/** {@inheritDoc} */
	public String toString() {
		return "MemoryIndex";
	}

	/** Not implemented. */
	public void close() throws IOException {
	}

	/** Not implemented. */
	public void flush() throws IOException {
	}

	/*
	 * FIXME.
	 */

	/** FIXME */
	protected DocumentPostingList docPostings;

	/** FIXME */
	protected TermPipeline pipeline_first;

	/** FIXME */
	protected final static String PIPELINE_NAMESPACE = "org.terrier.terms.";

	/** FIXME */
	protected TermPipeline getEndOfPipeline() {
		return new BasicTermProcessor();
	}

	/** FIXME */
	@SuppressWarnings("unchecked")
	protected void load_pipeline() {
		String[] pipes = ApplicationSetup
				.getProperty("termpipelines", "Stopwords,PorterStemmer").trim()
				.split("\\s*,\\s*");

		TermPipeline next = getEndOfPipeline();
		final TermPipeline last = next;
		TermPipeline tmp;
		for (int i = pipes.length - 1; i >= 0; i--) {
			try {
				String className = pipes[i];
				if (className.length() == 0)
					continue;
				if (className.indexOf(".") < 0)
					className = PIPELINE_NAMESPACE + className;
				else if (className.startsWith("uk.ac.gla.terrier"))
					className = className.replaceAll("uk.ac.gla.terrier",
							"org.terrier");
				@SuppressWarnings("rawtypes")
				Class pipeClass = Class.forName(className, false, this
						.getClass().getClassLoader());
				tmp = (TermPipeline) (pipeClass
						.getConstructor(new Class[] { TermPipeline.class })
						.newInstance(new Object[] { next }));
				next = tmp;
			} catch (Exception e) {
				logger.warn("TermPipeline object " + PIPELINE_NAMESPACE
						+ pipes[i] + " not found: " + e);
				e.printStackTrace();
			}
		}
		String skipTerms = null;
		// add SkipTermPipeline as the first pipeline step to allow for special
		// terms to skip the pipeline processing sequence
		if ((skipTerms = ApplicationSetup.getProperty("termpipelines.skip",
				null)) != null && skipTerms.trim().length() > 0)
			pipeline_first = new SkipTermPipeline(next, last);
		else
			pipeline_first = next;
	}

	/** FIXME */
	private class BasicTermProcessor implements TermPipeline {
		public void processTerm(String term) {
			if (term != null) {
				docPostings.insert(term);
			}
		}

		public boolean reset() {
			return true;
		}
	}
	
	@Override
	public boolean removeDocument(int docid) {		
		return false;
	}
	
	
	public MemoryIndex(IndexOnDisk superIndex){
		this(superIndex, true);
	}
	
	/**
	 * 
	 * Create a memory index from an existing on-disk index.
	 * 
	 * If the restored index used non-incremental docis, the restoration will take that
	 * into account.
	 * 
	 * This implementation will
	 * 
	 * @param superIndex
	 * @param compressedMeta
	 */
	@SuppressWarnings("unchecked")
	public MemoryIndex(IndexOnDisk superIndex, boolean compressedMeta){
		
		super(0l, 0l, 0l); // Do nothing.
		
		fieldIDs = new TObjectIntHashMap<String>(fieldtags.length);
        for (int i = 0; i < fieldtags.length; i++)
            fieldIDs.put(fieldtags[i], i);
        if (blocks && fields) {
            logger.warn("MemoryIndex: Creating new MemoryIndex, properties indicate that blocks and fields should be selected, but are not supported.");
            for (String field : fieldtags)
                logger.info("***MemIndex*** fields: " + field);
        } else if (blocks)
        	 logger.warn("MemoryIndex: Creating new MemoryIndex, properties indicate that blocks should be selected, but are not supported.");
        else if (fields) {
        	 logger.warn("MemoryIndex: Creating new MemoryIndex, properties indicate that fields should be selected, but are not supported by MemoryIndex, use MemoryFieldsIndex instead.");
            for (String field : fieldtags)
                logger.info("***MemIndex*** fields: " + field);
        }
        
		
		//Lexicon - At minimum we need the getLexiconEntry(term) method to work
		Lexicon<String> fullLex = superIndex.getLexicon();
		LexiconEntry currentEntry;
		
		//Inverted - We need getPostings(term) to work
		PostingIndex<Pointer> fullInverted = (PostingIndex<Pointer>) superIndex.getInvertedIndex();
		int currentPostingID;
		int currentTermID;
		
		//Document - We will use a special document index implementation such that we can retain the old docids
		//document = new MemoryDocumentIndexMap();
		lexicon = new MemoryLexicon();
		inverted = new MemoryInvertedIndex(lexicon, superIndex.getDocumentIndex());
		stats = new MemoryCollectionStatistics(0, 0, 0, 0, new long[] {});
		load_pipeline(); // For term processing (stemming, stop-words).
		
		logger.info("reading out inverted..");
		long before = Calendar.getInstance().getTimeInMillis();
		Iterator<Entry<String, LexiconEntry>> iterator = fullLex.iterator();
		int numTerms =0;
		while(iterator.hasNext()){
			Entry<String, LexiconEntry> next = iterator.next();
			numTerms++;
			//logger.info("Adding "+term);
			// get the lexicon entry first (this has the wrong pointer and statistics)
			currentEntry = next.getValue();
			String term = next.getKey();
			if (currentEntry!=null) {
				
				// Create the new lexicon entry object and get the termID
				currentTermID = lexicon.term(term, new MemoryLexiconEntry(0,0));
				
				// we will iterate over the documents for the term from within the specified
				// range and re-calculate the term statistics
				int TF = 0;
				int n_t = 0;
				
				try {
					IterablePosting posting = fullInverted.getPostings(currentEntry);
					posting.next();
					//logger.info("Iterating over postings");
					while (!posting.endOfPostings()) {
						currentPostingID=posting.getId();
						
						
						
						//logger.info(term+ " "+currentPostingID);
						inverted.add(currentTermID, currentPostingID, posting.getFrequency());
						TF += posting.getFrequency();
						n_t += 1;
						//((MemoryDocumentIndexMap)document).addDocument(currentPostingID,posting.getDocumentLength());
					
						
						posting.next();

					}
 				} catch (IOException e) {
 					logger.error("Failed to get the posting list for term "+term);
					e.printStackTrace();
				}
				
				// update the lexicon statistics
				lexicon.term(term, new MemoryLexiconEntry(n_t,TF));
				//System.err.println(lexicon.getLexiconEntry(currentTermID).getValue().getDocumentFrequency()+" "+lexicon.getLexiconEntry(currentTermID).getValue().getFrequency());
				
				if (n_t==0) {
					inverted.add(currentTermID, 0, 0);
				}
				
				
			} else {
				logger.warn("Term "+term+" did not have a lexicon entry, inserting empty term.");
				currentTermID = lexicon.term(term, new MemoryLexiconEntry(0,0));
			}
		}
		long after= Calendar.getInstance().getTimeInMillis();
		logger.info("Loading Lexicon and Inverted took " + (after-before) + " miliseconds" );
		
		
		logger.info("reading out doucment and metaindices..");
		//Stats - We need to get the total term length over all documents in the range to calc
		//        the average document length used in some weighting models
		before = Calendar.getInstance().getTimeInMillis();
		long numberTokens = 0;
		Iterator<DocumentIndexEntry> it= (Iterator<DocumentIndexEntry>) superIndex.getIndexStructureInputStream("document");
		Iterator<String[]> metaIter = (Iterator<String[]>) superIndex
				.getIndexStructureInputStream("meta");
		
		this.document = new MemoryDocumentIndexMap();
		
		if(compressedMeta)
			metadata= new MemoryCompressedMetaIndex();
		else
			metadata= new MemoryMetaIndex();
		
		int numberOfDocuments=0;
		
		boolean first = true;
		boolean nonincremental = false;
		while(it.hasNext()){
			DocumentIndexEntry die = it.next();
			
			if(first){
				
				first= false;
				if (die instanceof NonIncrementalDocumentIndexEntry ){
					nonincremental = true;
					if(!compressedMeta)
						metadata= new MemoryMetaIndexMap();
				}
			}
			
			int documentLength = die.getDocumentLength();
			numberTokens+=documentLength;
			numberOfDocuments++;
			if(nonincremental){
				int docid = ((NonIncrementalDocumentIndexEntry)die).getDocid();
				((MemoryDocumentIndexMap)document).addDocument(documentLength, docid);
				((MetaIndexMap)metadata).writeDocumentEntry(docid, metaIter.next());
			}
			else{
				document.addDocument(documentLength);
				metadata.writeDocumentEntry(metaIter.next());
			}
		}
		after = Calendar.getInstance().getTimeInMillis();
		logger.info("Loading document and meta took " + (after-before) + " miliseconds" );
		
		
		logger.info("updating stats..");

		//WARNING: number of unique terms and the number of pointers are set to 0 (should they be the same value?)
		stats = new MemoryCollectionStatistics(numberOfDocuments, numTerms, numberTokens, 0, new long[] {});
		
		//Meta - We can just use the original meta index as we do lookups based on docid
		//       this is covered by the following methods
		
		

		//this.=(PostingIndex<Pointer>)superIndex.getDirectIndex();
		//if (superIndex.hasIndexStructure("direct"))
		
		// Last step - Remove stopwords from the index
		/*StatisticStopwordRemoval ssr = new TermDF();
		Iterator<Entry<String, LexiconEntry>> termIterator = lexicon.iterator();
		Entry<String, LexiconEntry> entry;
		while (termIterator.hasNext()) {
			entry = termIterator.next();
			//System.err.println("Term: "+entry.getKey()+" "+entry.getValue().getDocumentFrequency());
			if (ssr.isStopword(entry.getKey(), this)) {
				inverted.remove(entry.getValue().getTermId());
				//System.err.println("Remove "+entry.getKey());
			}
		}*/
        
        
        
        
        
	}
}
