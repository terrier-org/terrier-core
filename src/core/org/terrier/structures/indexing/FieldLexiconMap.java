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
 * The Original Code is FieldLexiconMap.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.indexing;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntProcedure;

import java.io.IOException;
import java.util.Arrays;

import org.terrier.structures.FieldLexiconEntry;
import org.terrier.structures.LexiconOutputStream;
import org.terrier.utility.TermCodes;
/** class FieldLexicanMap */
public class FieldLexiconMap extends LexiconMap {

	protected final int fieldCount;
	protected final TObjectIntHashMap<String>field_tfs[]; 
	/**
	 * constructor
	 * @param _fieldCount
	 */
	@SuppressWarnings("unchecked")
	public FieldLexiconMap(int _fieldCount)
	{
		super();
		fieldCount = _fieldCount;
		field_tfs = new TObjectIntHashMap[fieldCount];
		for(int fi=0;fi<fieldCount;fi++)
			field_tfs[fi] = new TObjectIntHashMap<String>(BUNDLE_AVG_UNIQUE_TERMS);
	}
	
	protected int[] getFieldFrequency(String term)
	{
		int[] fieldFrequencies = new int[fieldCount];
		for(int fi=0;fi<fieldCount;fi++)
			fieldFrequencies[fi] = field_tfs[fi].get(term);
		return fieldFrequencies;
	}
	
	/** Inserts all the terms from a document posting
	  * into the lexicon map
	  * @param _doc The postinglist for that document. Assumed to be a FieldDocumentPostingList.
	  */
	public void insert(DocumentPostingList _doc)
	{
		super.insert(_doc);
		FieldDocumentPostingList doc = (FieldDocumentPostingList)_doc;
		int fi = 0;
		for(TObjectIntHashMap<String> docField : doc.field_occurrences)
		{
			final TObjectIntHashMap<String> thisField = field_tfs[fi];
			//final int fii = fi;
			docField.forEachEntry(new TObjectIntProcedure<String>() {
				public boolean execute(String term, int freq) {
					//System.out.println("term " + term + " tf_" + fii + "="+ freq);
					thisField.adjustOrPutValue(term, freq, freq);
					return true;
				}
			});
			fi++;
		}
	}
	
	/** Stores the lexicon tree to a lexicon stream as a sequence of entries.
	  * The binary tree is traversed in order, by called the method
	  * traverseAndStoreToStream.
	  * @param lexiconStream The lexicon output stream to store to. */
	public void storeToStream(LexiconOutputStream<String> lexiconStream) throws IOException
	{
		final String[] terms = tfs.keys(new String[0]);
		Arrays.sort(terms);
		for (String t : terms)
		{
			final FieldLexiconEntry fle = new FieldLexiconEntry(getFieldFrequency(t));
			fle.setTermId(TermCodes.getCode(t));
			fle.setStatistics(nts.get(t), tfs.get(t));
			final int[] TFf = new int[fieldCount];
			for(int fi=0;fi< fieldCount;fi++)
				TFf[fi] = field_tfs[fi].get(t);
			fle.setFieldFrequencies(TFf);
			lexiconStream.writeNextEntry(t, fle);
		}
	}

	@Override
	public void clear() {
		super.clear();
		for(int fi=0;fi<fieldCount;fi++)
			field_tfs[fi].clear();
	}
}
