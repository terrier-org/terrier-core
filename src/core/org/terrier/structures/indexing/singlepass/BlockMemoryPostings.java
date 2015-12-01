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
 * The Original Code is BlockMemoryPostings.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Roi Blanco (rblanc{at}@udc.es)
 *   Craig Macdonald (craigm{at}dcs.gla.ac.uk)
 */
package org.terrier.structures.indexing.singlepass;

import java.io.IOException;

import org.terrier.structures.indexing.BlockDocumentPostingList;
import org.terrier.structures.indexing.DocumentPostingList;

/**
 * Class for handling posting lists containing block information in memory while indexing.
 * @author Roi Blanco
 */
public class BlockMemoryPostings extends MemoryPostings{

	/**  	 
	 * Add the terms in a DocumentPostingList to the postings in memory. 	 
	 * @param _docPostings BlockDocumentPostingList containing the posting information for one document.
	 * @param docid Document id of the indexed document
	 * @throws IOException if an I/O error occurs. 	 
	 */
	public void addTerms(DocumentPostingList _docPostings, int docid) throws IOException {  	 
		BlockDocumentPostingList docPostings = (BlockDocumentPostingList)  _docPostings; 	 
		for (String term : docPostings.termSet()) 	 
			add(term, docid, docPostings.getFrequency(term), docPostings.getBlocks(term)); 	 
	}

	/** Add the specified to the term posting to the memory postings.
	 * @param term The String form of the term
	 * @param doc the document id of the doc
	 * @param frequency the frequency of the specified term in the document
	 * @param blocks the blockids at which this term occurs
	 */
	public void add(String term, int doc, int frequency, int[] blocks) throws IOException{
		BlockPosting post;
		
		if((post =(BlockPosting) postings.get(term)) != null) {						
			valueBytes += post.insert(doc, frequency, blocks);
			int tf = post.getTF();
			// Update the max size
			if(maxSize < tf) maxSize = tf; 
		}
		else{
			post = new BlockPosting();
			valueBytes += post.writeFirstDoc(doc, frequency, blocks);			
			postings.put(term,post);
			keyBytes += (long)(12 + 2*term.length());
		}
		numPointers++;
	}	
}
