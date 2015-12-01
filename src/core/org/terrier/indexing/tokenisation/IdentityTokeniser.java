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
 * The Original Code is IdentityTokeniser.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.indexing.tokenisation;

import java.io.IOException;
import java.io.Reader;

/**
 * A Tokeniser implementation that returns the input as is.
 * 
 * @author Craig Macdonald
 */
public class IdentityTokeniser extends Tokeniser {

	@Override
	public TokenStream tokenise(Reader reader) {
		final StringBuilder sb = new StringBuilder();
		int ch = 0;
		try{
			while((ch = reader.read()) != -1)
			{
				sb.append((char)ch);
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		final String s = sb.toString();
		return new TokenStream() {
			
			boolean hasNext = true;
			
			@Override
			public String next() {
				hasNext = false;
				return s;
			}
			
			@Override
			public boolean hasNext() {
				return hasNext;
			}
		};
	}

}
