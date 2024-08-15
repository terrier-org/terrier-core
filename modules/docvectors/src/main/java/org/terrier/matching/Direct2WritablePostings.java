package org.terrier.matching;

import gnu.trove.TIntIntHashMap;

import java.util.Set;
import org.terrier.structures.Index;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.Lexicon;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.FieldEntryStatistics;

import org.terrier.learning.*;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.structures.postings.FieldPosting;
import java.io.IOException;

/** This class makes a FatResultSet from a DirectIndex. This can be used to facilitate further scoring */
public class Direct2WritablePostings {

    static FatResultSet decorateFromDirect(ResultSet rs, Index index, MatchingQueryTerms mqt) throws IOException {
        Set<String>[] ts = new Set[mqt.length()];
        for (int i=0;i<ts.length;i++)
        {
            ts[i] = mqt.get(mqt.getTerms()[i]).getValue().getTags();
        }
        return decorateFromDirect(rs, index, mqt.getTerms(), mqt.getTermWeights(), ts);
    }

    static FatResultSet decorateFromDirect(ResultSet rs, Index index, String[] qs, double[] ks, Set<String>[] ts)  throws IOException {
        assert index.hasIndexStructure("direct");

        final DocumentIndex doi = index.getDocumentIndex();
        final PostingIndex<?> direct = index.getDirectIndex();
        final Lexicon<String> lex = index.getLexicon();
        final int[] docids = rs.getDocids();
        final int l = docids.length;
        final int t = ks.length;
        final int fieldCount = index.getCollectionStatistics().getNumberOfFields();
        
        final EntryStatistics[] entryStats = new EntryStatistics[t];
        final WritablePosting[][] postings = new WritablePosting[l][t];        
        final boolean[] fields = new boolean[t];
        final TIntIntHashMap termIds = new TIntIntHashMap(t);
        for (int i=0; i<t; i++) {
            LexiconEntry le = lex.getLexiconEntry(qs[i]);
			entryStats[i] = le.getWritableEntryStatistics();
            fields[i] = entryStats[i] instanceof FieldEntryStatistics;
            termIds.put(le.getTermId(), i+1);
		}

        for (int i=0;i<l;i++) {
            IterablePosting ip = direct.getPostings(doi.getDocumentEntry(docids[i]));
            while (ip.next() != IterablePosting.EOL) {
                int termOffset = termIds.get(ip.getId());
                if (termOffset == 0)
                    continue;
                termOffset--;
                WritablePosting wp = postings[i][termOffset] = ip.asWritablePosting();
                wp.setDocumentLength(ip.getDocumentLength());
                if (fields[termOffset])
                {
                    final int[] fieldLengths =  ((FieldPosting)ip).getFieldLengths();
                    final int[] newFieldLengths = new int[fieldCount];
                    System.arraycopy(fieldLengths, 0, newFieldLengths, 0, fieldCount);
                    // assert fieldLengths.length == super.cs.getNumberOfFields() 
                    //     : " posting "+p +" for docid " + ip.getId() + " has wrong number of fields for length";
                    ((FieldPosting)wp).setFieldLengths(newFieldLengths);
                }
            }
        }
        FatQueryResultSet fat = new FatQueryResultSet(l, index.getCollectionStatistics(), qs, entryStats, ks, ts);
        fat.setPostings(postings);
        fat.setDocids(docids);
        fat.setScores(rs.getScores());
        return fat;
    }
    

}