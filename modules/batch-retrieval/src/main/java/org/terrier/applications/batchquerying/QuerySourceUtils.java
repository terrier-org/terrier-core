package org.terrier.applications.batchquerying;

import org.terrier.indexing.tokenisation.Tokeniser;

public class QuerySourceUtils {

	public static QuerySource create(String[] qids, String[] qs, boolean tokenise) {
		return create(qids, qs, tokenise ? Tokeniser.getTokeniser() : null);
	}
	
	public static QuerySource create(String[] qids, String[] qs, Tokeniser tok) {
		return new QuerySource() {

			int i=-1;
			@Override
			public boolean hasNext() {
				return i < qids.length -1;
			}

			@Override
			public String next() {
				i++;
				String q = qs[i];
				if (tok != null)
				{
					q = String.join(" ", tok.getTokens(q));
				}
				return q;
			}

			@Override
			public String getQueryId() {
				return qids[i];
			}

			@Override
			public void reset() {
				i = -1;
			}

			@Override
			public String[] getInfo() {
				return new String[]{"DirectQuerySource"};
			}
			
		};
	}
	
}
