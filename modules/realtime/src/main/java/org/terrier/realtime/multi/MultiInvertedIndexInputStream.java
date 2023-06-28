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
        
        // List<Iterator<Map.Entry<String,LexiconEntry>> iterLexs;
        // List<Iterator<Map.Entry<Map.Entry<String,LexiconEntry>,Integer>> iterLexsOffset = new ArrayList<>();
        // for (int i=0;i<iterLexs.size();i++) {
        //     iterLexsOffset.add(IteratorUtils.addOffset(iterLexs.get(i), i));
        // }
        
        // var lexIterOffset = IteratorUtils.merge(  
		// 		// comparator
		// 		(Map.Entry<Map.Entry<String,LexiconEntry>,Integer> term1, Map.Entry<Map.Entry<String,LexiconEntry>,Integer> term2) -> term1.getKey().getKey().compareTo(term2.getKey().getKey()), 
		// 		// merger
		// 		(Map.Entry<Map.Entry<String,LexiconEntry>,Integer> term1, Map.Entry<Map.Entry<String,LexiconEntry>,Integer> term2) -> new MapEntry(  
		// 			term1.getKey(), 
		// 			makeMLE(term1.getKey().getValue(), term2.getLey().getValue(), term1.getValue(), term2.getValue() )),
		// 		// iterators
		// 		(Iterator<Map.Entry<Map.Entry<String,LexiconEntry>>,Integer>[]) iters.toArray(new Iterator<?>[iters.size()]));
    //}

    // MultiLexiconEntry makeMLE(LexiconEntry le_a, LexiconEntry le_b, int pos_a, int pos_b) {
    //     LexiconEntry children = new LexiconEntry[offset.length];
    //     children[pos_a] = le_a;
    //     children[pos_b] = le_b;
    //     return new MultiLexiconEntry(children, 0);
    // }

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
