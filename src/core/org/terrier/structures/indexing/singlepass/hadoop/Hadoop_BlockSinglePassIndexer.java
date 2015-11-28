
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
 * The Original Code is Hadoop_BlockSinglePassIndexer.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Richard McCreadie <richardm{a.}dcs.gla.ac.uk>
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *  Rodrygo Santos <rodrygo{a.}dcs.gla.ac.uk>
 */

package org.terrier.structures.indexing.singlepass.hadoop;

import gnu.trove.THashSet;
import gnu.trove.TIntHashSet;

import java.io.IOException;

import org.terrier.compression.bit.BitOutputStream;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.bit.BitPostingIndex;
import org.terrier.structures.bit.BitPostingIndexInputStream;
import org.terrier.structures.indexing.BlockDocumentPostingList;
import org.terrier.structures.indexing.BlockFieldDocumentPostingList;
import org.terrier.structures.indexing.singlepass.BlockFieldMemoryPostings;
import org.terrier.structures.indexing.singlepass.BlockFieldPostingInRun;
import org.terrier.structures.indexing.singlepass.BlockMemoryPostings;
import org.terrier.structures.indexing.singlepass.BlockPostingInRun;
import org.terrier.structures.indexing.singlepass.RunsMerger;
import org.terrier.structures.postings.bit.BlockFieldIterablePosting;
import org.terrier.structures.postings.bit.BlockIterablePosting;
import org.terrier.terms.TermPipeline;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.FieldScore;

/** A MapReduce single-pass indexer that records term positions (blocks).
 * All normal block properties are supported. For more information, see {@link org.terrier.structures.indexing.classical.BlockIndexer}.
 * @since 2.2
 * @author Richard McCreadie, Craig Macdonald and Rodrygo Santos
  */
public class Hadoop_BlockSinglePassIndexer extends Hadoop_BasicSinglePassIndexer {

	/** This class implements an end of a TermPipeline that adds the
	 *  term to the DocumentTree. This TermProcessor does NOT have field
	 *  support.
	 */	 
	protected class BasicTermProcessor implements TermPipeline {
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
	protected class FieldTermProcessor implements TermPipeline {
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
	/** The block number in the current document. */
	protected int blockId;
		/** The maximum number of terms allowed in a block */
	protected int BLOCK_SIZE;
	/** 
	 * The maximum number allowed number of blocks in a document. 
	 * After this value, all the remaining terms are in the final block */
	protected int MAX_BLOCKS;
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
			return new FieldTermProcessor();
		}
		return new BasicTermProcessor();
	}

	/**
	 * Constructs an instance of this class, where the created data structures
	 * are stored in the given path.
	 */	
	public Hadoop_BlockSinglePassIndexer()
	{
		super();
		invertedIndexClass = BitPostingIndex.class.getName();
		invertedIndexInputStreamClass = BitPostingIndexInputStream.class.getName();
		basicInvertedIndexPostingIteratorClass = BlockIterablePosting.class.getName();
		fieldInvertedIndexPostingIteratorClass = BlockFieldIterablePosting.class.getName();
	}

	/** 
	 * {@inheritDoc} 
	 */
	public void createMemoryPostings(){
		if (useFieldInformation) 
			mp = new BlockFieldMemoryPostings();
		else 
			mp = new BlockMemoryPostings();
	}


	protected void createDocumentPostings(){
		if (FieldScore.FIELDS_COUNT > 0)
			termsInDocument = new BlockFieldDocumentPostingList(FieldScore.FIELDS_COUNT);
		else
			termsInDocument = new BlockDocumentPostingList();
		blockId = 0;
		numOfTokensInBlock = 0;
	}
	
	protected RunsMerger createtheRunMerger() {
		runIteratorF =
			new HadoopRunIteratorFactory(null,
				(useFieldInformation
					? BlockFieldPostingInRun.class
					: BlockPostingInRun.class),
				super.numFields);
		HadoopRunsMerger tempRM = new HadoopRunsMerger(runIteratorF);
		try{
			tempRM.setBos(new BitOutputStream(
					((IndexOnDisk) currentIndex).getPath() + ApplicationSetup.FILE_SEPARATOR
					+ ((IndexOnDisk) currentIndex).getPrefix() + ".inverted.bf" ));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return tempRM;
	}

	@Override
	protected void load_indexer_properties() {
		super.load_indexer_properties();
		BLOCK_SIZE = ApplicationSetup.BLOCK_SIZE;
		MAX_BLOCKS = ApplicationSetup.MAX_BLOCKS;
	}
	
	
}
