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
 * The Original Code is MemoryFieldsIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.memory.fields;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.terrier.indexing.Document;
import org.terrier.structures.AbstractPostingOutputStream;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.FSOMapFileLexiconOutputStream;
import org.terrier.structures.FieldDocumentIndexEntry;
import org.terrier.structures.FieldLexiconEntry;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.indexing.DiskIndexWriter;
import org.terrier.structures.indexing.CompressingMetaIndexBuilder;
import org.terrier.structures.indexing.CompressionFactory;
import org.terrier.structures.indexing.DocumentIndexBuilder;
import org.terrier.structures.indexing.DocumentPostingList;
import org.terrier.structures.indexing.FieldDocumentPostingList;
import org.terrier.structures.indexing.LexiconBuilder;
import org.terrier.structures.seralization.FixedSizeTextFactory;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.structures.IndexFactory;
import org.terrier.querying.IndexRef;

/** An in-memory incremental fields index (non-compressed).
 * @author Stuart Mackie
 * @since 4.0
 */
public class MemoryFieldsIndex extends MemoryFields {


    public static class Loader implements IndexFactory.IndexLoader
	{
		@Override
		public boolean supports(IndexRef ref) {
			return ref.size() == 1 && ref.toString().equals("#memoryfields");
		}

		@Override
		public Index load(IndexRef ref) {
			return new MemoryFieldsIndex();
		}

		@Override
		public Class<? extends Index> indexImplementor(IndexRef ref) {
			return MemoryFieldsIndex.class;
		}
	}

    /** Constructor. */
    public MemoryFieldsIndex() {
        super();
        logger.info("** Fields **");
        inverted = new MemoryFieldsInvertedIndex(lexicon, document);
        direct = new MemoryFieldsDirectIndex(this.getDocumentIndex());
    }

    /** {@inheritDoc} */
    @Override
	public void indexDocument(Map<String, String> docProperties,
			DocumentPostingList docContents) throws Exception {

		synchronized(indexingLock) {
		
		// Don't index null documents.
		if (docContents == null || docProperties == null)
			return;
		
		// Write the document's properties to the meta index.
		metadata.writeDocumentEntry(docProperties);
		
        // Add the document's length to the document index.
        ((MemoryDocumentIndexFields) document).addDocument(docContents.getDocumentLength(), ((FieldDocumentIndexEntry) docContents.getDocumentStatistics()).getFieldLengths());

        // Keep tally on fields.
        long[] fieldcounts = new long[fieldtags.length];

        // For each term in the document:
        for (String term : docContents.termSet()) {

            MemoryFieldsLexiconEntry le = new MemoryFieldsLexiconEntry(1, docContents.getFrequency(term), ((FieldDocumentPostingList)docContents).getFieldFrequencies(term));

            // Insert/update term in lexicon.
            int termid = lexicon.term(term, le);
            int docid = stats.getNumberOfDocuments();
            int tf =  docContents.getFrequency(term);
            int[] tff = ((FieldDocumentPostingList)docContents).getFieldFrequencies(term);

            // Insert/update term posting list.
            //System.err.println(term+" "+((FieldDocumentPostingList)docContents).getFieldFrequencies(term)[0]+" "+((FieldDocumentPostingList)docContents).getFieldFrequencies(term)[1]);
            ((MemoryFieldsInvertedIndex) inverted).add(termid, docid, tf, tff);

            ((MemoryFieldsDirectIndex)direct).add(docid, termid, tf, tff);

            // Keep tally on fields.
            int[] ffreq = ((FieldDocumentPostingList)docContents).getFieldFrequencies(term);
            for (int i = 0; i < fieldcounts.length; i++)
                fieldcounts[i] += ffreq[i];
        }

        // Update collection statistics.
        stats.update(1, docContents.getDocumentLength(), docContents.termSet().length);
        stats.updateUniqueTerms(lexicon.numberOfEntries());
        stats.updateFields(fieldcounts);
        stats.relcaluate();
		}
	}
    
    /** {@inheritDoc} */
    @Override
    public void indexDocument(Document doc) throws Exception {

        if (doc == null)
            return;

        // Process terms through term pipeline.
        fdpl = new FieldDocumentPostingList(fieldtags.length);
        while (!doc.endOfDocument()) {
            String term = doc.getNextTerm();
            if (term == null || term.equals(""))
                continue;
            docFields = doc.getFields();
            pipeline.processTerm(term);
        }

        indexDocument(doc.getAllProperties(), fdpl);
    }

    @Override
    protected DiskIndexWriter makeDiskIndexWriter(String path, String prefix) {
        return new DiskIndexWriter(path, prefix)
            .setFields(this.getCollectionStatistics().getFieldNames())
            .withDirect();
	}

    /** Not implemented. */
    public Index merge(String diskpath, String diskprefix, String newpath, String newprefix) throws IOException {
        return null;
    }
}
