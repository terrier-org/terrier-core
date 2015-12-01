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
 * The Original Code is MemoryFieldsLexiconEntry.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory.fields;

import org.terrier.realtime.memory.MemoryLexiconEntry;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.FieldEntryStatistics;

/** Lexicon entry (fields). 
 * 
 *  @author Stuart Mackie
 * @since 4.0
 * */
@SuppressWarnings("serial")
public class MemoryFieldsLexiconEntry extends MemoryLexiconEntry implements FieldEntryStatistics {

    int[] fields;

    /** Constructor (termid, df, tf, fields). */
    public MemoryFieldsLexiconEntry(int termid, int df, int tf, int[] fields) {
        super(termid, df, tf);
        this.fields = fields;
    }

    /** Constructor (df, tf, fields). */
    public MemoryFieldsLexiconEntry(int df, int tf, int[] fields) {
        super(df, tf);
        this.fields = fields;
    }
    
    /**
	 * Constructor (termid).
	 */
	public MemoryFieldsLexiconEntry(int termid) {
		super(termid);
		this.fields = new int[0];
	}

    /** {@inheritDoc} */
    public int[] getFieldFrequencies() {
        return fields;
    }

    /** {@inheritDoc} */
    public void add(EntryStatistics le) {
        super.add(le);
        int[] fields = ((FieldEntryStatistics) le).getFieldFrequencies();
        for (int i = 0; i < fields.length; i++)
            this.fields[i] += fields[i];
    }

    /** {@inheritDoc} */
    public void subtract(EntryStatistics le) {
        super.subtract(le);
        int[] fields = ((FieldEntryStatistics) le).getFieldFrequencies();
        for (int i = 0; i < fields.length; i++)
            this.fields[i] -= fields[i];
    }
}
