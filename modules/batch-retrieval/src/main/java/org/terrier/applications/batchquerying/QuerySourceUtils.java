package org.terrier.applications.batchquerying;

import java.util.Arrays;
import java.util.Iterator;

import org.terrier.indexing.tokenisation.Tokeniser;

public class QuerySourceUtils
{
	public static QuerySource create(String[] qids, String[] queries, Tokeniser tok) {
		return new QuerySource(){
	        
            Iterator<String> parent = Arrays.asList(queries).iterator();
            int offset = -1;

            @Override
            public String next() {
                offset++;
                String q = parent.next();
                if (tok != null)
                    q = String.join(" ", tok.getTokens(q));
                return q;
            }
        
            @Override
            public boolean hasNext() {
                return parent.hasNext();
            }
        
            @Override
            public void reset() {
                parent = Arrays.asList(queries).iterator();
                offset = -1;
            }
        
            @Override
            public String getQueryId() {
                return qids[offset];
            }
        
            @Override
            public String[] getInfo() {
                return new String[]{"directQueries"}; 
            }
        };
	}
    
    public static QuerySource create(String[] qids, String[] queries, boolean tokenise)
    {
    	return create(qids, queries, tokenise ? Tokeniser.getTokeniser() : null);
    }
}