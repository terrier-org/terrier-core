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
 * The Original Code is MemoryFieldsIterablePosting.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory.fields;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import java.io.IOException;

import org.terrier.realtime.memory.MemoryIterablePosting;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.FieldPostingImpl;
import org.terrier.structures.postings.WritablePosting;

/** Iterable posting (fields). 
 * 
 *  @author Stuart Mackie
 * @since 4.0
 * */
public class MemoryFieldsIterablePosting extends MemoryIterablePosting implements FieldPosting {

    // Iterate over (docid,freq,(fields)).
    private TIntObjectHashMap<int[]> fields;

    /** Constructor (docid,freq,(fields)). */
    public MemoryFieldsIterablePosting(DocumentIndex docindex, TIntArrayList docids, TIntArrayList freqs, TIntObjectHashMap<int[]> fields) {
        super(docindex, docids, freqs);
        this.fields = fields;
    }

    /** {@inheritDoc} */
    public int[] getFieldFrequencies() {
    	if (pl_doc.size()==0) {
    		int[] f = {};
    		fields.put(index, f);
    	}
        return fields.get(getId());
    }

    /** {@inheritDoc}*/
    public int[] getFieldLengths() {
        try {
			return ((org.terrier.structures.FieldDocumentIndex) doi).getFieldLengths(getId());
		} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
    }

	@Override
	public void setFieldLengths(int[] newLengths) {
		// TODO Auto-generated method stub
		
	}
	
	/** {@inheritDoc} */
	@Override
	public WritablePosting asWritablePosting() {
		FieldPostingImpl bp = new FieldPostingImpl(getFieldFrequencies());
		bp.setId(getId());
		bp.setTf(getFrequency());
		return bp;
	}
}
