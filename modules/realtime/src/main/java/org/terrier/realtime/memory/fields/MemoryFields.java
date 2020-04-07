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
 * The Original Code is MemoryFields.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory.fields;

import gnu.trove.TIntHashSet;

import java.util.Set;

import org.terrier.realtime.memory.MemoryCollectionStatistics;
import org.terrier.realtime.memory.MemoryIndex;
import org.terrier.structures.FieldDocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.indexing.FieldDocumentPostingList;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;
import org.terrier.terms.TermPipeline;
import org.terrier.utility.ApplicationSetup;

/** Super-type of fields index implementations. 
 *  @author Stuart Mackie
 * @since 4.0
 * */
public abstract class MemoryFields extends MemoryIndex {

    /** Constructor. */
    public MemoryFields() {
        super();
        document = new MemoryDocumentIndexFields();
        long[] fieldTokens = new long[fieldtags.length];
        for (int i = 0; i < fieldtags.length; i++)
            fieldTokens[i] = 0;
        stats = new MemoryCollectionStatistics(0, 0, 0, 0, fieldTokens, fieldtags);
        load_pipeline();
        pipeline = pipeline_first;
    }

    /** {@inheritDoc} */
    public FieldDocumentIndex getDocumentIndex() {
        return (FieldDocumentIndex) document;
    }
    
    /*
     * Term pipeline.
     */

    protected TermPipeline             pipeline;
    protected FieldDocumentPostingList fdpl;
    protected Set<String>              docFields;

    protected TermPipeline getEndOfPipeline() {
        return new TermProcessor();
    }

    private class TermProcessor implements TermPipeline {
        public void processTerm(String term) {
            if (term != null) {
                TIntHashSet freqs = new TIntHashSet(0);
                for (String docField : docFields)
                    freqs.add(fieldIDs.get(docField));
                if (fieldIDs.containsKey("ELSE") && freqs.size() == 0)
                    freqs.add(fieldIDs.get("ELSE"));
                ((FieldDocumentPostingList) fdpl).insert(term, freqs.toArray());
            }
        }

        public boolean reset() {
            return true;
        }
    }



}
