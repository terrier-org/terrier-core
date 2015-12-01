/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is Posting.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.postings;

/** This interface represents one posting in a posting list.
 * From a Posting object, the id of the posting list entry (e.g. document id
 * for inverted index, or term id for direct index), the frequency,
 * and the length of the document can be accessed.
 * 
 * @since 3.0
 * @author Craig Macdonald
 */
public interface Posting {
	/** Return the id of the current posting. For the inverted index, this is
	 * the docid; for the direct index it corresponds to the term id
	 * @return id of the posting */
    int getId();
    
    /** Return the frequency of the term in the current document
     * @return frequency of posting, in tokens */
    int getFrequency();
    
    /** Return the length of the document for this posting.
     * Usually uses the DocumentIndex, may do otherwise if
     * document length statistics are in posting list
     * @return length of the document of the current posting in tokens.
     */
    int getDocumentLength();
    
    /** It may be of benefit to update the frequency of the id in some applications.
     * This should not be used lightly, as it may break the decompression of other
     * postings in iterators.
     */
    void setId(int id);
    
    /** Copies this posting to one free of an iterator. Kind of like a clone.
     * @return an identical posting, but which can be maniulated free of this iterator
     */
    WritablePosting asWritablePosting();
}
