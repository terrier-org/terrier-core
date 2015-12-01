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
 * The Original Code is EnglishTokeniser.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati (original author)
 *   Ben He
 *   Vassilis Plachouras
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   
 */
package org.terrier.indexing.tokenisation;

import java.io.IOException;
import java.io.Reader;

import org.terrier.utility.ApplicationSetup;

/** Tokenises text obtained from a text stream assuming English language.
 * Acceptable characters are A-Z a-z and 0-9. All other 
 * characters cause a new token.
 * <p>
 * Furthermore, there is an additional checking of terms, to reduce
 * index noise, as follows:
 * <ol>
 * <li> Any term which is longer than <tt>max.term.length</tt> (usually
 * 20 characters) is discarded.</li>
 * <li> Any term which has more than 4 digits is discarded.</li>
 * <li> Any term which has more than 3 consecutive identical 
 * characters are discarded. </li>
 * </ol>
 * 
 * <b>Properties:</b>
 * <ul>
 * <li><tt>lowercase</tt> - should all terms be lowercased or not?
 * <li><tt>max.term.length</tt> - maximum acceptable term length, default is 20.
 * </ul>
 * 
 * @author Gianni Amati, Ben He, Vassilis Plachouras, Craig Macdonald
 */
public class EnglishTokeniser extends Tokeniser {

	/** The maximum number of digits that are allowed in valid terms. */
	protected final static int maxNumOfDigitsPerTerm = 4;
	/**
	 * The maximum number of consecutive same letters or digits that are
	 * allowed in valid terms.
	 */
	protected final static int maxNumOfSameConseqLettersPerTerm = 3;
	/**
	 * Whether tokens longer than MAX_TERM_LENGTH should be dropped.
	 */
	protected final static boolean DROP_LONG_TOKENS = true;
	
	static final boolean LOWERCASE = Boolean.parseBoolean(ApplicationSetup.getProperty("lowercase", "true"));
	static final int MAX_TERM_LENGTH = ApplicationSetup.MAX_TERM_LENGTH;
	
	static class EnglishTokenStream extends TokenStream
	{
		int ch;
		boolean eos = false;
		int counter = 0;
		Reader br;

		public EnglishTokenStream(Reader _br)
		{
			this.br = _br;
			if (this.br == null)
			{
				this.eos = true;
			}
		}
		
		@Override
		public boolean hasNext() {
			return ! eos;
		}
		
		@Override
		public String next() 
		{
			try{
				ch = this.br.read();
				while(ch != -1)
				{			
					/* skip non-alphanumeric charaters */
					while (ch != -1 && (ch < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z')
						&& (ch < '0' || ch > '9') 
						 /* removed by Craig: && ch != '<' && ch != '&' */
					) 
					{
						ch = br.read();
						counter++;
					}
					final StringBuilder sw = new StringBuilder(MAX_TERM_LENGTH);
					//now accept all alphanumeric charaters
					while (ch != -1 && (
						((ch >= 'A') && (ch <= 'Z'))
						|| ((ch >= 'a') && (ch <= 'z'))
						|| ((ch >= '0') && (ch <= '9'))))
					{
						/* add character to word so far */
						sw.append((char)ch);
						ch = br.read();
						counter++;
					}
					if (sw.length() > MAX_TERM_LENGTH)
						if (DROP_LONG_TOKENS)
							return null;
						else
							sw.setLength(MAX_TERM_LENGTH);
					String s = check(sw.toString());
					if (s.length() > 0)
						return s;
				}
				eos = true;
				return null;
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		
	}
	
	@Override
	public TokenStream tokenise(final Reader reader) {
		return new EnglishTokenStream(reader);
	}

	/**
	 * Checks whether a term is shorter than the maximum allowed length,
	 * and whether a term does not have many numerical digits or many
	 * consecutive same digits or letters.
	 * @param s String the term to check if it is valid.
	 * @return String the term if it is valid, otherwise it returns null.
	 */
	static String check(String s) {
		//if the s is null
		//or if it is longer than a specified length
		s = s.trim();
		final int length = s.length();
		int counter = 0;
		int counterdigit = 0;
		int ch = -1;
		int chNew = -1;
		for(int i=0;i<length;i++)
		{
			chNew = s.charAt(i);
			if (chNew >= 48 && chNew <= 57)
				counterdigit++;
			if (ch == chNew)
				counter++;
			else
				counter = 1;
			ch = chNew;
			/* if it contains more than 3 consequtive same letters,
			   or more than 4 digits, then discard the term. */
			if (counter > maxNumOfSameConseqLettersPerTerm
				|| counterdigit > maxNumOfDigitsPerTerm)
				return "";
		}
		return LOWERCASE ? s.toLowerCase() : s;
	}

}
