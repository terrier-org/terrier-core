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
