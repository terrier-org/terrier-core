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
 * The Original Code is BlockIndexer.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 * Craig Macdonald <craigm{a.}dcs.gla.ac.uk> 
 * Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 * Rodrygo Santo <rodrygo{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures.indexing.classical;
import gnu.trove.THashSet;
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
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.indexing.BlockDocumentPostingList;
import org.terrier.structures.indexing.BlockFieldDocumentPostingList;
import org.terrier.structures.indexing.CompressionFactory;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;
import org.terrier.structures.indexing.DocumentIndexBuilder;
import org.terrier.structures.indexing.DocumentPostingList;
import org.terrier.structures.indexing.FieldLexiconMap;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.LexiconBuilder;
import org.terrier.structures.indexing.LexiconMap;
import org.terrier.terms.TermPipeline;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.FieldScore;
import org.terrier.utility.TermCodes;
/**
 * An indexer that saves block information for the indexed terms. Block information is usually recorded in terms of relative term positions (position 1, positions 2, etc),
 * however, since 2.2, Terrier supports the presence of "marker terms" during indexing which are used to increment the block counter.
 * <p>
 * <B>Properties:</b>
 * </p>
 * <ul> 
 * <li><tt>blocks.size</tt> - How many terms should be in one block. If you want to use phrasal search, this need to be 1 (default).</li>
 * <li><tt>blocks.max</tt> - Maximum number of blocks in a document. After this number of blocks, all subsequent terms will be in the same block. Default 100,000</li>
 * <li><tt>block.indexing</tt> - This class should only be used if the <tt>block.indexing</tt> property is set.</li>
 * <li>indexing.max.encoded.documentindex.docs - how many docs before the DocumentIndexEncoded is dropped in favour of the DocumentIndex (on disk implementation).</li>
 * <li><i>See Also: Properties in </i>org.terrier.indexing.Indexer <i>and</i> org.terrier.indexing.BasicIndexer</li>
 * </ul>
 * <p><b>Markered Blocks</b><br>Markers are terms (artificially inserted or otherwise into the term stream that are used to denote when the block counter should
 * be incremented. This functionality is enabled using the <tt>block.delimiters.enabled</tt> property, while the terms are specified using a comma delimited fashion with the
 * <tt>block.delimiters</tt> property. The following lists the properties:
 * <ul>
 * <li><tt>block.delimiters.enabled</tt> - enabled markered blocks. Defaults to false, set to true to enable.</li>
 * <li><tt>block.delimiters</tt> - comma delimited list of terms that are markers. Defaults to empty. Terms are lowercased is <tt>lowercase</tt> is set to true (default).</li>
 * <li><tt>block.delimiters.index.terms</tt> - set to true if markers terms should actually be indexed. Defaults to false.</li>
 * <li><tt>block.delimiters.index.doclength</tt> - set to true if markers terms should contribute to document length. Defaults to false, only has effect if 
 * <tt>block.delimiters.index.terms</tt> is set. </li>
 * </ul>
 * @author Craig Macdonald, Vassilis Plachouras, Rodrygo Santos
 */
public class BlockIndexer extends BasicIndexer {
	
	/** This class implements an end of a TermPipeline that adds the
	 *  term to the DocumentTree. This TermProcessor does NOT have field
	 *  support.
	 */	 
	protected class BlockTermProcessor implements TermPipeline {
		public void processTerm(String t) {
			//	null means the term has been filtered out (eg stopwords)
			if (t != null) {
				//add term to thingy tree
				((BlockDocumentPostingList)termsInDocument).insert(t, blockId);
				numOfTokensInDocument++;
				if (++numOfTokensInBlock >= BLOCK_SIZE && blockId < MAX_BLOCKS) {
					numOfTokensInBlock = 0;
					blockId++;
				}
			}
		}
		
		public boolean reset() {
			return true;
		}
	}
	/** 
	 * This class implements an end of a TermPipeline that adds the
	 * term to the DocumentTree. This TermProcessor does have field
	 * support.
	 */
	protected class BlockFieldTermProcessor implements TermPipeline {
		final TIntHashSet fields = new TIntHashSet(numFields);
		final boolean ELSE_ENABLED = fieldNames.containsKey("ELSE");
		final int ELSE_FIELD_ID = fieldNames.get("ELSE") -1;
		public void processTerm(String t) {
			//	null means the term has been filtered out (eg stopwords)
			if (t != null) {
				//add term to document posting list
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
				((BlockFieldDocumentPostingList)termsInDocument).insert(t,fields.toArray(), blockId);
				numOfTokensInDocument++;
				if (++numOfTokensInBlock >= BLOCK_SIZE && blockId < MAX_BLOCKS) {
					numOfTokensInBlock = 0;
					blockId++;
				}
				fields.clear();
			}
		}
		
		public boolean reset() {
			return true;
		}
	}

	/**
	 * This class behaves in a similar fashion to BasicTermProcessor except that
	 * this one treats blocks bounded by delimiters instead of fixed-sized blocks.
	 * @author Rodrygo Santos
	 * @since 2.2
	 */
	protected class DelimTermProcessor implements TermPipeline {
		protected THashSet<String> blockDelimiterTerms;
		protected final boolean indexDelimiters;
		protected final boolean countDelimiters;
		
		public DelimTermProcessor(String[] _delims, boolean _indexDelimiters, boolean _countDelimiters) {
			blockDelimiterTerms = new THashSet<String>();
			for (String t : _delims)
				blockDelimiterTerms.add(t);
			indexDelimiters = _indexDelimiters;
			countDelimiters = _countDelimiters;
		}
		
		public void processTerm(String t) {
			if (t== null)
				return;
			// current term is a delimiter
			if (blockDelimiterTerms.contains(t)) {
				// delimiters should also be indexed
				if (indexDelimiters) {
						((BlockDocumentPostingList)termsInDocument).insert(t, blockId);
						if (countDelimiters)
								numOfTokensInDocument++;
				}
				numOfTokensInBlock = 0;
				blockId++;
			}
			else {
				// index non-delimiter term
				((BlockDocumentPostingList)termsInDocument).insert(t, blockId);
				numOfTokensInDocument++;
			}
		}
		
		public boolean reset() {
			return true;
		}
	}

	/**
	 * This class behaves in a similar fashion to FieldTermProcessor except that
	 * this one treats blocks bounded by delimiters instead of fixed-sized blocks.
	 * @author Rodrygo Santos
	 * @since 2.2
	 */
	protected class DelimFieldTermProcessor implements TermPipeline {
		protected final THashSet<String> blockDelimiterTerms;
		protected final boolean indexDelimiters;
		protected final boolean countDelimiters;

		public DelimFieldTermProcessor(String[] _delims, boolean _indexDelimiters, boolean _countDelimiters) {
			blockDelimiterTerms = new THashSet<String>();
			for (String t : _delims)
				blockDelimiterTerms.add(t);
			indexDelimiters = _indexDelimiters;
			countDelimiters = _countDelimiters;
		}

		public void processTerm(String t) {
			if (t== null)
				return;
			// current term is a delimiter
			if (blockDelimiterTerms.contains(t)) {
				// delimiters should also be indexed
				if (indexDelimiters)
				{
					final int[] fieldIds = new int[numFields];
					int i=0;
					for (String fieldName: termFields)
					{
						fieldIds[i] = fieldNames.get(fieldName);
						i++;
					}
					((BlockFieldDocumentPostingList)termsInDocument).insert(t, fieldIds, blockId);
					if (countDelimiters)
						numOfTokensInDocument++;
				}
				numOfTokensInBlock = 0;
				blockId++;
				}
				else {
				// index non-delimiter term
				final int[] fieldIds = new int[numFields];
				int i=0;
				for (String fieldName: termFields)
				{
					fieldIds[i] = fieldNames.get(fieldName);
					i++;
				}
				((BlockFieldDocumentPostingList)termsInDocument).insert(t, fieldIds, blockId);
				numOfTokensInDocument++;
			}
		}
		
		public boolean reset() {
			return true;
		}
	}

	/** The number of tokens in the current block of the current document. */
	protected int numOfTokensInBlock = 0;
	/** The block number of the current document. */
	protected int blockId;
	
	/** The maximum number of terms allowed in a block. See Property <tt>blocks.size</tt> */
	protected int BLOCK_SIZE;
	/** 
	 * The maximum number allowed number of blocks in a document. 
	 * After this value, all the remaining terms are in the final block. 
	 * See Property <tt>blocks.max</tt>. */
	protected int MAX_BLOCKS;
	

	/** Constructs an instance of this class, where the created data structures
	  * are stored in the given path, with the given prefix on the filenames.
	  * @param pathname String the path in which the created data structures will be saved. This is assumed to be
	 * absolute.
	  * @param prefix String the prefix on the filenames of the created data structures, usually "data"
	  */
	public BlockIndexer(String pathname, String prefix) {
		super(pathname, prefix);
		if (this.getClass() == BlockIndexer.class)
			init();
		int blockSize = BLOCK_SIZE;
		if (Boolean.parseBoolean(ApplicationSetup.getProperty("block.delimiters.enabled", "false")))
				blockSize = 2;
		compressionDirectConfig = CompressionFactory.getCompressionConfiguration("direct", FieldScore.FIELD_NAMES, blockSize, MAX_BLOCKS);
		compressionInvertedConfig = CompressionFactory.getCompressionConfiguration("inverted", FieldScore.FIELD_NAMES, blockSize, MAX_BLOCKS);
		super.blocks = true;
	}

	/** 
	 * Returns the object that is to be the end of the TermPipeline. 
	 * This method is used at construction time of the parent object. 
	 * @return TermPipeline the last component of the term pipeline.
	 */
	protected TermPipeline getEndOfPipeline() {
		// if using delimited blocks
		if (Boolean.parseBoolean(ApplicationSetup.getProperty("block.delimiters.enabled", "false"))) 
		{
			String delim = ApplicationSetup.getProperty("block.delimiters", "").trim();
			if (Boolean.parseBoolean(ApplicationSetup.getProperty("lowercase", "true")))
				delim = delim.toLowerCase();
			String delims[] = delim.split("\\s*,\\s*");
			final boolean indexDelims = Boolean.parseBoolean(ApplicationSetup.getProperty("block.delimiters.index.terms", "false"));
			final boolean countDelims = Boolean.parseBoolean(ApplicationSetup.getProperty("block.delimiters.index.doclength","true"));
			return (FieldScore.USE_FIELD_INFORMATION)
				? new DelimFieldTermProcessor(delims, indexDelims, countDelims)
				: new DelimTermProcessor(delims, indexDelims, countDelims);
		}
		else if (FieldScore.USE_FIELD_INFORMATION) {
			return new BlockFieldTermProcessor();
		}
		return new BlockTermProcessor();
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
		/* add doc to documentindex */
		DocumentIndexEntry die = _termsInDocument.getDocumentStatistics();
		die.setBitIndexPointer(dirIndexPost);
		docIndexBuilder.addEntryToBuffer(die);
		/** add doc metadata to index */
		metaBuilder.writeDocumentEntry(docProperties);
	}
	
	/**
	 * Creates the inverted index from the already created direct index,
	 * document index and lexicon. It saves block information and possibly
	 * field information as well.
	 * @see org.terrier.structures.indexing.Indexer#createInvertedIndex()
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
		long beginTimestamp = System.currentTimeMillis();

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

		logger.info("Started building the block inverted index...");
		invertedIndexBuilder = new BlockInvertedIndexBuilder(currentIndex, "inverted", compressionInvertedConfig);
		invertedIndexBuilder.setExternalParalllism(externalParalllism);
		invertedIndexBuilder.createInvertedIndex();		
		this.finishedInvertedIndexBuild();
		try{
			currentIndex.flush();
		} catch (IOException ioe) {
			logger.error("Cannot flush index: ", ioe);
		}

		long endTimestamp = System.currentTimeMillis();
		logger.info("Finished building the block inverted index...");
		long seconds = (endTimestamp - beginTimestamp) / 1000;
		logger.info("Time elapsed for inverted file: " + seconds);
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

	
	protected void createDocumentPostings(){
		if (FieldScore.FIELDS_COUNT > 0)
			termsInDocument = new BlockFieldDocumentPostingList(FieldScore.FIELDS_COUNT);
		else
			termsInDocument = new BlockDocumentPostingList();
		blockId = 0;
		numOfTokensInBlock = 0;	
	}

	@Override
	protected void load_indexer_properties() {
		super.load_indexer_properties();
		BLOCK_SIZE = ApplicationSetup.BLOCK_SIZE;
		MAX_BLOCKS = ApplicationSetup.MAX_BLOCKS;
	}
}
