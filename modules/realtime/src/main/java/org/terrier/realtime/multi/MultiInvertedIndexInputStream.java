package org.terrier.realtime.multi;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.Index;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.Pointer;
import org.terrier.structures.IndexUtil;
import java.util.Iterator;
import java.util.Map;
import java.io.IOException;

/** This is not a streaming implementation */
public class MultiInvertedIndexInputStream implements PostingIndexInputStream {

    Iterator<Map.Entry<String,LexiconEntry>> iterLex;
    Index mindex;
    LexiconEntry le;

    public MultiInvertedIndexInputStream(Iterator<Map.Entry<String,LexiconEntry>> iterLex, Index mindex) {
        this.mindex = mindex;
        this.iterLex = iterLex;
    }

    public IterablePosting getNextPostings() throws IOException {
        if (! iterLex.hasNext())
        {
            return null;
        }
        String t = iterLex.next().getKey();
        // this is inefficient 
        le = mindex.getLexicon().getLexiconEntry(t);
        return mindex.getInvertedIndex().getPostings(le);
    }

    public boolean hasNext() {
        return iterLex.hasNext();
    }

    public IterablePosting next() {
        try{
            String t = iterLex.next().getKey();
            // this is inefficient 
            le = mindex.getLexicon().getLexiconEntry(t);
            return mindex.getInvertedIndex().getPostings(le);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /** Returns the number of postings in the current IterablePosting object */
    public int getNumberOfCurrentPostings() {
        return le.getNumberOfEntries();
    }
	
    /** Returns the pointer associated with the current postings being accessed */
    public Pointer getCurrentPointer() {
        return le;
    }

    public int getEntriesSkipped() {
        return 0;
    }

    public void close() throws IOException {
        IndexUtil.close(iterLex);
    }
}
