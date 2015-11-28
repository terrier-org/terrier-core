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
 * The Original Code is DirectIndex.java.
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
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.bit.BasicIterablePosting;
import org.terrier.utility.FieldScore;
/**
 * A class that implements the direct index and saves 
 * information about whether a term appears in 
 * one of the specified fields.
 * @author Douglas Johnson, Vassilis Plachouras, Craig Macdonald
 * @deprecated
 */
public class DirectIndex extends BitPostingIndex {

	/** The logger used for the Lexicon */
	protected static final Logger logger = LoggerFactory.getLogger(DirectIndex.class);
	
	/** Indicates whether field information is indexed. */
	//protected static final boolean saveTagInformation = 
	//	FieldScore.USE_FIELD_INFORMATION;
	
	
	/** The document index employed for retrieving the document offsets.*/
	protected DocumentIndex docIndex;
	
	/**
	 * Constructs an instance of the class with 
	 * the given index, using the specified structure name.
	 * @param index The index to be used
	 * @param structureName the name of this direct index
	 * @throws IOException 
	 */
	public DirectIndex(IndexOnDisk index, String structureName) throws IOException {
		super(index, structureName, BasicIterablePosting.class);
		docIndex = index.getDocumentIndex();
	}

	/**
	 * Constructs an instance of the class with
	 * @param index
	 * @param structureName
	 * @param postingClass
	 * @throws IOException
	 */
	public DirectIndex(IndexOnDisk index, String structureName,
			Class<? extends IterablePosting> postingClass) throws IOException 
	{
		super(index, structureName, postingClass);
		docIndex = index.getDocumentIndex();
	}

	/**
	 * Returns a two dimensional array containing the 
	 * term ids and the term frequencies for 
	 * the given document. 
	 * @return int[][] the two dimensional [n][3] array 
	 * 		   containing the term ids, frequencies and field scores. If
	 *         the given document identifier is not found in the document
	 *         index, then the method returns null. If fields are not used, 
	 *         then the dimension of the returned array are [n][2].
	 * @param docid the document identifier of the document which terms 
	 * 		  we retrieve.
	 */
	public int[][] getTerms(int docid) throws IOException
	{
		DocumentIndexEntry de = docIndex.getDocumentEntry(docid);
		if (de == null)
			return null;
		if (de.getNumberOfEntries() == 0)
			return null;
		return getTerms(de);
	}
	/** 
	 * Get the terms for the document at the specified bit index pointer.
	 * See getTerms(int docid) for the return specification.
	 */
	public int[][] getTerms(BitIndexPointer pointer) throws IOException {
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
					documentTerms[0][i] = file.readGamma() + documentTerms[0][i - 1];
					documentTerms[1][i] = file.readUnary();
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
	/**
	 * main
	 * @param args
	 * @throws Exception
	 */
	public static void main (String args[]) throws Exception
	{
		IndexOnDisk index = Index.createIndex();
		if (index == null)
		{
			System.err.println("Couldn't load index: " + Index.getLastIndexLoadError());
			return;
		}
		PostingIndex<Pointer> direct = (PostingIndex<Pointer>) index.getDirectIndex();
		DocumentIndex doc = index.getDocumentIndex();
		DocumentIndexEntry die = doc.getDocumentEntry(Integer.parseInt(args[0]));
		System.err.println("docid" + args[0] + " pointer = "+ die.toString());
		IterablePosting pi = direct.getPostings(die);
		System.out.print(args[0] + " ");
		while(pi.next() != IterablePosting.EOL)
		{
			System.out.print("(" + pi.getId() + ", " + pi.getFrequency() + ") ");
		}
		System.out.println();
	}

}
