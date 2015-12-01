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
 * The Original is in 'MSExcelDocument.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.indexing;

import java.io.InputStream;
import java.util.Map;

import org.terrier.indexing.tokenisation.Tokeniser;

/** MSExcelDocument is a placeholder class - all functionality is implemented
 * in {@link POIDocument}. This class will be removed in a future
 * release of Terrier. 
 */
@Deprecated
public class MSExcelDocument extends POIDocument {

	public MSExcelDocument(InputStream docStream,
			Map<String, String> docProperties, Tokeniser tok) {
		super(docStream, docProperties, tok);
	}

	public MSExcelDocument(String filename, InputStream docStream,
			Tokeniser tokeniser) {
		super(filename, docStream, tokeniser);
	}

}
