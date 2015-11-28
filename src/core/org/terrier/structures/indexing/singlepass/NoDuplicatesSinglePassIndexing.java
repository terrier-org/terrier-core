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
 * The Original Code is NoDuplicatesSinglePassIndexing.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Dyaa Albakour <dyaa.albakour@glasgow.ac.uk>
 */

package org.terrier.structures.indexing.singlepass;

import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;

import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.SimpleDocumentIndexEntry;
import org.terrier.structures.indexing.DocumentPostingList;
import org.terrier.utility.FieldScore;

/**
 * Single pass indexer that performs document deduplication based upon the
 * the docno.
 * @author Dyaa Albakour
 * @since 4.0
 *
 */
public class NoDuplicatesSinglePassIndexing extends BasicSinglePassIndexer {

	protected TreeSet<String> seenDocnos;
	
	protected NoDuplicatesSinglePassIndexing(long a, long b, long c) {
		super(a, b, c);
		seenDocnos = new TreeSet<String>();
	}
	
	public NoDuplicatesSinglePassIndexing(String pathname, String prefix) {
		super(pathname, prefix);
		seenDocnos = new TreeSet<String>();
		if (this.getClass() == NoDuplicatesSinglePassIndexing.class) 
            init();
	}
	
	/**
	 * {@inheritDoc}.
	 * This implementation only places content in the runs in memory, which will eventually be flushed to disk.
	 */
	@Override
	protected void indexDocument(Map<String,String> docProperties, DocumentPostingList termsInDocument) throws Exception
	{
		if (seenDocnos.contains(docProperties.get("docno"))) return;
		else seenDocnos.add(docProperties.get("docno"));
		
		if (termsInDocument.getDocumentLength() > 0) {
			numberOfDocsSinceCheck++;
			numberOfDocsSinceFlush++;
			
			checkFlush();
			mp.addTerms(termsInDocument, currentId);
			DocumentIndexEntry die = termsInDocument.getDocumentStatistics();
			docIndexBuilder.addEntryToBuffer((FieldScore.FIELDS_COUNT > 0) ? die : new SimpleDocumentIndexEntry(die));
			metaBuilder.writeDocumentEntry(docProperties);
			currentId++;
			numberOfDocuments++;
		}
	}
	
	/** Adds an entry to document index for empty document @param docid, only if
	IndexEmptyDocuments is set to true.
	 */
	protected void indexEmpty(Map<String,String> docProperties) throws IOException
	{
		
		if (seenDocnos.contains(docProperties.get("docno"))) return;
		else seenDocnos.add(docProperties.get("docno"));
		
		if (! IndexEmptyDocuments)
			return;
		/* add doc to documentindex, even though it's empty */	
		logger.warn("Adding empty document "+docProperties.get("docno"));
		docIndexBuilder.addEntryToBuffer(emptyDocIndexEntry);
		metaBuilder.writeDocumentEntry(docProperties);	
	}

}
