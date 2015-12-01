/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is LexiconUtil.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
/** 
 * Lexicon utilities class. Enables the printing of the lexicon.
 */
public class LexiconUtil {
	/** 
	 * Prints Lexicon
	 * @param index
	 * @param structureName
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void printLexicon(Index index, String structureName) throws IOException
	{
		Iterator<Map.Entry<?,LexiconEntry>> lexiconStream = 
			(Iterator<Map.Entry<?,LexiconEntry>>)index.getIndexStructureInputStream(structureName);
		while (lexiconStream.hasNext())
		{
			Map.Entry<?, LexiconEntry> lee = lexiconStream.next();
			System.out.println(lee.getKey().toString()+","+lee.getValue().toString());
		}
		IndexUtil.close(lexiconStream);
	}
}
