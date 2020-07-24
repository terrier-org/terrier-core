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
 * The Original Code is DocumentIndexEntry.java
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import org.apache.hadoop.io.Writable;

/** 
 * A document index entry. Also acts as a pointer into the direct index.
 */
public abstract class DocumentIndexEntry implements Pointer, Writable
{
    int entries;
    int doclength;
    
    /** 
     * Return the length of the document.
     * 
     * @return the length of the document.
     */
    public int getDocumentLength()
    {
        return doclength;
    }
    
    /** 
     * Set the length of the document.
     * 
     * @param l the length of the document.
     */
    public void setDocumentLength(int l)
    {
        doclength = l;
    }
    
    /** 
     * {@inheritDoc} 
     */
    @Override
    public int getNumberOfEntries() 
    {
        return entries;
    }

    public void setDocumentIndexStatistics(DocumentIndexEntry die) {
        this.doclength = die.getDocumentLength();
        this.entries = die.getNumberOfEntries();
    }

    @Override public String toString() {
        return "dl=" + getDocumentLength() + " N=" + getNumberOfEntries();
    }
}