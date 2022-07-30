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
 * The Original Code is WhitespaceTokeniser.java.
 *
 * The Original Code is Copyright (C) 2004-2022 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.indexing.tokenisation;

import java.io.IOException;
import java.io.Reader;

/**
 * A Tokeniser implementation that tokenises only on whitespace, i.e.
 * as defined by Character.isWhitespace(int) method in the JRE.
 * 
 * @author Craig Macdonald
 */
@SuppressWarnings("serial")
public class WhitespaceTokeniser extends Tokeniser {

	@Override
	public TokenStream tokenise(final Reader reader) {
		if (reader == null) {
			return Tokeniser.EMPTY_STREAM;
		}
		final StringBuilder sb = new StringBuilder();
		return new TokenStream() {
			
			boolean hasNext = true;
			
			@Override
			public String next() {
				try{
					int ch = 0;
					while((ch = reader.read()) != -1) {
						if (Character.isWhitespace(ch)) {
							if (sb.length() > 0) {
								String rtr = sb.toString();
								sb.setLength(0);
								return rtr;
							}
						}
						else {
							sb.append((char)ch);
						}
					}
					if (sb.length() > 0) {
						String rtr = sb.toString();
						sb.setLength(0);
						return rtr;
					}
					hasNext = false;
					return null;
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
			
			@Override
			public boolean hasNext() {
				return hasNext;
			}
		};
	}

}
