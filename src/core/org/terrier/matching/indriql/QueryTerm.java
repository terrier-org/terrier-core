package org.terrier.matching.indriql;

import java.io.IOException;
import java.io.Serializable;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.utility.ApplicationSetup;

public abstract class QueryTerm implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	protected static boolean IGNORE_LOW_IDF_TERMS = Boolean.parseBoolean(ApplicationSetup.getProperty("ignore.low.idf.terms","true"));
	
	
	@Override
	public abstract String toString();
	
	public abstract MatchingEntry getMatcher(MatchingQueryTerms.QueryTermProperties qtp, Index index, Lexicon<String> lex, PostingIndex<Pointer> inv, CollectionStatistics collStats) throws IOException;

	@Override
	public boolean equals(Object _o) {
		if (! (_o instanceof QueryTerm))
			return false;
		QueryTerm o = (QueryTerm)_o;
		return o.toString().equals(this.toString());
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override 
	public QueryTerm clone()
	{
		try{
			return (QueryTerm) super.clone();
		} catch (CloneNotSupportedException cnse){
			//this is cloneable, cannot happen
			return null;
		}
	}
	
}
