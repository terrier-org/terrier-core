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
 * The Original Code is EntryStatistics.java
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import java.io.Serializable;

/** 
 * An interface for basic statistics about a lexicon entry.
 * 
 * @since 3.0
 * @author Craig Macdonald
 */
public interface EntryStatistics extends Serializable
{
    /** 
     * Return the frequency (total number of occurrences) of the term.
     * 
     * @return the frequency (total number of occurrences) of the entry (term).
     */
    int getFrequency(); // F

    /** 
     * Set the frequency (total number of occurrences) of the term.
     * 
     * @param F the frequency (total number of occurrences) of the entry (term).
     */
    void setFrequency(int F);

    /** 
     * Return the number of documents that the term occurs in.
     * 
     * @return the number of documents that the term occurs in.
     */
    int getDocumentFrequency(); // Nt

    /** 
     * Set the number of documents that the term occurs in.
     * 
     * @param nt the number of documents that the term occurs in.
     */
    void setDocumentFrequency(int nt);
    
    /** 
     * Return the id of the term.
     * 
     * @return the id of the term.
     */
    int getTermId();

    /** 
     * Return the maximum in-document term frequency of the term
     * among all documents the terms appears in.
     * 
     * @return the maximum in-document term frequency of the term
     *         among all documents the terms appears in.
     */
    int getMaxFrequencyInDocuments();

    /** 
     * Set the maximum in-document term frequency of the term
     * among all documents the terms appears in.
     * 
     * @param max the maximum in-document term frequency of the term
     *            among all documents the terms appears in.
     */
    void setMaxFrequencyInDocuments(int max);

    /** 
     * Increment the statistics of this object by that of another.
     * 
     * @param e the other object whose statistics are used to 
     *          increment the statistics of this object.
     */
    void add(EntryStatistics e);

    /** 
     * Decrement the statistics of this object by that of another.
     * 
     * @param e the other object whose statistics are used to 
     *          decrement the statistics of this object.
     */
    void subtract(EntryStatistics e);
    
    /** 
     * Copy this entry statistics to one that can be reused. Kind of like a clone.
     * 
     * @return an identical entry statistics, but which can be reused.
     * @since 3.6
     */
    EntryStatistics getWritableEntryStatistics();
}
