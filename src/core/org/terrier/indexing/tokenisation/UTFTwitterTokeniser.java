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
 * The Original Code is UTFTwitterTokeniser.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   
 */

package org.terrier.indexing.tokenisation;

import java.io.IOException;
import java.io.Reader;

import org.terrier.utility.ApplicationSetup;

/**
 * A tokeniser designed for use on tweets. It maintains UTF-8 encoding 
 * and keeps mentions
 * 
 * @author Richard McCreadie
 * @since 4.0
 */
public class UTFTwitterTokeniser extends Tokeniser {

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
	
	static class UTFTokenStream extends TokenStream
	{
		int ch;
		boolean eos = false;
		int counter = 0;
		Reader br;

		public UTFTokenStream(Reader _br)
		{
			this.br = _br;			
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
					while ( ch != -1 && !(ch=='/') && !(ch=='@') &&  ! (Character.isLetterOrDigit((char)ch) || Character.getType((char)ch) == Character.NON_SPACING_MARK || Character.getType((char)ch) == Character.COMBINING_SPACING_MARK)
						) 
						 /* removed by Craig: && ch != '<' && ch != '&' */
			
					{
						ch = br.read();
						counter++;
					}
					StringBuilder sw = new StringBuilder(MAX_TERM_LENGTH);
					//now accept all alphanumeric charaters
					while (ch != -1 && (Character.isLetterOrDigit((char)ch) || Character.getType((char)ch) == Character.NON_SPACING_MARK || Character.getType((char)ch) == Character.COMBINING_SPACING_MARK || ch=='/' || ch=='@'))
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
		return new UTFTokenStream(reader);
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
			if (Character.isDigit(chNew))
				counterdigit++;
			if (ch == chNew)
				counter++;
			else
				counter = 1;
			ch = chNew;
			/* if it contains more than 4 consequtive same letters,
			   or more than 4 digits, then discard the term. */
			if (counter > maxNumOfSameConseqLettersPerTerm
				|| counterdigit > maxNumOfDigitsPerTerm)
				return "";
		}
		return LOWERCASE ? s.toLowerCase() : s;
	}
}
