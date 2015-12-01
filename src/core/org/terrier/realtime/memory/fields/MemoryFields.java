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
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
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
        stats = new MemoryCollectionStatistics(0, 0, 0, 0, fieldTokens);
        load_pipeline();
    }

    /** {@inheritDoc} */
    public FieldDocumentIndex getDocumentIndex() {
        return (FieldDocumentIndex) document;
    }

    /** {@inheritDoc} */
    @Override
    public void collectProperties(Index memory, Index index, CompressionConfiguration compressionConfig) {

        /*
         * index
         */
        index.getProperties().put("index.terrier.version", ApplicationSetup.TERRIER_VERSION);
        index.getProperties().put("index.created", String.valueOf(System.currentTimeMillis()));

        /*
         * num.{Documents,Pointers,Terms,Tokens} max.term.length
         */
        index.getProperties().put("num.Documents", String.valueOf(this.getCollectionStatistics().getNumberOfDocuments()));
        index.getProperties().put("num.Pointers", String.valueOf(this.getCollectionStatistics().getNumberOfPointers()));
        index.getProperties().put("num.Terms", String.valueOf(this.getCollectionStatistics().getNumberOfUniqueTerms()));
        index.getProperties().put("num.Tokens", String.valueOf(this.getCollectionStatistics().getNumberOfTokens()));
        index.getProperties().put("max.term.length", String.valueOf(ApplicationSetup.MAX_TERM_LENGTH));

        /*
         * index.lexicon
         * structureName,className,paramTypes,paramValues
         */

        index.addIndexStructure("lexicon", "org.terrier.structures.FSOMapFileLexicon", new String[] { "java.lang.String", "org.terrier.structures.IndexOnDisk" }, new String[] { "structureName", "index" });
        index.addIndexStructure("lexicon-keyfactory", "org.terrier.structures.seralization.FixedSizeTextFactory", new String[] { "java.lang.String" }, new String[] { "${max.term.length}" });
        index.addIndexStructure("lexicon-valuefactory", "org.terrier.structures.FieldLexiconEntry$Factory", new String[] { "java.lang.String" }, new String[] { "${index.inverted.fields.count}" });
        index.addIndexStructureInputStream("lexicon", "org.terrier.structures.FSOMapFileLexicon$MapFileLexiconIterator", new String[] { "java.lang.String", "org.terrier.structures.IndexOnDisk" }, new String[] { "structureName", "index" });
        index.addIndexStructureInputStream("lexicon-entry", "org.terrier.structures.FSOMapFileLexicon$MapFileLexiconEntryIterator", new String[] { "java.lang.String", "org.terrier.structures.IndexOnDisk" }, new String[] { "structureName", "index" });

        /*
         * index.document
         * structureName,className,paramTypes,paramValues
         */
        
        index.addIndexStructure("document", "org.terrier.structures.FSAFieldDocumentIndex", new String[] { "org.terrier.structures.IndexOnDisk", "java.lang.String" }, new String[] { "index", "structureName" });
        index.addIndexStructure("document-factory", "org.terrier.structures.FieldDocumentIndexEntry$Factory", new String[] { "java.lang.String" }, new String[] { "${index.inverted.fields.count}" });
        index.addIndexStructureInputStream("document", "org.terrier.structures.FSADocumentIndex$FSADocumentIndexIterator", new String[] { "org.terrier.structures.IndexOnDisk", "java.lang.String" }, new String[] { "index", "structureName" });
        /*
         * index.inverted
         * structureName,className,paramTypes,paramValues
         */

        compressionConfig.writeIndexProperties(index, "lexicon-entry-inputstream");
        //index.addIndexStructure("inverted", "org.terrier.structures.InvertedIndex", new String[] { "org.terrier.structures.Index", "java.lang.String", "org.terrier.structures.DocumentIndex", "java.lang.Class" }, new String[] { "index", "structureName", "document", "org.terrier.structures.postings.FieldIterablePosting" });
        //index.addIndexStructureInputStream("inverted", "org.terrier.structures.InvertedIndexInputStream", new String[] { "org.terrier.structures.Index", "java.lang.String", "java.util.Iterator", "java.lang.Class" }, new String[] { "index", "structureName", "lexicon-entry-inputstream", "org.terrier.structures.postings.FieldIterablePosting" });
        index.getProperties().put("index.inverted.fields.count", String.valueOf(fieldtags.length));
        index.getProperties().put("index.inverted.fields.names", ApplicationSetup.getProperty("FieldTags.process", ""));

        /*
         * index.meta
         * structureName,className,paramTypes,paramValues
         */

        index.addIndexStructure("meta", "org.terrier.structures.CompressingMetaIndex", new String[] { "org.terrier.structures.IndexOnDisk", "java.lang.String" }, new String[] { "index", "structureName" });
        index.addIndexStructureInputStream("meta", "org.terrier.structures.CompressingMetaIndex$InputStream", new String[] { "org.terrier.structures.IndexOnDisk", "java.lang.String" }, new String[] { "index", "structureName" });
       
        
        /*for (Object o : index.getProperties().keySet()) {
        	System.err.println(o.toString()+" "+index.getProperties().getProperty((String)o));
        }*/
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
