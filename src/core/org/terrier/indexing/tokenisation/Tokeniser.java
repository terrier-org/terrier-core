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
 * The Original Code is UTFTokeniser.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Rodrygo Santos <rodrgyo{a.}dcs.gla.ac.uk>
 */
package org.terrier.indexing.tokenisation;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.terrier.utility.ApplicationSetup;

/** 
 * A tokeniser class is responsible for tokenising a block of text.
 * It is expected that no markup is present in this text. Input
 * is usually a Reader, while output is in the form of a TokenStream.
 * Tokenisers are typically used by {@link org.terrier.indexing.Document}
 * implementations.
 * <p>
 * <b>Available tokenisers</b>
 * There are two default tokenisers shipped with Terrier, namely
 * <a href="EnglishTokeniser.html">EnglishTokeniser</a> (default, only accepts
 * A-Z, a-z and 0-9 as valid characters. Everything else causes a token boundary), 
 * and <a href="UTFTokeniser.html">UTFTokeniser</a>.
 * The tokeniser used by default can be specified using the
 * <tt>tokeniser</tt> property.
 * <p>
 * <b>Properties:</b>
 * <ul>
 * <li><tt>tokeniser</tt> - name of the tokeniser class to use.
 * </ul>
 * <p>
 * <b>Example:</b>
 * <pre>
 * Tokeniser tokeniser = Tokeniser.getTokeniser();
 * TokenStream toks = tokeniser.tokenise(new StringReader("This is a block of text."));
 * while(toks.hasNext())
 * {
 *   System.out.println(toks.next());
 * }
 * </pre>
 * @author Craig Macdonald &amp; Rodrygo Santos
 * @since 3.5
 * @see TokenStream
 * @see EnglishTokeniser
 * @see UTFTokeniser
 */
public abstract class Tokeniser {

	/**
	 * empty stream
	 */
	public static final TokenStream EMPTY_STREAM = new TokenStream()
	{
		@Override
		public final boolean hasNext() {
			return false;
		}

		@Override
		public final String next() {
			return null;
		}		
	};
	
	/** Instantiates Tokeniser class named in the <tt>tokeniser</tt> property.
	 * @return Named tokeniser class from <tt>tokeniser</tt> property.
	 */
	public static Tokeniser getTokeniser()
	{
		//TODO: add filter
		String tokeniserClassName = ApplicationSetup.getProperty("tokeniser", EnglishTokeniser.class.getName());
		if (! tokeniserClassName.contains("."))
			tokeniserClassName = "org.terrier.indexing.tokenisation." + tokeniserClassName;
		Tokeniser rtr;
		try{
			rtr = Class.forName(tokeniserClassName).asSubclass(Tokeniser.class).newInstance();
		}catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		return rtr;
	}
	
	/** Tokenises the text obtained from the specified reader.
	 * @param reader Stream of text to be tokenised
	 * @return a TokenStream of the tokens found in the text.
	 */
	public abstract TokenStream tokenise(Reader reader);
	
	/** Utility method which returns all of the tokens for a given
	 * stream.
	 * @param reader Stream of text to be tokenised
	 * @return All of the tokens found in the stream of text.
	 * @throws IOException
	 */
	public String[] getTokens(Reader reader) throws IOException
	{
		List<String> tokens = new ArrayList<String>();
		Iterator<String> iter = tokenise(reader);
		while(iter.hasNext())
		{	
			String t = iter.next();
			if (t != null)
				tokens.add(t);
		}
		return tokens.toArray(new String[tokens.size()]);	
	}	
}
