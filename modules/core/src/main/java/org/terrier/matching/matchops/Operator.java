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
 * The Original Code is Operator.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.matching.matchops;

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
/** A match op is a possible query "term" in the query 
 * @since 5.0
 */
public abstract class Operator implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	protected static boolean IGNORE_LOW_IDF_TERMS = Boolean.parseBoolean(ApplicationSetup.getProperty("ignore.low.idf.terms","false"));
	
	
	@Override
	public abstract String toString();
	
	/** get an entry for matching for this query op. calls getPostingIterator() internally */
	public abstract MatchingEntry getMatcher(MatchingQueryTerms.QueryTermProperties qtp, Index index, Lexicon<String> lex, PostingIndex<Pointer> inv, CollectionStatistics collStats) throws IOException;

	@Override
	public boolean equals(Object _o) {
		if (! (_o instanceof Operator))
			return false;
		Operator o = (Operator)_o;
		return o.toString().equals(this.toString());
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override 
	public Operator clone()
	{
		try{
			return (Operator) super.clone();
		} catch (CloneNotSupportedException cnse){
			//this is cloneable, cannot happen
			return null;
		}
	}
	
	/** get posting iterator for this query op. */
	public abstract Pair<EntryStatistics,IterablePosting> getPostingIterator(Index index) throws IOException;
	
	public static Operator parse(String stringRep) {
		if (! stringRep.startsWith("#"))
		{
			return new SingleTermOp(stringRep);
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
		if (prefix.startsWith(ANDQueryOp.STRING_PREFIX))
		{
			return new ANDQueryOp(terms);
		}
//		if (insideBracket.startsWith(DateRangeTerm.STRING_PREFIX))
//		{
//			return new DateRangeTerm(terms);
//		}
		if (prefix.startsWith(PhraseOp.STRING_PREFIX))
		{
			return new PhraseOp(terms);
		}
		if (prefix.startsWith(SynonymOp.STRING_PREFIX))
		{
			return new SynonymOp(terms);
		}
		if (prefix.startsWith(UnorderedWindowOp.STRING_PREFIX))
		{
			int distance = Integer.parseInt( prefix.replaceFirst(UnorderedWindowOp.STRING_PREFIX, "") );
			return new UnorderedWindowOp(terms, distance);
		}
		throw new IllegalArgumentException("Unsupported operator " + prefix);
	}
}
