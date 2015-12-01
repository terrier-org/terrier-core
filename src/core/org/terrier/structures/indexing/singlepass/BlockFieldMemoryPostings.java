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
 * The Original Code is BlockFieldMemoryPostings.java.
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

import org.terrier.structures.indexing.BlockFieldDocumentPostingList;
import org.terrier.structures.indexing.DocumentPostingList;
/**
 * Class for handling posting lists containing block and field information in memory while indexing.
 * @author Roi Blanco
 *
 */
public class BlockFieldMemoryPostings extends BlockMemoryPostings{
	/** 
	 * {@inheritDoc} 
	 */
	public void addTerms(DocumentPostingList _docPostings, int docid) throws IOException {
		BlockFieldDocumentPostingList docPostings = (BlockFieldDocumentPostingList) _docPostings;
		for (String term : docPostings.termSet())
			add(term, docid, docPostings.getFrequency(term) , docPostings.getFieldFrequencies(term), docPostings.getBlocks(term));
	}
	
	/**
	 * add
	 * @param term
	 * @param doc
	 * @param frequency
	 * @param fieldFrequencies
	 * @param blockids
	 * @throws IOException
	 */
	public void add(String term, int doc, int frequency, int[] fieldFrequencies, int[] blockids)  throws IOException{
		BlockFieldPosting post;	
		if((post = (BlockFieldPosting)postings.get(term)) != null) {						
			valueBytes += post.insert(doc, frequency, fieldFrequencies, blockids);
			int tf = post.getTF();			
			if(maxSize < tf) maxSize = tf; 
		}
		else{
			post = new BlockFieldPosting();
			valueBytes += post.writeFirstDoc(doc, frequency, fieldFrequencies, blockids);			
			postings.put(term,post);
			keyBytes += (long)(12 + 2*term.length());
		}
		numPointers++;
	}
}
