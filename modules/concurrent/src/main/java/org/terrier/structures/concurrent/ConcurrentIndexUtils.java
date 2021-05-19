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
 * The Original Code is Copyright (C) 2017-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures.concurrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	static Logger logger = LoggerFactory.getLogger(ConcurrentIndexUtils.class);
	static final String[] BIT_STRUCTURES = {"inverted", "direct"};

	public static boolean isConcurrent(Index index) {
		String[] structures = new String[]{"document", "lexicon", "meta"};
		for (String s : structures) {
			if (! index.hasIndexStructure(s))
				continue;
			if (! index.getIndexStructure(s).getClass().isAnnotationPresent(ConcurrentReadable.class) )
			{
				logger.debug("Structure " + s + " is not concurrent readable");
				return false;
			}
		}
		
		for(String s : BIT_STRUCTURES) {
			if (! index.hasIndexStructure(s))
				continue;
			PostingIndex<?> pi = (PostingIndex<?>) index.getIndexStructure(s);
			if ( pi instanceof BitPostingIndex) {
				if (! ConcurrentBitPostingIndexUtilities.isConcurrent((BitPostingIndex) pi))
				{
					logger.debug("Structure " + s + " is not using a concurrent bitin");
					return false;
				}	
			}
		}
		return true;
	}

	public static Index makeConcurrentForRetrieval(Index index) {
		
		DocumentIndex newDoi = null;
		if (index.hasIndexStructure("document") && ! index.getDocumentIndex().getClass().isAnnotationPresent(ConcurrentReadable.class) )
		{
			DocumentIndex oldDoi = index.getDocumentIndex();
			logger.debug("Upgrading document index "+oldDoi.getClass().getName()+" to be concurrent");
			if (oldDoi instanceof FieldDocumentIndex)
				newDoi = new ConcurrentFieldDocumentIndex((FieldDocumentIndex)oldDoi);
			else
				newDoi = new ConcurrentDocumentIndex(oldDoi);
			
			assert newDoi.getClass().isAnnotationPresent(ConcurrentReadable.class);
			IndexUtil.forceStructure(index, "document", newDoi);
		}
		
		if (index.hasIndexStructure("inverted") && ! index.getInvertedIndex().getClass().isAnnotationPresent(ConcurrentReadable.class) )
		{
			PostingIndex<?> inv = index.getInvertedIndex();
			logger.debug("Upgrading inverted index "+inv.getClass().getName()+" to be concurrent");
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

		if (index.hasIndexStructure("direct") && ! index.getDirectIndex().getClass().isAnnotationPresent(ConcurrentReadable.class) )
		{
			PostingIndex<?> dir = index.getDirectIndex();
			logger.debug("Upgrading inverted index "+dir.getClass().getName()+" to be concurrent");
			if (dir instanceof BitPostingIndex)
			{
				//NB: this does not add the @ConcurrentReadable annotation
				ConcurrentBitPostingIndexUtilities.makeConcurrent((BitPostingIndex)dir, newDoi);
			}
			else
			{
				throw new IllegalArgumentException("Cannot make a " + dir + " concurrent compatible");
			}
		}
		
		if (index.hasIndexStructure("lexicon") && ! index.getLexicon().getClass().isAnnotationPresent(ConcurrentReadable.class) )
		{
			Lexicon<String> oldLex = index.getLexicon();
			logger.debug("Upgrading lexicon index "+oldLex.getClass().getName()+" to be concurrent");
			Lexicon<String> newLex = new ConcurrentLexicon(oldLex);
			IndexUtil.forceStructure(index, "lexicon", newLex);
			assert newLex.getClass().isAnnotationPresent(ConcurrentReadable.class);
		}
		
		if (index.hasIndexStructure("meta") && ! index.getMetaIndex().getClass().isAnnotationPresent(ConcurrentReadable.class) )
		{
			MetaIndex oldmeta = index.getMetaIndex();
			logger.debug("Upgrading meta index "+oldmeta.getClass().getName()+" to be concurrent");
			logger.debug(String.valueOf(index.getMetaIndex().getClass().isAnnotationPresent(ConcurrentReadable.class)));
			MetaIndex newmeta = new ConcurrentMetaIndex(oldmeta);
			assert newmeta.getClass().isAnnotationPresent(ConcurrentReadable.class);
			IndexUtil.forceStructure(index, "meta", newmeta);
		}
		if (index.hasIndexStructure("meta") && ! index.getMetaIndex().getClass().equals(ConcurrentDecodingMetaIndex.class) ) {
			MetaIndex oldmeta = index.getMetaIndex();
			logger.debug("Upgrading meta index "+oldmeta.getClass().getName()+" to use concurrent decoding");
			logger.debug(String.valueOf(index.getMetaIndex().getClass().isAnnotationPresent(ConcurrentReadable.class)));
			MetaIndex newmeta = new ConcurrentDecodingMetaIndex(oldmeta);
			assert newmeta.getClass().isAnnotationPresent(ConcurrentReadable.class);
			IndexUtil.forceStructure(index, "meta", newmeta);
		}
		
		return index;
	}
	
}
