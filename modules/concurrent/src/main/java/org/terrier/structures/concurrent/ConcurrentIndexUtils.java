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
 * The Original Code is ConcurrentIndexUtils.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures.concurrent;

import org.terrier.structures.ConcurrentReadable;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.FieldDocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Lexicon;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.bit.BitPostingIndex;
import org.terrier.structures.bit.ConcurrentBitPostingIndexUtilities;
import org.terrier.structures.concurrent.ConcurrentDocumentIndex.ConcurrentFieldDocumentIndex;

public class ConcurrentIndexUtils {

	static final String[] STRUCTURES = new String[]{
		"inverted", "document", "lexicon"
	};
	public static Index makeConcurrentForRetrieval(Index index) {
		
		DocumentIndex newDoi = null;
		if (index.hasIndexStructure("document") && ! index.getDocumentIndex().getClass().isAnnotationPresent(ConcurrentReadable.class) )
		{
			DocumentIndex oldDoi = index.getDocumentIndex();
			if (oldDoi instanceof FieldDocumentIndex)
				newDoi = new ConcurrentFieldDocumentIndex((FieldDocumentIndex)oldDoi);
			else
				newDoi = new ConcurrentDocumentIndex(oldDoi);
			
			IndexUtil.forceStructure(index, "document", newDoi);
		}
		
		if (index.hasIndexStructure("inverted") && ! index.getInvertedIndex().getClass().isAnnotationPresent(ConcurrentReadable.class) )
		{
			PostingIndex<?> inv = index.getInvertedIndex();
			if (inv instanceof BitPostingIndex)
			{
				//NB: this does not add the @ConcurrentReadable annotation
				ConcurrentBitPostingIndexUtilities.makeConcurrent((BitPostingIndex)inv, newDoi);
			}
			else
			{
				throw new IllegalArgumentException("Cannot make a " + inv + " concurrent compatible");
			}
		}
		
		if (index.hasIndexStructure("lexicon") && ! index.getLexicon().getClass().isAnnotationPresent(ConcurrentReadable.class) )
		{
			Lexicon<String> oldLex = index.getLexicon();
			Lexicon<String> newLex = new ConcurrentLexicon(oldLex);
			IndexUtil.forceStructure(index, "lexicon", newLex);
		}
		
		if (index.hasIndexStructure("meta") && ! index.getMetaIndex().getClass().isAnnotationPresent(ConcurrentReadable.class) )
		{
			MetaIndex oldmeta = index.getMetaIndex();
			MetaIndex newmeta = new ConcurrentMetaIndex(oldmeta);
			IndexUtil.forceStructure(index, "meta", newmeta);
		}
		
		return index;		
	}
	
}
