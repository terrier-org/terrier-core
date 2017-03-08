package org.terrier.matching.indriql;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang3.tuple.Pair;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.ApplicationSetup;

public abstract class QueryTerm implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	protected static boolean IGNORE_LOW_IDF_TERMS = Boolean.parseBoolean(ApplicationSetup.getProperty("ignore.low.idf.terms","true"));
	
	
	@Override
	public abstract String toString();
	
	/** get an entry for matching for this query op. calls getPostingIterator() internally */
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
	
	/** get posting iterator for this query op. */
	public abstract Pair<EntryStatistics,IterablePosting> getPostingIterator(Index index) throws IOException;
	
	public static QueryTerm parse(String stringRep) {
		if (! stringRep.startsWith("#"))
		{
			return new SingleQueryTerm(stringRep);
		}
		int firstBracket = stringRep.indexOf('(');
		int lastBracket = stringRep.lastIndexOf(')');
		String prefix = stringRep.substring(0, firstBracket-1);
		String insideBracket = stringRep.substring(firstBracket+1, lastBracket-1);
		if (insideBracket.contains("#"))
		{
			throw new IllegalArgumentException("Recursive parsing not supported!");
		}
		String[] terms = insideBracket.split("\\s+");
		if (prefix.startsWith(ANDQueryTerm.STRING_PREFIX))
		{
			return new ANDQueryTerm(terms);
		}
//		if (insideBracket.startsWith(DateRangeTerm.STRING_PREFIX))
//		{
//			return new DateRangeTerm(terms);
//		}
		if (prefix.startsWith(PhraseTerm.STRING_PREFIX))
		{
			return new PhraseTerm(terms);
		}
		if (prefix.startsWith(SynonymTerm.STRING_PREFIX))
		{
			return new SynonymTerm(terms);
		}
		if (prefix.startsWith(UnorderedWindowTerm.STRING_PREFIX))
		{
			int distance = Integer.parseInt( prefix.replaceFirst(UnorderedWindowTerm.STRING_PREFIX, "") );
			return new UnorderedWindowTerm(terms, distance);
		}
		throw new IllegalArgumentException("Unsupported operator " + prefix);
	}
}
