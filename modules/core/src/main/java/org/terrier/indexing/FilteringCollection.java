package org.terrier.indexing;

import java.io.IOException;
class FilteringCollection implements Collection {

    Collection parent;
    int startOffset;
    int each;
    boolean EOC = false;
    boolean first = true;

    FilteringCollection(Collection _parent, int _startOffset, int _each) {
        this.parent = _parent;
        this.startOffset = _startOffset;
        this.each = _each;
        firstMove();
    }

    void firstMove() {
        if (startOffset > 0)
            for(int i=0;i<startOffset;i++)
            {
                //System.err.println(this+ " firstMove i=" + i);
                if (! parent.nextDocument()) {
                    EOC = true;
                    return;
                }
            }
        first = true; 
    }
    
    public boolean nextDocument() {
        if (EOC)
            return false;
        if (first)
        {
            first = false;
            return parent.nextDocument();
        }
        for(int i=0;i<each;i++) {
            //System.err.println(this+ " nextDocument i=" + i);
            boolean parentNext = parent.nextDocument();
            if (! parentNext)
            {
                EOC = true;
                return false;
            }
        }
        
        return true;
    }

    public String toString() {
        return this.getClass().getSimpleName()+ "(" + parent.toString() + ", " + startOffset + ", " + each + ")";
    }
	
	public Document getDocument() {
        return parent.getDocument();
    }
	
	public boolean endOfCollection() {
        return EOC || parent.endOfCollection();
    }

    public void reset() {
        parent.reset();
        firstMove();
    }

    public void close() throws IOException {
        parent.close();
    }
}
