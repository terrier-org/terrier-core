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
 * The Original Code is InvertedIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Douglas Johnson <johnsoda{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> 
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures.bit;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.compression.bit.BitIn;
import org.terrier.compression.bit.BitInSeekable;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.bit.BasicIterablePosting;
import org.terrier.utility.FieldScore;
import org.terrier.utility.io.WrappedIOException;
/**
 * This class implements the inverted index 
 * for performing retrieval, with field information
 * optionally.
 * @author Douglas Johnson, Vassilis Plachouras, Craig Macdonald
 * @deprecated
 */
public class InvertedIndex extends BitPostingIndex {
	/** The logger used for the Lexicon */
	protected static final Logger logger = LoggerFactory.getLogger(InvertedIndex.class);
	
	/** This is used during retrieval for a rough guess sizing of the temporaryTerms
	  * arraylist in getDocuments(). The higher this value, the less chance that the
	  * arraylist will have to be grown (growing is expensive), however more memory
	  * may be used unnecessarily. */
	public static final double NORMAL_LOAD_FACTOR = 1.0;
	/** This is used during retrieval for a rough guess sizing of the temporaryTerms
	  * arraylist in getDocuments() - retrieval with Fields. The higher this value, 
	  * the less chance that the arraylist will have to be grown (growing is expensive), 
	  * however more memory may be used unnecessarily. */
	public static final double FIELD_LOAD_FACTOR = 1.0;
	/** Indicates whether field information is used.*/
	final boolean useFieldInformation = FieldScore.USE_FIELD_INFORMATION;
	
	/** 
	 * Get the BitFiles
	 */
	public BitInSeekable[] getBitFiles() {
		return super.file;
	}
	/**
	 * Construct an instance of the class with
	 * @param index
	 * @param structureName
	 * @throws IOException
	 */
	public InvertedIndex(IndexOnDisk index, String structureName) throws IOException
	{
		this(index, structureName, index.getDocumentIndex());
	}
	/**
	 * Construct an instance of the class with
	 * @param index
	 * @param structureName
	 * @param _doi
	 * @throws IOException
	 */
	public InvertedIndex(IndexOnDisk index, String structureName, DocumentIndex _doi) throws IOException
	{
		super(index, structureName, BasicIterablePosting.class);
	}
	/**
	 * Construct an instance of the class with
	 * @param index
	 * @param structureName
	 * @param _doi
	 * @param postingClass
	 * @throws IOException
	 */
	public InvertedIndex(IndexOnDisk index, String structureName, DocumentIndex _doi, Class<? extends IterablePosting> postingClass) throws IOException
	{
		super(index, structureName, postingClass);
	}

	/** 
	 * Print out the Inverted Index
	 */
	public void print()
	{
		throw new UnsupportedOperationException("InvIndex.print() is missing. Use IndexUtil instead.");
	}
	
	@Override
	public IterablePosting getPostings(Pointer pointer) throws IOException {
		final BitIn _file = this.file[((BitIndexPointer)pointer).getFileNumber()].readReset(((BitIndexPointer)pointer).getOffset(), ((BitIndexPointer)pointer).getOffsetBits());
		IterablePosting rtr = null;
		try{
			rtr = (fieldCount > 0) 
				? postingConstructor.newInstance(_file, pointer.getNumberOfEntries(), doi, fieldCount)
				: postingConstructor.newInstance(_file, pointer.getNumberOfEntries(), doi);
		} catch (Exception e) {
			throw new WrappedIOException(e);
		}
		return rtr;
	}
	/** 
	 * Get the documents for the specified term (lexicon entry for the term)
	 */
	public int[][] getDocuments(LexiconEntry le) {
		return getDocuments((BitIndexPointer)le);
	}
	/** 
	 * Get the documents for for the posting list using the pointer given
	 */
	public int[][] getDocuments(BitIndexPointer pointer) {
		if (pointer==null)
			return null;
		final boolean loadTagInformation = FieldScore.USE_FIELD_INFORMATION;
		final int count = pointer.getNumberOfEntries();
		try{
			final BitIn file = this.file[pointer.getFileNumber()].readReset(pointer.getOffset(), pointer.getOffsetBits());
			int[][] documentTerms = null;
			if (loadTagInformation) { //if there are tag information to process			
				documentTerms = new int[2+fieldCount][count];
				documentTerms[0][0] = file.readGamma() - 1;
				documentTerms[1][0] = file.readUnary();
				for (int f = 0; f < fieldCount; f++) {
					documentTerms[2+f][0] = file.readUnary() - 1;
				}				

				for (int i = 1; i < count; i++) {					
					documentTerms[0][i]  = file.readGamma() + documentTerms[0][i - 1];
					documentTerms[1][i]  = file.readUnary();
					for (int f = 0; f < fieldCount; f++) {
						documentTerms[2+f][i] = file.readUnary() - 1;
					}
				}				
			} else { //no tag information to process					
				documentTerms = new int[2][count];
				//new		
				documentTerms[0][0] = file.readGamma() - 1;
				documentTerms[1][0] = file.readUnary();
				for(int i = 1; i < count; i++){							 
					documentTerms[0][i] = file.readGamma() + documentTerms[0][i - 1];
					documentTerms[1][i] = file.readUnary();
				}
			}
			file.close();
			return documentTerms;
		} catch (IOException ioe) {
			logger.error("Problem reading inverted index", ioe);
			return null;
		}
	}

}
