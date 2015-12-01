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
 * The Original Code is BasicShakNullRecompress.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */

package org.terrier.integer.tests;

import org.terrier.applications.InvertedIndexRecompresser;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.tests.BasicShakespeareEndToEndTest;
import org.terrier.tests.BatchEndToEndTest;

public class BasicShakNullRecompress extends BasicShakespeareEndToEndTest {

	static class DoRecompress extends BatchEndToEndTestEventHooks
	{
		@Override
		public void finishedIndexing(BatchEndToEndTest test) throws Exception {
			IndexOnDisk.setIndexLoadingProfileAsRetrieval(false);
			IndexOnDisk index = Index.createIndex();
			InvertedIndexRecompresser.recompressInverted(index);
			index.close();
		}
		
	}
	
	public BasicShakNullRecompress()
	{
		super.testHooks.add(new DoRecompress());
	}
	
	
}
